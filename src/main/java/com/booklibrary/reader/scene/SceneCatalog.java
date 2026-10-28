package com.booklibrary.reader.scene;

import java.util.List;

/**
 * The list of views the reader can choose from.
 *
 * Author: Farid Rahmani
 * Contribution: Chose and made the reading scenes (AI videos and picture).
 * Purpose: each scene is a file in {@code src/main/resources/scenes/} with the
 * same name as its id. To add a scene, put a new mp4 there and add one line below.
 */
public final class SceneCatalog {

    private SceneCatalog() {
    }

    /**
     * Creates the list of scenes. The first one is the old library.
     *
     * @return a new list with all scenes
     */
    public static List<AmbientScene> all() {
        return List.of(
                new MediaScene("old-library", "Old library"),
                new MediaScene("cabin-fireplace", "Winter cabin by the fireplace"),
                new MediaScene("cabin-forest-view", "Cabin overlooking the forest"),
                new MediaScene("coastal-reading-room", "Coastal reading room"),
                new MediaScene("mountain-chalet", "Mountain chalet"),
                new MediaScene("desert-night", "Desert retreat at blue hour"),
                new MediaScene("ocean-beach", "Ocean waves on an empty beach"),
                new MediaScene("japanese-garden-rain", "Rain in a Zen garden"),
                new MediaScene("rainy-window-city", "Rain on a Tokyo apartment"),
                new MediaScene("night-train", "Vintage train at night"));
    }
}
