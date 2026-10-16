package com.booklibrary.model;

/**
 * The reading status of a book in the private library.
 *
 * Author: Farid Rahmani
 * Contribution: Designed the domain model and relationships between books, writers, genres and bookshelves.
 * Purpose: represents whether a book has been read, is currently being
 * read, or has not been started yet. Used by {@link Book} and by the
 * search/filter and reporting features.
 */
public enum ReadingStatus {
    READ,
    UNREAD,
    READING;

    /**
     * Returns a user-friendly label for display in the UI.
     *
     * @return a human-readable label for this status
     */
    public String getDisplayLabel() {
        return switch (this) {
            case READ -> "Read";
            case UNREAD -> "Unread";
            case READING -> "Currently reading";
        };
    }
}
