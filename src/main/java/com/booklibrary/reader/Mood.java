package com.booklibrary.reader;

/**
 * The moods the reading music can have. Each mood has one music file
 * in {@code /music/<fileName>.wav}.
 *
 * Author: Farid Rahmani
 * Contribution: Defined the moods used to match music to the story.
 */
public enum Mood {
    CALM("calm", "Calm"),
    ROMANTIC("romantic", "Romantic"),
    ACTION("action", "Action"),
    TENSE("tense", "Tense"),
    SAD("sad", "Sad"),
    MYSTERIOUS("mysterious", "Mysterious"),
    UPLIFTING("uplifting", "Uplifting");

    private final String fileName;
    private final String label;

    Mood(String fileName, String label) {
        this.fileName = fileName;
        this.label = label;
    }

    /**
     * Returns the resource path of the music file for this mood.
     *
     * @return for example {@code /music/calm.wav}
     */
    public String resourcePath() {
        return "/music/" + fileName + ".wav";
    }

    /**
     * Returns the name shown to the reader.
     *
     * @return a short English label
     */
    public String getLabel() {
        return label;
    }
}
