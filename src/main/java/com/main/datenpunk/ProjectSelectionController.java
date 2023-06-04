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



    ObservableList<ProjectTableElement> projectTableElements= FXCollections.observableArrayList();



    DAO dao = DAO.getInstance();
    Singleton singleton = Singleton.getInstance();

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
        stage.show();
    }

    @FXML
    public void onOpenFromFile() throws IOException {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "\\Datenpunk\\Projects"));
        String path = String.valueOf(fileChooser.showOpenDialog(projectTable.getScene().getWindow()));
        String subString = path.substring(path.lastIndexOf("."));
        if(subString.equals(".dtpnkl") || subString.equals(".dtpnkr")){

            File file = new File(path);

            if(!inProjectList(path)){
                FileWriter fileWriter = new FileWriter(System.getProperty("user.home")+"\\Datenpunk\\projects.dtpnk",true);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.append(path).append("\n");
                bufferedWriter.close();
                fileWriter.close();
            }


            String name = path.substring(path.lastIndexOf("\\")+1,path.lastIndexOf("."));

            BasicFileAttributes attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            boolean local = path.charAt(path.length() - 1) != 'r';
            openProject(new ProjectTableElement(name,attributes.lastAccessTime().toString(),attributes.creationTime().toString(),file.getAbsolutePath(),local));
        }



    }
    private boolean inProjectList(String path) {

        Scanner scanner = new Scanner(System.getProperty("user.home")+"\\Datenpunk\\projects.dtpnk");

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

    public void onDelete() throws IOException {

        if(projectTable.getSelectionModel().getSelectedItem() != null){
            ProjectTableElement element = projectTable.getSelectionModel().getSelectedItem();
            Alert alert = new Alert((Alert.AlertType.CONFIRMATION));
            alert.setContentText("Do you want to delete Project: " + element.getName());
            if(alert.showAndWait().get() == ButtonType.OK){
            deleteProject(element.getName(),element.getLocation());
            }
        }
    }


    public void deleteProject(String name, String location) throws IOException {

            String subString = System.getProperty("user.home")+"\\Datenpunk";
            File file = new File(subString+"\\Projects\\"+name);
            file.delete();

            file = new File(location);
            file.delete();


            if (!checkSavedPasswordAndConnect(new File(subString + "\\connection.dtpnk"), "")) {

                FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("databaseConnection-view.fxml"));
                Scene scene = new Scene(fxmlLoader.load());

                Stage stage = new Stage();

                stage.setTitle("Connect to Database");
                stage.setScene(scene);

                DatabaseConnectionController databaseConnectionController = fxmlLoader.getController();
                databaseConnectionController.setName("");      //TODO: better data transfer
                databaseConnectionController.setRetrunStage((Stage) searchBar.getScene().getWindow());
                databaseConnectionController.setDeletion(true);
                databaseConnectionController.setName(name);
                stage.setResizable(false);
                stage.show();
            }
            else {
                dao.dropDatabase(name);
            }

            removeFromProjectsFile(location);

    }



    public void removeFromProjectsFile(String location) throws IOException {
        String subPath = System.getProperty("user.home")+"\\Datenpunk";
        File file = new File(subPath+"\\projects.dtpnk");
        File tmpFile = new File(subPath+"\\tmp.dtpnk");
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
                scanner.close();
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return false;

    }

    public void onOpen() throws IOException {
        if(projectTable.getSelectionModel().getSelectedItem() != null){
                        ProjectTableElement element = projectTable.getSelectionModel().getSelectedItem();
            openProject(element);
        }
    }

    private void openProject(ProjectTableElement element) throws IOException {
        File file = new File(System.getProperty("user.home")+"\\Datenpunk\\connection.dtpnk");
        if(checkSavedPasswordAndConnect(file,element.getName())){
            if(singleton.getColumns() != null)
                singleton.getColumns().clear();
            if(singleton.getColumnInfo() != null)
                singleton.getColumnInfo().clear();
            singleton.choices.clear();
            singleton.choiceNames.clear();

            singleton.setCurrentProject(element.getName());
            singleton.setColumnInfo();
            openProjectWindow();
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

    private void openProjectWindow() throws IOException {



        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        Stage stage = (Stage) projectTable.getScene().getWindow();
        stage.setTitle("Datenpunk");
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/main/datenpunk/application.css")).toExternalForm());
        MainController controller = fxmlLoader.getController();
        singleton.setController(controller);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setResizable(true);
        stage.show();
        controller.initializeCellFactories();


    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        projectTable.setRowFactory( tableView -> {
            TableRow<ProjectTableElement> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 && (!row.isEmpty())){
                    if(event.getButton().equals(MouseButton.PRIMARY)) {
                        if (event.getClickCount() == 2) {               //TODO: known issue: opens detail view of selected item even by double-click on table header

                            try {
                                openProject(projectTable.getSelectionModel().getSelectedItem());
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

    public void initalizeTable(){
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

        projectTable.getSelectionModel().select(0);

    }
    public void getProjects(File file)  {
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

            projectTableElements.add(new ProjectTableElement(name,attributes.lastAccessTime().toString(),attributes.creationTime().toString(),path,local));
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
