package com.booklibrary.kids;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import org.junit.jupiter.api.Test;

/**
 * Tests that the built-in children's stories are complete and that their cues really work.
 *
 * Author: Farid Rahmani
 * Contribution: Wrote the tests that protect the story data (the drawing itself is checked by running the app).
 */
class KidsStoriesTest {

    @Test
    void storiesHaveTextAndActorsOnEveryPage() {
        assertFalse(KidsStories.all().isEmpty());
        for (KidsStory story : KidsStories.all()) {
            assertFalse(story.pages().isEmpty(), story.title());
            for (KidsPage page : story.pages()) {
                assertFalse(page.text().isBlank(), story.title());
                assertFalse(page.actors().isEmpty(), story.title());
            }
        }
    }

    @Test
    void everyCueWordIsInTheTextAndHasAnActorToMove() {
        for (KidsStory story : KidsStories.all()) {
            for (KidsPage page : story.pages()) {
                String text = page.text().toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}]+", " ");
                for (var cue : page.cues().entrySet()) {
                    assertTrue((" " + text + " ").contains(" " + cue.getKey() + " "),
                            "word not in text: " + cue.getKey());
                    assertTrue(page.actors().stream().anyMatch(a -> a.type() == cue.getValue().type()),
                            "no actor for cue: " + cue.getKey() + " in " + story.title());
                }
            }
        }
    }

    @Test
    void cueIgnoresCapitalsAndPunctuation() {
        KidsPage page = KidsStories.all().get(0).pages().get(0);
        assertEquals(Cue.of(ActorType.SUN, Action.REACT), page.cueFor("SUN."));
        assertEquals(ActorType.BIRD, page.cueFor("birds").type());
        assertNull(page.cueFor("meadow"));
        assertNull(page.cueFor("Luna,"));
        assertNull(page.cueFor(null));
    }

    @Test
    void storiesActOutRealActions() {
        long moves = KidsStories.all().stream().flatMap(s -> s.pages().stream()).flatMap(p -> p.cues().values().stream())
                .filter(c -> c.action() == Action.WALK || c.action() == Action.SWIM || c.action() == Action.FLY).count();
        assertTrue(moves >= 10, "characters should travel across the pages");
        assertTrue(KidsStories.all().size() >= 6);
    }

    @Test
    void movingActionsHaveADistanceAndTimedOnesHaveALength() {
        for (KidsStory story : KidsStories.all()) {
            for (KidsPage page : story.pages()) {
                for (Cue cue : page.cues().values()) {
                    boolean travels = cue.action() == Action.WALK || cue.action() == Action.SWIM || cue.action() == Action.FLY;
                    if (travels) {
                        assertTrue(cue.moveX() != 0 || cue.moveY() != 0, story.title());
                    }
                    assertTrue(cue.action() == Action.SLEEP || cue.action().seconds() > 0);
                }
            }
        }
    }

    @Test
    void reactionStrengthFallsFromOneToZero() {
        assertEquals(0.0, KidsArt.strength(-1));
        assertEquals(1.0, KidsArt.strength(0));
        assertTrue(KidsArt.strength(1.0) > 0 && KidsArt.strength(1.0) < 1);
        assertEquals(0.0, KidsArt.strength(KidsArt.REACTION_SECONDS + 1));
    }
}
