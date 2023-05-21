package com.main.datenpunk;

import database.DAO;
import enteties.ColumnInfo;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.time.*;
import java.util.*;

public class NewChartController implements Initializable {

    @FXML
    public ColorPicker colorPicker;
    public CheckBox relativeCheck, pointCheck;
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
        columnInfo.add(new ColumnInfo("objects","id", false));
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

        ySelectionBox.getSelectionModel().selectedItemProperty().addListener((observableValue, s, t1) -> {
            getAvailableSeries();
            hideComparatorField();
        });

        ChangeListener<String> boundsListener = (observableValue, s, t1) -> setBounds();

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


        ChangeListener<Object> changeChartListener = (observableValue, o, t1) -> {
            xType = ((RadioButton)xTypeGroup.getSelectedToggle()).getText();
            setPoints(pointCheck.isSelected());
            updateChart();
            setBounds();

        };

        fromDatePicker.valueProperty().addListener(changeChartListener);
        toDatePicker.valueProperty().addListener(changeChartListener);
        xMinField.textProperty().addListener(changeChartListener);
        xMaxField.textProperty().addListener(changeChartListener);
        ySelectionBox.getSelectionModel().selectedItemProperty().addListener(changeChartListener);
        xSelectionBox.getSelectionModel().selectedItemProperty().addListener(changeChartListener);
        rangeField.textProperty().addListener(changeChartListener);
        relativeCheck.selectedProperty().addListener(changeChartListener);
        pointCheck.selectedProperty().addListener(changeChartListener);
        xTypeGroup.selectedToggleProperty().addListener(changeChartListener);
        seriesList.getItems().addListener((ListChangeListener<String>) change -> {
            updateChart();
            setBounds();
        });
        seriesList.getSelectionModel().selectedItemProperty().addListener((observableValue, s, t1) -> {
            if(t1 != null) {
                String hex = t1.substring(t1.lastIndexOf("(") + 1, t1.lastIndexOf(")"));
                Color color = new Color((float) Integer.parseInt(hex.substring(1, 3), 16) / 255, (float) Integer.parseInt(hex.substring(3, 5), 16) / 255, (float) Integer.parseInt(hex.substring(5, 7), 16) / 255, 1f);
                colorPicker.setValue(color);
            }
        });

