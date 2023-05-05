package com.main.datenpunk;

import database.DAO;
import enteties.Status;
import enteties.ObjectTableElement;
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

    private ObservableList<ObjectTableElement> tableReference;

    public void onCancel() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    public void onAccept() {

        String name = nameField.getText();
        String type = typeField.getText();

        if(!name.isEmpty() && !type.isEmpty()) {
            int id = dao.insert(nameField.getText(), typeField.getText());

            Status status = dao.selectStatus("Planned");


            ObjectTableElement newObject = new ObjectTableElement(
                    id,
                    name,
                    type,
                    status,
                    dao.format.format(System.currentTimeMillis()));
            tableReference.add(newObject);
            onCancel();
        }

    }

    public void setTableReference(ObservableList<ObjectTableElement> tableReference) {
        this.tableReference = tableReference;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dao = DAO.getInstance();
    }
}
