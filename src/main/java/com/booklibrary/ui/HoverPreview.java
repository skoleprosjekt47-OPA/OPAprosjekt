package com.booklibrary.ui;

import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Shows a small card with cover, short description and details when the mouse rests on a book.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented the hover card (cover and short introduction) for book lists.
 * Purpose: attach it to a table row or list cell. After the mouse has rested on
 * the item for a moment, the details are fetched on a background thread and
 * shown in a popup. Results are remembered, so hovering the same book again is instant.
 *
 * @param <T> the type of the items in the list, for example {@code Book}
 */
public class HoverPreview<T> {

    /**
     * The information shown in the card.
     *
     * @param title       the book title
     * @param byline      the author and year line
     * @param description a short introduction; may be empty
     * @param coverUrl    the address of the cover picture; may be {@code null}
     */
    public record Data(String title, String byline, String description, String coverUrl) { }

    private static final Duration DELAY = Duration.millis(450);

    private final Function<T, Data> loader;
    private final Function<T, String> keyOf;
    private final Map<String, Data> cache = new ConcurrentHashMap<>();
    private final Popup popup = new Popup();
    private int ticket;

    /**
     * Creates a preview helper.
     *
     * @param loader finds the information for an item; runs on a background thread, may be slow
     * @param keyOf  gives a unique text for an item, used to remember the result
     */
    public HoverPreview(Function<T, Data> loader, Function<T, String> keyOf) {
        this.loader = loader;
        this.keyOf = keyOf;
        popup.setAutoHide(false);
    }

    /**
     * Makes a node (for example a table row) show the card when hovered.
     *
     * @param node the node to watch
     * @param item gives the item currently shown by the node, or {@code null} if the node is empty
     */
    public void attach(Node node, Supplier<T> item) {
        PauseTransition wait = new PauseTransition(DELAY);
        node.setOnMouseEntered(event -> {
            T value = item.get();
            if (value == null) {
                return;
            }
            double x = event.getScreenX() + 18;
            double y = event.getScreenY() + 18;
            wait.setOnFinished(done -> load(node, value, x, y));
            wait.playFromStart();
        });
        node.setOnMouseExited(event -> cancel(wait));
        node.setOnMousePressed(event -> cancel(wait));
    }

    private void cancel(PauseTransition wait) {
        wait.stop();
        ticket++;
        popup.hide();
    }

    private void load(Node node, T value, double x, double y) {
        int myTicket = ++ticket;
        String key = keyOf.apply(value);
        Data cached = cache.get(key);
        if (cached != null) {
            show(node, cached, x, y);
            return;
        }
        Task<Data> task = new Task<>() {
            @Override
            protected Data call() {
                return loader.apply(value);
            }
        };
        task.setOnSucceeded(event -> {
            Data data = task.getValue();
            if (data != null) {
                cache.put(key, data);
                if (myTicket == ticket) {
                    show(node, data, x, y);
                }
            }
        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void show(Node node, Data data, double x, double y) {
        if (node.getScene() == null || node.getScene().getWindow() == null) {
            return;
        }
        VBox text = new VBox(5);
        text.setPrefWidth(270);
        Label title = new Label(data.title());
        title.setWrapText(true);
        title.setStyle("-fx-text-fill: #d9b45a; -fx-font-family: 'Georgia'; -fx-font-size: 15px; -fx-font-weight: bold;");
        Label byline = new Label(data.byline());
        byline.setWrapText(true);
        byline.setStyle("-fx-text-fill: #9a927f; -fx-font-size: 12px;");
        Label description = new Label(data.description().isBlank()
                ? "No description found for this book." : data.description());
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: #efe8d8; -fx-font-size: 12px;");
        text.getChildren().addAll(title, byline, description);

        HBox card = new HBox(14);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color: rgba(14, 14, 19, 0.97); -fx-border-color: #c9a24b; "
                + "-fx-border-radius: 8; -fx-background-radius: 8; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 20, 0.2, 0, 6);");
        if (data.coverUrl() != null && !data.coverUrl().isBlank()) {
            ImageView cover = new ImageView(new Image(data.coverUrl(), 130, 195, true, true, true));
            cover.setPreserveRatio(true);
            cover.setFitWidth(130);
            cover.setFitHeight(195);
            card.getChildren().add(cover);
        }
        card.getChildren().add(text);
        popup.getContent().setAll(card);
        popup.show(node.getScene().getWindow(), x, y);
    }
}
