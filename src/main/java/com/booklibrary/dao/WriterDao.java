package com.booklibrary.dao;

import com.booklibrary.model.Writer;

import java.sql.SQLException;
import java.util.List;

/**
 * Data access contract for {@link Writer} records.
 *
 * Author: Farid Rahmani
 * Contribution: Defined the DAO interface so controllers/services depend on an abstraction, not a concrete JDBC implementation.
 * Purpose: decouples the rest of the application from how writers are
 * persisted (MySQL today, something else tomorrow) by depending on this
 * interface rather than a concrete implementation.
 */
public interface WriterDao {

    /**
     * Returns every writer.
     *
     * @return a list of all writers
     * @throws SQLException if the query fails
     */
    List<Writer> findAll() throws SQLException;

    /**
     * Returns a single writer by its id, or {@code null} if no such writer
     * exists.
     *
     * @param writerId the id of the writer to find
     * @return the matching writer, or {@code null} if not found
     * @throws SQLException if the query fails
     */
    Writer findById(int writerId) throws SQLException;

    /**
     * Persists a new writer and returns it with its generated database id.
     *
     * @param writer the writer to insert
     * @return the inserted writer, with its id populated
     * @throws SQLException if the insert fails
     */
    Writer insert(Writer writer) throws SQLException;

    /**
     * Persists changes to an existing writer.
     *
     * @param writer the writer to update, identified by its id
     * @throws SQLException if the update fails
     */
    void update(Writer writer) throws SQLException;

    /**
     * Deletes the writer with the given id.
     *
     * @param writerId the id of the writer to delete
     * @throws SQLException if the delete fails
     */
    void delete(int writerId) throws SQLException;
}
