package com.main.datenpunk;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("projectSelection-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Datenpunk");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.getIcons().add(new Image(Objects.requireNonNull(MainApplication.class.getResource("Datenpunk.png")).openStream()));
        ProjectSelectionController controller = fxmlLoader.getController();
        stage.show();
        controller.initializeTable();

    }

    public static void main(String[] args) {
        launch();
    }
}