package database;

import enteties.ColumnInfo;
import enteties.HistoryTableElement;
import enteties.Status;
import enteties.ObjectTableElement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

import java.sql.*;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.ArrayList;

import java.util.List;

public class DAO {

    private Connection connection;
    public Format format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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


    public void createDatabase(String name){
        try{
            String query = "CREATE DATABASE \"datenpunk_" +name+"\"";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public void dropDatabase(String name){
        try{
            String query = "DROP DATABASE \"datenpunk_" +name+"\"";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void createTables(){
        try{
            //Create objects Table
            String query = "CREATE TABLE objects(id SERIAL PRIMARY KEY,name VARCHAR(200),type VARCHAR(200))";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();

            //Create status Table
            query = "CREATE TABLE status(id SERIAL PRIMARY KEY, sortorder INT UNIQUE NOT NULL, name VARCHAR(200) NOT NULL UNIQUE,colour CHAR(7))";
            statement = connection.prepareStatement(query);
            statement.executeUpdate();
            fillStatusTable();

            //create history Table
            query = "CREATE TABLE history(id INT,status VARCHAR(200),timestamp BIGINT,PRIMARY KEY(id,timestamp),FOREIGN KEY(id) REFERENCES objects(id), FOREIGN KEY (status) REFERENCES status(name))";
            statement = connection.prepareStatement(query);
            statement.executeUpdate();


        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    private void fillStatusTable() {
        try{
            String query =  "INSERT INTO status(sortorder,name,colour) VALUES(1,'Complete','#66ff66');" +
                            "INSERT INTO status(sortorder,name,colour) VALUES(2,'In-Progress','#ffff00');" +
                            "INSERT INTO status(sortorder,name,colour) VALUES(3,'Planned','#ff0000');";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public List<ColumnInfo> selectTableColumns(String table){
        List<ColumnInfo> names = new ArrayList<>();
        try{
            String query = "SELECT column_name,data_type FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = N'"+table+"' AND column_name != 'id'";
            PreparedStatement statement = connection.prepareStatement(query);
            System.out.println(statement);
            ResultSet resultSet = statement.executeQuery();
            ColumnInfo columnInfo;
            while (resultSet.next()){
                columnInfo = new ColumnInfo(table,resultSet.getString("column_name"),resultSet.getString("data_type").contains("char"));
                names.add(columnInfo);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return names;
    }


    private void buildString(List<String> strings,ObservableList<ListView<String>> listViews,int id, String name){
        if(listViews.get(id).getItems().size() > 0) {
            ObservableList<String> stringList = listViews.get(id).getItems();
            for (int i = 0; i < stringList.size(); i++) {
                strings.set(id, strings.get(id).concat("LOWER("));
                strings.set(id, strings.get(id).concat( name));
                strings.set(id, strings.get(id).concat(")"));
                if(id >= strings.size()/2){
                    strings.set(id, strings.get(id).concat(" NOT"));
                }
                strings.set(id, strings.get(id).concat(" LIKE "));
                strings.set(id, strings.get(id).concat("'%"));
                strings.set(id, strings.get(id).concat(stringList.get(i).toLowerCase()));
                strings.set(id, strings.get(id).concat("%'"));
                if(i <= stringList.size()-2){
                    strings.set(id, strings.get(id).concat(" OR "));
                }
            }
        }
    }


    public ObservableList<ObjectTableElement> selectMain(LocalDate fromDate,LocalDate toDate, ObservableList<ListView<String>> listViews){

        ObservableList<ObjectTableElement> objectTableElements = FXCollections.observableArrayList();

        List<String> lists = new ArrayList<>();
        for (int i = 0; i < listViews.size(); i++) {
            lists.add("");
        }
        buildString(lists,listViews,0,"o.name");                //TODO: this will have to be dynamic in the future
        buildString(lists,listViews,1,"o.type");
        buildString(lists,listViews,2,"h.status");
        buildString(lists,listViews,3,"o.name");
        buildString(lists,listViews,4,"o.type");
        buildString(lists,listViews,5,"h.status");


        StringBuilder subquery = new StringBuilder();
        boolean first = true;
        for (String list : lists) {

            if (!list.equals("")) {
                if (first) {
                    subquery.append(" WHERE ");
                    first = false;
                } else {
                    subquery.append(" AND ");
                }
                subquery.append(list);
            }
        }




        long fromTimestamp;
        if(fromDate == null)
            fromTimestamp = 0;
        else
            fromTimestamp = ZonedDateTime.of(fromDate.atTime(23,59), ZoneId.systemDefault()).toInstant().toEpochMilli();

        long toTimestamp = ZonedDateTime.of(toDate.atTime(23,59), ZoneId.systemDefault()).toInstant().toEpochMilli();

        PreparedStatement statement;
        ResultSet resultSet;
        try {
            String query = "SELECT o.id, o.name, o.type, h.status, s.sortOrder, s.colour, i.t  FROM objects o JOIN history h ON (o.id = h.id) JOIN (SELECT id,max(timestamp) AS t FROM history WHERE timestamp >= ? AND timestamp <= ? GROUP BY id) AS i ON (i.id = o.id AND i.t=h.timestamp) JOIN status s ON s.name=h.status" + subquery; //TODO: fix SQL-Injection
            statement = connection.prepareStatement(query);
            statement.setLong(1,fromTimestamp);
            statement.setLong(2,toTimestamp);
            System.out.println(statement);
            resultSet = statement.executeQuery();

            int id,sortOrder;
            String name, type, status, color;
            long timestampOut;

            while(resultSet.next()){
                id = resultSet.getInt("id");
                name = resultSet.getString("name");
                type = resultSet.getString("type");
                status = resultSet.getString("status");
                sortOrder = resultSet.getInt("sortOrder");
                color = resultSet.getString("colour");
                timestampOut = resultSet.getLong("t");
                objectTableElements.add(new ObjectTableElement(id,name,type,new Status(status,sortOrder,color),format.format(timestampOut)));
            }
            return objectTableElements;
        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    public ObjectTableElement selectElement(int id){
        try {
            String query = "SELECT o.id, o.name, o.type, h.status, s.sortOrder, s.colour, i.t  FROM objects o JOIN history h ON (o.id = h.id) JOIN (SELECT id,max(timestamp) AS t FROM history GROUP BY id) AS i ON (i.id = o.id AND i.t=h.timestamp) JOIN status s ON s.name=h.status WHERE o.id = ?";
            PreparedStatement statement =  connection.prepareStatement(query);
            statement.setInt(1,id);
            ResultSet resultSet = statement.executeQuery();

            int sortOrder;
            String name, type, status, color;
            long timestamp;

            if(resultSet.next()){

                name = resultSet.getString("name");
                type = resultSet.getString("type");
                status = resultSet.getString("status");
                sortOrder = resultSet.getInt("sortOrder");
                color = resultSet.getString("colour");
                timestamp = resultSet.getLong("t");
                return new ObjectTableElement(id,name,type,new Status(status,sortOrder,color), format.format(timestamp));
            }


        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    public ObservableList<HistoryTableElement> selectHistory(int id){

        ObservableList<HistoryTableElement> history = FXCollections.observableArrayList();
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
                timestamp = format.format(date);

                history.add(new HistoryTableElement(status,timestamp));
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
            String query = "SELECT * FROM status";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            int sortOrder;
            String name, color;

            while(resultSet.next()){
                sortOrder = resultSet.getInt("sortOrder");
                name = resultSet.getString("name");
                color = resultSet.getString("colour");
                statuses.add(new Status(name,sortOrder,color));
            }
            return statuses;


        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    public Status selectStatus(String name) {
        try{
            String query = "SELECT * FROM status WHERE name = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1,name);
            ResultSet resultSet = statement.executeQuery();
            int sortOrder;
            String sName, color;

            if(resultSet.next()){
                sortOrder = resultSet.getInt("sortOrder");
                sName = resultSet.getString("name");
                color = resultSet.getString("colour");
                return new Status(sName,sortOrder,color);
            }


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
                return resultSet.getInt("sortorder");
            }


        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    public String getFirstOrLastValue(boolean first, String source){
        try{
            String query;
            if(first)
                query = "SELECT "+source+ " FROM history,objects ORDER BY "+source+ " ASC LIMIT 1";
            else
                query = "SELECT "+source+ " FROM history,objects ORDER BY "+source+ " DESC LIMIT 1";

            PreparedStatement statement = connection.prepareStatement(query);
            System.out.println(statement);
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()){
                return String.valueOf(resultSet.getLong(source.substring(source.indexOf(".")+1)));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public Float getContinuousValues(Number start,Number end, String yAxis, String value, long startDataTimestamp, long endDataTimestamp, String comparator, String xAxis){
        try{
            String subquery1 = "",subquery2 = "", subquery3 = "";
            switch (value) {
                case "All" -> subquery1 = "COUNT";
                case "value" -> subquery3 = " ORDER BY history.timestamp DESC LIMIT 1";
                case "sum" -> subquery1 = "SUM";
                case "average" -> subquery1 = "AVG";
                case "greater than" -> {
                    subquery1 = "COUNT";
                    subquery3 = " AND " + yAxis + " > " + comparator + "";
                }
                case "greater or equal" -> {
                    subquery1 = "COUNT";
                    subquery3 = " AND " + yAxis + " >= '" + comparator + "'";
                }
                case "less than" -> {
                    subquery1 = "COUNT";
                    subquery3 = " AND " + yAxis + " < '" + comparator + "'";
                }
                case "less or equal" -> {
                    subquery1 = "COUNT";
                    subquery3 = " AND " + yAxis + " <= '" + comparator + "'";
                }
                case "equals" -> {
                    subquery1 = "COUNT";
                    subquery3 = " AND " + yAxis + " = " + comparator;
                }
                default -> {
                    subquery1 = "COUNT";
                    subquery3 = " AND " + yAxis + " = '" + value + "'";
                }
            }

            if(xAxis.equals("timestamp")){
                subquery2 = " AND timestamp < " + end;
            }

            String query = "SELECT " + subquery1 + " (" + yAxis + ") FROM objects JOIN history ON objects.id = history.id JOIN (SELECT id,MAX(timestamp) AS t FROM history WHERE timestamp >= " + startDataTimestamp + subquery2 + " AND timestamp < " + endDataTimestamp + " GROUP BY id) AS i ON (i.id = objects.id AND i.t=history.timestamp) JOIN status ON status.name = history.status WHERE "+ xAxis +" >= " + start + " AND " +xAxis + " < " + end + subquery3;
            System.out.println(query);
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                if(value.equals("value"))
                    return resultSet.getFloat(yAxis.substring(yAxis.indexOf(".")+1));
                return resultSet.getFloat(subquery1.toLowerCase());
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0f;

    }

    public Float getValuesByTime(LocalDate start, LocalDate end, String yAxis, String value, long startDataTimestamp, long endDataTimestamp, String comparator) {

        long startTimestamp = ZonedDateTime.of(start.atStartOfDay(), ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endTimestamp = ZonedDateTime.of(end.atStartOfDay(), ZoneId.systemDefault()).toInstant().toEpochMilli();
        return getContinuousValues(startTimestamp,endTimestamp,yAxis,value,startDataTimestamp,endDataTimestamp,comparator,"timestamp");

    }


    public Integer insert(String name, String type){

        try {
            String query = "INSERT INTO objects(name, type) VALUES(?,?);";
            PreparedStatement statement = connection.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
            statement.setString(1,name);
            statement.setString(2,type);
            statement.execute();
            int id = -1;
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                id = generatedKeys.getInt(1);
            }
            String historyQuery = "INSERT INTO History(id, status, timestamp) VALUES(?,?,?)";
            statement = connection.prepareStatement(historyQuery);
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

        String query = "INSERT INTO history VALUES (?,?,?)";
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1,id);
            statement.setString(2,status);
            statement.setLong(3,System.currentTimeMillis());
            statement.execute();

        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }


    public void deleteObject(int id){

        try{
            String query = "DELETE FROM history WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1,id);
            statement.executeUpdate();
            query = "DELETE FROM objects WHERE id = ?";
            statement = connection.prepareStatement(query);
            statement.setInt(1,id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> selectSeriesOptions(String name) {

        List<String> series = new ArrayList<>();
        try{
            String query;                                     //TODO: only returns statuses that have already been in use
            if(name.equals("name"))                           //TODO: temporary solution might have to be more complicated with dynamic database
                query = "SELECT o."+name+" FROM objects o, history, status group by o."+name;
            else
                query = "SELECT "+name+" FROM objects, history, status group by "+name;
            PreparedStatement statement = connection.prepareStatement(query);
            System.out.println(statement);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()){
                series.add(resultSet.getString(name));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return series;
    }
}
