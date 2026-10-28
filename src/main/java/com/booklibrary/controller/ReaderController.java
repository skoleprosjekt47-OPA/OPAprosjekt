package com.booklibrary.controller;

import com.booklibrary.model.CatalogBook;
import com.booklibrary.reader.Annotation;
import com.booklibrary.reader.AnnotationManager;
import com.booklibrary.reader.BookType;
import com.booklibrary.reader.ContrastMode;
import com.booklibrary.reader.DemoBook;
import com.booklibrary.reader.GutenbergBook;
import com.booklibrary.reader.GutenbergClient;
import com.booklibrary.reader.JsonAnnotationStore;
import com.booklibrary.reader.Mood;
import com.booklibrary.reader.MoodAnalyzer;
import com.booklibrary.reader.MusicPlayer;
import com.booklibrary.reader.Paginator;
import com.booklibrary.reader.ReadAloud;
import com.booklibrary.reader.ReaderSettings;
import com.booklibrary.reader.scene.AmbientScene;
import com.booklibrary.reader.scene.SceneCatalog;
import com.booklibrary.reader.scene.SceneView;
import com.booklibrary.reader.speech.SpeechEngine;
import com.booklibrary.reader.speech.SpeechEngines;
import com.booklibrary.reader.speech.Voice;
import com.booklibrary.translate.BookTranslator;
import com.booklibrary.translate.JdkJsonPoster;
import com.booklibrary.translate.Language;
import com.booklibrary.translate.Languages;
import com.booklibrary.translate.TranslationProvider;
import com.booklibrary.translate.TranslationProviders;
import com.booklibrary.translate.TranslationStore;
import com.booklibrary.ui.AnnotatedPageView;
import com.booklibrary.ui.HoverPreview;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Controller for the "Reading" tab: pick a book, a view and read page by page with music that follows the story.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented the reading mode (book search, pages, views and mood music).
 * Purpose: connects the screen (reader.fxml) with the logic in the
 * {@code com.booklibrary.reader} package. Searching and downloading run on
 * background threads so the window never freezes.
 */
public class ReaderController {

    private static final int PAGE_SIZE = 620;

    @FXML private Region leftPanel;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private ListView<GutenbergBook> resultList;
    @FXML private Label statusLabel;
    @FXML private ComboBox<AmbientScene> sceneBox;
    @FXML private ComboBox<BookType> typeBox;
    @FXML private Slider volumeSlider;
    @FXML private CheckBox muteBox;
    @FXML private CheckBox focusBox;
    @FXML private SceneView sceneView;
    @FXML private StackPane readerPane;
    @FXML private Label bookTitleLabel;
    @FXML private AnnotatedPageView pageView;
    @FXML private ListView<Annotation> notesList;
    @FXML private Label pageInfo;
    @FXML private Label moodLabel;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Region dimLayer;
    @FXML private ComboBox<Voice> voiceBox;
    @FXML private Slider speedSlider;
    @FXML private Label speedLabel;
    @FXML private Label speechInfoLabel;
    @FXML private ComboBox<String> fontBox;
    @FXML private Slider sizeSlider;
    @FXML private Slider spacingSlider;
    @FXML private Slider darknessSlider;
    @FXML private CheckBox boldBox;
    @FXML private ComboBox<ContrastMode> contrastBox;
    @FXML private Button readButton;
    @FXML private Button prevSentenceButton;
    @FXML private Button nextSentenceButton;
    @FXML private Button stopButton;
    @FXML private Label translateInfoLabel;
    @FXML private ComboBox<TranslationProvider> providerBox;
    @FXML private ComboBox<Language> languageBox;
    @FXML private Button translateButton;
    @FXML private ProgressBar translateProgress;
    @FXML private Button cancelTranslateButton;
    @FXML private Button viewTranslationButton;
    @FXML private Label translateStatusLabel;

