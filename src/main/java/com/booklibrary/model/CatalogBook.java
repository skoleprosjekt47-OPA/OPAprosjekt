package com.booklibrary.model;

import java.util.List;

/**
 * A book found in an external library catalog (for example Open Library or
 * Google Books). It is read-only information and is not stored in the
 * database until the user adds it to the private library.
 *
 * Author: Farid Rahmani
 * Contribution: Designed the catalog search model and the mapping to the private library.
 * Purpose: an immutable value object (Java record) that carries the
 * information a catalog provider returns for one search hit, so the user
 * interface does not depend on any provider's JSON format.
 *
 * @param source      name of the library catalog that returned the book
 * @param externalId  the id of the book inside that catalog
 * @param title       the book title
 * @param authors     the authors, possibly empty
 * @param year        the first publication year, or {@code null} if unknown
 * @param description a short description, or {@code null} if unknown
 * @param subjects    subjects or categories, possibly empty
 * @param coverUrl    URL of a cover image, or {@code null} if unknown
 * @param pageCount   number of pages, or {@code null} if unknown
 * @param publisher   the publisher, or {@code null} if unknown
 * @param language    the language code, or {@code null} if unknown
 * @param infoUrl     URL of the book's page in the catalog, or {@code null}
 */
public record CatalogBook(String source, String externalId, String title, List<String> authors,
                          Integer year, String description, List<String> subjects, String coverUrl,
                          Integer pageCount, String publisher, String language, String infoUrl) {

    /**
     * Returns all authors as one comma-separated text for display.
     *
     * @return the authors joined with ", ", or an empty string if there are none
     */
    public String authorsText() {
        return String.join(", ", authors);
    }

    /**
     * Returns the first author, used when the book is added to the private library.
     *
     * @return the first author, or {@code null} if the catalog gave no author
     */
    public String firstAuthor() {
        return authors.isEmpty() ? null : authors.get(0);
    }

    /**
     * Returns the first subject, used as the genre when the book is added to the private library.
     *
     * @return the first subject, or {@code null} if the catalog gave no subject
     */
    public String firstSubject() {
        return subjects.isEmpty() ? null : subjects.get(0);
    }

    /** The source name used for free books from Project Gutenberg. */
    public static final String GUTENBERG = "Project Gutenberg";

    /**
     * Tells whether the whole book can be read for free inside the application.
     *
     * @return {@code true} for books from Project Gutenberg (their copyright has expired)
     */
    public boolean isFreeToRead() {
        return GUTENBERG.equals(source) && externalId != null;
    }

    /**
     * Returns the address of the plain text of a free book.
     *
     * @return the text address, or {@code null} if the book is not free to read
     */
    public String freeTextUrl() {
        return isFreeToRead()
                ? "https://www.gutenberg.org/cache/epub/" + externalId + "/pg" + externalId + ".txt"
                : null;
    }
}
