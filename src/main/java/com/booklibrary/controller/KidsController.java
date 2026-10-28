package com.booklibrary.controller;

import com.booklibrary.kids.KidsPage;
import com.booklibrary.kids.KidsStage;
import com.booklibrary.kids.KidsStories;
import com.booklibrary.kids.KidsStory;
import com.booklibrary.kids.StoryText;
import com.booklibrary.reader.ReadAloud;
import com.booklibrary.reader.speech.SpeechEngine;
import com.booklibrary.reader.speech.SpeechEngines;
import com.booklibrary.reader.speech.Voice;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;

/**
 * Controller of the "Kids" tab: animated children's stories that can be read aloud.
 *
 * Author: Farid Rahmani
 * Contribution: Connected the story pages, the animated stage, the read-aloud player and the settings.
 * Purpose: while the voice reads, the sentence and word are highlighted and the characters
 * act out their words (walk, swim, fly, dance, sing, sleep). Animations can be switched off, and the settings are saved.
 */
public class KidsController {

    @FXML private ComboBox<KidsStory> storyBox;
    @FXML private CheckBox animBox;
    @FXML private Slider textSlider;
    @FXML private Label summaryLabel;
    @FXML private KidsStage stage;
    @FXML private StoryText storyText;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Button readButton;
    @FXML private Button stopButton;
    @FXML private Label pageLabel;
    @FXML private ComboBox<Voice> voiceBox;
    @FXML private Slider speedSlider;
    @FXML private Label speedLabel;
    @FXML private Label infoLabel;

    private final Preferences prefs = Preferences.userNodeForPackage(KidsController.class);
    private final SpeechEngine speech = SpeechEngines.detect();
    private ReadAloud readAloud;
    private KidsStory story;
    private int pageIndex;
    private boolean readerTurnedPage;
    private int sentenceStart;
    private int sentenceEnd;

    /** Called by JavaFX after the FXML file is loaded. */
    @FXML
    public void initialize() {
        storyBox.setItems(FXCollections.observableArrayList(KidsStories.all()));

        animBox.setSelected(prefs.getBoolean("animated", true));
        stage.setAnimated(animBox.isSelected());
        animBox.selectedProperty().addListener((obs, oldValue, value) -> {
            stage.setAnimated(value);
            prefs.putBoolean("animated", value);
        });

        textSlider.setValue(prefs.getDouble("textSize", 28));
        textSlider.valueProperty().addListener((obs, oldValue, value) -> {
            storyText.setFontSize(value.doubleValue());
            prefs.putDouble("textSize", value.doubleValue());
        });

        setupReadAloud();

        storyBox.valueProperty().addListener((obs, oldValue, value) -> {
            if (value != null) {
                openStory(value);
            }
        });
        storyBox.getSelectionModel().selectFirst();
    }

    /**
     * Called when the tab is shown or hidden.
     *
     * @param active {@code true} when the Kids tab is visible
     */
    public void setActive(boolean active) {
        stage.setActive(active);
        if (!active && readAloud.getState() == ReadAloud.State.PLAYING) {
            readAloud.pause();
        }
    }

    // ------------------------------------------------------------------ pages

    private void openStory(KidsStory value) {
        readAloud.stop();
        story = value;
        pageIndex = 0;
        summaryLabel.setText(value.summary());
        showPage(false);
    }

    private KidsPage page() {
        return story.pages().get(pageIndex);
    }

    private void showPage(boolean fade) {
        if (!readerTurnedPage) {
            readAloud.stop();
        }
        readerTurnedPage = false;
        KidsPage current = page();
        storyText.setStory(current.text(), textSlider.getValue());
        if (fade) {
            stage.fadeToPage(current);
        } else {
            stage.showPage(current);
        }
        pageLabel.setText("Page " + (pageIndex + 1) + " of " + story.pages().size());
        prevButton.setDisable(pageIndex == 0);
        nextButton.setDisable(pageIndex >= story.pages().size() - 1);
    }

    @FXML
    private void onNext() {
        if (pageIndex < story.pages().size() - 1) {
            pageIndex++;
            showPage(true);
        }
    }

    @FXML
    private void onPrevious() {
        if (pageIndex > 0) {
            pageIndex--;
            showPage(true);
        }
    }

    // ------------------------------------------------------------------ reading aloud

