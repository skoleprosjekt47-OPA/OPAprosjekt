package com.booklibrary.model;

/**
 * Represents a book genre (e.g. Mystery, Classic, Dystopian).
 *
 * Author: Farid Rahmani
 * Contribution: Designed the domain model and relationships between books, writers, genres and bookshelves.
 * Purpose: a simple domain entity grouping books by category. One genre
 * can be linked to many {@link Book} records.
 */
public class Genre {

    private int genreId;
    private String name;
    private String description;

    /**
     * Creates a new genre that has not yet been assigned a database id
     * (used before an INSERT).
     *
     * @param name        the genre's name
     * @param description a short description of the genre
     */
    public Genre(String name, String description) {
        this(0, name, description);
    }

    /**
     * Creates a genre with a known database id (used after a SELECT).
     *
     * @param genreId     the primary key of the genre row
     * @param name        the genre's name
     * @param description a short description of the genre
     */
    public Genre(int genreId, String name, String description) {
        this.genreId = genreId;
        this.name = name;
        this.description = description;
    }

    /** Returns the genre's database id. */
    public int getGenreId() {
        return genreId;
    }

    /** Sets the genre's database id. */
    public void setGenreId(int genreId) {
        this.genreId = genreId;
    }

    /** Returns the genre's name. */
    public String getName() {
        return name;
    }

    /** Sets the genre's name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Returns the genre's description. */
    public String getDescription() {
        return description;
    }

    /** Sets the genre's description. */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the genre's name, used for display in lists and combo boxes.
     *
     * @return the genre's name
     */
    @Override
    public String toString() {
        return name;
    }
}
