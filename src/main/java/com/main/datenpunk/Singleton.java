package com.main.datenpunk;

import database.DAO;
import enteties.ChartDescriptor;
import enteties.ColumnInfo;
import enteties.Status;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class Singleton {

    DAO dao = DAO.getInstance();
    MainController controller;
    Stage mainstage;
    private String currentProject;
    private final String workingDirectory = System.getProperty("user.home") + "\\Datenpunk";
    private static Singleton instance = null;

    private List<ColumnInfo> columnInfo;

    private String password;


    private final List<VBox> columns = new ArrayList<>();
    private final List<String> columnNames = new ArrayList<>();
    List<List<Status>> choices = new ArrayList<>();
    List<String> choiceNames = new ArrayList<>();

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setController(MainController controller){
        this.controller = controller;
    }
    public MainController getController(){
        return controller;
    }

    public void setColumnInfo(){
        columnInfo = new ArrayList<>();
        columnInfo.addAll(dao.selectTableColumns());
    }

    public List<ColumnInfo> getColumnInfo(){
        return columnInfo;
    }

    public List<VBox> getColumns() {
        return columns;
    }

    private Singleton() {
    }

    public static Singleton getInstance() {
        if (instance == null)
            instance = new Singleton();
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

    public void removeFromProjectsFile(String location) throws IOException {

        File file = new File(workingDirectory+"\\projects.dtpnk");
        File tmpFile = new File(workingDirectory+"\\tmp.dtpnk");
        System.out.println(tmpFile.createNewFile());

        Scanner scanner = new Scanner(file);
        FileWriter fileWriter = new FileWriter(tmpFile,true);
        BufferedWriter writer = new BufferedWriter(fileWriter);
        String line;
        while(scanner.hasNext()){
            line = scanner.nextLine();
            if(!line.equals(location)){
                writer.append(line).append("\n");
            }
        }
        writer.close();
        fileWriter.close();
        scanner.close();


        System.out.println(file.delete());
        file = new File(workingDirectory + "\\projects.dtpnk");
        tmpFile.renameTo(file);
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setMainStage(Stage stage) {
        mainstage = stage;
    }

    public void openProject(){
        columns.clear();
        if(choiceNames != null)
            choiceNames.clear();
        if (columnInfo != null)
            columnInfo.clear();
        columnNames.clear();
        choices.clear();
        choiceNames.clear();

        setColumnInfo();

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
            Stage stage = mainstage;
            stage.setTitle("Datenpunk");
            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/main/datenpunk/application.css")).toExternalForm());
            stage.setScene(scene);
            MainController controller = fxmlLoader.getController();
            setController(controller);

            stage.setMaximized(true);
            stage.setResizable(true);
            stage.toFront();
            stage.show();
            controller.setupLater();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
