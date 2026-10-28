package com.booklibrary.translate;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Saves translations on the computer, one file per book and language, in
 * {@code ~/.booklibrary/translations}.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented saving of finished and half finished translations.
 * Purpose: a long book takes many requests. Every finished piece is saved, so a stopped or failed
 * translation continues where it ended, and nothing is paid for twice. A finished translation can be
 * opened again at once. The files belong to the operating system user, like the highlights do.
 */
public class TranslationStore {

    private final Path directory;
    private final Gson gson = new Gson();

    /**
     * Creates a store.
     *
     * @param directory the folder for the files
     */
    public TranslationStore(Path directory) {
        this.directory = directory;
    }

    /**
     * Creates a store in {@code ~/.booklibrary/translations}.
     *
     * @return the store
     */
    public static TranslationStore atDefaultLocation() {
        return new TranslationStore(Path.of(System.getProperty("user.home"), ".booklibrary", "translations"));
    }

    /** What is saved for one book and language. */
    public static final class Saved {
        private String textHash = "";
        private int chunkSize;
        private int total;
        private String provider = "";
        private Map<Integer, String> chunks = new TreeMap<>();

        /**
         * Returns the finished pieces.
         *
         * @return piece number to translated text
         */
        public Map<Integer, String> chunks() {
            return chunks;
        }

        /**
         * Tells if every piece is translated.
         *
         * @return {@code true} when complete
         */
        public boolean isComplete() {
            return total > 0 && chunks.size() >= total;
        }

        /**
         * Returns the whole translated text.
         *
         * @return all pieces in order, separated by blank lines
         */
        public String text() {
            return String.join("\n\n", new TreeMap<>(chunks).values());
        }

        /**
         * Returns the service that made the translation.
         *
         * @return the name
         */
        public String provider() {
            return provider;
        }
    }

    /**
     * Makes a short fingerprint of a text, to know if a saved translation belongs to it.
     *
     * @param text the original text
     * @return a hex string
     */
    public static String hash(String text) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes, 0, 12);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private Path fileFor(String bookKey, Language language) {
        String safe = bookKey.replaceAll("[^A-Za-z0-9_-]", "_");
        return directory.resolve(safe + "." + language.code() + ".json");
    }

    /**
     * Loads what is saved, if it belongs to this text and these piece sizes.
     *
     * @param bookKey   identifies the book
     * @param language  the target language
     * @param textHash  fingerprint of the original text
     * @param chunkSize the piece size that is used now; a different size cannot be continued
     * @return the saved data, or a new empty one
     */
    public Saved load(String bookKey, Language language, String textHash, int chunkSize) {
        Saved saved = read(bookKey, language);
        if (saved == null || !textHash.equals(saved.textHash) || chunkSize != saved.chunkSize) {
            Saved fresh = new Saved();
            fresh.textHash = textHash;
            fresh.chunkSize = chunkSize;
            return fresh;
        }
        return saved;
    }

    /**
     * Finds a finished translation of exactly this text.
     *
     * @param bookKey  identifies the book
     * @param language the target language
     * @param textHash fingerprint of the original text
     * @return the finished translation, or empty
     */
    public Optional<Saved> completed(String bookKey, Language language, String textHash) {
        Saved saved = read(bookKey, language);
        if (saved != null && textHash.equals(saved.textHash) && saved.isComplete()) {
            return Optional.of(saved);
        }
        return Optional.empty();
    }

    private Saved read(String bookKey, Language language) {
        Path file = fileFor(bookKey, language);
        if (!Files.isRegularFile(file)) {
            return null;
        }
        try {
            Saved saved = gson.fromJson(Files.readString(file, StandardCharsets.UTF_8), Saved.class);
            if (saved != null && saved.chunks == null) {
                saved.chunks = new TreeMap<>();
            }
            return saved;
        } catch (IOException | JsonParseException e) {
            return null;
        }
    }

    /**
     * Saves the state. The file is written to a temporary file first, so a crash cannot damage it.
     *
     * @param bookKey  identifies the book
     * @param language the target language
     * @param saved    the data
     * @param total    how many pieces the whole book has
     * @param provider the service used
     * @throws IOException if the file cannot be written
     */
    public void save(String bookKey, Language language, Saved saved, int total, String provider) throws IOException {
        saved.total = total;
        saved.provider = provider;
        Files.createDirectories(directory);
        Path file = fileFor(bookKey, language);
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(temp, gson.toJson(saved), StandardCharsets.UTF_8);
        Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Removes a saved translation.
     *
     * @param bookKey  identifies the book
     * @param language the target language
     * @throws IOException if the file cannot be deleted
     */
    public void delete(String bookKey, Language language) throws IOException {
        Files.deleteIfExists(fileFor(bookKey, language));
    }
}