        comparatorField.textProperty().addListener((observableValue, s, t1) -> comparatorValid());


    }

    private void setPoints(boolean points){
        if(chart.getClass().equals(LineChart.class))
            ((LineChart)chart).setCreateSymbols(points);
        else if(chart.getClass().equals(AreaChart.class))
            ((AreaChart)chart).setCreateSymbols(points);
        else if(chart.getClass().equals(StackedAreaChart.class))
            ((StackedAreaChart)chart).setCreateSymbols(points);
    }

    private void setBounds() {
        if (yMaxField.isVisible()) {
            String yMax = yMaxField.getText();
            String yMin = yMinField.getText();
            try {
                if (yMax.equals(yMin) && yMax.equals("")) {
                    ((XYChart) chart).getYAxis().setAutoRanging(true);
                    yMinField.setStyle("fx-border-width: 0px;");
                    yMaxField.setStyle("fx-border-width: 0px;");

                } else {
                    Axis<Number> axis = ((XYChart) chart).getYAxis();
                    axis.setAutoRanging(false);
                    if (axis.getClass().equals(NumberAxis.class)) {
                        if (!yMin.equals("")) {
                            if (yMin.matches("[-]?[0-9]+[.]?[0-9]?+")) {
                                ((NumberAxis) axis).setLowerBound(Double.parseDouble(yMin));
                                yMinField.setStyle("fx-border-width: 0px;");
                            }
                            else{
                                yMinField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                            }
                        }
                        else
                            yMinField.setStyle("fx-border-width: 0px;");

                        if (!yMax.equals("")) {
                            if (yMax.matches("[-]?[0-9]+[.]?[0-9]?+")) {
                                ((NumberAxis) axis).setUpperBound(Double.parseDouble(yMax));
                                yMaxField.setStyle("fx-border-width: 0px;");
                            } else {
                                yMaxField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                            }
                        }
                        else
                            yMaxField.setStyle("fx-border-width: 0px;");
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
                xMinField.setVisible(false);
                xMaxField.setVisible(false);
                rangeField.setVisible(false);
                xContiniuous = false;
            } else {
                xToggle.setVisible(true);
                xMinField.setVisible(true);
                xMaxField.setVisible(true);
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
                System.out.println(dao.selectColumnEntries(name));
                series.addAll(dao.selectColumnEntries(name));
            }
            seriesSelectionBox.getItems().setAll(series);
        }
    }

    private void showTextFields(boolean show, boolean points) {
        for (TextField textField : textFields) {
            textField.setVisible(show);
        }
        xOptions.setVisible(true);
        xToggle.setVisible(true);
        seriesPane.setVisible(true);
        pointCheck.setVisible(points);
    }

    private void changeChart() {                                //changes Layout to fit selected Chart type
        chartPane.setCenter(null);
        switch (currentChartType) {
            case "Line Chart" -> {
                chart = new LineChart<>(new CategoryAxis(), new NumberAxis());
                showTextFields(true, true);
            }
            case "Area Chart" -> {
                chart = new AreaChart<>(new CategoryAxis(), new NumberAxis());
                showTextFields(true, true);
            }
            case "Stacked Area Chart" -> {
                chart = new StackedAreaChart<>(new CategoryAxis(), new NumberAxis());
                showTextFields(true, true);
            }
            case "Bar Chart" -> {
                chart = new BarChart<>(new CategoryAxis(), new NumberAxis());
                showTextFields(true, false);
            }
            case "Stacked Bar Chart" -> {
                chart = new StackedBarChart<>(new CategoryAxis(), new NumberAxis());
                showTextFields(true, false);
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

        String previous = xSelectionBox.getValue();
        xSelectionBox.getItems().setAll(new ArrayList<>());
        for (ColumnInfo column : columnInfo) {
            if (!(column.discrete && !(chart.getClass().equals(BarChart.class) || chart.getClass().equals(StackedBarChart.class))))
                xSelectionBox.getItems().add(column.name);
        }
        if(xSelectionBox.getItems().contains(previous))
            xSelectionBox.setValue(previous);
    }

    private void updateChart() {

        if (!chartSelectionList.getSelectionModel().getSelectedItem().equals("Pie Chart")) {
            ((XYChart) chart).getData().clear();
            for (String series : seriesList.getItems()) {
                String range = rangeField.getText();

                String value = series.substring(series.indexOf(":") + 2, series.lastIndexOf(" "));

                String xMin = xMinField.getText();
                String xMax = xMaxField.getText();


                String xAxis = xSelectionBox.getValue();
                String yAxis = ySelectionBox.getValue();

                for (ColumnInfo info:columnInfo) {
                    if(info.name.equals(xAxis))
                        xAxis = info.table + "." + xAxis;
                    if(info.name.equals(yAxis))
                        yAxis = info.table + "." + yAxis;
                }


                LocalDate startDataDate, endDataDate;
                long startDataTimestamp, endDataTimestamp;

                if(fromDatePicker.getValue() != null) {
                    startDataDate = fromDatePicker.getValue();
                    startDataTimestamp = ZonedDateTime.of(startDataDate.atStartOfDay(), ZoneId.systemDefault()).toInstant().toEpochMilli();
                }
                else {
                    startDataTimestamp = 0;
                    startDataDate = Instant.ofEpochMilli(0).atZone(ZoneId.systemDefault()).toLocalDate();
                }
                if(toDatePicker.getValue() != null) {
                    endDataDate = toDatePicker.getValue().plusDays(1);
                    endDataTimestamp = ZonedDateTime.of(endDataDate.atStartOfDay(), ZoneId.systemDefault()).toInstant().toEpochMilli();
                }
                else {
                    endDataTimestamp = Long.MAX_VALUE;
                }

                String comparator = series.substring(series.lastIndexOf(" ")+1,series.lastIndexOf("("));

                String name = xSelectionBox.getValue();
                Boolean discrete = null;
                for (ColumnInfo column : columnInfo) {
                    if (column.name.equals(name))
                        discrete = column.discrete;
                }
                if (discrete != null) {
                    XYChart.Series<String, Float> plot = new XYChart.Series<>();
                    plot.setName(series.substring(0, series.lastIndexOf(":")));
                    if (discrete) {
                        for(String category:dao.selectColumnEntries(xAxis)) {
                            float total = 1f;
                            if (relativeCheck.isSelected()) {
                                total = dao.getXYValues(null, null,category, yAxis, "All", startDataTimestamp, endDataTimestamp, comparator,xAxis);
                            }
                            if (total > 0)
                                plot.getData().add(new XYChart.Data<>(category, dao.getXYValues(null, null,category, yAxis, value, startDataTimestamp, endDataTimestamp, comparator,xAxis) / total));
                            else plot.getData().add(new XYChart.Data<>(category, 0f));
                        }
                    } else {

                        if(range.equals("") || !range.matches("[0-9]+[.]?[0-9]?+")) {
                            rangeField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                            return;
                        }
                        float stepSize = Float.parseFloat(range);
                        if(stepSize == 0){
                            rangeField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                            return;
                        }
                        rangeField.setStyle("--fx-border-width: 0px;");

                        if (xMin.equals(""))
                            xMin = dao.getFirstOrLastValue(true, xAxis);
                        if (xMax.equals(""))
                            xMax = dao.getFirstOrLastValue(false, xAxis);

                        if (xAxis.equals("history.timestamp")) {

                            if(!range.matches("[0-9]+")) {
                                rangeField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                                return;
                            }

                            if(xMinField.getText().equals(""))
                                xMin = Instant.ofEpochMilli(Long.parseLong(xMin)).atZone(ZoneId.systemDefault()).toLocalDate().toString();
                            if(xMaxField.getText().equals(""))
                                xMax = Instant.ofEpochMilli(Long.parseLong(xMax)).atZone(ZoneId.systemDefault()).toLocalDate().toString();

                            LocalDate startDate, endDate;
                            try {
                                startDate = LocalDate.parse(xMin);
                                xMinField.setStyle("-fx-border-width: 0px;");
                            }catch (Exception e){
                                xMinField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                                return;
                            }
                            try {
                                endDate = LocalDate.parse(xMax);
                                xMaxField.setStyle("-fx-border-width: 0px;");
                            }catch (Exception e){
                                xMaxField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                                return;
                            }

                            List<LocalDate> xValues = startDate.datesUntil(endDate.plusDays(1)).toList();
                            for (int i = 0; i < xValues.size(); i += stepSize) {

                                float total = 1f;
                                if (xType.equals("Accumulative")) {
                                    if (relativeCheck.isSelected()) {
                                        total = dao.getValuesByTime(startDataDate, xValues.get(i).plusDays((long) stepSize), yAxis, "All", startDataTimestamp, endDataTimestamp, comparator);
                                    }
                                    if (total > 0)
                                        plot.getData().add(new XYChart.Data<>(xValues.get(i).toString().substring(0,10), dao.getValuesByTime(startDataDate, xValues.get(i).plusDays((long) stepSize), yAxis, value, startDataTimestamp, endDataTimestamp, comparator) / total));
                                    else plot.getData().add(new XYChart.Data<>(xValues.get(i).toString().substring(0,10), 0f));
                                } else {
                                    if (relativeCheck.isSelected()) {
                                        total = dao.getValuesByTime(xValues.get(i), xValues.get(i).plusDays((long) stepSize), yAxis, "All", startDataTimestamp, endDataTimestamp, comparator);
                                    }
                                    if (total > 0)
                                        plot.getData().add(new XYChart.Data<>(xValues.get(i).toString().substring(0,10), dao.getValuesByTime(xValues.get(i), xValues.get(i).plusDays((long) stepSize), yAxis, value, startDataTimestamp, endDataTimestamp, comparator) / total));
                                    else plot.getData().add(new XYChart.Data<>(xValues.get(i).toString().substring(0,10), 0f));
                                }
                            }
                        } else {


                            float xMinF = Float.parseFloat(xMin);
                            float xMaxF = Float.parseFloat(xMax);

                            for (float i = xMinF; i < xMaxF; i += stepSize) {

                                float total = 1;
                                if (xType.equals("Accumulative")) {
                                    if (relativeCheck.isSelected()) {
                                        total = dao.getXYValues(xMinF, i+stepSize,null, yAxis, "All", startDataTimestamp, endDataTimestamp, comparator,xAxis);
                                    }
                                    if (total > 0)
                                        plot.getData().add(new XYChart.Data<>(String.valueOf(i), dao.getXYValues(xMinF, i+stepSize,null, yAxis, value, startDataTimestamp, endDataTimestamp, comparator,xAxis) / total));
                                    else plot.getData().add(new XYChart.Data<>(String.valueOf(i), 0f));
                                } else {
                                    if (relativeCheck.isSelected()) {
                                        total = dao.getXYValues(i, i+stepSize,null, yAxis, "All", startDataTimestamp, endDataTimestamp, comparator,xAxis);
                                    }
                                    if (total > 0)
                                        plot.getData().add(new XYChart.Data<>(String.valueOf(i), dao.getXYValues(i, i+stepSize,null, yAxis, value, startDataTimestamp, endDataTimestamp, comparator,xAxis) / total));
                                    else plot.getData().add(new XYChart.Data<>(String.valueOf(i), 0f));
                                }
                            }

                        }
                    }
                    ((XYChart<String,Float>) chart).getData().add(plot);
                    chart.setAnimated(false);
                }

            }
            List<String> colors = new ArrayList<>();

            for (XYChart.Series<?, ?> plot : ((XYChart<?, ?>) chart).getData()) {         //sets line and symbol colors
                int index = ((XYChart<?, ?>) chart).getData().indexOf(plot);          //I hate all of this but at least it's somewhat readable
                String series = seriesList.getItems().get(index);
                String color = series.substring(series.lastIndexOf("(") + 1, series.lastIndexOf(")"));
                colors.add(color);

                Class<? extends Chart> chartClass = chart.getClass();



                if(chartClass.equals(LineChart.class)) {
                    plot.getNode().lookup(".chart-series-line").setStyle("-fx-stroke: " + color);
                    if(pointCheck.isSelected())
                        for (XYChart.Data<?, ?> data : plot.getData())
                            data.getNode().lookup(".chart-line-symbol").setStyle("-fx-background-color: " + color + ",whitesmoke;");
                }
                else if (chartClass.equals(AreaChart.class) || chartClass.equals(StackedAreaChart.class)){

                    plot.getNode().lookup(".chart-series-area-line").setStyle("-fx-stroke: " + color);
                    plot.getNode().lookup(".chart-series-area-fill").setStyle("-fx-fill: rgba("+Integer.parseInt(color.substring(1,3),16)+","+Integer.parseInt(color.substring(3,5),16)+","+Integer.parseInt(color.substring(5),16)+",0.15);" );
                    if(pointCheck.isSelected())
                        for (XYChart.Data<?, ?> data : plot.getData())
                            data.getNode().lookup(".chart-area-symbol").setStyle("-fx-background-color: " + color + ",whitesmoke;");
                }
                else if(chartClass.equals(BarChart.class) || chartClass.equals(StackedBarChart.class)){
                    for(XYChart.Data<?,?> data : plot.getData()) {
                        data.getNode().lookup(".chart-bar").setStyle("-fx-bar-fill: " + color);
                    }
                }
            }
            chart.applyCss();               //generates legend
            for (Node node : chart.lookupAll(".chart-legend-item-symbol")) {        //sets legend colors
                for (String styleClass : node.getStyleClass()){
                    if (styleClass.startsWith("series")) {
                        final int index = Integer.parseInt(styleClass.substring(6));
                        String inside  = ";";
                        if(!chart.getClass().equals(BarChart.class) && !chart.getClass().equals(StackedBarChart.class))
                            inside = ",whitesmoke;";
                        node.setStyle("-fx-background-color: " + colors.get(index) + inside);
                    }
                }
            }

        }
        else{
            //TODO: implement
        }

    }

    private void addToSeries(String value,String color){

            String function;

            if (comparatorField.isVisible() && !value.equals("value") && !value.equals("sum") && !value.equals("average")) {
                if(!comparatorValid())
                    return;
                String comparator = comparatorField.getText();
                if(comparator.equals(""))
                    comparator = "0";
                function = value + " " + comparator;
            } else {
                function = value +" ";
            }
            if (seriesNameField.getText().equals(""))
                seriesList.getItems().add(function + ": " + function + "("+color+")");
            else
                seriesList.getItems().add(seriesNameField.getText() + ": " + function + " ("+color+")");

    }

    public void onSeriesAdd() {


        if (seriesSelectionBox.getValue() != null) {
            String color = "#" + colorPicker.getValue().toString().substring(2, 8);
            String value = seriesSelectionBox.getValue();
            addToSeries(value, color);
        }

    }

    public void onAddAll() {            //TODO: check for comparator dependency

        List<String> allSeries = seriesSelectionBox.getItems();

        for (int i = 1; i < allSeries.size(); i++) {

            int rand = new Random().nextInt(0xffffff);
            StringBuilder color = new StringBuilder(Integer.toHexString(rand));

            while (color.length() < 6){
                color.insert(0, "0");
            }
            color.insert(0,"#");
            addToSeries(allSeries.get(i), String.valueOf(color));

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

    @FXML
    public void onChangeValue() {
        if(seriesList.getSelectionModel().getSelectedItem() != null && !comparatorField.getText().equals("")){
            String toChange = seriesList.getSelectionModel().getSelectedItem();
            int index = seriesList.getItems().indexOf(toChange);
            String value = toChange.substring(toChange.lastIndexOf(" ")+1,toChange.lastIndexOf("("));
            if(value.equals("") || !comparatorValid())
                return;
            String name = toChange.substring(0,toChange.lastIndexOf(":"));
            String command = toChange.substring(toChange.lastIndexOf(":")+2,toChange.lastIndexOf("("));
            String tmp = command.substring(0,command.lastIndexOf(" "));
            String newCommand = tmp+" " +comparatorField.getText();
            if(name.equals(command))
                toChange = newCommand + ": " + newCommand+toChange.substring(toChange.lastIndexOf("("));
            else
                toChange = toChange.substring(0,toChange.lastIndexOf(":")+2) + newCommand +"(" + toChange.substring(toChange.lastIndexOf("#"));
            seriesList.getItems().set(index,toChange);
            seriesList.getSelectionModel().select(index);
        }
    }

    private boolean comparatorValid() {
        String comparator = comparatorField.getText();
        if(!comparator.equals("")){
            if(comparator.matches("[-]?[0-9]+[.]?[0-9]?+")){
                comparatorField.setStyle("-fx-border-width: 0px;");
                return true;
            }
        }
        else {
            comparatorField.setStyle("-fx-border-width: 0px;");
            return true;
        }
        comparatorField.setStyle("-fx-border-color: red;-fx-border-width: 2px;");
        return false;

    }
}
