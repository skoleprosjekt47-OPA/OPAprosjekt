package com.booklibrary.catalog;

import com.booklibrary.model.CatalogBook;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the Open Library and Google Books providers.
 *
 * Author: Farid Rahmani
 * Contribution: Wrote the tests that check the JSON mapping and the search URLs.
 * Purpose: proves that fixed JSON answers from each catalog are turned into
 * correct {@link CatalogBook} objects, without any internet connection.
 */
class CatalogProvidersTest {

    private static final String OPEN_LIBRARY_JSON = """
            {"numFound": 2, "docs": [
              {"key": "/works/OL1W", "title": "Nineteen Eighty-Four", "author_name": ["George Orwell"],
               "first_publish_year": 1949, "subject": ["Dystopia", "Totalitarianism"], "cover_i": 123,
               "publisher": ["Secker & Warburg"], "language": ["eng"], "number_of_pages_median": 328},
              {"key": "/works/OL2W", "title": "Untitled data"},
              {"title": ""}
            ]}""";

    private static final String GOOGLE_JSON = """
            {"items": [
              {"id": "abc", "volumeInfo": {"title": "Animal Farm", "authors": ["George Orwell"],
               "publishedDate": "1945-08", "description": "A fable.", "categories": ["Fiction"],
               "pageCount": 112, "imageLinks": {"thumbnail": "http://books.google.com/x.jpg"},
               "language": "en", "infoLink": "https://books.google.com/abc"}},
              {"id": "def", "volumeInfo": {"title": "No Details"}},
              {"id": "ghi"}
            ]}""";

    @Test
    void openLibraryMapsAllFields() {
        List<CatalogBook> books = new OpenLibraryProvider(url -> "").parse(OPEN_LIBRARY_JSON);

        assertEquals(2, books.size());
        CatalogBook first = books.get(0);
        assertEquals("Open Library", first.source());
        assertEquals("Nineteen Eighty-Four", first.title());
        assertEquals("George Orwell", first.firstAuthor());
        assertEquals(1949, first.year());
        assertEquals("Dystopia", first.firstSubject());
        assertEquals("https://covers.openlibrary.org/b/id/123-M.jpg", first.coverUrl());
        assertEquals("https://openlibrary.org/works/OL1W", first.infoUrl());
        assertEquals(328, first.pageCount());
        assertEquals("Secker & Warburg", first.publisher());
    }

    @Test
    void openLibraryToleratesMissingFields() {
        CatalogBook sparse = new OpenLibraryProvider(url -> "").parse(OPEN_LIBRARY_JSON).get(1);

        assertEquals("Untitled data", sparse.title());
        assertNull(sparse.firstAuthor());
        assertNull(sparse.year());
        assertNull(sparse.coverUrl());
        assertEquals("", sparse.authorsText());
    }

    @Test
    void openLibraryEncodesTheQueryInTheUrl() throws Exception {
        String[] requested = new String[1];
        new OpenLibraryProvider(url -> {
            requested[0] = url;
            return "{\"docs\": []}";
        }).search("war & peace");

        assertTrue(requested[0].startsWith("https://openlibrary.org/search.json"));
        assertTrue(requested[0].endsWith("q=war+%26+peace"));
    }

    @Test
    void googleBooksMapsAllFieldsAndUsesHttpsCover() {
        List<CatalogBook> books = new GoogleBooksProvider(url -> "").parse(GOOGLE_JSON);

        assertEquals(2, books.size());
        CatalogBook first = books.get(0);
        assertEquals("Google Books", first.source());
        assertEquals("Animal Farm", first.title());
        assertEquals(1945, first.year());
        assertEquals("A fable.", first.description());
        assertEquals("Fiction", first.firstSubject());
        assertEquals("https://books.google.com/x.jpg", first.coverUrl());
        assertNull(books.get(1).year());
    }

    @Test
    void emptyAnswersGiveNoBooks() {
        assertTrue(new OpenLibraryProvider(url -> "").parse("{}").isEmpty());
        assertTrue(new GoogleBooksProvider(url -> "").parse("{\"totalItems\": 0}").isEmpty());
    }
}
