package com.booklibrary.controller;

import com.booklibrary.model.Book;
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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
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

    private final LibraryService libraryService = new LibraryService();

    /**
     * JavaFX lifecycle method called after the FXML is loaded. Wires the
     * table columns to the {@link Book} properties, populates the status
     * filter, loads the writer/genre/bookshelf filter options, and loads
     * the initial book list.
     */
    @FXML
    private void initialize() {
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
