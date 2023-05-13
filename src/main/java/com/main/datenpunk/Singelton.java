package com.main.datenpunk;

public class Singelton {

    private String currentProject;
    private final String workingDirectory = System.getProperty("user.home")+"\\Datenpunk";
    private static Singelton instance = null;


    private Singelton(){}

    public static Singelton getInstance() {
        if(instance == null)
            instance = new Singelton();
        return instance;
    }

    public void setCurrentProject(String name){
        currentProject = name;
    }
    public String getCurrentProject(){
        return currentProject;
    }
    public String getWorkingDirectory(){
        return workingDirectory;
    }
}
