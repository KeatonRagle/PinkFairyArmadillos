package com.pink.pfa.services;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;

import com.pink.pfa.services.WebScraperService.PetInfoBuilder;

@ExtendWith(MockitoExtension.class)
class WebScraperServiceTest {

    @InjectMocks
    private WebScraperService webScraperService;

    @Mock
    private WebDriver driver;

    private final String BASIC_HTML = """
        <html>
            <body>
                <a class="test-anchor" href="https://example.com/pet1"></a>
                <iframe class="test-iframe" src="https://example.com/embed1"></iframe>
            </body>
        </html>
    """;

    private final String EMPTY_HTML = "<html><body></body></html>";

    @BeforeEach
    void setup() {
        lenient().when(driver.getPageSource()).thenReturn(BASIC_HTML);
    }

    @Test
    void testFindURLS_ShouldReturnAnchorAndIframeLinks() {

        List<String> urls = webScraperService.FindURLS(
                "https://example.com",
                "test-iframe",
                "test-anchor",
                driver
        );

        assertEquals(2, urls.size());
        assertTrue(urls.contains("https://example.com/pet1"));
        assertTrue(urls.contains("https://example.com/embed1"));
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

        List<String> urls = webScraperService.FindURLS(
                "https://example.com",
                "",
                "",
                driver
        );

        assertEquals(2, urls.size());
    }

    @Test
    void testFindPetDisplayMethod_ShouldDetectShelterLuvByUrl() {

        WebScraperService.PetDisplayMethod method =
                webScraperService.FindPetDisplayMethod("https://shelterluv.com/embed/animal/123");

        assertEquals(WebScraperService.PetDisplayMethod.SHELTER_LUV, method);
    }

    @Test
    void testFindPetDisplayMethod_ShouldReturnBasicForUnknownUrl() {

        WebScraperService.PetDisplayMethod method =
                webScraperService.FindPetDisplayMethod("https://example.com");

        assertEquals(WebScraperService.PetDisplayMethod.BASIC, method);
    }

    @Test
    void testFindPetDisplayMethod_InvalidUrl_ShouldReturnBasic() {

        WebScraperService.PetDisplayMethod method =
                webScraperService.FindPetDisplayMethod("invalid-url");

        assertEquals(WebScraperService.PetDisplayMethod.BASIC, method);
    }

    @Test
    void testPetInfoBuilderBuild_ShouldClearInternalState() {

        WebScraperService.PetInfoBuilder builder =
                webScraperService.new PetInfoBuilder("https://example.com") {

                    @Override
                    public PetInfoBuilder AddName() {
                        petInfo.put("Name", "Buddy");
                        return this;
                    }

                    @Override public PetInfoBuilder AddType() { return this; }
                    @Override public PetInfoBuilder AddBreed() { return this; }
                    @Override public PetInfoBuilder AddGender() { return this; }
                    @Override public PetInfoBuilder AddAge() { return this; }
                    @Override public PetInfoBuilder AddPrice() { return this; }
                    @Override public PetInfoBuilder AddImage() { return this; }
                };

        Map<String, Object> firstBuild = builder.AddName().Build();
        Map<String, Object> secondBuild = builder.Build();

        assertEquals("Buddy", firstBuild.get("Name"));
        assertTrue(secondBuild.isEmpty());
    }

    @Test
    void testBuild_ReturnsDefensiveCopy() {

        WebScraperService.PetInfoBuilder builder =
                webScraperService.new PetInfoBuilder("https://example.com") {

                    @Override
                    public PetInfoBuilder AddName() {
                        petInfo.put("Name", "Max");
                        return this;
                    }

                    @Override public PetInfoBuilder AddType() { return this; }
                    @Override public PetInfoBuilder AddBreed() { return this; }
                    @Override public PetInfoBuilder AddGender() { return this; }
                    @Override public PetInfoBuilder AddAge() { return this; }
                    @Override public PetInfoBuilder AddPrice() { return this; }
                    @Override public PetInfoBuilder AddImage() { return this; }
                };

        Map<String, Object> result = builder.AddName().Build();
        result.put("Injected", "BadData");

        Map<String, Object> second = builder.Build();

        assertFalse(second.containsKey("Injected"));
    }

    // =========================================================
    // AttemptScrape Tests
    // =========================================================

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
}