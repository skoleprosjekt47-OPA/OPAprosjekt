package com.booklibrary.catalog;

import com.booklibrary.model.CatalogBook;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Catalog provider for the free Open Library search API (openlibrary.org).
 *
 * Author: Farid Rahmani
 * Contribution: Implemented the Open Library search and the mapping from its JSON to {@link CatalogBook}.
 * Purpose: searches millions of books by title, author or subject and
 * turns each hit into a {@link CatalogBook} with cover image and details.
 */
public class OpenLibraryProvider implements LibraryProvider {

    private static final String SEARCH_URL = "https://openlibrary.org/search.json?limit=20&fields="
            + "key,title,author_name,first_publish_year,subject,cover_i,publisher,language,number_of_pages_median&q=";

    private final HttpFetcher fetcher;

    /**
     * Creates a provider that downloads data with the given fetcher.
     *
     * @param fetcher the object used to send HTTP requests
     */
    public OpenLibraryProvider(HttpFetcher fetcher) {
        this.fetcher = fetcher;
    }

    /**
     * Returns the catalog name.
     *
     * @return "Open Library"
     */
    @Override
    public String getName() {
        return "Open Library";
    }

    /**
     * Searches Open Library.
     *
     * @param query the search text
     * @return the matching books, possibly empty
     * @throws IOException          if the service cannot be reached
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    @Override
    public List<CatalogBook> search(String query) throws IOException, InterruptedException {
        String json = fetcher.get(SEARCH_URL + URLEncoder.encode(query, StandardCharsets.UTF_8));
        return parse(json);
    }

    /**
     * Converts Open Library's JSON answer to catalog books. Public so it can be unit-tested with fixed JSON.
     *
     * @param json the JSON text returned by the search API
     * @return the books found in the answer, possibly empty
     */
    public List<CatalogBook> parse(String json) {
        List<CatalogBook> books = new ArrayList<>();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonElement docsElement = root.get("docs");
        if (docsElement == null || !docsElement.isJsonArray()) {
            return books;
        }
        JsonArray docs = docsElement.getAsJsonArray();
        for (JsonElement element : docs) {
            JsonObject doc = element.getAsJsonObject();
            String title = JsonUtil.string(doc, "title");
            if (title == null || title.isBlank()) {
                continue;
            }
            String key = JsonUtil.string(doc, "key");
            Integer coverId = JsonUtil.integer(doc, "cover_i");
            List<String> publishers = JsonUtil.strings(doc, "publisher", 1);
            List<String> languages = JsonUtil.strings(doc, "language", 1);
            books.add(new CatalogBook(
                    getName(),
                    key,
                    title,
                    JsonUtil.strings(doc, "author_name", 5),
                    JsonUtil.integer(doc, "first_publish_year"),
                    null,
                    JsonUtil.strings(doc, "subject", 8),
                    coverId == null ? null : "https://covers.openlibrary.org/b/id/" + coverId + "-M.jpg",
                    JsonUtil.integer(doc, "number_of_pages_median"),
                    publishers.isEmpty() ? null : publishers.get(0),
                    languages.isEmpty() ? null : languages.get(0),
                    key == null ? null : "https://openlibrary.org" + key));
        }
        return books;
    }
}
