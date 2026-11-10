package com.booklibrary.ui;

import com.booklibrary.reader.speech.SpeechEngine;
import com.booklibrary.reader.speech.SpeechEngines;
import com.booklibrary.reader.speech.Voice;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

/**
 * A friendly assistant that teaches new users the app, step by step, with a speech bubble and a voice.
 *
 * Author: Farid Rahmani
 * Contribution: Designed the guided tour with the assistant character (code drafted with AI assistance).
 * Purpose: when the app is opened for the first time, the character appears in the corner and
 * explains one tab at a time. The character is an animation without sound: a sheet of pictures
 * (assistant_sheet.png) that is played while the voice speaks. The user presses "I understand" to go on; the assistant then
 * praises the user and shows the next tab. The voice uses the {@link SpeechEngine} of the
 * operating system (the same one as the reader), so it needs no internet and no key. If
 * speech is not available the tour still works with text only. The words are in {@link GuideSteps}.
 */
public class GuideAssistant {

    private static final String PREF_DISABLED = "guideDisabled";
    private static final int WORDS_PER_MINUTE = 175;

    /** The animation sheet: 79 pictures of 181 x 330 pixels, 10 in each row, played at 10 pictures a second. */
    private static final String SHEET = "/images/assistant_sheet.png";
    private static final int FRAME_WIDTH = 181;
    private static final int FRAME_HEIGHT = 330;
    private static final int COLUMNS = 10;
    private static final int FRAMES = 79;
    private static final int FRAME_MILLIS = 100;

    /** Voices of women, so the assistant sounds like the character. The first installed one in the engine's list is used. */
    private static final List<String> FEMALE_NAMES = List.of("Samantha", "Ava", "Allison", "Susan", "Zoe", "Karen",
            "Moira", "Tessa", "Serena", "Victoria", "Fiona", "Zira", "Hazel", "Jenny", "Aria", "Emma");

    private final TabPane tabs;
    private final List<GuideSteps.Step> steps = GuideSteps.all();
    private final Preferences preferences = Preferences.userNodeForPackage(GuideAssistant.class);

    private final HBox panel = new HBox(8);
    private final ImageView character = new ImageView();
    private final Label stepLabel = new Label();
    private final Label bubbleText = new Label();
    private final Button understandButton = new Button();
    private final Button skipButton = new Button("Skip tour");
    private final Button voiceButton = new Button("Voice: on");
    private final CheckBox dontShowAgain = new CheckBox("Do not show at startup");
    private final TranslateTransition bob;
    private final Timeline talk;
    private int position;
    private boolean stopRequested;

