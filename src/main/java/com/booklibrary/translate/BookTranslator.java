package com.booklibrary.translate;

import com.booklibrary.reader.Paginator;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Translates a whole book piece by piece with one translation service.
 *
 * Author: Farid Rahmani
 * Contribution: Wrote the loop that cuts the book up, translates the pieces, retries, saves, and can be stopped.
 * Purpose: the work can take many minutes and many requests, so it
 * <ul>
 *   <li>saves every finished piece, so it continues after a stop, an error or a restart,</li>
 *   <li>retries a busy server a few times with a pause, but stops at once for a wrong key or no credit,</li>
 *   <li>reports progress after every piece, and</li>
 *   <li>can be cancelled at any time.</li>
 * </ul>
 */
public class BookTranslator {

    /** Receives progress. It is called from the thread that translates. */
    public interface Listener {
        /**
         * One more piece is done.
         *
         * @param done  finished pieces, including pieces found in the saved file
         * @param total all pieces
         */
        void progress(int done, int total);

        /**
         * The service was busy, so the translator waits before it tries again.
         *
         * @param attempt       the attempt that failed, starting at 1
         * @param attempts      how many attempts there are in all
         * @param waitSeconds   how long the wait is
         * @param reason        what the service said
         */
        default void waiting(int attempt, int attempts, long waitSeconds, String reason) {
        }
    }

    private static final int ATTEMPTS = 4;
    private static final int SAVE_EVERY = 3;

    private final TranslationProvider provider;
    private final TranslationStore store;
    private final long retryPauseMillis;

    /**
     * Creates a translator.
     *
     * @param provider the service
     * @param store    where progress is saved
     */
    public BookTranslator(TranslationProvider provider, TranslationStore store) {
        this(provider, store, 8000);
    }

    /**
     * Creates a translator with a chosen pause between retries (used by tests).
     *
     * @param provider         the service
     * @param store            where progress is saved
     * @param retryPauseMillis the pause before a retry is multiplied by the attempt number
     */
    public BookTranslator(TranslationProvider provider, TranslationStore store, long retryPauseMillis) {
        this.provider = provider;
        this.store = store;
        this.retryPauseMillis = retryPauseMillis;
    }

    /**
     * Cuts a book the way this service needs it.
     *
     * @param text the book text
     * @return the pieces to send
     */
    public List<String> pieces(String text) {
        return TextChunker.chunk(Paginator.toParagraphs(Paginator.stripGutenbergBoilerplate(text)),
                provider.maxChunkChars());
    }

    /**
     * Translates a book.
     *
     * @param bookKey  identifies the book, used for the saved file
     * @param text     the book text
     * @param language the target language
     * @param listener receives progress, may be {@code null}
     * @param cancel   set to {@code true} to stop; the progress is saved
     * @return the translated book as text with blank lines between paragraphs
     * @throws IOException           if a piece fails for good; the finished pieces stay saved
     * @throws InterruptedException  if the thread is interrupted
     * @throws CancellationException if the user cancelled
     */
    public String translate(String bookKey, String text, Language language, Listener listener, AtomicBoolean cancel)
            throws IOException, InterruptedException {
        if (!provider.supports(language)) {
            throw new IOException(provider.displayName() + " cannot translate into " + language.name() + ".");
        }
        List<String> pieces = pieces(text);
        if (pieces.isEmpty()) {
            throw new IOException("There is no text to translate.");
        }
        TranslationStore.Saved saved = store.load(bookKey, language, TranslationStore.hash(text), provider.maxChunkChars());
        int total = pieces.size();
        int sinceSave = 0;
        if (listener != null) {
            listener.progress(saved.chunks().size(), total);
        }
        for (int i = 0; i < total; i++) {
            if (saved.chunks().containsKey(i)) {
                continue;
            }
            if (cancel.get()) {
                store.save(bookKey, language, saved, total, provider.displayName());
                throw new CancellationException("Translation stopped. Finished parts are saved.");
            }
            String translated;
            try {
                translated = translateWithRetry(pieces.get(i), language, cancel, listener);
            } catch (IOException | CancellationException e) {
                store.save(bookKey, language, saved, total, provider.displayName());
                if (e instanceof CancellationException) {
                    throw e;
                }
                throw new IOException(e.getMessage() + " (" + saved.chunks().size() + " of " + total
                        + " parts are saved. Start again to continue.)", e);
            }
            saved.chunks().put(i, translated);
            sinceSave++;
            if (sinceSave >= SAVE_EVERY) {
                store.save(bookKey, language, saved, total, provider.displayName());
                sinceSave = 0;
            }
            if (listener != null) {
                listener.progress(saved.chunks().size(), total);
            }
        }
        store.save(bookKey, language, saved, total, provider.displayName());
        return saved.text();
    }

    private String translateWithRetry(String piece, Language language, AtomicBoolean cancel, Listener listener)
            throws IOException, InterruptedException {
        TranslationException last = null;
        for (int attempt = 1; attempt <= ATTEMPTS; attempt++) {
            if (cancel.get()) {
                throw new CancellationException("Translation stopped. Finished parts are saved.");
            }
            try {
                return provider.translate(piece, language);
            } catch (TranslationException e) {
                if (!e.isRetryable()) {
                    throw e;
                }
                last = e;
                if (attempt == ATTEMPTS) {
                    break;
                }
                long pause = retryPauseMillis * attempt;
                if (listener != null) {
                    listener.waiting(attempt, ATTEMPTS, pause / 1000, e.getMessage());
                }
                Thread.sleep(pause);
            }
        }
        throw last;
    }
}
