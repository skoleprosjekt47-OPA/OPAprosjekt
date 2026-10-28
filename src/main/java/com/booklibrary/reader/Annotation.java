package com.booklibrary.reader;

import java.util.UUID;

/**
 * A mark the reader made in a book: a highlighted or underlined passage, with an optional personal comment.
 *
 * Author: Farid Rahmani
 * Contribution: Designed the data kept for highlights and comments.
 * Purpose: a mark belongs to one page and covers the characters from
 * {@code start} (included) to {@code end} (excluded) on that page. The
 * quoted text is saved as well, so the notes list can show it without
 * opening the page.
 */
public class Annotation {

    private String id = UUID.randomUUID().toString();
    private int page;
    private int start;
    private int end;
    private String text = "";
    private HighlightColor color;
    private boolean underline;
    private String comment;
    private long createdAt = System.currentTimeMillis();

    /** Needed by the JSON library. */
    public Annotation() {
    }

    /**
     * Creates a mark.
     *
     * @param page  the zero based page number
     * @param start first marked character on the page
     * @param end   the position after the last marked character
     * @param text  the marked text
     */
    public Annotation(int page, int start, int end, String text) {
        this.page = page;
        this.start = start;
        this.end = end;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public int getPage() {
        return page;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getText() {
        return text;
    }

    public HighlightColor getColor() {
        return color;
    }

    public void setColor(HighlightColor color) {
        this.color = color;
    }

    public boolean isUnderline() {
        return underline;
    }

    public void setUnderline(boolean underline) {
        this.underline = underline;
    }

    public String getComment() {
        return comment;
    }

    /**
     * Sets or removes the comment.
     *
     * @param comment the comment; {@code null} or blank removes it
     */
    public void setComment(String comment) {
        this.comment = comment == null || comment.isBlank() ? null : comment.strip();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * Tells whether the mark has a comment.
     *
     * @return {@code true} if there is a comment
     */
    public boolean hasComment() {
        return comment != null;
    }

    /**
     * Tells whether the mark still does anything: a colour, an underline or a comment.
     *
     * @return {@code false} if it can be deleted
     */
    public boolean isVisible() {
        return color != null || underline || comment != null;
    }

    /**
     * Tells whether a character position is inside the mark.
     *
     * @param index a character position on the page
     * @return {@code true} if the position is covered
     */
    public boolean contains(int index) {
        return index >= start && index < end;
    }

    /**
     * Tells whether the mark overlaps a range.
     *
     * @param from first character of the range
     * @param to   position after the last character of the range
     * @return {@code true} if at least one character is shared
     */
    public boolean overlaps(int from, int to) {
        return start < to && end > from;
    }
}
