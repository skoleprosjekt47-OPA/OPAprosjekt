package com.booklibrary.reader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the pure logic of the reading mode (no JavaFX needed).
 *
 * Author: Farid Rahmani
 * Contribution: Wrote the tests for page splitting, mood detection and Gutenberg parsing.
 */
class ReaderLogicTest {

    @Test
    void stripsGutenbergHeaderAndFooter() {
        String raw = "Licence text\n*** START OF THE PROJECT GUTENBERG EBOOK X ***\nReal story.\n*** END OF THE PROJECT GUTENBERG EBOOK X ***\nFooter";
        assertEquals("Real story.", Paginator.stripGutenbergBoilerplate(raw));
    }

    @Test
    void joinsWrappedLinesIntoParagraphs() {
        List<String> paragraphs = Paginator.toParagraphs("one\ntwo\n\nthree");
        assertEquals(List.of("one two", "three"), paragraphs);
    }

    @Test
    void pagesAreNotMuchLongerThanRequested() {
        String text = ("word ".repeat(120) + "\n\n").repeat(10);
        List<String> pages = Paginator.paginate(text, 500);
        assertTrue(pages.size() > 3);
        for (String page : pages) {
            assertTrue(page.length() <= 500, "page too long: " + page.length());
        }
    }

    @Test
    void noWordsAreLostWhenPaginating() {
        String text = "alpha beta gamma delta ".repeat(200);
        String joined = String.join(" ", Paginator.paginate(text, 300)).replaceAll("\\s+", " ").strip();
        assertEquals(text.replaceAll("\\s+", " ").strip(), joined);
    }

    @Test
    void emptyTextGivesOnePage() {
        assertEquals(1, Paginator.paginate("", 500).size());
    }

    @Test
    void actionTextGivesActionMusicInAdventureBook() {
        MoodAnalyzer analyzer = new MoodAnalyzer(BookType.ADVENTURE);
        assertEquals(Mood.ACTION, analyzer.next("Run! The enemy attacked with a sword, a fight and a battle began. Escape!"));
    }

    @Test
    void romanticTextGivesRomanticMusicInRomanceBook() {
        MoodAnalyzer analyzer = new MoodAnalyzer(BookType.ROMANCE);
        assertEquals(Mood.ROMANTIC, analyzer.next("He whispered of love, and one tender kiss. Her heart, her dear beloved."));
    }

    @Test
    void selfDevelopmentNeverGetsActionOrRomanticMusic() {
        MoodAnalyzer analyzer = new MoodAnalyzer(BookType.SELF_DEVELOPMENT);
        Mood mood = analyzer.next("Fight, battle, attack, kill, run! A kiss, love, passion and desire.");
        assertTrue(BookType.SELF_DEVELOPMENT.allows(mood));
        assertFalse(mood == Mood.ACTION || mood == Mood.ROMANTIC);
    }

    @Test
    void plainTextFallsBackToDefaultMood() {
        MoodAnalyzer analyzer = new MoodAnalyzer(BookType.CLASSIC);
        assertEquals(Mood.CALM, analyzer.next("The table was made of wood and stood in the room."));
    }

    @Test
    void guessesBookTypeFromSubjects() {
        assertEquals(BookType.ROMANCE, BookType.guess(List.of("Love stories", "England -- Fiction")));
        assertEquals(BookType.MYSTERY, BookType.guess(List.of("Detective and mystery stories")));
        assertEquals(BookType.SELF_DEVELOPMENT, BookType.guess(List.of("Conduct of life")));
        assertEquals(BookType.CLASSIC, BookType.guess(List.of()));
    }

    @Test
    void parsesGutendexResponse() {
        String json = """
                {"results":[
                 {"id":84,"title":"Frankenstein","authors":[{"name":"Shelley, Mary"}],
                  "subjects":["Horror tales"],
                  "formats":{"text/html":"https://x/84.html",
                             "text/plain; charset=us-ascii":"https://x/ascii.txt",
                             "text/plain; charset=utf-8":"https://x/84.txt.utf-8"}},
                 {"id":2,"title":"No text","authors":[],"subjects":[],"formats":{"text/html":"https://x/2.html"}}
                ]}""";
        List<GutenbergBook> books = new GutenbergClient(url -> "").parse(json);
        assertEquals(1, books.size());
        assertEquals("Shelley, Mary", books.get(0).author());
        assertEquals("https://x/84.txt.utf-8", books.get(0).textUrl());
    }

    @Test
    void demoBookGivesDifferentMoodsAcrossPages() {
        List<String> pages = Paginator.paginate(DemoBook.text(), 600);
        MoodAnalyzer analyzer = new MoodAnalyzer(BookType.GENERAL_FICTION);
        java.util.Set<Mood> seen = new java.util.HashSet<>();
        for (String page : pages) {
            seen.add(analyzer.next(page));
        }
        assertTrue(seen.contains(Mood.ACTION));
        assertTrue(seen.contains(Mood.ROMANTIC));
        assertTrue(seen.size() >= 4, "moods seen: " + seen);
    }
}
