package com.booklibrary.kids;

/**
 * Something a character can do when the story says it.
 *
 * Author: Farid Rahmani
 * Contribution: Defined the actions that connect the story text to real movement.
 */
public enum Action {
    /** The character's own special reaction (rain from a cloud, bubbles from the fish, glitter from stars). */
    REACT(1.8),
    /** Jump up and land again. */
    HOP(1.0),
    /** Walk, run or crawl to another place on the page. */
    WALK(2.0),
    /** Swim to another place on the page. */
    SWIM(2.2),
    /** Fly to another place on the page. */
    FLY(2.4),
    /** Spin around. */
    SPIN(1.2),
    /** Dance with the whole body. */
    DANCE(2.6),
    /** Wave hello. */
    WAVE(1.6),
    /** Shake or shiver. */
    SHAKE(1.0),
    /** Grow bigger for a moment. */
    GROW(1.2),
    /** Sing, with music notes floating up. */
    SING(2.6),
    /** Fall asleep and stay asleep on this page. */
    SLEEP(0.0);

    private final double seconds;

    Action(double seconds) {
        this.seconds = seconds;
    }

    /**
     * Returns how long the action lasts.
     *
     * @return seconds; 0 for an action that never ends
     */
    public double seconds() {
        return seconds;
    }
}
