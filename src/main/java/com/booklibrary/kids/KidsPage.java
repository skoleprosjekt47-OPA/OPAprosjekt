package com.booklibrary.kids;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * One page of an animated story.
 *
 * Author: Farid Rahmani
 * Contribution: Designed how a page links its text to its animations.
 * Purpose: {@code cues} connect words to actors. When the voice reaches the word,
 * that character does the action (walks, jumps, sleeps...), so the story and the pictures move together.
 *
 * @param text     the story text of the page
 * @param backdrop the place
 * @param actors   the characters and objects on the page
 * @param cues     lower case word to what happens when the voice reads it
 */
public record KidsPage(String text, Backdrop backdrop, List<Placed> actors, Map<String, Cue> cues) {

    /**
     * Finds what should happen when a word is read.
     *
     * @param word a word as it appears in the text, maybe with punctuation
     * @return the cue, or {@code null} if the word has no cue
     */
    public Cue cueFor(String word) {
        if (word == null) {
            return null;
        }
        String clean = word.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}]", "");
        return cues.get(clean);
    }
}
