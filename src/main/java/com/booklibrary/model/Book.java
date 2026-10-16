package com.booklibrary.model;

/**
 * Represents a single book in the private library collection.
 *
 * Author: Farid Rahmani
 * Contribution: Designed the domain model and relationships between books, writers, genres and bookshelves.
 * Purpose: the central domain entity of the application. A book is
 * linked to exactly one {@link Writer}, one {@link Genre} and one
 * {@link Bookshelf}, and carries its own {@link ReadingStatus}.
 */
public class Book {

    private int bookId;
    private String title;
    private int releaseYear;
    private ReadingStatus readingStatus;
    private Writer writer;
    private Genre genre;
    private Bookshelf bookshelf;

    /**
     * Creates a new book that has not yet been assigned a database id
     * (used before an INSERT).
     */
    public Book(String title, int releaseYear, ReadingStatus readingStatus,
                Writer writer, Genre genre, Bookshelf bookshelf) {
        this(0, title, releaseYear, readingStatus, writer, genre, bookshelf);
    }

    /**
     * Creates a book with a known database id (used after a SELECT).
     */
    public Book(int bookId, String title, int releaseYear, ReadingStatus readingStatus,
                Writer writer, Genre genre, Bookshelf bookshelf) {
        this.bookId = bookId;
        this.title = title;
        this.releaseYear = releaseYear;
        this.readingStatus = readingStatus;
        this.writer = writer;
        this.genre = genre;
        this.bookshelf = bookshelf;
    }

    /** Returns the book's database id. */
    public int getBookId() {
        return bookId;
    }

    /** Sets the book's database id. */
    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    /** Returns the book's title. */
    public String getTitle() {
        return title;
    }

    /** Sets the book's title. */
    public void setTitle(String title) {
        this.title = title;
    }

    /** Returns the book's release year. */
    public int getReleaseYear() {
        return releaseYear;
    }

    /** Sets the book's release year. */
    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    /** Returns the book's reading status. */
    public ReadingStatus getReadingStatus() {
        return readingStatus;
    }

    /** Sets the book's reading status. */
    public void setReadingStatus(ReadingStatus readingStatus) {
        this.readingStatus = readingStatus;
    }

    /** Returns the book's writer. */
    public Writer getWriter() {
        return writer;
    }

    /** Sets the book's writer. */
    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    /** Returns the book's genre. */
    public Genre getGenre() {
        return genre;
    }

    /** Sets the book's genre. */
    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    /** Returns the bookshelf the book is stored on. */
    public Bookshelf getBookshelf() {
        return bookshelf;
    }

    /** Sets the bookshelf the book is stored on. */
    public void setBookshelf(Bookshelf bookshelf) {
        this.bookshelf = bookshelf;
    }

    /**
     * Returns the book's title, used for display in lists and combo boxes.
     *
     * @return the book's title
     */
    @Override
    public String toString() {
        return title;
    }
}
