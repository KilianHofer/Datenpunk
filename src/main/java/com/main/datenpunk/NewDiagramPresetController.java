package com.main.datenpunk;


import enteties.ChartDescriptor;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class NewDiagramPresetController {

    public TextField nameField;
    Singleton singleton = Singleton.getInstance();

    public void onCancel() {
        ((Stage)nameField.getScene().getWindow()).close();
    }

    public void onCreate() throws IOException {
        String path = singleton.getWorkingDirectory() + "\\Projects\\" + singleton.getCurrentProject() + "\\DiagramPresets\\";
        File file = new File(path);
        if (!file.exists())
            Files.createDirectory(file.toPath());
        path += nameField.getText() + ".json";
        file = new File(path);

        if(nameField.getText().equals("Custom")){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("'Custom' is not a valid Preset name");
            alert.show();
        }
        else if (file.exists()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("A preset with this name already exists!");
            alert.showAndWait();
        }
        else if(!nameField.getText().equals("")) {

            if (file.exists()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("A diagram Preset with this name already exists for this Project");
                alert.show();
                return;
            }
            Files.createFile(file.toPath());

            JSONArray chartList = new JSONArray();

            for (ChartDescriptor chart : singleton.getController().charts) {
                JSONObject chartDetails = new JSONObject();

                chartDetails.put("title", chart.title);
                chartDetails.put("xName", chart.xName);
                chartDetails.put("yName", chart.yName);
                chartDetails.put("chartType", chart.chartType);
                chartDetails.put("fromDate", String.valueOf(chart.fromDate));
                chartDetails.put("toDate", String.valueOf(chart.toDate));
                chartDetails.put("seriesList", chart.seriesList);
                chartDetails.put("showPoints", chart.showPoints);
                chartDetails.put("isRelative", chart.isRelative);
                chartDetails.put("xAxis", chart.xAxis);
                chartDetails.put("xMin", chart.xMin);
                chartDetails.put("xMax", chart.xMax);
                chartDetails.put("xType", chart.xType);
                chartDetails.put("yAxis", chart.yAxis);
                chartDetails.put("yMin", chart.yMin);
                chartDetails.put("yMax", chart.yMax);
                chartDetails.put("stepSize", String.valueOf(chart.stepSize));

                JSONObject chartObject = new JSONObject();
                chartObject.put("chartDescriptor", chartDetails);

                chartList.add(chartObject);

            }
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(chartList.toJSONString());
                fileWriter.flush();
                singleton.getController().setChartPreset(nameField.getText());
                singleton.getController().selectChartPresets();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Could not save preset.\n Error: " + e.getMessage());
                alert.show();
                onCancel();
            }
            onCancel();
        }
    }
}
