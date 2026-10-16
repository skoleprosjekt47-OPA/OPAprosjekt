package com.booklibrary.dao.impl;

import com.booklibrary.dao.BookshelfDao;
import com.booklibrary.model.Bookshelf;
import com.booklibrary.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link BookshelfDao}, backed by the
 * {@code bookshelf} table in MySQL.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented the JDBC persistence logic (SQL queries, PreparedStatements, result mapping) for this entity.
 * Purpose: translates {@link Bookshelf} objects to and from rows in the
 * {@code bookshelf} table.
 */
public class BookshelfDaoImpl implements BookshelfDao {

    /**
     * Selects every row from the {@code bookshelf} table, ordered by name.
     *
     * @return a list of all bookshelves
     * @throws SQLException if the query fails
     */
    @Override
    public List<Bookshelf> findAll() throws SQLException {
        String sql = "SELECT bookshelf_id, name, room, shelf_number FROM bookshelf ORDER BY name";
        List<Bookshelf> bookshelves = new ArrayList<>();
        Connection connection = DatabaseConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                bookshelves.add(mapRow(resultSet));
            }
        }
        return bookshelves;
    }

    /**
     * Selects the row from the {@code bookshelf} table with the given id.
     *
     * @param bookshelfId the id of the bookshelf to find
     * @return the matching bookshelf, or {@code null} if not found
     * @throws SQLException if the query fails
     */
    @Override
    public Bookshelf findById(int bookshelfId) throws SQLException {
        String sql = "SELECT bookshelf_id, name, room, shelf_number FROM bookshelf WHERE bookshelf_id = ?";
        Connection connection = DatabaseConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookshelfId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    /**
     * Inserts a new row into the {@code bookshelf} table and sets the
     * generated id back onto the given bookshelf.
     *
     * @param bookshelf the bookshelf to insert
     * @return the inserted bookshelf, with its id populated
     * @throws SQLException if the insert fails
     */
    @Override
    public Bookshelf insert(Bookshelf bookshelf) throws SQLException {
        String sql = "INSERT INTO bookshelf (name, room, shelf_number) VALUES (?, ?, ?)";
        Connection connection = DatabaseConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, bookshelf.getName());
            statement.setString(2, bookshelf.getRoom());
            statement.setString(3, bookshelf.getShelfNumber());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    bookshelf.setBookshelfId(keys.getInt(1));
                }
            }
        }
        return bookshelf;
    }

    /**
     * Updates the row in the {@code bookshelf} table matching the
     * bookshelf's id.
     *
     * @param bookshelf the bookshelf to update, identified by its id
     * @throws SQLException if the update fails
     */
    @Override
    public void update(Bookshelf bookshelf) throws SQLException {
        String sql = "UPDATE bookshelf SET name = ?, room = ?, shelf_number = ? WHERE bookshelf_id = ?";
        Connection connection = DatabaseConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, bookshelf.getName());
            statement.setString(2, bookshelf.getRoom());
            statement.setString(3, bookshelf.getShelfNumber());
            statement.setInt(4, bookshelf.getBookshelfId());
            statement.executeUpdate();
        }
    }

    /**
     * Deletes the row in the {@code bookshelf} table with the given id.
     *
     * @param bookshelfId the id of the bookshelf to delete
     * @throws SQLException if the delete fails
     */
    @Override
    public void delete(int bookshelfId) throws SQLException {
        String sql = "DELETE FROM bookshelf WHERE bookshelf_id = ?";
        Connection connection = DatabaseConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookshelfId);
            statement.executeUpdate();
        }
    }

    /** Maps the current result set row into a {@link Bookshelf}. */
    private Bookshelf mapRow(ResultSet resultSet) throws SQLException {
        return new Bookshelf(
                resultSet.getInt("bookshelf_id"),
                resultSet.getString("name"),
                resultSet.getString("room"),
                resultSet.getString("shelf_number")
        );
    }
}
