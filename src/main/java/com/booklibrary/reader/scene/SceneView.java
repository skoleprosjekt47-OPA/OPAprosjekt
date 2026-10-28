package com.booklibrary.reader.scene;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;

import java.net.URL;

/**
 * The area that shows the animated background of the reading mode.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented the animation loop and the option to use real videos as scenes.
 * Purpose: runs the drawing of an {@link AmbientScene} about 30 times per
 * second. If a video called {@code /scenes/<id>.mp4} exists, the video is
 * played in a loop instead of the drawing.
 */
public class SceneView extends StackPane {

    private static final long FRAME_NANOS = 33_000_000L;

    private final Canvas canvas = new Canvas();
    private final AnimationTimer timer;
    private AmbientScene scene;
    private MediaPlayer video;
    private MediaView videoView;
    private ImageView imageView;
    private long startNanos;
    private long lastNanos;
    private boolean failed;

    /** Creates an empty view. Call {@link #setScene(AmbientScene)} and {@link #start()}. */
    public SceneView() {
        setMinSize(0, 0);
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        getChildren().add(canvas);
        setStyle("-fx-background-color: #0b0b0f;");
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (startNanos == 0) {
                    startNanos = now;
                }
                if (now - lastNanos < FRAME_NANOS) {
                    return;
                }
                lastNanos = now;
                render((now - startNanos) / 1_000_000_000.0);
            }
        };
    }

    /**
     * Changes the scene that is shown.
     *
     * @param newScene the scene to show
     */
    public void setScene(AmbientScene newScene) {
        this.scene = newScene;
        this.failed = false;
        this.startNanos = 0;
        disposeVideo();
        URL picture = findPicture(newScene.getId());
        if (picture != null) {
            imageView = new ImageView(new Image(picture.toExternalForm()));
            imageView.setPreserveRatio(false);
            imageView.fitWidthProperty().bind(widthProperty());
            imageView.fitHeightProperty().bind(heightProperty());
            getChildren().add(0, imageView);
            canvas.setVisible(false);
            return;
        }
        URL url = getClass().getResource("/scenes/" + newScene.getId() + ".mp4");
        if (url != null) {
            try {
                video = new MediaPlayer(new Media(url.toExternalForm()));
                video.setCycleCount(MediaPlayer.INDEFINITE);
                video.setMute(true);
                video.setOnError(this::disposeVideo);
                videoView = new MediaView(video);
                videoView.setPreserveRatio(false);
                videoView.fitWidthProperty().bind(widthProperty());
                videoView.fitHeightProperty().bind(heightProperty());
                getChildren().add(0, videoView);
                canvas.setVisible(false);
                video.play();
            } catch (RuntimeException e) {
                disposeVideo();
            }
        }
    }

    /** Starts the animation. */
    public void start() {
        timer.start();
        if (video != null) {
            video.play();
        }
    }

    /** Pauses the animation, for example when the reading tab is not visible. */
    public void stop() {
        timer.stop();
        if (video != null) {
            video.pause();
        }
    }

    private URL findPicture(String id) {
        URL jpg = getClass().getResource("/scenes/" + id + ".jpg");
        return jpg != null ? jpg : getClass().getResource("/scenes/" + id + ".png");
    }

    private void disposeVideo() {
        if (imageView != null) {
            getChildren().remove(imageView);
            imageView = null;
        }
        if (video != null) {
            video.dispose();
            video = null;
        }
        if (videoView != null) {
            getChildren().remove(videoView);
            videoView = null;
        }
        canvas.setVisible(true);
    }

    private void render(double seconds) {
        if (scene == null || video != null || imageView != null || getWidth() < 2 || getHeight() < 2) {
            return;
        }
        GraphicsContext g = canvas.getGraphicsContext2D();
        try {
            if (!failed) {
                scene.draw(g, getWidth(), getHeight(), seconds);
                return;
            }
        } catch (RuntimeException e) {
            failed = true;
            System.err.println("Scene " + scene.getId() + " failed: " + e);
        }
        g.setFill(Color.web("#0b0b0f"));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}
