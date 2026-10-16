package com.booklibrary.dao;

import com.booklibrary.model.Book;

import java.sql.SQLException;
import java.util.List;

/**
 * Data access contract for {@link Book} records.
 *
 * Author: Farid Rahmani
 * Contribution: Defined the DAO interface so controllers/services depend on an abstraction, not a concrete JDBC implementation.
 * Purpose: decouples the rest of the application from how books are
 * persisted. Every query returns fully populated {@link Book} objects
 * (writer, genre and bookshelf already joined in), so calling code never
 * has to run follow-up queries.
 */
public interface BookDao {

    /**
     * Returns every book in the collection, including its writer, genre
     * and bookshelf.
     *
     * @return a list of all books
     * @throws SQLException if the query fails
     */
    List<Book> findAll() throws SQLException;

    /**
     * Returns a single book by its id, or {@code null} if no such book
     * exists.
     *
     * @param bookId the id of the book to find
     * @return the matching book, or {@code null} if not found
     * @throws SQLException if the query fails
     */
    Book findById(int bookId) throws SQLException;

    /**
     * Returns the books matching the given optional filters. A
     * {@code null} or blank value for a given filter means "do not
     * filter by this field".
     *
     * @param titleContains text that must appear in the title (case-insensitive), or null
     * @param writerId      restrict to this writer's id, or null for any writer
     * @param genreId       restrict to this genre's id, or null for any genre
     * @param bookshelfId   restrict to this bookshelf's id, or null for any bookshelf
     * @param readingStatus restrict to this reading status, or null for any status
     * @return the list of books matching the given filters
     * @throws SQLException if the query fails
     */
    List<Book> search(String titleContains, Integer writerId, Integer genreId,
                       Integer bookshelfId, String readingStatus) throws SQLException;

    /**
     * Persists a new book and returns it with its generated database id.
     *
     * @param book the book to insert
     * @return the inserted book, with its id populated
     * @throws SQLException if the insert fails
     */
    Book insert(Book book) throws SQLException;

    /**
     * Persists changes to an existing book.
     *
     * @param book the book to update, identified by its id
     * @throws SQLException if the update fails
     */
    void update(Book book) throws SQLException;

    /**
     * Deletes the book with the given id.
     *
     * @param bookId the id of the book to delete
     * @throws SQLException if the delete fails
     */
    void delete(int bookId) throws SQLException;
}
