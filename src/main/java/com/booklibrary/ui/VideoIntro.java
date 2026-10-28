package com.booklibrary.ui;

import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

/**
 * Plays a video file as the intro before the main window opens.
 *
 * Author: Farid Rahmani
 * Contribution: Added support for a custom (AI-made) intro video.
 * Purpose: shows the video full screen on a black background and calls
 * {@code onFinished} when it ends, is skipped, or cannot be played.
 */
public class VideoIntro {

    private final StackPane root = new StackPane();
    private final MediaPlayer player;
    private final Runnable onFinished;
    private boolean finished;

    /**
     * Prepares the video.
     *
     * @param videoUrl   the address of the video file (for example from {@code getResource})
     * @param onFinished the action to run when the video has ended, been skipped or failed
     */
    public VideoIntro(String videoUrl, Runnable onFinished) {
        this.onFinished = onFinished;
        root.setStyle("-fx-background-color: black;");

        player = new MediaPlayer(new Media(videoUrl));
        MediaView view = new MediaView(player);
        view.setPreserveRatio(true);
        view.fitWidthProperty().bind(root.widthProperty());
        view.fitHeightProperty().bind(root.heightProperty());
        root.getChildren().add(view);

        player.setOnEndOfMedia(this::finish);
        player.setOnError(this::finish);
    }

    /**
     * Returns the visual content to put in a scene.
     *
     * @return the root node
     */
    public Parent getRoot() {
        return root;
    }

    /**
     * Starts playing the video.
     */
    public void play() {
        player.play();
    }

    /**
     * Stops the video and continues to the application.
     */
    public void skip() {
        finish();
    }

    private void finish() {
        if (finished) {
            return;
        }
        finished = true;
        player.stop();
        player.dispose();
        onFinished.run();
    }
}
