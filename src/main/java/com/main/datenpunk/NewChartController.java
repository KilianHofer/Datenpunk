package com.main.datenpunk;

import database.DAO;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.time.*;
import java.util.*;

public class NewChartController implements Initializable {

    @FXML
    public ColorPicker colorPicker;
    public CheckBox relativeCheck;
    @FXML
    private ToggleGroup xTypeGroup;
    String xType = "Accumulative";
    @FXML
    private BorderPane chartPane;
    @FXML
    VBox seriesPane;
    @FXML
    private ListView<String> chartSelectionList, seriesList;
    @FXML
    private ChoiceBox<String> xSelectionBox, ySelectionBox, seriesSelectionBox;
    @FXML
    private TextField titleField, xMinField, xNameField, xMaxField, yMinField, yNameField, yMaxField, comparatorField, seriesNameField, rangeField;

    @FXML
    DatePicker fromDatePicker, toDatePicker;

    @FXML
    HBox xOptions, xToggle;

    DAO dao = DAO.getInstance();

    private Chart chart;
    private String currentChartType;

    List<ColumnInfo> columnInfo;

    ObservableList<TextField> textFields = FXCollections.observableArrayList();

    String[] continuousOptions = new String[]{"value", "sum", "average", "greater than", "greater or equal", "less than", "less or equal", "equals"};

    boolean xContiniuous = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        columnInfo = new ArrayList<>();
        columnInfo.add(new ColumnInfo("id", false));
        columnInfo.addAll(dao.selectTableColumns("objects"));
        columnInfo.addAll(dao.selectTableColumns("history"));

        textFields.addAll(xMinField, xMaxField, xNameField, yMinField, yMaxField, yNameField);

        chartSelectionList.getItems().addAll("Line Chart", "Area Chart", "Stacked Area Chart", "Bar Chart", "Stacked Bar Chart", "Pie Chart");
        chartSelectionList.getSelectionModel().selectedItemProperty().addListener((observableValue, s, t1) -> {
            currentChartType = chartSelectionList.getSelectionModel().getSelectedItem();
            changeChart();
        });
        chartSelectionList.getSelectionModel().select(0);

        for (ColumnInfo column : columnInfo) {
            ySelectionBox.getItems().add(column.name);
            if (!(column.discrete && xMinField.isVisible()))
                xSelectionBox.getItems().add(column.name);
        }
        xSelectionBox.getSelectionModel().selectedItemProperty().addListener((observableValue, s, t1) -> hideContinuousOptions());

        ySelectionBox.getSelectionModel().selectedItemProperty().addListener((observableValue, s, t1) -> getAvailableSeries());
        seriesSelectionBox.getSelectionModel().selectedItemProperty().addListener((observableValue, s, t1) -> hideComparatorField());

        ChangeListener<String> boundsListener = (observableValue, s, t1) -> setBounds();

        xMinField.textProperty().addListener(boundsListener);
        xMaxField.textProperty().addListener(boundsListener);
        yMinField.textProperty().addListener(boundsListener);
        yMaxField.textProperty().addListener(boundsListener);


