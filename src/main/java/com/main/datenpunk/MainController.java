package com.main.datenpunk;

import database.DAO;
import enteties.TableElement;
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
    private TableView<TableElement> objectTable;

    @FXML
    private TableColumn<TableElement, StringProperty> nameColumn;
    @FXML
    private TableColumn<TableElement, StringProperty> typeColumn;
    @FXML
    private TableColumn<TableElement, StringProperty> statusColumn;

    ObservableList<TableElement> tableElements = FXCollections.observableArrayList();

    private void updateTable() {
        tableElements = dao.selectMain();
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        objectTable.setItems(tableElements);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        dao = DAO.getInstance();
        updateTable();

    }

    @FXML
    public void onTableClick(MouseEvent event) throws IOException {

        if(event.getButton().equals(MouseButton.PRIMARY)) {
            if (event.getClickCount() == 2) {               //TODO: known issue: tries to open detail view even by double-click on table header

                TableElement currentElement = objectTable.getSelectionModel().getSelectedItem();

                FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("detail-view.fxml"));
                Scene scene = new Scene(fxmlLoader.load());


                Stage stage = new Stage();

                stage.setTitle(currentElement.getName());
                stage.setScene(scene);
                stage.initModality(Modality.WINDOW_MODAL);
                stage.initOwner(objectTable.getScene().getWindow());

                DetailController detailController = fxmlLoader.getController();
                detailController.setCurrentElement(currentElement);     //TODO: Better data transfer

                stage.show();


            }
        }
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
        addElementController.setTableReference(tableElements);      //TODO: better data transfer

        stage.show();
    }




}