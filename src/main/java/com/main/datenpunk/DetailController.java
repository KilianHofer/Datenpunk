package com.main.datenpunk;

import database.DAO;
import enteties.HistoryElement;
import enteties.TableElement;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;


public class DetailController{

    private DAO dao;
    private TableElement currentElement;

    @FXML
    public TextField nameField,typeField,statusField;

    ObservableList<HistoryElement> historyElements;
    @FXML
    public TableView<HistoryElement> historyTable;

    @FXML
    public TableColumn<HistoryElement,String> statusColumn, dateColumn;


    public void setDao(DAO dao){
        this.dao = dao;
        historyElements = dao.selectHistory(8);
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        historyTable.setItems(historyElements);
    }

    public void setCurrentElement(TableElement element){
        currentElement = element;
        nameField.setText(currentElement.getName());
        typeField.setText(currentElement.getType());
        statusField.setText(currentElement.getStatus());
    }

    public void onUpdatePressed() {
        currentElement.setName(nameField.getText());        //TODO: check for changes
        currentElement.setType(typeField.getText());
        currentElement.setStatus(statusField.getText());
        dao.update(8,statusField.getText());           //TODO: get index of object


    }
}
