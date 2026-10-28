package com.booklibrary.ui;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The short animated intro that plays before the main window opens.
 *
 * Author: Farid Rahmani
 * Contribution: Designed and implemented the intro animation (drawn book, title, floating gold dust).
 * Purpose: shows a golden open book that is drawn line by line, the
 * application name and a tagline for about five seconds, then calls the
 * action given to the constructor. The user can skip it with a click or a key.
 * It is built only from JavaFX shapes and a {@link Timeline}, so it needs no
 * video file and no extra libraries.
 */
public class IntroScreen {

    private static final double BOOK_PATH_LENGTH = 300;

    private final StackPane root = new StackPane();
    private final Timeline timeline = new Timeline();
    private final List<Animation> dustAnimations = new ArrayList<>();
    private final Runnable onFinished;
    private boolean finished;

    /**
     * Builds the intro.
     *
     * @param onFinished the action to run when the intro has ended or has been skipped
     */
    public IntroScreen(Runnable onFinished) {
        this.onFinished = onFinished;
        build();
    }

    /**
     * Returns the visual content to put in a scene.
     *
     * @return the root node of the intro
     */
    public Parent getRoot() {
        return root;
    }

    /**
     * Starts the animation.
     */
    public void play() {
        for (Animation animation : dustAnimations) {
            animation.play();
        }
        timeline.play();
    }

    /**
     * Ends the intro immediately and continues to the application. Does nothing if it has already ended.
     */
    public void skip() {
        if (!finished) {
            timeline.stop();
            finish();
        }
    }

    /**
     * Stops all animations and runs the "finished" action exactly once.
     */
    private void finish() {
        if (finished) {
            return;
        }
        finished = true;
        for (Animation animation : dustAnimations) {
            animation.stop();
        }
        onFinished.run();
    }

