package com.booklibrary.catalog;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Small helpers for reading optional fields from Gson objects.
 *
 * Author: Farid Rahmani
 * Contribution: Wrote the null-safe JSON helpers used by the catalog providers.
 * Purpose: catalog APIs leave out fields they do not know, so every read
 * must tolerate missing or null values instead of throwing.
 */
final class JsonUtil {

    private JsonUtil() {
    }

    /**
     * Reads a text field.
     *
     * @param object the JSON object
     * @param key    the field name
     * @return the text, or {@code null} if the field is missing or null
     */
    static String string(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element == null || element.isJsonNull() || !element.isJsonPrimitive() ? null : element.getAsString();
    }

    /**
     * Reads a whole-number field.
     *
     * @param object the JSON object
     * @param key    the field name
     * @return the number, or {@code null} if the field is missing or not a number
     */
    static Integer integer(JsonObject object, String key) {
        String text = string(object, key);
        if (text == null) {
            return null;
        }
        try {
            return (int) Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Reads a list of texts, keeping at most a given number of entries.
     *
     * @param object the JSON object
     * @param key    the field name
     * @param max    the maximum number of entries to keep
     * @return the texts, or an empty list if the field is missing
     */
    static List<String> strings(JsonObject object, String key, int max) {
        List<String> result = new ArrayList<>();
        JsonElement element = object.get(key);
        if (element != null && element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement item : array) {
                if (result.size() >= max) {
                    break;
                }
                if (item.isJsonPrimitive()) {
                    result.add(item.getAsString());
                }
            }
        }
        return result;
    }
}
