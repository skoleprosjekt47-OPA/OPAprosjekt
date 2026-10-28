package com.booklibrary.controller;

import com.booklibrary.model.Book;
import com.booklibrary.model.Bookshelf;
import com.booklibrary.model.Genre;
import com.booklibrary.model.ReadingStatus;
import com.booklibrary.model.Writer;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controller for the add/edit book dialog (book_form.fxml).
 *
 * Author: Farid Rahmani
 * Contribution: Implemented the add/edit book dialog: input validation and building the resulting Book object.
 * Purpose: reads the user's input, validates it, and builds the
 * resulting {@link Book}. Does not talk to the database itself - the
 * caller ({@link MainController}) is responsible for saving the result
 * through {@link com.booklibrary.service.LibraryService}.
 */
public class BookFormController {

    @FXML private TextField titleField;
    @FXML private TextField yearField;
    @FXML private ComboBox<Writer> writerBox;
    @FXML private ComboBox<Genre> genreBox;
    @FXML private ComboBox<Bookshelf> bookshelfBox;
    @FXML private ComboBox<ReadingStatus> statusBox;
    @FXML private Label errorLabel;
    @FXML private Button saveButton;

    private int editingBookId = 0;
    private boolean saved = false;
    private List<Bookshelf> knownShelves = List.of();

    /**
     * Populates the dropdowns with the available writers, genres and
     * bookshelves, and pre-fills the form if editing an existing book.
     *
     * @param existingBook the book to edit, or {@code null} to add a new one
     */
    public void initializeData(List<Writer> writers, List<Genre> genres, List<Bookshelf> bookshelves,
                                Book existingBook) {
        writerBox.setItems(FXCollections.observableArrayList(writers));
        genreBox.setItems(FXCollections.observableArrayList(genres));
        bookshelfBox.setItems(FXCollections.observableArrayList(bookshelves));
        knownShelves = List.copyOf(bookshelves);
        bookshelfBox.setEditable(true);
        bookshelfBox.setPromptText("Pick one or type a new name");
        bookshelfBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Bookshelf shelf) {
                return shelf == null ? "" : shelf.getName();
            }

            @Override
            public Bookshelf fromString(String text) {
                return Bookshelf.fromName(text, knownShelves);
            }
        });
        statusBox.setItems(FXCollections.observableArrayList(ReadingStatus.values()));

        if (existingBook != null) {
            editingBookId = existingBook.getBookId();
            titleField.setText(existingBook.getTitle());
            yearField.setText(String.valueOf(existingBook.getReleaseYear()));
            writerBox.setValue(existingBook.getWriter());
            genreBox.setValue(existingBook.getGenre());
            bookshelfBox.setValue(existingBook.getBookshelf());
            statusBox.setValue(existingBook.getReadingStatus());
        } else {
            statusBox.setValue(ReadingStatus.UNREAD);
        }
    }

    /**
     * Handles the Save button click: validates the title, release year and
     * reading status fields, builds the resulting {@link Book} into
     * {@code result}, marks the form as saved, and closes the window. If
     * validation fails, shows an error message and leaves the dialog open.
     */
    @FXML
    private void onSave() {
        String title = titleField.getText();
        String yearText = yearField.getText();

        if (title == null || title.isBlank()) {
            errorLabel.setText("Title is required.");
            return;
        }
        int year;
        try {
            year = Integer.parseInt(yearText.trim());
        } catch (NumberFormatException e) {
            errorLabel.setText("Release year must be a number.");
            return;
        }
        if (statusBox.getValue() == null) {
            errorLabel.setText("Please choose a reading status.");
            return;
        }

        result = new Book(editingBookId, title.trim(), year, statusBox.getValue(),
                writerBox.getValue(), genreBox.getValue(),
                Bookshelf.fromName(bookshelfBox.getEditor().getText(), knownShelves));
        saved = true;
        closeWindow();
    }

    /**
     * Handles the Cancel button click: discards any input and closes the
     * window without saving.
     */
    @FXML
    private void onCancel() {
        saved = false;
        closeWindow();
    }

    private Book result;

    /**
     * @return the book built from the form, or {@code null} if the user
     *         cancelled or has not saved yet.
     */
    public Book getResult() {
        return saved ? result : null;
    }

    /** Closes the dialog window that hosts this form. */
    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }
}
