package com.booklibrary.catalog;

import com.booklibrary.model.CatalogBook;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link CatalogService} using fake providers.
 *
 * Author: Farid Rahmani
 * Contribution: Wrote the tests for the parallel search and the error handling.
 * Purpose: checks that results from several catalogs are merged and that one
 * failing catalog does not stop the others.
 */
class CatalogServiceTest {

    private static CatalogBook book(String source, String title) {
        return new CatalogBook(source, "id", title, List.of("Someone"), 2000, null, List.of(), null, null, null, null, null);
    }

    private static LibraryProvider working(String name, String title) {
        return new LibraryProvider() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public List<CatalogBook> search(String query) {
                return List.of(book(name, title));
            }
        };
    }

    private static LibraryProvider failing(String name) {
        return new LibraryProvider() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public List<CatalogBook> search(String query) throws IOException {
                throw new IOException("offline");
            }
        };
    }

    @Test
    void mergesResultsFromAllProvidersInOrder() {
        CatalogService service = new CatalogService(List.of(working("A", "First"), working("B", "Second")));

        CatalogSearchResult result = service.search("anything");

        assertEquals(2, result.books().size());
        assertEquals("First", result.books().get(0).title());
        assertEquals("Second", result.books().get(1).title());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void oneFailingProviderOnlyAddsAnError() {
        CatalogService service = new CatalogService(List.of(failing("A"), working("B", "Second")));

        CatalogSearchResult result = service.search("anything");

        assertEquals(1, result.books().size());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().get(0).startsWith("A:"));
    }

    @Test
    void blankQueryGivesEmptyResultWithoutCallingProviders() {
        CatalogService service = new CatalogService(List.of(failing("A")));

        CatalogSearchResult result = service.search("   ");

        assertTrue(result.books().isEmpty());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void listsProviderNames() {
        assertEquals(List.of("A", "B"), new CatalogService(List.of(working("A", "x"), working("B", "y"))).getProviderNames());
    }

    @Test
    void reportsEachCatalogAsItAnswers() {
        CatalogService service = new CatalogService(List.of(working("A", "First"), working("B", "Second")));
        List<Integer> sizes = new java.util.concurrent.CopyOnWriteArrayList<>();
        CatalogSearchResult result = service.search("anything", false, partial -> sizes.add(partial.books().size()));
        assertEquals(2, sizes.size());
        assertEquals(2, sizes.stream().mapToInt(Integer::intValue).max().orElse(0));
        assertEquals(2, result.books().size());
    }

    @Test
    void quickSearchSkipsGutenbergAndCachesCleanAnswers() {
        int[] calls = {0};
        LibraryProvider counting = new LibraryProvider() {
            @Override
            public String getName() {
                return "Fast";
            }

            @Override
            public List<CatalogBook> search(String query) {
                calls[0]++;
                return List.of(book("Fast", "Hit"));
            }
        };
        LibraryProvider gutenberg = working(CatalogBook.GUTENBERG, "Free");
        CatalogService service = new CatalogService(List.of(counting, gutenberg));
        assertEquals(1, service.searchQuick("dune").books().size());
        service.searchQuick("DUNE");
        assertEquals(1, calls[0]);
    }
}
