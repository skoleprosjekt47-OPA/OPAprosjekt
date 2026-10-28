package com.booklibrary.translate;

import java.io.IOException;

/**
 * A service that can translate text. Several services can be used, since they cost different
 * amounts and need different keys.
 *
 * Author: Farid Rahmani
 * Contribution: Designed the interface that lets the app switch between translation services (strategy pattern).
 */
public interface TranslationProvider {

    /**
     * Returns the name shown to the user.
     *
     * @return for example "DeepL"
     */
    String displayName();

    /**
     * Tells if the key or address needed for this service is set.
     *
     * @return {@code true} if the service can be used
     */
    boolean isConfigured();

    /**
     * Tells which setting is needed.
     *
     * @return for example "set DEEPL_API_KEY"
     */
    String setupHint();

    /**
     * Tells if the service can translate into a language.
     *
     * @param language the target language
     * @return {@code true} if it is supported
     */
    boolean supports(Language language);

    /**
     * Returns the largest text, in characters, to send in one request.
     *
     * @return the number of characters
     */
    int maxChunkChars();

    /**
     * Translates one piece of text. Paragraph breaks (blank lines) must be kept.
     *
     * @param text   the text, at most {@link #maxChunkChars()} characters
     * @param target the language to translate into
     * @return the translated text
     * @throws IOException          if the request fails
     * @throws InterruptedException if the thread is interrupted
     */
    String translate(String text, Language target) throws IOException, InterruptedException;

    @Override
    String toString();
}
