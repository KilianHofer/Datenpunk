package database;

import java.sql.*;

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
            System.out.println(e);
            return false;
        }
    }


    public void seletAll(){
        Statement statement;
        ResultSet resultSet;
        try {
            String query = "SELECT * FROM objects";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);

            while(resultSet.next()){
                System.out.print(resultSet.getString("name"));
                System.out.print(" ");
                System.out.println(resultSet.getString("type"));
            }
        }catch (Exception e){
            System.out.println(e);
        }
    }

    public void insert(String name, String type, Long timestamp){

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
            statement.setLong(3,timestamp);
            statement.execute();


        }catch (Exception e){
            System.out.println(e);
        }



    }

    public Connection getConnection() {
        return connection;
    }
}
