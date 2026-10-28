package com.booklibrary.controller;

import com.booklibrary.MainApp;
import com.booklibrary.catalog.AdaptationCatalog;
import com.booklibrary.catalog.CatalogSearchResult;
import com.booklibrary.catalog.FilmSite;
import com.booklibrary.catalog.CatalogService;
import com.booklibrary.model.Adaptation;
import com.booklibrary.model.Book;
import com.booklibrary.model.Bookshelf;
import com.booklibrary.model.CatalogBook;
import com.booklibrary.model.ReadingStatus;
import com.booklibrary.ui.HoverPreview;
import com.booklibrary.service.LibraryService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Controller for the "Discover" tab, where the user searches external library
 * catalogs and can add a found book to the private library.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented the Discover screen: background search, result table, detail panel and "Add to my library".
 * Purpose: connects the Discover view (discover.fxml) to {@link CatalogService}
 * (searching the outside world) and {@link LibraryService} (saving to the
 * private library). Searches run in a background thread so the window never freezes.
 */
public class DiscoverController {

    @FXML private VBox adaptationBox;
    @FXML private VBox adaptationList;
    private final com.booklibrary.catalog.WikidataAdaptationFinder adaptationFinder =
            new com.booklibrary.catalog.WikidataAdaptationFinder(
                    new com.booklibrary.catalog.JdkHttpFetcher(java.time.Duration.ofSeconds(25)));
    private int adaptationRequest;
    private int searchRequest;
    /** Hover card for a search result; everything it shows is already in the result, so it appears at once. */
    private final HoverPreview<CatalogBook> resultPreview = new HoverPreview<>(book -> new HoverPreview.Data(
            book.title(),
            (book.authors().isEmpty() ? "" : book.authorsText() + "  -  ") + (book.year() == null ? "" : book.year()),
            book.description() == null ? ""
                    : book.description().strip().length() > 380
                    ? book.description().strip().substring(0, 380).strip() + "..." : book.description().strip(),
            book.coverUrl()),
            book -> book.source() + "|" + book.externalId());
    @FXML private Label adaptationStatus;
    @FXML private TextField queryField;
    @FXML private Button searchButton;
    @FXML private ProgressIndicator progress;
    @FXML private Label statusLabel;

    @FXML private TableView<CatalogBook> resultTable;
    @FXML private TableColumn<CatalogBook, String> titleColumn;
    @FXML private TableColumn<CatalogBook, String> authorColumn;
    @FXML private TableColumn<CatalogBook, String> yearColumn;
    @FXML private TableColumn<CatalogBook, String> sourceColumn;

    @FXML private ImageView coverView;
    @FXML private Label titleLabel;
    @FXML private Label authorsLabel;
    @FXML private Label metaLabel;
    @FXML private Label subjectsLabel;
    @FXML private TextArea descriptionArea;
    @FXML private Button addButton;
    @FXML private Button openButton;
    @FXML private Button readButton;
    @FXML private Button addReadButton;
    @FXML private CheckBox freeOnlyBox;

    private final CatalogService catalogService = new CatalogService();
    private final LibraryService libraryService = new LibraryService();
    private Runnable onLibraryChanged = () -> { };
    private Consumer<CatalogBook> onReadRequested = book -> { };
    private List<CatalogBook> allResults = List.of();

