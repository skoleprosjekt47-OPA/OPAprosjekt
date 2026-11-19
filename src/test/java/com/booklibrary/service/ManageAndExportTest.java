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
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Author: Team 10 (set this to the member who owns the tests)
 * Contribution: Wrote the unit tests for managing writers, genres and bookshelves
 * and for the CSV export.
 * Purpose: verifies the new service methods in {@link LibraryService} using
 * in-memory fake DAOs, so no MySQL database is needed.
 */
class ManageAndExportTest {

    private Writer orwell;
    private Genre dystopia;
    private Bookshelf livingRoom;
    private LibraryService service;

    @BeforeEach
    void setUp() {
        orwell = new Writer(1, "George Orwell", "British");
        dystopia = new Genre(1, "Dystopia", "Bleak futures");
        livingRoom = new Bookshelf(1, "Living room shelf", "Living room", "A1");
        Book nineteenEightyFour = new Book(1, "1984", 1949, ReadingStatus.UNREAD, orwell, dystopia, livingRoom);
        Book tricky = new Book(2, "Hello, \"World\"", 2000, ReadingStatus.READ, null, null, null);

        service = new LibraryService(
                new FakeBookDao().withBooks(List.of(nineteenEightyFour, tricky)),
                new FakeWriterDao().withWriters(List.of(orwell)),
                new FakeGenreDao().withGenres(List.of(dystopia)),
                new FakeBookshelfDao().withBookshelves(List.of(livingRoom)));
    }

    @Test
    void deleteWriterRemovesItFromTheList() throws SQLException {
        service.deleteWriter(1);
        assertTrue(service.getAllWriters().isEmpty());
    }

    @Test
    void addGenreMakesItListed() throws SQLException {
        service.addGenre(new Genre(2, "Fantasy", "Imaginary worlds"));
        assertEquals(2, service.getAllGenres().size());
    }

    @Test
    void deleteBookshelfRemovesItFromTheList() throws SQLException {
        service.deleteBookshelf(1);
        assertTrue(service.getAllBookshelves().isEmpty());
    }

    @Test
    void csvHasAHeaderAndOneRowPerBook() throws SQLException {
        String csv = LibraryService.toCsv(service.getAllBooks());
        String[] lines = csv.split("\n");

        assertEquals("Title,Year,Writer,Genre,Bookshelf,Status", lines[0]);
        assertEquals("1984,1949,George Orwell,Dystopia,Living room shelf,Unread", lines[1]);
        assertEquals(3, lines.length);
    }

    @Test
    void csvQuotesValuesWithCommasAndQuotes() throws SQLException {
        String csv = LibraryService.toCsv(service.getAllBooks());

        assertTrue(csv.contains("\"Hello, \"\"World\"\"\",2000,,,,Read"));
    }

    @Test
    void exportWritesTheCsvFile(@TempDir Path folder) throws SQLException, IOException {
        Path file = folder.resolve("books.csv");

        int count = service.exportBooksToCsv(file);

        assertEquals(2, count);
        assertTrue(Files.readString(file, StandardCharsets.UTF_8).startsWith("Title,Year,"));
    }
}
