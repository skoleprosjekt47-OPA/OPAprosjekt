package com.booklibrary.reader.scene;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * A reading view that is shown as a video or picture from {@code src/main/resources/scenes/<id>.mp4} (or .jpg/.png).
 *
 * Author: Farid Rahmani
 * Contribution: Added the scenes made with AI video for the reading mode.
 * Purpose: {@link SceneView} plays the media file. This class only gives the
 * scene its id and name, and draws a dark background if the file cannot be shown.
 */
public final class MediaScene implements AmbientScene {

    private final String id;
    private final String name;

    /**
     * Creates a scene.
     *
     * @param id   the file name without extension
     * @param name the name shown to the reader
     */
    public MediaScene(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void draw(GraphicsContext g, double width, double height, double seconds) {
        g.setFill(Color.web("#0b0b0f"));
        g.fillRect(0, 0, width, height);
    }

    @Override
    public String toString() {
        return name;
    }
}
