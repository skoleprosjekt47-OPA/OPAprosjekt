package com.booklibrary.translate;

import java.io.IOException;
import java.util.Map;

/**
 * Sends a JSON request to a web service. It exists so that the translation services can be
 * tested without the internet, by giving them a fake poster.
 */
public interface JsonPoster {

    /**
     * Sends a POST request.
     *
     * @param url     the address
     * @param headers extra headers such as the key
     * @param body    the JSON text
     * @return the response body
     * @throws IOException          if the request fails; a {@link TranslationException} for bad status codes
     * @throws InterruptedException if the thread is interrupted
     */
    String post(String url, Map<String, String> headers, String body) throws IOException, InterruptedException;
}
