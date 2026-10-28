package com.booklibrary.kids;

/**
 * An actor placed on a page.
 *
 * @param type  what it is
 * @param x     horizontal place, 0 (left) to 1 (right)
 * @param y     vertical place of its feet, 0 (top) to 1 (bottom)
 * @param scale size, 1 is normal
 */
public record Placed(ActorType type, double x, double y, double scale) {
}
