package database;

import enteties.HistoryElement;
import enteties.TableElement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.text.Format;
import java.text.SimpleDateFormat;

public class DAO {      //TODO: make singelton

    private Connection connection;

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
            String query = "SELECT o.id, o.name, o.type, h.status from objects o join history h on (o.id = h.id) join (select id,max(timestamp) as t from history group by id) as i on i.id = o.id and i.t=h.timestamp";
            statement = connection.prepareStatement(query);
            resultSet = statement.executeQuery();

            int id;
            String name, type, status;

            while(resultSet.next()){
                id = resultSet.getInt("id");
                name = resultSet.getString("name");
                type = resultSet.getString("type");
                status = resultSet.getString("status");
                tableElements.add(new TableElement(id,name,type,status));
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
            statement.setString(2,"Planned");   //TODO: check for existing statuses
            statement.setLong(3,System.currentTimeMillis());
            statement.execute();
            return id;


        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    public void update(int id, String status){

        String querey = ("INSERT INTO history VALUES (?,?,?)");
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
