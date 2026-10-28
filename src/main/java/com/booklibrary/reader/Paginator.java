package com.booklibrary.reader;

import java.util.ArrayList;
import java.util.List;

/**
 * Cleans up book text and cuts it into pages.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented the page splitting for the reader.
 * Purpose: removes the Project Gutenberg licence text, joins hard wrapped
 * lines into real paragraphs and then fills pages of about the same size
 * without ever breaking a word.
 */
public final class Paginator {

    private Paginator() {
    }

    /**
     * Removes the Project Gutenberg header and footer, if present.
     *
     * @param raw the full downloaded text
     * @return only the book itself
     */
    public static String stripGutenbergBoilerplate(String raw) {
        String text = raw.replace("\r\n", "\n").replace("﻿", "");
        int start = text.indexOf("*** START OF");
        if (start >= 0) {
            int lineEnd = text.indexOf('\n', start);
            text = lineEnd >= 0 ? text.substring(lineEnd + 1) : "";
        }
        int end = text.indexOf("*** END OF");
        if (end >= 0) {
            text = text.substring(0, end);
        }
        return text.strip();
    }

    /**
     * Joins lines that belong to the same paragraph. In plain text books a blank
     * line separates paragraphs, while single line breaks are only wrapping.
     *
     * @param text text with hard wrapped lines
     * @return paragraphs separated by one blank line
     */
    public static List<String> toParagraphs(String text) {
        List<String> paragraphs = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String line : text.replace("\r\n", "\n").split("\n", -1)) {
            if (line.isBlank()) {
                if (current.length() > 0) {
                    paragraphs.add(current.toString());
                    current.setLength(0);
                }
            } else {
                if (current.length() > 0) {
                    current.append(' ');
                }
                current.append(line.strip());
            }
        }
        if (current.length() > 0) {
            paragraphs.add(current.toString());
        }
        return paragraphs;
    }

    /**
     * Cuts text into pages.
     *
     * @param text     the book text (may still contain Gutenberg boilerplate)
     * @param pageSize the wanted number of characters per page, at least 200
     * @return the pages in reading order; never empty
     */
    public static List<String> paginate(String text, int pageSize) {
        int size = Math.max(200, pageSize);
        List<String> pages = new ArrayList<>();
        StringBuilder page = new StringBuilder();
        for (String paragraph : toParagraphs(stripGutenbergBoilerplate(text))) {
            String rest = paragraph;
            while (!rest.isEmpty()) {
                int room = size - page.length();
                int needed = rest.length() + (page.length() > 0 ? 2 : 0);
                if (needed <= room) {
                    if (page.length() > 0) {
                        page.append("\n\n");
                    }
                    page.append(rest);
                    rest = "";
                } else if (page.length() > size * 0.6 || room < 80) {
                    // page is full enough: start a new one
                    pages.add(page.toString());
                    page.setLength(0);
                } else {
                    // split the paragraph at a space so the page is filled
                    int cut = rest.lastIndexOf(' ', room - 2);
                    if (cut <= 0) {
                        cut = Math.min(rest.length(), room - 2);
                    }
                    if (page.length() > 0) {
                        page.append("\n\n");
                    }
                    page.append(rest, 0, cut);
                    rest = rest.substring(cut).stripLeading();
                    pages.add(page.toString());
                    page.setLength(0);
                }
            }
        }
        if (page.length() > 0) {
            pages.add(page.toString());
        }
        if (pages.isEmpty()) {
            pages.add("(This book has no text.)");
        }
        return pages;
    }
}
