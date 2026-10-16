package com.booklibrary.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Provides a single shared JDBC connection to the MySQL database.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented the shared MySQL database connection used by all DAO implementations.
 * Purpose: centralizes database connection details so DAO classes do not
 * each open their own connection. Reads connection settings from
 * db.properties on the classpath if present (keys: db.url, db.user,
 * db.password), otherwise falls back to the defaults below. This makes it
 * possible to point the app at a different MySQL instance (for example a
 * teammate's machine) without changing any code.
 */
public final class DatabaseConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/book_library?useSSL=false&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private static Connection connection;

    private DatabaseConnection() {
    }

    /**
     * Returns a shared, open connection to the database, creating it on
     * first use.
     *
     * @return an open {@link Connection}
     * @throws SQLException if the connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            Properties props = loadProperties();
            String url = props.getProperty("db.url", DEFAULT_URL);
            String user = props.getProperty("db.user", DEFAULT_USER);
            String password = props.getProperty("db.password", DEFAULT_PASSWORD);
            connection = DriverManager.getConnection(url, user, password);
        }
        return connection;
    }

    /**
     * Loads db.properties from the classpath if it exists. Returns an
     * empty {@link Properties} (so defaults apply) if the file is missing
     * or cannot be read.
     */
    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = DatabaseConnection.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
        }
        return props;
    }
}
