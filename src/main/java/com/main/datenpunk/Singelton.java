package com.main.datenpunk;

import database.DAO;
import enteties.ChartDescriptor;
import enteties.ColumnInfo;
import javafx.scene.chart.*;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;

public class Singelton {

    DAO dao = DAO.getInstance();
    MainController controller;

    private String currentProject;
    private final String workingDirectory = System.getProperty("user.home") + "\\Datenpunk";
    private static Singelton instance = null;

    private List<ColumnInfo> columnInfo;

    public void setController(MainController controller){
        this.controller = controller;
    }
    public MainController getController(){
        return controller;
    }

    public void setColumnInfo(){
        columnInfo = new ArrayList<>();
        columnInfo.add(new ColumnInfo("objects","id", false,false));
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
}
