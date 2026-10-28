package com.booklibrary.reader.scene;

import javafx.scene.canvas.GraphicsContext;

/**
 * A moving background "view" the reader can choose, for example a snowy cabin or a mountain top.
 *
 * Author: Farid Rahmani
 * Contribution: Designed the scene system for the reading mode.
 */
public interface AmbientScene {

    /**
     * Returns the short identifier. A video named {@code /scenes/<id>.mp4} replaces the drawing.
     *
     * @return the id, lower case without spaces
     */
    String getId();

    /**
     * Returns the name shown to the reader.
     *
     * @return the display name
     */
    String getName();

    /**
     * Draws one frame.
     *
     * @param g       the graphics context to draw on
     * @param width   the width in pixels
     * @param height  the height in pixels
     * @param seconds the time since the scene started, in seconds
     */
    void draw(GraphicsContext g, double width, double height, double seconds);
}
