package com.pink.pfa.services;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pink.pfa.models.AdoptionSite;
import com.pink.pfa.models.Pet;
import com.pink.pfa.models.datatransfer.ScrapedPetDTO;

import jakarta.persistence.NoResultException;

/**
 * Service responsible for scraping pet adoption websites.
 * <p>
 * This class leverages:
 * <ul>
 * <li>Selenium RemoteWebDriver for rendering JavaScript-heavy frameworks (React/Next.js)</li>
 * <li>Jsoup for parsing static and hydrated HTML source</li>
 * <li>Custom JavaScript execution for Shadow DOM and pet-scroller widget interaction</li>
 * <li>BFS-style traversal for site-wide link discovery</li>
 * </ul>
 *
 * Support includes providers like ShelterLuv and Petfinder widgets.
 */
@Service
public class WebScraperService {
    private static final Logger log = LoggerFactory.getLogger(WebScraperService.class);

    public WebScraperService() {}

    /**
     * Enum representing supported pet listing display providers.
     */
    enum PetDisplayMethod {
        SHELTER_LUV,    /** ShelterLuv embedded pet listing system */
        PETFINDER,      /** PetFinder embedded pet listing system */
        UNSUPPORTED     /** Unsupported listing structure */
    };

    /**
     * Extracts dynamic URLs from a given page using Selenium and Jsoup.
     * <p>
     * For Petfinder widgets, this method performs specialized Shadow DOM traversal
     * and handles internal pagination within the pet-scroller component.
     *
     * @param url The URL to inspect.
     * @param iframeClass Filter for identifying specific provider iframes.
     * @param anchorClass Filter for identifying specific pet profile links.
     * @param driver Active Selenium WebDriver instance.
     * @return A list of discovered URLs (e.g., direct animal profile links).
     */
    List<String> FindURLS(String url, String iframeClass, String anchorClass, WebDriver driver) {
        List<String> dynamicUrls = new ArrayList<>();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(100));

        driver.get(url);

