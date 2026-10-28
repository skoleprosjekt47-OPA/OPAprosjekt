package com.booklibrary.catalog;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * {@link HttpFetcher} implementation based on the HTTP client built into Java 11 and later.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented the HTTP access used by the catalog search.
 * Purpose: sends real GET requests with a timeout and a User-Agent
 * header, as the Open Library API terms ask clients to identify themselves.
 */
public class JdkHttpFetcher implements HttpFetcher {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final Duration requestTimeout;

    /** Creates a fetcher that waits at most 15 seconds for an answer. */
    public JdkHttpFetcher() {
        this(Duration.ofSeconds(15));
    }

    /**
     * Creates a fetcher with a custom waiting time, for slow servers.
     *
     * @param requestTimeout how long to wait for an answer
     */
    public JdkHttpFetcher(Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    /**
     * Downloads the text found at a URL.
     *
     * @param url the full URL to fetch
     * @return the response body as text
     * @throws IOException          if the request fails or the status code is not 200
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    @Override
    public String get(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(requestTimeout)
                .header("User-Agent", "PrivateBookLibraryManager/1.0 (student project, USN OAP200V)")
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " from " + request.uri().getHost());
        }
        return response.body();
    }
}
