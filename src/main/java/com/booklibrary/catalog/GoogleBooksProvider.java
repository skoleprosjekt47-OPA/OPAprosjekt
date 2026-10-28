package com.booklibrary.catalog;

import com.booklibrary.model.CatalogBook;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Catalog provider for the Google Books search API.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented the Google Books search and the mapping from its JSON to {@link CatalogBook}.
 * Purpose: a second library catalog, which shows that the application can
 * search several libraries through the same {@link LibraryProvider} interface.
 */
public class GoogleBooksProvider implements LibraryProvider {

    private static final String SEARCH_URL = "https://www.googleapis.com/books/v1/volumes?maxResults=20&q=";

    private final HttpFetcher fetcher;

    /**
     * Creates a provider that downloads data with the given fetcher.
     *
     * @param fetcher the object used to send HTTP requests
     */
    public GoogleBooksProvider(HttpFetcher fetcher) {
        this.fetcher = fetcher;
    }

    /**
     * Returns the catalog name.
     *
     * @return "Google Books"
     */
    @Override
    public String getName() {
        return "Google Books";
    }

    /**
     * Searches Google Books.
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
     * Converts Google Books' JSON answer to catalog books. Public so it can be unit-tested with fixed JSON.
     *
     * @param json the JSON text returned by the search API
     * @return the books found in the answer, possibly empty
     */
    public List<CatalogBook> parse(String json) {
        List<CatalogBook> books = new ArrayList<>();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonElement itemsElement = root.get("items");
        if (itemsElement == null || !itemsElement.isJsonArray()) {
            return books;
        }
        for (JsonElement element : itemsElement.getAsJsonArray()) {
            JsonObject item = element.getAsJsonObject();
            JsonElement infoElement = item.get("volumeInfo");
            if (infoElement == null || !infoElement.isJsonObject()) {
                continue;
            }
            JsonObject info = infoElement.getAsJsonObject();
            String title = JsonUtil.string(info, "title");
            if (title == null || title.isBlank()) {
                continue;
            }
            String cover = null;
            JsonElement images = info.get("imageLinks");
            if (images != null && images.isJsonObject()) {
                cover = JsonUtil.string(images.getAsJsonObject(), "thumbnail");
                if (cover != null) {
                    cover = cover.replaceFirst("^http://", "https://");
                }
            }
            books.add(new CatalogBook(
                    getName(),
                    JsonUtil.string(item, "id"),
                    title,
                    JsonUtil.strings(info, "authors", 5),
                    yearOf(JsonUtil.string(info, "publishedDate")),
                    JsonUtil.string(info, "description"),
                    JsonUtil.strings(info, "categories", 8),
                    cover,
                    JsonUtil.integer(info, "pageCount"),
                    JsonUtil.string(info, "publisher"),
                    JsonUtil.string(info, "language"),
                    JsonUtil.string(info, "infoLink")));
        }
        return books;
    }

    /**
     * Extracts the year from a Google Books date such as "1949", "1949-06" or "1949-06-08".
     *
     * @param date the date text, may be {@code null}
     * @return the year, or {@code null} if it cannot be read
     */
    private Integer yearOf(String date) {
        if (date == null || date.length() < 4) {
            return null;
        }
        try {
            return Integer.parseInt(date.substring(0, 4));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