    /**
     * Creates all nodes and the timeline that animates them.
     */
    private void build() {
        root.setStyle("-fx-background-color: radial-gradient(center 50% 42%, radius 75%, #1d1c24 0%, #0b0b0f 100%);");
        root.setOpacity(0);

        Pane dust = createDust();

        SVGPath book = new SVGPath();
        book.setContent("M2 6 C8 3 16 3 24 8 C32 3 40 3 46 6 L46 38 C40 35 32 35 24 40 C16 35 8 35 2 38 Z M24 8 L24 40");
        book.setFill(Color.TRANSPARENT);
        book.setStroke(Color.web("#d9b45a"));
        book.setStrokeWidth(1.4);
        book.getStrokeDashArray().addAll(BOOK_PATH_LENGTH, BOOK_PATH_LENGTH);
        book.setStrokeDashOffset(BOOK_PATH_LENGTH);
        book.setScaleX(2.6);
        book.setScaleY(2.6);
        Group bookGroup = new Group(book);

        Label title = new Label(spaced("PRIVATE BOOK LIBRARY"));
        title.setStyle("-fx-text-fill: #d9b45a; -fx-font-family: 'Georgia'; -fx-font-size: 30px;");
        title.setOpacity(0);
        title.setTranslateY(14);

        Region rule = new Region();
        rule.setPrefSize(320, 2);
        rule.setMinSize(320, 2);
        rule.setMaxSize(320, 2);
        rule.setStyle("-fx-background-color: linear-gradient(to right, transparent, #d9b45a, transparent);");
        rule.setScaleX(0);

        Label tagline = new Label("Your collection. Beautifully kept.");
        tagline.setStyle("-fx-text-fill: #cfc6b0; -fx-font-family: 'Georgia'; -fx-font-size: 16px; -fx-font-style: italic;");
        tagline.setOpacity(0);

        VBox center = new VBox(26, bookGroup, title, rule, tagline);
        center.setAlignment(Pos.CENTER);

        Label hint = new Label("Click or press any key to skip");
        hint.setStyle("-fx-text-fill: #7d776a; -fx-font-size: 12px;");
        hint.setOpacity(0);
        StackPane.setAlignment(hint, Pos.BOTTOM_CENTER);
        StackPane.setMargin(hint, new Insets(0, 0, 24, 0));

        root.getChildren().addAll(dust, center, hint);

        Interpolator ease = Interpolator.EASE_BOTH;
        timeline.getKeyFrames().addAll(
                // the whole screen fades in
                new KeyFrame(Duration.seconds(0.0), new KeyValue(root.opacityProperty(), 0.0)),
                new KeyFrame(Duration.seconds(0.7), new KeyValue(root.opacityProperty(), 1.0, ease)),
                // the book is drawn line by line
                new KeyFrame(Duration.seconds(0.4), new KeyValue(book.strokeDashOffsetProperty(), BOOK_PATH_LENGTH),
                        new KeyValue(book.scaleXProperty(), 2.4), new KeyValue(book.scaleYProperty(), 2.4)),
                new KeyFrame(Duration.seconds(2.4), e -> book.getStrokeDashArray().clear(),
                        new KeyValue(book.strokeDashOffsetProperty(), 0.0, ease),
                        new KeyValue(book.scaleXProperty(), 2.6, ease), new KeyValue(book.scaleYProperty(), 2.6, ease)),
                // the title rises into place
                new KeyFrame(Duration.seconds(1.8), new KeyValue(title.opacityProperty(), 0.0),
                        new KeyValue(title.translateYProperty(), 14.0)),
                new KeyFrame(Duration.seconds(2.9), new KeyValue(title.opacityProperty(), 1.0, ease),
                        new KeyValue(title.translateYProperty(), 0.0, ease)),
                // the golden rule grows from the middle
                new KeyFrame(Duration.seconds(2.4), new KeyValue(rule.scaleXProperty(), 0.0)),
                new KeyFrame(Duration.seconds(3.3), new KeyValue(rule.scaleXProperty(), 1.0, ease)),
                // tagline and skip hint
                new KeyFrame(Duration.seconds(3.0), new KeyValue(tagline.opacityProperty(), 0.0)),
                new KeyFrame(Duration.seconds(3.9), new KeyValue(tagline.opacityProperty(), 1.0, ease)),
                new KeyFrame(Duration.seconds(3.2), new KeyValue(hint.opacityProperty(), 0.0)),
                new KeyFrame(Duration.seconds(4.0), new KeyValue(hint.opacityProperty(), 0.8, ease)),
                // everything fades out
                new KeyFrame(Duration.seconds(5.0), new KeyValue(root.opacityProperty(), 1.0)),
                new KeyFrame(Duration.seconds(5.7), new KeyValue(root.opacityProperty(), 0.0, ease)));
        timeline.setOnFinished(event -> finish());
    }

    /**
     * Creates slowly floating gold specks in the background.
     *
     * @return a pane with the specks; the animations are stored to be started by {@link #play()}
     */
    private Pane createDust() {
        Pane dust = new Pane();
        dust.setMouseTransparent(true);
        Random random = new Random(7);
        for (int i = 0; i < 26; i++) {
            Circle speck = new Circle(0.8 + random.nextDouble() * 1.8, Color.web("#d9b45a"));
            speck.setOpacity(0.10 + random.nextDouble() * 0.30);
            speck.setLayoutX(random.nextDouble() * 1000);
            speck.setLayoutY(60 + random.nextDouble() * 600);
            TranslateTransition drift = new TranslateTransition(Duration.seconds(5 + random.nextDouble() * 6), speck);
            drift.setByY(-(60 + random.nextDouble() * 120));
            drift.setByX(-20 + random.nextDouble() * 40);
            drift.setCycleCount(Animation.INDEFINITE);
            drift.setAutoReverse(true);
            drift.setInterpolator(Interpolator.EASE_BOTH);
            dustAnimations.add(drift);
            dust.getChildren().add(speck);
        }
        return dust;
    }

    /**
     * Spreads the letters of a text apart to give a wide, elegant look.
     *
     * @param text the text, for example "PRIVATE BOOK LIBRARY"
     * @return the text with a space between letters and three spaces between words
     */
    private static String spaced(String text) {
        StringBuilder spaced = new StringBuilder();
        for (char letter : text.toCharArray()) {
            if (letter == ' ') {
                spaced.append("   ");
            } else {
                spaced.append(letter).append(' ');
            }
        }
        return spaced.toString().trim();
    }
}
