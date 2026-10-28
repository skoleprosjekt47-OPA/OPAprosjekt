package com.booklibrary.model;

/**
 * A film or series based on a book.
 *
 * @param title the title of the film or series
 * @param type  "film" or "series"
 * @param year  the release year, or {@code null} if unknown
 * @param url   the address where it can be watched, or {@code null} if the
 *              user has not added a link yet
 */
public record Adaptation(String title, String type, Integer year, String url) {

    /**
     * Tells whether the user has added a link to watch this adaptation.
     *
     * @return {@code true} if a link is present
     */
    public boolean hasLink() {
        return url != null;
    }

    /**
     * Builds the text shown on the watch button.
     *
     * @return for example "The Fellowship of the Ring (2001) · film"
     */
    public String label() {
        return title + (year == null ? "" : " (" + year + ")") + " · " + type;
    }
}
