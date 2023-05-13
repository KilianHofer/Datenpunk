package com.main.datenpunk;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
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
    public CheckBox dateRangeCheck, whitelistCheck, blacklistCheck;

    Singelton singelton = Singelton.getInstance();
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
            String path = singelton.getWorkingDirectory() + "\\Projects\\" + singelton.getCurrentProject() + "\\Presets\\" + nameField.getText();
            System.out.println(path);

            try {
                Files.createDirectory(new File(path).toPath());
                List<String> lines;


                lines = new ArrayList<>();
                lines.add(controller.getFromDate());
                lines.add(controller.getToDate());
                writeToFile(path + "\\dateRange.dtpnk", lines, dateRangeCheck.isSelected());

                lines = controller.getWhitelist();
                writeToFile(path + "\\whitelist.dtpnk", lines, whitelistCheck.isSelected());

                lines = controller.getBlacklist();
                writeToFile(path + "\\blacklist.dtpnk", lines, blacklistCheck.isSelected());

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
        file.createNewFile();

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
