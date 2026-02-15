package com.pink.pfa.services;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;


/**
 * Utility class demonstrating basic web scraping using the Jsoup library.
 * <p>
 * This class connects to a remote HTML page (Wikipedia in this example),
 * parses the returned document, and extracts specific elements using CSS selectors.
 * It showcases:
 * <ul>
 *   <li>Establishing an HTTP connection with {@link Jsoup}.</li>
 *   <li>Parsing HTML into a {@link Document} object.</li>
 *   <li>Selecting elements using CSS-style queries.</li>
 *   <li>Extracting attributes such as title and absolute URLs.</li>
 * </ul>
 *
 * Note:
 * This is currently a demonstration / testing utility and is not managed
 * by Spring (no {@code @Service} or dependency injection involved).
 */
@Service
public class WebScraperService {
    /**
     * Default constructor for HTMLParser.
     * Currently performs no initialization logic.
     */
    public WebScraperService() {}


    /**
     * Connects to Wikipedia's homepage, retrieves the HTML document,
     * and prints the page title along with selected news headline links
     * from the "In the news" section.
     *
     * Uses the CSS selector {@code #mp-itn b a} to locate anchor elements
     * within bold tags inside the main page news section.
     *
     * If a network or IO error occurs, the exception message is printed
     * to standard output.
     */
    public void ScrapeTest() {
        try {
            Document doc = Jsoup.connect("https://en.wikipedia.org/").get();
            System.out.println(doc.title());
            Elements newsHeadlines = doc.select("#mp-itn b a");
            for (Element headline : newsHeadlines) {
                System.out.println(headline.attr("title") + "\n\t" + headline.absUrl("href"));
            }
        } catch (IOException e) {
            System.out.println("IOException thrown for the following reason - " + e.getMessage());
        }
    }

    void LookForLinks(String url, Document doc, Queue<String> urlBFSQueue) {
        try {
            URI uri = new URI(url);
            Elements anchors = doc.select("a");

            for (Element anchor : anchors) {
                URI currUri = new URI(anchor.attr("href"));
                if (uri.getHost().equals(currUri.getHost())) {
                    urlBFSQueue.add(anchor.attr("href"));
                }
            }
        } catch (URISyntaxException e) {
            System.out.println("URISyntaxException thrown for the following reason - " + e.getMessage());
        }
    }

    boolean IsFlexBox(Element element) {
        return element.attr("style").contains("display: flex") || 
               element.attr("style").contains("display: inline-flex") ||
               element.attr("class").contains("flex") || 
               element.attr("class").contains("container") ||
               element.attr("class").contains("wrapper") ||
               element.attr("class").contains("flex-wrapper")||
               element.attr("class").contains("gallery");
    }

    Map<String, Object> AttemptScrape(String url) {
        Map<String, Object> data = new HashMap<>();

        try {
            Document doc = Jsoup.connect(url).get();

        } catch (IOException e) {
            System.out.println("IOException thrown for the following reason - " + e.getMessage());
            data.clear();
            data.put("error", "Document could not be accessed - " + e.getMessage());
        }

        return data;
    }

    public Map<String, Object> ScrapeSite(String url) {
        List<Map<String, Object>> scrappedData = new ArrayList();

        try {
            Document doc = Jsoup.connect(url).get();

            Elements flexBoxes = doc.select("div").stream()
                .filter(element -> IsFlexBox(element))
                .collect(Collectors.toCollection(Elements::new));

            for (Element flexBox : flexBoxes) {
                Elements childrenWithAnchors = flexBox.children().stream()
                    .filter(child -> child.tagName().equals("a"))
                    .collect(Collectors.toCollection(Elements::new));

                for (Element childAnchor : childrenWithAnchors) {
                    Map<String, Object> data = AttemptScrape(childAnchor.attr("href"));
                    if (!data.containsKey("error")) {
                        scrappedData.add(data);
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("IOException thrown for the following reason - " + e.getMessage());
        }
        
        return Map.of(
            "data", scrappedData
        );
    }
}
