package com.booklibrary.dao.impl;

import com.booklibrary.dao.WriterDao;
import com.booklibrary.model.Writer;
import com.booklibrary.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link WriterDao}, backed by the {@code writer}
 * table in MySQL.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented the JDBC persistence logic (SQL queries, PreparedStatements, result mapping) for this entity.
 * Purpose: translates {@link Writer} objects to and from rows in the
 * {@code writer} table.
 */
public class WriterDaoImpl implements WriterDao {

    /**
     * Selects every row from the {@code writer} table, ordered by name.
     *
     * @return a list of all writers
     * @throws SQLException if the query fails
     */
    @Override
    public List<Writer> findAll() throws SQLException {
        String sql = "SELECT writer_id, name, nationality FROM writer ORDER BY name";
        List<Writer> writers = new ArrayList<>();
        Connection connection = DatabaseConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                writers.add(mapRow(resultSet));
            }
        }
        return writers;
    }

    /**
     * Selects the row from the {@code writer} table with the given id.
     *
     * @param writerId the id of the writer to find
     * @return the matching writer, or {@code null} if not found
     * @throws SQLException if the query fails
     */
    @Override
    public Writer findById(int writerId) throws SQLException {
        String sql = "SELECT writer_id, name, nationality FROM writer WHERE writer_id = ?";
        Connection connection = DatabaseConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, writerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    /**
     * Inserts a new row into the {@code writer} table and sets the
     * generated id back onto the given writer.
     *
     * @param writer the writer to insert
     * @return the inserted writer, with its id populated
     * @throws SQLException if the insert fails
     */
    @Override
    public Writer insert(Writer writer) throws SQLException {
        String sql = "INSERT INTO writer (name, nationality) VALUES (?, ?)";
        Connection connection = DatabaseConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, writer.getName());
            statement.setString(2, writer.getNationality());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    writer.setWriterId(keys.getInt(1));
                }
            }
        }
        return writer;
    }

    /**
     * Updates the row in the {@code writer} table matching the writer's id.
     *
     * @param writer the writer to update, identified by its id
     * @throws SQLException if the update fails
     */
    @Override
    public void update(Writer writer) throws SQLException {
        String sql = "UPDATE writer SET name = ?, nationality = ? WHERE writer_id = ?";
        Connection connection = DatabaseConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, writer.getName());
            statement.setString(2, writer.getNationality());
            statement.setInt(3, writer.getWriterId());
            statement.executeUpdate();
        }
    }

    /**
     * Deletes the row in the {@code writer} table with the given id.
     *
     * @param writerId the id of the writer to delete
     * @throws SQLException if the delete fails
     */
    @Override
    public void delete(int writerId) throws SQLException {
        String sql = "DELETE FROM writer WHERE writer_id = ?";
        Connection connection = DatabaseConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, writerId);
            statement.executeUpdate();
        }
    }

    /** Maps the current result set row into a {@link Writer}. */
    private Writer mapRow(ResultSet resultSet) throws SQLException {
        return new Writer(
                resultSet.getInt("writer_id"),
                resultSet.getString("name"),
                resultSet.getString("nationality")
        );
    }
}
