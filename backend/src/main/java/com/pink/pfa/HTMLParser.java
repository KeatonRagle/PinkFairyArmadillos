package com.pink.pfa;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


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
public class HTMLParser {

    
    /**
     * Default constructor for HTMLParser.
     * Currently performs no initialization logic.
     */
    public HTMLParser() {}


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
    public static void ParserTest() {
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
}
