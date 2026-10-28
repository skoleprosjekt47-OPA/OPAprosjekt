package com.booklibrary.translate;

import java.io.IOException;

/**
 * A translation request failed.
 *
 * Author: Farid Rahmani
 * Contribution: Separated errors that are worth retrying (busy server) from errors that are not (wrong key).
 */
public class TranslationException extends IOException {

    private final boolean retryable;

    /**
     * Creates the exception.
     *
     * @param message   what went wrong, written so that a user can understand it
     * @param retryable {@code true} if trying again later may work
     */
    public TranslationException(String message, boolean retryable) {
        super(message);
        this.retryable = retryable;
    }

    /**
     * Tells if it makes sense to try again.
     *
     * @return {@code true} for rate limits and server errors
     */
    public boolean isRetryable() {
        return retryable;
    }
}
