package com.pink.pfa.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.pink.pfa.services.WebScraperService.PetInfoBuilder;

@ExtendWith(MockitoExtension.class)
class WebScrapingServiceTest {

    @InjectMocks
    private WebScraperService webScraperService;

    @Mock(extraInterfaces = JavascriptExecutor.class)
    private WebDriver driver;

    @BeforeEach
    void setUp() {
        lenient().doNothing().when(driver).get(anyString());
        lenient().when(driver.getPageSource()).thenReturn(BASIC_HTML);
    }

    private final String BASIC_HTML = """
        <html>
            <body>
                <a class="test-anchor" href="https://example.com/pet1"></a>
                <iframe class="test-iframe" src="https://example.com/embed1"></iframe>
            </body>
        </html>
    """;

    private final String EMPTY_HTML = "<html><body></body></html>";

    private void setupPetFinderMocks(String html) {
        when(driver.getPageSource()).thenReturn(html);
        lenient().when(driver.findElement(By.cssSelector("div.swiper-slide-active"))).thenReturn(mock(WebElement.class));
        lenient().when(driver.findElement(By.cssSelector("#pet-details-about-section"))).thenReturn(mock(WebElement.class));
    }

    @Test
    void testFindURLS_ShouldReturnAnchorAndIframeLinks() {
        String targetUrl = "https://www.shelterluv.com/available-pets";
        
        WebElement mockIframe = mock(WebElement.class);
        when(mockIframe.getAttribute("class")).thenReturn("test-iframe");
        when(mockIframe.getAttribute("src")).thenReturn("https://example.com/embed1");
        
        when(driver.findElements(By.tagName("iframe"))).thenReturn(List.of(mockIframe));

        List<String> urls = webScraperService.FindURLS(
                targetUrl,
                "test-iframe",
                "test-anchor",
                driver
        );

        assertEquals(2, urls.size());
        assertTrue(urls.contains("https://example.com/pet1"), "Should find anchor link via Jsoup");
        assertTrue(urls.contains("https://example.com/embed1"), "Should find iframe link via Selenium");
    }

    @Test
    void testFindURLS_NoMatches_ShouldReturnEmptyList() {

        when(driver.getPageSource()).thenReturn(EMPTY_HTML);

        List<String> urls = webScraperService.FindURLS(
                "https://example.com",
                "no-match",
                "no-match",
                driver
        );

        assertTrue(urls.isEmpty());
    }

    @Test
    void testFindURLS_EmptyClassFilters_ShouldReturnAllTags() {
        String url = "https://www.shelterluv.com/available-pets";

        WebElement mockIframe = mock(WebElement.class);
        when(mockIframe.getAttribute("src")).thenReturn("https://example.com/embed1");
        when(mockIframe.getAttribute("class")).thenReturn("some-class"); 
        
        when(driver.findElements(By.tagName("iframe"))).thenReturn(List.of(mockIframe));

        List<String> urls = webScraperService.FindURLS(
                url,
                "",
                "",
                driver
        );

        assertEquals(2, urls.size());
    }

    @Test
    void testFindPetDisplayMethod_DetectionByUrl() {
        // ShelterLuv URL detection
        assertEquals(WebScraperService.PetDisplayMethod.SHELTER_LUV, 
            webScraperService.FindPetDisplayMethod("https://new.shelterluv.com/embed/animal/123"));

        // PetFinder URL detection
        assertEquals(WebScraperService.PetDisplayMethod.PETFINDER, 
            webScraperService.FindPetDisplayMethod("https://www.petfinder.com/dog/buddy-456"));
    }

    @Test
    void testFindPetDisplayMethod_ShouldReturnBasicForUnknownUrl() {

        WebScraperService.PetDisplayMethod method =
                webScraperService.FindPetDisplayMethod("https://example.com");

        assertEquals(WebScraperService.PetDisplayMethod.UNSUPPORTED, method);
    }

    @Test
    void testFindPetDisplayMethod_InvalidUrl_ShouldReturnBasic() {

        WebScraperService.PetDisplayMethod method =
                webScraperService.FindPetDisplayMethod("invalid-url");

        assertEquals(WebScraperService.PetDisplayMethod.UNSUPPORTED, method);
    }

