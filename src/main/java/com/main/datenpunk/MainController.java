package com.main.datenpunk;

import database.DAO;
import enteties.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Stream;

public class MainController implements Initializable {


    private final DAO dao = DAO.getInstance();
    private final Singleton singleton = Singleton.getInstance();
    @FXML
    public TilePane chartContainer;
    public ChoiceBox<String> chartPresetBox;
    public BorderPane tableWidthContainer;
    public SplitPane maxWidthPane;
    public ScrollPane maxHeightPane;
    @FXML
    private TilePane whiteListContainer, blackListContainer;
    @FXML
    private Menu showHideMenu;


    @FXML
    private DatePicker toDatePicker, fromDatePicker;

    @FXML
    SplitPane objectTable;


    @FXML
    private ChoiceBox<String> presetBox;
    private final ObservableList<Control> controlList = FXCollections.observableArrayList();

    private final ObservableList<ListView<String>> listViews = FXCollections.observableArrayList();

    private final ObservableList<Button> addButtons = FXCollections.observableArrayList();
    private final ObservableList<Button> removeButtons = FXCollections.observableArrayList();

    private LocalDate toDate, fromDate;

    private List<String> presets = new ArrayList<>();
    List<ChartDescriptor> charts = new ArrayList<>();
    int chartEditIndex;

    boolean changingPresets = false;
    boolean switchSelection = false;
    Integer selectedIndex = null;

    private String sortType = "ASC";
    private String sortColumn = "Status";

    List<Float> columnWidths = new ArrayList<>();

    private void getStatuses() {
        for (ColumnInfo columnInfo : singleton.getColumnInfo()) {
            List<Status> list = new ArrayList<>();
            if (columnInfo.colored) {
                list.addAll(dao.selectStatuses(columnInfo.name));
            }
            singleton.choices.add(list);
            singleton.choiceNames.add(columnInfo.name);
        }
    }


    public List<String> getWhitelist(int id) {
        return ((ListView<String>) ((VBox) whiteListContainer.getChildren().get(id)).getChildren().get(3)).getItems();
    }


    public List<String> getBlacklist(int id) {
        return ((ListView<String>) ((VBox) blackListContainer.getChildren().get(id)).getChildren().get(3)).getItems();
    }

    public void selectPresets() {

        presets = new ArrayList<>();

        List<Path> paths;
        try {
            Stream<Path> files = Files.list(Paths.get(singleton.getWorkingDirectory() + "\\Projects\\" + singleton.getCurrentProject() + "\\Presets"));
            paths = files.toList();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String path;
        File file;
        for (Path value : paths) {
            path = value.toString();
            file = new File(path);
            if (file.isFile())
                presets.add(path.substring(path.lastIndexOf("\\") + 1, path.lastIndexOf(".")));
        }

        presetBox.getItems().setAll(presets);
        presetBox.setValue("Custom");
    }

    public void setPreset(String name) {
        presetBox.setValue(name);
    }

    @FXML
    public void updateTable() {

        String sortColumnName = sortColumn;
        for (VBox column : singleton.getColumns()) {
            Button button = (Button) column.getChildren().get(0);
            String columnName = button.getText();
            ListView<String> listView = (ListView<String>) column.getChildren().get(1);
            listView.getItems().setAll(dao.selectMain(fromDate, toDate, listViews, columnName, sortColumnName, sortType));
        }
    }

    ListChangeListener<String> listChangeListener = change -> {
        for (VBox column : singleton.getColumns()) {
            int ROW_HEIGHT = 24;
            ListView<String> listView = (ListView<String>) column.getChildren().get(1);
            listView.setPrefHeight(listView.getItems().size() * ROW_HEIGHT + 2 + ROW_HEIGHT);
        }
    };

    ChangeListener widthListener = (ChangeListener<Number>) (observableValue, number, t1) -> {
        for(int i = 0; i < singleton.getColumns().size();i++){
            columnWidths.set(i,((Double)singleton.getColumns().get(i).getWidth()).floatValue());
        }
    };

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        tableWidthContainer.widthProperty().addListener((observableValue, number, t1) -> setTableWidth());
        maxWidthPane.setMaxWidth(1000);
        getStatuses();
        for (int i = 0; i < singleton.getColumnInfo().size(); i++) {
            ColumnInfo columnInfo = singleton.getColumnInfo().get(i);
            String name = columnInfo.name;
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
            listView.setOnMouseClicked(this::openDetailView);
            listView.getItems().addListener(listChangeListener);
            VBox vBox = new VBox(button, listView);
            objectTable.getItems().add(i, vBox);
            singleton.getColumns().add(vBox);
            singleton.getColumnNames().add(name);
            CheckMenuItem checkMenuItem = new CheckMenuItem(name);
            checkMenuItem.setSelected(true);
            checkMenuItem.setOnAction(this::onCheckVisible);
            showHideMenu.getItems().add(checkMenuItem);

            if (!name.equals("id") && !name.equals("Date")) {
                addFiltersSettings(whiteListContainer, columnInfo, name);
                addFiltersSettings(blackListContainer, columnInfo, name);
            }

        }
        for (int i = 0; i < singleton.getColumns().size(); i++) {
            columnWidths.add(null);
        }
        for (SplitPane.Divider divider:objectTable.getDividers()){
            divider.positionProperty().addListener(widthListener);
        }
        resizeColumns();


        toDate = LocalDate.now();
        toDatePicker.setValue(toDate);


        selectPresets();
        selectChartPresets();
        setChartPreset("Custom");

        presetBox.setOnAction(this::onPresetChange);
        chartPresetBox.setOnAction(this::loadChartPreset);


        updateTable();

    }

