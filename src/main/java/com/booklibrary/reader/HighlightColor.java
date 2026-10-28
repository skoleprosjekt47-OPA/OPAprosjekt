package com.booklibrary.reader;

/**
 * The colours the reader can use to mark text.
 *
 * Author: Farid Rahmani
 * Contribution: Defined the highlight colours for the reading mode.
 */
public enum HighlightColor {
    YELLOW("Yellow", "#f5d02e"),
    BLUE("Blue", "#4aa3ff"),
    GREEN("Green", "#4cd37b"),
    PINK("Pink", "#ff7ab8");

    private final String label;
    private final String hex;

    HighlightColor(String label, String hex) {
        this.label = label;
        this.hex = hex;
    }

    /**
     * Returns the name shown to the reader.
     *
     * @return for example "Yellow"
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the colour as a web colour.
     *
     * @return for example "#f5d02e"
     */
    public String getHex() {
        return hex;
    }
}
