package com.main.datenpunk;

import database.DAO;
import enteties.ChartDescriptor;
import enteties.ColoredObjectTableCell;
import enteties.ObjectTableElement;
import enteties.Status;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.stream.Stream;

public class MainController implements Initializable {


    private final DAO dao = DAO.getInstance();
    private final Singelton singelton = Singelton.getInstance();
    @FXML
    public VBox chartContainer;
    public ChoiceBox chartPresetBox;


    @FXML
    private DatePicker toDatePicker,fromDatePicker;

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
    private ChoiceBox<String> whitelistStatusBox, blacklistStatusBox, presetBox;
    private final ObservableList<Control> controlList = FXCollections.observableArrayList();

    @FXML
    private ListView<String> whitelistNameList, whitelistTypeList, whitelistStatusList, blacklistNameList, blacklistTypeList, blacklistStatusList;
    private final ObservableList<ListView<String>> listViews = FXCollections.observableArrayList();

    @FXML
    private Button addToNameWhitelist,addToTypeWhitelist,addToStatusWhitelist,addToNameBlacklist,addToTypeBlacklist,addToStatusBlacklist,removeFromNameWhitelist,removeFromTypeWhitelist,removeFromStatusWhitelist,removeFromNameBlacklist,removeFromTypeBlacklist,removeFromStatusBlacklist;
    private final ObservableList<Button> addButtons = FXCollections.observableArrayList();
    private final ObservableList<Button> removeButtons = FXCollections.observableArrayList();

    private LocalDate toDate,fromDate;

    private List<String> presets = new ArrayList<>();
    private final List<String> statusNames = new ArrayList<>();

    private ObservableList<ObjectTableElement> objectTableElements = FXCollections.observableArrayList();

    List<ChartDescriptor> charts = new ArrayList<>();
    int chartEditIndex;

    private void getStatuses(){
        List<Status> statuses = dao.selectStatuses();
        Status status;
        for (Status value : statuses) {
            status = value;
            statusNames.add(status.getName());
        }
    }

    public List<String> getWhitelist(){
        List<String> list = new ArrayList<>();

        for (int i = 0; i < listViews.size()/2; i++) {
            list.addAll(listViews.get(i).getItems());
            list.add("");
        }
        return  list;
    }

    public List<String> getBlacklist(){
        List<String> list = new ArrayList<>();

        for (int i = listViews.size()/2; i < listViews.size(); i++) {
            list.addAll(listViews.get(i).getItems());
            list.add("");
        }
        return  list;
    }