    @Test
    void testPetInfoBuilderBuild_ShouldClearInternalState() {

        WebScraperService.PetInfoBuilder builder =
                webScraperService.new PetInfoBuilder("https://example.com", "https://example.com") {

                    @Override
                    public PetInfoBuilder AddName() {
                        petInfo.put("Name", "Buddy");
                        return this;
                    }

                    @Override public PetInfoBuilder AddType() { return this; }
                    @Override public PetInfoBuilder AddBreed() { return this; }
                    @Override public PetInfoBuilder AddGender() { return this; }
                    @Override public PetInfoBuilder AddAge() { return this; }
                    @Override public PetInfoBuilder AddSize() { return this; }
                    @Override public PetInfoBuilder AddLocation() { return this; }
                    @Override public PetInfoBuilder AddImage() { return this; }
                    @Override public PetInfoBuilder AddSecondaryImages() { return this; }
                };

        Map<String, Object> firstBuild = builder.AddName().Build();
        Map<String, Object> secondBuild = builder.Build();

        assertEquals("Buddy", firstBuild.get("Name"));
        assertTrue(secondBuild.isEmpty());
    }

    @Test
    void testBuild_ReturnsDefensiveCopy() {

        WebScraperService.PetInfoBuilder builder =
                webScraperService.new PetInfoBuilder("https://example.com", "https://example.com") {

                    @Override
                    public PetInfoBuilder AddName() {
                        petInfo.put("Name", "Max");
                        return this;
                    }

                    @Override public PetInfoBuilder AddType() { return this; }
                    @Override public PetInfoBuilder AddBreed() { return this; }
                    @Override public PetInfoBuilder AddGender() { return this; }
                    @Override public PetInfoBuilder AddAge() { return this; }
                    @Override public PetInfoBuilder AddSize() { return this; }
                    @Override public PetInfoBuilder AddLocation() { return this; }
                    @Override public PetInfoBuilder AddImage() { return this; }
                    @Override public PetInfoBuilder AddSecondaryImages() { return this; }
                };

        Map<String, Object> result = builder.AddName().Build();
        result.put("Injected", "BadData");

        Map<String, Object> second = builder.Build();

        assertFalse(second.containsKey("Injected"));
    }

    @Test
    void testAttemptScrape_MissingRequiredFields_ShouldReturnEmpty() {

        Map<String, Object> result =
                webScraperService.AttemptScrape(
                        "https://example.com",
                        "https://example.com",
                        driver
                );

        assertTrue(result.containsKey("empty"));
    }

    @ParameterizedTest
    @CsvSource({
        "'Female', 'F'",
        "'Male', 'M'",
        "'Unknown', 'U'"
    })
    void testShelterLuvBuilder_GenderTruncation(String inputGender, String expectedChar) throws IOException {
        // Simulates the sibling element structure ShelterLuv uses: <div>Sex</div><div>Female</div>
        String html = "<div data-cy='name'><h1>Test</h1><div><div>Sex</div><div>" + inputGender + "</div></div></div>";
        
        when(driver.getPageSource()).thenReturn(html);
        when(driver.findElement(By.tagName("img"))).thenReturn(mock(WebElement.class));

        WebScraperService.ShelterLuvBuilder builder = 
            webScraperService.new ShelterLuvBuilder("site", "url", driver);
        
        assertEquals(expectedChar, builder.AddGender().Build().get("Gender"), 
            "Gender should be truncated to a single character");
    }

    @ParameterizedTest
    @CsvSource({
        "'1Y/0M/0W', 52",  // 1 * 52
        "'0Y/6M/0W', 26",  // (6 * 52) / 12
        "'0Y/0M/4W', 4",   // Just weeks
        "'2Y/3M/1W', 118"  // 104 + 13 + 1
    })
    void testShelterLuvBuilder_AgeMath(String ageString, int expectedWeeks) throws IOException {
        String html = "<div data-cy='name'><h1>Test</h1><div><div>Age</div><div>" + ageString + "</div></div></div>";
        
        when(driver.getPageSource()).thenReturn(html);
        when(driver.findElement(By.tagName("img"))).thenReturn(mock(WebElement.class));

        WebScraperService.ShelterLuvBuilder builder = 
            webScraperService.new ShelterLuvBuilder("site", "url", driver);
        
        assertEquals(expectedWeeks, builder.AddAge().Build().get("Age"));
    }

    @ParameterizedTest
    @CsvSource({
        "10.5, Small",       // < 25
        "25.0, Medium",      // 25-59
        "65.0, Large",       // 60-99
        "105.0, Extra Large" // >= 100
    })
    void testShelterLuvBuilder_DogSizeMapping(double weight, String expectedSize) throws IOException {
        // Structure: <div>Weight</div><div>20.5 lbs</div>
        String html = "<div data-cy='name'><h1>Test</h1><div><div>Weight</div><div>" + weight + " lbs</div></div></div>";
        
        when(driver.getPageSource()).thenReturn(html);
        when(driver.findElement(By.tagName("img"))).thenReturn(mock(WebElement.class));

        // Passing a URL containing 'animalType=Dog' triggers the Dog logic
        WebScraperService.ShelterLuvBuilder builder = 
            webScraperService.new ShelterLuvBuilder("https://site.com?animalType=Dog", "url", driver);
        
        assertEquals(expectedSize, builder.AddType().AddSize().Build().get("Size"));
    }

