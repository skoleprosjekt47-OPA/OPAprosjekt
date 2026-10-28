package com.booklibrary.ui;

import com.booklibrary.reader.Annotation;
import com.booklibrary.reader.AnnotationManager;
import com.booklibrary.reader.HighlightColor;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.HitInfo;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Popup;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.BiConsumer;

/**
 * Shows one page of a book where the reader can select text, highlight it, underline it and add comments.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented the selection, highlight colours, underline and comment features of the reader.
 * Purpose: the text is drawn in a {@link TextFlow}. A transparent layer behind it
 * paints the coloured highlights, and a layer on top holds the small pencil
 * symbols that mark a comment. When the reader drags over text, a small
 * toolbar appears with four colours, underline, comment and remove. All marks
 * are kept by an {@link AnnotationManager}, which saves them at once.
 */
public class AnnotatedPageView extends StackPane {

    private static final String TOOLBAR_STYLE = "-fx-background-color: rgba(14, 14, 19, 0.97); -fx-border-color: #c9a24b; "
            + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 6 8 6 8; "
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 14, 0.2, 0, 4);";
    private static final String FLAT_BUTTON = "-fx-background-color: transparent; -fx-text-fill: #efe8d8; -fx-padding: 3 6 3 6;";

    private final Pane markLayer = new Pane();
    private final TextFlow textFlow = new TextFlow();
    private final Pane iconLayer = new Pane();
    private final Popup toolbar = new Popup();

    private AnnotationManager manager;
    private Runnable onChanged = () -> { };
    private Font font = Font.font("Georgia", FontWeight.BOLD, 18);
    private Color textColor = Color.WHITE;
    private BiConsumer<Integer, Integer> onReadSelection = (from, to) -> { };
    private int readSentenceStart = -1;
    private int readSentenceEnd = -1;
    private int readWordStart = -1;
    private int readWordEnd = -1;
    private int page;
    private String text = "";
    private int selectionStart = -1;
    private int selectionEnd = -1;
    private int anchor;
    private boolean dragging;

    /** Creates an empty page view. Call {@link #setManager} and {@link #showPage} next. */
    public AnnotatedPageView() {
        setAlignment(Pos.TOP_LEFT);
        markLayer.setMouseTransparent(true);
        iconLayer.setPickOnBounds(false);
        textFlow.setLineSpacing(5);
        textFlow.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,1.0), 9, 0.8, 0, 1);");
        getChildren().addAll(markLayer, textFlow, iconLayer);
        toolbar.setAutoHide(true);

        textFlow.layoutBoundsProperty().addListener((observable, oldBounds, newBounds) -> Platform.runLater(this::drawMarks));
        textFlow.setOnMousePressed(this::onPressed);
        textFlow.setOnMouseDragged(this::onDragged);
        textFlow.setOnMouseReleased(this::onReleased);
    }

    /**
     * Sets the object that keeps the marks.
     *
     * @param manager the annotation manager
     */
    public void setManager(AnnotationManager manager) {
        this.manager = manager;
    }

    /**
     * Sets an action that runs after a mark was added, changed or removed (to refresh the notes list).
     *
     * @param onChanged the action
     */
    public void setOnChanged(Runnable onChanged) {
        this.onChanged = onChanged;
    }

    /**
     * Sets the action for the "Read" button of the selection toolbar.
     *
     * @param onReadSelection receives the first and last+1 character of the selected text
     */
    public void setOnReadSelection(BiConsumer<Integer, Integer> onReadSelection) {
        this.onReadSelection = onReadSelection;
    }

    /**
     * Changes how the book text looks.
     *
     * @param family      the font family, for example "Georgia"
     * @param size        the font size in points
     * @param lineSpacing the extra space between lines
     * @param bold        {@code true} for bold text
     * @param color       the text colour
     */
    public void setTextStyle(String family, double size, double lineSpacing, boolean bold, Color color) {
        this.font = Font.font(family, bold ? FontWeight.BOLD : FontWeight.NORMAL, size);
        this.textColor = color;
        textFlow.setLineSpacing(lineSpacing);
        refresh();
    }

    /**
     * Returns the text of the page that is shown.
     *
     * @return the page text
     */
    public String getText() {
        return text;
    }

    /**
     * Shows which sentence and word is being read aloud.
     *
     * @param sentenceStart first character of the sentence
     * @param sentenceEnd   position after the sentence
     * @param wordStart     first character of the word, or -1 for no word
     * @param wordEnd       position after the word
     */
    public void setReadingRange(int sentenceStart, int sentenceEnd, int wordStart, int wordEnd) {
        this.readSentenceStart = sentenceStart;
        this.readSentenceEnd = sentenceEnd;
        this.readWordStart = wordStart;
        this.readWordEnd = wordEnd;
        drawMarks();
    }

    /** Removes the reading marker. */
    public void clearReading() {
        setReadingRange(-1, -1, -1, -1);
    }

    /**
     * Shows a page.
     *
     * @param pageNumber the zero based page number, used to find the marks of the page
     * @param pageText   the text of the page
     */
    public void showPage(int pageNumber, String pageText) {
        this.page = pageNumber;
        this.text = pageText;
        toolbar.hide();
        clearSelection();
        refresh();
    }

    /**
     * Draws the page again, for example after a mark was removed from the notes list.
     */
    public void refresh() {
        rebuildText();
        Platform.runLater(this::drawMarks);
    }

    // ------------------------------------------------------------------ drawing

    private void rebuildText() {
        List<Annotation> marks = marksOnPage();
        TreeSet<Integer> cuts = new TreeSet<>();
        cuts.add(0);
        cuts.add(text.length());
        for (Annotation a : marks) {
            cuts.add(clamp(a.getStart()));
            cuts.add(clamp(a.getEnd()));
        }
        List<Integer> points = new ArrayList<>(cuts);
        List<Text> parts = new ArrayList<>();
        for (int i = 0; i < points.size() - 1; i++) {
            int from = points.get(i);
            int to = points.get(i + 1);
            if (from == to) {
                continue;
            }
            Text part = new Text(text.substring(from, to));
            part.setFill(textColor);
            part.setFont(font);
            part.setUnderline(marks.stream().anyMatch(a -> underlined(a) && a.getStart() <= from && a.getEnd() >= to));
            parts.add(part);
        }
        textFlow.getChildren().setAll(parts);
    }

    private static boolean underlined(Annotation a) {
        return a.isUnderline() || (a.hasComment() && a.getColor() == null);
    }

    private void drawMarks() {
        markLayer.getChildren().clear();
        iconLayer.getChildren().clear();
        for (Annotation a : marksOnPage()) {
            if (a.getColor() != null) {
                addShape(a.getStart(), a.getEnd(), Color.web(a.getColor().getHex(), 0.45));
            }
        }
        if (selectionStart >= 0 && selectionEnd > selectionStart) {
            addShape(selectionStart, selectionEnd, Color.web("#ffffff", 0.30));
        }
        if (readSentenceStart >= 0) {
            addShape(readSentenceStart, readSentenceEnd, Color.web("#d9b45a", 0.22));
        }
        if (readWordStart >= 0) {
            addShape(readWordStart, readWordEnd, Color.web("#d9b45a", 0.60));
        }
        for (Annotation a : marksOnPage()) {
            if (a.hasComment()) {
                addCommentIcon(a);
            }
        }
    }

    private void addShape(int from, int to, Color color) {
        int start = clamp(from);
        int end = clamp(to);
        if (end <= start) {
            return;
        }
        PathElement[] shape = textFlow.rangeShape(start, end);
        if (shape.length == 0) {
            return;
        }
        Path path = new Path(shape);
        path.setFill(color);
        path.setStroke(null);
        markLayer.getChildren().add(path);
    }

    private void addCommentIcon(Annotation a) {
        int end = clamp(a.getEnd());
        int start = Math.max(clamp(a.getStart()), end - 1);
        if (end <= start) {
            return;
        }
        PathElement[] shape = textFlow.rangeShape(start, end);
        if (shape.length == 0) {
            return;
        }
        Bounds bounds = new Path(shape).getBoundsInLocal();
        Label icon = new Label("✎");
        icon.setStyle("-fx-text-fill: #17130a; -fx-background-color: #d9b45a; -fx-background-radius: 9; "
                + "-fx-font-size: 11px; -fx-padding: 0 5 0 5;");
        icon.setCursor(Cursor.HAND);
        icon.setLayoutX(bounds.getMaxX() + 1);
        icon.setLayoutY(bounds.getMinY() - 8);
        Tooltip.install(icon, new Tooltip(a.getComment()));
        icon.setOnMouseClicked(event -> {
            event.consume();
            editComment(a);
        });
        iconLayer.getChildren().add(icon);
    }

    private List<Annotation> marksOnPage() {
        return manager == null ? List.of() : manager.onPage(page);
    }

    private int clamp(int index) {
        return Math.max(0, Math.min(text.length(), index));
    }

    // ------------------------------------------------------------------ mouse selection

    private int insertionIndexAt(MouseEvent event) {
        HitInfo hit = textFlow.hitTest(new Point2D(event.getX(), event.getY()));
        return clamp(hit.getInsertionIndex());
    }

    private void onPressed(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        toolbar.hide();
        HitInfo hit = textFlow.hitTest(new Point2D(event.getX(), event.getY()));
        anchor = clamp(hit.getInsertionIndex());
        clickedChar = hit.getCharIndex();
        dragging = false;
        clearSelection();
        drawMarks();
    }

    private int clickedChar;

    private void onDragged(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        int current = insertionIndexAt(event);
        selectionStart = Math.min(anchor, current);
        selectionEnd = Math.max(anchor, current);
        dragging = true;
        drawMarks();
    }

    private void onReleased(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        if (dragging && selectionEnd > selectionStart) {
            snapToWords();
            drawMarks();
            if (selectionEnd > selectionStart) {
                showToolbar(event.getScreenX(), event.getScreenY());
            }
        } else if (manager != null) {
            Annotation existing = manager.findAt(page, clickedChar);
            if (existing != null) {
                selectionStart = existing.getStart();
                selectionEnd = existing.getEnd();
                drawMarks();
                showToolbar(event.getScreenX(), event.getScreenY());
            }
        }
        dragging = false;
    }

    private void clearSelection() {
        selectionStart = -1;
        selectionEnd = -1;
    }

    /** Moves the selection edges to whole words and removes spaces at the ends. */
    private void snapToWords() {
        while (selectionStart > 0 && !Character.isWhitespace(text.charAt(selectionStart - 1))) {
            selectionStart--;
        }
        while (selectionEnd < text.length() && !Character.isWhitespace(text.charAt(selectionEnd))) {
            selectionEnd++;
        }
        while (selectionStart < selectionEnd && Character.isWhitespace(text.charAt(selectionStart))) {
            selectionStart++;
        }
        while (selectionEnd > selectionStart && Character.isWhitespace(text.charAt(selectionEnd - 1))) {
            selectionEnd--;
        }
    }

    // ------------------------------------------------------------------ toolbar and actions

    private void showToolbar(double screenX, double screenY) {
        if (manager == null || getScene() == null) {
            return;
        }
        HBox bar = new HBox(4);
        bar.setAlignment(Pos.CENTER);
        bar.setStyle(TOOLBAR_STYLE);

        for (HighlightColor color : HighlightColor.values()) {
            Button button = new Button();
            button.setGraphic(new Circle(8, Color.web(color.getHex())));
            button.setStyle(FLAT_BUTTON);
            button.setTooltip(new Tooltip(color.getLabel() + " highlight"));
            button.setOnAction(event -> applyColor(color));
            bar.getChildren().add(button);
        }
        Button underline = new Button("U");
        underline.setStyle(FLAT_BUTTON + " -fx-underline: true; -fx-font-weight: bold;");
        underline.setTooltip(new Tooltip("Underline"));
        underline.setOnAction(event -> toggleUnderline());

        Button comment = new Button("✎ Comment");
        comment.setStyle(FLAT_BUTTON);
        comment.setOnAction(event -> commentOnSelection());
        Button read = new Button("\u25B6 Read");
        read.setStyle(FLAT_BUTTON);
        read.setTooltip(new Tooltip("Read the selected text aloud"));
        read.setOnAction(event -> {
            toolbar.hide();
            onReadSelection.accept(selectionStart, selectionEnd);
        });
        bar.getChildren().addAll(underline, comment, read);

        if (!manager.overlapping(page, selectionStart, selectionEnd).isEmpty()) {
            Button remove = new Button("✕ Remove");
            remove.setStyle(FLAT_BUTTON + " -fx-text-fill: #ff9a9a;");
            remove.setOnAction(event -> removeMarks());
            bar.getChildren().add(remove);
        }
        toolbar.getContent().setAll(bar);
        toolbar.show(getScene().getWindow(), screenX - 40, screenY + 18);
    }

    private Annotation markForSelection() {
        return manager.getOrCreate(page, selectionStart, selectionEnd, text.substring(selectionStart, selectionEnd));
    }

    private void applyColor(HighlightColor color) {
        toolbar.hide();
        Annotation mark = markForSelection();
        mark.setColor(mark.getColor() == color ? null : color);
        manager.changed(mark);
        finishChange();
    }

    private void toggleUnderline() {
        toolbar.hide();
        Annotation mark = markForSelection();
        mark.setUnderline(!mark.isUnderline());
        manager.changed(mark);
        finishChange();
    }

    private void removeMarks() {
        toolbar.hide();
        for (Annotation mark : manager.overlapping(page, selectionStart, selectionEnd)) {
            manager.remove(mark);
        }
        finishChange();
    }

    private void commentOnSelection() {
        toolbar.hide();
        editComment(markForSelection());
    }

    private void editComment(Annotation mark) {
        Dialog<ButtonType> dialog = new Dialog<>();
        if (getScene() != null) {
            dialog.initOwner(getScene().getWindow());
        }
        dialog.setTitle("Comment");
        String quote = mark.getText().length() > 90 ? mark.getText().substring(0, 90) + "..." : mark.getText();
        dialog.setHeaderText("“" + quote + "”");

        TextArea area = new TextArea(mark.hasComment() ? mark.getComment() : "");
        area.setWrapText(true);
        area.setPrefRowCount(5);
        area.setPrefColumnCount(34);
        area.setPromptText("Write your comment...");
        StackPane content = new StackPane(area);
        content.setPadding(new Insets(6));
        dialog.getDialogPane().setContent(content);

        ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType delete = new ButtonType("Delete comment", ButtonBar.ButtonData.LEFT);
        if (mark.hasComment()) {
            dialog.getDialogPane().getButtonTypes().addAll(save, delete, ButtonType.CANCEL);
        } else {
            dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
        }
        Platform.runLater(area::requestFocus);

        Optional<ButtonType> answer = dialog.showAndWait();
        if (answer.isPresent() && answer.get() == save) {
            mark.setComment(area.getText());
        } else if (answer.isPresent() && answer.get() == delete) {
            mark.setComment(null);
        }
        manager.changed(mark);
        finishChange();
    }

    private void finishChange() {
        clearSelection();
        refresh();
        onChanged.run();
    }
}
