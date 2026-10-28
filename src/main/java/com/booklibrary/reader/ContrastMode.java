package com.booklibrary.reader;

/**
 * Ready-made combinations of text colour and background darkness for the reading view.
 *
 * Author: Farid Rahmani
 * Contribution: Added contrast choices to make reading more comfortable and accessible.
 */
public enum ContrastMode {
    NORMAL("Normal", "#ffffff", 0.45),
    HIGH("High contrast", "#ffffff", 0.78),
    SOFT("Soft and warm", "#f3e7cc", 0.40),
    YELLOW("Yellow on dark", "#ffe14d", 0.74);

    private final String label;
    private final String textHex;
    private final double darkness;

    ContrastMode(String label, String textHex, double darkness) {
        this.label = label;
        this.textHex = textHex;
        this.darkness = darkness;
    }

    /**
     * Returns the text colour.
     *
     * @return a web colour such as "#ffffff"
     */
    public String getTextHex() {
        return textHex;
    }

    /**
     * Returns how dark the background is made, so text is easier to see.
     *
     * @return a value between 0 (not darkened) and 1 (black)
     */
    public double getDarkness() {
        return darkness;
    }

    @Override
    public String toString() {
        return label;
    }
}
