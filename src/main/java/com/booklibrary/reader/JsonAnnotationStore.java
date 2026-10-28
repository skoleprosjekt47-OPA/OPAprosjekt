package com.booklibrary.reader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps the marks in a JSON file in the user's home folder.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented the permanent storage of highlights and comments.
 * Purpose: the file ({@code ~/.booklibrary/annotations.json}) belongs to the
 * computer user, so marks of different people on the same computer do not mix.
 * A new file is written first and then moved into place, so a crash while
 * saving cannot destroy the old data.
 */
public class JsonAnnotationStore implements AnnotationStore {

    private static final Type TYPE = new TypeToken<Map<String, List<Annotation>>>() { }.getType();

    private final Path file;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Creates a store that uses a given file.
     *
     * @param file the JSON file; it is created when the first mark is saved
     */
    public JsonAnnotationStore(Path file) {
        this.file = file;
    }

    /**
     * Creates the store in the normal place.
     *
     * @return a store using {@code ~/.booklibrary/annotations.json}
     */
    public static JsonAnnotationStore atDefaultLocation() {
        return new JsonAnnotationStore(Path.of(System.getProperty("user.home"), ".booklibrary", "annotations.json"));
    }

    @Override
    public synchronized List<Annotation> load(String bookKey) {
        return new ArrayList<>(readAll().getOrDefault(bookKey, List.of()));
    }

    @Override
    public synchronized void save(String bookKey, List<Annotation> marks) {
        Map<String, List<Annotation>> all = readAll();
        if (marks.isEmpty()) {
            all.remove(bookKey);
        } else {
            all.put(bookKey, new ArrayList<>(marks));
        }
        try {
            Files.createDirectories(file.getParent());
            Path temp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(temp, gson.toJson(all, TYPE), StandardCharsets.UTF_8);
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save highlights and comments", e);
        }
    }

    private Map<String, List<Annotation>> readAll() {
        if (!Files.exists(file)) {
            return new HashMap<>();
        }
        try {
            Map<String, List<Annotation>> all = gson.fromJson(Files.readString(file, StandardCharsets.UTF_8), TYPE);
            return all == null ? new HashMap<>() : all;
        } catch (IOException | JsonParseException e) {
            backUpBrokenFile();
            return new HashMap<>();
        }
    }

    private void backUpBrokenFile() {
        try {
            Files.move(file, file.resolveSibling(file.getFileName() + ".broken"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
            // nothing more can be done
        }
    }
}
