package com.booklibrary;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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

    /**
     * JavaFX lifecycle method that loads main.fxml, builds the primary
     * scene and shows the application's main window.
     *
     * @param primaryStage the primary stage provided by the JavaFX runtime
     * @throws Exception if the FXML view cannot be loaded
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Private Book Library Manager");
        primaryStage.setScene(new Scene(root, 900, 600));
        primaryStage.show();
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
