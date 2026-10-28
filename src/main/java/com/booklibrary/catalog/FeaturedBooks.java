package com.booklibrary.catalog;

import com.booklibrary.reader.GutenbergBook;

import java.util.List;
import java.util.Locale;

/**
 * A built-in list of well-known free books, used when the online Project Gutenberg catalogue cannot be reached.
 *
 * Author: Farid Rahmani
 * Contribution: Added a reserve list so free books can always be found and read.
 * Purpose: each entry has the Project Gutenberg number, from which the text and cover addresses are built.
 */
final class FeaturedBooks {

    private record Entry(int id, String title, String author, String subjects) { }

    private static final List<Entry> ENTRIES = List.of(
            new Entry(120, "Treasure Island", "Robert Louis Stevenson", "Adventure stories; Pirates; Sea stories"),
            new Entry(1184, "The Count of Monte Cristo", "Alexandre Dumas", "Adventure stories; Revenge"),
            new Entry(1257, "The Three Musketeers", "Alexandre Dumas", "Adventure stories; War"),
            new Entry(164, "Twenty Thousand Leagues Under the Sea", "Jules Verne", "Adventure stories; Science fiction; Sea stories"),
            new Entry(2701, "Moby Dick", "Herman Melville", "Adventure stories; Sea stories; Whaling"),
            new Entry(345, "Dracula", "Bram Stoker", "Horror tales; Vampires; Gothic fiction"),
            new Entry(84, "Frankenstein", "Mary Shelley", "Horror tales; Science fiction; Gothic fiction"),
            new Entry(174, "The Picture of Dorian Gray", "Oscar Wilde", "Horror tales; Gothic fiction"),
            new Entry(1661, "The Adventures of Sherlock Holmes", "Arthur Conan Doyle", "Detective and mystery stories"),
            new Entry(2852, "The Hound of the Baskervilles", "Arthur Conan Doyle", "Detective and mystery stories; Mystery"),
            new Entry(1342, "Pride and Prejudice", "Jane Austen", "Love stories; Courtship; England"),
            new Entry(1260, "Jane Eyre", "Charlotte Bronte", "Love stories; Governesses"),
            new Entry(768, "Wuthering Heights", "Emily Bronte", "Love stories; Revenge; Gothic fiction"),
            new Entry(1513, "Romeo and Juliet", "William Shakespeare", "Love stories; Tragedies"),
            new Entry(11, "Alice's Adventures in Wonderland", "Lewis Carroll", "Fantasy; Children's stories"),
            new Entry(16, "Peter Pan", "J. M. Barrie", "Fantasy; Children's stories"),
            new Entry(514, "Little Women", "Louisa May Alcott", "Family life; Sisters"),
            new Entry(45, "Anne of Green Gables", "L. M. Montgomery", "Family life; Orphans"),
            new Entry(98, "A Tale of Two Cities", "Charles Dickens", "Historical fiction; French Revolution"),
            new Entry(1400, "Great Expectations", "Charles Dickens", "Bildungsromans; England"),
            new Entry(2554, "Crime and Punishment", "Fyodor Dostoyevsky", "Psychological fiction; Crime"),
            new Entry(2680, "Meditations", "Marcus Aurelius", "Philosophy; Conduct of life"),
            new Entry(132, "The Art of War", "Sun Tzu", "Military art and science; Strategy; Philosophy"),
            new Entry(4507, "As a Man Thinketh", "James Allen", "Self-help; Success; Conduct of life"),
            new Entry(205, "Walden", "Henry David Thoreau", "Nature; Philosophy; Conduct of life"));

    private FeaturedBooks() {
    }

    /**
     * Finds the built-in books whose title or author contains the search text.
     *
     * @param query the search text
     * @return the matching books; empty if nothing matches
     */
    static List<GutenbergBook> matching(String query) {
        String needle = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
        if (needle.isEmpty()) {
            return List.of();
        }
        return ENTRIES.stream()
                .filter(e -> e.title().toLowerCase(Locale.ROOT).contains(needle)
                        || e.author().toLowerCase(Locale.ROOT).contains(needle)
                        || needle.contains(e.title().toLowerCase(Locale.ROOT)))
                .map(e -> new GutenbergBook(e.id(), e.title(), e.author(), List.of(e.subjects().split("; ")),
                        "https://www.gutenberg.org/cache/epub/" + e.id() + "/pg" + e.id() + ".txt"))
                .toList();
    }
}
