package com.booklibrary.dao;

import com.booklibrary.model.Bookshelf;

import java.sql.SQLException;
import java.util.List;

/**
 * Data access contract for {@link Bookshelf} records.
 *
 * Author: Farid Rahmani
 * Contribution: Defined the DAO interface so controllers/services depend on an abstraction, not a concrete JDBC implementation.
 * Purpose: decouples the rest of the application from how bookshelves
 * are persisted, the same way {@link WriterDao} does for writers.
 */
public interface BookshelfDao {

    /**
     * Returns every bookshelf.
     *
     * @return a list of all bookshelves
     * @throws SQLException if the query fails
     */
    List<Bookshelf> findAll() throws SQLException;

    /**
     * Returns a single bookshelf by its id, or {@code null} if no such
     * bookshelf exists.
     *
     * @param bookshelfId the id of the bookshelf to find
     * @return the matching bookshelf, or {@code null} if not found
     * @throws SQLException if the query fails
     */
    Bookshelf findById(int bookshelfId) throws SQLException;

    /**
     * Persists a new bookshelf and returns it with its generated database id.
     *
     * @param bookshelf the bookshelf to insert
     * @return the inserted bookshelf, with its id populated
     * @throws SQLException if the insert fails
     */
    Bookshelf insert(Bookshelf bookshelf) throws SQLException;

    /**
     * Persists changes to an existing bookshelf.
     *
     * @param bookshelf the bookshelf to update, identified by its id
     * @throws SQLException if the update fails
     */
    void update(Bookshelf bookshelf) throws SQLException;

    /**
     * Deletes the bookshelf with the given id.
     *
     * @param bookshelfId the id of the bookshelf to delete
     * @throws SQLException if the delete fails
     */
    void delete(int bookshelfId) throws SQLException;
}
