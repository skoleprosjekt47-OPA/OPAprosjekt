package com.booklibrary.catalog;

import com.booklibrary.model.Adaptation;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Finds the films and series based on any book by asking Wikidata, the free
 * open knowledge base behind Wikipedia. No key is needed.
 *
 * <p>Two questions are asked: first which Wikidata items look like this book
 * (a search on the title, keeping items whose description sounds like a book
 * and, when possible, names the author), then which films and television
 * series are "based on" those items. Answers are kept in memory so each book
 * is only looked up once per run.</p>
 */
public class WikidataAdaptationFinder {

    private static final String API = "https://www.wikidata.org/w/api.php";
    private static final String SPARQL = "https://query.wikidata.org/sparql";
    private static final List<String> BOOK_WORDS = List.of("novel", "book", "novella", "story", "stories",
            "trilogy", "series of", "fantasy", "fiction", "play", "poem", "comic", "manga", "literary", "tale");

    private final HttpFetcher fetcher;
    private final Map<String, List<Adaptation>> cache = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Creates a finder.
     *
     * @param fetcher sends the web requests
     */
    public WikidataAdaptationFinder(HttpFetcher fetcher) {
        this.fetcher = fetcher;
    }

    /**
     * Finds films and series based on a book.
     *
     * @param title   the book title
     * @param authors the book's authors; may be empty
     * @return the adaptations oldest first, possibly empty if there are none
     * @throws IOException          if Wikidata cannot be reached or answers badly
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public List<Adaptation> find(String title, List<String> authors) throws IOException, InterruptedException {
        String clean = cleanTitle(title);
        if (clean.isBlank()) {
            return List.of();
        }
        String key = AdaptationCatalog.normalize(clean) + "|" + (authors.isEmpty() ? "" : authors.get(0));
        List<Adaptation> cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        List<String> ids = findBookIds(clean, authors);
        List<Adaptation> result = ids.isEmpty() ? List.of() : findAdaptations(ids);
        cache.put(key, result);
        return result;
    }

    /**
     * Removes a subtitle after a colon and trailing brackets so "Dune: Deluxe Edition" searches "Dune".
     *
     * @param title the title as the catalog gives it
     * @return the title to search for
     */
    static String cleanTitle(String title) {
        if (title == null) {
            return "";
        }
        String t = title.replaceAll("\\(.*?\\)", " ").trim();
        int colon = t.indexOf(':');
        return (colon > 2 ? t.substring(0, colon) : t).trim();
    }

    /**
     * Picks the Wikidata items that look like the book from a search answer.
     *
     * @param json    the answer of {@code wbsearchentities}
     * @param authors the book's authors; may be empty
     * @return Wikidata ids such as "Q15228"
     */
    static List<String> bookIdsFrom(String json, List<String> authors) {
        List<String> bookLike = new ArrayList<>();
        List<String> byAuthor = new ArrayList<>();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray found = root.has("search") ? root.getAsJsonArray("search") : new JsonArray();
        for (JsonElement e : found) {
            JsonObject o = e.getAsJsonObject();
            String id = JsonUtil.string(o, "id");
            String description = JsonUtil.string(o, "description");
            if (id == null || description == null) {
                continue;
            }
            String d = description.toLowerCase(Locale.ROOT);
            if (BOOK_WORDS.stream().noneMatch(d::contains) || d.contains("film") || d.contains("television")) {
                continue;
            }
            bookLike.add(id);
            for (String author : authors) {
                String[] parts = author.trim().split("\\s+");
                String surname = parts[parts.length - 1].toLowerCase(Locale.ROOT);
                if (surname.length() > 2 && d.contains(surname)) {
                    byAuthor.add(id);
                    break;
                }
            }
        }
        return byAuthor.isEmpty() ? bookLike : byAuthor;
    }

    private List<String> findBookIds(String title, List<String> authors) throws IOException, InterruptedException {
        String url = API + "?action=wbsearchentities&type=item&language=en&uselang=en&limit=10&format=json&search="
                + URLEncoder.encode(title, StandardCharsets.UTF_8);
        try {
            return bookIdsFrom(fetcher.get(url), authors);
        } catch (RuntimeException e) {
            throw new IOException("Unexpected answer from Wikidata", e);
        }
    }

    /**
     * Builds the question for films and series based on the given items.
     *
     * @param ids Wikidata ids of the book
     * @return a SPARQL query
     */
    static String query(List<String> ids) {
        StringBuilder values = new StringBuilder();
        for (String id : ids) {
            values.append("wd:").append(id).append(' ');
        }
        return "SELECT ?film ?filmLabel ?date ?isSeries WHERE { VALUES ?book { " + values + "} "
                + "?film wdt:P144 ?book . "
                + "{ ?film wdt:P31/wdt:P279* wd:Q11424 . BIND(false AS ?isSeries) } UNION "
                + "{ ?film wdt:P31/wdt:P279* wd:Q5398426 . BIND(true AS ?isSeries) } "
                + "OPTIONAL { ?film wdt:P577 ?d1 } OPTIONAL { ?film wdt:P580 ?d2 } "
                + "BIND(COALESCE(?d1, ?d2) AS ?date) "
                + "SERVICE wikibase:label { bd:serviceParam wikibase:language \"en\". } } LIMIT 60";
    }

    private List<Adaptation> findAdaptations(List<String> ids) throws IOException, InterruptedException {
        String url = SPARQL + "?format=json&query=" + URLEncoder.encode(query(ids), StandardCharsets.UTF_8);
        try {
            return adaptationsFrom(fetcher.get(url));
        } catch (RuntimeException e) {
            throw new IOException("Unexpected answer from Wikidata", e);
        }
    }

    /**
     * Turns a SPARQL answer into adaptations, merging duplicates and dropping unnamed items.
     *
     * @param json the SPARQL JSON answer
     * @return adaptations, oldest first
     */
    static List<Adaptation> adaptationsFrom(String json) {
        Map<String, Adaptation> byId = new LinkedHashMap<>();
        JsonArray rows = JsonParser.parseString(json).getAsJsonObject()
                .getAsJsonObject("results").getAsJsonArray("bindings");
        for (JsonElement e : rows) {
            JsonObject row = e.getAsJsonObject();
            String id = value(row, "film");
            String label = value(row, "filmLabel");
            if (id == null || label == null || label.matches("Q\\d+")) {
                continue;
            }
            String date = value(row, "date");
            Integer year = null;
            if (date != null && date.length() >= 4) {
                try {
                    year = Integer.parseInt(date.substring(0, 4));
                } catch (NumberFormatException ignored) {
                    year = null;
                }
            }
            boolean series = "true".equals(value(row, "isSeries"));
            Adaptation old = byId.get(id);
            if (old == null || (year != null && (old.year() == null || year < old.year()))) {
                byId.put(id, new Adaptation(label, series ? "series" : "film", year, null));
            }
        }
        List<Adaptation> list = new ArrayList<>(byId.values());
        list.sort(Comparator.comparing((Adaptation a) -> a.year() == null ? 9999 : a.year()).thenComparing(Adaptation::title));
        return list;
    }

    private static String value(JsonObject row, String key) {
        JsonElement e = row.get(key);
        if (e == null || !e.isJsonObject()) {
            return null;
        }
        return JsonUtil.string(e.getAsJsonObject(), "value");
    }
}
