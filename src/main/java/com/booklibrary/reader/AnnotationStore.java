package com.booklibrary.reader;

import java.util.List;

/**
 * Saves and loads the marks of the reader, one list per book.
 *
 * Author: Farid Rahmani
 * Contribution: Designed the storage interface so the storage can be changed later (for example to MySQL).
 */
public interface AnnotationStore {

    /**
     * Loads the marks of a book.
     *
     * @param bookKey the identifier of the book, for example "gutenberg:120"
     * @return the saved marks; empty if there are none
     */
    List<Annotation> load(String bookKey);

    /**
     * Saves the marks of a book, replacing what was saved before.
     *
     * @param bookKey the identifier of the book
     * @param marks   all marks of the book; an empty list removes the book from the store
     */
    void save(String bookKey, List<Annotation> marks);
}
