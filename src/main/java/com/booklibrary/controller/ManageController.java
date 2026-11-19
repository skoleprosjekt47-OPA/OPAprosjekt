package com.booklibrary.controller;

import com.booklibrary.model.Bookshelf;
import com.booklibrary.model.Genre;
import com.booklibrary.model.Writer;
import com.booklibrary.service.LibraryService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Controller for the Manage tab (manage.fxml): add, edit and delete writers,
 * genres and bookshelves.
 *
 * Author: Team 10 (set this to the member who owns this class)
 * Contribution: Implemented the Manage tab so writers, genres and bookshelves
 * can be registered and managed, as the project description requires.
 * Purpose: wires three lists and their Add/Edit/Delete buttons to
 * {@link LibraryService}. Contains no SQL. After every change it calls the
 * callback set with {@link #setOnChange(Runnable)} so the Books tab can
 * refresh its filters and table.
 */
public class ManageController {

    @FXML private ListView<Writer> writerList;
    @FXML private ListView<Genre> genreList;
    @FXML private ListView<Bookshelf> bookshelfList;

    private final LibraryService libraryService = new LibraryService();
    private Runnable onChange = () -> { };

    /**
     * JavaFX lifecycle method called after the FXML is loaded. Fills the three lists.
     */
    @FXML
    private void initialize() {
        reload();
    }

    /**
     * Sets the action that runs after a writer, genre or bookshelf has been changed.
     *
     * @param onChange the callback, ignored if {@code null}
     */
    public void setOnChange(Runnable onChange) {
        if (onChange != null) {
            this.onChange = onChange;
        }
    }

    /**
     * Reloads all three lists from the database.
     */
    public void reload() {
        try {
            writerList.setItems(FXCollections.observableArrayList(libraryService.getAllWriters()));
            genreList.setItems(FXCollections.observableArrayList(libraryService.getAllGenres()));
            bookshelfList.setItems(FXCollections.observableArrayList(libraryService.getAllBookshelves()));
        } catch (SQLException e) {
            showError("Could not load the lists: " + e.getMessage());
        }
    }

    // ---- writers ----

    /**
     * Handles the Add button: asks for the details of a new writer and adds it.
     */
    @FXML
    private void onAddWriter() {
        String[] v = askFields("Add writer", new String[] {"Name", "Nationality"}, new String[] {"", ""});
        if (v == null) {
            return;
        }
        try {
            libraryService.addWriter(new Writer(v[0], emptyToNull(v[1])));
            changed();
        } catch (SQLException e) {
            showError("Could not add the writer: " + e.getMessage());
        }
    }

    /**
     * Handles the Edit button: edits the selected writer, or shows an error if none is selected.
     */
    @FXML
    private void onEditWriter() {
        Writer selected = writerList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a writer to edit.");
            return;
        }
        String[] v = askFields("Edit writer", new String[] {"Name", "Nationality"},
                new String[] {selected.getName(), selected.getNationality()});
        if (v == null) {
            return;
        }
        try {
            selected.setName(v[0]);
            selected.setNationality(emptyToNull(v[1]));
            libraryService.updateWriter(selected);
            changed();
        } catch (SQLException e) {
            showError("Could not update the writer: " + e.getMessage());
            reload();
        }
    }

    /**
     * Handles the Delete button: deletes the selected writer after the user confirms. Books keep existing but lose the link.
     */
    @FXML
    private void onDeleteWriter() {
        Writer selected = writerList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a writer to delete.");
            return;
        }
        if (confirmDelete(selected.getName())) {
            try {
                libraryService.deleteWriter(selected.getWriterId());
                changed();
            } catch (SQLException e) {
                showError("Could not delete the writer: " + e.getMessage());
            }
        }
    }

    // ---- genres ----

    /**
     * Handles the Add button: asks for the details of a new genre and adds it.
     */
    @FXML
    private void onAddGenre() {
        String[] v = askFields("Add genre", new String[] {"Name", "Description"}, new String[] {"", ""});
        if (v == null) {
            return;
        }
        try {
            libraryService.addGenre(new Genre(v[0], emptyToNull(v[1])));
            changed();
        } catch (SQLException e) {
            showError("Could not add the genre: " + e.getMessage());
        }
    }

    /**
     * Handles the Edit button: edits the selected genre, or shows an error if none is selected.
     */
    @FXML
    private void onEditGenre() {
        Genre selected = genreList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a genre to edit.");
            return;
        }
        String[] v = askFields("Edit genre", new String[] {"Name", "Description"},
                new String[] {selected.getName(), selected.getDescription()});
        if (v == null) {
            return;
        }
        try {
            selected.setName(v[0]);
            selected.setDescription(emptyToNull(v[1]));
            libraryService.updateGenre(selected);
            changed();
        } catch (SQLException e) {
            showError("Could not update the genre: " + e.getMessage());
            reload();
        }
    }

    /**
     * Handles the Delete button: deletes the selected genre after the user confirms. Books keep existing but lose the link.
     */
    @FXML
    private void onDeleteGenre() {
        Genre selected = genreList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a genre to delete.");
            return;
        }
        if (confirmDelete(selected.getName())) {
            try {
                libraryService.deleteGenre(selected.getGenreId());
                changed();
            } catch (SQLException e) {
                showError("Could not delete the genre: " + e.getMessage());
            }
        }
    }

    // ---- bookshelves ----

    /**
     * Handles the Add button: asks for the details of a new bookshelf and adds it.
     */
    @FXML
    private void onAddBookshelf() {
        String[] v = askFields("Add bookshelf", new String[] {"Name", "Room", "Shelf number"},
                new String[] {"", "", ""});
        if (v == null) {
            return;
        }
        try {
            libraryService.addBookshelf(new Bookshelf(v[0], emptyToNull(v[1]), emptyToNull(v[2])));
            changed();
        } catch (SQLException e) {
            showError("Could not add the bookshelf: " + e.getMessage());
        }
    }

    /**
     * Handles the Edit button: edits the selected bookshelf, or shows an error if none is selected.
     */
    @FXML
    private void onEditBookshelf() {
        Bookshelf selected = bookshelfList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a bookshelf to edit.");
            return;
        }
        String[] v = askFields("Edit bookshelf", new String[] {"Name", "Room", "Shelf number"},
                new String[] {selected.getName(), selected.getRoom(), selected.getShelfNumber()});
        if (v == null) {
            return;
        }
        try {
            selected.setName(v[0]);
            selected.setRoom(emptyToNull(v[1]));
            selected.setShelfNumber(emptyToNull(v[2]));
            libraryService.updateBookshelf(selected);
            changed();
        } catch (SQLException e) {
            showError("Could not update the bookshelf: " + e.getMessage());
            reload();
        }
    }

    /**
     * Handles the Delete button: deletes the selected bookshelf after the user confirms. Books keep existing but lose the link.
     */
    @FXML
    private void onDeleteBookshelf() {
        Bookshelf selected = bookshelfList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a bookshelf to delete.");
            return;
        }
        if (confirmDelete(selected.getName())) {
            try {
                libraryService.deleteBookshelf(selected.getBookshelfId());
                changed();
            } catch (SQLException e) {
                showError("Could not delete the bookshelf: " + e.getMessage());
            }
        }
    }

    // ---- helpers ----

    /**
     * Reloads the lists and tells the Books tab that something changed.
     */
    private void changed() {
        reload();
        onChange.run();
    }

    /**
     * Shows a small dialog with one text field per label and returns the trimmed
     * values. The first field is required.
     *
     * @param title   dialog title
     * @param labels  one label per field
     * @param initial the starting text of each field (may contain {@code null})
     * @return the entered values, or {@code null} if the user cancelled or left the first field empty
     */
    private String[] askFields(String title, String[] labels, String[] initial) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(12));
        TextField[] fields = new TextField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            fields[i] = new TextField(initial[i] == null ? "" : initial[i]);
            grid.add(new Label(labels[i]), 0, i);
            grid.add(fields[i], 1, i);
        }
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> answer = dialog.showAndWait();
        if (answer.isEmpty() || answer.get() != ButtonType.OK) {
            return null;
        }
        String[] values = new String[labels.length];
        for (int i = 0; i < labels.length; i++) {
            values[i] = fields[i].getText().trim();
        }
        if (values[0].isEmpty()) {
            showError(labels[0] + " is required.");
            return null;
        }
        return values;
    }

    /**
     * Asks the user to confirm a deletion.
     *
     * @param name the name shown in the question
     * @return {@code true} if the user pressed OK
     */
    private boolean confirmDelete(String name) {
        Alert confirm = new Alert(AlertType.CONFIRMATION, "Delete \"" + name + "\"?\n"
                + "Books that use it are kept, but they will no longer be linked to it.");
        Optional<ButtonType> answer = confirm.showAndWait();
        return answer.isPresent() && answer.get() == ButtonType.OK;
    }

    /**
     * Turns blank text into {@code null} so optional columns stay empty in the database.
     *
     * @param text the text to check
     * @return the text, or {@code null} if it is blank
     */
    private static String emptyToNull(String text) {
        return text == null || text.isBlank() ? null : text;
    }

    /**
     * Shows an error alert with the given message.
     *
     * @param message the message to show
     */
    private void showError(String message) {
        new Alert(AlertType.ERROR, message).showAndWait();
    }
}
