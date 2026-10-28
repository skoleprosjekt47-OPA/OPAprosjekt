package com.booklibrary.reader.speech;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Text to speech on Windows using the voices of the system, reached through PowerShell.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented speech on Windows so team members on Windows can use the reader too.
 */
public class WindowsSpeechEngine implements SpeechEngine {

    private static final String SPEAK_SCRIPT = "[Console]::InputEncoding = [System.Text.Encoding]::UTF8; "
            + "Add-Type -AssemblyName System.Speech; "
            + "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; "
            + "if ($env:BL_VOICE) { $s.SelectVoice($env:BL_VOICE) }; "
            + "$s.Rate = [int]$env:BL_RATE; "
            + "$t = [Console]::In.ReadToEnd(); $s.Speak($t)";

    private static final String LIST_SCRIPT = "Add-Type -AssemblyName System.Speech; "
            + "(New-Object System.Speech.Synthesis.SpeechSynthesizer).GetInstalledVoices() | "
            + "ForEach-Object { $_.VoiceInfo.Name + '|' + $_.VoiceInfo.Culture.Name }";

    @Override
    public String getName() {
        return "Windows speech";
    }

    @Override
    public boolean isAvailable() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    @Override
    public List<Voice> voices() {
        List<Voice> voices = new ArrayList<>();
        try {
            Process process = new ProcessBuilder("powershell", "-NoProfile", "-NonInteractive", "-Command", LIST_SCRIPT)
                    .redirectErrorStream(true).start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            process.waitFor();
            for (String line : output.split("\\R")) {
                String[] parts = line.strip().split("\\|");
                if (parts.length == 2 && !parts[0].isBlank()) {
                    voices.add(new Voice(parts[0], parts[1].replace('-', '_')));
                }
            }
        } catch (IOException e) {
            return List.of();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return voices;
    }

    @Override
    public Process speak(String text, Voice voice, int wordsPerMinute) throws IOException {
        int rate = Math.max(-10, Math.min(10, Math.round((wordsPerMinute - 175) / 15f)));
        ProcessBuilder builder = new ProcessBuilder("powershell", "-NoProfile", "-NonInteractive", "-Command", SPEAK_SCRIPT)
                .redirectErrorStream(true);
        builder.environment().put("BL_RATE", String.valueOf(rate));
        builder.environment().put("BL_VOICE", voice == null ? "" : voice.name());
        Process process = builder.start();
        try (OutputStream in = process.getOutputStream()) {
            in.write(text.getBytes(StandardCharsets.UTF_8));
        }
        return process;
    }
}
