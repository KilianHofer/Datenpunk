package com.main.datenpunk;

import database.DAO;
import enteties.*;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


public class DetailController implements Initializable {

    public TilePane settingsContainer;
    private DAO dao;
    private final Singleton singleton = Singleton.getInstance();

    boolean switchSelection = false;
    boolean updated = false;
    Integer selectedIndex;

    @FXML
    private SplitPane historyTable;

    int currentElement;

    private String sortType = "DESC";
    private String sortColumn = "Date";


    public void updateTable() {

        String sortColumnName = sortColumn;
        for (Node column : historyTable.getItems()) {
            Button button = (Button) ((VBox) column).getChildren().get(0);
            String columnName = "";
            for (ColumnInfo columnInfo : singleton.getColumnInfo()) {
                if (columnInfo.name.equals(button.getText().toLowerCase()))
                    columnName = columnInfo.table + "." + columnInfo.name;
                if (columnInfo.name.equals(sortColumnName.toLowerCase()))
                    sortColumnName = columnInfo.table + "." + sortColumnName;

            }
            ListView<String> listView = (ListView<String>) ((VBox) column).getChildren().get(1);
            listView.getItems().setAll(dao.selectHistory(currentElement, columnName, sortColumn, sortType));
        }
    }

    public void setCurrentElement(int id) {

        currentElement = id;
        for (Node column : settingsContainer.getChildren()) {
            VBox vBox = (VBox) column;
            String name = ((Label) vBox.getChildren().get(0)).getText();
            name = name.substring(0, name.length() - 1).toLowerCase();
            for (ColumnInfo columnInfo : singleton.getColumnInfo()) {
                if (columnInfo.name.equals(name)) {
                    name = columnInfo.table + "." + name;
                    break;
                }
            }
            Node setting = vBox.getChildren().get(1);
            if (setting.getClass().equals(TextField.class))
                ((TextField) setting).setText(dao.selectElement(id, name));
            else
                ((ChoiceBox<String>) setting).setValue(dao.selectElement(id, name));

        }


        updateTable();
    }

    public void onUpdatePressed() {


        List<String> historyNames = new ArrayList<>();
        List<String> newHistoryValues = new ArrayList<>();
        List<String> oldHistoryValues = new ArrayList<>();
        for (Node column : historyTable.getItems()) {
            String name = ((Button) ((VBox) column).getChildren().get(0)).getText().toLowerCase();
            if(!name.equals("date")){
                historyNames.add(name);
                oldHistoryValues.add(dao.selectHistoryElement(currentElement,name));
            }
        }
        for (Node setting : settingsContainer.getChildren()) {
            VBox vBox = (VBox) setting;
            String column = ((Label) vBox.getChildren().get(0)).getText();
            column = column.substring(0, column.length() - 1).toLowerCase();
            Node settingValue = vBox.getChildren().get(1);
            String value;
            if (settingValue.getClass().equals(TextField.class))
                value = ((TextField) settingValue).getText();
            else
                value = ((ChoiceBox<String>) settingValue).getValue();

            if (historyNames.contains(column)) {
                newHistoryValues.add(value);
            } else {
                if(!column.equals("history"))
                    dao.updateValue(currentElement, column, value);
            }
        }
        for(int i = 0;i<oldHistoryValues.size();i++){
            if(!newHistoryValues.get(i).equals(oldHistoryValues.get(i))){
                updated = true;
                break;
            }
        }

        if (updated) {
            updated = false;
            dao.updateHistory(currentElement, historyNames, newHistoryValues);
            updateTable();
        }
        singleton.getController().updateTable();

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dao = DAO.getInstance();


        for (ColumnInfo columnInfo : singleton.getColumnInfo()) {
            if (!columnInfo.name.equals("id") && !columnInfo.name.equals("date")) {
                String name = columnInfo.name.substring(0, 1).toUpperCase() + columnInfo.name.substring(1);
                Label label = new Label(name + ":");
                Node setting;
                switch (columnInfo.type) {
                    case "choice" -> {
                        setting = new ChoiceBox<String>();
                        ((ChoiceBox<String>) setting).setPrefWidth(150);
                        for (int i = 0; i < singleton.choiceNames.size(); i++) {
                            if (singleton.choiceNames.get(i).equals(columnInfo.name)) {
                                for (Status choice : singleton.choices.get(i)) {
                                    ((ChoiceBox<String>) setting).getItems().add(choice.getName());
                                }
                            }
                        }
                    }
                    case "text" -> setting = new TextField();
                    case "integer" -> {
                        setting = new TextField();
                        ((TextField) setting).textProperty().addListener((observableValue, s, t1) -> {
                            if (t1.matches("[0-9]+"))
                                setting.setStyle("-fx-border-width: 0px");
                            else
                                setting.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                        });
                    }
                    default -> {
                        setting = new TextField();
                        ((TextField) setting).textProperty().addListener((observableValue, s, t1) -> {
                            if (t1.matches("[0-9]+.?[0-9]?+"))
                                setting.setStyle("-fx-border-width: 0px");
                            else
                                setting.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                        });
                    }
                }

                VBox vBox = new VBox(label, setting);
                settingsContainer.getChildren().add(vBox);
            }
        }
        for (int i = 0; i < singleton.getColumnInfo().size(); i++) {
            ColumnInfo columnInfo = singleton.getColumnInfo().get(i);
            if (!columnInfo.table.equals("history"))
                continue;
            String name = columnInfo.name.substring(0, 1).toUpperCase() + columnInfo.name.substring(1);
            Button button = new Button(name);
            button.setPrefWidth(9999);
            button.setStyle("-fx-background-radius: 0px");
            button.setOnAction(this::changeTableSortOrder);
            ListView<String> listView = new ListView<>();
            listView.getSelectionModel().selectedIndexProperty().addListener((observableValue, s, t1) -> {
                if (!switchSelection) {
                    switchSelection = true;
                    for (VBox column : singleton.getColumns()) {
                        int index = ((ListView<String>) column.getChildren().get(1)).getSelectionModel().getSelectedIndex();
                        if ((selectedIndex == null || index != selectedIndex) && index != -1) {
                            selectedIndex = index;
                            break;
                        }
                    }
                    for (VBox column : singleton.getColumns()) {
                        ((ListView<String>) column.getChildren().get(1)).getSelectionModel().select(selectedIndex);
                    }
                    switchSelection = false;
                }

            });
            listView.getItems().addListener(listChangeListener);
            VBox vBox = new VBox(button, listView);
            historyTable.getItems().add(vBox);
        }
        updateTable();
    }

