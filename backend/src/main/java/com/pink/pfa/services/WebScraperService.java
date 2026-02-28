package com.pink.pfa.services;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

/**
 * Service responsible for scraping pet adoption websites.
 * <p>
 * This class uses:
 * <ul>
 *     <li>Selenium WebDriver for dynamic content loading</li>
 *     <li>Jsoup for HTML parsing and element extraction</li>
 *     <li>A BFS-style traversal for discovering embedded pet listing pages</li>
 * </ul>
 *
 * It currently supports multiple pet display providers, such as ShelterLuv.
 */
@Service
public class WebScraperService {
    public WebScraperService() {}

    /**
     * Enum representing supported pet listing display providers.
     */
    enum PetDisplayMethod {
        SHELTER_LUV,    /** ShelterLuv embedded pet listing system */
        BASIC           /** Basic or unsupported listing structure */
    };

    /**
     * Extracts dynamic URLs from a given page using Selenium and Jsoup.
     *
     * @param url The URL to inspect.
     * @param iframeClass The class name to filter iframe elements.
     * @param anchorClass The class name to filter anchor elements.
     * @param driver Active Selenium WebDriver instance.
     * @return A list of discovered URLs matching the provided filters.
     */
    List<String> FindURLS(String url, String iframeClass, String anchorClass, WebDriver driver) {
        List<String> dynamicUrls = new ArrayList<>();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(100));

