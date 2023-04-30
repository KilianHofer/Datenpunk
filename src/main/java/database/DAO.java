package database;

import enteties.HistoryElement;
import enteties.Status;
import enteties.TableElement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class DAO {

    private Connection connection;

    private static DAO instance = null;

    private DAO(){
    }

    public static DAO getInstance(){
        if(instance == null){
            instance = new DAO();
        }
        return instance;

    }


    public Boolean connectToDB(String name, String user, String password) {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + name, user, password);
            if (connection != null) {
                System.out.println("connected!");
                return true;
            } else {
                System.out.println("connection failed!");
                return false;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }


    public ObservableList<TableElement> selectMain(){

        ObservableList<TableElement> tableElements = FXCollections.observableArrayList();

        PreparedStatement statement;
        ResultSet resultSet;
        try {
            String query = "SELECT o.id, o.name, o.type, h.status, s.sortOrder  FROM objects o JOIN history h ON (o.id = h.id) JOIN (SELECT id,max(timestamp) AS t FROM history GROUP BY id) AS i ON (i.id = o.id AND i.t=h.timestamp) JOIN status s ON s.name=h.status";
            statement = connection.prepareStatement(query);
            resultSet = statement.executeQuery();

            int id,sortOrder;
            String name, type, status;

            while(resultSet.next()){
                id = resultSet.getInt("id");
                name = resultSet.getString("name");
                type = resultSet.getString("type");
                status = resultSet.getString("status");
                sortOrder = resultSet.getInt("sortOrder");
                tableElements.add(new TableElement(id,name,type,status,sortOrder));
            }
            return tableElements;
        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    public ObservableList<HistoryElement> selectHistory(int id){

        ObservableList<HistoryElement> history = FXCollections.observableArrayList();
        PreparedStatement statement;
        ResultSet resultSet;
        String query = "SELECT * FROM history WHERE id=?";
        try{
            statement = connection.prepareStatement(query);
            statement.setInt(1,id);
            resultSet = statement.executeQuery();

            String status, timestamp;

            while(resultSet.next()){
                status = resultSet.getString("status");
                long time = resultSet.getLong("timestamp");
                Date date = new Date(time);
                Format format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                timestamp = format.format(date);

                history.add(new HistoryElement(status,timestamp));
            }
            return history;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }


    public Integer getMaxStatusSortOrder(){
        try{
            String query = "SELECT sortOrder FROM status ORDER BY sortOrder DESC LIMIT 1";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                return resultSet.getInt("sortOrder");
            }


        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    public List<Status> selectStatuses(){
        try{
            List<Status> statuses = new ArrayList<>();
            String query = "SELECT name,sortOrder FROM status";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            int sortOrder;
            String name;

            while(resultSet.next()){
                sortOrder = resultSet.getInt("sortOrder");
                name = resultSet.getString("name");
                statuses.add(new Status(name,sortOrder));
            }
            return statuses;


        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    public Integer selectSortOrder(String name) {
        try{
            String query = "SELECT sortOrder FROM status WHERE name = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1,name);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                return resultSet.getInt("name");
            }


        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }


    public Integer insert(String name, String type){

        try {
            String query = ( "INSERT INTO objects(name, type) VALUES(?,?);");
            PreparedStatement statement = connection.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
            statement.setString(1,name);
            statement.setString(2,type);
            statement.execute();
            int id = -1;
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                id = generatedKeys.getInt(1);
            }
            query = "INSERT INTO History(id, status, timestamp) VALUES(?,?,?)";
            statement = connection.prepareStatement(query);
            statement.setInt(1,id);
            statement.setString(2,"Planned");   //TODO: make default setting
            statement.setLong(3,System.currentTimeMillis());
            statement.execute();
            return id;


        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    public void updateValues(int id, String name, String type){
        String query = "UPDATE objects SET name = ?, type = ? WHERE id = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1,name);
            statement.setString(2,type);
            statement.setInt(3,id);
            statement.executeUpdate();
        }catch (SQLException e){
            System.out.println(e.getMessage());         //TODO: better error handling
        }
    }

    public void updateHistory(int id, String status){

        String querey = "INSERT INTO history VALUES (?,?,?)";
        try {
            PreparedStatement statement = connection.prepareStatement(querey);
            statement.setInt(1,id);
            statement.setString(2,status);
            statement.setLong(3,System.currentTimeMillis());
            statement.execute();

        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }


}
