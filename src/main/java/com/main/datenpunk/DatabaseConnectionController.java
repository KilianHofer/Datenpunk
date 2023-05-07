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

        DAO dao = DAO.getInstance();
        if(dao.connectToDB("Datenpunk","postgres",passwordField.getText())){
            //dao.createTables();
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage)(passwordField.getScene().getWindow());
            stage.setTitle("Datenpunk");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setResizable(true);
            stage.show();

        }

    }

    public void onCancel() {

        Stage stage = (Stage) passwordField.getScene().getWindow();
        stage.close();

    }
}
