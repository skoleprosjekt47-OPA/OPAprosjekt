package com.booklibrary.ui;

import javafx.collections.ListChangeListener;
import javafx.scene.Scene;
import javafx.stage.Window;

import java.util.Objects;

/**
 * Applies the dark luxury style sheet to every window of the application.
 *
 * Author: Farid Rahmani
 * Contribution: Designed the central theme handling so every window and dialog gets the same look.
 * Purpose: instead of adding the style sheet in every controller, this class
 * watches all JavaFX windows (main window, form dialogs, alerts) and adds
 * theme.css to their scenes automatically. To change the look of the whole
 * application, only theme.css has to be edited.
 */
public final class ThemeManager {

    private static final String STYLESHEET = Objects
            .requireNonNull(ThemeManager.class.getResource("/css/theme.css"), "theme.css not found")
            .toExternalForm();

    private static boolean installed;

    private ThemeManager() {
    }

    /**
     * Starts watching all windows. Safe to call more than once.
     */
    public static void install() {
        if (installed) {
            return;
        }
        installed = true;
        for (Window window : Window.getWindows()) {
            watch(window);
        }
        Window.getWindows().addListener((ListChangeListener<Window>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Window window : change.getAddedSubList()) {
                        watch(window);
                    }
                }
            }
        });
    }

    /**
     * Applies the style sheet to a window's current scene and to every scene it gets later.
     *
     * @param window the window to watch
     */
    private static void watch(Window window) {
        apply(window.getScene());
        window.sceneProperty().addListener((observable, oldScene, newScene) -> apply(newScene));
    }

    /**
     * Adds the style sheet to a scene unless it already has it.
     *
     * @param scene the scene, may be {@code null}
     */
    private static void apply(Scene scene) {
        if (scene != null && !scene.getStylesheets().contains(STYLESHEET)) {
            scene.getStylesheets().add(STYLESHEET);
        }
    }
}
