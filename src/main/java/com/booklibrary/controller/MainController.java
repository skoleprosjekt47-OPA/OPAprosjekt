package com.booklibrary.controller;

import com.booklibrary.catalog.CatalogService;
import com.booklibrary.model.Book;
import com.booklibrary.model.CatalogBook;
import com.booklibrary.ui.HoverPreview;
import com.booklibrary.model.Bookshelf;
import com.booklibrary.model.Genre;
import com.booklibrary.model.ReadingStatus;
import com.booklibrary.model.Writer;
import com.booklibrary.service.LibraryService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Controller for the main window (main.fxml): the book list with search
 * and filters, and the reports tab.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented the main window controller: search/filter, table wiring, add/edit/delete actions and the reports tab.
 * Purpose: wires the UI controls to {@link LibraryService}. Contains no
 * SQL and no business rules of its own - it only reacts to user actions
 * and refreshes the view with what the service returns.
 */
public class MainController {

    @FXML private TextField searchField;
    @FXML private ComboBox<Writer> writerFilter;
    @FXML private ComboBox<Genre> genreFilter;
    @FXML private ComboBox<Bookshelf> bookshelfFilter;
    @FXML private ComboBox<ReadingStatus> statusFilter;

    @FXML private TableView<Book> bookTable;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, Number> yearColumn;
    @FXML private TableColumn<Book, String> writerColumn;
    @FXML private TableColumn<Book, String> genreColumn;
    @FXML private TableColumn<Book, String> bookshelfColumn;
    @FXML private TableColumn<Book, String> statusColumn;

    @FXML private Label statusLabel;

    @FXML private Label totalBooksLabel;
    @FXML private ListView<String> statusReportList;
    @FXML private ListView<String> genreReportList;
    @FXML private ListView<String> writerReportList;

    /** Controller of the included Discover tab; injected by FXML from fx:id="discover". */
    @FXML private DiscoverController discoverController;
    @FXML private ReaderController readerController;
    @FXML private KidsController kidsController;
    @FXML private TabPane mainTabs;

    private final LibraryService libraryService = new LibraryService();
    private final CatalogService catalogService = new CatalogService();
    private final HoverPreview<Book> bookPreview = new HoverPreview<>(this::loadBookPreview,
            book -> book.getTitle() + "|" + (book.getWriter() == null ? "" : book.getWriter().getName()));

