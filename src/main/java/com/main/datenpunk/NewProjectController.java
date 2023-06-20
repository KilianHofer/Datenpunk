package com.main.datenpunk;

import database.DAO;
import enteties.ColumnInfo;
import enteties.Status;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

public class NewProjectController implements Initializable {


    public BorderPane mainPane;
    public Button createButton;
    @FXML
    VBox columnContainer;

    @FXML
    TextField nameMaxLengthField;
    DAO dao = DAO.getInstance();
    Singleton singleton = Singleton.getInstance();

    ObservableList<String> choices = FXCollections.observableArrayList("Text", "Integer", "Decimal", "Choice");
    Stage returnStage;
    @FXML
    TextField nameField, pathField;


    boolean changingOrder = false;
    List<Node> oldOrder = new ArrayList<>();
    List<Node> newOrder = new ArrayList<>();
    boolean updating = false;

    ObservableList<Node> oldColumns = FXCollections.observableArrayList();          //lists to detect what changed
    List<String> oldPositions = new ArrayList<>();
    List<String> oldNames = new ArrayList<>();
    List<Boolean> oldTrackChecks = new ArrayList<>();
    List<Boolean> oldRequiredChecks = new ArrayList<>();
    List<String> oldTypes = new ArrayList<>();
    List<String> oldLengths = new ArrayList<>();
    List<List<String>> oldChoices = new ArrayList<>();
    static List<List<String>> listChanged = new ArrayList<>();

    List<List<String>> columnPositionChanges = new ArrayList<>();            //lists that contain what has to be changed
    List<List<String>> columnNameChanges = new ArrayList<>();
    List<List<String>> historyChanges = new ArrayList<>();
    List<List<String>> requiredChanges = new ArrayList<>();
    List<List<String>> typeChanges = new ArrayList<>();
    List<List<String>> lengthChanges = new ArrayList<>();
    List<List<String>> listValueChanges = new ArrayList<>();
    List<List<String>> listPositionChanges = new ArrayList<>();




    public void setUpdating(){
        updating = true;
        mainPane.setTop(null);
        createButton.setText("Update");
        nameField.setText(singleton.getCurrentProject());

        listChanged.clear();

        columnContainer.getChildren().clear();
        for(int i = 0; i<singleton.getColumnInfo().size();i++){
            ColumnInfo columnInfo = singleton.getColumnInfo().get(i);
            onAdd();
            String name = columnInfo.name;
            TextField columnName = getColumnNameField(i);
            columnName.requestFocus();
            columnName.setText(name);

            CheckBox historyCheck = getColumnHistoryCheck(i);
            historyCheck.setSelected(!columnInfo.table.equals("objects"));

            CheckBox requiredCheck = getColumnRequiredCheck(i);
            requiredCheck.setSelected(columnInfo.required);

            ChoiceBox<String> choiceBox = getColumnChoiceBox(i);
            String type = columnInfo.type;
            if(type.equals("serial") ||type.equals("date"))
                type = type.toUpperCase();
            else
                type = type.substring(0,1).toUpperCase()+type.substring(1);
            choiceBox.requestFocus();
            getColumnChoiceBox(i).setValue(type);
            if(type.equals("Choice")){
                ListView<String> listView = getColumnSelectionList(i);
                List<Status> list = singleton.choices.get(i);
                for(Status choice:list){
                    listView.getItems().add(choice.getName()+"("+choice.getColor()+")");
                }
            }
            if(type.equals("Text")){
                TextField length = getColumnMaxLengthField(i);
                length.requestFocus();
                length.setText(String.valueOf(columnInfo.length));
            }
            if(name.equals("id") || name.equals("Name") || name.equals("Status") || name.equals("Date")){
                columnName.setDisable(true);
                choiceBox.setDisable(true);
                getColumnCloseButton(i).setDisable(true);
                if(name.equals("id")  || name.equals("Date")){
                    historyCheck.setDisable(true);
                    requiredCheck.setDisable(true);
                }
            }
        }
        for(int i = 0; i<columnContainer.getChildren().size();i++){
            oldColumns.add(columnContainer.getChildren().get(i));
            oldPositions.add(getColumnPositionField(i).getText());
            oldNames.add(getColumnNameField(i).getText());
            String type = getColumnChoiceBox(i).getValue();

            oldTypes.add(type);
            if(type.equals("Text"))
                oldLengths.add(getColumnMaxLengthField(i).getText());
            else
                oldLengths.add(null);
            List<String> list = listChanged.get(i);
            if(type.equals("Choice")) {
                List<String> oldList = getColumnSelectionList(i).getItems();
                oldChoices.add(new ArrayList<>(oldList));
                for(int j = 0; j<oldList.size();j++){
                    list.add("");
                }
            }
            else {
                oldChoices.add(null);
            }
            oldTrackChecks.add(getColumnHistoryCheck(i).isSelected());
            oldRequiredChecks.add(getColumnRequiredCheck(i).isSelected());
        }
    }


