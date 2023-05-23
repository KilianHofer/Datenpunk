package com.main.datenpunk;


import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class FileNotFoundController implements Initializable{

    public Label errorLabel,optionsLabel;

    ProjectSelectionController projectSelectionController;
    String name, path;

    public void setProjectSelectionController(ProjectSelectionController controller) {
        projectSelectionController =  controller;
    }
    public void setErrorMessage(String message){
        errorLabel.setText(message);
        name = message.substring(message.indexOf("'")+1,message.indexOf("."));
        path = message.substring(message.indexOf(":")+2);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        optionsLabel.setText("""
                If the file has been deleted you can delete the rest of the project.
                If the file has been moved you can reconnect it with the new path.
                If you want to keep the project as is you can reconstruct the file.""");
    }


    public void onDelete() throws IOException {
        System.out.println(name);
        projectSelectionController.deleteProject(name,path);
        onClose();
    }
    public void onReconnect() throws IOException {
        onRecover(true);
    }
    public void onReconstruct() throws IOException {
        onRecover(false);
    }

    private void onRecover(boolean reconnect) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("recoverProject-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());


        Stage stage = (Stage) errorLabel.getScene().getWindow();

        stage.setTitle("Connect to Database");
        stage.setScene(scene);

        RecoverProjectController controller = fxmlLoader.getController();
        controller.setPath(path);      //TODO: better data transfer
        controller.setReconnect(reconnect);
        controller.setController(projectSelectionController);
        stage.setResizable(false);
        stage.show();
    }

    private void onClose(){
        ((Stage)errorLabel.getScene().getWindow()).close();
    }
}