    public void selectPresets() {

        presets = new ArrayList<>();

        List<Path> paths;
        try{
            Stream<Path> files = Files.list(Paths.get(singelton.getWorkingDirectory()+"\\Projects\\"+singelton.getCurrentProject()+"\\Presets"));
            paths = files.toList();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String path;
        File file;
        for (Path value : paths) {
            path = value.toString();
            file = new File(path);
            if (file.isDirectory())
                presets.add(path.substring(path.lastIndexOf("\\") + 1));
        }

        presetBox.getItems().setAll(presets);
        presetBox.setValue("Custom");
    }

    public void setPreset(String name){
        presetBox.setValue(name);
    }

    @FXML
    public void updateTable() {

        objectTable.getItems().clear();

        new TableService(objectTable,fromDate,toDate,listViews,statusColumn).start();

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        singelton.setController(this);

        objectTable.setRowFactory( tableView -> {
            TableRow<ObjectTableElement> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 && (!row.isEmpty())){
                    try {
                        openDetailView();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            return row;
        });

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

        controlList.addAll(whitelistNameField,whitelistTypeField,whitelistStatusBox,blacklistNameField,blacklistTypeField,blacklistStatusBox);
        listViews.addAll(whitelistNameList,whitelistTypeList,whitelistStatusList,blacklistNameList,blacklistTypeList, blacklistStatusList);
        addButtons.addAll(addToNameWhitelist,addToTypeWhitelist,addToStatusWhitelist,addToNameBlacklist,addToTypeBlacklist,addToStatusBlacklist);
        removeButtons.addAll(removeFromNameWhitelist,removeFromTypeWhitelist,removeFromStatusWhitelist,removeFromNameBlacklist,removeFromTypeBlacklist,removeFromStatusBlacklist);

        getStatuses();
        selectPresets();
        selectChartPresets();
        setChartPreset("Custom");

        presetBox.setOnAction(this::onPresetChange);
        chartPresetBox.setOnAction(this::loadChartPreset);

        whitelistStatusBox.getItems().setAll(statusNames);
        blacklistStatusBox.getItems().setAll(statusNames);


        updateTable();

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
        stage.setResizable(false);
        stage.show();
    }

    public void onCheckVisible(){               //TODO: known issue: when table columns are switched the wrong column gets hidden

        List<String> columnNames = new ArrayList<>();
        for (TableColumn column: objectTable.getColumns()) {
            columnNames.add(column.getText());
        }

        for (CheckMenuItem checkMenu:checkMenus) {
            int index = columnNames.indexOf(checkMenu.getText());
            objectTable.getColumns().get(index).setVisible(checkMenu.isSelected());
        }
    }

    @FXML
    public void selectToDate() {

        toDate = toDatePicker.getValue();
        //presetBox.setValue("Custom");
        updateTable();
    }
    @FXML
    public void selectFromDate() {

        fromDate = fromDatePicker.getValue();
        //presetBox.setValue("Custom");
        updateTable();
    }

    public String getFromDate(){

        if(fromDate == null)
            return "";
        return fromDate.toString();
    }
    public String getToDate(){
        if(toDate == null)
            return "";
        return toDate.toString();
    }

    public void onResetDates() {
        presetBox.setValue("Custom");
        resetDates();
    }
    private void resetDates(){
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

        presetBox.setValue("Custom");
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
            presetBox.setValue("Custom");
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

    public void onNewPreset() throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("newPreset-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.setTitle("New preset");
        stage.setScene(scene);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(objectTable.getScene().getWindow());
        stage.setResizable(false);

        NewPresetController controller = fxmlLoader.getController();
        controller.setPresets(presets);
        controller.setController(this);

        stage.show();

    }

    public void onDeletePreset() {

        String name = presetBox.getValue();
        if(!name.equals("Custom")) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setContentText("Delete Prefab: " + name);
            if (alert.showAndWait().get() == ButtonType.OK) {
                File file = new File(singelton.getWorkingDirectory() + "\\Projects\\" + singelton.getCurrentProject() + "\\Presets\\" + name);
                file.delete();
                selectPresets();
            }
        }

    }

    public void onResetPresets() {

        presetBox.setValue("Custom");
        for (ListView<String> list : listViews) {
            list.getItems().setAll(new ArrayList<>());
        }
        onResetDates();
        updateTable();

    }

    public void onPresetChange(ActionEvent event) {
        String preset = presetBox.getValue();
        if(!preset.equals("Custom")){
            try {
                String path = singelton.getWorkingDirectory() + "\\Projects\\" + singelton.getCurrentProject() + "\\Presets\\" + preset;
                Scanner scanner = new Scanner(new File(path+"\\dateRange.dtpnk"));

                String next;
                if(scanner.hasNext()){
                    next = scanner.next();
                    if(next.equals(""))
                        fromDate = null;
                    else
                        fromDate = LocalDate.parse(next);
                    fromDatePicker.setValue(fromDate);
                    if(scanner.hasNext()){
                        next = scanner.next();
                        if(next.equals(""))
                            fromDate = LocalDate.now();
                        else
                            toDate = LocalDate.parse(next);
                        toDatePicker.setValue(toDate);
                    }
                }
                else {
                    resetDates();
                }
                scanner.close();

                scanner = new Scanner(new File(path+"\\whitelist.dtpnk"));
                int count = listViews.size();
                for (int i = 0; i < count; i++) {
                    if(i == count/2){
                        scanner.close();
                        scanner = new Scanner(new File(path+"\\blacklist.dtpnk"));
                    }
                    ListView<String> list = listViews.get(i);
                    list.getItems().setAll(new ArrayList<>());
                    while(scanner.hasNext()){
                        String line = scanner.nextLine();
                        if(!line.equals(""))
                            list.getItems().add(line);
                        else
                            break;
                    }
                }
                scanner.close();

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            updateTable();
        }
    }

    public void setCustom() {
        presetBox.setValue("Custom");
    }

    public void showChartOptions(MouseEvent event) {
        VBox vBox = (VBox) ((HBox)event.getSource()).getChildren().get(1);
        vBox.setVisible(true);
    }

    public void hideChartOptions(MouseEvent event) {
        VBox vBox = (VBox) ((HBox)event.getSource()).getChildren().get(1);
        vBox.setVisible(false);
    }

    public void onAddChart() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("newChart-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        Stage stage = new Stage();

        stage.setTitle("New Diagram");
        stage.setScene(scene);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(objectTable.getScene().getWindow());

        stage.setResizable(false);
        stage.show();
    }

    public void addNewChart(ChartDescriptor chartDescriptor){
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

        VBox vBox = new VBox();
        vBox.setVisible(false);

        Button closeButton = new Button("⛌");
        closeButton.setOnAction(this::onDeleteChart);


        Button editButton = new Button("\uD83D\uDD89");
        editButton.setOnAction(this::onEditChart);
        vBox.getChildren().addAll(closeButton,editButton);

        hBox.getChildren().add(vBox);

        chartContainer.getChildren().add(hBox);
        chartContainer.setPrefWidth(Region.USE_COMPUTED_SIZE);

        singelton.threadGenerateChart(chartVBox,chartDescriptor);

    }

    public void setChart(ChartDescriptor chartDescriptor) {
        setChartPreset("Custom");
        charts.set(chartEditIndex,chartDescriptor);
        VBox vBox = ((VBox)((HBox)(chartContainer.getChildren().get(chartEditIndex))).getChildren().get(0));
        singelton.threadGenerateChart(vBox,chartDescriptor);
    }

    private void onEditChart(ActionEvent event){

        try {

            int index = chartContainer.getChildren().indexOf(((Button) event.getSource()).getParent().getParent());
            chartEditIndex = index;
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("newChart-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            Stage stage = new Stage();

            stage.setTitle("Update Diagram");
            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(objectTable.getScene().getWindow());

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
        Button button = (Button)event.getSource();
        HBox hBox = (HBox) button.getParent().getParent();
        alert.setContentText("Do you want to delete this Diagram: \n"+((Chart)((VBox)hBox.getChildren().get(0)).getChildren().get(0)).getTitle());
        if(alert.showAndWait().get() == ButtonType.OK) {
            int index = chartContainer.getChildren().indexOf(hBox);
            VBox vBox = (VBox) hBox.getParent();
            vBox.getChildren().remove(hBox);
            charts.remove(index);
        }
    }

    public void refreshCharts(){
        for(ChartDescriptor chartDescriptor: charts){
            addChart(chartDescriptor);
        }
    }

    public void selectChartPresets(){
        List<String> chartPresets = new ArrayList<>();

        List<Path> paths;
        try{
            Stream<Path> files = Files.list(Paths.get(singelton.getWorkingDirectory()+"\\Projects\\"+singelton.getCurrentProject()+"\\DiagramPresets"));
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
                chartPresets.add(path.substring(path.lastIndexOf("\\") + 1,path.lastIndexOf(".")));
        }

        chartPresetBox.getItems().setAll(chartPresets);
    }

    public void setChartPreset(String name){
        chartPresetBox.setValue(name);
    }

    public void onAddChartPreset() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("newDiagramPreset-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());


        Stage stage = new Stage();

        stage.setTitle("New Diagram Preset");
        stage.setScene(scene);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(objectTable.getScene().getWindow());

        stage.setResizable(false);
        stage.show();

    }
    public void onDeleteChartPreset() {


        try{
            File file = new File(singelton.getWorkingDirectory()+"\\Projects\\"+singelton.getCurrentProject()+"\\DiagramPresets\\"+chartPresetBox.getValue()+".json");
            Files.delete(file.toPath());
        }catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Could not delete chart preset: "+chartPresetBox.getValue());
            alert.show();
        }
        onResetCharts();
        selectChartPresets();

    }

    private void loadChartPreset(Event event){

        if(chartPresetBox.getValue().equals("Custom"))
            return;
        resetCharts();

        JSONParser jsonParser = new JSONParser();

        try(FileReader reader = new FileReader(singelton.getWorkingDirectory()+"\\Projects\\"+singelton.getCurrentProject()+"\\DiagramPresets\\"+chartPresetBox.getValue()+".json")){
            Object obj = jsonParser.parse(reader);
            JSONArray chartList = (JSONArray)obj;

            charts.clear();
            chartList.forEach(chart -> parseChartDescriptor((JSONObject) chart));

        } catch (ParseException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void parseChartDescriptor(JSONObject chart) {

        JSONObject o = (JSONObject)chart.get("chartDescriptor");

        ChartDescriptor chartDescriptor = new ChartDescriptor(
                (String)o.get("title"),
                (String)o.get("xName"),
                (String)o.get("yName"),
                (String)o.get("chartType"),
                (String) o.get("fromDate"),
                (String) o.get("toDate"),
                (List<String>)o.get("seriesList"),
                (boolean)o.get("showPoints"),
                (boolean)o.get("isRelative"),
                (String)o.get("xAxis"),
                (String)o.get("xMin"),
                (String)o.get("xMax"),
                (String)o.get("xType"),
                (String)o.get("yAxis"),
                (String)o.get("yMin"),
                (String)o.get("yMax"),
                Float.parseFloat((String)o.get("stepSize")));

        addChart(chartDescriptor);
    }

    public void onResetCharts() {
        chartPresetBox.setValue("Custom");
        resetCharts();

    }
    private void resetCharts(){
        charts.clear();
        chartContainer.getChildren().clear();
    }
}