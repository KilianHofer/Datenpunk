package com.main.datenpunk;

import database.DAO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DatabaseConnectionController{
        @FXML
    public PasswordField passwordField;
    public CheckBox rememberMe;

    public Runnable method;
    public Runnable cancelMethod;

    public void onConnect(){

        DAO dao = DAO.getInstance();
        Singleton singleton = Singleton.getInstance();

        if(dao.connectToDB("","postgres",passwordField.getText())){
            checkRememberMe();
            singleton.setPassword(passwordField.getText());
            method.run();
            Stage stage = (Stage) passwordField.getScene().getWindow();
            stage.close();
        }
        else {
            passwordField.setStyle("-fx-border-color: red; -fx-border-width: 2px");
        }
    }



    private void checkRememberMe() {
        if (rememberMe.isSelected()) {
            File file = new File(System.getProperty("user.home") + "\\Datenpunk\\connection.dtpnk");
            try {
                if (!file.exists()) {
                    if(!file.createNewFile()){
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("Could not save password");
                        alert.showAndWait();
                    }
                }
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write(passwordField.getText());
                writer.close();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    public void onCancel() {

        if(cancelMethod != null)
            cancelMethod.run();
        Stage stage = (Stage) passwordField.getScene().getWindow();
        stage.close();

    }
}
