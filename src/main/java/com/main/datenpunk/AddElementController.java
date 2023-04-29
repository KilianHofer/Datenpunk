package com.main.datenpunk;

import enteties.TableElement;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddElementController {

    @FXML
    public TextField nameField, typeField;

    private ObservableList<TableElement> tableReference;
    public void onCancel() {

        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();


    }

    public void onAccept() {

        TableElement newObject = new TableElement(
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
