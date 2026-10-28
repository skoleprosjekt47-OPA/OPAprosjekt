package com.booklibrary.translate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.function.Function;

/**
 * Finds the API keys and server addresses of the translation services.
 *
 * Author: Farid Rahmani (team 10), written with help from Claude (AI).
 * Contribution: Added a second place to put the keys, so that everyone who runs the project can translate without
 * setting environment variables.
 * Purpose: a value is looked up first in the environment variable of the same name, and then in a file called
 * {@code keys.properties}, first in the folder the program is started from and then in {@code ~/.booklibrary}.
 * The file is ignored by git, so a key is never committed. The program only reads the file and never writes a key.
 */
public final class ApiKeys {

    private ApiKeys() {
    }

    /**
     * Looks up one value the way the application does it.
     *
     * @param name the name, for example {@code GEMINI_API_KEY}
     * @return the value, or {@code null} if it is set nowhere or is blank
     */
    public static String get(String name) {
        return lookup(name, System::getenv,
                Path.of("keys.properties"),
                Path.of(System.getProperty("user.home", "."), ".booklibrary", "keys.properties"));
    }

    /**
     * Looks up one value with the given sources. Used by {@link #get(String)} and by the tests.
     *
     * @param name  the name of the value
     * @param env   reads an environment variable
     * @param files property files to try, in order
     * @return the first non-blank value found, or {@code null}
     */
    static String lookup(String name, Function<String, String> env, Path... files) {
        String fromEnv = env.apply(name);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.strip();
        }
        for (Path file : files) {
            String value = readFrom(file, name);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static String readFrom(Path file, String name) {
        if (!Files.isRegularFile(file)) {
            return null;
        }
        try (InputStream in = Files.newInputStream(file)) {
            Properties props = new Properties();
            props.load(in);
            String value = props.getProperty(name);
            return value == null || value.isBlank() ? null : value.strip();
        } catch (IOException | IllegalArgumentException e) {
            return null; // an unreadable file counts as "no key here"
        }
    }
}
