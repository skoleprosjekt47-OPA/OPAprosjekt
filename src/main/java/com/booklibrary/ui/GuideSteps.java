package com.booklibrary.ui;

import java.util.List;

/**
 * The texts of the guided tour that the assistant gives to new users.
 *
 * Author: Farid Rahmani
 * Contribution: Wrote the steps of the guided tour (drafted with AI assistance).
 * Purpose: keeps the words of the tour in one place without any JavaFX code, so the
 * steps can be tested and changed easily. {@link GuideAssistant} shows them.
 */
public final class GuideSteps {

    /** Used for a step that is not about one tab (the welcome and the goodbye). */
    public static final int NO_TAB = -1;

    private static final List<String> PRAISE = List.of("Great!", "Perfect!", "Super!", "Excellent!");

    private static final List<Step> STEPS = List.of(
            new Step(NO_TAB, "Hi! I am your personal assistant. I will show you the different parts of the app. "
                    + "Press \"I understand\" when you are ready for the next part."),
            new Step(0, "This is your library. Here you can add, edit and delete books, and search or filter "
                    + "by writer, genre, bookshelf and reading status."),
            new Step(1, "Discover searches Open Library, Google Books and Project Gutenberg at the same time. "
                    + "Hold the mouse over a book to see a preview."),
            new Step(2, "In Reading you can read free books page by page, highlight text, write notes, "
                    + "listen with read aloud and translate the book."),
            new Step(3, "Kids has simple stories for children."),
            new Step(4, "Reports shows how many books you have, and how they are divided by reading status, "
                    + "genre and writer."),
            new Step(NO_TAB, "That is everything! You can open this guide again with the Guide button. "
                    + "Enjoy your library!"));

    private GuideSteps() {
    }

    /** One step of the tour. */
    public static final class Step {
        private final int tabIndex;
        private final String text;

        /**
         * Creates a step.
         *
         * @param tabIndex the tab to show (0 is the first tab), or {@link #NO_TAB}
         * @param text     what the assistant says
         */
        public Step(int tabIndex, String text) {
            this.tabIndex = tabIndex;
            this.text = text;
        }

        /**
         * Returns the tab to show.
         *
         * @return the tab index, or {@link #NO_TAB}
         */
        public int tabIndex() {
            return tabIndex;
        }

        /**
         * Returns what the assistant says.
         *
         * @return the text of the step
         */
        public String text() {
            return text;
        }
    }

    /**
     * Returns all steps in order.
     *
     * @return an unmodifiable list of steps
     */
    public static List<Step> all() {
        return STEPS;
    }

    /**
     * Returns a short word of praise, said when the user presses "I understand".
     *
     * @param number any whole number; the words repeat in a fixed order
     * @return for example "Great!"
     */
    public static String praise(int number) {
        return PRAISE.get(Math.floorMod(number, PRAISE.size()));
    }
}
