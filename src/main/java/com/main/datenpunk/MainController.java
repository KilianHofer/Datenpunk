package com.main.datenpunk;

import database.DAO;
import enteties.ColoredObjectTableCell;
import enteties.ObjectTableElement;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private DAO dao;

    @FXML
    private TableView<ObjectTableElement> objectTable;

    @FXML
    private TableColumn<ObjectTableElement, StringProperty> nameColumn;
    @FXML
    private TableColumn<ObjectTableElement, StringProperty> typeColumn;
    @FXML
    private TableColumn<ObjectTableElement, String> statusColumn;

    ObservableList<ObjectTableElement> objectTableElements = FXCollections.observableArrayList();

    @FXML
    private void updateTable() {
        objectTableElements = dao.selectMain();

        ObservableList<TableColumn<ObjectTableElement,?>> sortColumns = FXCollections.observableArrayList();
        if(objectTable.getSortOrder().size()>0) {
             sortColumns = FXCollections.observableArrayList(objectTable.getSortOrder());
        }
        else{
            sortColumns.add(statusColumn);
        }

        objectTable.setItems(objectTableElements);
        objectTable.getSortOrder().addAll(sortColumns);
        System.out.println(objectTable.getSortOrder().size());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(factory -> new ColoredObjectTableCell());

        dao = DAO.getInstance();
        updateTable();

    }

    @FXML
    public void onTableClick(MouseEvent event) throws IOException {

        if(event.getButton().equals(MouseButton.PRIMARY)) {
            if (event.getClickCount() == 2) {               //TODO: known issue: tries to open detail view even by double-click on table header

                openDetailView();

            }
        }
    }

    private void openDetailView() throws IOException {
        ObjectTableElement currentElement = objectTable.getSelectionModel().getSelectedItem();

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("detail-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());


        Stage stage = new Stage();

        stage.setTitle(currentElement.getName());
        stage.setScene(scene);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(objectTable.getScene().getWindow());

        DetailController detailController = fxmlLoader.getController();
        detailController.setCurrentElement(currentElement.getId());     //TODO: Better data transfer

        stage.show();
    }

    @FXML
    public void onNewObject() throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("addElement-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());


        Stage stage = new Stage();

        stage.setTitle("New Object");
        stage.setScene(scene);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(objectTable.getScene().getWindow());

        AddElementController addElementController = fxmlLoader.getController();
        addElementController.setTableReference(objectTableElements);      //TODO: better data transfer

        stage.show();
    }




}