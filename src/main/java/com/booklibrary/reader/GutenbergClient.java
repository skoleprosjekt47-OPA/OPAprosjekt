package com.booklibrary.reader;

import com.booklibrary.catalog.HttpFetcher;
import com.booklibrary.catalog.JdkHttpFetcher;
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
 * Searches Project Gutenberg (free books whose copyright has expired) and downloads their text.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented the search and download of free books for the reader.
 * Purpose: uses the public Gutendex API (gutendex.com), which describes the
 * Project Gutenberg catalogue as JSON. The network access goes through
 * {@link HttpFetcher}, so the parsing can be tested without internet.
 */
public class GutenbergClient {

    private static final String SEARCH_URL = "https://gutendex.com/books/?languages=en&search=";

    private final HttpFetcher fetcher;

    /** Creates a client that uses the real internet. */
    public GutenbergClient() {
        this(new JdkHttpFetcher(java.time.Duration.ofSeconds(40)));
    }

    /**
     * Creates a client with a custom fetcher (used by tests).
     *
     * @param fetcher the object doing the HTTP requests
     */
    public GutenbergClient(HttpFetcher fetcher) {
        this.fetcher = fetcher;
    }

    /**
     * Searches for books by title or author.
     *
     * @param query the search text; blank returns the most popular books
     * @return up to about 32 matching books that have a plain text version
     * @throws IOException          if the request fails
     * @throws InterruptedException if the thread is interrupted
     */
    public List<GutenbergBook> search(String query) throws IOException, InterruptedException {
        String encoded = URLEncoder.encode(query == null ? "" : query.strip(), StandardCharsets.UTF_8);
        return parse(fetcher.get(SEARCH_URL + encoded));
    }

    /**
     * Downloads the full text of a book.
     *
     * @param book the book to download
     * @return the raw text including the Gutenberg header and footer
     * @throws IOException          if the request fails
     * @throws InterruptedException if the thread is interrupted
     */
    public String downloadText(GutenbergBook book) throws IOException, InterruptedException {
        return fetcher.get(book.textUrl());
    }

    /**
     * Reads a Gutendex response.
     *
     * @param json the JSON text returned by the API
     * @return the books that have a plain text version
     */
    public List<GutenbergBook> parse(String json) {
        List<GutenbergBook> books = new ArrayList<>();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray results = root.has("results") && root.get("results").isJsonArray()
                ? root.getAsJsonArray("results") : new JsonArray();
        for (JsonElement element : results) {
            JsonObject item = element.getAsJsonObject();
            int id = item.get("id").getAsInt();
            String title = text(item, "title", "Untitled");
            String author = "Unknown";
            if (item.has("authors") && item.get("authors").isJsonArray()
                    && item.getAsJsonArray("authors").size() > 0) {
                author = text(item.getAsJsonArray("authors").get(0).getAsJsonObject(), "name", "Unknown");
            }
            List<String> subjects = new ArrayList<>();
            if (item.has("subjects") && item.get("subjects").isJsonArray()) {
                for (JsonElement subject : item.getAsJsonArray("subjects")) {
                    subjects.add(subject.getAsString());
                }
            }
            String url = findTextUrl(item);
            if (url != null) {
                books.add(new GutenbergBook(id, title, author, subjects, url));
            }
        }
        return books;
    }

    private static String findTextUrl(JsonObject item) {
        if (!item.has("formats") || !item.get("formats").isJsonObject()) {
            return null;
        }
        String fallback = null;
        for (var entry : item.getAsJsonObject("formats").entrySet()) {
            String type = entry.getKey().toLowerCase();
            String url = entry.getValue().getAsString();
            if (type.startsWith("text/plain") && !url.endsWith(".zip")) {
                if (type.contains("utf-8")) {
                    return url;
                }
                fallback = url;
            }
        }
        return fallback;
    }

    private static String text(JsonObject object, String key, String fallback) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : fallback;
    }
}
