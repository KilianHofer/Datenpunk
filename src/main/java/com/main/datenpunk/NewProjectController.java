package com.main.datenpunk;

import database.DAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class NewProjectController {

    @FXML
    public TextField nameField,pathField;
    DAO dao = DAO.getInstance();

    @FXML
    public void onCreate() {

        String name = nameField.getText();
        String path = pathField.getText();
        File file = new File(path);
        if(name != "" && path != "") {
            if (!file.exists()) {
                file.mkdir();
            }
            file = new File(path + "/" + name + ".dtpnk");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    //dao.createDatabase(name);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
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
}