    @ParameterizedTest
    @CsvSource({
        "5.0, Small",       // < 8
        "9.0, Medium",      // 8-11
        "13.0, Large",      // 12-14
        "16.0, Extra Large" // >= 15
    })
    void testShelterLuvBuilder_CatSizeMapping(double weight, String expectedSize) throws IOException {
        String html = "<div data-cy='name'><h1>Test</h1><div><div>Weight</div><div>" + weight + " lbs</div></div></div>";
        
        when(driver.getPageSource()).thenReturn(html);
        when(driver.findElement(By.tagName("img"))).thenReturn(mock(WebElement.class));

        // Passing a URL containing 'animalType=Cat'
        WebScraperService.ShelterLuvBuilder builder = 
            webScraperService.new ShelterLuvBuilder("https://site.com?animalType=Cat", "url", driver);
        
        assertEquals(expectedSize, builder.AddType().AddSize().Build().get("Size"));
    }

    @Test
    void testPetFinderBuilder_NameSanitization() throws IOException {
        // Tests the .replace("About ", "") logic
        String html = "<section id='pet-details-about-section'><h2 id='Detail_Main'>About Buddy</h2></section>";
        setupPetFinderMocks(html);

        WebScraperService.PetFinderBuilder builder = 
            webScraperService.new PetFinderBuilder("site", "url", driver);
        
        assertEquals("Buddy", builder.AddName().Build().get("Name"));
    }

    @ParameterizedTest
    @CsvSource({
        "'(2-4 years)', 52",   // (4-2)/2 * 52
        "'(1-5 years)', 104",  // (5-1)/2 * 52
        "'(10-12 years)', 52"  // (12-10)/2 * 52
    })
    void testPetFinderBuilder_AgeCalculation(String range, int expectedWeeks) throws IOException {
        String html = """
            <section id='pet-details-about-section'>
                <h3>Physical Traits</h3>
                <div>
                    <div>
                        <div>
                            <svg></svg>
                            <span></span>
                            <div>%s</div>
                        </div>
                    </div>
                </div>
            </section>
            """.formatted(range);
        setupPetFinderMocks(html);

        WebScraperService.PetFinderBuilder builder = 
            webScraperService.new PetFinderBuilder("site", "url", driver);
        
        assertEquals(expectedWeeks, builder.AddAge().Build().get("Age"));
    }

    @ParameterizedTest
    @CsvSource({
        "'Female', 'F'",
        "'Male', 'M'"
    })
    void testPetFinderBuilder_GenderTruncation(String inputGender, String expectedChar) throws IOException {
        // Mimics the Petfinder structure: h3(Physical Traits) -> sibling -> child -> child -> child(Gender)
        String html = """
            <section id='pet-details-about-section'>
                <h3>Physical Traits</h3>
                <div>
                    <div>
                        <span>Age Container</span>
                        <div>
                            <span>Icon</span>
                            <span>""" + inputGender + """
                            </span>
                        </div>
                    </div>
                </div>
            </section>
            """;
        
        setupPetFinderMocks(html);

        WebScraperService.PetFinderBuilder builder = 
            webScraperService.new PetFinderBuilder("site", "https://petfinder.com/dog/1", driver);
        
        assertEquals(expectedChar, builder.AddGender().Build().get("Gender"), 
            "PetFinder gender should be truncated to a single character");
    }

    @Test
    void testPetFinderBuilder_SizeExtraction() throws IOException {
        // PetFinder structure: Physical Traits -> sibling -> child(0) -> child(2) -> child(1).text
        String html = """
            <section id='pet-details-about-section'>
                <h3>Physical Traits</h3>
                <div>
                    <div>
                        <div>Age Container</div>
                        <div>Gender Container</div>
                        <div>
                            <span>Icon</span>
                            <span>Large</span>
                            <span>(70 lbs)</span>
                        </div>
                    </div>
                </div>
            </section>
            """;
        setupPetFinderMocks(html);

        WebScraperService.PetFinderBuilder builder = 
            webScraperService.new PetFinderBuilder("site", "https://petfinder.com/dog/1", driver);
        
        assertEquals("Large", builder.AddSize().Build().get("Size"));
    }
}
