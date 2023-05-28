package com.main.datenpunk;

import database.DAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AddElementController implements Initializable {

    private DAO dao;
    Singelton singelton = Singelton.getInstance();

    @FXML
    public TextField nameField, typeField;


    public void onCancel() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    public void onAccept() {

        String name = nameField.getText();
        String type = typeField.getText();

        if(!name.isEmpty() && !type.isEmpty()) {
            dao.insert(nameField.getText(), typeField.getText());

            singelton.getController().updateTable();
            onCancel();
        }

    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dao = DAO.getInstance();
    }
}
