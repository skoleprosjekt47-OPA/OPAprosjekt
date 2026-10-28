package com.booklibrary.reader;

import java.util.List;

/**
 * A free book found on Project Gutenberg.
 *
 * @param id       the Project Gutenberg number
 * @param title    the title
 * @param author   the author, "Unknown" if missing
 * @param subjects the catalogue subjects, used to guess the {@link BookType}
 * @param textUrl  the address of the plain text version
 */
public record GutenbergBook(int id, String title, String author, List<String> subjects, String textUrl) {

    @Override
    public String toString() {
        return title + " - " + author;
    }
}
