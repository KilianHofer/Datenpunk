package com.main.datenpunk;

import database.DAO;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;

public class NewProjectController implements Initializable {


    public VBox columnContainer;
    DAO dao = DAO.getInstance();
    Singelton singelton = Singelton.getInstance();

    ObservableList<String> choices = FXCollections.observableArrayList("Text","Integer","Decimal","Choice");
    Stage returnStage;
    @FXML
    public TextField nameField,pathField;

    boolean changingOrder = false;


    ChangeListener<String> positionListener = new ChangeListener<>() {

        @Override
        public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
            if (!changingOrder) {
                    TextField changedField = (TextField) nameField.getScene().focusOwnerProperty().get();
                    if (t1.matches("[1-9]+") && Integer.parseInt(t1) <= columnContainer.getChildren().size()) {
                        changedField.setStyle("-fx-border-width: 0px;");
                        if (!t1.equals(s))
                            changeColumnOrder(columnContainer.getChildren().indexOf(changedField.getParent().getParent().getParent()), Integer.parseInt(t1));
                    } else {
                        changedField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");

                }
            }
        }
    };

    ChangeListener<String> emptyListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
            if(t1.equals("")){
                nameField.getScene().focusOwnerProperty().get().setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            }
            else {
                nameField.getScene().focusOwnerProperty().get().setStyle("-fx-border-width: 0px;");
            }
        }
    };

    ChangeListener<String> choiceListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {           //TODO: implement

        }
    };



    @FXML
    public void onCreate() throws IOException {
        String name = nameField.getText();
        String path = pathField.getText();
        File file = new File(path);
        if(!name.equals("") && !path.equals("")) {
            if (!file.exists()) {
                Files.createDirectory(file.toPath());
            }
            path += "\\" + name + ".dtpnkl";
            file = new File(path);
            if (!file.exists()) {
                try {
                    if(!file.createNewFile()){
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("could not create file");
                        alert.show();
                        return;
                    }

                    File projectFile = new File(System.getProperty("user.home")+"\\Datenpunk\\projects.dtpnk");
                    BufferedWriter writer = new BufferedWriter(new FileWriter(projectFile,true));
                    writer.append(path).append("\n");
                    writer.close();


                    path = System.getProperty("user.home")+"\\Datenpunk\\Projects";
                    path += "\\" + name;
                    file = new File(path);
                    Files.createDirectory(file.toPath());
                    file = new File(path+"\\Presets");
                    Files.createDirectory(file.toPath());
                    connectToDatabase();

                } catch (IOException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            }
            else{
                Alert alert = new Alert((Alert.AlertType.ERROR));
                alert.setContentText("A project with this name already exists in this directory!");
                alert.showAndWait();
            }
        }

    }

    private void connectToDatabase() {
        String name = nameField.getText();
        File file = new File(System.getProperty("user.home")+"\\Datenpunk\\connection.dtpnk");
        try {
            if (file.exists()) {
                Scanner scanner = new Scanner(file);
                if(scanner.hasNext()){
                    String password = scanner.next();
                    if(dao.connectToDB("","postgres",password)){
                        dao.createDatabase(name);
                    }
                    if(dao.connectToDB("datenpunk_"+name,"postgres",password)){
                        dao.createTables();
                        singelton.setCurrentProject(name);
                        singelton.setColumnInfo();
                        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
                        Stage stage = returnStage;
                        stage.setTitle("Datenpunk");
                        stage.setScene( new Scene(fxmlLoader.load()));
                        stage.setMaximized(true);
                        stage.setResizable(true);
                        stage.show();

                        stage = (Stage) nameField.getScene().getWindow();
                        stage.close();
                        return;

                    }
                }
            }
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("databaseConnection-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());


            Stage stage = (Stage) nameField.getScene().getWindow();

            stage.setTitle("Connect to Database");
            stage.setScene(scene);

            DatabaseConnectionController databaseConnectionController = fxmlLoader.getController();
            databaseConnectionController.setName(name);      //TODO: better data transfer
            databaseConnectionController.setRetrunStage(returnStage);
            databaseConnectionController.setNew(true);
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }

    }

    @FXML
    public void onCancel() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void onSelect() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File file = new File(pathField.getText());
        if(file.exists()){
            directoryChooser.setInitialDirectory(file);
        }
        String directory = String.valueOf(directoryChooser.showDialog(pathField.getScene().getWindow()));
        if(!directory.equals("null"))
            pathField.setText(directory);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        pathField.setText(System.getProperty("user.home")+"\\Datenpunk\\Projects");

        getColumnChoiceBox(0).setValue("SERIAL");
        getColumnChoiceBox(1).setValue("Text");
        getColumnChoiceBox(2).setValue("Choice");
        getColumnChoiceBox(3).setValue("DATE");

        getColumnSelectionList(2).getItems().addAll("Planned","In-Progress","Complete");


        for(int i = 0; i<4; i++){
            getColumnPositionField(i).textProperty().addListener(positionListener);
        }


    }

    private void changeColumnOrder(int oldValue, int newValue) {

        changingOrder = true;
        VBox old = (VBox)columnContainer.getChildren().get(oldValue);
        columnContainer.getChildren().remove(old);
        columnContainer.getChildren().add(newValue-1,old);
        for(int i = 0; i < columnContainer.getChildren().size();i++){
            getColumnPositionField(i).setText(String.valueOf(i+1));
        }
        changingOrder = false;

    }

    public void setReturnStage(Stage stage) {
        returnStage = stage;
    }


    private HBox getColumnContainer(int id){
        return (HBox)((VBox)columnContainer.getChildren().get(id)).getChildren().get(0);
    }
    private TextField getColumnPositionField(int id){
        return (TextField) ((VBox)getColumnContainer(id).getChildren().get(0)).getChildren().get(1);
    }
    private TextField getColumnNameField(int id){
        return (TextField)((VBox)getColumnContainer(id).getChildren().get(1)).getChildren().get(1);
    }
    private CheckBox getColumnHistoryCheck(int id){
        return (CheckBox)((VBox)getColumnContainer(id).getChildren().get(1)).getChildren().get(2);
    }
    private ChoiceBox<String> getColumnChoiceBox(int id){
        return (ChoiceBox)((VBox)getColumnContainer(id).getChildren().get(2)).getChildren().get(1);
    }
    private ListView<String> getColumnSelectionList(int id){
        return (ListView) ((VBox)getColumnContainer(id).getChildren().get(2)).getChildren().get(5);
    }

    public void onAdd() {

        Label padding1 = new Label();

        TextField positionField = new TextField();
        positionField.setText(String.valueOf(columnContainer.getChildren().size()+1));
        positionField.textProperty().addListener(positionListener);
        positionField.setPrefSize(30,25);


        VBox first = new VBox(padding1,positionField);



        Label nameLabel = new Label("Name:");

        TextField nameField = new TextField();
        nameField.promptTextProperty().addListener(emptyListener);
        nameField.setPrefWidth(149);

        CheckBox trackHistory = new CheckBox("Track History");

        VBox second = new VBox(nameLabel,nameField,trackHistory);
        VBox.setMargin(nameField,new Insets(0,0,5,0));


        Label typeLabel = new Label("Type:");
        ChoiceBox<String> typeBox = new ChoiceBox<>(choices);
        typeBox.setValue("Text");
        typeBox.valueProperty().addListener(choiceListener);
        typeBox.setPrefWidth(150);

        VBox third = new VBox(typeLabel,typeBox);
        VBox.setMargin(typeBox,new Insets(0,0,5,0));


        Label padding2 = new Label();

        Button removeButton = new Button("⛌");
        removeButton.setOnAction(this::onRemoveFromList);

        VBox fourth = new VBox(padding2,removeButton);


        HBox hBox = new HBox(first,second,third,fourth);
        VBox vBox = new VBox(hBox,new Separator());

        HBox.setMargin(first,new Insets(10,5,10,5));
        HBox.setMargin(second,new Insets(10,5,10,0));
        HBox.setMargin(third,new Insets(10,5,10,5));
        HBox.setMargin(fourth,new Insets(10,0,0,0));


        columnContainer.getChildren().add(vBox);

    }

    public void onRemoveFromList(ActionEvent event) {
        columnContainer.getChildren().remove(((Button) event.getSource()).getParent().getParent().getParent());
    }

    public void onAddToList(ActionEvent event) {
    }
}
