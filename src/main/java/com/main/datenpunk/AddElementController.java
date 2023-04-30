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


        int id = dao.insert(nameField.getText(),typeField.getText());

        TableElement newObject = new TableElement(
                id,
                nameField.getText(),
                typeField.getText(),
                "Planned");
        tableReference.add(newObject);
        onCancel();

    }

    public void setTableReference(ObservableList<TableElement> tableReference) {
        this.tableReference = tableReference;
    }
}
