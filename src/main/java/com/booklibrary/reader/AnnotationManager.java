package com.booklibrary.reader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Handles the marks of the book that is open: adding, finding, changing, removing and saving.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented the logic for highlights, underlines and comments.
 * Purpose: every change is saved at once, so nothing is lost if the program
 * is closed. The class has no JavaFX code, so it can be tested alone.
 */
public class AnnotationManager {

    private final AnnotationStore store;
    private final List<Annotation> items = new ArrayList<>();
    private String bookKey;

    /**
     * Creates a manager.
     *
     * @param store where the marks are saved
     */
    public AnnotationManager(AnnotationStore store) {
        this.store = store;
    }

    /**
     * Opens a book and loads its saved marks.
     *
     * @param key the identifier of the book, for example "gutenberg:120" or "demo"
     */
    public void open(String key) {
        this.bookKey = key;
        items.clear();
        items.addAll(store.load(key));
        sort();
    }

    /**
     * Returns all marks of the open book in reading order.
     *
     * @return a new list
     */
    public List<Annotation> all() {
        return new ArrayList<>(items);
    }

    /**
     * Returns the marks on one page.
     *
     * @param page the zero based page number
     * @return the marks on that page in reading order
     */
    public List<Annotation> onPage(int page) {
        return items.stream().filter(a -> a.getPage() == page).toList();
    }

    /**
     * Finds the newest mark that covers a character.
     *
     * @param page  the page
     * @param index the character position
     * @return the mark, or {@code null} if nothing is marked there
     */
    public Annotation findAt(int page, int index) {
        Annotation found = null;
        for (Annotation a : onPage(page)) {
            if (a.contains(index) && (found == null || a.getCreatedAt() >= found.getCreatedAt())) {
                found = a;
            }
        }
        return found;
    }

    /**
     * Finds all marks that overlap a range.
     *
     * @param page the page
     * @param from the first character
     * @param to   the position after the last character
     * @return the overlapping marks
     */
    public List<Annotation> overlapping(int page, int from, int to) {
        return onPage(page).stream().filter(a -> a.overlaps(from, to)).toList();
    }

    /**
     * Returns the mark for exactly this range, or creates a new one. The new mark is not visible
     * until a colour, underline or comment is set, and then {@link #changed(Annotation)} must be called.
     *
     * @param page the page
     * @param from the first character
     * @param to   the position after the last character
     * @param text the marked text
     * @return an existing or new mark
     */
    public Annotation getOrCreate(int page, int from, int to, String text) {
        for (Annotation a : onPage(page)) {
            if (a.getStart() == from && a.getEnd() == to) {
                return a;
            }
        }
        Annotation created = new Annotation(page, from, to, text);
        items.add(created);
        sort();
        return created;
    }

    /**
     * Saves after a mark was changed. A mark that no longer does anything is removed.
     *
     * @param mark the mark that was changed
     */
    public void changed(Annotation mark) {
        if (!mark.isVisible()) {
            items.remove(mark);
        }
        save();
    }

    /**
     * Removes a mark.
     *
     * @param mark the mark to remove
     */
    public void remove(Annotation mark) {
        items.remove(mark);
        save();
    }

    private void sort() {
        items.sort(Comparator.comparingInt(Annotation::getPage).thenComparingInt(Annotation::getStart));
    }

    private void save() {
        if (bookKey != null) {
            store.save(bookKey, items);
        }
    }
}
