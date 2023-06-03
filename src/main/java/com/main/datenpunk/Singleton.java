package com.main.datenpunk;

import database.DAO;
import enteties.ChartDescriptor;
import enteties.ColumnInfo;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;

public class Singleton {

    DAO dao = DAO.getInstance();
    MainController controller;

    public boolean sorting = false;

    private String currentProject;
    private final String workingDirectory = System.getProperty("user.home") + "\\Datenpunk";
    private static Singleton instance = null;

    private List<ColumnInfo> columnInfo;


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
}
