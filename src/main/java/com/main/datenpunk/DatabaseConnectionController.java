package com.main.datenpunk;

import database.DAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DatabaseConnectionController {
        @FXML
    public PasswordField passwordField;
    public CheckBox remenberMe;
    private String name;
    private Stage returnStage;
    private boolean newProject;

    public void onConnect() throws IOException {

        DAO dao = DAO.getInstance();

        if(dao.connectToDB("","postgres",passwordField.getText()) && newProject) {
            dao.createDatabase(name);
        }
        if (dao.connectToDB("datenpunk_" + name, "postgres", passwordField.getText())) {
            if(newProject)
                dao.createTables();

            if (remenberMe.isSelected()) {
                File file = new File(System.getProperty("user.home") + "\\Datenpunk\\connection.dtpnk");
                if (!file.exists()) {
                    file.createNewFile();
                }
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write(passwordField.getText());
                writer.close();
            }


            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = returnStage;
            stage.setTitle("Datenpunk");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setResizable(true);
            onCancel();
            System.out.println("test");
            stage.show();
        }

    }

    public void onCancel() {

        Stage stage = (Stage) passwordField.getScene().getWindow();
        stage.close();

    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRetrunStage(Stage returnStage) {
        this.returnStage = returnStage;
    }

    public void setNew(boolean newProject) {
        this.newProject = newProject;
    }
}