        // Waits until any dynamically loaded anchor tags are found or until 100 milliseconds has past
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("a")));
        } catch (TimeoutException e) {
            System.out.println("No anchors could be found");
        }

        // Waits until any dynamically loaded iframe tags are found or until 100 milliseconds has past
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("iframe")));
        } catch (TimeoutException e) {
            System.out.println("No iframes could be found");
        }

        // Finds any anchor tags and adds their href links to the list of urls
        Document doc = Jsoup.parse(driver.getPageSource());
        Elements anchors = doc.select("a").stream()
            .filter(anchor -> anchor.attr("class").contains(anchorClass))
            .collect(Collectors.toCollection(Elements::new));
        for (Element anchor : anchors) {
            dynamicUrls.add(anchor.attr("href"));
        }

        // Finds any iframe tags and adds their associated links to the list of urls
        List<WebElement> iframes = driver.findElements(By.tagName("iframe")).stream()
            .filter(iframe -> iframe.getAttribute("class").contains(iframeClass))
            .collect(Collectors.toCollection(ArrayList::new));

        PetDisplayMethod displayType = FindPetDisplayMethod(url);
        for (WebElement iframe : iframes) {
            switch (displayType) {
                case PetDisplayMethod.SHELTER_LUV: {
                    dynamicUrls.add(iframe.getAttribute("src"));
                    break;
                }
                case PetDisplayMethod.PETFINDER: {
                    try {
                        // If the iframe is a petfinder site it needs to be fully rendered in order to get its content
                        // We wait until the petfinder scroller widget has loaded and then switch to it
                        WebElement petIframe = wait.until(ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector("iframe[srcdoc*='pet-scroller']")));
                        driver.switchTo().frame(petIframe);

                        // After switching we wait until the custom pet-scroller tag that contains all of the pet cards has loaded
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("pet-scroller")));

                        boolean hasNextPage = true;
                        while (hasNextPage) {
                            // Runs a JavaScript snippet that attempts to find at least one of the pet cards
                            // If the pet scroller cannot be found or if its shadow DOM has not loaded then we assume that the cards have also not loaded
                            boolean cardsLoaded = (Boolean) ((JavascriptExecutor) driver).executeScript(
                                "const host = document.querySelector('pet-scroller');" +
                                "if (!host || !host.shadowRoot) return false;" +
                                "return host.shadowRoot.querySelectorAll('a.petCard').length > 0;"
                            );

                            // In the event that the cards had not loaded yet, we retry a few times before giving up
                            int retries = 0;
                            while (!cardsLoaded && retries < 10) {
                                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.petCard")));
                                cardsLoaded = (Boolean) ((JavascriptExecutor) driver).executeScript(
                                    "return document.querySelector('pet-scroller').shadowRoot.querySelectorAll('a.petCard').length > 0;"
                                );
                                retries++;
                            }

                            // This JavaScript snippet will find all of the pet cards and pull out their href links
                            String getUrlsScript = 
                                "return Array.from(document.querySelector('pet-scroller').shadowRoot" +
                                ".querySelectorAll('a.petCard'))" +
                                ".map(a => a.href);";
                            
                            @SuppressWarnings("unchecked")
                            List<String> petAnchors = (List<String>) ((JavascriptExecutor) driver).executeScript(getUrlsScript);
                            
                            // Adds the new links to the list
                            for (String petAnchor : petAnchors) {
                                if (!dynamicUrls.contains(petAnchor)) {
                                    dynamicUrls.add(petAnchor);
                                }
                            }

                            // Clicks the Next button on the page to load more pets from the widget
                            // If there is not pet button to click then we can assume that their are no more pets to find and the loop terminates
                            String clickNextScript = 
                                "const host = document.querySelector('pet-scroller');" +
                                "const buttons = Array.from(host.shadowRoot.querySelectorAll('button'));" +
                                "const nextBtn = buttons.find(b => b.textContent.trim().includes('Next'));" +
                                "if (nextBtn && !nextBtn.disabled) {" +
                                "  nextBtn.click();" +
                                "  return true;" +
                                "}" +
                                "return false;";

                            hasNextPage = (Boolean) ((JavascriptExecutor) driver).executeScript(clickNextScript);
                        }
                    } catch (TimeoutException e) {
                        System.out.println("Cannot find any more urls");
                    } finally {
                        driver.switchTo().defaultContent();
                    }
                    break;
                }
            }
        }

        return dynamicUrls;
    }

    /**
     * Determines the pet display provider by inspecting URL patterns 
     * and searching for specific provider script/iframe fingerprints in the DOM.
     *
     * @param url The URL to inspect.
     * @return The detected {@link PetDisplayMethod}, or UNSUPPORTED if no match is found.
     */
    PetDisplayMethod FindPetDisplayMethod(String url) {
        // If the url contains the shelterluv hostname then it is a shelterluv site
        if (url.contains("shelterluv.com")) {
            return PetDisplayMethod.SHELTER_LUV;
        }

        // If the url contains petfinders.com then it is a petfinders site
        if (url.contains("petfinder.com")) {
            return PetDisplayMethod.PETFINDER;
        }

        try {
            // If the doc contains any shelterluv scripts then it is a shelterluv site
            Document doc = Jsoup.connect(url).get();
            Elements shelterLuvScripts = doc.select("script").stream()
                .filter(element -> element.attr("src").contains("new.shelterluv.com"))
                .collect(Collectors.toCollection(Elements::new));

            if (shelterLuvScripts.size() > 0) {
                return PetDisplayMethod.SHELTER_LUV;
            }
        } catch (Exception e) {
            System.out.println("Failed to find document when determining if this was a shelterluv site");
        }
  
        try {
            // If the doc contains any iframes using the pet-scroller widget then it is a petfinder site
            Document doc = Jsoup.connect(url).get();
            Elements petfinderScripts = doc.select("iframe").stream()
                .filter(element -> element.attr("srcdoc").contains("</pet-scroller>"))
                .collect(Collectors.toCollection(Elements::new));

            if (petfinderScripts.size() > 0) {
                System.out.println("Petfinder Site");
                return PetDisplayMethod.PETFINDER;
            }
        } catch (Exception e) {
            System.out.println("Failed to find document when determining if this was a petfinder site");
        }

        return PetDisplayMethod.UNSUPPORTED;
    }

    /**
     * Builder interface used to construct a pet information map.
     */
    interface IPetInfoBuilder {
        /** @return Builder instance with name populated */
        PetInfoBuilder AddName();       

        /** @return Builder instance with type populated */
        PetInfoBuilder AddType();       

        /** @return Builder instance with breed populated */
        PetInfoBuilder AddBreed();      

        /** @return Builder instance with gender populated */
        PetInfoBuilder AddGender();     

        /** @return Builder instance with age populated */
        PetInfoBuilder AddAge();        

        /** @return Builder instance with size populated */
        PetInfoBuilder AddSize();       

        /** @return Builder instance with price populated */  
        PetInfoBuilder AddPrice();          
        
        /** @return Builder instance with image populated */
        PetInfoBuilder AddImage();
        
        /**
         * Builds and returns the collected pet information.
         *
         * @return Map containing scraped pet data.
         */
        Map<String, Object> Build();
    }

    /**
     * Abstract base builder containing shared logic
     * for pet information extraction.
     */
    abstract class PetInfoBuilder implements IPetInfoBuilder {
        protected String mainUrl;               /** Original url that was passed into the current scraping session */
        protected String currUrl;               /** Current url that is being scrapped */
        protected Map<String, Object> petInfo;  /** Internal storage for scraped pet data */

        /**
         * Constructs a new builder instance.
         *
         * @param mainUrl The parent listing URL.
         * @param currUrl The active scrapping URL.
         */
        public PetInfoBuilder(String mainUrl, String currUrl) {
            this.mainUrl = mainUrl;
            this.currUrl = currUrl;
            this.petInfo = new HashMap<>();
        }

        /**
         * Finalizes and returns collected pet data.
         * Clears internal state after building.
         *
         * @return Copy of pet information map.
         */
        @Override
        public Map<String, Object> Build() {
            Map<String, Object> result = new HashMap<>(petInfo);
            petInfo.clear();
            return result;
        }
    }

    /**
     * Concrete builder implementation for ShelterLuv pet pages.
     * <p>
     * Extracts:
     * <ul>
     *     <li>Name</li>
     *     <li>Type</li>
     *     <li>Breed</li>
     *     <li>Gender</li>
     *     <li>Age</li>
     *     <li>Image</li>
     * </ul>
     */
    class ShelterLuvBuilder extends PetInfoBuilder {
        Element petImage = null;    /** Pet profile image element */
        Element mainInfoDiv = null; /** Main information container element */
        WebDriver driver = null;     /** Selenium WebDriver instance */

        /**
         * Creates a ShelterLuv builder and loads the pet page.
         *
         * @param origSiteUrl Original parent URL.
         * @param currUrl Specific ShelterLuv pet URL.
         * @param webDriver Active Selenium WebDriver.
         * @throws IOException If page content cannot be loaded.
         */
        public ShelterLuvBuilder(String origSiteUrl, String currUrl, WebDriver webDriver) throws IOException {
            super(origSiteUrl, currUrl);
            this.driver = webDriver;

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(100));

            driver.get(currUrl);

            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("img")));
            } catch (TimeoutException e) {
                System.out.println("No images could be found");
            }

            Document doc = Jsoup.parse(driver.getPageSource());

            // Attempts to find the pet image associated with the current pet
            petImage = doc.select("img").stream()
                .filter(element -> element.attr("src").contains("shelterluv.com") && element.attr("src").contains("profile-pictures"))
                .collect(Collectors.toCollection(Elements::new))
                .first();

            // Attempts to find the div that contains most of the information about the pet
            mainInfoDiv = doc.select("div").stream()
                .filter(element -> element.attr("data-cy").equals("name"))
                .collect(Collectors.toCollection(Elements::new))
                .first();

            if (petImage == null) {
                System.out.println("PET IMAGE NULL");
            }

            if (mainInfoDiv == null) {
                System.out.println("MAIN INFO NULL");
            }
        }

        /** {@inheritDoc} */
        @Override
        public PetInfoBuilder AddName() {
            if (mainInfoDiv != null) {
                Element name = mainInfoDiv.children().stream()
                    .filter(element -> element.tagName().equals("h1"))
                    .collect(Collectors.toCollection(Elements::new))
                    .first();

                if (name != null) {
                    petInfo.put("Name", name.text());
                }
            }
            
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public PetInfoBuilder AddType() {
            if (mainUrl.contains("animalType=Dog")) {
                petInfo.put("Type", "Dog");
            } else if (mainUrl.contains("animalType=Cat")) {
                petInfo.put("Type", "Cat");
            } else {
                petInfo.put("Type", "Other");
            }

            return this;
        }

        /** {@inheritDoc} */
        @Override
        public PetInfoBuilder AddBreed() {
            if (mainInfoDiv != null) {
                Element breedContainer = mainInfoDiv.children().stream()
                    .filter(element -> element.text().contains("Breed"))
                    .collect(Collectors.toCollection(Elements::new))
                    .first();
                if (breedContainer == null) return this;

                Element breedLabel = breedContainer.children().stream()
                        .filter(element -> element.text().contains("Breed"))
                        .collect(Collectors.toCollection(Elements::new))
                        .first();
                if (breedLabel == null) return this;

                Element breed = breedLabel.siblingElements().first();
                if (breed != null) {
                    petInfo.put("Breed", breed.text());
                }
            }

            return this;
        }

        /** {@inheritDoc} */
        @Override
        public PetInfoBuilder AddGender() {
            if (mainInfoDiv != null) {
                Element genderContainer = mainInfoDiv.children().stream()
                    .filter(element -> element.text().contains("Sex"))
                    .collect(Collectors.toCollection(Elements::new))
                    .first();
                if (genderContainer == null) return this;

                Element genderLabel = genderContainer.children().stream()
                        .filter(element -> element.text().contains("Sex"))
                        .collect(Collectors.toCollection(Elements::new))
                        .first();
                if (genderLabel == null) return this;

                Element gender = genderLabel.siblingElements().first();
                if (gender != null) {
                    petInfo.put("Gender", gender.text().substring(0, 1));
                }
            }

            return this;
        }

        /** {@inheritDoc} */
        @Override
        public PetInfoBuilder AddAge() {
            if (mainInfoDiv != null) {
                Element ageContainer = mainInfoDiv.children().stream()
                    .filter(element -> element.text().contains("Age"))
                    .collect(Collectors.toCollection(Elements::new))
                    .first();
                if (ageContainer == null) return this;

                Element ageLabel = ageContainer.children().stream()
                        .filter(element -> element.text().contains("Age"))
                        .collect(Collectors.toCollection(Elements::new))
                        .first();
                if (ageLabel == null) return this;

                Element age = ageLabel.siblingElements().first();
                if (age != null) {
                    String formattedAge = age.text().replace("Y/", ".").replace("M/", ".").replace("W", "");
                    String[] ageComponents = formattedAge.split("\\.");
                    int ageValue = Integer.parseInt(ageComponents[0]) * 52 +
                            Integer.parseInt(ageComponents[1]) * 52 / 12 +
                            Integer.parseInt(ageComponents[2]);
                    petInfo.put("Age", ageValue);
                }
            }

            return this;
        }

        @Override
        public PetInfoBuilder AddSize() {
            if (mainInfoDiv != null) {
                Element sizeContainer = mainInfoDiv.children().stream()
                    .filter(element -> element.text().contains("Weight"))
                    .collect(Collectors.toCollection(Elements::new))
                    .first();
                if (sizeContainer == null) return this;

                Element sizeLabel = sizeContainer.children().stream()
                        .filter(element -> element.text().contains("Weight"))
                        .collect(Collectors.toCollection(Elements::new))
                        .first();
                if (sizeLabel == null) return this;

                Element size = sizeLabel.siblingElements().first();
                if (size != null) {
                    String formattedSize = size.text().replace(" lbs", "");
                    double sizeVal = Double.parseDouble(formattedSize);
                    String sizeType = "Unknown";
                    switch (petInfo.get("Type").toString()) {
                        case "Dog": {
                            if (sizeVal < 25) sizeType = "Small";
                            if (sizeVal >= 25 && sizeVal < 60) sizeType = "Medium";
                            if (sizeVal >= 60 && sizeVal < 100) sizeType = "Large";
                            if (sizeVal >= 100) sizeType = "Extra Large";
                            break;
                        }
                        case "Cat": {
                            if (sizeVal < 8) sizeType = "Small";
                            if (sizeVal >= 8 && sizeVal < 12) sizeType = "Medium";
                            if (sizeVal >= 12 && sizeVal < 15) sizeType = "Large";
                            if (sizeVal >= 15) sizeType = "Extra Large";
                            break;
                        }
                    }
                    petInfo.put("Size", sizeType);
                }
            }

            return this;
        }

        /** {@inheritDoc} */
        @Override
        public PetInfoBuilder AddPrice() {
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public PetInfoBuilder AddImage() {
            if (petImage != null) {
                petInfo.put("Image", petImage.attr("src"));
            }

            return this;
        }
    }

    /**
     * Concrete builder implementation for PetFinder profile pages.
     * <p>
     * Specifically handles:
     * <ul>
     * <li>Waiting for Swiper.js/carousel elements to hydrate</li>
     * <li>Parsing specific Petfinder section IDs (e.g., #pet-details-about-section)</li>
     * <li>Extracting breed, age, and gender from localized trait containers</li>
     * </ul>
     */
    class PetFinderBuilder extends PetInfoBuilder {
        Element petImage = null;    /** Pet profile image element */
        Element mainInfoDiv = null; /** Main information container element */
        WebDriver driver = null;     /** Selenium WebDriver instance */

        /**
         * Constructs a PetFinder builder and triggers the initial page load.
         *
         * @param origSiteUrl The parent site URL for context.
         * @param currUrl The direct Petfinder animal profile URL.
         * @param webDriver Active Selenium WebDriver for dynamic rendering.
         * @throws IOException If the connection fails or the rendered source is inaccessible.
         */
        public PetFinderBuilder(String origSiteUrl, String currUrl, WebDriver webDriver) throws IOException {
            super(origSiteUrl, currUrl);
            this.driver = webDriver;

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(100));

            driver.get(currUrl);

            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.swiper-slide-active")));
            } catch (TimeoutException e) {
                System.out.println("Cannot find pet photos");
            }

            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#pet-details-about-section")));
            } catch (TimeoutException e) {
                System.out.println("Cannot find main info div");
            }

            Document doc = Jsoup.parse(driver.getPageSource());

            // Attempts to find the pet image associated with the current pet
            try {
                petImage = doc.selectFirst("div.swiper-slide-active")
                    .select("img").stream()
                        .filter(element -> element.attr("src").contains("cloudfront.net/animal"))
                        .collect(Collectors.toCollection(Elements::new))
                        .first();
            } catch (Exception e) {
                System.out.println("Failed to get pet image");
            }

            // Attempts to find the section that contains most of the information about the pet
            mainInfoDiv = doc.select("section").stream()
                .filter(element -> element.attr("id").equals("pet-details-about-section"))
                .collect(Collectors.toCollection(Elements::new))
                .first();

            if (petImage == null) {
                System.out.println("PET IMAGE NULL");
            }

            if (mainInfoDiv == null) {
                System.out.println("MAIN INFO NULL");
            }
        }

        /** {@inheritDoc} */
        @Override
        public PetInfoBuilder AddName() {
            if (mainInfoDiv != null) {
                Element name = mainInfoDiv.select("h2").stream()
                    .filter(element -> element.attr("id").equals("Detail_Main"))
                    .collect(Collectors.toCollection(Elements::new))
                    .first();

                if (name != null) {
                    petInfo.put("Name", name.text().replace("About ", ""));
                }
            }
            
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public PetInfoBuilder AddType() {
            if (currUrl.contains("petfinder.com/dog")) {
                petInfo.put("Type", "Dog");
            } else if (currUrl.contains("petfinder.com/cat")) {
                petInfo.put("Type", "Cat");
            } else {
                petInfo.put("Type", "Other");
            }

            return this;
        }

        /** {@inheritDoc} */
        @Override
        public PetInfoBuilder AddBreed() {
            if (mainInfoDiv != null) {
                Element breed = mainInfoDiv.select("h3").stream()
                    .filter(element -> element.text().equals("Breed"))
                    .collect(Collectors.toCollection(Elements::new))
                    .first().parent().siblingElements()
                    .first().select("span").first().child(0);

                if (breed != null) {
                    petInfo.put("Breed", breed.text());
                }
            }

            return this;
        }

        /** {@inheritDoc} */
        @Override
        public PetInfoBuilder AddGender() {
            if (mainInfoDiv != null) {
                Element genderElement = mainInfoDiv.select("h3").stream()
                    .filter(element -> element.text().equals("Physical Traits"))
                    .collect(Collectors.toCollection(Elements::new))
                    .first().siblingElements().first().child(0).child(1);

                String gender = genderElement.child(1).text();

                if (gender != null) {
                    petInfo.put("Gender", gender.substring(0, 1));
                }
            }

            return this;
        }

        @Override
        public PetInfoBuilder AddAge() {
            if (mainInfoDiv != null) {
                Element ageElement = mainInfoDiv.select("h3").stream()
                    .filter(element -> element.text().equals("Physical Traits"))
                    .collect(Collectors.toCollection(Elements::new))
                    .first().siblingElements().first().child(0).child(0);

                String age = ageElement.child(2).text();

                if (age != null) {
                    String formattedAge = age.replace("(", "").replace(" years)", "");
                    String[] ageComponents = formattedAge.split("-");
                    int ageVal = (Integer.parseInt(ageComponents[1]) - Integer.parseInt(ageComponents[0])) / 2 * 52;
                    petInfo.put("Age", ageVal);
                }
            }

            return this;
        }

        /** {@inheritDoc} */
        @Override
        public PetInfoBuilder AddSize() {
            if (mainInfoDiv != null) {
                Element sizeElement = mainInfoDiv.select("h3").stream()
                    .filter(element -> element.text().equals("Physical Traits"))
                    .collect(Collectors.toCollection(Elements::new))
                    .first().siblingElements().first().child(0).child(2);

                String size = sizeElement.child(1).text();

                if (size != null) {
                    petInfo.put("Size", size);
                }
            }

            return this;
        }

        /** {@inheritDoc} */
        @Override
        public PetInfoBuilder AddPrice() {
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public PetInfoBuilder AddImage() {
            if (petImage != null) {
                petInfo.put("Image", petImage.attr("src"));
            }

            return this;
        }
    }

    /**
     * Attempts to scrape raw pet data from a specific profile URL based on detected provider.
     *
     * @param mainUrl The root or listing URL (used for animal type context).
     * @param url The specific direct pet profile URL to scrape.
     * @param driver Active Selenium WebDriver.
     * @return Map containing scraped pet fields, or keys "error"/"empty" on failure.
     */
    Map<String, Object> AttemptScrape(String mainUrl, String url, WebDriver driver) {
        Map<String, Object> data = new HashMap<>();

        try {
            // Determines the method for scrapping the current site
            IPetInfoBuilder petInfoBuilder;
            switch (FindPetDisplayMethod(url)) {
                case SHELTER_LUV: petInfoBuilder = new ShelterLuvBuilder(mainUrl, url, driver); break;
                case PETFINDER: petInfoBuilder = new PetFinderBuilder(mainUrl, url, driver); break;
                default: {
                    data.put("empty", "No valid data found");
                    return data;
                }
            }
            
            // Attempts to scrape pet data from the site 
            data = petInfoBuilder.AddName()
                .AddAge()
                .AddType()
                .AddBreed()
                .AddGender()
                .AddSize()
                .AddImage()
                .AddPrice()
                .Build();

            // Ensures that at least some distinguishable data is present before returning it
            if (
                data.keySet().contains("Name") && 
                data.keySet().contains("Type") && 
                data.keySet().contains("Gender")
            ) {
                return data;
            }            
        } catch (IOException e) {
            System.out.println("IOException thrown for the following reason - " + e.getMessage());
            data.clear();
            data.put("error", "Document could not be accessed - " + e.getMessage());
        }

        data.put("empty", "No valid data found");
        return data;
    }

    /**
     * Orchestrates a site-wide crawl starting from the root URL.
     * <p>
     * Performs a breadth-first search to discover listing pages, detects provider 
     * specific iframes/widgets, and uses a headless Chrome instance with 
     * stealth configurations to minimize detection by Web Application Firewalls.
     *
     * @param url Root URL of the adoption website to crawl.
     * @return A list of successfully scraped and converted {@link Pet} entities.
     */
    public List<Pet> ScrapeSite(AdoptionSite site) {
        String url = site.getUrl();
        List<Pet> scrappedData = new ArrayList<>();

        // Connect to the Docker container
        // Use "http://localhost:4444/wd/hub" if running script on host
        // Use "http://chrome:4444/wd/hub" if script is also in a Docker container
        try {
            // Set up headless browser so it can best replicate the conditions of an actual browser
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
            options.setExperimentalOption("useAutomationExtension", false);
            RemoteWebDriver driver = new RemoteWebDriver(URI.create("http://chrome:4444/wd/hub").toURL(), options);

            // Set up queues and set for urls that have been seen and that need to be looked at
            Queue<String> urlBFSQueue = new ArrayDeque<>();
            Queue<List<String>> terminatingUrlQueue = new ArrayDeque<>();
            Set<String> seenUrls = new HashSet<>();
            urlBFSQueue.add(url);
            seenUrls.add(url);

            // Run a breadth firth search on the site by searching through links that are found in the current page
            while (!urlBFSQueue.isEmpty()) {
                try {
                    // Set up the information needed to find urls on the current site
                    String currUrl = urlBFSQueue.remove();
                    String iframeClass = "";
                    String anchorClass = "";
                    String hostname = "";
                    List<String> terminatingUrls = new ArrayList<>();
                    PetDisplayMethod displayMethod = FindPetDisplayMethod(currUrl);
                    switch (displayMethod) {
                        case PetDisplayMethod.SHELTER_LUV: {
                            iframeClass = "shelterluv";
                            terminatingUrls.add("new.shelterluv.com/embed/animal/");
                            hostname = "new.shelterluv.com";
                            
                            break;
                        }
                        case PetDisplayMethod.PETFINDER: {
                            terminatingUrls.add("www.petfinder.com/dog/");
                            terminatingUrls.add("www.petfinder.com/cat/");
                            hostname = "www.petfinder.com";
                        }
                        default: break;
                    }

                    // Retrieves urls on the current site and adds them to their respective queues
                    System.out.println("Curr URL - " + currUrl);
                    List<String> newUrls = FindURLS(currUrl, iframeClass, anchorClass, driver);
                    URI origUri = new URI(url);
                    for (String newUrl : newUrls) {
                        if (terminatingUrls.stream().anyMatch(newUrl::contains) && !terminatingUrls.isEmpty()) {
                            // If the url is suspected to be a terminating url that needs to be scrapped it is added to the terminating queue
                            System.out.println("New Terminating URL - " + newUrl);
                            terminatingUrlQueue.add(List.of(currUrl, newUrl));
                            seenUrls.add(newUrl);
                        } else {
                            // Elsewise the url is added to the queue to be searched further if it meets certain criteria
                            URI uri = new URI(newUrl);
                            if (newUrl.contains("https://") && (uri.getHost().equals(origUri.getHost()) || uri.getHost().equals(hostname)) && !seenUrls.contains(newUrl)) {
                                System.out.println("New URL - " + newUrl);
                                urlBFSQueue.add(newUrl);
                                seenUrls.add(newUrl);
                            }
                        }
                    }
                } catch (URISyntaxException e) {
                    System.out.println("URISyntaxException thrown for the following reason - " + e.getMessage());
                }
            }

            // Iterates through all of the terminating urls and attempts to scrape data off of them
            while (!terminatingUrlQueue.isEmpty()) {
                List<String> terminatingUrlList = terminatingUrlQueue.remove();
                String parentUrl = terminatingUrlList.get(0);
                String childUrl = terminatingUrlList.get(1);

                System.out.println("Curr URL - " + childUrl);
                Map<String, Object> potentialData = AttemptScrape(parentUrl, childUrl, driver);
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(potentialData);
                System.out.println(json);
                if (!potentialData.containsKey("error") && !potentialData.containsKey("empty")) {
                    try {
                        Pet pet = ScrapedPetDTO.fromMap(potentialData).toEntity();
                        pet.setSite(site);
                        scrappedData.add(pet);
                    } catch (Exception e) {
                        log.warn("Failed to convert scraped data to Pet: {}", e.getMessage());
                    }
                }
            }
            
            driver.quit();

        } catch (MalformedURLException e) {}
        
        return scrappedData;
    }

    /**
     * Main entry point for the scraper service.
     * Scrapes pet listings from one or more adoption sites and returns the aggregated results.
     *
     * <p>Iterates over each provided URL, invoking the web scraper service and converting
     * raw results into {@link Pet} entities via {@link ScrapedPetDTO}. Individual pet
     * conversion failures are logged and skipped; however, a site-level failure will
     * immediately halt processing and return a 500 response.
     *
     * @param siteUrls a list of adoption site URLs to scrape
     * @return a {@code pets} list
     */
    public List<Pet> runScraper(List<AdoptionSite> adoptionSites) {
        List<Pet> allScrapedPets = new ArrayList<>();

        // scrape each site
        for (AdoptionSite adoptionSite : adoptionSites) {
            try {
                allScrapedPets.addAll(ScrapeSite(adoptionSite));
            } catch (Exception e) {
                log.error("Failed to scrape {}: {}", adoptionSite.getUrl(), e.getMessage());
            }
        }

        if (allScrapedPets.isEmpty()) {
            throw new NoResultException();
        }

        // return a status code OK along with all the pet objects
        return allScrapedPets;
    }
}
