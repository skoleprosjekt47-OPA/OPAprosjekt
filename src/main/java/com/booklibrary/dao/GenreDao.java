package com.booklibrary.dao;

import com.booklibrary.model.Genre;

import java.sql.SQLException;
import java.util.List;

/**
 * Data access contract for {@link Genre} records.
 *
 * Author: Farid Rahmani
 * Contribution: Defined the DAO interface so controllers/services depend on an abstraction, not a concrete JDBC implementation.
 * Purpose: decouples the rest of the application from how genres are
 * persisted, the same way {@link WriterDao} does for writers.
 */
public interface GenreDao {

    /**
     * Returns every genre.
     *
     * @return a list of all genres
     * @throws SQLException if the query fails
     */
    List<Genre> findAll() throws SQLException;

    /**
     * Returns a single genre by its id, or {@code null} if no such genre
     * exists.
     *
     * @param genreId the id of the genre to find
     * @return the matching genre, or {@code null} if not found
     * @throws SQLException if the query fails
     */
    Genre findById(int genreId) throws SQLException;

    /**
     * Persists a new genre and returns it with its generated database id.
     *
     * @param genre the genre to insert
     * @return the inserted genre, with its id populated
     * @throws SQLException if the insert fails
     */
    Genre insert(Genre genre) throws SQLException;

    /**
     * Persists changes to an existing genre.
     *
     * @param genre the genre to update, identified by its id
     * @throws SQLException if the update fails
     */
    void update(Genre genre) throws SQLException;

    /**
     * Deletes the genre with the given id.
     *
     * @param genreId the id of the genre to delete
     * @throws SQLException if the delete fails
     */
    void delete(int genreId) throws SQLException;
}