    public void initializeCellFactories() {
        for (int i = 0; i < historyTable.getItems().size(); i++) {
            VBox vBox = (VBox) historyTable.getItems().get(i);
            ListView<String> listView = (ListView<String>) (vBox).getChildren().get(1);
            ColumnInfo column = null;
            for (ColumnInfo columnInfo : singleton.getColumnInfo()) {
                if (columnInfo.name.equals(((Button) vBox.getChildren().get(0)).getText().toLowerCase())) {
                    column = columnInfo;
                    break;
                }
            }


            assert column != null;
            if (column.colored) {
                listView.setCellFactory(new Callback<>() {
                    @Override
                    public ListCell<String> call(ListView<String> stringListView) {
                        return new ListCell<>() {
                            @Override
                            protected void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item == null || empty) {
                                    setText(null);
                                    setStyle("-fx-control-opacity: 0;");
                                } else {
                                    setText(item);
                                    for (int i = 0; i < singleton.choices.size(); i++) {
                                        if (((Button) ((VBox) getParent().getParent().getParent().getParent().getParent()).getChildren().get(0)).getText().toLowerCase().equals(singleton.choiceNames.get(i))) {
                                            for (Status status : singleton.choices.get(i)) {
                                                if (item.equals(status.getName())) {
                                                    setStyle("-fx-control-inner-background: " + status.getColor());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        };
                    }
                });
            }
        }
    }

    ListChangeListener<String> listChangeListener = change -> {
        for (VBox column : singleton.getColumns()) {
            int ROW_HEIGHT = 24;
            ListView<String> listView = (ListView<String>) column.getChildren().get(1);
            listView.setPrefHeight(listView.getItems().size() * ROW_HEIGHT + 2);
        }
    };

    private void changeTableSortOrder(ActionEvent event) {
        Button button = (Button) event.getSource();
        if (button.getText().equals(sortColumn)) {
            if (sortType.equals("ASC"))
                sortType = "DESC";
            else
                sortType = "ASC";
        } else {
            sortColumn = button.getText();
            sortType = "ASC";
        }
        updateTable();
    }

    public void onDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText("Delete this Object?");
        if (alert.showAndWait().get() == ButtonType.OK) {
            dao.deleteObject(currentElement);
            singleton.getController().updateTable();
            ((Stage) historyTable.getScene().getWindow()).close();
        }

    }
}
