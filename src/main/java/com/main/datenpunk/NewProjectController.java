package com.main.datenpunk;

import database.DAO;
import enteties.Status;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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


    ChangeListener<String> positionListener = new ChangeListener<>() {

        @Override
        public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
            if (!changingOrder) {
                TextField changedField = (TextField) nameField.getScene().focusOwnerProperty().get();
                if (t1.matches("[1-9][0-9]?+") && Integer.parseInt(t1) <= columnContainer.getChildren().size()) {
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
                nameField.getScene().focusOwnerProperty().get().setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            } else {
                nameField.getScene().focusOwnerProperty().get().setStyle("-fx-border-width: 0px;");
            }
        }
    };

    ChangeListener<String> numberListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
            TextField changedField = (TextField) nameField.getScene().focusOwnerProperty().get();
            if (t1.matches("[1-9][0-9]+?")) {
                changedField.setStyle("-fx-border-width: 0px;");
            } else {
                changedField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            }
        }
    };

    ChangeListener<String> choiceListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {

            ChoiceBox<String> choiceBox = (ChoiceBox<String>) nameField.getScene().focusOwnerProperty().get();
            VBox vBox = (VBox) choiceBox.getParent();

            for (int i = 2; i < vBox.getChildren().size(); i++) {
                vBox.getChildren().remove(i);
            }

            switch (t1) {
                case "Text" -> {
                    TextField textField = new TextField();
                    textField.setPromptText("Max. Length");
                    textField.textProperty().addListener(numberListener);
                    vBox.getChildren().add(textField);
                    VBox.setMargin(textField, new Insets(5, 0, 0, 0));
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
                    Button upButton = new Button("▲");
                    upButton.setStyle("-fx-font-size: 10");
                    upButton.setOnAction(NewProjectController::moveUp);
                    Button downButton = new Button("▼");
                    downButton.setStyle("-fx-font-size: 10");
                    downButton.setOnAction(NewProjectController::moveDown);
                    HBox hBox = new HBox(upButton,downButton);
                    hBox.setAlignment(Pos.CENTER);
                    borderPane.setCenter(hBox);
                    vBox.getChildren().add(borderPane);

                    ListView<String> listView = new ListView<>();
                    listView.setPrefSize(150, 150);
                    listView.setCellFactory(cellfactory);
                    vBox.getChildren().add(listView);

                    Insets insets = new Insets(5, 0, 5, 0);

                    VBox.setMargin(textField, insets);
                    VBox.setMargin(borderPane, insets);
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

        for (int i = 0; i < columns.size(); i++) {
            names.add(getColumnNameField(i).getText().toLowerCase());
            types.add(getColumnChoiceBox(i).getValue().toLowerCase());
            if (getColumnHistoryCheck(i).isSelected())
                tables.add("history");
            else
                tables.add("objects");
            positions.add(Integer.parseInt(getColumnPositionField(i).getText()));
        }

        dao.createColumnTable(names, types, tables, positions);
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
                }
            }
            openProject();

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void openProject() {
        String name = nameField.getText();

        if (!dao.connectToDB("", "postgres", singleton.getPassword())) {
            deleteCreatedData();
            openDBPasswordWindow();
            return;
        }
        dao.createDatabase(name);
        if (!dao.connectToDB("datenpunk_" + name, "postgres", singleton.getPassword())) {
            deleteCreatedData();
            return;
        }

        createTables();

        if (singleton.getColumns() != null)
            singleton.getColumns().clear();
        if (singleton.getColumnInfo() != null)
            singleton.getColumnInfo().clear();
        singleton.choices.clear();
        singleton.choiceNames.clear();

        singleton.setCurrentProject(name);
        singleton.setColumnInfo();

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
            Stage stage = returnStage;
            stage.setTitle("Datenpunk");
            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/main/datenpunk/application.css")).toExternalForm());
            stage.setScene(scene);
            MainController controller = fxmlLoader.getController();
            singleton.setController(controller);

            stage.setMaximized(true);
            stage.setResizable(true);
            stage.show();
            controller.setupLater();

            stage = (Stage) nameField.getScene().getWindow();
            stage.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void openDBPasswordWindow(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("databaseConnection-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            DatabaseConnectionController connectionController = fxmlLoader.getController();
            connectionController.method = this::onCreate;
            stage.setTitle("Connect to Database");
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(nameField.getScene().getWindow());
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void onCancel() {
        Stage stage = (Stage) nameField.getScene().getWindow();
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
        VBox old = (VBox) columnContainer.getChildren().get(oldValue);
        columnContainer.getChildren().remove(old);
        columnContainer.getChildren().add(newValue - 1, old);
        for (int i = 0; i < columnContainer.getChildren().size(); i++) {
            getColumnPositionField(i).setText(String.valueOf(i + 1));
        }
        changingOrder = false;

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
        return (CheckBox) ((VBox) getColumnContainer(id).getChildren().get(1)).getChildren().get(2);
    }

    private ChoiceBox<String> getColumnChoiceBox(int id) {
        return (ChoiceBox<String>) ((VBox) getColumnContainer(id).getChildren().get(2)).getChildren().get(1);
    }

    private TextField getColumnMaxLengthField(int id) {
        return (TextField) ((VBox) getColumnContainer(id).getChildren().get(2)).getChildren().get(2);
    }

    private ListView<String> getColumnSelectionList(int id) {
        return (ListView<String>) ((VBox) getColumnContainer(id).getChildren().get(2)).getChildren().get(5);
    }

    public void onAdd() {

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

        VBox second = new VBox(nameLabel, nameField, trackHistory);
        VBox.setMargin(nameField, new Insets(0, 0, 5, 0));


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
    }

    public static void removeFromList(ActionEvent event) {
        ListView<String> list = (ListView<String>) ((VBox) ((Button) event.getSource()).getParent().getParent()).getChildren().get(5);
        list.getItems().remove(list.getSelectionModel().getSelectedItem());

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
        VBox vBox = (VBox)button.getParent().getParent().getParent();
        ListView<String> listView = (ListView<String>)vBox.getChildren().get(5);

        String item = listView.getSelectionModel().getSelectedItem();
        if(item != null){
            int index = listView.getItems().indexOf(item);
            String tmp = listView.getItems().get(index+offset);
            listView.getItems().set(index+offset,item);
            listView.getItems().set(index,tmp);
            listView.getSelectionModel().select(index+offset);
        }

    }
}
