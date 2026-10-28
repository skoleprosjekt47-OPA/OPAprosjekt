package com.booklibrary.catalog;

import java.io.IOException;

/**
 * Abstraction over "download the text at this URL".
 *
 * Author: Farid Rahmani
 * Contribution: Designed the abstraction so catalog providers can be tested without internet access.
 * Purpose: lets the catalog providers get JSON from the internet through
 * a small interface. The real implementation uses java.net.http; tests
 * supply a fake that returns fixed JSON text.
 */
public interface HttpFetcher {

    /**
     * Downloads the text found at a URL using an HTTP GET request.
     *
     * @param url the full URL to fetch
     * @return the response body as text
     * @throws IOException          if the request fails or the server does not answer with status 200
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    String get(String url) throws IOException, InterruptedException;
}
