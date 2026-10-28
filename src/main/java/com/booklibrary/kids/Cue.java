package com.booklibrary.kids;

/**
 * What happens when a certain word is read: one kind of character does an action.
 *
 * @param type   which characters do it
 * @param action what they do
 * @param moveX  how far they move sideways (fraction of the page width, negative is left)
 * @param moveY  how far they move up or down (fraction of the page height, negative is up)
 */
public record Cue(ActorType type, Action action, double moveX, double moveY) {

    /**
     * Makes a cue without movement.
     *
     * @param type   the characters
     * @param action the action
     * @return the cue
     */
    public static Cue of(ActorType type, Action action) {
        return new Cue(type, action, 0, 0);
    }

    /**
     * Makes a cue where the characters move sideways.
     *
     * @param type   the characters
     * @param action the action, usually WALK, SWIM or FLY
     * @param moveX  sideways distance
     * @return the cue
     */
    public static Cue to(ActorType type, Action action, double moveX) {
        return new Cue(type, action, moveX, 0);
    }

    /**
     * Makes a cue where the characters move sideways and up or down.
     *
     * @param type   the characters
     * @param action the action
     * @param moveX  sideways distance
     * @param moveY  vertical distance
     * @return the cue
     */
    public static Cue to(ActorType type, Action action, double moveX, double moveY) {
        return new Cue(type, action, moveX, moveY);
    }
}