    ChangeListener<String> positionListener = new ChangeListener<>() {

        @Override
        public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
            if (!changingOrder) {
                TextField changedField = (TextField) mainPane.getScene().focusOwnerProperty().get();
                if (t1.matches("^[1-9][0-9]*$") && Integer.parseInt(t1) <= columnContainer.getChildren().size()) {
                    changedField.setStyle("-fx-border-width: 0px;");
                    if (!t1.equals(s))
                        changeColumnOrder(columnContainer.getChildren().indexOf(changedField.getParent().getParent().getParent()), Integer.parseInt(t1));
                } else {
                    changedField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");

                }
            }
        }
    };

    ChangeListener<String> emptyListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
            if (t1.equals("")) {
                mainPane.getScene().focusOwnerProperty().get().setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            } else {
                mainPane.getScene().focusOwnerProperty().get().setStyle("-fx-border-width: 0px;");
            }
        }
    };

    ChangeListener<String> numberListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
            TextField changedField = (TextField) mainPane.getScene().focusOwnerProperty().get();
            if (t1.matches("^[1-9][0-9]*$")) {
                changedField.setStyle("-fx-border-width: 0px;");
            } else {
                changedField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            }
        }
    };

    ChangeListener<String> choiceListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {

            ChoiceBox<String> choiceBox = (ChoiceBox<String>) mainPane.getScene().focusOwnerProperty().get();
            VBox vBox = (VBox) choiceBox.getParent();
            VBox closeVBox = (VBox)((HBox)choiceBox.getParent().getParent()).getChildren().get(3);
            int limit = vBox.getChildren().size();
            if (limit > 2) {
                vBox.getChildren().subList(2, limit).clear();
            }
            limit = closeVBox.getChildren().size();
            if (limit > 2) {
                closeVBox.getChildren().subList(2, limit).clear();
            }

            switch (t1) {
                case "Text" -> {
                    TextField textField = new TextField();
                    textField.setPromptText("Max. Length");
                    textField.textProperty().addListener(numberListener);
                    vBox.getChildren().add(textField);
                }
                case "Choice" -> {
                    TextField textField = new TextField();
                    textField.setPromptText("Name");
                    textField.setOnAction(NewProjectController::addToList);
                    vBox.getChildren().add(textField);

                    ColorPicker colorPicker = new ColorPicker();
                    colorPicker.setPrefWidth(150);
                    vBox.getChildren().add(colorPicker);

                    BorderPane borderPane = new BorderPane();
                    Button removeButton = new Button("Remove");
                    removeButton.setOnAction(NewProjectController::removeFromList);
                    borderPane.setLeft(removeButton);
                    Button addButton = new Button("Add");
                    addButton.setOnAction(NewProjectController::addToList);
                    borderPane.setRight(addButton);
                    Button setButton = new Button("Set");
                    setButton.setOnAction(NewProjectController.this::onSetValues);
                    borderPane.setCenter(setButton);
                    vBox.getChildren().add(borderPane);

                    ListView<String> listView = new ListView<>();
                    listView.setPrefSize(150, 150);
                    listView.setCellFactory(cellfactory);
                    vBox.getChildren().add(listView);

                    VBox.setMargin(textField, new Insets(0,0,5,0));
                    VBox.setMargin(borderPane, new Insets(5, 0, 5, 0));


                    Button upButton = new Button("▲");
                    upButton.setStyle("-fx-font-size: 10");
                    upButton.setOnAction(NewProjectController::moveUp);
                    Button downButton = new Button("▼");
                    downButton.setStyle("-fx-font-size: 10");
                    downButton.setOnAction(NewProjectController::moveDown);

                    closeVBox.getChildren().add(upButton);
                    closeVBox.getChildren().add(downButton);
                    VBox.setMargin(upButton,new Insets(95,0,0,0));
                }
            }
        }
    };

    Callback<ListView<String>, ListCell<String>> cellfactory = new Callback<>() {
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

                        setStyle("-fx-control-inner-background: " + item.substring(item.lastIndexOf("(") + 1, item.lastIndexOf(")")) + ";");
                    }
                }
            };
        }
    };

    @FXML
    public void onCreate() {
        if(checkValidInputs()) {
            if (!updating)
                createProject();
            else {
                editProject();
                openOldProject();
                onCancel();
            }
        }
    }

    private boolean checkValidInputs() {

        if(nameField.getText().equals("") && !updating){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("The project needs a name");
            alert.show();
            return false;
        }

        List<String> nameList = new ArrayList<>();
        for(int i = 0; i<columnContainer.getChildren().size();i++){
            String name = getColumnNameField(i).getText();
            if(!name.equals("") && !nameList.contains(name) && name.length() <= 200){
                nameList.add(name);
            }
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                if(name.equals(""))
                    alert.setContentText("Names cannot be empty");
                else if(name.length() > 200)
                    alert.setContentText("Name of "+name+" is too long!\n" +
                            "Names may not be longer than 200 Characters");
                else
                    alert.setContentText("Duplicate names are not allowed");
                alert.show();
                return false;
            }

            String type = getColumnChoiceBox(i).getValue();
            if(type.equals("Text")){
                String value = getColumnMaxLengthField(i).getText();
                if(!value.matches("^[1-9][0-9]*$")){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Invalid length value for "+name);
                    alert.show();
                    return false;
                }

            }
        }

        return true;
    }

    private void editProject() {
        clearChangeLists();
        Alert presetAlert = new Alert(Alert.AlertType.CONFIRMATION);
        presetAlert.setContentText("Changing the projects Structure will most likely break pre-existing Presets!\nContinue anyway?");
        if(presetAlert.showAndWait().get() == ButtonType.CANCEL){
            return;
        }
        for(int i = 0; i< columnContainer.getChildren().size();i++){
            Node column = columnContainer.getChildren().get(i);
            if(!oldColumns.contains(column)){
                List<String> list = new ArrayList<>();
                list.add(String.valueOf(getColumnHistoryCheck(i).isSelected()));
                list.add("");
                list.add(getColumnNameField(i).getText());
                String type = getColumnChoiceBox(i).getValue();
                list.add(type);
                list.add(getColumnPositionField(i).getText());
                list.add(String.valueOf(getColumnRequiredCheck(i).isSelected()));
                if(type.equals("Text"))
                    list.add(getColumnMaxLengthField(i).getText());
                else
                    list.add("0");
                if(type.equals("Choice")){
                    list.addAll(getColumnSelectionList(i).getItems());
                }
                columnNameChanges.add(list);
            }
            else {
                int index = oldColumns.indexOf(columnContainer.getChildren().get(i));
                if(!oldPositions.get(index).equals(getColumnPositionField(i).getText())){
                    List<String> list = new ArrayList<>();
                    list.add(oldNames.get(index));
                    list.add(getColumnPositionField(i).getText());
                    columnPositionChanges.add(list);

                }
                if(!oldNames.get(index).equals(getColumnNameField(i).getText())){
                    List<String> list = new ArrayList<>();
                    list.add(String.valueOf(getColumnHistoryCheck(i).isSelected()));
                    list.add(oldNames.get(index));
                    list.add(getColumnNameField(i).getText());
                    columnNameChanges.add(list);
                }
                String newType = getColumnChoiceBox(i).getValue();
                if(!oldTypes.get(index).equals(newType)){
                    List<String> list = new ArrayList<>();
                    list.add(String.valueOf(getColumnHistoryCheck(i).isSelected()));
                    list.add(oldNames.get(index));
                    list.add(oldTypes.get(i));
                    list.add(newType);
                    if(newType.equals("Text"))
                        list.add(getColumnMaxLengthField(i).getText());
                    else
                        list.add("");
                    if(newType.equals("Choice"))
                        list.addAll(getColumnSelectionList(i).getItems());
                    typeChanges.add(list);
                }
                if(!oldTrackChecks.get(index).equals(getColumnHistoryCheck(i).isSelected())){
                    if(!getColumnHistoryCheck(i).isSelected()) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setContentText("Historical data of '" + oldNames.get(oldColumns.indexOf(column)) + "' will be deleted!");
                        if (alert.showAndWait().get().equals(ButtonType.CANCEL)) {
                            return;
                        }
                    }
                    List<String> list = new ArrayList<>();
                    list.add(oldNames.get(index));
                    list.add(String.valueOf(getColumnHistoryCheck(i).isSelected()));
                    String type = getColumnChoiceBox(i).getValue();
                    list.add(type);
                    if(type.equals("Text"))
                        list.add(getColumnMaxLengthField(i).getText());
                    else
                        list.add("");
                    historyChanges.add(list);

                }
                if(!oldRequiredChecks.get(index).equals(getColumnRequiredCheck(i).isSelected())){
                    List<String> list = new ArrayList<>();
                    list.add(oldNames.get(index));
                    list.add(String.valueOf(getColumnRequiredCheck(i).isSelected()));
                    requiredChanges.add(list);
                }
                if(getColumnMaxLengthField(i) != null && oldLengths.get(index) != null && !oldLengths.get(index).equals(getColumnMaxLengthField(i).getText())){
                    List<String> list = new ArrayList<>();
                    list.add(String.valueOf(getColumnHistoryCheck(i).isSelected()));
                    list.add(oldNames.get(index));
                    list.add(String.valueOf(getColumnMaxLengthField(i).getText()));
                    lengthChanges.add(list);
                }
                if(newType.equals("Choice")) {
                    List<String> newList = getColumnSelectionList(i).getItems();
                    List<String> oldList = oldChoices.get(index);
                    if (oldList != null && !oldList.equals(newList)) {
                        String listName = oldNames.get(index);
                        for (int j = 0; j < oldList.size(); j++) {
                            String oldValue = oldList.get(j);
                            if (!newList.contains(oldValue)) {
                                if (!listChanged.get(i).contains(oldValue)) {
                                    List<String> list = new ArrayList<>();
                                    list.add(listName);
                                    list.add(oldValue);
                                    list.add("");
                                    list.add(String.valueOf(i));
                                    listValueChanges.add(list);
                                } else {
                                    int oldPos = oldList.indexOf(oldValue);
                                    int newPos = listChanged.get(i).indexOf(oldValue);
                                    if (oldPos != newPos) {
                                        List<String> list = new ArrayList<>();
                                        list.add(listName);
                                        list.add(oldValue);
                                        list.add(String.valueOf(newPos));
                                        listPositionChanges.add(list);
                                    }
                                    String newValue = newList.get(newPos);
                                    if (!oldValue.equals(newValue)) {
                                        List<String> list = new ArrayList<>();
                                        list.add(listName);
                                        list.add(oldValue);
                                        list.add(newValue);
                                        list.add(String.valueOf(i));
                                        listValueChanges.add(list);
                                    }
                                }
                            } else {
                                int oldPos = oldList.indexOf(oldValue);
                                int newPos = newList.indexOf(oldValue);
                                if (oldPos != newPos) {
                                    List<String> list = new ArrayList<>();
                                    list.add(listName);
                                    list.add(oldValue);
                                    list.add(String.valueOf(newPos));
                                    listPositionChanges.add(list);
                                }
                            }
                        }
                        for (int j = 0; j < newList.size(); j++) {
                            if (!oldList.contains(newList.get(j)) && !oldList.contains(listChanged.get(i).get(j))) {
                                List<String> list = new ArrayList<>();
                                list.add(listName);
                                list.add("");
                                list.add(newList.get(j));
                                list.add(getColumnPositionField(i).getText());
                                listValueChanges.add(list);
                            }
                        }
                    }
                }
            }
        }
        for(Node column:oldColumns){
            if(!columnContainer.getChildren().contains(column)){
                int index = oldColumns.indexOf(column);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setContentText("Data of '"+oldNames.get(index)+"' will be deleted!");
                if(alert.showAndWait().get().equals(ButtonType.CANCEL)){
                    return;
                }
                List<String> list = new ArrayList<>();
                list.add(String.valueOf(oldTrackChecks.get(index)));
                list.add(oldNames.get(index));
                list.add("");
                columnNameChanges.add(list);
            }
        }
        editColumns();
    }

    private void clearChangeLists() {
        columnNameChanges.clear();
        columnPositionChanges.clear();
        typeChanges.clear();
        historyChanges.clear();
        requiredChanges.clear();
        lengthChanges.clear();
        listValueChanges.clear();
        listPositionChanges.clear();
    }

    private void editColumns() {
        for(List<String> list:columnPositionChanges){
            String name = list.get(0);
            String newPos = list.get(1);
            System.out.println("Moving column "+name+" to "+newPos);
            dao.changeColumnPosition(name,newPos);
        }
        for(List<String> list:historyChanges){
            String name = list.get(0);
            String newValue = list.get(1);
            String type = list.get(2);
            String length = list.get(3);
            System.out.println("Changing history of "+name+" to "+newValue);
            dao.changeColumnHistory(name,newValue,type, length);
        }
        for(List<String> list:requiredChanges){
            String name = list.get(0);
            String newValue = list.get(1);
            System.out.println("Changing required of "+name+" to "+newValue);
            dao.changeColumnRequired(name,newValue);
        }
        for(List<String> list:typeChanges){
            String table = list.get(0);
            String name = list.get(1);
            String oldType = list.get(2);
            String newType = list.get(3);
            String length = list.get(4);
            List<String> choiceList = new ArrayList<>();
            for(int i = 5;i<list.size();i++){
                choiceList.add(list.get(i));
            }
            System.out.println("Changing type of "+name+" to "+newType);

            if(!dao.changeColumnType(table,name,oldType,newType,length,choiceList)){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Invalid values in column "+name+".\nCould not change Datatype.");
                alert.showAndWait();
            }
        }
        for(List<String> list:lengthChanges){
            String table = list.get(0);
            String name = list.get(1);
            String newValue = list.get(2);
            System.out.println("Changing length of "+name+" to "+newValue);
            if(!dao.changeColumnLength(table,name,newValue)){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Invalid max length for "+name);
                alert.showAndWait();
            }
        }
        for(List<String> list:listPositionChanges){
            String listName = list.get(0);
            String name = list.get(1);
            String newValue = list.get(2);
            System.out.println("Changing position of "+name+" to "+newValue + " in "+listName);
            dao.changeChoicePosition(listName,name,newValue);
        }
        for(List<String> list: listValueChanges){
            String listName = list.get(0);
            String oldValue = list.get(1);
            String newValue = list.get(2);
            String position = list.get(3);
            if(oldValue.equals("")){
                System.out.println("Create choice: "+newValue + " in "+listName);
                dao.insertChoice(listName,newValue,position);
            }
            else if(newValue.equals("")){
                System.out.println("Delete choice: "+oldValue + " in "+listName);
                dao.deleteChoice(listName,oldValue);
            }
            else {
                System.out.println("Change choice "+oldValue+" to "+newValue + " in "+listName);
                dao.changeChoice(listName,oldValue,newValue);
            }
        }
        for(List<String> list: columnNameChanges){
            String table = list.get(0);
            String oldValue = list.get(1);
            String newValue = list.get(2);
            String type = "";
            String position = "";
            String required = "";
            String length = "0";
            List<String> choiceList = new ArrayList<>();
            if(list.size() > 3) {
                type = list.get(3);
                position = list.get(4);
                required = list.get(5);
                length = list.get(6);

                for (int i = 7; i < list.size(); i++) {
                    choiceList.add(list.get(i));
                }
            }
            if(oldValue.equals("")){
                System.out.println("Create column: "+newValue);
                dao.alterColumnAdd(table,newValue,type,position,required,length,choiceList);
            }
            else if(newValue.equals("")){
                System.out.println("Delete column: "+oldValue);
                dao.alterColumnDelete(table,oldValue);
            }
            else {
                System.out.println("Change column "+oldValue+" to "+newValue);
                dao.alterColumnChange(table,oldValue,newValue);
            }
        }

    }

    public void createProject(){
        String name = nameField.getText();
        String path = pathField.getText();
        File file = new File(path);
        if (name.equals("")) {
            nameField.setStyle("-fx-border-color: red; -fx-border-width: 2px");
            return;
        }
        if (path.equals("")) {
            pathField.setStyle("-fx-border-color: red; -fx-border-width: 2px");
            return;
        }
        nameField.setStyle(" -fx-border-width: 0px");
        pathField.setStyle(" -fx-border-width: 0px");

        try {
            if (!file.exists()) {
                Files.createDirectory(file.toPath());
            }
            path += "\\" + name + ".dtpnkl";
            file = new File(path);
            if (!file.exists()) {
                try {
                    if (!file.createNewFile()) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("could not create file");
                        alert.show();
                        return;
                    }

                    File projectFile = new File(System.getProperty("user.home") + "\\Datenpunk\\projects.dtpnk");
                    BufferedWriter writer = new BufferedWriter(new FileWriter(projectFile, true));
                    writer.append(path).append("\n");
                    writer.close();


                    path = System.getProperty("user.home") + "\\Datenpunk\\Projects";
                    path += "\\" + name;
                    file = new File(path);
                    Files.createDirectory(file.toPath());
                    file = new File(path + "\\Presets");
                    Files.createDirectory(file.toPath());
                    file = new File(path + "\\DiagramPresets");
                    Files.createDirectory(file.toPath());
                    connectToDatabase();

                } catch (IOException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            } else {
                Alert alert = new Alert((Alert.AlertType.ERROR));
                alert.setContentText("A project with this name already exists in this directory!");
                alert.showAndWait();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createTables() {
        List<Node> columns = columnContainer.getChildren();
        List<Integer> objects = new ArrayList<>();
        List<Integer> history = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            if (getColumnHistoryCheck(i).isSelected())
                history.add(i);
            else
                objects.add(i);
        }
        createColumnTable();
        createTable(objects, false);
        createTable(history, true);
    }

    private void createColumnTable() {
        List<Node> columns = columnContainer.getChildren();
        List<String> names = new ArrayList<>();
        List<String> types = new ArrayList<>();
        List<String> tables = new ArrayList<>();
        List<Integer> positions = new ArrayList<>();
        List<Boolean> required = new ArrayList<>();
        List<Integer> lengths = new ArrayList<>();

        for (int i = 0; i < columns.size(); i++) {
            names.add(getColumnNameField(i).getText());
            String type = getColumnChoiceBox(i).getValue();
            types.add(type);
            if (getColumnHistoryCheck(i).isSelected())
                tables.add("history");
            else
                tables.add("objects");
            positions.add(Integer.parseInt(getColumnPositionField(i).getText()));
            required.add(getColumnRequiredCheck(i).isSelected());
            if(type.equals("Text"))
                lengths.add(Integer.parseInt(getColumnMaxLengthField(i).getText()));
            else
                lengths.add(0);
        }

        dao.createColumnTable(names, types, tables, positions,required,lengths);
    }

    private void createTable(List<Integer> list, boolean history) {
        List<String> names = new ArrayList<>();
        List<String> types = new ArrayList<>();
        List<String> foreignNames = new ArrayList<>();
        List<List<Status>> foreignLists = new ArrayList<>();
        List<Integer> positions = new ArrayList<>();
        for (int id : list) {
            String type = "";
            names.add(getColumnNameField(id).getText());
            positions.add(Integer.parseInt(getColumnPositionField(id).getText()));
            switch (getColumnChoiceBox(id).getValue()) {
                case "Text" -> type = "VARCHAR(" + getColumnMaxLengthField(id).getText() + ")";
                case "Integer" -> type = "INT";
                case "Decimal" -> type = "FLOAT";
                case "Choice" -> {
                    type = "VARCHAR(200)";
                    foreignNames.add(getColumnNameField(id).getText());
                    ObservableList<String> categoryList = getColumnSelectionList(id).getItems();
                    List<Status> statuses = new ArrayList<>();
                    for (int j = 0; j < categoryList.size(); j++) {
                        String line = categoryList.get(j);
                        String name = line.substring(0, line.lastIndexOf("("));
                        String color = line.substring(line.lastIndexOf("(") + 1, line.lastIndexOf(")"));

                        statuses.add(new Status(name, j, color));
                    }
                    foreignLists.add(statuses);

                }
                case "SERIAL" -> type = "SERIAL";
                case "DATE" -> type = "BIGINT";
            }
            types.add(type);
        }
        if (!history) {
            dao.createTable("objects", names, types, foreignNames, foreignLists);
        } else {
            dao.createTable("history", names, types, foreignNames, foreignLists);
        }
    }

    private void deleteCreatedData() {
        try {
            String path = singleton.getWorkingDirectory() + "\\Projects\\" + nameField.getText();
            File file = new File(path);
            for (File childFile : Objects.requireNonNull(file.listFiles())) {
                Files.delete(childFile.toPath());
            }
            Files.delete(file.toPath());
            path += ".dtpnkl"; //TODO: might also be .dtpnkr in the future
            file = new File(path);
            Files.delete(file.toPath());
            singleton.removeFromProjectsFile(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void connectToDatabase() {

        File file = new File(System.getProperty("user.home") + "\\Datenpunk\\connection.dtpnk");
        try {
            if(singleton.getPassword() == null) {
                if (file.exists()) {
                    Scanner scanner = new Scanner(file);
                    if (scanner.hasNext()) {
                        singleton.setPassword(scanner.next());

                    }
                    scanner.close();
                }
            }
            openNewProject();

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void openNewProject() {
        String name = nameField.getText();
        if (!dao.connectToDB("", "postgres", singleton.getPassword())) {
            openDBPasswordWindow();
            return;
        }
        dao.createDatabase(name);
        if (!dao.connectToDB("datenpunk_" + name, "postgres", singleton.getPassword())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Could not create Database for this project!");
            alert.show();
            deleteCreatedData();
            return;
        }

        createTables();
        singleton.setCurrentProject(nameField.getText());
        singleton.openProject();
        ((Stage) mainPane.getScene().getWindow()).close();

    }

    private void openOldProject(){
        if (dao.connectToDB("datenpunk_" + nameField.getText(), "postgres", singleton.getPassword())){
            singleton.setCurrentProject(nameField.getText());
            singleton.openProject();
        }
    }



    private void openDBPasswordWindow(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("databaseConnection-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            DatabaseConnectionController connectionController = fxmlLoader.getController();
            connectionController.method = this::openNewProject;
            connectionController.cancelMethod = this::deleteCreatedData;
            stage.setTitle("Connect to Database");
            stage.setScene(scene);
            stage.setOnCloseRequest(event -> {
                deleteCreatedData();
                stage.close();
            });
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(mainPane.getScene().getWindow());
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void onCancel() {
        Stage stage = (Stage) mainPane.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void onSelect() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File file = new File(pathField.getText());
        if (file.exists()) {
            directoryChooser.setInitialDirectory(file);
        }
        String directory = String.valueOf(directoryChooser.showDialog(pathField.getScene().getWindow()));
        if (!directory.equals("null"))
            pathField.setText(directory);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        for(int i = 0;i<4;i++)
            listChanged.add(new ArrayList<>());

        pathField.setText(System.getProperty("user.home") + "\\Datenpunk\\Projects");

        getColumnChoiceBox(0).setValue("SERIAL");
        getColumnChoiceBox(1).setValue("Text");
        getColumnChoiceBox(2).setValue("Choice");
        getColumnChoiceBox(3).setValue("DATE");

        nameMaxLengthField.textProperty().addListener(numberListener);

        getColumnSelectionList(2).setCellFactory(cellfactory);

        getColumnSelectionList(2).getItems().addAll("Complete(#009900)", "In-Progress(#ffcc00)", "Planned(#990000)");


        for (int i = 0; i < 4; i++) {
            getColumnPositionField(i).textProperty().addListener(positionListener);
        }


    }

    private void changeColumnOrder(int oldValue, int newValue) {

        changingOrder = true;
        oldOrder = new ArrayList<>(columnContainer.getChildren());
        VBox old = (VBox) columnContainer.getChildren().get(oldValue);
        columnContainer.getChildren().remove(old);
        columnContainer.getChildren().add(newValue - 1, old);
        generateColumnOrder();
        changingOrder = false;
    }
    private void  generateColumnOrder(){
        changingOrder = true;
        for (int i = 0; i < columnContainer.getChildren().size(); i++) {
            getColumnPositionField(i).setText(String.valueOf(i + 1));
        }
        if(updating) {
            newOrder = new ArrayList<>(columnContainer.getChildren());
            changeChangedOrder();
        }
    }

    private void changeChangedOrder(){
        List<List<String>> tmp = new ArrayList<>(listChanged);
        for(int i = 0; i<oldOrder.size();i++){
            int index = newOrder.indexOf(oldOrder.get(i));
            tmp.set(index,listChanged.get(i));
        }
        listChanged=tmp;
    }

    public void setReturnStage(Stage stage) {
        returnStage = stage;
    }


    private HBox getColumnContainer(int id) {
        return (HBox) ((VBox) columnContainer.getChildren().get(id)).getChildren().get(0);
    }

    private TextField getColumnPositionField(int id) {
        return (TextField) ((VBox) getColumnContainer(id).getChildren().get(0)).getChildren().get(1);
    }

    private TextField getColumnNameField(int id) {
        return (TextField) ((VBox) getColumnContainer(id).getChildren().get(1)).getChildren().get(1);
    }

    private CheckBox getColumnHistoryCheck(int id) {
        return (CheckBox)((VBox) getColumnContainer(id).getChildren().get(1)).getChildren().get(2);
    }

    private CheckBox getColumnRequiredCheck(int id) {
        return (CheckBox)((VBox) getColumnContainer(id).getChildren().get(1)).getChildren().get(3);
    }

    private ChoiceBox<String> getColumnChoiceBox(int id) {
        return (ChoiceBox<String>) ((VBox) getColumnContainer(id).getChildren().get(2)).getChildren().get(1);
    }

    private TextField getColumnMaxLengthField(int id) {
        VBox vBox = (VBox) getColumnContainer(id).getChildren().get(2);
        if(vBox.getChildren().size()==3)
            return (TextField) vBox.getChildren().get(2);
        else
            return null;
    }

    private ListView<String> getColumnSelectionList(int id) {
        return (ListView<String>) ((VBox) getColumnContainer(id).getChildren().get(2)).getChildren().get(5);
    }

    private Button getColumnCloseButton(int id){
        return (Button)((VBox)getColumnContainer(id).getChildren().get(3)).getChildren().get(1);
    }

    public void onAdd() {

        listChanged.add(new ArrayList<>());

        Label padding1 = new Label();

        TextField positionField = new TextField();
        positionField.setText(String.valueOf(columnContainer.getChildren().size() + 1));
        positionField.textProperty().addListener(positionListener);
        positionField.setPrefSize(30, 25);


        VBox first = new VBox(padding1, positionField);


        Label nameLabel = new Label("Name:");

        TextField nameField = new TextField();
        nameField.setStyle("-fx-border-color: red;-fx-border-width: 2px");
        nameField.promptTextProperty().addListener(emptyListener);
        nameField.setPrefWidth(149);
        nameField.textProperty().addListener(emptyListener);

        CheckBox trackHistory = new CheckBox("Track History");
        CheckBox required = new CheckBox("Required");

        VBox second = new VBox(nameLabel, nameField, trackHistory,required);
        VBox.setMargin(nameField, new Insets(0, 0, 5, 0));
        VBox.setMargin(required,new Insets(5,0,0,0));


        Label typeLabel = new Label("Type:");
        ChoiceBox<String> typeBox = new ChoiceBox<>(choices);
        typeBox.setValue("Text");
        typeBox.valueProperty().addListener(choiceListener);
        typeBox.setPrefWidth(150);

        TextField textField = new TextField();
        textField.setPromptText("Max. Length");
        textField.setStyle("-fx-border-color: red;-fx-border-width: 2px");
        textField.textProperty().addListener(numberListener);

        VBox third = new VBox(typeLabel, typeBox, textField);
        VBox.setMargin(typeBox, new Insets(0, 0, 5, 0));


        Label padding2 = new Label();

        Button removeButton = new Button("⛌");
        removeButton.setOnAction(this::onRemoveColumn);

        VBox fourth = new VBox(padding2, removeButton);


        HBox hBox = new HBox(first, second, third, fourth);
        VBox vBox = new VBox(hBox, new Separator());

        HBox.setMargin(first, new Insets(10, 5, 10, 5));
        HBox.setMargin(second, new Insets(10, 5, 10, 0));
        HBox.setMargin(third, new Insets(10, 5, 10, 5));
        HBox.setMargin(fourth, new Insets(10, 0, 0, 0));


        columnContainer.getChildren().add(vBox);

    }

    private void onRemoveColumn(ActionEvent event) {
        columnContainer.getChildren().remove(((Button) event.getSource()).getParent().getParent().getParent());
        generateColumnOrder();
    }

    public static void removeFromList(ActionEvent event) {
        VBox vBox = (VBox) ((Button) event.getSource()).getParent().getParent();
        ListView<String> list = (ListView<String>) vBox.getChildren().get(5);
        int index = list.getSelectionModel().getSelectedIndex();
        list.getItems().remove(index);

        TextField columnNumber = (TextField)((VBox)((HBox)vBox.getParent()).getChildren().get(0)).getChildren().get(1);
        listChanged.get(Integer.parseInt(columnNumber.getText())-1).remove(index);
    }

    public static void addToList(ActionEvent event) {
        Control control = (Control) event.getSource();
        VBox vBox;
        if(control.getClass().equals(Button.class))
            vBox = (VBox) control.getParent().getParent();
        else
            vBox = (VBox) control.getParent();
        TextField textField = (TextField) vBox.getChildren().get(2);
        if (textField.getText().equals("")) {
            textField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            return;
        }
        textField.setStyle("-fx-border-width: 0px;");
        ColorPicker colorPicker = (ColorPicker) vBox.getChildren().get(3);
        ListView<String> list = (ListView<String>) (vBox).getChildren().get(5);
        String color = colorPicker.getValue().toString();
        String string = textField.getText() + "(#" + color.substring(2, 8) + ")";
        list.getItems().add(string);

        TextField columnNumber = (TextField)((VBox)((HBox)vBox.getParent()).getChildren().get(0)).getChildren().get(1);
        listChanged.get(Integer.parseInt(columnNumber.getText())-1).add("");
        textField.setText("");
    }

    public void onRemoveFromList(ActionEvent event) {
        removeFromList(event);
    }

    public void onAddToList(ActionEvent event) {
        addToList(event);
    }

    public void onMoveUp(ActionEvent actionEvent) {
        changePosition(actionEvent,-1);
    }

    public void onMoveDown(ActionEvent actionEvent) {
        changePosition(actionEvent,1);
    }
    public static void moveUp(ActionEvent actionEvent){
        changePosition(actionEvent,-1);
    }

    public static void moveDown(ActionEvent actionEvent) {
        changePosition(actionEvent,1);
    }

    private static void changePosition(ActionEvent actionEvent, int offset){
        Button button = (Button)actionEvent.getSource();
        HBox hBox = (HBox)button.getParent().getParent();
        VBox vBox = (VBox)hBox.getChildren().get(2);
        ListView<String> listView = (ListView<String>)vBox.getChildren().get(5);

        String item = listView.getSelectionModel().getSelectedItem();
        if(item != null){
            int index = listView.getItems().indexOf(item);
            if(index+offset >= 0 && index+offset <listView.getItems().size()) {
                String tmp = listView.getItems().get(index + offset);
                listView.getItems().set(index + offset, item);
                listView.getItems().set(index, tmp);
                listView.getSelectionModel().select(index + offset);


                int columnPosition = Integer.parseInt(((TextField)((VBox)hBox.getChildren().get(0)).getChildren().get(1)).getText())-1;
                String itemBool = listChanged.get(columnPosition).get(index);
                String tmpBool = listChanged.get(columnPosition).get(index+offset);
                listChanged.get(columnPosition).set(index+offset,itemBool);
                listChanged.get(columnPosition).set(index,tmpBool);

            }
        }
    }

    public void onSetValues(ActionEvent event) {
        VBox vBox = (VBox)((Button)event.getSource()).getParent().getParent();
        ListView<String> listView = (ListView<String>) vBox.getChildren().get(5);
        int index = listView.getSelectionModel().getSelectedIndex();
        if(index >= 0){
            String item = listView.getSelectionModel().getSelectedItem();

            if(updating) {
                int columnPosition = Integer.parseInt(((TextField) ((VBox) ((HBox) vBox.getParent()).getChildren().get(0)).getChildren().get(1)).getText()) - 1;
                listChanged.get(columnPosition).set(index, item);
            }

            TextField textField = (TextField)vBox.getChildren().get(2);
            if(!textField.getText().equals("")){
                item = textField.getText() + item.substring(item.lastIndexOf("("));
            }
            ColorPicker colorPicker = (ColorPicker)vBox.getChildren().get(3);
            item = item.substring(0,item.lastIndexOf("(")+2)+colorPicker.getValue().toString().substring(2,8)+")";
            listView.getItems().set(index,item);
            textField.setText("");
        }
    }
}
