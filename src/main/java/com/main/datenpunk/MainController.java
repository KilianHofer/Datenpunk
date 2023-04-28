package com.main.datenpunk;

import enteties.TableElement;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private TableView<TableElement> objectTable;

    @FXML
    private TableColumn<TableElement, StringProperty> nameColumn;
    @FXML
    private TableColumn<TableElement, StringProperty> typeColumn;
    @FXML
    private TableColumn<TableElement, StringProperty> statusColumn;

    ObservableList<TableElement> tableElements = FXCollections.observableArrayList(

            new TableElement("Barrel","Clutter","Planned"),
            new TableElement("Fish","Food","Complete"),
            new TableElement("Tree","Vegetation","Planned"),
            new TableElement("House","Building","Planned"),
            new TableElement("Book","Clutter","Complete"),
            new TableElement("Mug","Clutter","Complete"),
            new TableElement("Chest","Container","Complete"),
            new TableElement("Sword","Weapon","Planned")
    );

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));


        objectTable.setItems(tableElements);



    }


}