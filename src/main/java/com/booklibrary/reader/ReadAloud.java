package com.booklibrary.reader;

import com.booklibrary.reader.speech.SpeechEngine;
import com.booklibrary.reader.speech.Voice;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads a page aloud, one sentence at a time, and reports which sentence and word is being read.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented the read aloud player with pause, resume, skip and word following.
 * Purpose: each sentence is given to the speech engine as its own job. That makes
 * pause (stop the job, remember the sentence), resume (read the sentence again),
 * and skipping forward and back simple and reliable. The sentence being read is
 * exactly known. The word being read is estimated from the reading speed, because
 * the operating system speech programs do not report word positions.
 * The class has no JavaFX code; the listener is called from background threads.
 */
public class ReadAloud {

    /** The state of the player. */
    public enum State { IDLE, PLAYING, PAUSED }

    /** Receives events from the player. All methods are called from background threads. */
    public interface Listener {
        /** A new sentence starts. */
        void sentence(int index, int start, int end);

        /** The estimated current word. */
        void word(int start, int end);

        /** The state changed. */
        void stateChanged(State state);

        /** All sentences were read to the end. */
        void finished();

        /** Reading failed. */
        void failed(String message);
    }

    private static final Pattern WORD = Pattern.compile("\\S+");
    private static final int START_DELAY_MILLIS = 250;

