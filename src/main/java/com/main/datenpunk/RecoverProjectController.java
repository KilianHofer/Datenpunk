package com.main.datenpunk;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class RecoverProjectController {

    @FXML
    public TextField pathField;
    @FXML
    public Button actionButton;
    private boolean reconnect = true;

    private ProjectSelectionController controller;
    String path, originalPath;
    String name;
    String type;

    Singleton singleton = Singleton.getInstance();

    boolean error = false;

    public void  setController(ProjectSelectionController controller){
        this.controller = controller;
    }

    public void setPath(String path){
        name = path.substring(path.lastIndexOf("\\")+1);
        type = path.substring(path.lastIndexOf("."));
        this.path = path.substring(0,path.lastIndexOf("\\")+1);
        originalPath = path;
        pathField.setText(this.path);
    }

    public void setReconnect(boolean reconnect){
        this.reconnect = reconnect;
        if(reconnect){
            actionButton.setText("Reconnect");
        }
        else{
            actionButton.setText("Reconstruct");
        }

    }

    public void onOpen() {
        if(reconnect){
            FileChooser fileChooser =new FileChooser();
            fileChooser.setInitialDirectory(new File(path));
            String tmpPath = String.valueOf(fileChooser.showOpenDialog(actionButton.getScene().getWindow()));
            String subPath = tmpPath.substring(tmpPath.lastIndexOf("\\")+1);
            System.out.println(subPath);
            if(subPath.equals(name)){
                path = tmpPath;
            }
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Invalid File!");
                alert.showAndWait();
            }

        }else {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File(path));
            directoryChooser.setInitialDirectory(new File(path.substring(0,path.lastIndexOf("\\"))));

            path = String.valueOf(directoryChooser.showDialog(actionButton.getScene().getWindow()));
        }
        pathField.setText(path);
    }

    public void onAction(){
        File file = new File(path);
        if(file.exists()){
            if(reconnect && file.isFile()){
                correctProjectList(file);
            }
            else if(!reconnect && file.isDirectory()){
                file = new File(file.getAbsolutePath()+"\\"+name);
                correctProjectList(file);
            }
        }
    }

    private void prefabRecoveryError(){
        if (!error) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Prefabs could not be recovered.");
            alert.showAndWait();
        }
    }

    private void correctProjectList(File file){

        try {
        if(!file.exists())
            Files.createFile(file.toPath());
        String folderPath = System.getProperty("user.home")+"\\Datenpunk\\Projects\\"+name.substring(0,name.lastIndexOf("."));
        file = new File(folderPath);
            if (!file.exists()) {
                Files.createDirectory(file.toPath());
                prefabRecoveryError();
            }
            file = new File(folderPath+"\\Presets");
            if(!file.exists()){
                Files.createDirectory(file.toPath());
                prefabRecoveryError();
            }

            singleton.removeFromProjectsFile(originalPath);

            File projectsFile = new File(System.getProperty("user.home") + "\\Datenpunk\\projects.dtpnk");
            FileWriter fileWriter = new FileWriter(projectsFile, true);
            BufferedWriter writer = new BufferedWriter(fileWriter);
            if (!reconnect)
                path += name;
            writer.append(path).append("\n");
            writer.close();
            fileWriter.close();

            controller.getProjects();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.show();
        }

        ((Stage)actionButton.getScene().getWindow()).close();

    }

}
