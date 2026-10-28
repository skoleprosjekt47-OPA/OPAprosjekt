package com.booklibrary.catalog;

import com.booklibrary.model.Adaptation;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A small bundled list of well-known books and the films and series based on them.
 * It is the fallback when the online film database ({@link WikidataAdaptationFinder})
 * cannot be reached.
 *
 * <p>Author: Farid Rahmani (team 10), written with help from Claude (AI).</p>
 */
public class AdaptationCatalog {

    private static final String BUNDLED = "/data/adaptations.json";
    private final List<Entry> entries = new ArrayList<>();

    private record Entry(List<String> match, List<Adaptation> adaptations) { }

    /**
     * Creates a catalog from the bundled list of well-known adaptations. It is used as a
     * fallback when the online film database cannot be reached.
     *
     * @return the loaded catalog
     */
    public static AdaptationCatalog load() {
        AdaptationCatalog catalog = new AdaptationCatalog();
        try (InputStream in = AdaptationCatalog.class.getResourceAsStream(BUNDLED)) {
            if (in != null) {
                catalog.add(read(new InputStreamReader(in, StandardCharsets.UTF_8)));
            }
        } catch (IOException | RuntimeException e) {
            // A broken bundled list must never stop the app.
        }
        return catalog;
    }

    /**
     * Finds the film and series adaptations of a book.
     *
     * @param bookTitle the title of the book, as the catalog gives it
     * @return the adaptations, possibly empty; never {@code null}
     */
    public List<Adaptation> forBook(String bookTitle) {
        String title = normalize(bookTitle);
        Map<String, Adaptation> found = new LinkedHashMap<>();
        if (!title.isEmpty()) {
            for (Entry entry : entries) {
                if (entry.match().stream().anyMatch(m -> (" " + title + " ").contains(" " + m + " "))) {
                    for (Adaptation a : entry.adaptations()) {
                        found.put(normalize(a.title()), a);
                    }
                }
            }
        }
        return new ArrayList<>(found.values());
    }

    /**
     * Chooses what to show for a book: the online results, or the bundled list if nothing was
     * found online (for example when offline).
     *
     * @param bookTitle the book's title
     * @param online    what the film database found; may be empty
     * @return the adaptations to show, never {@code null}
     */
    public List<Adaptation> combine(String bookTitle, List<Adaptation> online) {
        return online.isEmpty() ? forBook(bookTitle) : new ArrayList<>(online);
    }

    /**
     * Removes case, accents-free punctuation and extra spaces so titles can be compared.
     *
     * @param text any title
     * @return lower-case letters, digits and single spaces
     */
    static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", " ").trim();
    }

    private static JsonArray read(java.io.Reader reader) {
        JsonElement root = JsonParser.parseReader(reader);
        return root.isJsonArray() ? root.getAsJsonArray() : new JsonArray();
    }

    private void add(JsonArray array) {
        for (JsonElement element : array) {
            try {
                JsonObject obj = element.getAsJsonObject();
                List<String> match = new ArrayList<>();
                for (JsonElement m : obj.getAsJsonArray("match")) {
                    String n = normalize(m.getAsString());
                    if (!n.isEmpty()) {
                        match.add(n);
                    }
                }
                List<Adaptation> list = new ArrayList<>();
                for (JsonElement a : obj.getAsJsonArray("adaptations")) {
                    JsonObject ao = a.getAsJsonObject();
                    String title = ao.get("title").getAsString();
                    String type = ao.has("type") ? ao.get("type").getAsString() : "film";
                    Integer year = ao.has("year") && !ao.get("year").isJsonNull() ? ao.get("year").getAsInt() : null;
                    list.add(new Adaptation(title, type, year, null));
                }
                if (!match.isEmpty() && !list.isEmpty()) {
                    entries.add(new Entry(match, list));
                }
            } catch (RuntimeException e) {
                // Skip one broken entry, keep the others.
            }
        }
    }
}
