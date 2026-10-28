package com.booklibrary.reader;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;

/**
 * Plays the background music of the reading mode and fades between moods.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented the music that follows the story.
 * Purpose: keeps one looping track per mood. When the mood changes, the new
 * track fades in while the old one fades out, so the change is smooth.
 * All tracks are instrumental (no singing).
 */
public class MusicPlayer {

    private static final Duration FADE = Duration.seconds(3);

    private MediaPlayer current;
    private Timeline fadeIn;
    private Mood mood;
    private double volume = 0.5;
    private boolean muted;
    private boolean active = true;

    /**
     * Starts the music for a mood, fading from the previous one. Does nothing if that mood already plays.
     *
     * @param newMood the mood to play
     */
    public void play(Mood newMood) {
        if (newMood == mood && current != null) {
            return;
        }
        mood = newMood;
        if (!active) {
            return;
        }
        startTrack(newMood);
    }

    private void startTrack(Mood newMood) {
        URL url = getClass().getResource(newMood.resourcePath());
        if (url == null) {
            System.err.println("Music file missing: " + newMood.resourcePath());
            return;
        }
        MediaPlayer old = current;
        MediaPlayer next = createPlayer(url);
        if (next == null) {
            return;
        }
        next.setCycleCount(MediaPlayer.INDEFINITE);
        next.setVolume(0);
        next.setOnError(() -> System.err.println("Music error: " + next.getError()));
        next.play();
        current = next;

        if (fadeIn != null) {
            fadeIn.stop();
        }
        fadeIn = new Timeline(new KeyFrame(FADE, new KeyValue(next.volumeProperty(), targetVolume())));
        fadeIn.play();

        if (old != null) {
            Timeline fadeOut = new Timeline(new KeyFrame(FADE, new KeyValue(old.volumeProperty(), 0)));
            fadeOut.setOnFinished(e -> old.dispose());
            fadeOut.play();
        }
    }

    private static MediaPlayer createPlayer(URL url) {
        try {
            return new MediaPlayer(new Media(url.toExternalForm()));
        } catch (RuntimeException e) {
            System.err.println("Could not play " + url + ": " + e);
            return null;
        }
    }

    private double targetVolume() {
        return muted ? 0 : volume;
    }

    /**
     * Sets the volume.
     *
     * @param value between 0 (silent) and 1 (full)
     */
    public void setVolume(double value) {
        volume = Math.max(0, Math.min(1, value));
        applyVolume();
    }

    /**
     * Mutes or unmutes the music.
     *
     * @param value {@code true} to mute
     */
    public void setMuted(boolean value) {
        muted = value;
        applyVolume();
    }

    private void applyVolume() {
        if (current != null) {
            if (fadeIn != null) {
                fadeIn.stop();
            }
            current.setVolume(targetVolume());
        }
    }

    /**
     * Pauses the music while the reading tab is hidden and resumes it when it is shown again.
     *
     * @param value {@code true} to play, {@code false} to pause
     */
    public void setActive(boolean value) {
        active = value;
        if (current == null) {
            if (value && mood != null) {
                startTrack(mood);
            }
            return;
        }
        if (value) {
            current.play();
        } else {
            current.pause();
        }
    }

    /** Stops the music and frees the resources. */
    public void stop() {
        if (fadeIn != null) {
            fadeIn.stop();
        }
        if (current != null) {
            current.dispose();
            current = null;
        }
        mood = null;
    }
}