    private void setupReadAloud() {
        readAloud = new ReadAloud(speech, new ReadAloud.Listener() {
            @Override
            public void sentence(int index, int start, int end) {
                Platform.runLater(() -> {
                    sentenceStart = start;
                    sentenceEnd = end;
                    storyText.highlight(start, end, -1);
                });
            }

            @Override
            public void word(int start, int end) {
                Platform.runLater(() -> onWord(start, end));
            }

            @Override
            public void stateChanged(ReadAloud.State state) {
                Platform.runLater(() -> updateReadButton(state));
            }

            @Override
            public void finished() {
                Platform.runLater(KidsController.this::onFinished);
            }

            @Override
            public void failed(String message) {
                Platform.runLater(() -> infoLabel.setText(message));
            }
        });

        speedSlider.setValue(prefs.getInt("speed", 150));
        speedLabel.setText((int) speedSlider.getValue() + " words per minute");
        speedSlider.valueProperty().addListener((obs, oldValue, value) -> {
            speedLabel.setText(value.intValue() + " words per minute");
            prefs.putInt("speed", value.intValue());
        });
        speedSlider.valueChangingProperty().addListener((obs, was, changing) -> {
            if (!changing) {
                readAloud.setSpeed((int) speedSlider.getValue());
            }
        });
        voiceBox.valueProperty().addListener((obs, oldValue, voice) -> {
            if (voice != null) {
                prefs.put("voice", voice.name());
                readAloud.setVoice(voice);
            }
        });

        boolean available = speech.isAvailable();
        readButton.setDisable(!available);
        stopButton.setDisable(!available);
        voiceBox.setDisable(!available);
        speedSlider.setDisable(!available);
        if (!available) {
            infoLabel.setText("Reading aloud works on macOS and Windows. You can still read and tap the pictures.");
            return;
        }
        Task<List<Voice>> task = new Task<>() {
            @Override
            protected List<Voice> call() {
                return speech.voices();
            }
        };
        task.setOnSucceeded(event -> showVoices(task.getValue()));
        Thread thread = new Thread(task, "kids-voices");
        thread.setDaemon(true);
        thread.start();
    }

    private void showVoices(List<Voice> voices) {
        if (voices.isEmpty()) {
            return;
        }
        voiceBox.setItems(FXCollections.observableArrayList(voices));
        String saved = prefs.get("voice", "");
        Voice chosen = voices.stream().filter(v -> v.name().equals(saved)).findFirst()
                .orElseGet(() -> voices.stream().filter(v -> v.language().toLowerCase().startsWith("en"))
                        .findFirst().orElse(voices.get(0)));
        voiceBox.setValue(chosen);
    }

    private Locale locale() {
        Voice voice = voiceBox.getValue();
        return voice == null ? Locale.ENGLISH : Locale.forLanguageTag(voice.language().replace('_', '-'));
    }

    private void startReading() {
        String text = page().text();
        readAloud.start(text, ReadAloud.sentenceRanges(text, locale()), 0, voiceBox.getValue(),
                (int) speedSlider.getValue());
    }

    /** A word is being read: highlight it and let the matching character react. */
    private void onWord(int start, int end) {
        String text = page().text();
        if (readAloud.getState() != ReadAloud.State.PLAYING || end > text.length()) {
            return;
        }
        storyText.highlight(sentenceStart, sentenceEnd, start);
        stage.perform(page().cueFor(text.substring(start, end)));
    }

    private void updateReadButton(ReadAloud.State state) {
        switch (state) {
            case PLAYING -> readButton.setText("⏸ Pause");
            case PAUSED -> readButton.setText("▶ Continue");
            default -> {
                readButton.setText("▶ Read the story");
                storyText.clear();
            }
        }
    }

    /** The page was read to the end: turn to the next page and keep reading. */
    private void onFinished() {
        storyText.clear();
        if (pageIndex < story.pages().size() - 1) {
            readerTurnedPage = true;
            pageIndex++;
            showPage(true);
            startReading();
        } else {
            infoLabel.setText("The end! Press Read the story to hear it again from this page.");
        }
    }

    @FXML
    private void onReadToggle() {
        switch (readAloud.getState()) {
            case PLAYING -> readAloud.pause();
            case PAUSED -> readAloud.resume();
            default -> startReading();
        }
    }

    @FXML
    private void onStop() {
        readAloud.stop();
        storyText.clear();
    }
}
