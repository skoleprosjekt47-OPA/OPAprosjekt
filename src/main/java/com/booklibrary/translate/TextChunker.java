package com.booklibrary.translate;

import java.util.ArrayList;
import java.util.List;

/**
 * Cuts a book into pieces that a translation service accepts, without cutting in the middle
 * of a paragraph when it can be avoided.
 *
 * Author: Farid Rahmani
 * Contribution: Wrote the splitting so that the translated book keeps its paragraphs.
 * Purpose: paragraphs are collected until the piece is full. A paragraph that is longer than
 * the limit is cut at sentence ends, and as a last resort at spaces.
 */
public final class TextChunker {

    private TextChunker() {
    }

    /**
     * Splits paragraphs into pieces.
     *
     * @param paragraphs the paragraphs of the book
     * @param maxChars   the largest allowed piece, at least 200
     * @return the pieces; paragraphs inside a piece are separated by a blank line
     */
    public static List<String> chunk(List<String> paragraphs, int maxChars) {
        int max = Math.max(200, maxChars);
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String paragraph : paragraphs) {
            String text = paragraph.strip();
            if (text.isEmpty()) {
                continue;
            }
            for (String piece : splitLong(text, max)) {
                if (current.length() > 0 && current.length() + 2 + piece.length() > max) {
                    chunks.add(current.toString());
                    current.setLength(0);
                }
                if (current.length() > 0) {
                    current.append("\n\n");
                }
                current.append(piece);
            }
        }
        if (current.length() > 0) {
            chunks.add(current.toString());
        }
        return chunks;
    }

    private static List<String> splitLong(String text, int max) {
        List<String> pieces = new ArrayList<>();
        String rest = text;
        while (rest.length() > max) {
            int cut = lastSentenceEnd(rest, max);
            if (cut <= 0) {
                cut = rest.lastIndexOf(' ', max);
            }
            if (cut <= 0) {
                cut = max;
            }
            pieces.add(rest.substring(0, cut).strip());
            rest = rest.substring(cut).strip();
        }
        if (!rest.isEmpty()) {
            pieces.add(rest);
        }
        return pieces;
    }

    private static int lastSentenceEnd(String text, int limit) {
        for (int i = Math.min(limit, text.length() - 1); i > limit / 2; i--) {
            char c = text.charAt(i - 1);
            if ((c == '.' || c == '!' || c == '?' || c == '。') && (i >= text.length() || Character.isWhitespace(text.charAt(i)))) {
                return i;
            }
        }
        return -1;
    }
}
