package com.main.datenpunk;

import database.DAO;
import enteties.ColumnInfo;
import enteties.Status;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AddElementController implements Initializable {

    public VBox settingContainer;
    private final DAO dao = DAO.getInstance();
    Singleton singleton = Singleton.getInstance();

    List<Boolean> accept = new ArrayList<>();
    List<Boolean> required = new ArrayList<>();



    public void onCancel() {
        Stage stage = (Stage) settingContainer.getScene().getWindow();
        stage.close();
    }

    public void onAccept() {
        if(!accept.contains(false)){
            List<String> objectColumns = new ArrayList<>();
            List<String> historyColumns = new ArrayList<>();
            for(ColumnInfo columnInfo: singleton.getColumnInfo()){
                if(columnInfo.table.equals("objects")) {
                    if (!columnInfo.name.equals("id"))
                        objectColumns.add(columnInfo.name);
                }
                else {
                    if (!columnInfo.name.equals("Date"))
                        historyColumns.add((columnInfo.name));
                }
            }

            List<String> objectValues = new ArrayList<>();
            List<String> historyValues = new ArrayList<>();

            for(Node setting:settingContainer.getChildren()){
                VBox vBox = (VBox)setting;

                String name = ((Label)vBox.getChildren().get(0)).getText();
                name = name.substring(0,name.length()-1);

                Node valueNode = vBox.getChildren().get(1);
                String value;
                if(valueNode.getClass().equals(TextField.class))
                    value = ((TextField) valueNode).getText();
                else
                    value = ((ChoiceBox<String>)valueNode).getValue();

                if(objectColumns.contains(name))
                    objectValues.add(value);
                else
                    historyValues.add(value);
            }
            dao.insert(objectColumns,objectValues,historyColumns,historyValues);
            singleton.getController().updateTable();
            onCancel();
        }
    }

    ChangeListener<String> emptyListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
            Node setting = settingContainer.getScene().focusOwnerProperty().get();
            VBox vBox = (VBox) setting.getParent();
            int index = settingContainer.getChildren().indexOf(vBox);
            if (t1.equals("")) {
                accept.set(index, false);
                setting.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            }
            else {
                accept.set(index, true);
                setting.setStyle("-fx-border-width: 0px;");
            }
        }
    };

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        for(ColumnInfo columnInfo: singleton.getColumnInfo()){
            String name = columnInfo.name;
            if(!name.equals("id") && !name.equals("Date")){
                name = name+":";
                Label label = new Label(name);
                Control setting;
                if(columnInfo.required)
                    accept.add(false);
                else
                    accept.add(true);
                required.add(columnInfo.required);
                switch (columnInfo.type) {
                    case "Choice" -> {
                        setting = new ChoiceBox<String>();
                        setting.setPrefWidth(150);
                        for (int i = 0; i < singleton.choiceNames.size(); i++) {
                            if (singleton.choiceNames.get(i).equals(columnInfo.name)) {
                                for (Status choice : singleton.choices.get(i)) {
                                    ((ChoiceBox<String>) setting).getItems().add(choice.getName());
                                }
                            }
                        }
                        if(columnInfo.required)
                            ((ChoiceBox<String>)setting).valueProperty().addListener(emptyListener);
                        else
                            ((ChoiceBox<String>) setting).getItems().add("");
                    }
                    case "Text" -> {
                        setting = new TextField();
                        if(columnInfo.required)
                            ((TextField)setting).textProperty().addListener(emptyListener);
                    }
                    case "Integer" -> {
                        setting = new TextField();
                        ((TextField) setting).textProperty().addListener((observableValue, s, t1) -> {
                            TextField textField = (TextField)settingContainer.getScene().focusOwnerProperty().get();
                            VBox vBox = (VBox)textField.getParent();
                            int index = settingContainer.getChildren().indexOf(vBox);
                            if (t1.matches("[0-9]+")) {
                                accept.set(index,true);
                                setting.setStyle("-fx-border-width: 0px");
                            }
                            else {
                                if(t1.equals("") && !required.get(index)){
                                    accept.set(index,true);
                                }
                                else {
                                    setting.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                                    accept.set(index, false);
                                }
                            }
                        });
                    }
                    default -> {
                        setting = new TextField();
                        ((TextField) setting).textProperty().addListener((observableValue, s, t1) -> {
                            TextField textField = (TextField)settingContainer.getScene().focusOwnerProperty().get();
                            VBox vBox = (VBox)textField.getParent();
                            int index = settingContainer.getChildren().indexOf(vBox);
                            if (t1.matches("^(0*[1-9][0-9]*(\\.[0-9]+)?|0+\\.[0-9]*[1-9][0-9]*)$")) {
                                setting.setStyle("-fx-border-width: 0px");
                                accept.set(index,true);
                            }
                            else {
                                if(t1.equals("") && !required.get(index)){
                                    accept.set(index,true);
                                }
                                else {
                                    setting.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                                    accept.set(index, false);
                                }
                            }
                        });
                    }
                }
                VBox vBox = new VBox(label, setting);
                VBox.setMargin(label,new Insets(5,0,0,0));
                settingContainer.getChildren().add(vBox);
            }
        }
    }
}
