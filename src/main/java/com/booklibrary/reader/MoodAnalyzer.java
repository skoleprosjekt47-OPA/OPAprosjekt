package com.booklibrary.reader;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Decides which music mood fits a page of text.
 *
 * Author: Farid Rahmani
 * Contribution: Designed the keyword based mood detection used for the reading music.
 * Purpose: counts mood words on the page (fight, kiss, tears, dark ...)
 * and picks the strongest mood that the {@link BookType} allows. To avoid the
 * music jumping around, the previous mood is kept unless the new one is clearly stronger.
 */
public class MoodAnalyzer {

    private static final Map<Mood, String[]> WORDS = new EnumMap<>(Mood.class);

    static {
        WORDS.put(Mood.ACTION, new String[]{"fight", "fought", "battle", "sword", "blade", "gun", "shot", "shoot",
                "blood", "run", "ran", "chase", "chased", "attack", "charge", "scream", "crash", "explod", "escape",
                "danger", "weapon", "kill", "strike", "struck", "rush", "flee", "fled", "war", "enemy", "enemies",
                "storm", "leap", "leapt", "smash", "hurry", "quick", "furious", "roar"});
        WORDS.put(Mood.ROMANTIC, new String[]{"love", "loved", "kiss", "beloved", "darling", "heart", "tender",
                "embrace", "passion", "desire", "dear", "sweet", "adore", "affection", "romance", "blush",
                "gentle", "marry", "marriage", "devotion", "beauty", "beautiful", "whisper"});
        WORDS.put(Mood.SAD, new String[]{"tear", "weep", "wept", "cry", "cried", "grief", "mourn", "death", "dead",
                "died", "lonely", "alone", "sorrow", "sad", "funeral", "loss", "lost", "farewell", "regret",
                "misery", "despair", "sigh", "sob", "gloom", "melanchol"});
        WORDS.put(Mood.TENSE, new String[]{"fear", "afraid", "terror", "horror", "dread", "trembl", "shudder",
                "panic", "nervous", "threat", "suspicion", "suspect", "hide", "hid", "silence", "silent",
                "footstep", "creep", "crept", "shadow", "scared", "frighten", "anxious", "danger", "waiting"});
        WORDS.put(Mood.MYSTERIOUS, new String[]{"mystery", "mysterious", "secret", "strange", "dark", "darkness",
                "night", "ghost", "unknown", "riddle", "curious", "whisper", "ancient", "forbidden", "hidden",
                "magic", "spell", "fog", "mist", "candle", "clue", "puzzle", "enigma", "symbol", "tomb"});
        WORDS.put(Mood.UPLIFTING, new String[]{"hope", "joy", "joyful", "laugh", "happy", "happiness", "success",
                "triumph", "victory", "dream", "inspire", "courage", "brave", "believe", "grow", "growth",
                "achieve", "celebrate", "bright", "smile", "freedom", "confidence", "purpose", "goal", "habit"});
        WORDS.put(Mood.CALM, new String[]{"quiet", "peace", "calm", "gentle", "soft", "slowly", "garden", "morning",
                "river", "meadow", "rest", "breeze", "stroll", "tea", "sunlight", "pleasant", "serene"});
    }

    /** How many points a new mood must be ahead of the current mood before the music changes. */
    private static final double SWITCH_MARGIN = 1.0;
    /** Minimum score needed before a mood is considered at all. */
    private static final double MIN_SCORE = 2.0;

    private final BookType type;
    private Mood current;

    /**
     * Creates an analyzer for one book.
     *
     * @param type the type of the book being read
     */
    public MoodAnalyzer(BookType type) {
        this.type = type;
        this.current = type.getDefaultMood();
    }

    /**
     * Scores a page without changing any state.
     *
     * @param page the text of the page
     * @return a score per mood; higher means a better fit
     */
    public Map<Mood, Double> score(String page) {
        Map<Mood, Double> scores = new EnumMap<>(Mood.class);
        String[] words = page.toLowerCase(Locale.ROOT).split("[^a-zæøå]+");
        for (Mood mood : Mood.values()) {
            double points = 0;
            for (String word : words) {
                for (String stem : WORDS.get(mood)) {
                    if (word.startsWith(stem)) {
                        points += 1;
                        break;
                    }
                }
            }
            scores.put(mood, points);
        }
        // exclamation marks and short sentences add excitement
        long exclamations = page.chars().filter(c -> c == '!').count();
        scores.merge(Mood.ACTION, exclamations * 0.5, Double::sum);
        scores.merge(Mood.TENSE, page.chars().filter(c -> c == '?').count() * 0.2, Double::sum);
        return scores;
    }

    /**
     * Picks the mood for the next page. Remembers the choice so the next call can avoid unnecessary changes.
     *
     * @param page the text of the page
     * @return the mood the music should have; always allowed by the book type
     */
    public Mood next(String page) {
        Map<Mood, Double> scores = score(page);
        Mood best = null;
        double bestScore = 0;
        for (Mood mood : Mood.values()) {
            if (!type.allows(mood)) {
                continue;
            }
            double value = scores.get(mood);
            if (value > bestScore) {
                best = mood;
                bestScore = value;
            }
        }
        if (best == null || bestScore < MIN_SCORE) {
            // nothing special on this page: settle back to the book's normal mood
            current = type.getDefaultMood();
            return current;
        }
        double currentScore = scores.get(current);
        if (!type.allows(current) || bestScore >= currentScore + SWITCH_MARGIN) {
            current = best;
        }
        return current;
    }
}