    private final ExecutorService speaker = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "guide-speech");
        thread.setDaemon(true);
        return thread;
    });
    private volatile Process currentSpeech;
    private Voice voice;
    private boolean voiceChosen;
    private boolean voiceOn = true;
    private int index;

    /**
     * Builds the assistant and adds it (hidden) and a small Guide button to the window.
     *
     * @param tabs the tabs of the main window, which the tour switches between
     * @param host the stack that holds the main window; the assistant is drawn on top of it
     * @throws IllegalStateException if the picture of the character is missing
     */
    public GuideAssistant(TabPane tabs, StackPane host) {
        this.tabs = tabs;

        InputStream stream = GuideAssistant.class.getResourceAsStream(SHEET);
        if (stream == null) {
            throw new IllegalStateException("assistant_sheet.png not found");
        }
        character.setImage(new Image(stream));
        showFrame(FRAMES - 1);
        character.setFitHeight(260);
        character.setPreserveRatio(true);
        character.setSmooth(true);

        talk = new Timeline(new KeyFrame(Duration.millis(FRAME_MILLIS), event -> nextFrame()));
        talk.setCycleCount(Animation.INDEFINITE);

        bob = new TranslateTransition(Duration.millis(1300), character);
        bob.setByY(-7);
        bob.setCycleCount(Animation.INDEFINITE);
        bob.setAutoReverse(true);

        stepLabel.setStyle("-fx-text-fill: #c9a96e; -fx-font-size: 11px; -fx-font-weight: bold;");
        bubbleText.setWrapText(true);
        bubbleText.setStyle("-fx-text-fill: #f2efe8; -fx-font-size: 14px;");
        understandButton.getStyleClass().add("primary-button");
        understandButton.setOnAction(event -> onUnderstand());
        skipButton.setOnAction(event -> close());
        voiceButton.setOnAction(event -> toggleVoice());
        dontShowAgain.setStyle("-fx-text-fill: #b8b4aa; -fx-font-size: 11px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox buttons = new HBox(8, understandButton, skipButton, spacer, voiceButton);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox bubble = new VBox(8, stepLabel, bubbleText, buttons, dontShowAgain);
        bubble.setPadding(new Insets(14));
        bubble.setPrefWidth(340);
        bubble.setMaxWidth(340);
        bubble.setMaxHeight(Region.USE_PREF_SIZE);
        bubble.setStyle("-fx-background-color: #1b1b24; -fx-background-radius: 14; "
                + "-fx-border-color: #c9a96e; -fx-border-radius: 14; -fx-border-width: 1.2;");

        panel.setAlignment(Pos.BOTTOM_RIGHT);
        panel.getChildren().addAll(bubble, character);
        panel.setPickOnBounds(false);
        panel.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        panel.setVisible(false);
        StackPane.setAlignment(panel, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(panel, new Insets(0, 14, 10, 0));

        Button guideButton = new Button("Guide");
        guideButton.setOnAction(event -> start());
        StackPane.setAlignment(guideButton, Pos.TOP_RIGHT);
        StackPane.setMargin(guideButton, new Insets(4, 8, 0, 0));

        host.getChildren().addAll(guideButton, panel);
    }

    /**
     * Starts the tour, unless the user has chosen not to see it at startup.
     */
    public void startIfEnabled() {
        if (!preferences.getBoolean(PREF_DISABLED, false)) {
            start();
        }
    }

    /**
     * Starts the tour from the first step.
     */
    public void start() {
        index = 0;
        panel.setVisible(true);
        bob.play();
        startTalking();
        showStep(null);
    }

    private void showStep(String praise) {
        GuideSteps.Step step = steps.get(index);
        stepLabel.setText("Step " + (index + 1) + " of " + steps.size());
        bubbleText.setText(step.text());
        boolean last = index == steps.size() - 1;
        understandButton.setText(last ? "Finish" : "I understand");
        skipButton.setVisible(!last);
        int tab = step.tabIndex();
        if (tab >= 0 && tab < tabs.getTabs().size()) {
            tabs.getSelectionModel().select(tab);
        }
        speak(praise == null ? step.text() : praise + " " + step.text());
    }

    private void onUnderstand() {
        if (index >= steps.size() - 1) {
            close();
            return;
        }
        index++;
        showStep(GuideSteps.praise(index));
    }

    /** Shows one picture of the sheet. */
    private void showFrame(int frame) {
        int column = frame % COLUMNS;
        int row = frame / COLUMNS;
        character.setViewport(new Rectangle2D(column * FRAME_WIDTH, row * FRAME_HEIGHT, FRAME_WIDTH, FRAME_HEIGHT));
    }

    /**
     * Plays the pictures forwards and then backwards, so the movement has no jump. The character stands
     * calmly in the last picture, and the movement starts and ends there.
     */
    private void nextFrame() {
        int cycle = 2 * (FRAMES - 1);
        position = (position + 1) % cycle;
        int frame = position <= FRAMES - 1 ? FRAMES - 1 - position : position - (FRAMES - 1);
        showFrame(frame);
    }

    private void startTalking() {
        stopRequested = false;
        if (talk.getStatus() != Animation.Status.RUNNING) {
            talk.play();
        }
    }

    /** Lets the movement finish its round and stand still. */
    private void stopTalking() {
        // The movement keeps looping, so the character stays alive and never freezes.
    }

    private void close() {
        stopSpeech();
        talk.stop();
        position = 0;
        showFrame(FRAMES - 1);
        bob.stop();
        panel.setVisible(false);
        preferences.putBoolean(PREF_DISABLED, dontShowAgain.isSelected());
    }

    private void toggleVoice() {
        voiceOn = !voiceOn;
        voiceButton.setText(voiceOn ? "Voice: on" : "Voice: off");
        if (voiceOn) {
            speak(steps.get(index).text());
        } else {
            stopSpeech();
            stopTalking();
        }
    }

    private void speak(String text) {
        if (!voiceOn) {
            return;
        }
        stopSpeech();
        speaker.submit(() -> {
            try {
                SpeechEngine engine = SpeechEngines.detect();
                if (!engine.isAvailable()) {
                    Platform.runLater(this::stopTalking);
                    return;
                }
                if (!voiceChosen) {
                    voice = chooseVoice(engine);
                    voiceChosen = true;
                }
                Process process = engine.speak(text, voice, WORDS_PER_MINUTE);
                currentSpeech = process;
                Platform.runLater(this::startTalking);
                process.onExit().thenRun(() -> Platform.runLater(() -> {
                    if (currentSpeech == process) {
                        stopTalking();
                    }
                }));
            } catch (IOException | RuntimeException e) {
                currentSpeech = null; // no voice: the tour continues with text only
                Platform.runLater(this::stopTalking);
            }
        });
    }

    private void stopSpeech() {
        Process process = currentSpeech;
        currentSpeech = null;
        if (process != null) {
            process.destroy();
        }
    }

    /**
     * Picks a woman's English voice. The speech engines list the best sounding voices first, so the first
     * English voice with a woman's name is used. If there is none, the first English voice is used.
     */
    private static Voice chooseVoice(SpeechEngine engine) {
        Voice firstEnglish = null;
        for (Voice candidate : engine.voices()) {
            if (!candidate.language().toLowerCase().startsWith("en")) {
                continue;
            }
            if (firstEnglish == null) {
                firstEnglish = candidate;
            }
            for (String name : FEMALE_NAMES) {
                if (candidate.name().contains(name)) {
                    return candidate;
                }
            }
        }
        return firstEnglish;
    }

    /** Makes sure the speaking thread does not keep running. */
    public void shutdown() {
        stopSpeech();
        speaker.shutdownNow();
    }
}
