package com.main.datenpunk;

import database.DAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class DatabaseConnectionController {
        @FXML
    public PasswordField passwordField;
    public CheckBox remenberMe;
    private String name;
    private Stage returnStage;
    private boolean newProject = false;
    private boolean deletion = false;

    public void onConnect() throws IOException {

        DAO dao = DAO.getInstance();
        Singleton singleton = Singleton.getInstance();

        if(deletion){
            dao.connectToDB("","postgres",passwordField.getText());
            dao.dropDatabase("");
            checkRememberMe();
            onCancel();
            return;
        }

        if(dao.connectToDB("","postgres",passwordField.getText()) && newProject) {
            dao.createDatabase(name);
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Could not connect to Database");
            alert.show();
            return;
        }
        if (dao.connectToDB("datenpunk_" + name, "postgres", passwordField.getText())) {
            if(newProject)
                dao.createTables();         //TODO: create dynamic database!!!

            checkRememberMe();

            if(singleton.getColumns() != null)
                singleton.getColumns().clear();
            if(singleton.getColumnInfo() != null)
                singleton.getColumnInfo().clear();
            singleton.choices.clear();
            singleton.choiceNames.clear();

            singleton.setCurrentProject(name);
            singleton.setColumnInfo();

            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/main/datenpunk/application.css")).toExternalForm());
            Stage stage = returnStage;
            stage.setTitle("Datenpunk");
            stage.setScene(scene);
            MainController controller = fxmlLoader.getController();
            singleton.setController(controller);
            stage.setMaximized(true);
            stage.setResizable(true);
            onCancel();
            System.out.println("test");
            stage.show();
            controller.initializeCellFactories();
        }
        else {
            passwordField.setStyle("-fx-border-color: red; -fx-border-width: 2px");
        }

    }

    private void checkRememberMe() {
        if (remenberMe.isSelected()) {
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
    public void setDeletion(boolean deletion) {
        this.deletion = deletion;
    }
}
