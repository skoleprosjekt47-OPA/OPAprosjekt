package com.booklibrary.model;

/**
 * Represents a writer (author) of books in the private library.
 *
 * Author: Farid Rahmani
 * Contribution: Designed the domain model and relationships between books, writers, genres and bookshelves.
 * Purpose: a simple domain entity holding a writer's identity and
 * nationality. One writer can be linked to many {@link Book} records.
 */
public class Writer {

    private int writerId;
    private String name;
    private String nationality;

    /**
     * Creates a new writer that has not yet been assigned a database id
     * (used before an INSERT).
     *
     * @param name        the writer's full name
     * @param nationality the writer's nationality, may be {@code null}
     */
    public Writer(String name, String nationality) {
        this(0, name, nationality);
    }

    /**
     * Creates a writer with a known database id (used after a SELECT).
     *
     * @param writerId    the primary key of the writer row
     * @param name        the writer's full name
     * @param nationality the writer's nationality, may be {@code null}
     */
    public Writer(int writerId, String name, String nationality) {
        this.writerId = writerId;
        this.name = name;
        this.nationality = nationality;
    }

    /** Returns the writer's database id. */
    public int getWriterId() {
        return writerId;
    }

    /** Sets the writer's database id. */
    public void setWriterId(int writerId) {
        this.writerId = writerId;
    }

    /** Returns the writer's name. */
    public String getName() {
        return name;
    }

    /** Sets the writer's name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Returns the writer's nationality. */
    public String getNationality() {
        return nationality;
    }

    /** Sets the writer's nationality. */
    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    /**
     * Returns the writer's name, used for display in lists and combo boxes.
     *
     * @return the writer's name
     */
    @Override
    public String toString() {
        return name;
    }
}
