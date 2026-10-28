package com.booklibrary.reader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for highlights, underlines and comments, including saving and loading.
 *
 * Author: Farid Rahmani
 * Contribution: Wrote the tests for the annotation logic and storage.
 */
class AnnotationTest {

    @TempDir
    Path folder;

    private AnnotationManager manager() {
        return new AnnotationManager(new JsonAnnotationStore(folder.resolve("notes.json")));
    }

    @Test
    void highlightIsSavedAndLoadedAgain() {
        AnnotationManager first = manager();
        first.open("demo");
        Annotation a = first.getOrCreate(2, 10, 25, "a marked passage");
        a.setColor(HighlightColor.PINK);
        a.setComment("  remember this  ");
        first.changed(a);

        AnnotationManager second = manager();
        second.open("demo");
        assertEquals(1, second.all().size());
        Annotation loaded = second.all().get(0);
        assertEquals(HighlightColor.PINK, loaded.getColor());
        assertEquals("remember this", loaded.getComment());
        assertEquals(2, loaded.getPage());
        assertEquals("a marked passage", loaded.getText());
    }

    @Test
    void booksDoNotShareMarks() {
        AnnotationManager m = manager();
        m.open("book-a");
        Annotation a = m.getOrCreate(0, 0, 5, "hello");
        a.setUnderline(true);
        m.changed(a);

        m.open("book-b");
        assertTrue(m.all().isEmpty());
        m.open("book-a");
        assertEquals(1, m.all().size());
    }

    @Test
    void sameRangeIsReusedInsteadOfDuplicated() {
        AnnotationManager m = manager();
        m.open("demo");
        Annotation a = m.getOrCreate(1, 3, 9, "abcdef");
        a.setColor(HighlightColor.YELLOW);
        m.changed(a);
        Annotation b = m.getOrCreate(1, 3, 9, "abcdef");
        assertSame(a, b);
        assertEquals(1, m.all().size());
    }

    @Test
    void markThatDoesNothingIsRemoved() {
        AnnotationManager m = manager();
        m.open("demo");
        Annotation a = m.getOrCreate(0, 0, 4, "text");
        a.setColor(HighlightColor.BLUE);
        m.changed(a);
        a.setColor(null);
        m.changed(a);
        assertTrue(m.all().isEmpty());
    }

    @Test
    void findsMarksByPositionAndOverlap() {
        AnnotationManager m = manager();
        m.open("demo");
        Annotation a = m.getOrCreate(4, 10, 20, "0123456789");
        a.setColor(HighlightColor.GREEN);
        m.changed(a);
        assertSame(a, m.findAt(4, 10));
        assertSame(a, m.findAt(4, 19));
        assertNull(m.findAt(4, 20));
        assertNull(m.findAt(3, 12));
        assertEquals(1, m.overlapping(4, 15, 30).size());
        assertTrue(m.overlapping(4, 20, 30).isEmpty());
    }

    @Test
    void blankCommentCountsAsNoComment() {
        Annotation a = new Annotation(0, 0, 3, "abc");
        a.setComment("   ");
        assertFalse(a.hasComment());
        assertFalse(a.isVisible());
    }

    @Test
    void brokenFileIsSetAsideAndDoesNotCrash() throws Exception {
        Path file = folder.resolve("notes.json");
        Files.writeString(file, "{ this is not json");
        AnnotationManager m = new AnnotationManager(new JsonAnnotationStore(file));
        m.open("demo");
        assertTrue(m.all().isEmpty());
        assertTrue(Files.exists(folder.resolve("notes.json.broken")));
    }
}
