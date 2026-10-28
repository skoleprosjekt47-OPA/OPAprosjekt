package com.booklibrary.catalog;

import com.booklibrary.model.CatalogBook;
import com.booklibrary.reader.GutenbergBook;
import com.booklibrary.reader.GutenbergClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Catalog provider for Project Gutenberg: free books that can be read in full inside the application.
 *
 * Author: Farid Rahmani
 * Contribution: Added the free book source to the Discover search.
 * Purpose: uses the Gutendex API through {@link GutenbergClient} and turns
 * each result into a {@link CatalogBook} marked as free to read.
 */
public class GutenbergProvider implements LibraryProvider {

    private final GutenbergClient client;

    /**
     * Creates the provider.
     *
     * @param fetcher the object that does the HTTP requests
     */
    public GutenbergProvider(HttpFetcher fetcher) {
        this.client = new GutenbergClient(fetcher);
    }

    @Override
    public String getName() {
        return CatalogBook.GUTENBERG;
    }

    @Override
    public List<CatalogBook> search(String query) throws IOException, InterruptedException {
        List<GutenbergBook> found;
        try {
            found = searchWithRetry(query);
        } catch (IOException e) {
            // the online catalogue is slow or down: fall back to the built-in list of well-known books
            List<GutenbergBook> featured = FeaturedBooks.matching(query);
            if (featured.isEmpty()) {
                throw e;
            }
            found = featured;
        }
        if (found.isEmpty()) {
            found = FeaturedBooks.matching(query);
        }
        return toCatalogBooks(found);
    }

    private List<GutenbergBook> searchWithRetry(String query) throws IOException, InterruptedException {
        try {
            return client.search(query);
        } catch (IOException first) {
            return client.search(query);
        }
    }

    private List<CatalogBook> toCatalogBooks(List<GutenbergBook> found) {
        List<CatalogBook> books = new ArrayList<>();
        for (GutenbergBook book : found) {
            String subjects = book.subjects().isEmpty() ? "" : " Subjects: " + String.join("; ", book.subjects()) + ".";
            books.add(new CatalogBook(
                    getName(),
                    String.valueOf(book.id()),
                    book.title(),
                    book.author().equals("Unknown") ? List.of() : List.of(firstNameFirst(book.author())),
                    null,
                    "Free book. You can read the whole text in the Reading tab." + subjects,
                    book.subjects(),
                    "https://www.gutenberg.org/cache/epub/" + book.id() + "/pg" + book.id() + ".cover.medium.jpg",
                    null,
                    "Project Gutenberg",
                    "English",
                    "https://www.gutenberg.org/ebooks/" + book.id()));
        }
        return books;
    }

    /**
     * Turns "Shelley, Mary Wollstonecraft" into "Mary Wollstonecraft Shelley".
     *
     * @param name the name as written in the catalogue
     * @return the name with the first name first
     */
    static String firstNameFirst(String name) {
        int comma = name.indexOf(',');
        if (comma < 0) {
            return name.strip();
        }
        return (name.substring(comma + 1).strip() + " " + name.substring(0, comma).strip()).strip();
    }
}
