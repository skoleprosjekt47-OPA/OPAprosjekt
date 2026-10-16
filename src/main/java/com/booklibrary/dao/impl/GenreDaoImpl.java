package com.booklibrary.dao.impl;

import com.booklibrary.dao.GenreDao;
import com.booklibrary.model.Genre;
import com.booklibrary.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link GenreDao}, backed by the {@code genre}
 * table in MySQL.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented the JDBC persistence logic (SQL queries, PreparedStatements, result mapping) for this entity.
 * Purpose: translates {@link Genre} objects to and from rows in the
 * {@code genre} table.
 */
public class GenreDaoImpl implements GenreDao {

    /**
     * Selects every row from the {@code genre} table, ordered by name.
     *
     * @return a list of all genres
     * @throws SQLException if the query fails
     */
    @Override
    public List<Genre> findAll() throws SQLException {
        String sql = "SELECT genre_id, name, description FROM genre ORDER BY name";
        List<Genre> genres = new ArrayList<>();
        Connection connection = DatabaseConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                genres.add(mapRow(resultSet));
            }
        }
        return genres;
    }

    /**
     * Selects the row from the {@code genre} table with the given id.
     *
     * @param genreId the id of the genre to find
     * @return the matching genre, or {@code null} if not found
     * @throws SQLException if the query fails
     */
    @Override
    public Genre findById(int genreId) throws SQLException {
        String sql = "SELECT genre_id, name, description FROM genre WHERE genre_id = ?";
        Connection connection = DatabaseConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, genreId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    /**
     * Inserts a new row into the {@code genre} table and sets the generated
     * id back onto the given genre.
     *
     * @param genre the genre to insert
     * @return the inserted genre, with its id populated
     * @throws SQLException if the insert fails
     */
    @Override
    public Genre insert(Genre genre) throws SQLException {
        String sql = "INSERT INTO genre (name, description) VALUES (?, ?)";
        Connection connection = DatabaseConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, genre.getName());
            statement.setString(2, genre.getDescription());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    genre.setGenreId(keys.getInt(1));
                }
            }
        }
        return genre;
    }

    /**
     * Updates the row in the {@code genre} table matching the genre's id.
     *
     * @param genre the genre to update, identified by its id
     * @throws SQLException if the update fails
     */
    @Override
    public void update(Genre genre) throws SQLException {
        String sql = "UPDATE genre SET name = ?, description = ? WHERE genre_id = ?";
        Connection connection = DatabaseConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, genre.getName());
            statement.setString(2, genre.getDescription());
            statement.setInt(3, genre.getGenreId());
            statement.executeUpdate();
        }
    }

    /**
     * Deletes the row in the {@code genre} table with the given id.
     *
     * @param genreId the id of the genre to delete
     * @throws SQLException if the delete fails
     */
    @Override
    public void delete(int genreId) throws SQLException {
        String sql = "DELETE FROM genre WHERE genre_id = ?";
        Connection connection = DatabaseConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, genreId);
            statement.executeUpdate();
        }
    }

    /** Maps the current result set row into a {@link Genre}. */
    private Genre mapRow(ResultSet resultSet) throws SQLException {
        return new Genre(
                resultSet.getInt("genre_id"),
                resultSet.getString("name"),
                resultSet.getString("description")
        );
    }
}
