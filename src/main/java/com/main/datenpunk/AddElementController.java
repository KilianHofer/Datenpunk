package com.main.datenpunk;

import database.DAO;
import enteties.TableElement;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AddElementController implements Initializable {

    private DAO dao;

    @FXML
    public TextField nameField, typeField;

    private ObservableList<TableElement> tableReference;

    public void onCancel() {

        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();


    }

    public void onAccept() {            //TODO: check not empty

        String name = nameField.getText();
        String type = typeField.getText();

        if(!name.isEmpty() && !type.isEmpty()) {
            int id = dao.insert(nameField.getText(), typeField.getText());

            TableElement newObject = new TableElement(
                    id,
                    name,
                    type,
                    "Planned");
            tableReference.add(newObject);
            onCancel();
        }

    }

    public void setTableReference(ObservableList<TableElement> tableReference) {
        this.tableReference = tableReference;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dao = DAO.getInstance();
    }
}
