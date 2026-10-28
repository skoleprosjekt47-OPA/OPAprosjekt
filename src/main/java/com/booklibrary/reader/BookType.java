package com.booklibrary.reader;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The kind of book being read. The type decides which moods are allowed,
 * so that for example a self-development book never gets action music.
 *
 * Author: Farid Rahmani
 * Contribution: Defined which music moods fit which kinds of books.
 */
public enum BookType {
    CLASSIC("Classic literature", Mood.CALM,
            EnumSet.of(Mood.CALM, Mood.ROMANTIC, Mood.SAD, Mood.TENSE, Mood.MYSTERIOUS, Mood.UPLIFTING)),
    ROMANCE("Romance", Mood.ROMANTIC,
            EnumSet.of(Mood.CALM, Mood.ROMANTIC, Mood.SAD, Mood.UPLIFTING)),
    ADVENTURE("Action / adventure", Mood.CALM,
            EnumSet.of(Mood.CALM, Mood.ACTION, Mood.TENSE, Mood.MYSTERIOUS, Mood.UPLIFTING, Mood.SAD)),
    MYSTERY("Mystery / thriller / horror", Mood.MYSTERIOUS,
            EnumSet.of(Mood.CALM, Mood.TENSE, Mood.MYSTERIOUS, Mood.ACTION, Mood.SAD)),
    SELF_DEVELOPMENT("Self-development / non-fiction", Mood.UPLIFTING,
            EnumSet.of(Mood.CALM, Mood.UPLIFTING)),
    GENERAL_FICTION("General fiction", Mood.CALM, EnumSet.allOf(Mood.class));

    private final String label;
    private final Mood defaultMood;
    private final Set<Mood> allowed;

    BookType(String label, Mood defaultMood, Set<Mood> allowed) {
        this.label = label;
        this.defaultMood = defaultMood;
        this.allowed = allowed;
    }

    public String getLabel() {
        return label;
    }

    /**
     * The mood used when the text does not point to anything special.
     *
     * @return the default mood for this type
     */
    public Mood getDefaultMood() {
        return defaultMood;
    }

    /**
     * Tells whether this kind of book may use a mood.
     *
     * @param mood the mood to check
     * @return {@code true} if the mood fits this type of book
     */
    public boolean allows(Mood mood) {
        return allowed.contains(mood);
    }

    @Override
    public String toString() {
        return label;
    }

    /**
     * Guesses the book type from the subject list of a library catalogue.
     *
     * @param subjects subjects such as "Love stories" or "Detective and mystery stories"; may be empty
     * @return the best matching type, {@link #CLASSIC} if nothing matches
     */
    public static BookType guess(List<String> subjects) {
        String all = String.join(" | ", subjects == null ? List.of() : subjects).toLowerCase(Locale.ROOT);
        if (containsAny(all, "self-help", "success", "conduct of life", "self-actualization", "habit",
                "psychology", "philosophy", "motivation", "business", "economics")) {
            return SELF_DEVELOPMENT;
        }
        if (containsAny(all, "detective", "mystery", "horror", "ghost", "vampire", "gothic", "crime", "murder")) {
            return MYSTERY;
        }
        if (containsAny(all, "love stories", "romance", "courtship", "marriage")) {
            return ROMANCE;
        }
        if (containsAny(all, "adventure", "war", "pirates", "sea stories", "western", "science fiction")) {
            return ADVENTURE;
        }
        return CLASSIC;
    }

    private static boolean containsAny(String text, String... needles) {
        for (String needle : needles) {
            if (text.contains(needle)) {
                return true;
            }
        }
        return false;
    }
}
