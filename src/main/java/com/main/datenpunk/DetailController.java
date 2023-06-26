package com.main.datenpunk;

import database.DAO;
import enteties.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    List<Boolean> accept = new ArrayList<>();
    List<Boolean> required = new ArrayList<>();
    List<String> types = new ArrayList<>();


    public void updateTable() {

        for (Node column : historyTable.getItems()) {
            Button button = (Button) ((VBox) column).getChildren().get(0);
            ListView<String> listView = (ListView<String>) ((VBox) column).getChildren().get(1);
            try {
                listView.getItems().setAll(dao.selectHistory(currentElement, button.getText(), sortColumn, sortType));
            }
            catch (Exception e) {
                System.out.println(button.getText());
            }
        }
    }

    public void setCurrentElement(int id) {

        currentElement = id;
        for (Node column : settingsContainer.getChildren()) {
            VBox vBox = (VBox) column;
            String name = ((Label) vBox.getChildren().get(0)).getText();
            name = name.substring(0, name.length() - 1);
            Node setting = vBox.getChildren().get(1);
            setting.requestFocus();
            if (setting.getClass().equals(TextField.class)){
                String value =dao.selectElement(id,name);
                if(value != null)
                    ((TextField) setting).setText(dao.selectElement(id, name));
            }
            else
                ((ChoiceBox<String>) setting).setValue(dao.selectElement(id, name));

        }


        updateTable();
    }

    public void onUpdatePressed() {

        if(!accept.contains(false)) {
            List<String> historyNames = new ArrayList<>();
            List<String> newHistoryValues = new ArrayList<>();
            List<String> oldHistoryValues = new ArrayList<>();
            for (Node column : historyTable.getItems()) {
                String name = ((Button) ((VBox) column).getChildren().get(0)).getText();
                if (!name.equals("Date")) {
                    historyNames.add(name);
                    oldHistoryValues.add(dao.selectHistoryElement(currentElement, name));
                }
            }
            for (int i = 0; i<settingsContainer.getChildren().size();i++) {
                VBox vBox = (VBox) settingsContainer.getChildren().get(i);
                String column = ((Label) vBox.getChildren().get(0)).getText();
                column = column.substring(0, column.length() - 1);
                Node settingValue = vBox.getChildren().get(1);
                String value;
                if (settingValue.getClass().equals(TextField.class)){
                    value = ((TextField) settingValue).getText();
                }
                else {
                    value = ((ChoiceBox<String>) settingValue).getValue();
                }

                if (historyNames.contains(column)) {
                    newHistoryValues.add(value);
                } else {
                    if (!column.equals("history"))
                        dao.updateValue(currentElement, column, value,types.get(i));
                }
            }

            for (int i = 0; i < oldHistoryValues.size(); i++) {
                String oldValue = oldHistoryValues.get(i);
                String newValue = newHistoryValues.get(i);
                if ((newValue != null && oldValue == null) || !Objects.equals(newValue, oldValue)) {
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

    }

    ChangeListener<String> emptyListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
            Node setting = settingsContainer.getScene().focusOwnerProperty().get();
            VBox vBox = (VBox) setting.getParent();
            int index = settingsContainer.getChildren().indexOf(vBox);
            if (t1.equals("")) {
                accept.set(index, false);
                setting.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            }
            else {
                accept.set(index, true);
                setting.setStyle("-fx-border-width: 0px;");
            }
        }
    };

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dao = DAO.getInstance();


        for (ColumnInfo columnInfo : singleton.getColumnInfo()) {
            if (!columnInfo.name.equals("id") && !columnInfo.name.equals("Date")) {
                String name = columnInfo.name;
                Label label = new Label(name + ":");
                Control setting;
                if(columnInfo.required)
                    accept.add(false);
                else
                    accept.add(true);
                required.add(columnInfo.required);
                types.add(columnInfo.type);
                switch (columnInfo.type) {
                    case "Choice" -> {
                        setting = new ChoiceBox<String>();
                        setting.setPrefWidth(150);
                        for (int i = 0; i < singleton.choiceNames.size(); i++) {
                            if (singleton.choiceNames.get(i).equals(columnInfo.name)) {
                                for (Status choice : singleton.choices.get(i)) {
                                    ((ChoiceBox<String>) setting).getItems().add(choice.getName());
                                }
                            }
                        }
                        if(columnInfo.required)
                            ((ChoiceBox<String>)setting).valueProperty().addListener(emptyListener);
                        else
                            ((ChoiceBox<String>) setting).getItems().add("");
                    }
                    case "Text" -> {
                        setting = new TextField();
                        if(columnInfo.required)
                            ((TextField) setting).textProperty().addListener(emptyListener);
                    }
                    case "Integer" -> {
                        setting = new TextField();
                        ((TextField) setting).textProperty().addListener((observableValue, s, t1) -> {
                            TextField textField = (TextField)settingsContainer.getScene().focusOwnerProperty().get();
                            VBox vBox = (VBox)textField.getParent();
                            int index = settingsContainer.getChildren().indexOf(vBox);
                            if (t1.matches("[0-9]+")) {
                                accept.set(index,true);
                                setting.setStyle("-fx-border-width: 0px");
                            }
                            else {
                                if(t1.equals("") && !required.get(index)){
                                    accept.set(index,true);
                                }
                                else {
                                    setting.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                                    accept.set(index, false);
                                }
                            }
                        });
                    }
                    default -> {
                        setting = new TextField();
                        ((TextField) setting).textProperty().addListener((observableValue, s, t1) -> {
                            TextField textField = (TextField)settingsContainer.getScene().focusOwnerProperty().get();
                            VBox vBox = (VBox)textField.getParent();
                            int index = settingsContainer.getChildren().indexOf(vBox);
                            if (t1.matches("^(0*[1-9][0-9]*(\\.[0-9]+)?|0+\\.[0-9]*[1-9][0-9]*)$")) {
                                setting.setStyle("-fx-border-width: 0px");
                                accept.set(index,true);
                            }
                            else {
                                if(t1.equals("") && !required.get(index)){
                                    accept.set(index,true);
                                }
                                else {
                                    setting.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                                    accept.set(index, false);
                                }
                            }
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
            String name = columnInfo.name;
            Button button = new Button(name);
            button.setPrefWidth(9999);
            button.setStyle("-fx-background-radius: 0px");
            button.setOnAction(this::changeTableSortOrder);
            ListView<String> listView = new ListView<>();
            listView.getSelectionModel().selectedIndexProperty().addListener((observableValue, s, t1) -> {
                if (!switchSelection) {
                    switchSelection = true;
                    for (Node node : historyTable.getItems()) {
                        VBox column = (VBox) node;
                        int index = ((ListView<String>) column.getChildren().get(1)).getSelectionModel().getSelectedIndex();
                        if ((selectedIndex == null || index != selectedIndex) && index != -1) {
                            selectedIndex = index;
                            break;
                        }
                    }
                    for (Node node : historyTable.getItems()) {
                        VBox column = (VBox)node;
                        ((ListView<String>) column.getChildren().get(1)).getSelectionModel().select(selectedIndex);
                    }
                    switchSelection = false;
                }

            });
            listView.getItems().addListener(listChangeListener);
            VBox vBox = new VBox(button, listView);
            historyTable.getItems().add(vBox);
        }

        for(int i = 0; i<historyTable.getDividers().size();i++){
            SplitPane.Divider divider = historyTable.getDividers().get(i);
            float position = 1/((float)historyTable.getDividers().size()+1)*(i+1);
            divider.setPosition(position);
        }
    }

    public void setupLater() {

        Window window = historyTable.getScene().getWindow();

        ListView<String> firstListView = (ListView<String>) ((VBox)historyTable.getItems().get(0)).getChildren().get(1);
        window.setHeight(Math.min(window.getHeight()+firstListView.getItems().size()*24,700));

        historyTable.setPrefWidth(historyTable.getItems().size()*150);

        window.setWidth(Math.min(historyTable.getWidth()+75,1000));

        historyTable.setMaxWidth(window.getWidth()-75);
        window.widthProperty().addListener((observableValue, number, t1) -> historyTable.setMaxWidth((Double)t1-75));


        for (int i = 0; i < historyTable.getItems().size(); i++) {
            VBox vBox = (VBox) historyTable.getItems().get(i);
            ListView<String> listView = (ListView<String>) (vBox).getChildren().get(1);
            ColumnInfo column = null;
            for (ColumnInfo columnInfo : singleton.getColumnInfo()) {
                if (columnInfo.name.equals(((Button) vBox.getChildren().get(0)).getText())) {
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
                                if (item == null || item.equals("") || empty) {
                                    setText(null);
                                    setStyle("-fx-control-opacity: 0;");
                                } else {
                                    setText(item);
                                    for (int i = 0; i < singleton.choices.size(); i++) {
                                        if (((Button) ((VBox) getParent().getParent().getParent().getParent().getParent()).getChildren().get(0)).getText().equals(singleton.choiceNames.get(i))) {
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
        for (Node node : historyTable.getItems()) {
            VBox column = (VBox)node;
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
