package com.main.datenpunk;

import database.DAO;
import enteties.TableElement;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddElementController {

    private DAO dao;

    @FXML
    public TextField nameField, typeField;

    private ObservableList<TableElement> tableReference;

    public void setDao(DAO dao){
        this.dao = dao;
    }
    public void onCancel() {

        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();


    }

    public void onAccept() {            //TODO: check not empty

        TableElement newObject = new TableElement(
                nameField.getText(),
                typeField.getText(),
                "Planned");
        tableReference.add(newObject);
        dao.insert(nameField.getText(),typeField.getText());
        onCancel();

    }

    public void setTableReference(ObservableList<TableElement> tableReference) {
        this.tableReference = tableReference;
    }
}
