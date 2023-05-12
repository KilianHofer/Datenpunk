package com.main.datenpunk;

import database.DAO;
import enteties.ProjectTableElement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ResourceBundle;
import java.util.Scanner;

public class ProjectSelectionController implements Initializable {

    @FXML
    public TextField searchBar;
    @FXML
    public TableView<ProjectTableElement> projectTable;

    @FXML
    public TableColumn<ProjectTableElement,String> nameColumn, lastVisitedColumn,createdAtColumn, locationColumn;



    ObservableList<ProjectTableElement> projectTableElements= FXCollections.observableArrayList();



    DAO dao = DAO.getInstance();

    @FXML
    public void onNew() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("newProject-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());


        Stage stage = new Stage();

        stage.setTitle("New Project");
        stage.setScene(scene);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(searchBar.getScene().getWindow());

        NewProjectController newProjectController = fxmlLoader.getController();
        newProjectController.setReturnStage((Stage)searchBar.getScene().getWindow());      //TODO: better data transfer
        stage.setResizable(false);
        stage.show();
    }

    @FXML
    public void onOpenFromFile() {

    }

    @FXML
    public void onClose() {
    }

    public void onDelete() throws IOException {

        if(projectTable.getSelectionModel().getSelectedItem() != null){
            ProjectTableElement element = projectTable.getSelectionModel().getSelectedItem();
            String location = element.getLocation();
            Alert alert = new Alert((Alert.AlertType.CONFIRMATION));
            alert.setContentText("Do you want to delete Project: " + element.getName());
            if(alert.showAndWait().get() == ButtonType.OK){
                File file = new File(location);
                file.delete();
                String subPath = System.getProperty("user.home")+"\\Datenpunk";
                file = new File(subPath+"\\Projects\\"+element.getName());
                file.delete();



                if (!checkSavedPasswordAndConnect(new File(subPath + "\\connection.dtpnk"), "")) {

                    FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("databaseConnection-view.fxml"));
                    Scene scene = new Scene(fxmlLoader.load());

                    Stage stage = new Stage();

                    stage.setTitle("Connect to Database");
                    stage.setScene(scene);

                    DatabaseConnectionController databaseConnectionController = fxmlLoader.getController();
                    databaseConnectionController.setName("");      //TODO: better data transfer
                    databaseConnectionController.setRetrunStage((Stage) searchBar.getScene().getWindow());
                    databaseConnectionController.setDeletion(true);
                    databaseConnectionController.setName(element.getName());
                    stage.setResizable(false);
                    stage.show();
                    }
                else {
                    dao.dropDatabase(element.getName());
                }

                RemoveFromProjectsFile(location);

            }
        }
}

    private void RemoveFromProjectsFile(String location) throws IOException {
        String subPath = System.getProperty("user.home")+"\\Datenpunk";
        File file = new File(subPath+"\\projects.dtpnk");
        File tmpFile = new File(subPath+"\\tmp.dtpnk");
        System.out.println(tmpFile.createNewFile());



        Scanner scanner = new Scanner(file);
        BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile,true));
        String line;
        while(scanner.hasNext()){
            line = scanner.nextLine();
            if(!line.equals(location)){
                writer.append(line).append("\n");
            }
        }
        scanner.close();
        writer.close();


        System.out.println(file.delete());
        file = new File(subPath + "\\projects.dtpnk");
        tmpFile.renameTo(file);

        getProjects(file);
    }

    public void onSearch() {
    }


    public boolean checkSavedPasswordAndConnect(File file, String dbName){
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
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return false;

    }

    public void onOpen() throws IOException {
        if(projectTable.getSelectionModel().getSelectedItem() != null){
            ProjectTableElement element = projectTable.getSelectionModel().getSelectedItem();


            File file = new File(System.getProperty("user.home")+"\\Datenpunk\\conneciton.dtpnk");
            if(checkSavedPasswordAndConnect(file,element.getName())){
                openProject();
            }
            else {

                FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("databaseConnection-view.fxml"));
                Scene scene = new Scene(fxmlLoader.load());


                Stage stage = new Stage();

                stage.initModality(Modality.WINDOW_MODAL);
                stage.initOwner(projectTable.getScene().getWindow());

                stage.setTitle("Connect to Database");
                stage.setScene(scene);

                DatabaseConnectionController databaseConnectionController = fxmlLoader.getController();
                databaseConnectionController.setName(element.getName());      //TODO: better data transfer
                databaseConnectionController.setRetrunStage((Stage) stage.getOwner());
                databaseConnectionController.setNew(false);
                stage.setResizable(false);
                stage.show();
            }



        }
    }

    private void openProject() throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        Stage stage = (Stage) projectTable.getScene().getWindow();
        stage.setTitle("Datenpunk");
        stage.setScene( new Scene(fxmlLoader.load()));
        stage.setMaximized(true);
        stage.setResizable(true);
        stage.show();


    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        lastVisitedColumn.setCellValueFactory(new PropertyValueFactory<>("lastVisited"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));

        String path = System.getProperty("user.home")+"\\Datenpunk";
        File file = new File(path);
        try {
            if (!file.exists()) {
                Files.createDirectory(file.toPath());
            }

            path += "\\projects.dtpnk";
            file = new File(path);
            if (!file.exists()) {
                Files.createFile(file.toPath());
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        getProjects(file);

    }

    private void getProjects(File file)  {
        try {
            projectTableElements = FXCollections.observableArrayList();
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()){
                getProjectData(scanner.nextLine());
            }
            scanner.close();

            projectTable.getItems().setAll(projectTableElements);
            projectTable.getSortOrder().add(lastVisitedColumn);

        } catch (FileNotFoundException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void getProjectData(String path) {
        File file = new File(path);
        try {

            int startPos = path.lastIndexOf('\\')+1;
            String name = path.substring(startPos,path.length()-7);         //.dtpnkl and .dtpnkr are 7 chars long

            boolean local = path.charAt(path.length() - 1) != 'r';

            BasicFileAttributes attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);

            projectTableElements.add(new ProjectTableElement(name,attributes.lastAccessTime().toString(),attributes.creationTime().toString(),path,local));
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    public void onDeleteSavedPasswords() {
        File connectionFile = new File(System.getProperty("user.home")+"\\Datenpunk\\connection.dtpnk");
        Alert alert;
        if(connectionFile.delete()){
            alert = new Alert((Alert.AlertType.INFORMATION));
            alert.setContentText("Saved passwords have been deleted!");
            alert.showAndWait();
        }
        else{
            alert = new Alert((Alert.AlertType.ERROR));
            alert.setContentText("Could not delete saved passwords!");
            alert.showAndWait();
        }
    }
}
