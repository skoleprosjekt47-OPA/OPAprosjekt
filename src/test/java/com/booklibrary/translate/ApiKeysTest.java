package com.booklibrary.translate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link ApiKeys}.
 *
 * Author: Farid Rahmani (team 10), written with help from Claude (AI).
 * Contribution: Tested the lookup order of the translation keys.
 * Purpose: checks that the environment wins, that the file is used as a fallback, and that missing or broken files do not crash.
 */
class ApiKeysTest {

    @TempDir
    Path folder;

    private Path write(String name, String text) throws IOException {
        Path file = folder.resolve(name);
        Files.writeString(file, text);
        return file;
    }

    @Test
    void environmentVariableWinsOverTheFile() throws IOException {
        Path file = write("keys.properties", "GEMINI_API_KEY=from-file\n");
        assertEquals("from-env", ApiKeys.lookup("GEMINI_API_KEY", name -> "from-env", file));
    }

    @Test
    void fileIsUsedWhenTheEnvironmentHasNothing() throws IOException {
        Path file = write("keys.properties", "GEMINI_API_KEY = from-file \n");
        assertEquals("from-file", ApiKeys.lookup("GEMINI_API_KEY", name -> null, file));
    }

    @Test
    void firstFileThatHasTheKeyWins() throws IOException {
        Path first = write("a.properties", "OTHER=1\n");
        Path second = write("b.properties", "GEMINI_API_KEY=second\n");
        assertEquals("second", ApiKeys.lookup("GEMINI_API_KEY", name -> "", first, second));
    }

    @Test
    void missingBlankOrUnknownGivesNull() throws IOException {
        Path blank = write("blank.properties", "GEMINI_API_KEY=   \n");
        assertNull(ApiKeys.lookup("GEMINI_API_KEY", name -> null, blank, folder.resolve("none.properties")));
        assertNull(ApiKeys.lookup("DEEPL_API_KEY", name -> null, blank));
    }

    @Test
    void brokenFileDoesNotCrash() throws IOException {
        Path broken = write("broken.properties", "GEMINI_API_KEY=\\uZZZZ\n");
        assertNull(ApiKeys.lookup("GEMINI_API_KEY", name -> null, broken));
    }
}
