package com.booklibrary.reader.speech;

/**
 * Chooses the speech system that fits the operating system.
 *
 * Author: Farid Rahmani
 * Contribution: Added the automatic choice of speech engine.
 */
public final class SpeechEngines {

    private SpeechEngines() {
    }

    /**
     * Finds the speech engine of this computer.
     *
     * @return the Mac or Windows engine; an engine whose {@code isAvailable()} is false on other systems
     */
    public static SpeechEngine detect() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            return new WindowsSpeechEngine();
        }
        return new MacSpeechEngine();
    }
}
