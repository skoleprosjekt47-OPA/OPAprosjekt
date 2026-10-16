package com.booklibrary.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Author: Farid Rahmani
 * Contribution: Wrote the unit tests for the domain model classes.
 * Purpose: verifies the two {@link Book} constructors behave as
 * documented - the id-less one must default bookId to 0, ready for an
 * INSERT to assign a real one.
 */
class BookTest {

    @Test
    void noArgIdConstructorDefaultsBookIdToZero() {
        Writer writer = new Writer("Jane Austen", "British");
        Genre genre = new Genre("Classic", "Timeless literature");
        Bookshelf shelf = new Bookshelf("Study shelf", "Study", "B2");

        Book book = new Book("Emma", 1815, ReadingStatus.UNREAD, writer, genre, shelf);

        assertEquals(0, book.getBookId());
        assertEquals("Emma", book.getTitle());
        assertEquals(1815, book.getReleaseYear());
        assertEquals(ReadingStatus.UNREAD, book.getReadingStatus());
    }

    @Test
    void fullConstructorKeepsTheGivenBookId() {
        Writer writer = new Writer(5, "Jane Austen", "British");
        Genre genre = new Genre(5, "Classic", "Timeless literature");
        Bookshelf shelf = new Bookshelf(5, "Study shelf", "Study", "B2");

        Book book = new Book(42, "Emma", 1815, ReadingStatus.READ, writer, genre, shelf);

        assertEquals(42, book.getBookId());
        assertEquals(writer, book.getWriter());
        assertEquals(genre, book.getGenre());
        assertEquals(shelf, book.getBookshelf());
    }

    @Test
    void toStringReturnsTheTitle() {
        Book book = new Book("Dune", 1965, ReadingStatus.READ, null, null, null);
        assertEquals("Dune", book.toString());
    }
}
