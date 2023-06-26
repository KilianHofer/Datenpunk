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
                subquery.append("\"").append(name).append("\" ").append(types.get(i));
                if(name.equals("id"))
                    subquery.append(" PRIMARY KEY");

                if(i != names.size()-1)
                    subquery.append(",");
            }

            for (int i = 0; i < categoryLists.size(); i++) {
                String categoryName = categoryNames.get(i);
                createAuxTable(categoryName, categoryLists.get(i));
                /*
                if(i == 0)
                    subquery.append(",");
                //subquery.append("FOREIGN KEY(\"").append(categoryName).append("\") REFERENCES \"").append(categoryName).append("\"(name)");
                if(i < categoryLists.size()-1)
                    subquery.append(",");

                 */
            }


            String query;
            if(tableName.equals("objects"))
                query = "CREATE TABLE objects(" + subquery + ")";
            else
                query = "CREATE TABLE history(id INT,"+subquery+",FOREIGN KEY(id) REFERENCES objects(id))";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    private void createAuxTable(String name,List<Status> categories){
        try {
            String query = "CREATE TABLE \"" + name + "\" (id SERIAL,sortorder INT,name VARCHAR(200) UNIQUE,colour char(7))";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();

            for(Status category:categories){
                String insertQuery = "INSERT INTO \"" + name + "\" (sortorder,name,colour) VALUES("+category.getSortOrder()+",'"+category.getName()+"','"+category.getColor()+"')";
                PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                insertStatement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createColumnTable(List<String> names, List<String> types, List<String> tables, List<Integer> positions, List<Boolean> required, List<Integer> lengths){
        try{
            String query = "CREATE TABLE columns(name VARCHAR(200),type VARCHAR (100),tables VARCHAR (100),position INT,required BOOLEAN,length INT)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();

            for (int i = 0; i < names.size(); i++) {
                String insertQuery = "INSERT INTO columns VALUES('"+names.get(i)+"','"+types.get(i)+"','"+tables.get(i)+"','"+positions.get(i)+"',"+required.get(i)+","+lengths.get(i)+")";
                PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                insertStatement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
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
                boolean colored = type.equals("Choice");
                boolean discrete = (colored || type.equals("Text"));
                Integer position = resultSet.getInt("position");
                boolean required = resultSet.getBoolean("required");
                int length = resultSet.getInt("length");
                columnInfo.add(new ColumnInfo(table,name,discrete,colored,position,type,required,length));
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return columnInfo;
    }


    private void buildString(List<String> strings,ObservableList<ListView<String>> listViews,int id, String name,String type){
        boolean number = !type.equals("Choice") && !type.equals("Text");
        if(listViews.get(id).getItems().size() > 0) {
            ObservableList<String> stringList = listViews.get(id).getItems();
            for (int i = 0; i < stringList.size(); i++) {


                if(number) {
                    String operator;
                    String value;
                    String line = stringList.get(i);
                    if (line.charAt(1) == '=') {
                        operator = line.substring(0, 2);
                        value = stringList.get(i).substring(2);
                    }
                    else {
                        operator = line.substring(0, 1);
                        value = line.substring(1);
                    }

                    String combinator = " AND ";

                    strings.set(id, strings.get(id).concat( name));
                    if(id%2!=0) {
                        combinator = " OR ";
                        switch (operator){
                            case "<" -> operator = ">=";
                            case "<=" -> operator = ">";
                            case "=" -> operator = "!=";
                            case ">=" -> operator = "<";
                            case ">" -> operator = "<=";
                        }
                    }
                    strings.set(id, strings.get(id).concat(operator));
                    strings.set(id,strings.get(id).concat(value));
                    if(i <= stringList.size()-2){
                        strings.set(id, strings.get(id).concat(combinator));
                    }
                }
                else {
                    String combinator = " OR ";
                    strings.set(id, strings.get(id).concat("LOWER("));
                    strings.set(id, strings.get(id).concat( name));
                    strings.set(id, strings.get(id).concat(")"));
                    if(id%2!=0) {
                        strings.set(id, strings.get(id).concat(" NOT"));
                        combinator = " AND ";
                    }
                    strings.set(id, strings.get(id).concat(" LIKE "));
                    strings.set(id, strings.get(id).concat("'"));
                    strings.set(id, strings.get(id).concat(stringList.get(i).toLowerCase()));
                    strings.set(id, strings.get(id).concat("'"));
                    if(i <= stringList.size()-2){
                        strings.set(id, strings.get(id).concat(combinator));
                    }
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
            if(!columnInfo.name.equals("id") && !columnInfo.name.equals("Date")) {
                buildString(lists, listViews, i++, columnInfo.table + ".\"" + columnInfo.name+"\"",columnInfo.type);
                buildString(lists, listViews, i++, columnInfo.table + ".\"" + columnInfo.name+"\"",columnInfo.type);
            }
        }

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

        String choiceTable = "";
        for (ColumnInfo status:singleton.getColumnInfo()){
            if(status.name.equals(sortColumn))
                choiceTable = status.table;
        }
        boolean sortChoice = false;
        String sortTable = sortColumn;
        for (ColumnInfo columnInfo : singleton.getColumnInfo()) {
            if (columnInfo.name.equals(column))
                column = columnInfo.table + ".\"" + columnInfo.name+"\"";
            if (columnInfo.name.equals(sortColumn)) {
                if(!columnInfo.colored)
                    sortColumn = columnInfo.table + ".\"" + sortColumn + "\"";
                else {
                    sortColumn = "\"" + sortColumn + "\".sortorder";
                    sortChoice = true;
                }
            }
        }

        String sortSubquery = "";
        if(sortChoice)
            sortSubquery = "LEFT JOIN \""+sortTable+"\" ON \""+sortTable+"\".name = "+choiceTable+".\""+sortTable+"\"";

        PreparedStatement statement;
        ResultSet resultSet;
        try {
            String query =
                    "SELECT "+column+"  FROM objects " +
                    "JOIN history ON (objects.\"id\" = history.\"id\") " +
                    "JOIN (SELECT \"id\",max(\"Date\") AS t FROM history WHERE \"Date\" >= ? AND \"Date\" <= ? GROUP BY \"id\") AS i ON (i.\"id\" = objects.\"id\" AND i.t=history.\"Date\") " + sortSubquery + subquery + " ORDER BY "+sortColumn+ " "+sortType; //TODO: fix SQL-Injection
            statement = connection.prepareStatement(query);
            statement.setLong(1,fromTimestamp);
            statement.setLong(2,toTimestamp);
            System.out.println(statement);
            resultSet = statement.executeQuery();

            String columnName = column.substring(column.lastIndexOf(".")+2,column.length()-1);

            while(resultSet.next()){

                String result = resultSet.getString(columnName);

                if(columnName.equals("Date"))
                    result = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(result)),ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                if(result == null)
                    result = "";
                objectTableElements.add(result);
            }
            return objectTableElements;
        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    public String selectElement(int id, String column){


        String statusTable = "";
        for (ColumnInfo status:singleton.getColumnInfo()){
            if(status.name.equals("Status"))
                statusTable = status.table;
            if (status.name.equals(column))
                column = status.table + ".\"" + column+"\"";
        }

        try {
            String query = "SELECT " + column +"  FROM objects " +
                    "JOIN history ON (objects.\"id\" = history.\"id\") " +
                    "JOIN (SELECT \"id\",max(\"Date\") AS t FROM history GROUP BY \"id\") AS i ON (i.\"id\" = objects.\"id\" AND i.t=history.\"Date\") " +
                    "JOIN \"Status\" s ON s.\"name\"="+statusTable+".\"Status\" WHERE objects.\"id\" = ?";
            PreparedStatement statement =  connection.prepareStatement(query);
            statement.setInt(1,id);
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()){
                String result = resultSet.getString(column.substring(column.lastIndexOf(".")+2,column.length()-1));
                if(result == null)
                    result = "";
                return result;
            }


        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    public List<String> selectHistory(int id,String column, String sortColumn,String sortType){

        String type = "";
        for (ColumnInfo columnInfo : singleton.getColumnInfo()) {
            if (columnInfo.name.equals(column)) {
                column = columnInfo.table + ".\"" + columnInfo.name + "\"";
                type = columnInfo.type;
            }
            if (columnInfo.name.equals(sortColumn)) {
                if(!columnInfo.colored)
                    sortColumn = columnInfo.table + ".\"" + sortColumn + "\"";
                else
                    sortColumn = "\""+sortColumn+"\".sortorder";
            }
        }

        List<String> history = new ArrayList<>();
        PreparedStatement statement;
        ResultSet resultSet;
        try{
            String query = "SELECT " + column + " FROM history WHERE \"id\"=? ORDER BY " + sortColumn + " " + sortType;
            statement = connection.prepareStatement(query);
            statement.setInt(1,id);
            resultSet = statement.executeQuery();

            String name = column.substring(column.lastIndexOf(".")+2,column.length()-1);


            while(resultSet.next()){
                String result;
                if(type.equals("Integer")) {
                    Integer intResult;
                    intResult = resultSet.getObject(name,Integer.class);
                    result = String.valueOf(intResult);
                }
                else  if(type.equals("Decimal")) {
                    Float floatResult;
                    Object resultObject = resultSet.getObject(name);
                    if(resultObject == null)
                        result = null;
                    else {
                        floatResult = resultSet.getObject(name, Double.class).floatValue();
                        result = String.valueOf(floatResult);
                    }
                }
                else
                     result = resultSet.getString(name);
                if(name.equals("Date"))
                    result = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(result)), TimeZone.getDefault().toZoneId()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                if(result == null || result.equals("null"))
                    result="";
                history.add(result);
            }
            return history;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String selectHistoryElement(int id,String column){
        try{
            String query = "SELECT \""+column+"\" FROM history WHERE \"id\" = "+id+" ORDER BY \"Date\" DESC LIMIT 1";
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
            String query = "SELECT * FROM \"" + table+"\" ORDER BY sortorder";
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


    public String getFirstOrLastValue(boolean first, String source,String type){
        try{

            
            
            String query;
            if(first)
                query = "SELECT "+source+ " FROM history,objects ORDER BY "+source+ " ASC LIMIT 1";
            else
                query = "SELECT "+source+ " FROM history,objects ORDER BY "+source+ " DESC LIMIT 1";

            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()){
                String substring = source.substring(source.indexOf(".") + 2, source.length() - 1);
                if(type.equals("Integer") || type.equals("DATE"))
                    return String.valueOf(resultSet.getLong(substring));
                else {
                    return String.valueOf(resultSet.getFloat(substring));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public Float getXYValues(Number start, Number end, String category, String yAxis, String value, long startDataTimestamp, long endDataTimestamp, String comparator, String xAxis){
        try{
            String subquery1 = "",subquery2 = "",subquery3 = "", subquery4, subquery5 = "";
            switch (value) {
                case "All" -> {
                    subquery1 = "COUNT";
                    subquery3 = "(" + yAxis + " != null OR " + yAxis + " != '') AND";
                }
                case "value" -> subquery5 = " ORDER BY history.\"Date\" DESC LIMIT 1";
                case "sum" -> subquery1 = "SUM";
                case "average" -> subquery1 = "AVG";
                case "greater than" -> {
                    subquery1 = "COUNT";
                    subquery5 = " AND " + yAxis + " > " + comparator + "";
                }
                case "greater or equal" -> {
                    subquery1 = "COUNT";
                    subquery5 = " AND " + yAxis + " >= '" + comparator + "'";
                }
                case "less than" -> {
                    subquery1 = "COUNT";
                    subquery5 = " AND " + yAxis + " < '" + comparator + "'";
                }
                case "less or equal" -> {
                    subquery1 = "COUNT";
                    subquery5 = " AND " + yAxis + " <= '" + comparator + "'";
                }
                case "equals" -> {
                    subquery1 = "COUNT";
                    subquery5 = " AND " + yAxis + " = " + comparator;
                }
                default -> {
                    subquery1 = "COUNT";
                    subquery3 = "(" + yAxis + " != null OR " + yAxis + " != '') AND";
                    subquery5 = " AND " + yAxis + " = '" + value + "'";
                }
            }

            if(xAxis.equals("Date")){
                subquery2 = " AND \"Date\" < " + end;
            }

            if(category == null){
                subquery4 = xAxis +" >= " + start + " AND " +xAxis + " < " + end;
            }
            else {
                subquery4 = xAxis + " = '" + category + "'";
            }

            String statusTable = "";
            for (ColumnInfo status:singleton.getColumnInfo()){
                if(status.name.equals("Status"))
                    statusTable = status.table;
            }

            String query = "SELECT " + subquery1 + " (" + yAxis + ") AS result FROM objects " +
                    "JOIN history ON objects.id = history.id " +
                    "JOIN (SELECT id,MAX(\"Date\") AS t FROM history WHERE \"Date\" >= " + startDataTimestamp + subquery2 + " AND \"Date\" < " + endDataTimestamp + " GROUP BY id) AS i ON (i.id = objects.id AND i.t=history.\"Date\") " +
                    "JOIN \"Status\" ON \"Status\".name = "+statusTable+".\"Status\" " +
                    "WHERE " + subquery3 + " ("+ subquery4 + subquery5+")";
            PreparedStatement statement = connection.prepareStatement(query);
            System.out.println(statement);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                if(value.equals("value"))
                    return resultSet.getFloat(yAxis.substring(yAxis.indexOf(".")+2,yAxis.length()-1));
                return resultSet.getFloat("result");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return 0f;

    }

    public Float getValuesByTime(LocalDate start, LocalDate end, String yAxis, String value, long startDataTimestamp, long endDataTimestamp, String comparator) {

        long startTimestamp = ZonedDateTime.of(start.atStartOfDay(), ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endTimestamp = ZonedDateTime.of(end.atStartOfDay(), ZoneId.systemDefault()).toInstant().toEpochMilli();
        return getXYValues(startTimestamp,endTimestamp,null,yAxis,value,startDataTimestamp,endDataTimestamp,comparator,"history.\"Date\"");

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

            String statusTable = "";
            for (ColumnInfo status:singleton.getColumnInfo()){
                if(status.name.equals("Status"))
                    statusTable = status.table;
            }

            String query = "SELECT "+subquery1+"*) FROM objects " +
                    "JOIN history ON objects.id=history.id " +
                    "JOIN (SELECT id, MAX(\"Date\") t FROM history WHERE \"Date\" >= "+startTimestamp+" AND \"Date\" <= "+endTimestamp+" GROUP BY id) AS i ON (i.id = objects.id AND i.t=history.\"Date\") " +
                    "JOIN \"Status\" ON \"Status\".name = "+statusTable+".\"Status\""+subquery2;
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
                objectColumnsSubquery.append("\"").append(objectColumns.get(i)).append("\"");
                String value = objectValues.get(i);
                if(value != null)
                    objectValuesSubquery.append("'").append(objectValues.get(i)).append("'");
                else
                    objectValuesSubquery.append("null");
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
                historyColumnsSubquery.append("\"").append(historyColumns.get(i)).append("\"");
                String value = historyValues.get(i);
                if(value != null)
                    historyValuesSubquery.append("'").append(historyValues.get(i)).append("'");
                else
                    historyValuesSubquery.append("null");
                if(i<historyColumns.size()-1){
                    historyColumnsSubquery.append(",");
                    historyValuesSubquery.append(",");
                }
            }

            String historyQuery = "INSERT INTO history(id, \"Date\","+historyColumnsSubquery+") VALUES(?,?,"+historyValuesSubquery+")";
            PreparedStatement historyStatement = connection.prepareStatement(historyQuery);
            historyStatement.setInt(1,id);
            historyStatement.setLong(2,System.currentTimeMillis());
            historyStatement.execute();


        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void updateValue(int id, String column, String value, String type){

        try {
            String subquery;
            if(type.equals("Integer") || type.equals("Decimal")) {
                if(!value.equals(""))
                    subquery = value;
                else
                    subquery = "null";
            }
            else
                subquery = "'"+value+"'";
            String query = "UPDATE objects SET \""+column+"\" = "+subquery+" WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1,id);
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
            columnSubquery.append(",").append("\"").append(column).append("\"");
            valueSubquery.append(",'").append(value).append("'");

        }

        String query = "INSERT INTO history(id,\"Date\""+columnSubquery+") VALUES ("+id+","+System.currentTimeMillis()+valueSubquery+")";
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
            if(name.equals("Name")) {
                String nameTable = "";
                for(ColumnInfo columnInfo:singleton.getColumnInfo()){
                    if(columnInfo.name.equals("Name"))
                        nameTable = columnInfo.table;
                }
                query = "SELECT \""+nameTable+"\".\"" + name + "\" FROM objects, history, \"Status\" group by \""+nameTable+"\".\"" + name + "\"";
            }
            else
                query = "SELECT "+name+" FROM objects, history, \"Status\" group by "+name+"";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            name = name.substring(1);
            if(name.contains("."))
                name = name.substring(name.lastIndexOf(".")+2);
            name = name.substring(0,name.length()-1);
            while (resultSet.next()) {
                String result = resultSet.getString(name);
                if (result!= null && !result.equals(""))
                    series.add(result);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return series;
    }

    public void changeColumnPosition(String name, String newPos) {
        try{
            String query = "UPDATE columns SET position = ? WHERE name = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1,Integer.parseInt(newPos));
            statement.setString(2,name);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void changeColumnHistory(String name, String newValue,String type, String length) {
        try{
            String datatype = "";
            switch (type){
                case "Text" -> datatype = "VARCHAR("+Integer.parseInt(length)+")";
                case "Integer" -> datatype = "INTEGER";
                case "Decimal" -> datatype = "FLOAT";
                case "Choice" -> datatype = "VARCHAR(200)";
            }

            String lastTimestampsQuery = "SELECT id,MAX(\"Date\") FROM history GROUP BY id";
            PreparedStatement lastTimestampStatement = connection.prepareStatement(lastTimestampsQuery);
            ResultSet lastTimestampResultSet = lastTimestampStatement.executeQuery();


            String table;
            if(newValue.equals("true")) {
                table = "history";
                String alterHistoryQuery = "ALTER TABLE history ADD \""+name+"\" "+datatype;
                PreparedStatement alterHistoryStatement = connection.prepareStatement(alterHistoryQuery);
                alterHistoryStatement.executeUpdate();

                while(lastTimestampResultSet.next()){
                    int id = lastTimestampResultSet.getInt("id");
                    long timestamp = lastTimestampResultSet.getLong("max");
                    String transferToObjectsQuery = "UPDATE history SET \""+name+"\"=(SELECT \""+name+"\" FROM objects WHERE id="+id+") WHERE id="+id+" AND \"Date\"="+timestamp;
                    PreparedStatement transferToObjectsStatement = connection.prepareStatement(transferToObjectsQuery);
                    transferToObjectsStatement.executeUpdate();
                }

                String alterObjectsQuery = "ALTER TABLE objects DROP COLUMN \""+name+"\"";
                PreparedStatement alterObjectsStatement = connection.prepareStatement(alterObjectsQuery);
                alterObjectsStatement.executeUpdate();
            }
            else {
                table = "objects";
                String alterObjectsQuery = "ALTER TABLE objects ADD \""+name+"\" "+datatype;
                PreparedStatement alterObjectsStatement = connection.prepareStatement(alterObjectsQuery);
                alterObjectsStatement.executeUpdate();

                while (lastTimestampResultSet.next()){
                    int id = lastTimestampResultSet.getInt("id");
                    long timestamp = lastTimestampResultSet.getLong("max");
                    String transferToHistoryQuery = "UPDATE objects SET \""+name+"\"=(SELECT \""+name+"\" FROM history WHERE id="+id+" AND \"Date\" = "+timestamp+") WHERE id="+id;
                    PreparedStatement transferToHistoryStatement = connection.prepareStatement(transferToHistoryQuery);
                    transferToHistoryStatement.executeUpdate();
                }

                String altertHistoryQuery = "ALTER TABLE history DROP COLUMN \""+name+"\"";
                PreparedStatement alterHistoryStatement = connection.prepareStatement(altertHistoryQuery);
                alterHistoryStatement.executeUpdate();
            }
            String columnsQuery = "UPDATE columns SET tables = ? WHERE name = ?";
            PreparedStatement columnsStatement = connection.prepareStatement(columnsQuery);
            columnsStatement.setString(1,table);
            columnsStatement.setString(2,name);
            columnsStatement.executeUpdate();


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void changeColumnRequired(String name, String newValue) {
        try {
            boolean value;
            value = newValue.equals("true");

            String query = "UPDATE columns SET required = ? WHERE name = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setBoolean(1, value);
            statement.setString(2, name);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean changeColumnLength(String table,String name, String newValue) {
        try{
            if(table.equals("true"))
                table = "history";
            else
                table = "objects";

            String query = "ALTER TABLE "+table+" ALTER COLUMN \""+name+"\" TYPE VARCHAR("+newValue+")";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();

            String columnQuery = "UPDATE columns SET length="+Integer.parseInt(newValue)+" WHERE name='"+name+"'";
            PreparedStatement columnStatement= connection.prepareStatement(columnQuery);
            columnStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public void changeChoicePosition(String table, String name, String newValue) {
        try{
            name = name.substring(0,name.lastIndexOf("("));
            String query = "UPDATE \""+table+"\" SET sortorder = ? WHERE name = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1,Integer.parseInt(newValue));
            statement.setString(2,name);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertChoice(String table, String newValue,String position) {
        try{
            String name = newValue.substring(0,newValue.lastIndexOf("("));
            String colour = newValue.substring(newValue.lastIndexOf("(")+1,newValue.lastIndexOf(")"));
            String query = "INSERT INTO \""+table+"\"(sortorder,name,colour) VALUES (?,?,?)";
            PreparedStatement statement  = connection.prepareStatement(query);
            statement.setInt(1, Integer.parseInt(position));
            statement.setString(2,name);
            statement.setString(3,colour);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteChoice(String table, String oldValue) {
        try{
            String name = oldValue.substring(0,oldValue.lastIndexOf("("));
            String query = "DELETE FROM \""+table+"\" WHERE name = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1,name);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void changeChoice(String table, String oldValue, String newValue) {
        try{

            String mainTable = "";
            for(ColumnInfo info:singleton.getColumnInfo()){
                if(info.name.equals(table)){
                    mainTable = info.table;
                    break;
                }
            }



            String oldName = oldValue.substring(0,oldValue.lastIndexOf("("));
            String newName = newValue.substring(0,newValue.lastIndexOf("("));
            String newColour = newValue.substring(newValue.lastIndexOf("(")+1,newValue.lastIndexOf(")"));

            String tableUpdateQuery = "UPDATE "+mainTable+" SET \""+table+"\" = '"+newName+"' WHERE \""+table+"\" = '"+oldName+"'";
            PreparedStatement tableUpdateStatement = connection.prepareStatement(tableUpdateQuery);
            tableUpdateStatement.executeUpdate();

            String query = "UPDATE \""+table+"\" SET name = ?,colour = ? WHERE name = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1,newName);
            statement.setString(2,newColour);
            statement.setString(3,oldName);
            statement.executeUpdate();



        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void alterColumnAdd(String table, String name,String type,String position,String required, String length,List<String> list) {
        try{
            boolean auxTable = false;
            String datatype = "";
            switch (type){
                case "Text" -> datatype = "VARCHAR("+length+")";
                case "Integer" -> datatype = "INTEGER";
                case "Decimal" -> datatype = "FLOAT";
                case "Choice" -> {
                    datatype = "VARCHAR(200)";
                    auxTable = true;}
            }
            if(auxTable) {
                List<Status> choiceList = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    String item = list.get(i);
                    String itemName = item.substring(0, item.lastIndexOf("("));
                    String colour = item.substring(item.lastIndexOf("(") + 1, item.lastIndexOf(")"));
                    choiceList.add(new Status(itemName, i, colour));
                }
                createAuxTable(name, choiceList);
            }

            if(table.equals("true"))
                table = "history";
            else
                table = "objects";

            String query = "ALTER TABLE \""+table+"\" ADD COLUMN \""+name+"\" "+datatype;
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();

            String columnQuery = "INSERT INTO columns VALUES(?,?,?,?,?,?)";
            PreparedStatement columnStatement = connection.prepareStatement(columnQuery);
            columnStatement.setString(1,name);
            columnStatement.setString(2,type);
            columnStatement.setString(3,table);
            columnStatement.setInt(4, Integer.parseInt(position));
            columnStatement.setBoolean(5,Boolean.getBoolean(required));
            columnStatement.setInt(6,Integer.parseInt(length));
            columnStatement.executeUpdate();


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void alterColumnDelete(String table, String oldValue) {
        try{

            if(table.equals("true"))
                table = "history";
            else
                table = "objects";

            String dropQuery = "DROP TABLE IF EXISTS \""+oldValue+"\"";
            PreparedStatement dropStatement = connection.prepareStatement(dropQuery);
            dropStatement.executeUpdate();

            String query = "ALTER TABLE \""+table+"\" DROP COLUMN \""+oldValue+"\"";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();

            String columnQuery = "DELETE FROM columns WHERE name=?";
            PreparedStatement columnStatement = connection.prepareStatement(columnQuery);
            columnStatement.setString(1,oldValue);
            columnStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void alterColumnChange(String table, String oldValue, String newValue) {
        try{
            if(table.equals("true"))
                table = "history";
            else
                table = "objects";

            boolean isChoice = false;
            for(ColumnInfo choice:singleton.getColumnInfo()){
                if(choice.name.equals(oldValue)){
                    isChoice = true;
                    break;
                }
            }

            String query = "ALTER TABLE \""+table+"\" RENAME COLUMN \""+oldValue+"\" TO \""+newValue+"\"";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();

            String columnQuery = "UPDATE columns SET name='"+newValue+"' WHERE name='"+oldValue+"'";
            PreparedStatement columnStatement = connection.prepareStatement(columnQuery);
            columnStatement.executeUpdate();

            if(isChoice) {
                String tableQuery = "ALTER TABLE \"" + oldValue + "\" RENAME TO \"" + newValue+"\"";
                PreparedStatement tableStatement = connection.prepareStatement(tableQuery);
                tableStatement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean changeColumnType(String table, String name,String oldType,String newType,String length,List<String> list) {

        if(table.equals("true"))
            table = "history";
        else
            table = "objects";

        try {
            if(oldType.equals("Choice")){
                String auxQuery = "DROP TABLE \""+name+"\"";
                PreparedStatement auxStatement = connection.prepareStatement(auxQuery);
                auxStatement.executeUpdate();
            }
            if(newType.equals("Choice")){
                List<Status> choicesList = new ArrayList<>();
                List<String> choiceNames = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    String item = list.get(i);
                    String choiceName = item.substring(0,item.lastIndexOf("("));
                    String choiceColour = item.substring(item.lastIndexOf("(")+1,item.lastIndexOf(")"));
                    choicesList.add(new Status(choiceName,i,choiceColour));
                    choiceNames.add(choiceName);
                }
                createAuxTable(name,choicesList);
                deleteInvalidChoices(choiceNames,table,name);
            }
            else if(!newType.equals("Text"))
                deleteNonNumerics(newType,table,name);
            String datatype = "";
            switch (newType){
                case "Text" -> datatype = "VARCHAR("+length+")";
                case "Integer" -> datatype = "INTEGER";
                case "Decimal" -> datatype = "FLOAT";
                case "Choice" -> datatype = "VARCHAR(200)";
            }

            String query ="ALTER TABLE \""+table+"\" ALTER COLUMN \""+name+"\" TYPE "+datatype+" USING (\""+name+"\"::"+datatype+")";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();

            String columnQuery = "UPDATE columns SET type='"+newType+"' WHERE name='"+name+"'";
            PreparedStatement columnStatement = connection.prepareStatement(columnQuery);
            columnStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private void deleteInvalidChoices(List<String> choiceNames,String table,String name) {
        try{
            StringBuilder subquery = new StringBuilder();
            for(int i = 0;i<choiceNames.size();i++){
                subquery.append("\"").append(name).append("\" != '").append(choiceNames.get(i)).append("'");
                if(i<choiceNames.size()-1)
                    subquery.append(" OR ");

            }
            String query = "UPDATE \"" + table+ "\" SET \""+name+"\"=null WHERE "+subquery;
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteNonNumerics(String type,String table,String name){
        String regex;
        if(type.equals("Integer"))
            regex = "'^-?[0-9]*$'";
        else
            regex = "'^\\d+(\\.\\d+)?$'";

        try {
            String query = "UPDATE \"" + table + "\" SET "+name+"=null WHERE \"" + name + "\" !~ " + regex;
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }
}
