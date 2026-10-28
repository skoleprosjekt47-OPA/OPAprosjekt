package com.booklibrary.reader;

import java.util.prefs.Preferences;

/**
 * Remembers the reading settings of the user: font, size, spacing, contrast, voice and speed.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented the permanent saving of reading and accessibility settings.
 * Purpose: uses the Java preferences of the computer user, so each person on the
 * computer gets their own settings and they are still there next time the app opens.
 */
public class ReaderSettings {

    private final Preferences prefs;

    /**
     * Creates settings stored in a given preferences node (tests use their own node).
     *
     * @param prefs where to save
     */
    public ReaderSettings(Preferences prefs) {
        this.prefs = prefs;
    }

    /**
     * Creates the normal settings of the current computer user.
     *
     * @return settings stored for this application
     */
    public static ReaderSettings forUser() {
        return new ReaderSettings(Preferences.userNodeForPackage(ReaderSettings.class));
    }

    public String getFontFamily() {
        return prefs.get("fontFamily", "Georgia");
    }

    public void setFontFamily(String value) {
        prefs.put("fontFamily", value);
    }

    public double getFontSize() {
        return clamp(prefs.getDouble("fontSize", 18), 12, 48);
    }

    public void setFontSize(double value) {
        prefs.putDouble("fontSize", clamp(value, 12, 48));
    }

    public double getLineSpacing() {
        return clamp(prefs.getDouble("lineSpacing", 5), 0, 30);
    }

    public void setLineSpacing(double value) {
        prefs.putDouble("lineSpacing", clamp(value, 0, 30));
    }

    public boolean isBold() {
        return prefs.getBoolean("bold", true);
    }

    public void setBold(boolean value) {
        prefs.putBoolean("bold", value);
    }

    public ContrastMode getContrast() {
        try {
            return ContrastMode.valueOf(prefs.get("contrast", ContrastMode.NORMAL.name()));
        } catch (IllegalArgumentException e) {
            return ContrastMode.NORMAL;
        }
    }

    public void setContrast(ContrastMode value) {
        prefs.put("contrast", value.name());
    }

    public double getDarkness() {
        return clamp(prefs.getDouble("darkness", getContrast().getDarkness()), 0, 0.95);
    }

    public void setDarkness(double value) {
        prefs.putDouble("darkness", clamp(value, 0, 0.95));
    }

    public int getSpeed() {
        return (int) clamp(prefs.getInt("speed", 170), 90, 320);
    }

    public void setSpeed(int value) {
        prefs.putInt("speed", (int) clamp(value, 90, 320));
    }

    /**
     * Returns the name of the chosen voice.
     *
     * @return the voice name, or an empty text if none was chosen
     */
    public String getVoiceName() {
        return prefs.get("voice", "");
    }

    public void setVoiceName(String value) {
        prefs.put("voice", value == null ? "" : value);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
