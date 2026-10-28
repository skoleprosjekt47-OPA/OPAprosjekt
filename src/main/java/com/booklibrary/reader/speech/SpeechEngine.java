package com.booklibrary.reader.speech;

import java.io.IOException;
import java.util.List;

/**
 * A text to speech system of the operating system.
 *
 * Author: Farid Rahmani
 * Contribution: Designed the speech interface so each operating system (and later a cloud voice) can be plugged in.
 */
public interface SpeechEngine {

    /**
     * Returns the name of the system, shown to the user.
     *
     * @return for example "macOS speech"
     */
    String getName();

    /**
     * Tells whether speech works on this computer.
     *
     * @return {@code true} if text can be read aloud
     */
    boolean isAvailable();

    /**
     * Lists the installed voices.
     *
     * @return the voices; empty if none were found
     */
    List<Voice> voices();

    /**
     * Starts reading a text. The method returns at once; the returned process ends when the speech ends,
     * and speech is stopped by destroying the process.
     *
     * @param text            the text to read
     * @param voice           the voice, or {@code null} for the default voice
     * @param wordsPerMinute  the reading speed
     * @return the running process
     * @throws IOException if the speech program cannot be started
     */
    Process speak(String text, Voice voice, int wordsPerMinute) throws IOException;
}
