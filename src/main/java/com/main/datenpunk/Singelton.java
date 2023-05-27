package com.main.datenpunk;

import database.DAO;
import enteties.ChartDescriptor;
import enteties.ColumnInfo;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Singelton {

    DAO dao = DAO.getInstance();
    MainController controller;

    Stage primaryStage;

    private String currentProject;
    private final String workingDirectory = System.getProperty("user.home") + "\\Datenpunk";
    private static Singelton instance = null;

    private List<ColumnInfo> columnInfo;

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setController(MainController controller){
        this.controller = controller;
    }
    public MainController getController(){
        return controller;
    }

    public void setColumnInfo(){
        columnInfo = new ArrayList<>();
        columnInfo.add(new ColumnInfo("objects","id", false));
        columnInfo.addAll(dao.selectTableColumns("objects"));
        columnInfo.addAll(dao.selectTableColumns("history"));
    }

    public List<ColumnInfo> getColumnInfo(){
        return columnInfo;
    }


    private Singelton() {
    }

    public static Singelton getInstance() {
        if (instance == null)
            instance = new Singelton();
        return instance;
    }

    public void setCurrentProject(String name) {
        currentProject = name;
    }

    public String getCurrentProject() {
        return currentProject;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void threadGenerateChart(VBox box, ChartDescriptor chartDescriptor) {
        new ChartService(box,chartDescriptor).start();
    }

    public boolean checkSavedPasswordAndConnect(String dbName){
        File file = new File(getWorkingDirectory()+"\\connection.dtpnk");
        String password;
        if(!dbName.equals("")){
            dbName = "datenpunk_" +dbName;
        }

        try {
            if (file.exists()) {
                Scanner scanner = new Scanner(file);
                if (scanner.hasNext()) {
                    password = scanner.next();
                    dao.connectToDB(dbName,"postgres",password);
                    return true;
                }
                scanner.close();
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public void openProjectWindow(String name) throws IOException {
        setCurrentProject(name);
        setColumnInfo();
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        Stage stage = primaryStage;
        stage.setTitle("Datenpunk");
        stage.setScene( new Scene(fxmlLoader.load()));
        stage.setMaximized(true);
        stage.setResizable(true);
        stage.show();
    }

    public void openDatabaseConnectionWindow(Stage stage,String name,boolean newProject,boolean deletion) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("databaseConnection-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(primaryStage);

        stage.setTitle("Connect to Database");
        stage.setScene(scene);

        DatabaseConnectionController databaseConnectionController = fxmlLoader.getController();
        databaseConnectionController.setName(name);      //TODO: better data transfer
        databaseConnectionController.setNew(newProject);
        databaseConnectionController.setDeletion(deletion);
        stage.setResizable(false);
        stage.show();
    }

}
