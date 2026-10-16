package com.booklibrary.service;

import com.booklibrary.fakes.FakeBookDao;
import com.booklibrary.fakes.FakeBookshelfDao;
import com.booklibrary.fakes.FakeGenreDao;
import com.booklibrary.fakes.FakeWriterDao;
import com.booklibrary.model.Book;
import com.booklibrary.model.Bookshelf;
import com.booklibrary.model.Genre;
import com.booklibrary.model.ReadingStatus;
import com.booklibrary.model.Writer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Author: Farid Rahmani
 * Contribution: Wrote the unit tests for the service layer, using fake DAOs so the
 * tests run without a real MySQL database.
 * Purpose: verifies the business logic in {@link LibraryService} -
 * searching/filtering, the summary report, and basic CRUD delegation -
 * using in-memory fake DAOs instead of a real MySQL database.
 */
class LibraryServiceTest {

    private Writer tolkien;
    private Writer orwell;
    private Genre fantasy;
    private Genre dystopia;
    private Bookshelf livingRoom;
    private LibraryService service;

    @BeforeEach
    void setUp() {
        tolkien = new Writer(1, "J.R.R. Tolkien", "British");
        orwell = new Writer(2, "George Orwell", "British");
        fantasy = new Genre(1, "Fantasy", "Imaginary worlds");
        dystopia = new Genre(2, "Dystopia", "Bleak futures");
        livingRoom = new Bookshelf(1, "Living room shelf", "Living room", "A1");

        Book hobbit = new Book(1, "The Hobbit", 1937, ReadingStatus.READ, tolkien, fantasy, livingRoom);
        Book nineteenEightyFour = new Book(2, "1984", 1949, ReadingStatus.UNREAD, orwell, dystopia, livingRoom);
        Book animalFarm = new Book(3, "Animal Farm", 1945, ReadingStatus.READING, orwell, dystopia, livingRoom);

        FakeBookDao bookDao = new FakeBookDao().withBooks(List.of(hobbit, nineteenEightyFour, animalFarm));
        FakeWriterDao writerDao = new FakeWriterDao().withWriters(List.of(tolkien, orwell));
        FakeGenreDao genreDao = new FakeGenreDao().withGenres(List.of(fantasy, dystopia));
        FakeBookshelfDao bookshelfDao = new FakeBookshelfDao().withBookshelves(List.of(livingRoom));

        service = new LibraryService(bookDao, writerDao, genreDao, bookshelfDao);
    }

    @Test
    void getAllBooksReturnsEveryBook() throws SQLException {
        assertEquals(3, service.getAllBooks().size());
    }

    @Test
    void searchByTitleIsCaseInsensitive() throws SQLException {
        List<Book> results = service.searchBooks("hobbit", null, null, null, null);
        assertEquals(1, results.size());
        assertEquals("The Hobbit", results.get(0).getTitle());
    }

    @Test
    void searchByWriterFiltersCorrectly() throws SQLException {
        List<Book> results = service.searchBooks(null, orwell.getWriterId(), null, null, null);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(b -> b.getWriter().getWriterId() == orwell.getWriterId()));
    }

    @Test
    void searchByReadingStatusFiltersCorrectly() throws SQLException {
        List<Book> results = service.searchBooks(null, null, null, null, ReadingStatus.READING);
        assertEquals(1, results.size());
        assertEquals("Animal Farm", results.get(0).getTitle());
    }

    @Test
    void searchWithNoFiltersReturnsEverything() throws SQLException {
        assertEquals(3, service.searchBooks(null, null, null, null, null).size());
    }

    @Test
    void buildSummaryCountsBooksByStatusGenreAndWriter() throws SQLException {
        LibraryService.LibrarySummary summary = service.buildSummary();

        assertEquals(3, summary.totalBooks());
        assertEquals(1L, summary.byReadingStatus().get(ReadingStatus.READ.getDisplayLabel()));
        assertEquals(1L, summary.byReadingStatus().get(ReadingStatus.UNREAD.getDisplayLabel()));
        assertEquals(1L, summary.byReadingStatus().get(ReadingStatus.READING.getDisplayLabel()));
        assertEquals(2L, summary.byGenre().get("Dystopia"));
        assertEquals(1L, summary.byGenre().get("Fantasy"));
        assertEquals(2L, summary.byWriter().get("George Orwell"));
    }

    @Test
    void addBookAssignsAnIdAndMakesItFindable() throws SQLException {
        Book newBook = new Book("The Two Towers", 1954, ReadingStatus.UNREAD, tolkien, fantasy, livingRoom);
        Book saved = service.addBook(newBook);

        assertTrue(saved.getBookId() > 0);
        assertEquals(4, service.getAllBooks().size());
    }

    @Test
    void deleteBookRemovesItFromTheCollection() throws SQLException {
        service.deleteBook(1);
        assertEquals(2, service.getAllBooks().size());
        assertNull(service.getAllBooks().stream()
                .filter(b -> b.getBookId() == 1)
                .findFirst()
                .orElse(null));
    }
}
