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
    public TextField nameField,pathField;


    @FXML
    public void onCreate() throws IOException {

        String name = nameField.getText();
        String path = pathField.getText();
        File file = new File(path);
        if(!name.equals("") && !path.equals("")) {
            if (!file.exists()) {
                Files.createDirectory(file.toPath());
            }
            path += "\\" + name + ".dtpnkl";
            file = new File(path);
            if (!file.exists()) {
                try {
                    if(!file.createNewFile()){
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("could not create file");
                        alert.show();
                        return;
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

    private void connectToDatabase() {
        String name = nameField.getText();
        File file = new File(System.getProperty("user.home")+"\\Datenpunk\\connection.dtpnk");
        try {
            if (file.exists()) {
                Scanner scanner = new Scanner(file);
                if(scanner.hasNext()){
                    String password = scanner.next();
                    if(dao.connectToDB("","postgres",password)){
                        dao.createDatabase(name);
                    }
                    if(dao.connectToDB("datenpunk_"+name,"postgres",password)){
                        dao.createTables();
                        singelton.setCurrentProject(name);
                        singelton.setColumnInfo();
                        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
                        Stage stage = returnStage;
                        stage.setTitle("Datenpunk");
                        stage.setScene( new Scene(fxmlLoader.load()));
                        stage.setMaximized(true);
                        stage.setResizable(true);
                        stage.show();

                        stage = (Stage) nameField.getScene().getWindow();
                        stage.close();
                        return;

                    }
                }
            }
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("databaseConnection-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());


            Stage stage = (Stage) nameField.getScene().getWindow();

            stage.setTitle("Connect to Database");
            stage.setScene(scene);

            DatabaseConnectionController databaseConnectionController = fxmlLoader.getController();
            databaseConnectionController.setName(name);      //TODO: better data transfer
            databaseConnectionController.setRetrunStage(returnStage);
            databaseConnectionController.setNew(true);
            stage.setResizable(false);
            stage.show();
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
