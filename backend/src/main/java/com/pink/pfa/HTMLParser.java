package com.pink.pfa;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HTMLParser {
    public HTMLParser() {}

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