    public void setupLater() {
        Window window = objectTable.getScene().getWindow();
        maxWidthPane.setMaxWidth(window.getWidth() - 50);
        maxHeightPane.setMaxHeight(window.getHeight() - 100);
        window.widthProperty().addListener((observableValue, number, t1) -> {
            maxWidthPane.setMaxWidth((Double) t1 - 50);
            chartContainer.setMaxWidth((Double) t1 - 50);
        });
        window.heightProperty().addListener((observableValue, number, t1) -> maxHeightPane.setMaxHeight((Double) t1 - 100));
        maxWidthPane.getDividers().get(0).setPosition(1);
        objectTable.setMaxWidth(window.getWidth() - 75);

        for (int i = 0; i < singleton.getColumnInfo().size(); i++) {
            ListView<String> listView = (ListView<String>) singleton.getColumns().get(i).getChildren().get(1);
            ColumnInfo columnInfo = singleton.getColumnInfo().get(i);


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
                                if (columnInfo.colored) {
                                    for (int i = 0; i < singleton.choices.size(); i++) {
                                        VBox vBox;
                                        Node node = getParent().getParent().getParent().getParent();
                                        if (!node.getClass().equals(VBox.class)) {
                                            node = getParent().getParent().getParent().getParent().getParent();
                                        }
                                        vBox = (VBox) node;

                                        if (((Button) vBox.getChildren().get(0)).getText().equals(singleton.choiceNames.get(i))) {
                                            for (Status status : singleton.choices.get(i)) {
                                                if (item.equals(status.getName())) {
                                                    setStyle("-fx-control-inner-background: " + status.getColor() + ";" +
                                                            "-fx-border-color: transparent;");
                                                } else if (item.equals("") && columnInfo.required) {
                                                    setStyle("-fx-control-inner-background: " + status.getColor() + ";" +
                                                            "-fx-border-color: red;");
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    if (item.equals("") && columnInfo.required) {
                                        setStyle("-fx-border-color: red;");
                                    } else {
                                        setStyle("-fx-border-color: transparent;");
                                    }
                                }
                            }
                        }
                    };
                }
            });

        }
    }

    private void addFiltersSettings(TilePane tilePane, ColumnInfo columnInfo, String name) {
        Label label = new Label(name + ":");
        Control control;


        if (columnInfo.type.equals("Choice")) {
            control = new ChoiceBox<String>();
            control.setPrefWidth(150);

            int index = 0;
            for (int j = 0; j < singleton.choiceNames.size(); j++) {
                if (singleton.choiceNames.get(j).equals(name)) {
                    index = j;
                    break;
                }
            }

            for (Status choice : singleton.choices.get(index)) {
                ((ChoiceBox<String>) control).getItems().add(choice.getName());
            }
        } else {
            control = new TextField();
            ((TextField) control).setOnAction(this::onAddToList);
        }
        controlList.add(control);

        Button addButton = new Button("Add");
        Button removeButton = new Button("Remove");
        addButton.setOnAction(this::onAddToList);
        removeButton.setOnAction(this::onRemoveFromList);
        addButtons.add(addButton);
        removeButtons.add(removeButton);
        BorderPane borderPane = new BorderPane();
        borderPane.setRight(addButton);
        borderPane.setLeft(removeButton);

        ListView<String> filterListView = new ListView<>();
        filterListView.setPrefSize(150, 100);
        filterListView.setOnMouseClicked(this::onListClick);
        listViews.add(filterListView);

        VBox filterVBox = new VBox(label, control, borderPane, filterListView);
        VBox.setMargin(borderPane, new Insets(5, 0, 5, 0));
        tilePane.getChildren().add(filterVBox);
        TilePane.setMargin(filterVBox, new Insets(0, 5, 5, 0));
    }

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

    private void resizeColumns() {
        float sum = 0;
        List<Float> toAdd = new ArrayList<>();
        if (columnWidths.contains(null)) {
            for (int i = 0; i < objectTable.getItems().size(); i++) {
                sum += 1 / ((float) objectTable.getItems().size());
                toAdd.add(sum);
            }
        } else {
            float total = 0;
            float subTotal = 0;
            for (Node node : objectTable.getItems()) {
                int i = singleton.getColumns().indexOf(node);
                total += columnWidths.get(i);

            }
            for (int i = 0; i < objectTable.getItems().size(); i++) {
                Node node = objectTable.getItems().get(i);
                int index = singleton.getColumns().indexOf(node);
                subTotal += columnWidths.get(index);
                Float value = 1 / (total / subTotal);
                toAdd.add(value);

            }
        }
        for (int i = 0; i < objectTable.getDividers().size(); i++) {
            SplitPane.Divider divider = objectTable.getDividers().get(i);
            divider.setPosition(toAdd.get(i));
        }
    }

    private void openDetailView(MouseEvent event) {

        if (event.getClickCount() == 2) {

            ListView<String> idList = null;
            for (VBox column : singleton.getColumns()) {
                if (((Button) column.getChildren().get(0)).getText().equals("id"))
                    idList = (ListView<String>) column.getChildren().get(1);
            }


            assert idList != null;
            if (idList.getSelectionModel().getSelectedItem() != null) {
                try {
                    String currentElement = idList.getSelectionModel().getSelectedItem();

                    FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("detail-view.fxml"));
                    Scene scene = new Scene(fxmlLoader.load());
                    scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/main/datenpunk/application.css")).toExternalForm());

                    Stage stage = new Stage();

                    stage.setTitle("Details");
                    stage.setScene(scene);
                    stage.initModality(Modality.WINDOW_MODAL);
                    stage.initOwner(objectTable.getScene().getWindow());

                    DetailController detailController = fxmlLoader.getController();
                    detailController.setCurrentElement(Integer.parseInt(currentElement));
                    stage.show();
                    detailController.setupLater();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

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
        stage.setResizable(false);
        stage.show();
    }

    public void onCheckVisible(Event event) {
        checkVisible();
    }

    private void checkVisible() {
        objectTable.getItems().clear();
        for (int i = 0; i < singleton.getColumns().size(); i++) {
            CheckMenuItem menuItem = (CheckMenuItem) showHideMenu.getItems().get(i);
            if (menuItem.isSelected()) {
                objectTable.getItems().add(singleton.getColumns().get(i));
            }
        }
        for (SplitPane.Divider divider:objectTable.getDividers()){
            divider.positionProperty().addListener(widthListener);
        }
        setTableWidth();
        resizeColumns();
    }

    private void setTableWidth() {
        objectTable.setMaxWidth(Math.max(objectTable.getItems().size() * 150, tableWidthContainer.getWidth() - 15));
    }

    @FXML
    public void selectToDate() {

        if (!changingPresets) {
            toDate = toDatePicker.getValue();
            updateTable();
        }
    }

    @FXML
    public void selectFromDate() {
        if (!changingPresets) {
            fromDate = fromDatePicker.getValue();
            updateTable();
        }
    }

    public String getFromDate() {

        if (fromDate == null)
            return "";
        return fromDate.toString();
    }

    public String getToDate() {
        if (toDate == null)
            return "";
        return toDate.toString();
    }

    public void onResetDates() {
        presetBox.setValue("Custom");
        resetDates();
    }

    private void resetDates() {

        toDate = LocalDate.now();
        fromDate = null;
        toDatePicker.setValue(toDate);
        fromDatePicker.setValue(fromDate);
        if (!changingPresets)
            updateTable();

    }

    public void onCancel() {
        Stage stage = (Stage) fromDatePicker.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void onAddToList(ActionEvent actionEvent) {

        if (!changingPresets) {
            int i;
            if (actionEvent.getSource().getClass() == Button.class)
                i = addButtons.indexOf(actionEvent.getSource());
            else
                i = controlList.indexOf(actionEvent.getSource());

            Control control = controlList.get(i);
            String text;
            if (control.getClass() == TextField.class) {
                text = ((TextField) control).getText();
                ((TextField) control).setText("");
            } else
                text = ((ChoiceBox<String>) control).getValue();

            presetBox.setValue("Custom");
            listViews.get(i).getItems().add(text);
            updateTable();
        }
    }

    @FXML
    public void onRemoveFromList(ActionEvent actionEvent) {
        removeFromList(removeButtons.indexOf(actionEvent.getSource()));
    }


    private void removeFromList(int id) {
        if (!changingPresets) {
            ListView<String> listView = listViews.get(id);
            if (listView.getSelectionModel().getSelectedItem() != null) {
                listView.getItems().remove(listView.getSelectionModel().getSelectedItem());
                presetBox.setValue("Custom");
                updateTable();
            }
        }
    }

    public void onListClick(MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY)) {
            if (event.getClickCount() == 2) {
                removeFromList(listViews.indexOf(event.getSource()));
            }
        }
    }

    public void onListKey(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.DELETE) || keyEvent.getCode().equals(KeyCode.BACK_SPACE)) {
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
        stage.initOwner(fromDatePicker.getScene().getWindow());
        NewProjectController newProjectController = fxmlLoader.getController();
        newProjectController.setReturnStage((Stage) fromDatePicker.getScene().getWindow());      //TODO: better data transfer
        stage.show();
    }

    public void onProjectSelection() throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("projectSelection-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        Stage stage = (Stage) fromDatePicker.getScene().getWindow();
        stage.setScene(scene);

        ProjectSelectionController controller = fxmlLoader.getController();
        controller.initializeTable();
        stage.show();

    }

    public void onNewPreset() throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("newPreset-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.setTitle("New preset");
        stage.setScene(scene);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(fromDatePicker.getScene().getWindow());
        stage.setResizable(false);

        NewPresetController controller = fxmlLoader.getController();
        controller.setPresets(presets);
        controller.setController(this);

        stage.show();

    }

    public void onDeletePreset() throws IOException {

        String name = presetBox.getValue();
        if (!name.equals("Custom")) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setContentText("Delete Prefab: " + name);
            if (alert.showAndWait().get() == ButtonType.OK) {
                File file = new File(singleton.getWorkingDirectory() + "\\Projects\\" + singleton.getCurrentProject() + "\\Presets\\" + name);
                for (File pFile : Objects.requireNonNull(file.listFiles())) {
                    Files.delete(pFile.toPath());
                }
                Files.delete(file.toPath());
                selectPresets();
            }
        }

    }

    public void onResetPresets() {

        presetBox.setValue("Custom");
        for (ListView<String> list : listViews) {
            list.getItems().setAll(new ArrayList<>());
        }
        for (MenuItem item : showHideMenu.getItems()) {
            ((CheckMenuItem) item).setSelected(true);
        }
        Collections.fill(columnWidths, null);
        checkVisible();
        onResetDates();

    }

    public void onPresetChange(ActionEvent event) {
        String preset = presetBox.getValue();
        if (preset != null && !preset.equals("Custom")) {
            changingPresets = true;

            JSONParser jsonParser = new JSONParser();
            String path = singleton.getWorkingDirectory() + "\\Projects\\" + singleton.getCurrentProject() + "\\Presets\\" + preset + ".json";


            try (FileReader reader = new FileReader(path)) {

                Object obj = jsonParser.parse(reader);
                JSONObject o = (JSONObject) obj;
                Filter filter = new Filter(
                        (String) o.get("start"),
                        (String) o.get("end"),
                        (List<String>) o.get("order"),
                        (List<Boolean>) o.get("visible"),
                        (List<Double>) o.get("widths"),
                        (List<List<String>>) o.get("whitelist"),
                        (List<List<String>>) o.get("blacklist")
                );

                if (filter.start.equals("")) {
                    fromDate = null;
                    fromDatePicker.setValue(null);
                } else {
                    fromDate = LocalDate.parse(filter.start);
                    fromDatePicker.setValue(fromDate);
                }
                if (filter.end.equals("")) {
                    toDate = LocalDate.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault());
                    toDatePicker.setValue(toDate);
                } else {
                    toDate = LocalDate.parse(filter.end);
                    toDatePicker.setValue(toDate);
                }

                if (!filter.widths.isEmpty()) {
                    for (int i = 0; i < columnWidths.size(); i++) {
                        if (i < filter.widths.size())
                            columnWidths.set(i, filter.widths.get(i).floatValue());
                        else
                            columnWidths.set(i, 150f);

                    }
                }

                for (int i = 0; i < whiteListContainer.getChildren().size(); i++) {
                    VBox vBox = (VBox) whiteListContainer.getChildren().get(i);
                    ListView<String> listView = (ListView<String>) vBox.getChildren().get(3);
                    if (!filter.whitelist.isEmpty() && i<filter.whitelist.size())
                        listView.getItems().setAll(filter.whitelist.get(i));

                    else
                        listView.getItems().clear();

                }


                for (int i = 0; i < blackListContainer.getChildren().size(); i++) {
                    VBox vBox = (VBox) blackListContainer.getChildren().get(i);
                    ListView<String> listView = (ListView<String>) vBox.getChildren().get(3);
                    if (!filter.blacklist.isEmpty() && i<filter.blacklist.size())
                        listView.getItems().setAll(filter.blacklist.get(i));

                    else
                        listView.getItems().clear();
                }

                for (int i = 0; i < showHideMenu.getItems().size(); i++) {
                    ((CheckMenuItem) showHideMenu.getItems().get(i)).setSelected(filter.visible.get(i));
                }
                checkVisible();


            } catch (IOException | ParseException | RuntimeException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("This Preset no longer works properly!");
                alert.show();
            }
            changingPresets = false;
            updateTable();
        }
    }

    public void setCustom() {
        presetBox.setValue("Custom");
    }

    public void showChartOptions(MouseEvent event) {
        VBox vBox = (VBox) ((HBox) event.getSource()).getChildren().get(1);
        vBox.setVisible(true);
    }

    public void hideChartOptions(MouseEvent event) {
        VBox vBox = (VBox) ((HBox) event.getSource()).getChildren().get(1);
        vBox.setVisible(false);
    }

    public void onAddChart() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("newChart-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        Stage stage = new Stage();

        stage.setTitle("New Diagram");
        stage.setScene(scene);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(fromDatePicker.getScene().getWindow());

        stage.setResizable(false);
        stage.show();
    }

    public void addNewChart(ChartDescriptor chartDescriptor) {
        setChartPreset("Custom");
        addChart(chartDescriptor);
    }

    public void addChart(ChartDescriptor chartDescriptor) {

        charts.add(chartDescriptor);

        HBox hBox = new HBox();

        VBox chartVBox = new VBox();
        chartVBox.getChildren().add(new PieChart());

        hBox.getChildren().add(chartVBox);
        hBox.setOnMouseEntered(this::showChartOptions);
        hBox.setOnMouseExited(this::hideChartOptions);
        hBox.setPrefWidth(Region.USE_COMPUTED_SIZE);

        VBox vBox = new VBox();
        vBox.setVisible(false);

        Button closeButton = new Button("⛌");
        closeButton.setOnAction(this::onDeleteChart);


        Button editButton = new Button("\uD83D\uDD89");
        editButton.setOnAction(this::onEditChart);
        vBox.getChildren().addAll(closeButton, editButton);

        hBox.getChildren().add(vBox);

        chartContainer.getChildren().add(hBox);
        chartContainer.setPrefWidth(Region.USE_COMPUTED_SIZE);

        singleton.threadGenerateChart(chartVBox, chartDescriptor);

    }

    public void setChart(ChartDescriptor chartDescriptor) {
        setChartPreset("Custom");
        charts.set(chartEditIndex, chartDescriptor);
        VBox vBox = ((VBox) ((HBox) (chartContainer.getChildren().get(chartEditIndex))).getChildren().get(0));
        singleton.threadGenerateChart(vBox, chartDescriptor);
    }

    private void onEditChart(ActionEvent event) {

        try {

            int index = chartContainer.getChildren().indexOf(((Button) event.getSource()).getParent().getParent());
            chartEditIndex = index;
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("newChart-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            Stage stage = new Stage();

            stage.setTitle("Update Diagram");
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(fromDatePicker.getScene().getWindow());

            NewChartController newChartController = fxmlLoader.getController();
            newChartController.loadChart(charts.get(index));

            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private void onDeleteChart(ActionEvent event) {

        setChartPreset("Custom");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        Button button = (Button) event.getSource();
        HBox hBox = (HBox) button.getParent().getParent();
        alert.setContentText("Do you want to delete this Diagram: \n" + ((Chart) ((VBox) hBox.getChildren().get(0)).getChildren().get(0)).getTitle());
        if (alert.showAndWait().get() == ButtonType.OK) {
            int index = chartContainer.getChildren().indexOf(hBox);
            VBox vBox = (VBox) hBox.getParent();
            vBox.getChildren().remove(hBox);
            charts.remove(index);
        }
    }

    public void selectChartPresets() {
        List<String> chartPresets = new ArrayList<>();

        List<Path> paths;
        try {
            Stream<Path> files = Files.list(Paths.get(singleton.getWorkingDirectory() + "\\Projects\\" + singleton.getCurrentProject() + "\\DiagramPresets"));
            paths = files.toList();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String path;
        File file;
        for (Path value : paths) {
            path = value.toString();
            file = new File(path);
            if (file.getName().endsWith(".json"))
                chartPresets.add(path.substring(path.lastIndexOf("\\") + 1, path.lastIndexOf(".")));
        }

        chartPresetBox.getItems().setAll(chartPresets);
    }

    public void setChartPreset(String name) {
        chartPresetBox.setValue(name);
    }

    public void onAddChartPreset() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("newDiagramPreset-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());


        Stage stage = new Stage();

        stage.setTitle("New Diagram Preset");
        stage.setScene(scene);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(fromDatePicker.getScene().getWindow());

        stage.setResizable(false);
        stage.show();

    }

    public void onDeleteChartPreset() {


        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setContentText("Do you really want to delete this Preset?\n" + chartPresetBox.getValue());
            if (alert.showAndWait().get() == ButtonType.OK) {
                File file = new File(singleton.getWorkingDirectory() + "\\Projects\\" + singleton.getCurrentProject() + "\\DiagramPresets\\" + chartPresetBox.getValue() + ".json");
                Files.delete(file.toPath());
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Could not delete chart preset: " + chartPresetBox.getValue());
            alert.show();
        }
        chartPresetBox.setValue("Custom");
        selectChartPresets();

    }

    private void loadChartPreset(Event event) {

        if (chartPresetBox.getValue().equals("Custom"))
            return;
        resetCharts();

        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader(singleton.getWorkingDirectory() + "\\Projects\\" + singleton.getCurrentProject() + "\\DiagramPresets\\" + chartPresetBox.getValue() + ".json")) {
            Object obj = jsonParser.parse(reader);
            JSONArray chartList = (JSONArray) obj;

            charts.clear();
            chartList.forEach(chart -> parseChartDescriptor((JSONObject) chart));

        } catch (ParseException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void parseChartDescriptor(JSONObject chart) {

        JSONObject o = (JSONObject) chart.get("chartDescriptor");

        ChartDescriptor chartDescriptor = new ChartDescriptor(
                (String) o.get("title"),
                (String) o.get("xName"),
                (String) o.get("yName"),
                (String) o.get("chartType"),
                (String) o.get("fromDate"),
                (String) o.get("toDate"),
                (List<String>) o.get("seriesList"),
                (boolean) o.get("showPoints"),
                (boolean) o.get("isRelative"),
                (String) o.get("xAxis"),
                (String) o.get("xMin"),
                (String) o.get("xMax"),
                (String) o.get("xType"),
                (String) o.get("yAxis"),
                (String) o.get("yMin"),
                (String) o.get("yMax"),
                Float.parseFloat((String) o.get("stepSize"))
        );

        addChart(chartDescriptor);
    }

    public void onResetCharts() {
        chartPresetBox.setValue("Custom");
        resetCharts();

    }

    private void resetCharts() {
        charts.clear();
        chartContainer.getChildren().clear();
    }

    @FXML
    private void onOpenSettings() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("newProject-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.setTitle("New Project");
        stage.setScene(scene);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(fromDatePicker.getScene().getWindow());
        NewProjectController newProjectController = fxmlLoader.getController();
        newProjectController.setReturnStage((Stage) fromDatePicker.getScene().getWindow());
        stage.show();
        newProjectController.setUpdating();

    }
}