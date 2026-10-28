package com.booklibrary.translate;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * {@link JsonPoster} that uses the HTTP client built into Java.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented the HTTPS requests to the translation services.
 * Purpose: gives clear messages for the common problems: wrong key, no credit, too many requests.
 */
public class JdkJsonPoster implements JsonPoster {

    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();

    @Override
    public String post(String url, Map<String, String> headers, String body) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("User-Agent", "PrivateBookLibraryManager/1.0 (student project, USN OAP200V)")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        headers.forEach(builder::header);
        HttpResponse<String> response;
        try {
            response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (java.net.http.HttpTimeoutException e) {
            throw new TranslationException("No answer from " + URI.create(url).getHost() + " in time.", true);
        }
        int status = response.statusCode();
        if (status == 200) {
            return response.body();
        }
        String host = URI.create(url).getHost();
        if (status == 401 || status == 403) {
            throw new TranslationException("The key was not accepted by " + host + " (HTTP " + status
                    + "). Check the key.", false);
        }
        if (status == 456 || status == 402) {
            throw new TranslationException("No translation credit left at " + host + " (HTTP " + status + ").", false);
        }
        if (status == 429 || status >= 500) {
            throw new TranslationException(host + " is busy or the free limit is used up (HTTP " + status + "). Wait a while and try again.", true);
        }
        String detail = response.body() == null ? "" : response.body();
        throw new TranslationException("HTTP " + status + " from " + host + ": "
                + detail.substring(0, Math.min(200, detail.length())), false);
    }
}
