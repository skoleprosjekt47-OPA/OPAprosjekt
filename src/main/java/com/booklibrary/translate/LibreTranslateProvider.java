package com.booklibrary.translate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.Map;

/**
 * Translation with LibreTranslate, an open source translation server. The address is read from
 * {@code LIBRETRANSLATE_URL} (for example {@code http://localhost:5000}) and an optional key from
 * {@code LIBRETRANSLATE_API_KEY}.
 *
 * Author: Farid Rahmani
 * Contribution: Integrated LibreTranslate as a free option that can run on your own computer.
 * Purpose: it costs nothing and sends no text to a company, but the quality is lower than DeepL or AI.
 */
public class LibreTranslateProvider implements TranslationProvider {

    /** The languages a standard LibreTranslate server usually offers. A server can have more or fewer. */
    private static final java.util.Set<String> COMMON = java.util.Set.of("nb", "en", "ar", "zh", "cs", "da", "nl", "fi",
            "fr", "de", "el", "he", "hi", "hu", "id", "it", "ja", "ko", "fa", "pl", "pt", "ru", "sk", "es", "sv", "tr",
            "uk", "vi");

    private final String url;
    private final String key;
    private final JsonPoster poster;

    /**
     * Creates the provider.
     *
     * @param url    the server address, or {@code null}
     * @param key    the key if the server needs one, or {@code null}
     * @param poster the HTTP sender
     */
    public LibreTranslateProvider(String url, String key, JsonPoster poster) {
        String clean = url == null ? "" : url.strip();
        this.url = clean.endsWith("/") ? clean.substring(0, clean.length() - 1) : clean;
        this.key = key == null ? "" : key.strip();
        this.poster = poster;
    }

    @Override
    public String displayName() {
        return "LibreTranslate";
    }

    @Override
    public boolean isConfigured() {
        return !url.isEmpty();
    }

    @Override
    public String setupHint() {
        return "set the environment variable LIBRETRANSLATE_URL";
    }

    @Override
    public boolean supports(Language language) {
        return COMMON.contains(language.code());
    }

    @Override
    public int maxChunkChars() {
        return 2000;
    }

    @Override
    public String translate(String text, Language target) throws IOException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("q", text);
        body.addProperty("source", "auto");
        body.addProperty("target", target.code());
        body.addProperty("format", "text");
        if (!key.isEmpty()) {
            body.addProperty("api_key", key);
        }
        String answer = poster.post(url + "/translate", Map.of(), body.toString());
        try {
            return JsonParser.parseString(answer).getAsJsonObject().get("translatedText").getAsString();
        } catch (RuntimeException e) {
            throw new TranslationException("LibreTranslate sent an answer that could not be read.", false);
        }
    }

    @Override
    public String toString() {
        return displayName();
    }
}