    private final GutenbergClient gutenberg = new GutenbergClient();
    private final MusicPlayer music = new MusicPlayer();
    private List<String> pages = List.of();
    private int pageIndex;
    private MoodAnalyzer analyzer = new MoodAnalyzer(BookType.GENERAL_FICTION);
    private final TranslationStore translations = TranslationStore.atDefaultLocation();
    private String originalTitle;
    private String originalText;
    private String originalKey;
    private BookType originalType = BookType.GENERAL_FICTION;
    private boolean showingTranslation;
    private AtomicBoolean translateCancel;
    private final ReaderSettings settings = ReaderSettings.forUser();
    private final SpeechEngine speech = SpeechEngines.detect();
    private ReadAloud readAloud;
    private boolean loadingSettings;
    private boolean selectionOnly;
    private boolean readerTurnedPage;
    private int readSentenceStart = -1;
    private int readSentenceEnd = -1;
    private final AnnotationManager annotations = new AnnotationManager(JsonAnnotationStore.atDefaultLocation());

    /**
     * Called by JavaFX after the FXML has been loaded. Fills the lists and shows the demo story.
     */
    @FXML
    private void initialize() {
        music.setActive(false); // nothing plays until the reading tab is opened
        sceneBox.setItems(FXCollections.observableArrayList(SceneCatalog.all()));
        sceneBox.getSelectionModel().selectFirst();
        sceneView.setScene(sceneBox.getValue());
        sceneBox.valueProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                sceneView.setScene(newScene);
            }
        });

        typeBox.setItems(FXCollections.observableArrayList(BookType.values()));
        typeBox.setValue(BookType.GENERAL_FICTION);
        typeBox.valueProperty().addListener((obs, oldType, newType) -> {
            if (newType != null && !pages.isEmpty()) {
                analyzer = new MoodAnalyzer(newType);
                showPage();
            }
        });

        volumeSlider.valueProperty().addListener((obs, oldValue, newValue) -> music.setVolume(newValue.doubleValue()));
        muteBox.selectedProperty().addListener((obs, oldValue, muted) -> music.setMuted(muted));
        focusBox.selectedProperty().addListener((obs, oldValue, focus) -> {
            leftPanel.setVisible(!focus);
            leftPanel.setManaged(!focus);
        });
        music.setVolume(volumeSlider.getValue());

        readerPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.SPACE) {
                onNext();
            } else if (event.getCode() == KeyCode.LEFT) {
                onPrevious();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                focusBox.setSelected(false);
            }
        });
        readerPane.setOnMouseClicked(event -> readerPane.requestFocus());

        HoverPreview<GutenbergBook> preview = new HoverPreview<>(book -> new HoverPreview.Data(
                book.title(),
                book.author(),
                book.subjects().isEmpty()
                        ? "A free book from Project Gutenberg."
                        : "Subjects: " + String.join("; ", book.subjects().subList(0, Math.min(4, book.subjects().size()))),
                "https://www.gutenberg.org/cache/epub/" + book.id() + "/pg" + book.id() + ".cover.medium.jpg"),
                book -> String.valueOf(book.id()));
        resultList.setCellFactory(list -> {
            ListCell<GutenbergBook> cell = new ListCell<>() {
                @Override
                protected void updateItem(GutenbergBook item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.toString());
                }
            };
            preview.attach(cell, cell::getItem);
            return cell;
        });
        resultList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                onOpenSelected();
            }
        });

        setupComfort();
        setupReadAloud();
        setupTranslation();
        pageView.setManager(annotations);
        pageView.setOnChanged(this::refreshNotes);
        notesList.setPlaceholder(new Label("Select text in the book to highlight or comment on it."));
        notesList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Annotation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                String content = item.hasComment() ? "\u270E " + item.getComment() : item.getText();
                setText("p. " + (item.getPage() + 1) + "  " + shorten(content, 60));
                String color = item.getColor() == null ? "#9a927f" : item.getColor().getHex();
                setStyle("-fx-border-color: transparent transparent transparent " + color + "; -fx-border-width: 0 0 0 4;");
            }
        });
        notesList.setOnMouseClicked(event -> {
            Annotation selected = notesList.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 2 && selected != null && selected.getPage() < pages.size()) {
                pageIndex = selected.getPage();
                showPage();
            }
        });

        openText(DemoBook.TITLE, DemoBook.text(), BookType.GENERAL_FICTION, "demo");
    }

    // ------------------------------------------------------------------ text and comfort

    private void setupComfort() {
        loadingSettings = true;
        List<String> wanted = List.of("Georgia", "Helvetica Neue", "Arial", "Verdana", "Tahoma", "Trebuchet MS",
                "Comic Sans MS", "Atkinson Hyperlegible", "OpenDyslexic", "Lexend", "Times New Roman");
        List<String> installed = Font.getFamilies();
        List<String> choices = new ArrayList<>(wanted.stream().filter(installed::contains).toList());
        if (choices.isEmpty()) {
            choices.add(Font.getDefault().getFamily());
        }
        fontBox.setItems(FXCollections.observableArrayList(choices));
        fontBox.setValue(choices.contains(settings.getFontFamily()) ? settings.getFontFamily() : choices.get(0));
        sizeSlider.setValue(settings.getFontSize());
        spacingSlider.setValue(settings.getLineSpacing());
        boldBox.setSelected(settings.isBold());
        contrastBox.setItems(FXCollections.observableArrayList(ContrastMode.values()));
        contrastBox.setValue(settings.getContrast());
        darknessSlider.setValue(settings.getDarkness());
        loadingSettings = false;

        fontBox.valueProperty().addListener((obs, oldValue, newValue) -> applyComfort());
        sizeSlider.valueProperty().addListener((obs, oldValue, newValue) -> applyComfort());
        spacingSlider.valueProperty().addListener((obs, oldValue, newValue) -> applyComfort());
        boldBox.selectedProperty().addListener((obs, oldValue, newValue) -> applyComfort());
        darknessSlider.valueProperty().addListener((obs, oldValue, newValue) -> applyComfort());
        contrastBox.valueProperty().addListener((obs, oldValue, mode) -> {
            if (mode != null && !loadingSettings) {
                darknessSlider.setValue(mode.getDarkness());
            }
            applyComfort();
        });
        applyComfort();
    }

    /** Applies the comfort settings to the page and saves them. */
    private void applyComfort() {
        if (loadingSettings || fontBox.getValue() == null || contrastBox.getValue() == null) {
            return;
        }
        pageView.setTextStyle(fontBox.getValue(), sizeSlider.getValue(), spacingSlider.getValue(),
                boldBox.isSelected(), Color.web(contrastBox.getValue().getTextHex()));
        dimLayer.setOpacity(darknessSlider.getValue());
        settings.setFontFamily(fontBox.getValue());
        settings.setFontSize(sizeSlider.getValue());
        settings.setLineSpacing(spacingSlider.getValue());
        settings.setBold(boldBox.isSelected());
        settings.setContrast(contrastBox.getValue());
        settings.setDarkness(darknessSlider.getValue());
    }

    // ------------------------------------------------------------------ read aloud

    private void setupReadAloud() {
        readAloud = new ReadAloud(speech, new ReadAloud.Listener() {
            @Override
            public void sentence(int index, int start, int end) {
                Platform.runLater(() -> {
                    readSentenceStart = start;
                    readSentenceEnd = end;
                    pageView.setReadingRange(start, end, -1, -1);
                });
            }

            @Override
            public void word(int start, int end) {
                Platform.runLater(() -> pageView.setReadingRange(readSentenceStart, readSentenceEnd, start, end));
            }

            @Override
            public void stateChanged(ReadAloud.State state) {
                Platform.runLater(() -> updateReadButton(state));
            }

            @Override
            public void finished() {
                Platform.runLater(ReaderController.this::onReadingFinished);
            }

            @Override
            public void failed(String message) {
                Platform.runLater(() -> {
                    statusLabel.setText(message);
                    pageView.clearReading();
                });
            }
        });

        speedSlider.setValue(settings.getSpeed());
        speedLabel.setText((int) speedSlider.getValue() + " words per minute");
        speedSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            speedLabel.setText(newValue.intValue() + " words per minute");
            settings.setSpeed(newValue.intValue());
            if (!speedSlider.isValueChanging()) {
                readAloud.setSpeed(newValue.intValue());
            }
        });
        speedSlider.valueChangingProperty().addListener((obs, was, changing) -> {
            if (!changing) {
                readAloud.setSpeed((int) speedSlider.getValue());
            }
        });
        voiceBox.valueProperty().addListener((obs, oldVoice, newVoice) -> {
            if (newVoice != null) {
                settings.setVoiceName(newVoice.name());
                readAloud.setVoice(newVoice);
            }
        });
        pageView.setOnReadSelection(this::readSelection);

        boolean available = speech.isAvailable();
        speechInfoLabel.setText(available
                ? "Using " + speech.getName() + ". Loading voices..."
                : "Reading aloud works on macOS and Windows. It is not available on this computer.");
        setReadControlsDisabled(!available);
        if (!available) {
            return;
        }
        Task<List<Voice>> task = new Task<>() {
            @Override
            protected List<Voice> call() {
                return speech.voices();
            }
        };
        task.setOnSucceeded(event -> showVoices(task.getValue()));
        task.setOnFailed(event -> showVoices(List.of()));
        startDaemon(task);
    }

    private void showVoices(List<Voice> voices) {
        if (voices.isEmpty()) {
            speechInfoLabel.setText("No voices were found. The system voice will be used.");
            return;
        }
        speechInfoLabel.setText("Using " + speech.getName() + ". Better voices can be added in the system settings.");
        voiceBox.setItems(FXCollections.observableArrayList(voices));
        Voice chosen = voices.stream().filter(v -> v.name().equals(settings.getVoiceName())).findFirst()
                .orElseGet(() -> voices.stream().filter(v -> v.language().toLowerCase().startsWith("en"))
                        .findFirst().orElse(voices.get(0)));
        voiceBox.setValue(chosen);
    }

    private void setReadControlsDisabled(boolean disabled) {
        readButton.setDisable(disabled);
        prevSentenceButton.setDisable(disabled);
        nextSentenceButton.setDisable(disabled);
        stopButton.setDisable(disabled);
        voiceBox.setDisable(disabled);
        speedSlider.setDisable(disabled);
    }

    private void updateReadButton(ReadAloud.State state) {
        switch (state) {
            case PLAYING -> readButton.setText("\u23F8 Pause");
            case PAUSED -> readButton.setText("\u25B6 Resume");
            default -> {
                readButton.setText("\u25B6 Read aloud");
                pageView.clearReading();
            }
        }
    }

    private Locale readingLocale() {
        Voice voice = voiceBox.getValue();
        return voice == null ? Locale.ENGLISH : Locale.forLanguageTag(voice.language().replace('_', '-'));
    }

    /** Starts reading the current page from a sentence. */
    private void startReading(int sentenceIndex) {
        String text = pageView.getText();
        selectionOnly = false;
        readAloud.start(text, ReadAloud.sentenceRanges(text, readingLocale()), sentenceIndex,
                voiceBox.getValue(), (int) speedSlider.getValue());
    }

    /** Reads only the selected text. */
    private void readSelection(int from, int to) {
        String text = pageView.getText();
        List<int[]> clipped = new ArrayList<>();
        for (int[] range : ReadAloud.sentenceRanges(text, readingLocale())) {
            int start = Math.max(range[0], from);
            int end = Math.min(range[1], to);
            if (end > start && !text.substring(start, end).isBlank()) {
                clipped.add(new int[]{start, end});
            }
        }
        if (clipped.isEmpty()) {
            return;
        }
        selectionOnly = true;
        readAloud.start(text, clipped, 0, voiceBox.getValue(), (int) speedSlider.getValue());
    }

    /** Called when the whole page has been read: continues on the next page. */
    private void onReadingFinished() {
        pageView.clearReading();
        if (!selectionOnly && pageIndex < pages.size() - 1) {
            readerTurnedPage = true;
            pageIndex++;
            showPage();
            startReading(0);
        }
    }

    @FXML
    private void onReadToggle() {
        switch (readAloud.getState()) {
            case PLAYING -> readAloud.pause();
            case PAUSED -> readAloud.resume();
            default -> startReading(0);
        }
    }

    @FXML
    private void onStopReading() {
        readAloud.stop();
        pageView.clearReading();
    }

    @FXML
    private void onNextSentence() {
        if (readAloud.getState() == ReadAloud.State.IDLE) {
            startReading(0);
            return;
        }
        int next = readAloud.getCurrent() + 1;
        if (next < readAloud.getSentenceCount()) {
            readAloud.jumpTo(next);
        } else if (!selectionOnly && pageIndex < pages.size() - 1) {
            boolean wasPlaying = readAloud.getState() == ReadAloud.State.PLAYING;
            readerTurnedPage = true;
            pageIndex++;
            showPage();
            if (wasPlaying) {
                startReading(0);
            }
        }
    }

    @FXML
    private void onPreviousSentence() {
        if (readAloud.getState() == ReadAloud.State.IDLE) {
            return;
        }
        int previous = readAloud.getCurrent() - 1;
        if (previous >= 0) {
            readAloud.jumpTo(previous);
        } else if (!selectionOnly && pageIndex > 0) {
            boolean wasPlaying = readAloud.getState() == ReadAloud.State.PLAYING;
            readerTurnedPage = true;
            pageIndex--;
            showPage();
            if (wasPlaying) {
                int last = ReadAloud.sentenceRanges(pageView.getText(), readingLocale()).size() - 1;
                startReading(Math.max(0, last));
            }
        }
    }


    // ------------------------------------------------------------------ translate the whole book

    private void setupTranslation() {
        List<TranslationProvider> usable = TranslationProviders.configured(System::getenv, new JdkJsonPoster());
        providerBox.setItems(FXCollections.observableArrayList(usable));
        languageBox.setItems(FXCollections.observableArrayList(Languages.all()));
        languageBox.setValue(Languages.byCode("nb"));
        languageBox.valueProperty().addListener((obs, oldValue, value) -> updateTranslationAvailability());
        providerBox.valueProperty().addListener((obs, oldValue, value) -> updateTranslationAvailability());
        if (usable.isEmpty()) {
            translateInfoLabel.setText("No translation service is set up yet. Set one of these environment variables "
                    + "and start the app again: GEMINI_API_KEY (AI, has a free plan), ANTHROPIC_API_KEY (AI, paid), "
                    + "DEEPL_API_KEY, or LIBRETRANSLATE_URL. The app never saves your key.");
            providerBox.setDisable(true);
            translateButton.setDisable(true);
        } else {
            providerBox.setValue(usable.get(0));
            translateInfoLabel.setText("The text of the book is sent over the internet to the service you choose. "
                    + "Finished parts are saved on this computer, so you can stop and continue.");
        }
        updateTranslationAvailability();
    }

    /** Enables the buttons that make sense for the open book. */
    private void updateTranslationAvailability() {
        Language language = languageBox.getValue();
        TranslationProvider provider = providerBox.getValue();
        boolean haveBook = originalText != null;
        boolean busy = cancelTranslateButton.isVisible();
        if (provider != null && language != null && !provider.supports(language) && !busy) {
            // pick another set up service that can translate into this language, for example AI for Pashto
            for (TranslationProvider other : providerBox.getItems()) {
                if (other.supports(language)) {
                    providerBox.setValue(other);
                    return;
                }
            }
        }
        if (provider != null) {
            boolean supported = language != null && provider.supports(language);
            translateButton.setDisable(!haveBook || !supported || busy);
            if (language != null && !supported) {
                translateStatusLabel.setText(provider.displayName() + " does not offer " + language.name()
                        + ". Choose another service or language.");
            } else if (!busy && !haveBook) {
                translateStatusLabel.setText("Open a book first.");
            } else if (!busy && language != null && language.lowResource()) {
                translateStatusLabel.setText("Few texts exist in " + language.name() + ", so the translation can be "
                        + "weaker here. Try one chapter first and let someone who speaks the language check it.");
            } else if (!busy) {
                translateStatusLabel.setText("");
            }
        }
        boolean saved = haveBook && language != null
                && translations.completed(originalKey, language, TranslationStore.hash(originalText)).isPresent();
        viewTranslationButton.setDisable(!showingTranslation && !saved);
        viewTranslationButton.setText(showingTranslation ? "Show original"
                : saved ? "Show " + language.name() + " translation" : "Show translation");
    }

    @FXML
    private void onTranslateBook() {
        TranslationProvider provider = providerBox.getValue();
        Language language = languageBox.getValue();
        if (originalText == null || provider == null || language == null) {
            return;
        }
        String key = originalKey;
        String text = originalText;
        BookTranslator translator = new BookTranslator(provider, translations);
        int pieces = translator.pieces(text).size();
        int characters = text.length();
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "The whole text of \"" + originalTitle + "\" ("
                + characters + " characters, about " + pieces + " requests) will be sent to " + provider.displayName()
                + " to be translated into " + language.name() + ".\n\nDepending on your account this can use credit "
                + "or cost money. You can stop at any time, and finished parts are kept."
                + (language.lowResource() ? "\n\nNote: " + language.name() + " has few texts online, so the "
                + "translation can contain mistakes." : ""), ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText("Translate the whole book?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        translateCancel = new AtomicBoolean();
        AtomicBoolean cancel = translateCancel;
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                try {
                    return translator.translate(key, text, language, new BookTranslator.Listener() {
                        @Override
                        public void progress(int done, int total) {
                            updateProgress(done, total);
                            updateMessage("Translated " + done + " of " + total + " parts. Waiting for the answer...");
                        }

                        @Override
                        public void waiting(int attempt, int attempts, long waitSeconds, String reason) {
                            updateMessage(reason + " Trying again in " + waitSeconds + " seconds (attempt "
                                    + (attempt + 1) + " of " + attempts + "). You can press Stop.");
                        }
                    }, cancel);
                } catch (java.util.concurrent.CancellationException e) {
                    return null; // stopped by the user; finished parts are saved
                }
            }
        };
        translateProgress.progressProperty().bind(task.progressProperty());
        translateStatusLabel.textProperty().bind(task.messageProperty());
        setTranslating(true);
        task.setOnSucceeded(event -> {
            finishTranslating();
            if (task.getValue() == null) {
                updateTranslationAvailability();
                translateStatusLabel.setText("Stopped. Finished parts are saved; press Translate to continue.");
            } else if (key.equals(originalKey)) {
                showTranslation(language, task.getValue());
            }
        });
        task.setOnFailed(event -> {
            finishTranslating();
            Throwable error = task.getException();
            boolean stopped = error instanceof java.util.concurrent.CancellationException;
            updateTranslationAvailability();
            translateStatusLabel.setText(stopped ? "Stopped. Finished parts are saved; press Translate to continue."
                    : "Translation failed: " + (error.getMessage() == null ? error.toString() : error.getMessage()));
        });
        startDaemon(task);
    }

    private void setTranslating(boolean translating) {
        translateProgress.setVisible(translating);
        translateProgress.setManaged(translating);
        cancelTranslateButton.setVisible(translating);
        cancelTranslateButton.setManaged(translating);
        translateButton.setDisable(translating);
        cancelTranslateButton.setDisable(false);
        languageBox.setDisable(translating);
        providerBox.setDisable(translating);
    }

    private void finishTranslating() {
        translateProgress.progressProperty().unbind();
        translateStatusLabel.textProperty().unbind();
        setTranslating(false);
        translateStatusLabel.setText("");
    }

    @FXML
    private void onCancelTranslate() {
        if (translateCancel != null) {
            translateCancel.set(true);
            cancelTranslateButton.setDisable(true);
        }
    }

    @FXML
    private void onToggleTranslation() {
        if (showingTranslation) {
            showingTranslation = false;
            showText(originalTitle, originalText, originalType, originalKey);
            updateTranslationAvailability();
            return;
        }
        Language language = languageBox.getValue();
        if (language == null || originalText == null) {
            return;
        }
        translations.completed(originalKey, language, TranslationStore.hash(originalText))
                .ifPresent(saved -> showTranslation(language, saved.text()));
    }

    private void showTranslation(Language language, String translatedText) {
        showingTranslation = true;
        showText(originalTitle + " (" + language.name() + ")", translatedText, originalType,
                originalKey + ":" + language.code());
        selectVoiceFor(language);
        updateTranslationAvailability();
    }

    /** Picks a voice that speaks the language of the translation, if one is installed. */
    private void selectVoiceFor(Language language) {
        for (Voice voice : voiceBox.getItems()) {
            String code = voice.language().toLowerCase(Locale.ROOT).replace('_', '-');
            if (code.startsWith(language.code()) || (language.code().equals("nb") && code.startsWith("no"))) {
                voiceBox.setValue(voice);
                return;
            }
        }
        if (!voiceBox.getItems().isEmpty()) {
            statusLabel.setText("No " + language.name() + " voice is installed, so reading aloud will sound wrong. "
                    + "Add one in the system settings.");
        }
    }

    private static String shorten(String text, int max) {
        String single = text.replaceAll("\\s+", " ").strip();
        return single.length() <= max ? single : single.substring(0, max - 1) + "\u2026";
    }

    private void refreshNotes() {
        notesList.setItems(FXCollections.observableArrayList(annotations.all()));
    }

    /**
     * Tells the reader whether its tab is visible. Animation and music pause when it is not.
     *
     * @param active {@code true} if the reading tab is selected
     */
    public void setActive(boolean active) {
        music.setActive(active);
        if (!active && readAloud != null && readAloud.getState() == ReadAloud.State.PLAYING) {
            readAloud.pause();
        }
        if (active) {
            sceneView.start();
        } else {
            sceneView.stop();
        }
    }

    @FXML
    private void onSearch() {
        String query = searchField.getText();
        statusLabel.setText("Searching Project Gutenberg...");
        searchButton.setDisable(true);
        Task<List<GutenbergBook>> task = new Task<>() {
            @Override
            protected List<GutenbergBook> call() throws Exception {
                return gutenberg.search(query);
            }
        };
        task.setOnSucceeded(event -> {
            resultList.setItems(FXCollections.observableArrayList(task.getValue()));
            statusLabel.setText(task.getValue().size() + " free books found. Double-click one to read it.");
            searchButton.setDisable(false);
        });
        task.setOnFailed(event -> {
            statusLabel.setText("Search failed: " + task.getException().getMessage());
            searchButton.setDisable(false);
        });
        startDaemon(task);
    }

    @FXML
    private void onOpenSelected() {
        GutenbergBook book = resultList.getSelectionModel().getSelectedItem();
        if (book == null) {
            statusLabel.setText("Select a book in the list first.");
            return;
        }
        downloadAndOpen(book);
    }

    /**
     * Opens a free book that was found in the Discover tab.
     *
     * @param book a book from Project Gutenberg
     */
    public void openFreeBook(CatalogBook book) {
        if (!book.isFreeToRead()) {
            return;
        }
        downloadAndOpen(new GutenbergBook(Integer.parseInt(book.externalId()), book.title(),
                book.authorsText(), book.subjects(), book.freeTextUrl()));
    }

    private void downloadAndOpen(GutenbergBook book) {
        statusLabel.setText("Downloading \"" + book.title() + "\"...");
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return gutenberg.downloadText(book);
            }
        };
        task.setOnSucceeded(event -> {
            BookType type = BookType.guess(book.subjects());
            openText(book.title() + " - " + book.author(), task.getValue(), type, "gutenberg:" + book.id());
            statusLabel.setText("Opened: " + book.title());
        });
        task.setOnFailed(event -> statusLabel.setText("Download failed: " + task.getException().getMessage()));
        startDaemon(task);
    }

    @FXML
    private void onOpenDemo() {
        openText(DemoBook.TITLE, DemoBook.text(), BookType.GENERAL_FICTION, "demo");
        statusLabel.setText("Demo story opened.");
    }

    @FXML
    private void onToggleMenu() {
        focusBox.setSelected(!focusBox.isSelected());
        readerPane.requestFocus();
    }

    @FXML
    private void onNext() {
        if (pageIndex < pages.size() - 1) {
            pageIndex++;
            showPage();
        }
    }

    @FXML
    private void onPrevious() {
        if (pageIndex > 0) {
            pageIndex--;
            showPage();
        }
    }

    /** Opens an original book. A translation of it can then be made or shown. */
    private void openText(String title, String text, BookType type, String bookKey) {
        originalTitle = title;
        originalText = text;
        originalKey = bookKey;
        originalType = type;
        showingTranslation = false;
        showText(title, text, type, bookKey);
        updateTranslationAvailability();
    }

    private void showText(String title, String text, BookType type, String bookKey) {
        pages = Paginator.paginate(text, PAGE_SIZE);
        pageIndex = 0;
        annotations.open(bookKey);
        refreshNotes();
        bookTitleLabel.setText(title);
        typeBox.setValue(type);
        analyzer = new MoodAnalyzer(type);
        showPage();
        readerPane.requestFocus();
    }

    private void showPage() {
        if (!readerTurnedPage) {
            readAloud.stop();
        }
        readerTurnedPage = false;
        String page = pages.get(pageIndex);
        pageView.showPage(pageIndex, page);
        pageView.clearReading();
        pageInfo.setText("Page " + (pageIndex + 1) + " of " + pages.size());
        prevButton.setDisable(pageIndex == 0);
        nextButton.setDisable(pageIndex >= pages.size() - 1);
        Mood mood = analyzer.next(page);
        moodLabel.setText("Music: " + mood.getLabel());
        music.play(mood);
    }

    private static void startDaemon(Task<?> task) {
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}