        seriesList.setCellFactory(new Callback<>() {
            @Override
            public ListCell<String> call(ListView<String> stringListView) {
                return new ListCell<>(){
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                            setStyle("-fx-control-opacity: 0;");
                        } else {
                            setText(item);

                            setStyle("-fx-control-inner-background: " + item.substring(item.lastIndexOf("(")+1,item.lastIndexOf(")")) + ";");


                        }
                    }
                };
            }
        });




    }

    private void setBounds() {
        if (xMaxField.isVisible()) {
            String yMax = yMaxField.getText();
            String yMin = yMinField.getText();
            try {
                if (yMax.equals(yMin) && yMax.equals("")) {
                    ((XYChart) chart).getYAxis().setAutoRanging(true);
                } else {
                    Axis<Number> axis = ((XYChart) chart).getYAxis();
                    axis.setAutoRanging(false);
                    if (axis.getClass().equals(NumberAxis.class)) {
                        if (!yMin.equals(""))
                            ((NumberAxis) axis).setLowerBound(Double.parseDouble(yMin));
                        if (!yMax.equals(""))
                            ((NumberAxis) axis).setUpperBound(Double.parseDouble(yMax));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void hideContinuousOptions() {
        String name = xSelectionBox.getValue();
        Boolean discrete = null;
        for (ColumnInfo column : columnInfo) {
            if (column.name.equals(name))
                discrete = column.discrete;
        }
        if (discrete != null) {
            if (discrete) {
                xToggle.setVisible(false);
                rangeField.setVisible(false);
                xContiniuous = false;
            } else {
                xToggle.setVisible(true);
                rangeField.setVisible(true);
                xContiniuous = true;
            }
        }
    }

    private void hideComparatorField() {

        String name = ySelectionBox.getValue();
        Boolean discrete = null;
        for (ColumnInfo column : columnInfo) {
            if (column.name.equals(name))
                discrete = column.discrete;
        }
        if (discrete != null) {
            String selected = seriesSelectionBox.getSelectionModel().getSelectedItem();
            comparatorField.setVisible(!discrete);
        }
    }


    private void getAvailableSeries() {
        seriesList.getItems().setAll(new ArrayList<>());
        seriesSelectionBox.setValue(null);
        String name = ySelectionBox.getValue();
        Boolean discrete = null;
        for (ColumnInfo column : columnInfo) {
            if (column.name.equals(name))
                discrete = column.discrete;
        }
        if (discrete != null) {
            List<String> series = new ArrayList<>();
            if (!discrete) {
                series.addAll(List.of(continuousOptions));
            } else {
                series.add("All");
                System.out.println(dao.selectSeriesOptions(name));
                series.addAll(dao.selectSeriesOptions(name));
            }
            seriesSelectionBox.getItems().setAll(series);
        }
    }

    private void showTextFields(boolean show, boolean bar) {
        for (TextField textField : textFields) {
            if (textField == xMinField || textField == xMaxField)
                textField.setVisible(show != bar);
            else
                textField.setVisible(show);
        }
        xOptions.setVisible(true);
        xToggle.setVisible(true);
        seriesPane.setVisible(true);

    }

    private void changeChart() {                                //changes Layout to fit selected Chart type
        chartPane.setCenter(null);
        switch (currentChartType) {
            case "Line Chart" -> {
                chart = new LineChart<>(new CategoryAxis(), new NumberAxis());
                showTextFields(true, false);
            }
            case "Area Chart" -> {
                chart = new AreaChart<>(new CategoryAxis(), new NumberAxis());
                showTextFields(true, false);
            }
            case "Stacked Area Chart" -> {
                chart = new StackedAreaChart<>(new CategoryAxis(), new NumberAxis());
                showTextFields(true, false);
            }
            case "Bar Chart" -> {
                chart = new BarChart<>(new CategoryAxis(), new NumberAxis());
                showTextFields(true, true);
                xToggle.setVisible(false);
            }
            case "Stacked Bar Chart" -> {
                chart = new StackedBarChart<>(new CategoryAxis(), new NumberAxis());
                showTextFields(true, true);
                xToggle.setVisible(false);
            }
            case "Pie Chart" -> {
                chart = new PieChart();
                showTextFields(false, false);
                xOptions.setVisible(false);
                xToggle.setVisible(false);
                seriesPane.setVisible(false);
            }
        }
        chart.setAnimated(false);
        chartPane.setCenter(chart);

        xSelectionBox.getItems().setAll(new ArrayList<>());
        for (ColumnInfo column : columnInfo) {
            if (!(column.discrete && xMinField.isVisible()))
                xSelectionBox.getItems().add(column.name);
        }
    }

    private void updateChart() {             //TODO: known issue: do not enter 0 as range!!!!!

        if (!chartSelectionList.getSelectionModel().getSelectedItem().equals("Pie Chart")) {
            ((XYChart) chart).getData().clear();
            for (String series : seriesList.getItems()) {
                String range = rangeField.getText();
                if(range.equals("") || !range.matches("[0-9]+")) {
                    rangeError();
                    return;
                }
                int stepSize = Integer.parseInt(range);
                if(stepSize == 0){
                    rangeError();
                    return;
                }

                String value = series.substring(series.indexOf(":") + 2, series.lastIndexOf(" "));

                LocalDate startDataDate = fromDatePicker.getValue();
                LocalDate endDataDate = toDatePicker.getValue();

                String xMin = xMinField.getText();
                String xMax = xMaxField.getText();

                String xAxis = xSelectionBox.getValue();
                String yAxis = ySelectionBox.getValue();

                long startDataTimestamp, endDataTimestamp;

                if (startDataDate == null)
                    startDataTimestamp = 0;
                else
                    startDataTimestamp = ZonedDateTime.of(startDataDate.atTime(23, 59, 59), ZoneId.systemDefault()).toInstant().toEpochMilli();
                if (endDataDate == null)
                    endDataTimestamp = Long.MAX_VALUE;
                else
                    endDataTimestamp = ZonedDateTime.of(endDataDate.atTime(23, 59, 59), ZoneId.systemDefault()).toInstant().toEpochMilli();

                if (xMin.equals(""))
                    xMin = dao.getFirstOrLastValue(true, xAxis);
                if (xMax.equals(""))
                    xMax = dao.getFirstOrLastValue(false, xAxis);



                XYChart.Series<String, Float> plot = new XYChart.Series<>();
                plot.setName(series.substring(0, series.lastIndexOf(":")));
                if (xSelectionBox.getValue().equals("timestamp")) {

                    LocalDate startDate = LocalDate.parse(xMin);
                    LocalDate endDate = LocalDate.parse(xMax);

                    if (startDataDate == null) {
                        startDataDate = Instant.ofEpochMilli(0).atZone(ZoneId.systemDefault()).toLocalDate();
                    }
                    String comperator = "5";        //TODO: read from comperatorfield

                    List<LocalDate> dates = startDate.datesUntil(endDate.plusDays(1)).toList();
                    for (int i = 0; i < dates.size(); i += stepSize) {

                        float total = 1;
                        if (xType.equals("Accumulative")) {
                            if (relativeCheck.isSelected()){
                                total = dao.getValuesByTime(startDataDate, dates.get(i).plusDays(stepSize), yAxis, "All", startDataTimestamp, endDataTimestamp, comperator);
                            }
                            if(total>0)
                                plot.getData().add(new XYChart.Data<>(dates.get(i).toString(), dao.getValuesByTime(startDataDate, dates.get(i).plusDays(stepSize), yAxis, value, startDataTimestamp, endDataTimestamp, comperator)/total));
                            else plot.getData().add(new XYChart.Data<>(dates.get(i).toString(),0f));
                        }
                        else {
                            if(relativeCheck.isSelected()) {
                                total = dao.getValuesByTime(dates.get(i), dates.get(i).plusDays(stepSize), yAxis, "All", startDataTimestamp, endDataTimestamp, comperator);
                            }
                            if(total>0)
                                plot.getData().add(new XYChart.Data<>(dates.get(i).toString(), dao.getValuesByTime(dates.get(i), dates.get(i).plusDays(stepSize), yAxis, value, startDataTimestamp, endDataTimestamp, comperator)/total));
                            else plot.getData().add(new XYChart.Data<>(dates.get(i).toString(),0f));
                        }
                    }
                }
                else{
                    String name = xSelectionBox.getValue();
                    Boolean discrete = null;
                    for (ColumnInfo column : columnInfo) {
                        if (column.name.equals(name))
                            discrete = column.discrete;
                    }
                    if (discrete != null) {
                        if(discrete){
                            //TODO: implement
                        }
                        else {
                            //TODO: Implement
                        }
                    }
                }
                ((XYChart) chart).getData().add(plot);
                chart.setAnimated(false);
            }
            List<String> colors = new ArrayList<>();

            //TODO: check for chart type

            for (LineChart.Series<?, ?> plot : ((XYChart<?, ?>) chart).getData()) {         //sets line and symbol colors
                int index = ((LineChart<?, ?>) chart).getData().indexOf(plot);          //I hate all of this but at least it's somewhat readable
                String series = seriesList.getItems().get(index);
                String color = series.substring(series.lastIndexOf("(") + 1, series.lastIndexOf(")"));
                colors.add(color);
                plot.getNode().lookup(".chart-series-line").setStyle("-fx-stroke: " + color);
                for (XYChart.Data<?, ?> data : plot.getData())
                    data.getNode().lookup(".chart-line-symbol").setStyle("-fx-background-color: " + color +",whitesmoke;");

            }
            chart.applyCss();               //generates legend
            chartPane.setCenter(chart);
            for (Node node : chart.lookupAll(".chart-legend-item-symbol")) {        //sets legend colors
                for (String styleClass : node.getStyleClass()){
                    if (styleClass.startsWith("series")) {
                        final int index = Integer.parseInt(styleClass.substring(6));
                        node.setStyle("-fx-background-color: " + colors.get(index) + ",whitesmoke;");
                    }
                }


            }

        }
        else{
            //TODO: implement
        }

    }

    private void rangeError() {
        String range = rangeField.getText();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        if(range.equals(""))
            alert.setContentText("range cannot be empty");
        else
            alert.setContentText(rangeField.getText()+" is not a valid Range");
        alert.showAndWait();
    }

    public void onSeriesAdd() {

        if (seriesSelectionBox.getValue() != null) {
            String color = "#" + colorPicker.getValue().toString().substring(2,8);
            String function;
            String value = seriesSelectionBox.getValue();
            if (comparatorField.isVisible() && !value.equals("value") && !value.equals("sum") && !value.equals("average")) {
                function = value + " " + comparatorField.getText();
            } else {
                function = value;
            }
            if (seriesNameField.getText().equals(""))
                seriesList.getItems().add(function + ": " + function + " ("+color+")");
            else
                seriesList.getItems().add(seriesNameField.getText() + ": " + function + " ("+color+")");
        }

    }

    public void onSeriesDelete() {

        if (seriesList.getSelectionModel().getSelectedItem() != null) {
            seriesList.getItems().remove(seriesList.getSelectionModel().getSelectedItem());
        }

    }

    public void onCancel() {
        ((Stage) chartPane.getScene().getWindow()).close();
    }

    public void onCreate() {
        updateChart();
    }

    public void onSeriesListClick(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            if (mouseEvent.getClickCount() == 2) {
                onSeriesDelete();
            }
        }
    }

    public void onAddAll() {            //TODO: check for comparator dependency

        List<String> allSeries = seriesSelectionBox.getItems();

        for (int i = 1; i < allSeries.size(); i++) {

            String function;
            if (comparatorField.isVisible() && !allSeries.get(i).equals("value") && !allSeries.get(i).equals("sum") && !allSeries.get(i).equals("average")) {
                function = allSeries.get(i) + " " + comparatorField.getText();
            } else {
                function = allSeries.get(i);
            }
            int rand = new Random().nextInt(16777215);      //random color, 16777215 is max color value #FFFFFF


            StringBuilder color = new StringBuilder(Integer.toHexString(rand));

            while (color.length() < 6){
                color.insert(0, "0");
            }

            seriesList.getItems().add(function + ": " + function + " (#"+color+")");

        }
    }

    public void onChooseX(ActionEvent event) {
        xType = ((RadioButton) event.getSource()).getText();
    }
    @FXML
    public void onChangeColor() {
        if(seriesList.getSelectionModel().getSelectedItem() != null){
            String toChange = seriesList.getSelectionModel().getSelectedItem();
            int index = seriesList.getItems().indexOf(toChange);
            toChange = toChange.substring(0, toChange.lastIndexOf("(")+1)+"#"+colorPicker.getValue().toString().substring(2,8)+")";
            seriesList.getItems().set(index,toChange);
        }
    }

    public void onChangeName() {

        if(seriesList.getSelectionModel().getSelectedItem() != null && !seriesNameField.getText().equals("")){
            String toChange = seriesList.getSelectionModel().getSelectedItem();
            int index = seriesList.getItems().indexOf(toChange);
            toChange = seriesNameField.getText() + toChange.substring(toChange.lastIndexOf(":"));
            seriesList.getItems().set(index,toChange);
        }
    }
}
