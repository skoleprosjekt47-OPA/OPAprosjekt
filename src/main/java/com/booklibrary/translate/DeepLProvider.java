package com.booklibrary.translate;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.Map;

/**
 * Translation with DeepL. The key is read from the environment variable {@code DEEPL_API_KEY}.
 *
 * Author: Farid Rahmani
 * Contribution: Integrated the DeepL API.
 * Purpose: DeepL gives good natural translations and has a free plan with a monthly limit.
 * A free key ends with ":fx" and uses a different address.
 */
public class DeepLProvider implements TranslationProvider {

    private final String key;
    private final JsonPoster poster;

    /**
     * Creates the provider.
     *
     * @param key    the DeepL key, or {@code null}
     * @param poster the HTTP sender
     */
    public DeepLProvider(String key, JsonPoster poster) {
        this.key = key == null ? "" : key.strip();
        this.poster = poster;
    }

    @Override
    public String displayName() {
        return "DeepL";
    }

    @Override
    public boolean isConfigured() {
        return !key.isEmpty();
    }

    @Override
    public String setupHint() {
        return "set the environment variable DEEPL_API_KEY";
    }

    @Override
    public boolean supports(Language language) {
        return language.deeplCode() != null;
    }

    @Override
    public int maxChunkChars() {
        return 4000;
    }

    @Override
    public String translate(String text, Language target) throws IOException, InterruptedException {
        String host = key.endsWith(":fx") ? "api-free.deepl.com" : "api.deepl.com";
        JsonObject body = new JsonObject();
        JsonArray texts = new JsonArray();
        texts.add(text);
        body.add("text", texts);
        body.addProperty("target_lang", target.deeplCode());
        body.addProperty("preserve_formatting", true);
        String answer = poster.post("https://" + host + "/v2/translate",
                Map.of("Authorization", "DeepL-Auth-Key " + key), body.toString());
        try {
            return JsonParser.parseString(answer).getAsJsonObject().getAsJsonArray("translations").get(0)
                    .getAsJsonObject().get("text").getAsString();
        } catch (RuntimeException e) {
            throw new TranslationException("DeepL sent an answer that could not be read.", false);
        }
    }

    @Override
    public String toString() {
        return displayName();
    }
}
