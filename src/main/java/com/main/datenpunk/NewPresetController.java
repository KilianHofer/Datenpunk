package com.main.datenpunk;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class NewPresetController {
    @FXML
    public TextField nameField;
    @FXML
    public CheckBox fromDateCheck, toDateCheck, whitelistCheck, blacklistCheck,columnVisCheck,columnSizeCheck;

    Singleton singleton = Singleton.getInstance();
    MainController controller;

    private List<String> presets;

    public void setPresets(List<String> presets){
        this.presets = presets;
    }

    public void setController(MainController controller){
        this.controller = controller;
    }

    @FXML
    public void onCancel() {
        ((Stage)nameField.getScene().getWindow()).close();
    }
    @FXML
    public void onCreate() {
        if(presets.contains(nameField.getText()) && !nameField.getText().equals("Custom")){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("A preset with this name already exists!");
            alert.showAndWait();
        }
        else {
            String path = singleton.getWorkingDirectory() + "\\Projects\\" + singleton.getCurrentProject() + "\\Presets\\" + nameField.getText();
            System.out.println(path);

            try {
                Files.createDirectory(new File(path).toPath());
                List<String> lines;


                lines = new ArrayList<>();
                lines.add(controller.getFromDate());
                writeToFile(path + "\\dateRange.dtpnk", lines, fromDateCheck.isSelected());

                lines.clear();
                lines.add(controller.getToDate());
                writeToFile(path + "\\dateRange.dtpnk", lines, toDateCheck.isSelected());

                lines = controller.getWhitelist();
                writeToFile(path + "\\whitelist.dtpnk", lines, whitelistCheck.isSelected());

                lines = controller.getBlacklist();
                writeToFile(path + "\\blacklist.dtpnk", lines, blacklistCheck.isSelected());

                lines.clear();
                for(Node column : singleton.getController().objectTable.getItems()){
                    lines.add(String.valueOf(((VBox) column).getWidth()));
                }
                writeToFile(path + "\\columnSizes.dtpnk",lines,columnSizeCheck.isSelected());

                lines.clear();
                for(Node column:singleton.getController().objectTable.getItems()){
                    lines.add(((Button)((VBox)column).getChildren().get(0)).getText());
                }
                writeToFile(path + "\\columns.dtpnk",lines,columnVisCheck.isSelected());


            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        controller.selectPresets();
        controller.setPreset(nameField.getText());
        onCancel();
    }

    private void writeToFile(String path, List<String> lines, boolean write) throws IOException {
        File file = new File(path);
        Files.createFile(file.toPath());

        if(write) {
            FileWriter fileWriter = new FileWriter(file, true);
            BufferedWriter writer = new BufferedWriter(fileWriter);

            for (String line : lines) {
                writer.append(line).append("\n");
            }

            writer.close();
            fileWriter.close();
        }
    }
}
