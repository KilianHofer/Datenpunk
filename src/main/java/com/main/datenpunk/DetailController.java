package com.main.datenpunk;

import database.DAO;
import enteties.HistoryElement;
import enteties.TableElement;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;


public class DetailController implements Initializable {

    private DAO dao;
    private TableElement currentElement;

    @FXML
    public TextField nameField,typeField,statusField;

    ObservableList<HistoryElement> historyElements;
    @FXML
    public TableView<HistoryElement> historyTable;

    @FXML
    public TableColumn<HistoryElement,String> statusColumn, dateColumn;



    public void updateTable(){
        historyElements = dao.selectHistory(currentElement.getId());
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        historyTable.setItems(historyElements);
    }

    public void setCurrentElement(TableElement element){
        currentElement = element;
        nameField.setText(currentElement.getName());
        typeField.setText(currentElement.getType());
        statusField.setText(currentElement.getStatus());
        updateTable();
    }

    public void onUpdatePressed() {

        String name = nameField.getText();
        String type = typeField.getText();

        if(!Objects.equals(name, currentElement.getName()) || !Objects.equals(type, currentElement.getType())) {
            currentElement.setName(name);
            currentElement.setType(type);
            dao.updateValues(currentElement.getId(),name,type);
        }

        if(!Objects.equals(statusField.getText(), currentElement.getStatus())) {
            currentElement.setStatus(statusField.getText());
            dao.updateHistory(currentElement.getId(), statusField.getText());
            updateTable();
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dao = DAO.getInstance();
    }
}