    /**
     * Called by JavaFX after the FXML is loaded. Sets up the table columns and
     * reacts to the user selecting a search result.
     */
    @FXML
    private void initialize() {
        resultTable.setRowFactory(table -> {
            javafx.scene.control.TableRow<CatalogBook> row = new javafx.scene.control.TableRow<>();
            resultPreview.attach(row, row::getItem);
            return row;
        });
        titleColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().title()));
        authorColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().authorsText()));
        yearColumn.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().year() == null ? "" : String.valueOf(d.getValue().year())));
        sourceColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().source()));

        resultTable.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldBook, newBook) -> showDetails(newBook));
        freeOnlyBox.selectedProperty().addListener((observable, was, now) -> showResults(null));
        progress.setVisible(false);
        statusLabel.setText("Search " + String.join(" and ", catalogService.getProviderNames())
                + " by title, author or subject.");
        showDetails(null);
    }

    /**
     * Registers an action that runs after a book was added to the private
     * library, so the Books tab can refresh itself.
     *
     * @param onLibraryChanged the action to run, never {@code null}
     */
    public void setOnLibraryChanged(Runnable onLibraryChanged) {
        this.onLibraryChanged = onLibraryChanged;
    }

    /**
     * Registers the action that opens a free book in the reading mode.
     *
     * @param onReadRequested receives the free book to read, never {@code null}
     */
    public void setOnReadRequested(Consumer<CatalogBook> onReadRequested) {
        this.onReadRequested = onReadRequested;
    }

    /**
     * Opens the selected free book in the reading mode.
     */
    @FXML
    private void onReadNow() {
        CatalogBook book = resultTable.getSelectionModel().getSelectedItem();
        if (book != null && book.isFreeToRead()) {
            onReadRequested.accept(book);
        }
    }

    /**
     * Fills the result table from the last search, applying the "free books only" filter.
     *
     * @param errors problems from the search to show after the count, or {@code null} to show none
     */
    private void showResults(List<String> errors) {
        showResults(errors, false);
    }

    private void showResults(List<String> errors, boolean keepSelection) {
        List<CatalogBook> shown = freeOnlyBox.isSelected()
                ? allResults.stream().filter(CatalogBook::isFreeToRead).toList()
                : allResults;
        CatalogBook selected = keepSelection ? resultTable.getSelectionModel().getSelectedItem() : null;
        resultTable.setItems(FXCollections.observableArrayList(shown));
        if (selected != null && shown.contains(selected)) {
            resultTable.getSelectionModel().select(selected);
        } else if (!shown.isEmpty()) {
            resultTable.getSelectionModel().selectFirst();
        }
        String text = shown.size() + " book(s) shown"
                + (freeOnlyBox.isSelected() ? " (free to read)" : "") + ".";
        if (errors != null && !errors.isEmpty()) {
            text += " Problems: " + String.join("; ", errors);
        }
        if (!allResults.isEmpty() || errors != null) {
            statusLabel.setText(text);
        }
    }

    /**
     * Starts a search in a background thread and shows the results when it finishes.
     */
    @FXML
    private void onSearch() {
        String query = queryField.getText();
        if (query == null || query.isBlank()) {
            statusLabel.setText("Type a title, author or subject first.");
            return;
        }
        setBusy(true);
        boolean freeOnly = freeOnlyBox.isSelected();
        statusLabel.setText(freeOnly ? "Searching free books (this can take a few seconds)..." : "Searching...");
        int myRequest = ++searchRequest;
        allResults = List.of();

        Task<CatalogSearchResult> task = new Task<>() {
            @Override
            protected CatalogSearchResult call() {
                // Show each catalog's answer as soon as it arrives; the slowest one no longer holds up the rest.
                return catalogService.search(query, freeOnly, partial -> javafx.application.Platform.runLater(() -> {
                    if (myRequest == searchRequest && !partial.books().isEmpty()) {
                        allResults = partial.books();
                        showResults(partial.errors(), true);
                        searchButton.setDisable(false);
                        queryField.setDisable(false);
                    }
                }));
            }
        };
        task.setOnSucceeded(event -> {
            if (myRequest != searchRequest) {
                return;
            }
            CatalogSearchResult result = task.getValue();
            allResults = result.books();
            showResults(result.errors(), true);
            setBusy(false);
        });
        task.setOnFailed(event -> {
            if (myRequest == searchRequest) {
                statusLabel.setText("Search failed: " + task.getException().getMessage());
                setBusy(false);
            }
        });

        Thread thread = new Thread(task, "catalog-search");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Shows or hides the busy indicator and disables the search controls while searching.
     *
     * @param busy {@code true} while a search is running
     */
    private void setBusy(boolean busy) {
        progress.setVisible(busy);
        searchButton.setDisable(busy);
        queryField.setDisable(busy);
    }

    /**
     * Fills the detail panel with the selected book, or clears it.
     *
     * @param book the selected search result, or {@code null} for no selection
     */
    private void showDetails(CatalogBook book) {
        boolean none = book == null;
        addButton.setDisable(none);
        openButton.setDisable(none || book.infoUrl() == null);
        readButton.setDisable(none || !book.isFreeToRead());
        addReadButton.setDisable(none || !book.isFreeToRead());
        showAdaptations(book);
        if (none) {
            titleLabel.setText("No book selected");
            authorsLabel.setText("");
            metaLabel.setText("");
            subjectsLabel.setText("");
            descriptionArea.setText("");
            coverView.setImage(null);
            return;
        }
        titleLabel.setText(book.title());
        authorsLabel.setText(book.authors().isEmpty() ? "Unknown author" : book.authorsText());
        metaLabel.setText(buildMeta(book));
        subjectsLabel.setText(book.subjects().isEmpty() ? "" : "Subjects: " + String.join(", ", book.subjects()));
        descriptionArea.setText(book.description() == null
                ? "No description from " + book.source() + "." : book.description());
        coverView.setImage(book.coverUrl() == null ? null : new Image(book.coverUrl(), true));
    }

    /**
     * Looks up the films and series based on the selected book (in a background thread, so the
     * window never freezes) and shows a watch button for each. A book without any gets a message.
     *
     * @param book the selected book, or {@code null} for none
     */
    private void showAdaptations(CatalogBook book) {
        adaptationList.getChildren().clear();
        int request = ++adaptationRequest;
        if (book == null) {
            adaptationBox.setVisible(false);
            adaptationBox.setManaged(false);
            return;
        }
        adaptationBox.setVisible(true);
        adaptationBox.setManaged(true);
        adaptationStatus.setText("Looking for films and series based on this book...");
        String title = book.title();
        List<String> authors = book.authors();
        Task<List<Adaptation>> task = new Task<>() {
            @Override
            protected List<Adaptation> call() throws Exception {
                AdaptationCatalog catalog = AdaptationCatalog.load();
                List<Adaptation> online;
                try {
                    online = adaptationFinder.find(title, authors);
                } catch (java.io.IOException e) {
                    online = List.of();
                    offline = true;
                }
                return catalog.combine(title, online);
            }

            private volatile boolean offline;

            @Override
            protected void succeeded() {
                if (request == adaptationRequest) {
                    listAdaptations(title, getValue(), offline);
                }
            }

            @Override
            protected void failed() {
                if (request == adaptationRequest) {
                    adaptationStatus.setText("Could not look up films right now.");
                }
            }
        };
        Thread thread = new Thread(task, "adaptation-lookup");
        thread.setDaemon(true);
        thread.start();
    }

    private void listAdaptations(String title, List<Adaptation> found, boolean offline) {
        adaptationList.getChildren().clear();
        if (found.isEmpty()) {
            adaptationStatus.setText(offline
                    ? "Could not reach the film database. Check your internet connection."
                    : "This book has no film or series that we could find.");
            return;
        }
        adaptationStatus.setText(offline ? "Offline: showing only the films we already know." : "");
        for (Adaptation a : found) {
            Button watch = new Button("\u25B6 Watch: " + a.label());
            watch.setOnAction(e -> MainApp.openInBrowser(FilmSite.urlFor(a)));
            adaptationList.getChildren().add(watch);
        }
    }

    /**
     * Builds the one-line summary of year, pages, publisher, language and source.
     *
     * @param book the book to describe
     * @return a text such as "1949 · 328 pages · Secker &amp; Warburg · Open Library"
     */
    private String buildMeta(CatalogBook book) {
        StringBuilder meta = new StringBuilder();
        if (book.year() != null) {
            meta.append(book.year());
        }
        if (book.pageCount() != null) {
            meta.append(meta.length() > 0 ? " · " : "").append(book.pageCount()).append(" pages");
        }
        if (book.publisher() != null) {
            meta.append(meta.length() > 0 ? " · " : "").append(book.publisher());
        }
        meta.append(meta.length() > 0 ? " · " : "").append(book.source());
        return meta.toString();
    }

    /**
     * Opens the selected book's page in the user's web browser.
     */
    @FXML
    private void onOpenPage() {
        CatalogBook book = resultTable.getSelectionModel().getSelectedItem();
        if (book != null) {
            MainApp.openInBrowser(book.infoUrl());
        }
    }

    /**
     * Asks for a bookshelf and reading status, then adds the selected book to the private library.
     */
    @FXML
    private void onAddToLibrary() {
        CatalogBook book = resultTable.getSelectionModel().getSelectedItem();
        if (book != null) {
            addToLibrary(book);
        }
    }

    /**
     * Adds the selected free book to the private library and then opens it in the reading mode.
     */
    @FXML
    private void onAddAndRead() {
        CatalogBook book = resultTable.getSelectionModel().getSelectedItem();
        if (book != null && book.isFreeToRead() && addToLibrary(book)) {
            onReadRequested.accept(book);
        }
    }

    /**
     * Adds a book to the private library after asking for bookshelf and status.
     *
     * @param book the book to add
     * @return {@code true} if the book was added, {@code false} if the user cancelled or an error occurred
     */
    private boolean addToLibrary(CatalogBook book) {
        try {
            if (libraryService.isInLibrary(book) && !confirm("\"" + book.title()
                    + "\" is already in your library. Add it again?")) {
                return false;
            }
            List<Bookshelf> shelves = libraryService.getAllBookshelves();
            Optional<Placement> placement = askPlacement(book, shelves);
            if (placement.isEmpty()) {
                return false;
            }
            Bookshelf shelf = placement.get().shelf();
            if (shelf.getBookshelfId() == 0) {
                shelf = libraryService.addBookshelf(shelf);
            }
            Book saved = libraryService.importCatalogBook(book, shelf, placement.get().status());
            statusLabel.setText("Added \"" + saved.getTitle() + "\" to your library.");
            onLibraryChanged.run();
            return true;
        } catch (SQLException e) {
            new Alert(AlertType.ERROR, "Could not add the book: " + e.getMessage()).showAndWait();
            return false;
        }
    }

    /**
     * Shows a dialog where the user picks (or names) a bookshelf and a reading status.
     *
     * @param book    the book being added
     * @param shelves the existing bookshelves
     * @return the user's choice, or empty if the dialog was cancelled
     */
    private Optional<Placement> askPlacement(CatalogBook book, List<Bookshelf> shelves) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add to my library");
        dialog.setHeaderText(book.title());

        ComboBox<Bookshelf> shelfBox = new ComboBox<>(FXCollections.observableArrayList(shelves));
        shelfBox.setEditable(true);
        shelfBox.setPromptText("Pick one or type a new name");
        shelfBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Bookshelf shelf) {
                return shelf == null ? "" : shelf.getName();
            }

            @Override
            public Bookshelf fromString(String text) {
                return Bookshelf.fromName(text, shelves);
            }
        });
        if (!shelves.isEmpty()) {
            shelfBox.setValue(shelves.get(0));
        }
        ComboBox<ReadingStatus> statusBox = new ComboBox<>(FXCollections.observableArrayList(ReadingStatus.values()));
        statusBox.setValue(ReadingStatus.UNREAD);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.addRow(0, new Label("Bookshelf"), shelfBox);
        grid.addRow(1, new Label("Reading status"), statusBox);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> answer = dialog.showAndWait();
        if (answer.isEmpty() || answer.get() != ButtonType.OK) {
            return Optional.empty();
        }
        Bookshelf shelf = Bookshelf.fromName(shelfBox.getEditor().getText(), shelves);
        return Optional.of(new Placement(shelf, statusBox.getValue()));
    }

    /**
     * Asks a yes/no question.
     *
     * @param message the question
     * @return {@code true} if the user answered OK
     */
    private boolean confirm(String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION, message);
        return alert.showAndWait().filter(button -> button == ButtonType.OK).isPresent();
    }

    /**
     * The user's choice in the "Add to my library" dialog.
     *
     * @param shelf  the chosen or new bookshelf (id 0 means it has to be created)
     * @param status the chosen reading status
     */
    private record Placement(Bookshelf shelf, ReadingStatus status) {
    }
}
