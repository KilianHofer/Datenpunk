package com.main.datenpunk;

import database.DAO;
import enteties.Status;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class NewChartController implements Initializable {


    @FXML
    private ToggleGroup xTypeGroup, yTypeGroup;
    String yType = "Count", xType = "Time";
    @FXML
    private BorderPane chartPane;
    @FXML
    private ListView<String> chartSelectionList,seriesList;
    @FXML
    private ChoiceBox<String> xSelectionBox,ySelectionBox,seriesSelectionBox;
    @FXML
    private TextField titleField,xMinField,xNameField,xMaxField,yMinField,yNameField,yMaxField;

    @FXML
    DatePicker fromDatePicker,toDatePicker;

    @FXML
    HBox yOptions,xToggle;

    DAO dao = DAO.getInstance();
    List<Status> statuses = dao.selectStatuses();

    private Chart chart;
    private String currentChartType;

    ObservableList<TextField> textFields = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        fromDatePicker.setValue(null);
        toDatePicker.setValue(LocalDate.now());

        textFields.addAll(xMinField,xMaxField,xNameField,yMinField,yMaxField,yNameField);

        chartSelectionList.getItems().addAll("Line Chart", "Area Chart","Stacked Area Chart", "Bar Chart", "Stacked Bar Chart","Pie Chart");
        chartSelectionList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                currentChartType = chartSelectionList.getSelectionModel().getSelectedItem();
                changeChart();
            }
        });
        chartSelectionList.getSelectionModel().select(0);
        

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
                yOptions.setVisible(false);
                xToggle.setVisible(false);
            }
        }

        chartPane.setCenter(chart);
    }

    public void onSeriesAdd() {
    }

    public void onSeriesDelete(){
    }

    public void onCancel() {
        ((Stage)chartPane.getScene().getWindow()).close();
    }
    public void onCreate(ActionEvent event) {
    }

    public void setYType(ActionEvent event) {
        yType = ((RadioButton)event.getSource()).getText();
    }

    public void setXType(ActionEvent event) {
        xType = ((RadioButton)event.getSource()).getText();
        xSelectionBox.setVisible(!xType.equals("Time"));

    }
}
