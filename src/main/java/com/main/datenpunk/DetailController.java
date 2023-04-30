package com.main.datenpunk;

import database.DAO;
import enteties.HistoryElement;
import enteties.Status;
import enteties.TableElement;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;


public class DetailController implements Initializable {

    private DAO dao;
    private TableElement currentElement;

    @FXML
    private TextField nameField,typeField;
    @FXML
    private ChoiceBox<String> statusBox;

    ObservableList<HistoryElement> historyElements;
    @FXML
    private TableView<HistoryElement> historyTable;

    @FXML
    private TableColumn<HistoryElement,String> statusColumn, dateColumn;



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
        statusBox.setValue(currentElement.getStatus());
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

        if(!Objects.equals(statusBox.getValue(), currentElement.getStatus())) {
            currentElement.setStatus(statusBox.getValue());
            dao.updateHistory(currentElement.getId(), statusBox.getValue());
            updateTable();
        }



    }
    List<Status> statuses;
    List<String> statusNames = new ArrayList<>();
    List<Integer>   statusSortOrder = new ArrayList<>();

    private void getStatuses(){
        statuses = dao.selectStatuses();
        Status status;
        for (Status value : statuses) {
            status = value;
            statusNames.add(status.getName());
            statusSortOrder.add(status.getSortOrder());
        }
    }



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dao = DAO.getInstance();
        getStatuses();
        statusBox.getItems().addAll(statusNames);



    }
}
