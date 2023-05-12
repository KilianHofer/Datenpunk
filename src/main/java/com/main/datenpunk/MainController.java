package com.main.datenpunk;

import database.DAO;
import enteties.ColoredObjectTableCell;
import enteties.ObjectTableElement;
import enteties.Status;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private DatePicker toDatePicker,fromDatePicker;
    private DAO dao;

    @FXML
    private TableView<ObjectTableElement> objectTable;
    @FXML
    private TableColumn<ObjectTableElement, StringProperty> nameColumn;
    @FXML
    private TableColumn<ObjectTableElement, StringProperty> typeColumn;
    @FXML
    private TableColumn<ObjectTableElement, String> idColumn,statusColumn, dateColumn;

    @FXML
    private CheckMenuItem idCheck,nameCheck,typeCheck,statusCheck,dateCheck;
    private final ObservableList<CheckMenuItem> checkMenus = FXCollections.observableArrayList();

    @FXML
    private TextField whitelistNameField, whitelistTypeField, blacklistNameField, blacklistTypeField;
    @FXML
    private ChoiceBox<String> whitelistStatusBox, blacklistStatusBox;
    private final ObservableList<Control> controlList = FXCollections.observableArrayList();

    @FXML
    private ListView<String> whitelistNameList, whitelistTypeList, whitelistStatusList, blacklistNameList, blacklistTypeList, blacklistStatusList;
    private final ObservableList<ListView<String>> listViews = FXCollections.observableArrayList();

    @FXML
    private Button addToNameWhitelist,addToTypeWhitelist,addToStatusWhitelist,addToNameBlacklist,addToTypeBlacklist,addToStatusBlacklist,removeFromNameWhitelist,removeFromTypeWhitelist,removeFromStatusWhitelist,removeFromNameBlacklist,removeFromTypeBlacklist,removeFromStatusBlacklist;
    private final ObservableList<Button> addButtons = FXCollections.observableArrayList();
    private final ObservableList<Button> removeButtons = FXCollections.observableArrayList();

    LocalDate toDate,fromDate;

    List<Status> statuses = new ArrayList<>();
    List<String> statusNames = new ArrayList<>();

    ObservableList<ObjectTableElement> objectTableElements = FXCollections.observableArrayList();

    private void getStatuses(){
        statuses = dao.selectStatuses();
        Status status;
        for (Status value : statuses) {
            status = value;
            statusNames.add(status.getName());
        }
    }

    @FXML
    private void updateTable() {
        objectTableElements = dao.selectMain(fromDate,toDate,listViews);

        ObservableList<TableColumn<ObjectTableElement,?>> sortColumns = FXCollections.observableArrayList();
        if(objectTable.getSortOrder().size()>0) {
             sortColumns = FXCollections.observableArrayList(objectTable.getSortOrder());
        }
        else{
            sortColumns.add(statusColumn);
        }

        objectTable.setItems(objectTableElements);
        objectTable.getSortOrder().addAll(sortColumns);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {



        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(factory -> new ColoredObjectTableCell());
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        toDate = LocalDate.now();
        toDatePicker.setValue(toDate);

        checkMenus.addAll(idCheck,nameCheck,typeCheck,statusCheck,dateCheck);
        onCheckVisible();

        dao = DAO.getInstance();
        /*objectTable.getScene().getWindow().setOnCloseRequest(windowEvent -> {         //TODO: disconnect from DB before closing window
            dao.disconnectFromDB();
            Platform.exit();
        });

         */

        controlList.addAll(whitelistNameField,whitelistTypeField,whitelistStatusBox,blacklistNameField,blacklistTypeField,blacklistStatusBox);
        listViews.addAll(whitelistNameList,whitelistTypeList,whitelistStatusList,blacklistNameList,blacklistTypeList, blacklistStatusList);
        addButtons.addAll(addToNameWhitelist,addToTypeWhitelist,addToStatusWhitelist,addToNameBlacklist,addToTypeBlacklist,addToStatusBlacklist);
        removeButtons.addAll(removeFromNameWhitelist,removeFromTypeWhitelist,removeFromStatusWhitelist,removeFromNameBlacklist,removeFromTypeBlacklist,removeFromStatusBlacklist);

        getStatuses();

        whitelistStatusBox.getItems().setAll(statusNames);
        blacklistStatusBox.getItems().setAll(statusNames);


        updateTable();



    }

    @FXML
    public void onTableClick(MouseEvent event) throws IOException {

        if(event.getButton().equals(MouseButton.PRIMARY)) {
            if (event.getClickCount() == 2) {               //TODO: known issue: opens detail view of selected item even by double-click on table header

                openDetailView();

            }
        }
    }

    private void openDetailView() throws IOException {
        if(objectTable.getSelectionModel().getSelectedItem() != null) {
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

            stage.setResizable(false);
            stage.show();
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
        addElementController.setTableReference(objectTableElements);      //TODO: better data transfer
        stage.setResizable(false);
        stage.show();
    }

    public void onCheckVisible(){               //TODO: known issue: when table columns are switched the wrong column gets hidden
        for (int i = 0; i < checkMenus.size(); i++) {
            objectTable.getColumns().get(i).setVisible(checkMenus.get(i).isSelected());
        }
    }

    @FXML
    public void getToDate() {

        toDate = toDatePicker.getValue();

        updateTable();
    }
    @FXML
    public void getFromDate() {

        fromDate = fromDatePicker.getValue();

        updateTable();
    }

    public void onReset() {
        toDate = LocalDate.now();
        fromDate = null;
        toDatePicker.setValue(toDate);
        fromDatePicker.setValue(fromDate);
        updateTable();
    }

    public void onCancel() {
        Stage stage = (Stage) objectTable.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void onAddToList(ActionEvent actionEvent) {

        int i;
        if(actionEvent.getSource().getClass() == Button.class)
            i = addButtons.indexOf(actionEvent.getSource());
        else
            i = controlList.indexOf(actionEvent.getSource());

        Control control = controlList.get(i);
        String text;
        if(control.getClass() == TextField.class) {
            text = ((TextField) control).getText();
            ((TextField) control).setText("");
        }
        else
            text = ((ChoiceBox<String>) control).getValue();


        listViews.get(i).getItems().add(text);
        updateTable();
    }

    @FXML
    public void onRemoveFromList(ActionEvent actionEvent) {
        removeFromList(removeButtons.indexOf(actionEvent.getSource()));
    }


    private void removeFromList(int id){
        ListView<String> listView = listViews.get(id);
        if(listView.getSelectionModel().getSelectedItem() != null){
            listView.getItems().remove(listView.getSelectionModel().getSelectedItem());
            updateTable();
        }
    }

    public void onListClick(MouseEvent event) {
        if(event.getButton().equals(MouseButton.PRIMARY)) {
            if (event.getClickCount() == 2) {               //TODO: known issue: opens detail view of selected item even by double-click on table header
                removeFromList(listViews.indexOf(event.getSource()));
            }
        }
    }

    public void onListKey(KeyEvent keyEvent) {
        if(keyEvent.getCode().equals(KeyCode.DELETE) || keyEvent.getCode().equals(KeyCode.BACK_SPACE)){
            removeFromList(listViews.indexOf(keyEvent.getSource()));
        }
    }

    public void onNewProject() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("newProject-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());


        Stage stage = new Stage();

        stage.setTitle("New Project");
        stage.setScene(scene);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(objectTable.getScene().getWindow());

        NewProjectController newProjectController = fxmlLoader.getController();
        newProjectController.setReturnStage((Stage)objectTable.getScene().getWindow());      //TODO: better data transfer
        stage.setResizable(false);
        stage.show();
    }

    public void onProjectSelection() throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("projectSelection-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        Stage stage = (Stage)objectTable.getScene().getWindow();
        stage.setScene(scene);

        ProjectSelectionController controller = fxmlLoader.getController();
        controller.initalizeTable();
        stage.show();

    }
}