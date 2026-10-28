package com.booklibrary.reader.speech;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Text to speech on macOS using the built-in {@code say} program.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented speech on Mac, with all voices installed in System Settings.
 * Purpose: {@code say} is free, works offline and supports many languages. Better
 * "Enhanced" or "Premium" voices can be downloaded in System Settings and then appear in the list.
 */
public class MacSpeechEngine implements SpeechEngine {

    private static final Path SAY = Path.of("/usr/bin/say");
    private static final Pattern VOICE_LINE = Pattern.compile("^(.+?)\\s{2,}([A-Za-z]{2,3}[_-][A-Za-z0-9]+)\\s+#.*$");

    /** Fun voices (robots, bells, whispers) that are hard to understand. They are hidden. */
    private static final java.util.Set<String> NOVELTY = java.util.Set.of(
            "Albert", "Bad News", "Bahh", "Bells", "Boing", "Bubbles", "Cellos", "Deranged", "Good News",
            "Hysterical", "Jester", "Junior", "Kathy", "Organ", "Princess", "Ralph", "Trinoids", "Whisper",
            "Wobble", "Zarvox", "Fred", "Superstar", "Grandma", "Grandpa", "Flo", "Reed", "Rocko", "Sandy",
            "Shelley", "Eddy");
    private static final java.util.List<String> FAVOURITES = java.util.List.of(
            "Ava", "Zoe", "Evan", "Nathan", "Samantha", "Daniel", "Karen", "Moira", "Tessa", "Serena",
            "Oliver", "Nora", "Henrik", "Iselin", "Lee", "Allison", "Tom");

    /** Lower number = better. Premium and Enhanced voices sound the most natural. */
    private static int rank(Voice voice) {
        String name = voice.name();
        if (name.contains("Premium")) {
            return 0;
        }
        if (name.contains("Enhanced")) {
            return 1;
        }
        String base = name.replaceAll("\\s*\\(.*\\)", "").strip();
        int favourite = FAVOURITES.indexOf(base);
        return favourite >= 0 ? 2 : 3;
    }

    @Override
    public String getName() {
        return "macOS speech";
    }

    @Override
    public boolean isAvailable() {
        return Files.isExecutable(SAY);
    }

    @Override
    public List<Voice> voices() {
        List<Voice> voices = new ArrayList<>();
        try {
            Process process = new ProcessBuilder(SAY.toString(), "-v", "?").redirectErrorStream(true).start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            process.waitFor();
            for (String line : output.split("\n")) {
                Matcher matcher = VOICE_LINE.matcher(line.stripTrailing());
                if (matcher.matches()) {
                    voices.add(new Voice(matcher.group(1).strip(), matcher.group(2)));
                }
            }
        } catch (IOException e) {
            return List.of();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        voices.removeIf(v -> NOVELTY.contains(v.name().replaceAll("\\s*\\(.*\\)", "").strip()));
        voices.sort(java.util.Comparator.comparingInt(MacSpeechEngine::rank).thenComparing(Voice::name));
        return voices;
    }

    @Override
    public Process speak(String text, Voice voice, int wordsPerMinute) throws IOException {
        List<String> command = new ArrayList<>(List.of(SAY.toString()));
        if (voice != null) {
            command.add("-v");
            command.add(voice.name());
        }
        command.add("-r");
        command.add(String.valueOf(wordsPerMinute));
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        try (OutputStream in = process.getOutputStream()) {
            in.write(text.getBytes(StandardCharsets.UTF_8));
        }
        return process;
    }
}
