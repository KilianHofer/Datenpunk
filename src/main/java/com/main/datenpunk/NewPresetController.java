package com.main.datenpunk;


import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NewPresetController {
    @FXML
    public TextField nameField;
    @FXML
    public CheckBox fromDateCheck, toDateCheck, whitelistCheck, blacklistCheck, columnVisCheck, columnSizeCheck;

    Singleton singleton = Singleton.getInstance();
    MainController controller;

    private List<String> presets;

    public void setPresets(List<String> presets) {
        this.presets = presets;
    }

    public void setController(MainController controller) {
        this.controller = controller;
    }

    @FXML
    public void onCancel() {
        ((Stage) nameField.getScene().getWindow()).close();
    }

    @FXML
    public void onCreate() {
        if (presets.contains(nameField.getText()) && !nameField.getText().equals("Custom")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("A preset with this name already exists!");
            alert.showAndWait();
        }
        else if(!nameField.getText().equals("Custom")) {
            String path = singleton.getWorkingDirectory() + "\\Projects\\" + singleton.getCurrentProject() + "\\Presets\\" + nameField.getText() + ".json";
            System.out.println(path);


            String start = "";
            String end =  "";
            if(fromDateCheck.isSelected())
                start = singleton.getController().getFromDate();
            if(toDateCheck.isSelected())
                end = singleton.getController().getToDate();

            List<String> names = new ArrayList<>();

            List<String> order = new ArrayList<>();
            List<Boolean> visible = new ArrayList<>();
            List<Float> widths = new ArrayList<>();
            List<List<String>> whitelist = new ArrayList<>();
            List<List<String>> blacklist = new ArrayList<>();
            int index = 0;
            for (int i = 0; i < singleton.getController().objectTable.getItems().size(); i++) {
                VBox vBox = (VBox) singleton.getController().objectTable.getItems().get(i);

                String name = ((Button) vBox.getChildren().get(0)).getText();
                if(columnVisCheck.isSelected())
                    names.add(name);
                if(true)            //TODO: replace with save column order checkbox
                    order.add(name);
                else
                    order.add(singleton.getColumnNames().get(i));


                if(!name.equals("id") && !name.equals("Date")) {

                    if(whitelistCheck.isSelected())
                        whitelist.add(singleton.getController().getWhitelist(index));
                    if(blacklistCheck.isSelected())
                        blacklist.add(singleton.getController().getBlacklist(index));
                    index++;
                }
            }
            for(int i = 0; i<singleton.getColumnNames().size();i++){

                String name = singleton.getColumnNames().get(i);
                if(names.contains(name) || !columnVisCheck.isSelected())
                    visible.add(true);
                else
                    visible.add(false);

                VBox vBox = singleton.getColumns().get(i);
                if(columnSizeCheck.isSelected())
                    widths.add(((Double) vBox.getWidth()).floatValue());
            }

            try (FileWriter fileWriter = new FileWriter(path)) {
                //Files.createFile(new File(path).toPath());

                JSONObject filterObject = new JSONObject();
                filterObject.put("start",start);
                filterObject.put("end",end);
                filterObject.put("order", order);
                filterObject.put("visible", visible);
                filterObject.put("widths", widths);
                filterObject.put("whitelist", whitelist);
                filterObject.put("blacklist", blacklist);



                fileWriter.write(filterObject.toJSONString());
                fileWriter.flush();


            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        controller.selectPresets();
        controller.setPreset(nameField.getText());
        onCancel();
    }
}
