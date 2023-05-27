package com.main.datenpunk;

import database.DAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;
import java.util.Scanner;

public class NewProjectController implements Initializable {



    DAO dao = DAO.getInstance();
    Singelton singelton = Singelton.getInstance();

    Stage returnStage;
    @FXML
    public TextField nameField,hostField,pathField;


    @FXML
    public void onCreate() throws IOException {

        String name = nameField.getText();
        String path = pathField.getText();
        File file = new File(path);
        if(!name.equals("") && !path.equals("")) {
            if (!file.exists()) {
                Files.createDirectory(file.toPath());
            }

            String ending;
            boolean local;

            if(hostField.getText().equals("localhost") || hostField.getText().matches("0?127.0{1,4}.0{1,4}.0{0,3}1")) {
                ending = ".dtpnkl";
                local = true;
            }
            else{
                ending = ".dtpnkr";
                local = false;
            }


            path += "\\" + name + ending;
            file = new File(path);
            if (!file.exists()) {
                try {
                    if(!file.createNewFile()){
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("could not create file");
                        alert.show();
                        return;
                    }

                    if(!local){
                        //TODO: write host into project file
                    }

                    File projectFile = new File(System.getProperty("user.home")+"\\Datenpunk\\projects.dtpnk");
                    BufferedWriter writer = new BufferedWriter(new FileWriter(projectFile,true));
                    writer.append(path).append("\n");
                    writer.close();


                    path = System.getProperty("user.home")+"\\Datenpunk\\Projects";
                    path += "\\" + name;
                    file = new File(path);
                    Files.createDirectory(file.toPath());
                    file = new File(path+"\\Presets");
                    Files.createDirectory(file.toPath());
                    connectToDatabase();

                } catch (IOException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            }
            else{
                Alert alert = new Alert((Alert.AlertType.ERROR));
                alert.setContentText("A project with this name already exists in this directory!");
                alert.showAndWait();
            }
        }
    }

    private void connectToDatabase(){
        String name = nameField.getText();

        try {
            if(singelton.checkSavedPasswordAndConnect("")){
                dao.createDatabase(name);
                singelton.checkSavedPasswordAndConnect(name);
                dao.createTables();
                singelton.openProjectWindow(name);
                ((Stage) nameField.getScene().getWindow()).close();
            }
            else {
                singelton.openDatabaseConnectionWindow((Stage) nameField.getScene().getWindow(),name,true,false);
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }

    }

    @FXML
    public void onCancel() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void onSelect() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File file = new File(pathField.getText());
        if(file.exists()){
            directoryChooser.setInitialDirectory(file);
        }
        String directory = String.valueOf(directoryChooser.showDialog(pathField.getScene().getWindow()));
        pathField.setText(directory);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pathField.setText(System.getProperty("user.home")+"\\Datenpunk\\Projects");
    }

    public void setReturnStage(Stage stage) {
        returnStage = stage;
    }
}
