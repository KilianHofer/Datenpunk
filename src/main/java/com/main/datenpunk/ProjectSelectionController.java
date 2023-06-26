package com.main.datenpunk;

import database.DAO;
import enteties.ProjectTableElement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Scanner;

public class ProjectSelectionController implements Initializable {

    @FXML
    public TextField searchBar;
    @FXML
    public TableView<ProjectTableElement> projectTable;

    @FXML
    public TableColumn<ProjectTableElement,String> nameColumn, lastVisitedColumn,createdAtColumn, locationColumn;

    private String nameToDelete,pathToDelete;

    ObservableList<ProjectTableElement> projectTableElements= FXCollections.observableArrayList();



    DAO dao = DAO.getInstance();
    Singleton singleton = Singleton.getInstance();


    public void setToDelete(String name, String path){
        nameToDelete = name;
        pathToDelete = path;
    }

    @FXML
    public void onNew() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("newProject-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());


        Stage stage = new Stage();

        stage.setTitle("New Project");
        stage.setScene(scene);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(searchBar.getScene().getWindow());

        singleton.setMainStage((Stage)searchBar.getScene().getWindow());
        stage.setHeight(600);
        stage.show();
    }

    @FXML
    public void onOpenFromFile() throws IOException {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "\\Datenpunk\\Projects"));
        String path = String.valueOf(fileChooser.showOpenDialog(projectTable.getScene().getWindow()));
        String subString = path.substring(path.lastIndexOf("."));
        if(subString.equals(".dtpnkl") || subString.equals(".dtpnkr")){

            if(!inProjectList(path)){
                                FileWriter fileWriter = new FileWriter(singleton.getWorkingDirectory()+"\\projects.dtpnk",true);
                BufferedWriter writer = new BufferedWriter(fileWriter);
                writer.append(path);
                writer.flush();
                writer.close();
                fileWriter.close();
                getProjects();
            }
            for(ProjectTableElement projectTableElement:projectTable.getItems()){
                if(projectTableElement.getLocation().equals(path)){
                    projectTable.getSelectionModel().select(projectTableElement);
                    break;
                }
            }
            openProject();
        }



    }
    private boolean inProjectList(String path) {

        Scanner scanner = new Scanner(singleton.getWorkingDirectory()+"\\projects.dtpnk");

        while (scanner.hasNext()){
            if(scanner.next().equals(path))
                return true;
        }
        return false;
    }

    @FXML
    public void onClose() {
        ((Stage)projectTable.getScene().getWindow()).close();
    }

    public void onDelete() {

        if(projectTable.getSelectionModel().getSelectedItem() != null){
            ProjectTableElement element = projectTable.getSelectionModel().getSelectedItem();
            Alert alert = new Alert((Alert.AlertType.CONFIRMATION));
            alert.setContentText("Do you want to delete Project: " + element.getName());
            if(alert.showAndWait().get() == ButtonType.OK){
                setToDelete(element.getName(),element.getLocation());
                deleteProject();
            }
        }
    }

    public void deleteProject() {



            String subString = System.getProperty("user.home")+"\\Datenpunk";

            try {
                if (singleton.getPassword() == null) {
                    if (!checkSavedPassword()) {
                        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("databaseConnection-view.fxml"));
                        Scene scene = new Scene(fxmlLoader.load());
                        Stage stage = new Stage();
                        DatabaseConnectionController connectionController = fxmlLoader.getController();
                        connectionController.method = this::deleteProject;
                        stage.setTitle("Connect to Database");
                        stage.setScene(scene);
                        stage.setResizable(false);
                        stage.show();
                        return;
                    }
                }
                    dao.connectToDB("", "postgres", singleton.getPassword());
                    dao.dropDatabase(nameToDelete);
                    singleton.removeFromProjectsFile(pathToDelete);
                    File file = new File(subString + "\\Projects\\" + nameToDelete);
                    if(file.exists()) {
                        for (File childFile : Objects.requireNonNull(file.listFiles())) {
                            Files.delete(childFile.toPath());
                        }
                        Files.delete(file.toPath());
                    }
                    file = new File(pathToDelete);
                    if(file.exists())
                        Files.delete(file.toPath());
                    getProjects();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }

    public boolean checkSavedPassword(){
        File file = new File(singleton.getWorkingDirectory()+"\\connection.dtpnk");
        String password;
        try {
            if (file.exists()) {
                Scanner scanner = new Scanner(file);
                if (scanner.hasNext()) {
                    password = scanner.next();
                    singleton.setPassword(password);
                    return true;
                }
                scanner.close();
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return false;

    }

    public void onOpen() throws IOException {
        if(projectTable.getSelectionModel().getSelectedItem() != null){
            openProject();
        }
    }

    private void openProject() throws IOException {

        if(singleton.getPassword() == null){
            checkSavedPassword();
            if (!dao.connectToDB("", "postgres", singleton.getPassword())) {

                FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("databaseConnection-view.fxml"));
                Scene scene = new Scene(fxmlLoader.load());
                Stage stage = new Stage();
                DatabaseConnectionController connectionController = fxmlLoader.getController();
                connectionController.method = this::openProjectWindow;
                stage.setTitle("Connect to Database");
                stage.setScene(scene);
                stage.setResizable(false);
                stage.show();
            }
            else {
                openProjectWindow();
            }
        }
        else {
            openProjectWindow();
        }
    }

    private void openProjectWindow(){

        ProjectTableElement element = projectTable.getSelectionModel().getSelectedItem();

        try {
            FileWriter fileWriter = new FileWriter(element.getLocation());
            fileWriter.write('!');                                      //accesses the file so that last accessed works properly
            fileWriter.flush();
            fileWriter.close();



            if(dao.connectToDB("datenpunk_"+element.getName(), "postgres", singleton.getPassword())) {
                singleton.setCurrentProject(element.getName());
                singleton.openProject();
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        projectTable.setRowFactory( tableView -> {
            TableRow<ProjectTableElement> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 && (!row.isEmpty())){
                    if(event.getButton().equals(MouseButton.PRIMARY)) {
                        if (event.getClickCount() == 2) {

                            try {
                                openProject();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                        }
                    }
                }
            });
            return row;
        });



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
    }

    public void initializeTable(){
        singleton.setMainStage((Stage) projectTable.getScene().getWindow());
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
        getProjects();

        projectTable.getSelectionModel().select(0);

    }
    public void getProjects()  {

        File file = new File(singleton.getWorkingDirectory()+"\\projects.dtpnk");
        try {
            projectTableElements = FXCollections.observableArrayList();
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()){
                getProjectData(scanner.nextLine());
            }
            scanner.close();

            FilteredList<ProjectTableElement> filteredList = new FilteredList<>(projectTableElements, elements -> true);
            searchBar.textProperty().addListener((obserbable, oldValue, newValue) -> filteredList.setPredicate(element -> {
                if(newValue == null || newValue.isEmpty()) return  true;
                return element.getName().toLowerCase().contains(newValue.toLowerCase());

            }));

            SortedList<ProjectTableElement> sortedList = new SortedList<>(filteredList);
            sortedList.comparatorProperty().bind(projectTable.comparatorProperty());
            projectTable.setItems(sortedList);
            projectTable.getSortOrder().add(lastVisitedColumn);

        } catch (FileNotFoundException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void getProjectData(String path) throws IOException {
        File file = new File(path);
        try {

            int startPos = path.lastIndexOf('\\')+1;
            String name = path.substring(startPos,path.length()-7);         //.dtpnkl and .dtpnkr are 7 chars long

            boolean local = path.charAt(path.length() - 1) != 'r';

            BasicFileAttributes attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);

            LocalDateTime visitedDate = LocalDateTime.ofInstant(attributes.lastAccessTime().toInstant(), ZoneId.systemDefault());
            LocalDateTime createdDate = LocalDateTime.ofInstant(attributes.creationTime().toInstant(), ZoneId.systemDefault());

            String visitedAt = visitedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String createdAt = createdDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            projectTableElements.add(new ProjectTableElement(name,visitedAt,createdAt,path,local));
        } catch (IOException ignore) {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("fileNotFound-view.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Error");
            stage.setScene( new Scene(fxmlLoader.load()));
            stage.setResizable(false);

            FileNotFoundController controller = fxmlLoader.getController();
            controller.setProjectSelectionController(this);      //TODO: better data transfer
            controller.setErrorMessage("File '"+file.getName()+"' could not be found at location:\n" + file.getAbsolutePath());

            stage.show();
        }
    }

    public void onDeleteSavedPasswords() {
        singleton.setPassword(null);
        File connectionFile = new File(System.getProperty("user.home")+"\\Datenpunk\\connection.dtpnk");
        Alert alert;
        if(connectionFile.delete()){

            alert = new Alert((Alert.AlertType.INFORMATION));
            alert.setContentText("Saved passwords have been deleted!");
            alert.showAndWait();
        }
        else if (connectionFile.exists()){
            alert = new Alert((Alert.AlertType.ERROR));
            alert.setContentText("Could not delete saved passwords!");
            alert.showAndWait();
        }
    }
}
