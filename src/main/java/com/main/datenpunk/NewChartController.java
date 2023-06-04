package com.main.datenpunk;

import database.DAO;
import enteties.ChartDescriptor;
import enteties.ColumnInfo;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
    public Button createButton;
    public VBox chartContainer;
    MainController controller;
    @FXML
    public ColorPicker colorPicker;
    public CheckBox relativeCheck, pointCheck;
    @FXML
    private ToggleGroup xTypeGroup;
    String xType = "Accumulative";
    @FXML
    private BorderPane chartPane;
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
    private ChartDescriptor chartDescriptor;
    private String currentChartType;


    ObservableList<TextField> textFields = FXCollections.observableArrayList();

    String[] continuousOptions = new String[]{"value", "sum", "average", "greater than", "greater or equal", "less than", "less or equal", "equals"};

    boolean xContiniuous = false;
    boolean update = false;
    boolean updating = false;

    Singleton singleton = Singleton.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        controller = singleton.getController();


        textFields.addAll(xMinField, xMaxField, xNameField, yMinField, yMaxField, yNameField);

        chartSelectionList.getItems().addAll("Line Chart", "Area Chart", "Stacked Area Chart", "Bar Chart", "Stacked Bar Chart", "Pie Chart");
        chartSelectionList.getSelectionModel().selectedItemProperty().addListener((observableValue, s, t1) -> {
            currentChartType = chartSelectionList.getSelectionModel().getSelectedItem();
            changeChart();
            updateChart();
        });
        chartSelectionList.getSelectionModel().select(0);

        xSelectionBox.getItems().clear();
        for (ColumnInfo column : singleton.getColumnInfo()) {
            ySelectionBox.getItems().add(column.name);
            if (!(column.discrete && xMinField.isVisible()))
                xSelectionBox.getItems().add(column.name);
        }
        xSelectionBox.getSelectionModel().selectedItemProperty().addListener((observableValue, s, t1) -> hideContinuousOptions());

        ySelectionBox.getSelectionModel().selectedItemProperty().addListener((observableValue, s, t1) -> {
            getAvailableSeries();
            hideComparatorField();
        });

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
        };

        yMinField.textProperty().addListener(/*boundsListener*/changeChartListener);
        yMaxField.textProperty().addListener(/*boundsListener*/changeChartListener);

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
            //setBounds();
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

    public void loadChart(ChartDescriptor c){

        update = true;
        updating = true;
        createButton.setText("Update");

        xNameField.setText(c.xName);
        yNameField.setText(c.yName);
        titleField.setText(c.title);

        chartSelectionList.getSelectionModel().select(chartSelectionList.getItems().indexOf(c.chartType));

        fromDatePicker.setValue(c.fromDate);
        toDatePicker.setValue(c.toDate);

        relativeCheck.setSelected(c.isRelative);
        pointCheck.setSelected(c.showPoints);

        xMinField.setText(c.xMin);
        xMaxField.setText(c.xMax);
        yMinField.setText(c.yMin);
        yMaxField.setText(c.yMax);

        String range;
        if(!c.chartType.equals("Pie Chart")) {
            if (c.xAxis.equals("date"))
                range = String.valueOf((int) c.stepSize);
            else
                range = String.valueOf(c.stepSize);
            rangeField.setText(range);
        }

        xSelectionBox.setValue(c.xAxis);
        ySelectionBox.setValue(c.yAxis);

        seriesList.getItems().addAll(c.seriesList);

        updating = false;
        changeChart();
        updateChart();


    }

    private void setPoints(boolean points){
        if(chart.getClass().equals(LineChart.class))
            ((LineChart)chart).setCreateSymbols(points);
        else if(chart.getClass().equals(AreaChart.class))
            ((AreaChart)chart).setCreateSymbols(points);
        else if(chart.getClass().equals(StackedAreaChart.class))
            ((StackedAreaChart)chart).setCreateSymbols(points);
    }



    private void hideContinuousOptions() {
        String name = xSelectionBox.getValue();
        Boolean discrete = null;
        for (ColumnInfo column : singleton.getColumnInfo()) {
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
        for (ColumnInfo column :singleton.getColumnInfo()) {
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
        for (ColumnInfo column : singleton.getColumnInfo()) {
            if (column.name.equals(name))
                discrete = column.discrete;
        }
        if (discrete != null) {
            List<String> series = new ArrayList<>();
            if (!discrete) {
                series.addAll(List.of(continuousOptions));
            } else {
                series.add("All");
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
        pointCheck.setVisible(points);
    }

    private void changeChart() {                                //changes Layout to fit selected Chart type

        chartContainer.getChildren().clear();

        if(currentChartType != null) {
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
                }
            }
            chart.setAnimated(false);
            chartContainer.getChildren().add(chart);

            String previous = xSelectionBox.getValue();

            xSelectionBox.getItems().setAll(new ArrayList<>());
            for (ColumnInfo column : singleton.getColumnInfo()) {
                if (!(column.discrete && !(chart.getClass().equals(BarChart.class) || chart.getClass().equals(StackedBarChart.class))))
                    xSelectionBox.getItems().add(column.name);
            }
            if (xSelectionBox.getItems().size()>0 && xSelectionBox.getItems().contains(previous))
                xSelectionBox.setValue(previous);
        }
    }

    private void updateChart() {

        if(!updating) {

            boolean cont = true;

            if (chartSelectionList.getSelectionModel().getSelectedItem() == null) {
                chartSelectionList.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                cont = false;
            }
            String chartType = chartSelectionList.getSelectionModel().getSelectedItem();
            if (xSelectionBox.getSelectionModel().getSelectedItem() == null && !chartType.equals("Pie Chart")) {
                xSelectionBox.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                cont = false;
            }
            if (ySelectionBox.getSelectionModel().getSelectedItem() == null) {
                ySelectionBox.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                cont = false;
            }
            if (!cont)
                return;

            chartSelectionList.setStyle("-fx-border-width: 0px;");
            xSelectionBox.setStyle("-fx-border-width: 0px;");
            ySelectionBox.setStyle("-fx-border-width: 0px;");


            LocalDate fromDate = fromDatePicker.getValue();
            LocalDate toDate = toDatePicker.getValue();
            List<String> seriesList = this.seriesList.getItems();
            boolean showPoints = pointCheck.isSelected();
            boolean isRelative = relativeCheck.isSelected();
            String xAxis = xSelectionBox.getValue();
            String xMin = xMinField.getText();
            String xMax = xMaxField.getText();
            String xType = this.xType;
            String yAxis = ySelectionBox.getValue();
            String yMin = yMinField.getText();
            String yMax = yMaxField.getText();
            String range = rangeField.getText();

            Boolean discrete = null;

            float stepSize = 1;

            for (ColumnInfo column : singleton.getColumnInfo()) {
                if (column.name.equals(xAxis)) {
                    discrete = column.discrete;
                    break;
                }
            }

            if (!chartType.equals("Pie Chart")) {
                if (Boolean.FALSE.equals(discrete)) {
                    if (!range.matches("[1-9][0-9]?+[.]?[0-9]?+") || (xAxis.equals("date") && !range.matches("[1-9][0-9]?+"))) {
                        rangeField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                        cont = false;
                    } else
                        rangeField.setStyle("--fx-border-width: 0px;");
                }
                if (!yMin.matches("[-]?[0-9]+[.]?[0-9]?+") && !yMin.equals("")) {
                    yMinField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                    cont = false;
                } else
                    yMinField.setStyle("--fx-border-width: 0px;");
                if (!yMax.matches("[-]?[0-9]+[.]?[0-9]?+") && !yMax.equals("")) {
                    yMaxField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                    cont = false;
                } else
                    yMaxField.setStyle("--fx-border-width: 0px;");
                if (!xAxis.equals("date")) {
                    if (!xMin.matches("[-]?[0-9]+[.]?[0-9]?+") && !xMin.equals("")) {
                        xMinField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                        cont = false;
                    } else
                        xMinField.setStyle("--fx-border-width: 0px;");
                    if (!xMax.matches("[-]?[0-9]+[.]?[0-9]?+") && !xMax.equals("")) {
                        xMaxField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                        cont = false;
                    } else
                        xMaxField.setStyle("--fx-border-width: 0px;");
                } else {
                    xMin = checkDateBound(xMinField);
                    xMax = checkDateBound(xMaxField);
                }

            }
            if (!cont || xMin.equals("false") || xMax.equals("false"))
                return;
            if (!chartType.equals("Pie Chart") && !discrete)
                stepSize = Float.parseFloat(range);
            chartDescriptor = new ChartDescriptor("", "", "", chartType, String.valueOf(fromDate), String.valueOf(toDate), seriesList, showPoints, isRelative, xAxis, xMin, xMax, xType, yAxis, yMin, yMax, stepSize);

            chartContainer = new VBox();
            chartPane.setCenter(chartContainer);
            chartContainer.getChildren().add(chart);

            singleton.threadGenerateChart(chartContainer, chartDescriptor);
        }

    }

    private String checkDateBound(TextField field) {
        String bound = field.getText();
        if(!bound.equals("")) {
            if (!bound.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
                field.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                return "false";
            }
            /*
            String substring = bound.substring(0, bound.indexOf("-"));
            if (substring.length() == 3)
                bound = "20" + bound;
            else if (substring.length() == 4) {
                bound = "2" + bound;
            }
            substring = bound.substring(bound.indexOf("-") + 1, bound.lastIndexOf("-"));
            if (substring.length() == 1) {
                bound = bound.substring(0, bound.indexOf("-") + 1) + "0" + substring + "-" + bound.substring(bound.lastIndexOf("-"));
            }
            substring = bound.substring(bound.lastIndexOf("-") + 1);
            if (substring.length() == 1) {
                bound = bound.substring(bound.lastIndexOf("-")) + "0" + substring;
            }

             */
        }
        field.setStyle("--fx-border-width: 0px;");
        return bound;
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

        chartDescriptor.setTitle(titleField.getText());
        if(!currentChartType.equals("Pie Chart")){
            chartDescriptor.setxName(xNameField.getText());
            chartDescriptor.setyName(yNameField.getText());
        }
        if(!update)
            controller.addNewChart(chartDescriptor);
        else
            controller.setChart(chartDescriptor);
        onCancel();
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