        driver.get(url);

        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("a")));
        } catch (TimeoutException e) {
            System.out.println("No anchors could be found");
        }

        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("iframe")));
        } catch (TimeoutException e) {
            System.out.println("No iframes could be found");
        }

        Document doc = Jsoup.parse(driver.getPageSource());

        Elements anchors = doc.select("a").stream()
            .filter(anchor -> anchor.attr("class").contains(anchorClass))
            .collect(Collectors.toCollection(Elements::new));
            
        for (Element anchor : anchors) {
            dynamicUrls.add(anchor.attr("href"));
        }

        Elements iframes = doc.select("iframe").stream()
            .filter(iframe -> iframe.attr("class").contains(iframeClass))
            .collect(Collectors.toCollection(Elements::new));
            
        for (Element iframe : iframes) {
            dynamicUrls.add(iframe.attr("src"));
        }

        return dynamicUrls;
    }

    /**
     * Determines which pet display method is used on the provided URL.
     *
     * @param url The URL to inspect.
     * @return The detected {@link PetDisplayMethod}.
     */
    PetDisplayMethod FindPetDisplayMethod(String url) {
        try {
            if (url.contains("shelterluv.com")) {
                return PetDisplayMethod.SHELTER_LUV;
            }

            Document doc = Jsoup.connect(url).get();
            Elements shelterLuvScripts = doc.select("script").stream()
                .filter(element -> element.attr("src").contains("new.shelterluv.com"))
                .collect(Collectors.toCollection(Elements::new));

            if (shelterLuvScripts.size() > 0) {
                return PetDisplayMethod.SHELTER_LUV;
            }
        } catch (Exception e) {
            System.out.println("Failed to find document when determining display method");
        }

        return PetDisplayMethod.BASIC;
    }

    /**
     * Builder interface used to construct a pet information map.
     */
    interface IPetInfoBuilder {
        PetInfoBuilder AddName();       /** @return Builder instance with name populated */
        PetInfoBuilder AddType();       /** @return Builder instance with type populated */
        PetInfoBuilder AddBreed();      /** @return Builder instance with breed populated */
        PetInfoBuilder AddGender();     /** @return Builder instance with gender populated */
        PetInfoBuilder AddAge();        /** @return Builder instance with age populated */
        PetInfoBuilder AddPrice();      /** @return Builder instance with price populated */
        PetInfoBuilder AddImage();      /** @return Builder instance with image populated */
        
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
        protected Map<String, Object> petInfo;  /** Internal storage for scraped pet data */

        /**
         * Constructs a new builder instance.
         *
         * @param mainUrl The parent listing URL.
         */
        public PetInfoBuilder(String mainUrl) {
            this.mainUrl = mainUrl;
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
            super(origSiteUrl);
            this.driver = webDriver;

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(100));

            driver.get(currUrl);

            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("img")));
            } catch (TimeoutException e) {
                System.out.println("No images could be found");
            }

            Document doc = Jsoup.parse(driver.getPageSource());

            petImage = doc.select("img").stream()
                .filter(element -> element.attr("src").contains("shelterluv.com") && element.attr("src").contains("profile-pictures"))
                .collect(Collectors.toCollection(Elements::new))
                .first();

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
                Element breed = mainInfoDiv.children().stream()
                    .filter(element -> element.text().contains("Breed"))
                    .collect(Collectors.toCollection(Elements::new))
                    .first().children().stream()
                        .filter(element -> element.text().contains("Breed"))
                        .collect(Collectors.toCollection(Elements::new))
                        .first().siblingElements().first();

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
                Element gender = mainInfoDiv.children().stream()
                    .filter(element -> element.text().contains("Sex"))
                    .collect(Collectors.toCollection(Elements::new))
                    .first().children().stream()
                        .filter(element -> element.text().contains("Sex"))
                        .collect(Collectors.toCollection(Elements::new))
                        .first().siblingElements().first();

                if (gender != null) {
                    petInfo.put("Gender", gender.text());
                }
            }

            return this;
        }

        /** {@inheritDoc} */
        @Override
        public PetInfoBuilder AddAge() {
            if (mainInfoDiv != null) {
                Element age = mainInfoDiv.children().stream()
                    .filter(element -> element.text().contains("Age"))
                    .collect(Collectors.toCollection(Elements::new))
                    .first().children().stream()
                        .filter(element -> element.text().contains("Age"))
                        .collect(Collectors.toCollection(Elements::new))
                        .first().siblingElements().first();

                if (age != null) {
                    petInfo.put("Age", age.text());
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
     * Attempts to scrape pet data from a given embed URL.
     *
     * @param mainUrl Parent site URL.
     * @param url Specific embed URL.
     * @param driver Active Selenium WebDriver.
     * @return Map containing pet data, or error/empty markers.
     */
    Map<String, Object> AttemptScrape(String mainUrl, String url, WebDriver driver) {
        Map<String, Object> data = new HashMap<>();

        try {
            IPetInfoBuilder petInfoBuilder;

            switch (FindPetDisplayMethod(url)) {
                case SHELTER_LUV: petInfoBuilder = new ShelterLuvBuilder(mainUrl, url, driver); break;
                default: {
                    data.put("empty", "No valid data found");
                    return data;
                }
            }
            
            data = petInfoBuilder.AddName()
                .AddType()
                .AddBreed()
                .AddGender()
                .AddImage()
                .AddPrice()
                .Build();

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
     * Main entry point for scraping a pet adoption website.
     * <p>
     * Performs:
     * <ul>
     *     <li>Breadth-first traversal of internal links</li>
     *     <li>Detection of embedded pet provider systems</li>
     *     <li>Batch scraping of discovered pet profile pages</li>
     * </ul>
     *
     * @param url Root URL of the adoption website.
     * @return Map containing a list of successfully scraped pet data under key {@code "data"}.
     */
    public Map<String, Object> ScrapeSite(String url) {
        List<Map<String, Object>> scrappedData = new ArrayList();

        // 2. Connect to the Docker container
        // Use "http://localhost:4444/wd/hub" if running script on host
        // Use "http://chrome:4444/wd/hub" if script is also in a Docker container
        try {
            Logger.getLogger("org.openqa.selenium.devtools.CdpVersionFinder").setLevel(Level.OFF);
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");

            RemoteWebDriver driver = new RemoteWebDriver(URI.create("http://chrome:4444/wd/hub").toURL(), options);

            Queue<String> urlBFSQueue = new ArrayDeque<>();
            Set<String> seenUrls = new HashSet<>();
            urlBFSQueue.add(url);
            seenUrls.add(url);

            Queue<List<String>> embedBatchQueue = new ArrayDeque<>();
            while (!urlBFSQueue.isEmpty()) {
                try {
                    String currUrl = urlBFSQueue.remove();

                    String iframeClass = "";
                    String anchorClass = "";
                    String hostname = "";
                    String embedUrl = "";
                    PetDisplayMethod displayMethod = FindPetDisplayMethod(currUrl);
                    switch (displayMethod) {
                        case PetDisplayMethod.SHELTER_LUV: {
                            iframeClass = "shelterluv";
                            embedUrl = "new.shelterluv.com/embed/animal/";
                            hostname = "new.shelterluv.com";
                            
                            break;
                        }
                        default: break;
                    }

                    System.out.println("Curr URL - " + currUrl);
                    List<String> newUrls = FindURLS(currUrl, iframeClass, anchorClass, driver);
                    URI origUri = new URI(url);
                    for (String newUrl : newUrls) {
                        if (newUrl.contains(embedUrl) && !embedUrl.equals("")) {
                            System.out.println("New URL - " + newUrl);
                            embedBatchQueue.add(List.of(currUrl, newUrl));
                            seenUrls.add(newUrl);
                        } else {
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

            while (!embedBatchQueue.isEmpty()) {
                List<String> embedUrlList = embedBatchQueue.remove();
                String parentUrl = embedUrlList.get(0);
                String childUrl = embedUrlList.get(1);

                System.out.println("Curr URL - " + childUrl);
                Map<String, Object> potentialData = AttemptScrape(parentUrl, childUrl, driver);
                if (!potentialData.containsKey("error") && !potentialData.containsKey("empty")) {
                    scrappedData.add(potentialData);
                }
            }
            
            driver.quit();

        } catch (MalformedURLException e) {
            
        }
        
        
        return Map.of(
            "data", scrappedData
        );
    }
}
