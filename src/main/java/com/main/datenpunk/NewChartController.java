package com.main.datenpunk;

import database.DAO;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class NewChartController implements Initializable {

    @FXML
    public RadioButton xStandard;
    @FXML
    private ToggleGroup xTypeGroup;
    String yType = "Count", xType = "Time";
    @FXML
    private BorderPane chartPane, seriesPane;
    @FXML
    private ListView<String> chartSelectionList,seriesList;
    @FXML
    private ChoiceBox<String> xSelectionBox,ySelectionBox,seriesSelectionBox;
    @FXML
    private TextField titleField,xMinField,xNameField,xMaxField,yMinField,yNameField,yMaxField,rangeField, comparatorField, seriesNameField;

    @FXML
    DatePicker fromDatePicker,toDatePicker;

    @FXML
    HBox yOptions,xToggle;

    DAO dao = DAO.getInstance();

    private Chart chart;
    private String currentChartType;

    List<ColumnInfo> columnInfo;

    ObservableList<TextField> textFields = FXCollections.observableArrayList();

    String[] continuousOptions = new String[]{"value", "sum", "average", "greater than", "greater or equal", "less than", "less or equal", "equals"};

    boolean xContiniuous = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        fromDatePicker.setValue(null);
        toDatePicker.setValue(LocalDate.now());

        textFields.addAll(xMinField,xMaxField,xNameField,yMinField,yMaxField,yNameField);

        chartSelectionList.getItems().addAll("Line Chart", "Area Chart","Stacked Area Chart", "Bar Chart", "Stacked Bar Chart","Pie Chart");
        chartSelectionList.getSelectionModel().selectedItemProperty().addListener((observableValue, s, t1) -> {
            currentChartType = chartSelectionList.getSelectionModel().getSelectedItem();
            changeChart();
        });
        chartSelectionList.getSelectionModel().select(0);

        columnInfo = new ArrayList<>();
        columnInfo.add(new ColumnInfo("id",false));
        columnInfo.addAll(dao.selectTableColumns("objects"));
        columnInfo.addAll(dao.selectTableColumns("history"));

        for(ColumnInfo column:columnInfo){
            ySelectionBox.getItems().add(column.name);
            if(!(column.discrete && xMinField.isVisible()))
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
        

    }

    private void setBounds() {
        if(xMaxField.isVisible()) {
            String xMax = xMaxField.getText();
            String xMin = xMinField.getText();
            String yMax = yMaxField.getText();
            String yMin = yMinField.getText();
            try {
                if (xMax.equals(xMin) && xMax.equals("")) {
                    ((XYChart) chart).getXAxis().setAutoRanging(true);
                } else {
                    Axis axis = ((XYChart) chart).getXAxis();
                    axis.setAutoRanging(false);
                    if (axis.getClass().equals(NumberAxis.class)) {
                        if (!xMin.equals(""))
                            ((NumberAxis) axis).setLowerBound(Double.parseDouble(xMin));
                        if (!xMax.equals(""))
                            ((NumberAxis) axis).setUpperBound(Double.parseDouble(xMax));
                    }
                }
                if (yMax.equals(yMin) && yMax.equals("")) {
                    ((XYChart) chart).getYAxis().setAutoRanging(true);
                } else {
                    Axis axis = ((XYChart) chart).getYAxis();
                    axis.setAutoRanging(false);
                    if (axis.getClass().equals(NumberAxis.class)) {
                        if (!yMin.equals(""))
                            ((NumberAxis) axis).setLowerBound(Double.parseDouble(yMin));
                        if (!yMax.equals(""))
                            ((NumberAxis) axis).setUpperBound(Double.parseDouble(yMax));
                    }
                }
            }catch (Exception ignore){

            }
        }
    }

    private void hideContinuousOptions() {
        String name = xSelectionBox.getValue();
        Boolean discrete = null;
        for(ColumnInfo column:columnInfo){
            if(column.name.equals(name))
                discrete = column.discrete;
        }
        if(discrete != null){
            if(discrete){
                xToggle.setVisible(false);
                rangeField.setVisible(false);
                xContiniuous = false;
            }
            else {
                xToggle.setVisible(true);
                if(!xContiniuous)
                    xStandard.fire();
                xContiniuous = true;
            }
        }
    }

    private void hideComparatorField() {

        String name = ySelectionBox.getValue();
        Boolean discrete = null;
        for(ColumnInfo column:columnInfo){
            if(column.name.equals(name))
                discrete = column.discrete;
        }
        if(discrete != null){
            comparatorField.setVisible(!discrete && !seriesSelectionBox.getSelectionModel().getSelectedItem().equals("value"));
        }
    }


    private void getAvailableSeries() {
        seriesSelectionBox.setValue(null);
        String name = ySelectionBox.getValue();
        Boolean discrete = null;
        for(ColumnInfo column:columnInfo){
            if(column.name.equals(name))
                discrete = column.discrete;
        }
        if(discrete != null){
            List<String> series = new ArrayList<>();
            if(!discrete){
                series.addAll(List.of(continuousOptions));
            }
            else {
                series.add("All");
                System.out.println(dao.selectSeriesOptions(name));
                series.addAll(dao.selectSeriesOptions(name));
            }
            seriesSelectionBox.getItems().setAll(series);
        }
    }

    private void showTextFields(boolean show,boolean bar){
        for (TextField textField:textFields) {
            if (textField == xMinField || textField == xMaxField)
                textField.setVisible(show != bar);
            else
                textField.setVisible(show);
        }
        yOptions.setVisible(true);
        xToggle.setVisible(true);
        seriesPane.setVisible(true);
        rangeField.setVisible(xType.equals("Range"));

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
                rangeField.setVisible(true);
            }
            case "Stacked Bar Chart" -> {
                chart = new StackedBarChart<>(new CategoryAxis(), new NumberAxis());
                showTextFields(true, true);
                xToggle.setVisible(false);
                rangeField.setVisible(true);
            }
            case "Pie Chart" -> {
                chart = new PieChart();
                showTextFields(false, false);
                yOptions.setVisible(false);
                xToggle.setVisible(false);
                seriesPane.setVisible(false);
                rangeField.setVisible(false);
            }
        }

        chartPane.setCenter(chart);
        xSelectionBox.getItems().setAll(new ArrayList<>());
        for(ColumnInfo column:columnInfo){
            if(!(column.discrete && xMinField.isVisible()))
                xSelectionBox.getItems().add(column.name);
        }
    }

    public void onSeriesAdd() {

        if(seriesSelectionBox.getValue() != null){
            String function;
            if(comparatorField.isVisible()){
                function = seriesSelectionBox.getValue()+comparatorField;
            }
            else{
                function = seriesSelectionBox.getValue();
            }
            if(seriesNameField.getText().equals(""))
                seriesList.getItems().add(function +": "+function);
            else
                seriesList.getItems().add(seriesNameField.getText()+": " +function);
        }

    }

    public void onSeriesDelete(){

        if(seriesList.getSelectionModel().getSelectedItem() != null){
            seriesList.getItems().remove(seriesList.getSelectionModel().getSelectedItem());
        }

    }

    public void onCancel() {
        ((Stage)chartPane.getScene().getWindow()).close();
    }
    public void onCreate() {
    }

    public void setXType(ActionEvent event) {
        xType = ((RadioButton)event.getSource()).getText();
        rangeField.setVisible(xType.equals("Range"));

    }

    public void onSeriesListClick(MouseEvent mouseEvent) {
        if(mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            if (mouseEvent.getClickCount() == 2) {
                onSeriesDelete();
            }
        }
    }
}
