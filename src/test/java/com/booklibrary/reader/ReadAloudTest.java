package com.booklibrary.reader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.booklibrary.reader.speech.SpeechEngine;
import com.booklibrary.reader.speech.Voice;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * Tests for sentence splitting, word timing, the read aloud player and the saved reading settings.
 *
 * Author: Farid Rahmani
 * Contribution: Wrote the tests for the reading aids.
 */
class ReadAloudTest {

    @Test
    void splitsTextIntoSentencesAndParagraphs() {
        String text = "First one. Second one! Third?\n\nNew paragraph here.";
        List<int[]> ranges = ReadAloud.sentenceRanges(text, Locale.ENGLISH);
        List<String> sentences = new ArrayList<>();
        for (int[] r : ranges) {
            sentences.add(text.substring(r[0], r[1]));
        }
        assertEquals(List.of("First one.", "Second one!", "Third?", "New paragraph here."), sentences);
    }

    @Test
    void findsWordsWithTheirPositions() {
        String text = "Hello big world";
        List<int[]> words = ReadAloud.wordRanges(text, 6, 15);
        assertEquals(2, words.size());
        assertEquals("big", text.substring(words.get(0)[0], words.get(0)[1]));
        assertEquals("world", text.substring(words.get(1)[0], words.get(1)[1]));
    }

    @Test
    void wordTimesGrowAndStayInsideTheSentenceTime() {
        String text = "one two three four five six";
        List<int[]> words = ReadAloud.wordRanges(text, 0, text.length());
        long[] times = ReadAloud.wordStartTimes(words, 120);
        assertEquals(0, times[0]);
        for (int i = 1; i < times.length; i++) {
            assertTrue(times[i] > times[i - 1]);
        }
        assertTrue(times[times.length - 1] < 6 * 500);
    }

    @Test
    void fasterSpeedGivesShorterTimes() {
        List<int[]> words = ReadAloud.wordRanges("alpha beta gamma delta", 0, 22);
        long slow = ReadAloud.wordStartTimes(words, 100)[3];
        long fast = ReadAloud.wordStartTimes(words, 300)[3];
        assertTrue(fast < slow);
    }

    @Test
    void settingsAreSavedAndKeptInsideLimits() throws BackingStoreException {
        Preferences node = Preferences.userRoot().node("booklibrary-test-" + UUID.randomUUID());
        try {
            ReaderSettings settings = new ReaderSettings(node);
            assertEquals("Georgia", settings.getFontFamily());
            settings.setFontFamily("Verdana");
            settings.setFontSize(500);
            settings.setContrast(ContrastMode.YELLOW);
            settings.setSpeed(10);
            ReaderSettings again = new ReaderSettings(node);
            assertEquals("Verdana", again.getFontFamily());
            assertEquals(48, again.getFontSize());
            assertEquals(ContrastMode.YELLOW, again.getContrast());
            assertEquals(90, again.getSpeed());
        } finally {
            node.removeNode();
        }
    }

    /** A speech engine that "speaks" by waiting a short time, so the player can be tested without sound. */
    private static class QuietEngine implements SpeechEngine {
        @Override
        public String getName() {
            return "quiet";
        }

        @Override
        public boolean isAvailable() {
            return true;
        }

        @Override
        public List<Voice> voices() {
            return List.of();
        }

        @Override
        public Process speak(String text, Voice voice, int wordsPerMinute) throws IOException {
            return new ProcessBuilder("sleep", "0.15").start();
        }
    }

    @Test
    void readsAllSentencesInOrderAndFinishes() throws Exception {
        Assumptions.assumeFalse(System.getProperty("os.name").toLowerCase().contains("win"));
        List<Integer> heard = new CopyOnWriteArrayList<>();
        CountDownLatch done = new CountDownLatch(1);
        ReadAloud player = new ReadAloud(new QuietEngine(), new ReadAloud.Listener() {
            @Override
            public void sentence(int index, int start, int end) {
                heard.add(index);
            }

            @Override
            public void word(int start, int end) {
            }

            @Override
            public void stateChanged(ReadAloud.State state) {
            }

            @Override
            public void finished() {
                done.countDown();
            }

            @Override
            public void failed(String message) {
            }
        });
        String text = "One. Two. Three.";
        player.start(text, ReadAloud.sentenceRanges(text, Locale.ENGLISH), 0, null, 170);
        assertTrue(done.await(5, TimeUnit.SECONDS));
        assertEquals(List.of(0, 1, 2), heard);
        assertEquals(ReadAloud.State.IDLE, player.getState());
    }

    @Test
    void pauseStopsAndResumeContinuesFromTheSameSentence() throws Exception {
        Assumptions.assumeFalse(System.getProperty("os.name").toLowerCase().contains("win"));
        CountDownLatch finished = new CountDownLatch(1);
        List<Integer> heard = new CopyOnWriteArrayList<>();
        ReadAloud player = new ReadAloud(new QuietEngine(), new ReadAloud.Listener() {
            @Override
            public void sentence(int index, int start, int end) {
                heard.add(index);
            }

            @Override
            public void word(int start, int end) {
            }

            @Override
            public void stateChanged(ReadAloud.State state) {
            }

            @Override
            public void finished() {
                finished.countDown();
            }

            @Override
            public void failed(String message) {
            }
        });
        String text = "One. Two. Three. Four.";
        player.start(text, ReadAloud.sentenceRanges(text, Locale.ENGLISH), 0, null, 170);
        Thread.sleep(250);
        player.pause();
        int stoppedAt = player.getCurrent();
        assertEquals(ReadAloud.State.PAUSED, player.getState());
        Thread.sleep(400);
        assertEquals(stoppedAt, player.getCurrent());
        player.resume();
        assertTrue(finished.await(5, TimeUnit.SECONDS));
        assertEquals(3, heard.get(heard.size() - 1));
    }
}
