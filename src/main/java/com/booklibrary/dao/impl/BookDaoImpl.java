package com.booklibrary.dao.impl;

import com.booklibrary.dao.BookDao;
import com.booklibrary.model.Book;
import com.booklibrary.model.Bookshelf;
import com.booklibrary.model.Genre;
import com.booklibrary.model.ReadingStatus;
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
 * JDBC implementation of {@link BookDao}, backed by the {@code book}
 * table in MySQL, joined with {@code writer}, {@code genre} and
 * {@code bookshelf}.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented the JDBC persistence logic (SQL queries, PreparedStatements, result mapping) for this entity.
 * Purpose: translates {@link Book} objects to and from rows in the
 * {@code book} table, always returning the related writer, genre and
 * bookshelf already populated so the UI never needs extra queries.
 */
public class BookDaoImpl implements BookDao {

    private static final String BASE_SELECT =
            "SELECT b.book_id, b.title, b.release_year, b.reading_status, " +
            "       w.writer_id, w.name AS writer_name, w.nationality, " +
            "       g.genre_id, g.name AS genre_name, g.description, " +
            "       s.bookshelf_id, s.name AS shelf_name, s.room, s.shelf_number " +
            "FROM book b " +
            "LEFT JOIN writer w ON b.writer_id = w.writer_id " +
            "LEFT JOIN genre g ON b.genre_id = g.genre_id " +
            "LEFT JOIN bookshelf s ON b.bookshelf_id = s.bookshelf_id";

    /**
     * Runs the base SELECT (joined with writer, genre and bookshelf) ordered
     * by title and maps every row to a fully populated {@link Book}.
     *
     * @return a list of all books
     * @throws SQLException if the query fails
     */
    @Override
    public List<Book> findAll() throws SQLException {
        String sql = BASE_SELECT + " ORDER BY b.title";
        List<Book> books = new ArrayList<>();
        Connection connection = DatabaseConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                books.add(mapRow(resultSet));
            }
        }
        return books;
    }

    /**
     * Runs the base SELECT filtered by {@code book_id} and maps the row to a
     * fully populated {@link Book}.
     *
     * @param bookId the id of the book to find
     * @return the matching book, or {@code null} if not found
     * @throws SQLException if the query fails
     */
    @Override
    public Book findById(int bookId) throws SQLException {
        String sql = BASE_SELECT + " WHERE b.book_id = ?";
        Connection connection = DatabaseConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    /**
     * Builds the base SELECT dynamically, appending an {@code AND} condition
     * for each non-null/non-blank filter, then runs it and maps every row to
     * a fully populated {@link Book}.
     *
     * @param titleContains text that must appear in the title (case-insensitive), or null
     * @param writerId      restrict to this writer's id, or null for any writer
     * @param genreId       restrict to this genre's id, or null for any genre
     * @param bookshelfId   restrict to this bookshelf's id, or null for any bookshelf
     * @param readingStatus restrict to this reading status, or null for any status
     * @return the list of books matching the given filters
     * @throws SQLException if the query fails
     */
    @Override
    public List<Book> search(String titleContains, Integer writerId, Integer genreId,
                              Integer bookshelfId, String readingStatus) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE 1 = 1");
        List<Object> params = new ArrayList<>();

        if (titleContains != null && !titleContains.isBlank()) {
            sql.append(" AND LOWER(b.title) LIKE ?");
            params.add("%" + titleContains.toLowerCase() + "%");
        }
        if (writerId != null) {
            sql.append(" AND b.writer_id = ?");
            params.add(writerId);
        }
        if (genreId != null) {
            sql.append(" AND b.genre_id = ?");
            params.add(genreId);
        }
        if (bookshelfId != null) {
            sql.append(" AND b.bookshelf_id = ?");
            params.add(bookshelfId);
        }
        if (readingStatus != null && !readingStatus.isBlank()) {
            sql.append(" AND b.reading_status = ?");
            params.add(readingStatus);
        }
        sql.append(" ORDER BY b.title");

        List<Book> books = new ArrayList<>();
        Connection connection = DatabaseConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    books.add(mapRow(resultSet));
                }
            }
        }
        return books;
    }

    /**
     * Inserts a new row into the {@code book} table and sets the generated
     * id back onto the given book.
     *
     * @param book the book to insert
     * @return the inserted book, with its id populated
     * @throws SQLException if the insert fails
     */
    @Override
    public Book insert(Book book) throws SQLException {
        String sql = "INSERT INTO book (title, release_year, reading_status, writer_id, genre_id, bookshelf_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        Connection connection = DatabaseConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindBook(statement, book);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    book.setBookId(keys.getInt(1));
                }
            }
        }
        return book;
    }

    /**
     * Updates the row in the {@code book} table matching the book's id with
     * its current field values.
     *
     * @param book the book to update, identified by its id
     * @throws SQLException if the update fails
     */
    @Override
    public void update(Book book) throws SQLException {
        String sql = "UPDATE book SET title = ?, release_year = ?, reading_status = ?, " +
                     "writer_id = ?, genre_id = ?, bookshelf_id = ? WHERE book_id = ?";
        Connection connection = DatabaseConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindBook(statement, book);
            statement.setInt(7, book.getBookId());
            statement.executeUpdate();
        }
    }

    /**
     * Deletes the row in the {@code book} table with the given id.
     *
     * @param bookId the id of the book to delete
     * @throws SQLException if the delete fails
     */
    @Override
    public void delete(int bookId) throws SQLException {
        String sql = "DELETE FROM book WHERE book_id = ?";
        Connection connection = DatabaseConnection.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, bookId);
            statement.executeUpdate();
        }
    }

    /** Binds a book's title, release year, reading status and foreign keys onto a prepared statement (columns 1-6). */
    private void bindBook(PreparedStatement statement, Book book) throws SQLException {
        statement.setString(1, book.getTitle());
        statement.setInt(2, book.getReleaseYear());
        statement.setString(3, book.getReadingStatus().name());
        statement.setObject(4, book.getWriter() != null ? book.getWriter().getWriterId() : null);
        statement.setObject(5, book.getGenre() != null ? book.getGenre().getGenreId() : null);
        statement.setObject(6, book.getBookshelf() != null ? book.getBookshelf().getBookshelfId() : null);
    }

    /** Maps the current result set row into a {@link Book} with its writer, genre and bookshelf populated. */
    private Book mapRow(ResultSet resultSet) throws SQLException {
        Writer writer = null;
        if (resultSet.getObject("writer_id") != null) {
            writer = new Writer(resultSet.getInt("writer_id"), resultSet.getString("writer_name"),
                    resultSet.getString("nationality"));
        }
        Genre genre = null;
        if (resultSet.getObject("genre_id") != null) {
            genre = new Genre(resultSet.getInt("genre_id"), resultSet.getString("genre_name"),
                    resultSet.getString("description"));
        }
        Bookshelf bookshelf = null;
        if (resultSet.getObject("bookshelf_id") != null) {
            bookshelf = new Bookshelf(resultSet.getInt("bookshelf_id"), resultSet.getString("shelf_name"),
                    resultSet.getString("room"), resultSet.getString("shelf_number"));
        }
        return new Book(
                resultSet.getInt("book_id"),
                resultSet.getString("title"),
                resultSet.getInt("release_year"),
                ReadingStatus.valueOf(resultSet.getString("reading_status")),
                writer, genre, bookshelf
        );
    }
}