    private final SpeechEngine engine;
    private final Listener listener;
    private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "read-aloud-words");
        thread.setDaemon(true);
        return thread;
    });
    private final List<ScheduledFuture<?>> wordTasks = new ArrayList<>();

    private String text = "";
    private List<int[]> sentences = List.of();
    private Voice voice;
    private int wordsPerMinute = 170;
    private volatile int generation;
    private volatile int current;
    private volatile State state = State.IDLE;
    private Process process;

    /**
     * Creates a player.
     *
     * @param engine   the speech system
     * @param listener receives events
     */
    public ReadAloud(SpeechEngine engine, Listener listener) {
        this.engine = engine;
        this.listener = listener;
    }

    // ------------------------------------------------------------------ helpers that can be tested alone

    /**
     * Splits a text into sentences. Paragraph breaks also end a sentence.
     *
     * @param text   the text
     * @param locale the language, used for the rules of sentence endings
     * @return the sentences as {start, end} pairs, in order, without empty ones
     */
    public static List<int[]> sentenceRanges(String text, Locale locale) {
        List<int[]> ranges = new ArrayList<>();
        int paragraphStart = 0;
        while (paragraphStart <= text.length()) {
            int paragraphEnd = text.indexOf('\n', paragraphStart);
            if (paragraphEnd < 0) {
                paragraphEnd = text.length();
            }
            addSentences(text, paragraphStart, paragraphEnd, locale, ranges);
            paragraphStart = paragraphEnd + 1;
        }
        return ranges;
    }

    private static void addSentences(String text, int from, int to, Locale locale, List<int[]> ranges) {
        if (to <= from) {
            return;
        }
        BreakIterator iterator = BreakIterator.getSentenceInstance(locale);
        String paragraph = text.substring(from, to);
        iterator.setText(paragraph);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            int s = start;
            int e = end;
            while (s < e && Character.isWhitespace(paragraph.charAt(s))) {
                s++;
            }
            while (e > s && Character.isWhitespace(paragraph.charAt(e - 1))) {
                e--;
            }
            if (e > s) {
                ranges.add(new int[]{from + s, from + e});
            }
        }
    }

    /**
     * Finds the words inside a part of a text.
     *
     * @param text  the text
     * @param start the first character
     * @param end   the position after the last character
     * @return the words as {start, end} pairs
     */
    public static List<int[]> wordRanges(String text, int start, int end) {
        List<int[]> words = new ArrayList<>();
        Matcher matcher = WORD.matcher(text.substring(start, end));
        while (matcher.find()) {
            words.add(new int[]{start + matcher.start(), start + matcher.end()});
        }
        return words;
    }

    /**
     * Estimates when each word starts to be spoken. Longer words get more time.
     *
     * @param words          the words from {@link #wordRanges}
     * @param wordsPerMinute the reading speed
     * @return for each word the start time in milliseconds after the sentence began
     */
    public static long[] wordStartTimes(List<int[]> words, int wordsPerMinute) {
        long[] times = new long[words.size()];
        double totalWeight = 0;
        for (int[] word : words) {
            totalWeight += word[1] - word[0] + 3;
        }
        double totalMillis = words.size() * 60000.0 / Math.max(60, wordsPerMinute);
        double passed = 0;
        for (int i = 0; i < words.size(); i++) {
            times[i] = Math.round(totalMillis * passed / totalWeight);
            passed += words.get(i)[1] - words.get(i)[0] + 3;
        }
        return times;
    }

    // ------------------------------------------------------------------ controls

    /**
     * Starts reading.
     *
     * @param pageText  the text
     * @param ranges    the sentences to read, from {@link #sentenceRanges}
     * @param fromIndex the sentence to start with
     * @param newVoice  the voice, or {@code null} for the default
     * @param wpm       the speed in words per minute
     */
    public synchronized void start(String pageText, List<int[]> ranges, int fromIndex, Voice newVoice, int wpm) {
        this.text = pageText;
        this.sentences = List.copyOf(ranges);
        this.voice = newVoice;
        this.wordsPerMinute = wpm;
        if (sentences.isEmpty()) {
            setState(State.IDLE);
            listener.finished();
            return;
        }
        playFrom(fromIndex);
    }

    /** Pauses reading. Resuming starts the same sentence again. */
    public synchronized void pause() {
        if (state == State.PLAYING) {
            cancelCurrent();
            setState(State.PAUSED);
        }
    }

    /** Continues after a pause. */
    public synchronized void resume() {
        if (state == State.PAUSED) {
            playFrom(current);
        }
    }

    /** Stops reading completely. */
    public synchronized void stop() {
        cancelCurrent();
        setState(State.IDLE);
    }

    /**
     * Jumps to another sentence.
     *
     * @param index the sentence number; it is kept inside the valid range
     */
    public synchronized void jumpTo(int index) {
        if (sentences.isEmpty()) {
            return;
        }
        int target = Math.max(0, Math.min(sentences.size() - 1, index));
        if (state == State.PLAYING) {
            playFrom(target);
        } else {
            current = target;
            int[] range = sentences.get(target);
            listener.sentence(target, range[0], range[1]);
        }
    }

    /**
     * Changes the voice. If reading, the current sentence is read again with the new voice.
     *
     * @param newVoice the voice
     */
    public synchronized void setVoice(Voice newVoice) {
        this.voice = newVoice;
        if (state == State.PLAYING) {
            playFrom(current);
        }
    }

    /**
     * Changes the speed. If reading, the current sentence is read again at the new speed.
     *
     * @param wpm words per minute
     */
    public synchronized void setSpeed(int wpm) {
        this.wordsPerMinute = wpm;
        if (state == State.PLAYING) {
            playFrom(current);
        }
    }

    /**
     * Returns the number of the sentence being read.
     *
     * @return the sentence number, starting at 0
     */
    public int getCurrent() {
        return current;
    }

    /**
     * Returns the number of sentences.
     *
     * @return how many sentences are loaded
     */
    public int getSentenceCount() {
        return sentences.size();
    }

    /**
     * Returns the state.
     *
     * @return idle, playing or paused
     */
    public State getState() {
        return state;
    }

    // ------------------------------------------------------------------ inner work

    private void setState(State newState) {
        if (state != newState) {
            state = newState;
            listener.stateChanged(newState);
        }
    }

    private void cancelCurrent() {
        generation++;
        if (process != null) {
            process.destroy();
            process = null;
        }
        synchronized (wordTasks) {
            for (ScheduledFuture<?> task : wordTasks) {
                task.cancel(false);
            }
            wordTasks.clear();
        }
    }

    private void playFrom(int index) {
        cancelCurrent();
        current = Math.max(0, Math.min(sentences.size() - 1, index));
        int gen = generation;
        int first = current;
        setState(State.PLAYING);
        Thread thread = new Thread(() -> run(gen, first), "read-aloud");
        thread.setDaemon(true);
        thread.start();
    }

    private void run(int gen, int first) {
        try {
            for (int i = first; i < sentences.size(); i++) {
                int[] range = sentences.get(i);
                Process running;
                synchronized (this) {
                    if (gen != generation) {
                        return;
                    }
                    current = i;
                    running = engine.speak(text.substring(range[0], range[1]), voice, wordsPerMinute);
                    process = running;
                    scheduleWords(gen, range[0], range[1]);
                }
                listener.sentence(i, range[0], range[1]);
                int exit = running.waitFor();
                synchronized (this) {
                    if (gen != generation) {
                        return;
                    }
                    if (exit != 0) {
                        setState(State.IDLE);
                        listener.failed("The speech program stopped with an error. Try another voice.");
                        return;
                    }
                }
            }
            synchronized (this) {
                if (gen != generation) {
                    return;
                }
                process = null;
                setState(State.IDLE);
            }
            listener.finished();
        } catch (IOException e) {
            synchronized (this) {
                if (gen == generation) {
                    setState(State.IDLE);
                }
            }
            listener.failed("Could not start speech: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void scheduleWords(int gen, int start, int end) {
        List<int[]> words = wordRanges(text, start, end);
        long[] times = wordStartTimes(words, wordsPerMinute);
        synchronized (wordTasks) {
            for (int i = 0; i < words.size(); i++) {
                int[] word = words.get(i);
                wordTasks.add(timer.schedule(() -> {
                    if (gen == generation) {
                        listener.word(word[0], word[1]);
                    }
                }, START_DELAY_MILLIS + times[i], TimeUnit.MILLISECONDS));
            }
        }
    }
}
