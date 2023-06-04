package database;

import com.main.datenpunk.Singleton;
import enteties.ColumnInfo;
import enteties.Status;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class DAO {

    private Connection connection;

    private static DAO instance = null;
    private static final Singleton singleton = Singleton.getInstance();

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

    public void createTable(String tableName,List<String> names,List<String> types,List<String> categoryNames,List<List<Status>> categoryLists){

        try {
            StringBuilder subquery = new StringBuilder();
            for (int i = 0; i < names.size(); i++) {
                String name = names.get(i);
                subquery.append(name).append(" ").append(types.get(i));
                if(name.equals("id"))
                    subquery.append(" PRIMARY KEY");

                if(i != names.size()-1)
                    subquery.append(",");
            }
            for (int i = 0; i < categoryLists.size(); i++) {
                String categoryName = categoryNames.get(i);
                createAuxTable(categoryName, categoryLists.get(i));
                if(i == 0)
                    subquery.append(",");
                subquery.append("FOREIGN KEY(").append(categoryName).append(") REFERENCES ").append(categoryName).append("(name)").append(",");
            }
            String query;
            if(tableName.equals("objects"))
                query = "CREATE TABLE objects(" + subquery + ")";
            else
                query = "CREATE TABLE history(id INT,"+subquery+"FOREIGN KEY(id) REFERENCES objects(id))";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    private void createAuxTable(String name,List<Status> categories){
        try {
            String query = "CREATE TABLE " + name + "(id SERIAL,sortorder INT,name VARCHAR(200) UNIQUE,colour char(7))";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();

            for(Status category:categories){
                String insertQuery = "INSERT INTO " + name + "(sortorder,name,colour) VALUES("+category.getSortOrder()+",'"+category.getName()+"','"+category.getColor()+"')";
                PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                insertStatement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createColumnTable(List<String> names,List<String> types, List<String> tables, List<Integer> positions){
        try{
            String query = "CREATE TABLE columns(name VARCHAR(200),type VARCHAR (100),tables VARCHAR (100),position INT)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();

            for (int i = 0; i < names.size(); i++) {
                String insertQuery = "INSERT INTO columns VALUES('"+names.get(i)+"','"+types.get(i)+"','"+tables.get(i)+"','"+positions.get(i)+"')";
                PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                insertStatement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
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
            query = "CREATE TABLE history(id INT,status VARCHAR(200),date BIGINT,PRIMARY KEY(id,date),FOREIGN KEY(id) REFERENCES objects(id), FOREIGN KEY (status) REFERENCES status(name))";
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

    public List<ColumnInfo> selectTableColumns(){
        List<ColumnInfo> columnInfo = new ArrayList<>();
        try{
            String query = "SELECT * FROM columns ORDER BY position";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()){
                String name = resultSet.getString("name");
                String table = resultSet.getString("tables");
                String type = resultSet.getString("type");
                boolean colored = type.equals("choice");
                boolean discrete = (colored || type.equals("text"));
                Integer position = resultSet.getInt("position");
                columnInfo.add(new ColumnInfo(table,name,discrete,colored,position,type));
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return columnInfo;
    }


    private void buildString(List<String> strings,ObservableList<ListView<String>> listViews,int id, String name){
        if(listViews.get(id).getItems().size() > 0) {
            ObservableList<String> stringList = listViews.get(id).getItems();
            for (int i = 0; i < stringList.size(); i++) {
                strings.set(id, strings.get(id).concat("LOWER("));
                strings.set(id, strings.get(id).concat( name));
                strings.set(id, strings.get(id).concat(")"));
                if(id%2!=0){
                    strings.set(id, strings.get(id).concat(" NOT"));
                }
                strings.set(id, strings.get(id).concat(" LIKE "));
                strings.set(id, strings.get(id).concat("'"));
                strings.set(id, strings.get(id).concat(stringList.get(i).toLowerCase()));
                strings.set(id, strings.get(id).concat("'"));
                if(i <= stringList.size()-2){
                    strings.set(id, strings.get(id).concat(" OR "));
                }
            }
        }
    }


    public List<String> selectMain(LocalDate fromDate,LocalDate toDate, ObservableList<ListView<String>> listViews,String column,String sortColumn,String sortType){

        List<String> objectTableElements = new ArrayList<>();
        if(column.equals(""))
            return objectTableElements;

        List<String> lists = new ArrayList<>();
        for (int i = 0; i < listViews.size(); i++) {
            lists.add("");
        }

        int i = 0;
        for(ColumnInfo columnInfo:singleton.getColumnInfo()){
            if(!columnInfo.name.equals("id") && !columnInfo.name.equals("date")) {
                buildString(lists, listViews, i++, columnInfo.table + "." + columnInfo.name);
                buildString(lists, listViews, i++, columnInfo.table + "." + columnInfo.name);
            }
        }

        /*
        buildString(lists,listViews,0,"objects.name");                //TODO: this will have to be dynamic in the future
        buildString(lists,listViews,1,"objects.name");
        buildString(lists,listViews,2,"objects.type");
        buildString(lists,listViews,3,"objects.type");
        buildString(lists,listViews,4,"history.status");
        buildString(lists,listViews,5,"history.status");

         */


        StringBuilder subquery = new StringBuilder();
        boolean first = true;
        for (String list : lists) {

            if (!list.equals("")) {
                if (first) {
                    subquery.append(" WHERE (");
                    first = false;
                } else {
                    subquery.append(" AND (");
                }
                subquery.append(list);
                subquery.append(")");
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
            String query =
                    "SELECT "+column+"  FROM objects " +
                    "JOIN history ON (objects.id = history.id) " +
                    "JOIN (SELECT id,max(date) AS t FROM history WHERE date >= ? AND date <= ? GROUP BY id) AS i ON (i.id = objects.id AND i.t=history.date) " +
                    "JOIN status ON status.name=history.status" + subquery + " ORDER BY "+sortColumn+ " "+sortType; //TODO: fix SQL-Injection
            statement = connection.prepareStatement(query);
            statement.setLong(1,fromTimestamp);
            statement.setLong(2,toTimestamp);
            resultSet = statement.executeQuery();

            String columnName = column.substring(column.lastIndexOf(".")+1);

            while(resultSet.next()){

                String result = resultSet.getString(columnName);

                if(columnName.equals("date"))
                    result = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(result)),ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                objectTableElements.add(result);
            }
            return objectTableElements;
        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    public String selectElement(int id, String column){
        try {
            String query = "SELECT " + column +"  FROM objects JOIN history ON (objects.id = history.id) JOIN (SELECT id,max(date) AS t FROM history GROUP BY id) AS i ON (i.id = objects.id AND i.t=history.date) JOIN status s ON s.name=history.status WHERE objects.id = ?";
            PreparedStatement statement =  connection.prepareStatement(query);
            statement.setInt(1,id);
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()){
                return resultSet.getString(column.substring(column.lastIndexOf(".")+1));
            }


        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    public List<String> selectHistory(int id,String column, String sortColumn,String sortType){

        List<String> history = new ArrayList<>();
        PreparedStatement statement;
        ResultSet resultSet;
        String query = "SELECT " + column + " FROM history WHERE id=? ORDER BY " + sortColumn + " " + sortType;
        try{
            statement = connection.prepareStatement(query);
            statement.setInt(1,id);
            resultSet = statement.executeQuery();

            String name = column.substring(column.lastIndexOf(".")+1);

            while(resultSet.next()){
                String result = resultSet.getString(name);
                if(name.equals("date"))
                    result = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(result)), TimeZone.getDefault().toZoneId()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                history.add(result);
            }
            return history;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public String selectHistoryElement(int id,String column){
        try{
            String query = "SELECT "+column+" FROM history WHERE id = "+id+" ORDER BY date DESC LIMIT 1";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                return resultSet.getString(column);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return "";
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

    public List<Status> selectStatuses(String table){
        try{
            List<Status> statuses = new ArrayList<>();
            String query = "SELECT * FROM " + table;
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

    public String getFirstOrLastValue(boolean first, String source){
        try{
            String query;
            if(first)
                query = "SELECT "+source+ " FROM history,objects ORDER BY "+source+ " ASC LIMIT 1";
            else
                query = "SELECT "+source+ " FROM history,objects ORDER BY "+source+ " DESC LIMIT 1";

            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()){
                return String.valueOf(resultSet.getLong(source.substring(source.indexOf(".")+1)));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public Float getXYValues(Number start, Number end, String category, String yAxis, String value, long startDataTimestamp, long endDataTimestamp, String comparator, String xAxis){
        try{
            String subquery1 = "",subquery2 = "", subquery3, subquery4 = "";
            switch (value) {
                case "All" -> subquery1 = "COUNT";
                case "value" -> subquery4 = " ORDER BY history.date DESC LIMIT 1";
                case "sum" -> subquery1 = "SUM";
                case "average" -> subquery1 = "AVG";
                case "greater than" -> {
                    subquery1 = "COUNT";
                    subquery4 = " AND " + yAxis + " > " + comparator + "";
                }
                case "greater or equal" -> {
                    subquery1 = "COUNT";
                    subquery4 = " AND " + yAxis + " >= '" + comparator + "'";
                }
                case "less than" -> {
                    subquery1 = "COUNT";
                    subquery4 = " AND " + yAxis + " < '" + comparator + "'";
                }
                case "less or equal" -> {
                    subquery1 = "COUNT";
                    subquery4 = " AND " + yAxis + " <= '" + comparator + "'";
                }
                case "equals" -> {
                    subquery1 = "COUNT";
                    subquery4 = " AND " + yAxis + " = " + comparator;
                }
                default -> {
                    subquery1 = "COUNT";
                    subquery4 = " AND " + yAxis + " = '" + value + "'";
                }
            }

            if(xAxis.equals("date")){
                subquery2 = " AND date < " + end;
            }

            if(category == null){
                subquery3 = xAxis +" >= " + start + " AND " +xAxis + " < " + end;
            }
            else {
                subquery3 = xAxis + " = '" + category + "'";
            }

            String query = "SELECT " + subquery1 + " (" + yAxis + ") FROM objects JOIN history ON objects.id = history.id JOIN (SELECT id,MAX(date) AS t FROM history WHERE date >= " + startDataTimestamp + subquery2 + " AND date < " + endDataTimestamp + " GROUP BY id) AS i ON (i.id = objects.id AND i.t=history.date) JOIN status ON status.name = history.status WHERE "+ subquery3 + subquery4;
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                if(value.equals("value"))
                    return resultSet.getFloat(yAxis.substring(yAxis.indexOf(".")+1));
                return resultSet.getFloat(subquery1.toLowerCase());
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return 0f;

    }

    public Float getValuesByTime(LocalDate start, LocalDate end, String yAxis, String value, long startDataTimestamp, long endDataTimestamp, String comparator) {

        long startTimestamp = ZonedDateTime.of(start.atStartOfDay(), ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endTimestamp = ZonedDateTime.of(end.atStartOfDay(), ZoneId.systemDefault()).toInstant().toEpochMilli();
        return getXYValues(startTimestamp,endTimestamp,null,yAxis,value,startDataTimestamp,endDataTimestamp,comparator,"date");

    }
    public Float getPieValues(String column ,String value,long startTimestamp,long endTimestamp, String comparator){
        try{
            String subquery1 = "COUNT(", subquery2;

            switch (value) {
                case "All" -> subquery2 = "";
                case "value", "sum", "average" -> {
                    return 0f;
                }
                case "greater than" -> subquery2 = " WHERE " + column + " > " + comparator;

                case "greater or equal" -> subquery2 = " WHERE " + column + " >= " + comparator;

                case "less than" -> subquery2 = " WHERE " + column + " < " + comparator;

                case "less or equal" -> subquery2 = " WHERE " + column + " <= " + comparator;

                case "equals" -> subquery2 = " WHERE " + column + " = " + comparator;
                default -> subquery2 = " WHERE " + column + " = '" + value + "'";

            }

            String query = "SELECT "+subquery1+"*) FROM objects JOIN history ON objects.id=history.id JOIN (SELECT id, MAX(date) t FROM history WHERE date >= "+startTimestamp+" AND date <= "+endTimestamp+" GROUP BY id) AS i ON (i.id = objects.id AND i.t=history.date) JOIN status ON status.name = history.status"+subquery2;
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                return resultSet.getFloat("count");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    public void insert(List<String> objectColumns, List<String> objectValues,List<String>historyColumns, List<String> historyValues){

        try {

            StringBuilder objectColumnsSubquery = new StringBuilder();
            StringBuilder objectValuesSubquery = new StringBuilder();
            for(int i = 0; i<objectColumns.size();i++){
                objectColumnsSubquery.append(objectColumns.get(i));
                objectValuesSubquery.append("'").append(objectValues.get(i)).append("'");
                if(i<objectColumns.size()-1){
                    objectColumnsSubquery.append(",");
                    objectValuesSubquery.append(",");
                }
            }

            String query = "INSERT INTO objects("+objectColumnsSubquery+") VALUES("+objectValuesSubquery+");";
            PreparedStatement statement = connection.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
            statement.execute();
            int id = -1;
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                id = generatedKeys.getInt(1);
            }


            StringBuilder historyColumnsSubquery = new StringBuilder();
            StringBuilder historyValuesSubquery = new StringBuilder();
            for(int i = 0; i<historyColumns.size();i++){
                historyColumnsSubquery.append(historyColumns.get(i));
                historyValuesSubquery.append("'").append(historyValues.get(i)).append("'");
                if(i<historyColumns.size()-1){
                    historyColumnsSubquery.append(",");
                    historyValuesSubquery.append(",");
                }
            }

            String historyQuery = "INSERT INTO History(id, date,"+historyColumnsSubquery+") VALUES(?,?,"+historyValuesSubquery+")";
            PreparedStatement historyStatement = connection.prepareStatement(historyQuery);
            historyStatement.setInt(1,id);
            historyStatement.setLong(2,System.currentTimeMillis());
            System.out.println(historyStatement);
            historyStatement.execute();


        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void updateValue(int id, String column, String value){
        String query = "UPDATE objects SET "+column+" = ? WHERE id = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1,value);
            statement.setInt(2,id);
            statement.executeUpdate();
        }catch (SQLException e){
            System.out.println(e.getMessage());         //TODO: better error handling
        }
    }

    public void updateHistory(int id, List<String> columns,List<String> values){

        StringBuilder columnSubquery  = new StringBuilder();
        StringBuilder valueSubquery = new StringBuilder();
        for(int i = 0;i<columns.size();i++){
            String column =columns.get(i);
            String value = values.get(i);
            columnSubquery.append(",").append(column);
            valueSubquery.append(",'").append(value).append("'");

        }

        String query = "INSERT INTO history(id,date"+columnSubquery+") VALUES ("+id+","+System.currentTimeMillis()+valueSubquery+")";
        try {
            PreparedStatement statement = connection.prepareStatement(query);
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

    public List<String> selectColumnEntries(String name) {

        List<String> series = new ArrayList<>();
        try{
            String query;                                     //TODO: only returns statuses that have already been in use
            if(name.equals("name"))                           //TODO: temporary solution might have to be more complicated with dynamic database
                query = "SELECT o."+name+" FROM objects o, history, status group by o."+name;
            else
                query = "SELECT "+name+" FROM objects, history, status group by "+name;
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            if(name.contains("."))
                name = name.substring(name.lastIndexOf(".")+1);
            while (resultSet.next()){
                series.add(resultSet.getString(name));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return series;
    }
}
