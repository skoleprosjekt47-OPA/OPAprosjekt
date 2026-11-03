package com.booklibrary;

import com.booklibrary.ui.GuideAssistant;
import com.booklibrary.ui.IntroScreen;
import com.booklibrary.ui.ThemeManager;
import com.booklibrary.ui.VideoIntro;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

/**
 * Application entry point for the Private Book Library Manager.
 *
 * Author: Farid Rahmani
 * Contribution: Implemented the JavaFX application entry point that loads the main view and starts the program.
 * Purpose: starts JavaFX, loads the main view (main.fxml) and shows the
 * primary window. Contains no business logic - it only wires the first
 * screen together.
 */
public class MainApp extends Application {

    private static HostServices hostServices;

    /**
     * JavaFX lifecycle method. Installs the dark theme, plays the short intro
     * and then loads main.fxml and shows the main window. The intro can be
     * skipped with a click or a key, or switched off with the Java option
     * {@code -Dbooklibrary.skipIntro=true}.
     *
     * @param primaryStage the primary stage provided by the JavaFX runtime
     * @throws Exception if the FXML view cannot be loaded
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        hostServices = getHostServices();
        ThemeManager.install();

        primaryStage.setTitle("Private Book Library Manager");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);

        if (System.getProperty("booklibrary.skipIntro") != null) {
            showMainWindow(primaryStage, false);
            primaryStage.show();
            return;
        }

        Runnable toMain = () -> showMainWindow(primaryStage, true);
        Runnable skip;
        Runnable play;
        Parent introRoot;

        URL video = getClass().getResource("/video/intro.mp4");
        VideoIntro videoIntro = null;
        if (video != null) {
            try {
                videoIntro = new VideoIntro(video.toExternalForm(), toMain);
            } catch (RuntimeException e) {
                videoIntro = null; // video could not be opened: use the animated intro instead
            }
        }
        if (videoIntro != null) {
            VideoIntro v = videoIntro;
            introRoot = v.getRoot();
            skip = v::skip;
            play = v::play;
        } else {
            IntroScreen i = new IntroScreen(toMain);
            introRoot = i.getRoot();
            skip = i::skip;
            play = i::play;
        }

        Scene introScene = new Scene(introRoot, 1000, 650, Color.BLACK);
        introScene.setOnMouseClicked(event -> skip.run());
        introScene.setOnKeyPressed(event -> skip.run());
        primaryStage.setScene(introScene);
        primaryStage.show();
        play.run();
    }

    /**
     * Loads the main view and shows it in the given stage, optionally with a soft fade-in.
     *
     * @param stage  the stage to show the main window in
     * @param fadeIn {@code true} to fade the window in
     */
    private void showMainWindow(Stage stage, boolean fadeIn) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main.fxml"));
            Parent root = loader.load();
            StackPane host = new StackPane(root);
            GuideAssistant guide = createGuide(root, host);
            Scene scene = new Scene(host, 1000, 650, Color.web("#0e0e13"));
            stage.setScene(scene);
            if (guide != null) {
                stage.setOnHidden(event -> guide.shutdown());
                if (System.getProperty("booklibrary.skipGuide") == null) {
                    guide.startIfEnabled();
                }
            }
            if (fadeIn) {
                root.setOpacity(0);
                FadeTransition fade = new FadeTransition(Duration.millis(600), root);
                fade.setToValue(1);
                fade.play();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not load the main window", e);
        }
    }

    /**
     * Creates the assistant that guides new users. If it cannot be created (for example the picture is
     * missing) the application simply starts without it.
     *
     * @param root the main view, which must be the tab pane
     * @param host the stack that will hold the main view and the assistant
     * @return the assistant, or {@code null} if it could not be created
     */
    private GuideAssistant createGuide(Parent root, StackPane host) {
        if (root instanceof TabPane tabs) {
            try {
                return new GuideAssistant(tabs, host);
            } catch (RuntimeException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Opens a web page in the user's default browser. Used by the Discover tab.
     *
     * @param url the address to open; ignored if {@code null}
     */
    public static void openInBrowser(String url) {
        if (hostServices != null && url != null) {
            hostServices.showDocument(url);
        }
    }

    /**
     * Standard main method. Needed in addition to {@link #start(Stage)} so
     * the application can also be launched as a plain runnable jar (not
     * only through the javafx-maven-plugin), where the JavaFX runtime
     * would otherwise not be found on the module path.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
