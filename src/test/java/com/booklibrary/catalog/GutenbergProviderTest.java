package com.booklibrary.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.booklibrary.model.CatalogBook;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the free book source (Project Gutenberg) in the Discover search.
 *
 * Author: Farid Rahmani
 * Contribution: Wrote the tests for the free book provider.
 */
class GutenbergProviderTest {

    private static final String JSON = """
            {"results":[{"id":84,"title":"Frankenstein","authors":[{"name":"Shelley, Mary Wollstonecraft"}],
             "subjects":["Horror tales"],
             "formats":{"text/plain; charset=utf-8":"https://x/84.txt.utf-8"}}]}""";

    @Test
    void turnsResultsIntoFreeCatalogBooks() throws Exception {
        List<CatalogBook> books = new GutenbergProvider(url -> JSON).search("frankenstein");
        assertEquals(1, books.size());
        CatalogBook book = books.get(0);
        assertEquals("Frankenstein", book.title());
        assertEquals(List.of("Mary Wollstonecraft Shelley"), book.authors());
        assertTrue(book.isFreeToRead());
        assertEquals("https://www.gutenberg.org/cache/epub/84/pg84.txt", book.freeTextUrl());
    }

    @Test
    void booksFromOtherSourcesAreNotFree() {
        CatalogBook book = new CatalogBook("Open Library", "OL1W", "Title", List.of(), null, null,
                List.of(), null, null, null, null, null);
        assertFalse(book.isFreeToRead());
        assertEquals(null, book.freeTextUrl());
    }

    @Test
    void usesBuiltInListWhenTheOnlineCatalogueFails() throws Exception {
        GutenbergProvider provider = new GutenbergProvider(url -> {
            throw new java.io.IOException("request timed out");
        });
        List<CatalogBook> books = provider.search("Treasure Island");
        assertEquals(1, books.size());
        assertTrue(books.get(0).isFreeToRead());
        assertEquals("120", books.get(0).externalId());
    }

    @Test
    void failsWhenOnlineCatalogueFailsAndNothingIsBuiltIn() {
        GutenbergProvider provider = new GutenbergProvider(url -> {
            throw new java.io.IOException("request timed out");
        });
        org.junit.jupiter.api.Assertions.assertThrows(java.io.IOException.class, () -> provider.search("zzzxxyy"));
    }
}
