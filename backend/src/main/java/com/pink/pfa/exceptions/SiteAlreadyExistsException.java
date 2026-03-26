package com.pink.pfa.exceptions;

/**
 * Thrown when an adoption site submission is rejected because a site
 * with the given URL already exists in the database.
 */
public class SiteAlreadyExistsException extends RuntimeException {

    /**
     * @param url the duplicate URL that triggered the exception
     */
    public SiteAlreadyExistsException(String url) {
        super("Site already exists with url: " + url);
    }
}
