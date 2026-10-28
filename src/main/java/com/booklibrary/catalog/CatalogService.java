package com.booklibrary.catalog;

import com.booklibrary.model.CatalogBook;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Searches several library catalogs at the same time.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented the parallel search over all providers and the error handling per provider.
 * Purpose: the single entry point the user interface uses to find books in
 * the outside world. Each provider is searched in its own thread, and a
 * failing provider only adds an error message instead of stopping the search.
 */
public class CatalogService {

    private static final int WAIT_SECONDS = 10;
    private static final int FREE_WAIT_SECONDS = 45;
    private static final int QUICK_WAIT_SECONDS = 6;

    private final List<LibraryProvider> providers;
    private final java.util.Map<String, CatalogSearchResult> cache = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Creates a service with the real catalogs Open Library, Google Books and Project Gutenberg.
     */
    public CatalogService() {
        this(defaultProviders());
    }

    /**
     * Creates a service with the given catalogs (used by tests with fake providers).
     *
     * @param providers the catalogs to search
     */
    public CatalogService(List<LibraryProvider> providers) {
        this.providers = List.copyOf(providers);
    }

    /**
     * Builds the list of real providers that share one HTTP fetcher.
     *
     * @return Open Library, Google Books and Project Gutenberg (free books)
     */
    private static List<LibraryProvider> defaultProviders() {
        HttpFetcher fetcher = new JdkHttpFetcher();
        HttpFetcher slowFetcher = new JdkHttpFetcher(java.time.Duration.ofSeconds(40));
        return List.of(new OpenLibraryProvider(fetcher), new GoogleBooksProvider(fetcher),
                new GutenbergProvider(slowFetcher));
    }

    /**
     * Returns the names of the catalogs this service searches.
     *
     * @return the catalog names in search order
     */
    public List<String> getProviderNames() {
        return providers.stream().map(LibraryProvider::getName).toList();
    }

    /**
     * Searches all catalogs in parallel and merges the answers.
     *
     * @param query the search text; a blank text gives an empty result
     * @return the books found and the errors from catalogs that failed
     */
    public CatalogSearchResult search(String query) {
        return search(query, false);
    }

    /**
     * Searches only the catalogs that offer free books to read in full (Project Gutenberg).
     *
     * @param query the search text; a blank text gives an empty result
     * @return the free books found and the errors from catalogs that failed
     */
    public CatalogSearchResult searchFree(String query) {
        return search(query, true);
    }

    private CatalogSearchResult search(String query, boolean freeOnly) {
        return search(query, freeOnly, partial -> { });
    }


    /**
     * Searches all catalogs in parallel and reports what has arrived each time one catalog
     * answers, so the screen can show the fast catalogs without waiting for the slowest.
     *
     * @param query    the search text; a blank text gives an empty result
     * @param freeOnly {@code true} to ask only the catalog with free books
     * @param progress called (from a background thread) with everything found so far
     * @return the final result, with the catalogs in their usual order
     */
    public CatalogSearchResult search(String query, boolean freeOnly,
                                      java.util.function.Consumer<CatalogSearchResult> progress) {
        return run(query, freeOnly ? List.of(CatalogBook.GUTENBERG) : null,
                freeOnly ? FREE_WAIT_SECONDS : WAIT_SECONDS, progress);
    }

    /**
     * A fast search for small jobs such as the hover preview: it skips the slow free-books
     * catalog and gives up waiting after a few seconds.
     *
     * @param query the search text
     * @return the books that arrived in time, possibly empty
     */
    public CatalogSearchResult searchQuick(String query) {
        List<String> names = providers.stream().map(LibraryProvider::getName)
                .filter(n -> !CatalogBook.GUTENBERG.equals(n)).toList();
        return run(query, names, QUICK_WAIT_SECONDS, partial -> { });
    }

    private CatalogSearchResult run(String query, List<String> only, int waitSeconds,
                                    java.util.function.Consumer<CatalogSearchResult> progress) {
        if (query == null || query.isBlank()) {
            return new CatalogSearchResult(List.of(), List.of());
        }
        String trimmed = query.trim();
        String cacheKey = trimmed.toLowerCase(java.util.Locale.ROOT) + "|" + only;
        CatalogSearchResult cached = cache.get(cacheKey);
        if (cached != null) {
            progress.accept(cached);
            return cached;
        }

        Object lock = new Object();
        List<CatalogBook> sofarBooks = new ArrayList<>();
        List<String> sofarErrors = new ArrayList<>();
        List<LibraryProvider> asked = new ArrayList<>();
        List<CompletableFuture<ProviderAnswer>> futures = new ArrayList<>();
        for (LibraryProvider provider : providers) {
            if (only != null && !only.contains(provider.getName())) {
                continue;
            }
            asked.add(provider);
            futures.add(CompletableFuture.supplyAsync(() -> ask(provider, trimmed)).thenApply(answer -> {
                CatalogSearchResult snapshot;
                synchronized (lock) {
                    sofarBooks.addAll(answer.books());
                    if (answer.error() != null) {
                        sofarErrors.add(answer.error());
                    }
                    snapshot = new CatalogSearchResult(List.copyOf(sofarBooks), List.copyOf(sofarErrors));
                }
                progress.accept(snapshot);
                return answer;
            }));
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(waitSeconds, java.util.concurrent.TimeUnit.SECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            // Give up waiting for the slow ones; they keep reporting through "progress" if they finish later.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (java.util.concurrent.ExecutionException e) {
            // ask() never throws, so nothing to do.
        }

        List<CatalogBook> books = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < futures.size(); i++) {
            CompletableFuture<ProviderAnswer> future = futures.get(i);
            if (!future.isDone()) {
                errors.add(asked.get(i).getName() + ": still loading, showing the rest");
                continue;
            }
            ProviderAnswer answer = future.join();
            books.addAll(answer.books());
            if (answer.error() != null) {
                errors.add(answer.error());
            }
        }
        CatalogSearchResult result = new CatalogSearchResult(books, errors);
        if (errors.isEmpty()) {
            cache.put(cacheKey, result);
        }
        return result;
    }

    /**
     * Asks one provider and converts any failure into an error message.
     *
     * @param provider the catalog to search
     * @param query    the search text
     * @return the books, or an error message if the catalog failed
     */
    private ProviderAnswer ask(LibraryProvider provider, String query) {
        try {
            return new ProviderAnswer(provider.search(query), null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ProviderAnswer(List.of(), provider.getName() + ": search was interrupted");
        } catch (Exception e) {
            return new ProviderAnswer(List.of(), provider.getName() + ": " + e.getMessage());
        }
    }

    /**
     * One provider's answer: books or an error message.
     *
     * @param books the books found
     * @param error the error message, or {@code null} if the search worked
     */
    private record ProviderAnswer(List<CatalogBook> books, String error) {
    }
}
