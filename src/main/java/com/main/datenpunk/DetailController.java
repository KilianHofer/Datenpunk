package com.main.datenpunk;

import database.DAO;
import enteties.TableElement;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class DetailController implements Initializable {

    private DAO dao;
    private TableElement currentElement;

    public void setDao(DAO dao){
        this.dao = dao;
    }

    @FXML
    public TextField nameField,typeField,statusField;

    @FXML
    public TableView historyTable;

    @FXML
    public TableColumn statusColumn, dateColumn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void setCurrentElement(TableElement element){
        currentElement = element;
        nameField.setText(currentElement.getName());
        typeField.setText(currentElement.getType());
        statusField.setText(currentElement.getStatus());
    }

    public void onUpdatePressed() {
        currentElement.setName(nameField.getText());
        currentElement.setType(typeField.getText());
        currentElement.setStatus(statusField.getText());
    }
}