    /**
     * JavaFX lifecycle method called after the FXML is loaded. Wires the
     * table columns to the {@link Book} properties, populates the status
     * filter, loads the writer/genre/bookshelf filter options, and loads
     * the initial book list.
     */
    @FXML
    private void initialize() {
        bookTable.setRowFactory(table -> {
            TableRow<Book> row = new TableRow<>();
            bookPreview.attach(row, row::getItem);
            ContextMenu menu = buildBookMenu(row);
            row.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> row.setContextMenu(isEmpty ? null : menu));
            return row;
        });
        titleColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getTitle()));
        yearColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(
                data.getValue().getReleaseYear()));
        writerColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getWriter() != null ? data.getValue().getWriter().getName() : ""));
        genreColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getGenre() != null ? data.getValue().getGenre().getName() : ""));
        bookshelfColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getBookshelf() != null ? data.getValue().getBookshelf().getName() : ""));
        statusColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getReadingStatus().getDisplayLabel()));

        statusFilter.setItems(FXCollections.observableArrayList(ReadingStatus.values()));

        loadFilterOptions();
        refreshTable(safeFindAll());

        discoverController.setOnLibraryChanged(() -> {
            loadFilterOptions();
            onSearch();
        });

        discoverController.setOnReadRequested(book -> {
            for (Tab tab : mainTabs.getTabs()) {
                if ("Reading".equals(tab.getText())) {
                    mainTabs.getSelectionModel().select(tab);
                }
            }
            readerController.openFreeBook(book);
        });

        // the reading mode plays music and animation only while its tab is visible
        mainTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            readerController.setActive(newTab != null && "Reading".equals(newTab.getText()));
            kidsController.setActive(newTab != null && "Kids".equals(newTab.getText()));
        });
    }

    /**
     * Builds the right-click menu of a book row: read it, change its reading status, edit or delete it.
     *
     * @param row the table row the menu belongs to
     * @return the menu
     */
    private ContextMenu buildBookMenu(TableRow<Book> row) {
        MenuItem read = new MenuItem("Read in Reading mode");
        read.setOnAction(event -> readInReadingMode(row.getItem()));

        MenuItem markRead = new MenuItem("Mark as Read");
        markRead.setOnAction(event -> setStatus(row.getItem(), ReadingStatus.READ));
        MenuItem markReading = new MenuItem("Mark as Currently reading");
        markReading.setOnAction(event -> setStatus(row.getItem(), ReadingStatus.READING));
        MenuItem markUnread = new MenuItem("Mark as Unread");
        markUnread.setOnAction(event -> setStatus(row.getItem(), ReadingStatus.UNREAD));

        MenuItem edit = new MenuItem("Edit...");
        edit.setOnAction(event -> {
            bookTable.getSelectionModel().select(row.getItem());
            onEditBook();
        });
        MenuItem delete = new MenuItem("Delete...");
        delete.setOnAction(event -> {
            bookTable.getSelectionModel().select(row.getItem());
            onDeleteBook();
        });

        ContextMenu menu = new ContextMenu(read, new SeparatorMenuItem(), markRead, markReading, markUnread,
                new SeparatorMenuItem(), edit, delete);
        menu.setOnShowing(event -> {
            if (row.getItem() != null) {
                bookTable.getSelectionModel().select(row.getItem());
            }
        });
        return menu;
    }

    /**
     * Changes the reading status of a book and refreshes the table.
     *
     * @param book   the book to change, ignored if {@code null}
     * @param status the new reading status
     */
    private void setStatus(Book book, ReadingStatus status) {
        if (book == null) {
            return;
        }
        try {
            book.setReadingStatus(status);
            libraryService.updateBook(book);
            onSearch();
        } catch (SQLException e) {
            showError("Could not change the status: " + e.getMessage());
        }
    }

    /**
     * Looks for the full free text of a library book (Project Gutenberg) and opens it in the Reading tab.
     * Shows a message if no free text exists, since most modern books are still under copyright.
     *
     * @param book the book to read, ignored if {@code null}
     */
    private void readInReadingMode(Book book) {
        if (book == null) {
            return;
        }
        String author = book.getWriter() == null ? "" : book.getWriter().getName();
        javafx.concurrent.Task<CatalogBook> task = new javafx.concurrent.Task<>() {
            @Override
            protected CatalogBook call() {
                for (String query : new String[]{book.getTitle(), (book.getTitle() + " " + author).trim()}) {
                    for (CatalogBook found : catalogService.searchFree(query).books()) {
                        if (found.isFreeToRead() && found.title() != null
                                && found.title().equalsIgnoreCase(book.getTitle())) {
                            return found;
                        }
                    }
                }
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            CatalogBook found = task.getValue();
            if (found == null) {
                new Alert(AlertType.INFORMATION, "No free full text was found for \"" + book.getTitle()
                        + "\". Only books whose copyright has expired can be read in the app.").showAndWait();
                return;
            }
            for (Tab tab : mainTabs.getTabs()) {
                if ("Reading".equals(tab.getText())) {
                    mainTabs.getSelectionModel().select(tab);
                }
            }
            readerController.openFreeBook(found);
        });
        task.setOnFailed(event -> showError("Could not look for the book: " + task.getException().getMessage()));
        Thread thread = new Thread(task, "find-free-text");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Finds a cover and a short description for a book in the library by searching the online catalogues.
     * Runs on a background thread when the mouse rests on a book.
     *
     * @param book the book the mouse is over
     * @return the information for the hover card; never {@code null}
     */
    private HoverPreview.Data loadBookPreview(Book book) {
        String author = book.getWriter() == null ? "" : book.getWriter().getName();
        String fallbackByline = (author.isBlank() ? "" : author + "  -  ") + book.getReleaseYear();
        try {
            List<CatalogBook> found = catalogService.searchQuick((book.getTitle() + " " + author).trim()).books();
            CatalogBook best = null;
            for (CatalogBook candidate : found) {
                boolean sameTitle = candidate.title() != null
                        && candidate.title().equalsIgnoreCase(book.getTitle());
                boolean hasCover = candidate.coverUrl() != null;
                if (sameTitle && hasCover) {
                    best = candidate;
                    break;
                }
                if (best == null && hasCover) {
                    best = candidate;
                }
            }
            if (best == null && !found.isEmpty()) {
                best = found.get(0);
            }
            if (best != null) {
                String description = best.description() == null ? "" : best.description().strip();
                if (description.length() > 380) {
                    description = description.substring(0, 380).strip() + "...";
                }
                String byline = fallbackByline;
                if (best.subjects() != null && !best.subjects().isEmpty()) {
                    byline += "  -  " + best.firstSubject();
                }
                return new HoverPreview.Data(book.getTitle(), byline, description, best.coverUrl());
            }
        } catch (RuntimeException e) {
            // no internet or catalogue down: show the basic card below
        }
        return new HoverPreview.Data(book.getTitle(), fallbackByline, "", null);
    }

    /**
     * Loads the writers, genres and bookshelves from the service into the
     * filter combo boxes, showing an error alert if the query fails.
     */
    private void loadFilterOptions() {
        try {
            writerFilter.setItems(FXCollections.observableArrayList(libraryService.getAllWriters()));
            genreFilter.setItems(FXCollections.observableArrayList(libraryService.getAllGenres()));
            bookshelfFilter.setItems(FXCollections.observableArrayList(libraryService.getAllBookshelves()));
        } catch (SQLException e) {
            showError("Could not load writers/genres/bookshelves: " + e.getMessage());
        }
    }

    /**
     * Fetches all books from the service, showing an error alert and
     * returning an empty list instead of propagating the exception if the
     * query fails.
     *
     * @return all books, or an empty list on failure
     */
    private List<Book> safeFindAll() {
        try {
            return libraryService.getAllBooks();
        } catch (SQLException e) {
            showError("Could not load books: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Replaces the table's contents with the given books and updates the
     * status label with the number of books shown.
     *
     * @param books the books to display
     */
    private void refreshTable(List<Book> books) {
        ObservableList<Book> items = FXCollections.observableArrayList(books);
        bookTable.setItems(items);
        statusLabel.setText(items.size() + " book(s) shown");
    }

    /**
     * Handles the Search button click: reads the search text and filter
     * selections, queries the service, and refreshes the table with the
     * results.
     */
    @FXML
    private void onSearch() {
        try {
            Integer writerId = writerFilter.getValue() != null ? writerFilter.getValue().getWriterId() : null;
            Integer genreId = genreFilter.getValue() != null ? genreFilter.getValue().getGenreId() : null;
            Integer bookshelfId = bookshelfFilter.getValue() != null ? bookshelfFilter.getValue().getBookshelfId() : null;
            ReadingStatus status = statusFilter.getValue();

            List<Book> results = libraryService.searchBooks(searchField.getText(), writerId, genreId,
                    bookshelfId, status);
            refreshTable(results);
        } catch (SQLException e) {
            showError("Search failed: " + e.getMessage());
        }
    }

    /**
     * Handles the Clear Filters button click: resets the search field and
     * all filter combo boxes, then reloads the full, unfiltered book list.
     */
    @FXML
    private void onClearFilters() {
        searchField.clear();
        writerFilter.setValue(null);
        genreFilter.setValue(null);
        bookshelfFilter.setValue(null);
        statusFilter.setValue(null);
        refreshTable(safeFindAll());
    }

    /**
     * Handles the Add Book button click: opens the book form dialog in
     * "add" mode (no existing book).
     */
    @FXML
    private void onAddBook() {
        openBookForm(null);
    }

    /**
     * Handles the Edit Book button click: opens the book form dialog
     * pre-filled with the currently selected book, or shows an error if no
     * book is selected.
     */
    @FXML
    private void onEditBook() {
        Book selected = bookTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a book to edit.");
            return;
        }
        openBookForm(selected);
    }

    /**
     * Handles the Delete Book button click: confirms the deletion with the
     * user, then deletes the selected book through the service and
     * refreshes the table. Shows an error if no book is selected or the
     * deletion fails.
     */
    @FXML
    private void onDeleteBook() {
        Book selected = bookTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a book to delete.");
            return;
        }
        Alert confirm = new Alert(AlertType.CONFIRMATION, "Delete \"" + selected.getTitle() + "\"?");
        confirm.showAndWait().ifPresent(buttonType -> {
            if (buttonType.getText().equalsIgnoreCase("OK")) {
                try {
                    libraryService.deleteBook(selected.getBookId());
                    refreshTable(safeFindAll());
                } catch (SQLException e) {
                    showError("Could not delete book: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Loads the book form dialog, lets the user add or edit a book, and if
     * they save, persists the result through the service and refreshes the
     * table. Shows an error alert if the form cannot be loaded or the save
     * fails.
     *
     * @param existingBook the book to edit, or {@code null} to add a new one
     */
    private void openBookForm(Book existingBook) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/book_form.fxml"));
            Parent root = loader.load();
            BookFormController controller = loader.getController();
            controller.initializeData(libraryService.getAllWriters(), libraryService.getAllGenres(),
                    libraryService.getAllBookshelves(), existingBook);

            Stage dialog = new Stage();
            dialog.setTitle(existingBook == null ? "Add book" : "Edit book");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            Book result = controller.getResult();
            if (result != null) {
                if (result.getBookshelf() != null && result.getBookshelf().getBookshelfId() == 0) {
                    result.setBookshelf(libraryService.addBookshelf(result.getBookshelf()));
                }
                if (existingBook == null) {
                    libraryService.addBook(result);
                } else {
                    libraryService.updateBook(result);
                }
                refreshTable(safeFindAll());
            }
        } catch (Exception e) {
            showError("Could not open the book form: " + e.getMessage());
        }
    }

    /**
     * Handles the Refresh Report button click: rebuilds the library summary
     * and updates the total-books label and the status/genre/writer report
     * lists.
     */
    @FXML
    private void onRefreshReport() {
        try {
            LibraryService.LibrarySummary summary = libraryService.buildSummary();
            totalBooksLabel.setText("Total books: " + summary.totalBooks());
            statusReportList.setItems(toDisplayList(summary.byReadingStatus()));
            genreReportList.setItems(toDisplayList(summary.byGenre()));
            writerReportList.setItems(toDisplayList(summary.byWriter()));
        } catch (SQLException e) {
            showError("Could not build report: " + e.getMessage());
        }
    }

    /**
     * Converts a map of label-to-count into a sorted, display-ready list of
     * "label: count" strings for a report {@link ListView}.
     *
     * @param counts the counts keyed by label
     * @return a sorted observable list of formatted "label: count" strings
     */
    private ObservableList<String> toDisplayList(Map<String, Long> counts) {
        List<String> lines = counts.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .sorted()
                .toList();
        return FXCollections.observableArrayList(lines);
    }

    /**
     * Shows an error alert dialog with the given message.
     *
     * @param message the error message to display
     */
    private void showError(String message) {
        new Alert(AlertType.ERROR, message).showAndWait();
    }
}
