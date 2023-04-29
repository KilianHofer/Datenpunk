package com.main.datenpunk;

import database.DAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.io.IOException;

public class DatabaseConnectionController {

    @FXML
    public PasswordField passwordField;

    public void onConnect() throws IOException {

        DAO dao = new DAO();
        if(dao.connectToDB("Datenpunk","postgres",passwordField.getText())){
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage)(passwordField.getScene().getWindow());
            stage.setTitle("Datenpunk");
            stage.setScene(scene);

            MainController mainController = fxmlLoader.getController();
            mainController.setDAO(dao);     //TODO: Better data transfer

            stage.show();
        }

    }

    public void onCancel() {

        Stage stage = (Stage) passwordField.getScene().getWindow();
        stage.close();

    }

    public void checkEnter() throws IOException {

        onConnect();

    }
}
