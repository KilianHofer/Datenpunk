package com.main.datenpunk;

import database.DAO;
import enteties.ColoredHistoryTableCell;
import enteties.HistoryTableElement;
import enteties.Status;
import enteties.ObjectTableElement;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;


public class DetailController implements Initializable {

    private DAO dao;
    private ObjectTableElement currentElement;

    @FXML
    private TextField nameField,typeField;
    @FXML
    private ChoiceBox<String> statusBox;

    ObservableList<HistoryTableElement> historyTableElements;
    @FXML
    private TableView<HistoryTableElement> historyTable;

    @FXML
    private TableColumn<HistoryTableElement,String> statusColumn, dateColumn;



    public void updateTable(){                              //update main table automatically
        historyTableElements = dao.selectHistory(currentElement.getId());
        historyTable.setItems(historyTableElements);

    }

    public void setCurrentElement(int id){
        currentElement = dao.selectElement(id);
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
        statusColumn.setCellFactory(factory -> new ColoredHistoryTableCell());
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        statusBox.getItems().addAll(statusNames);



    }

    public void onDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText("Delete this Object?");
        if(alert.showAndWait().get() == ButtonType.OK){
            dao.deleteObject(currentElement.getId());
            ((Stage)nameField.getScene().getWindow()).close();
        }

    }
}
