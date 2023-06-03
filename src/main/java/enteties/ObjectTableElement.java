package enteties;

import javafx.beans.property.*;
import java.util.ArrayList;
import java.util.List;

public class ObjectTableElement {

    private final IntegerProperty id;
    private final StringProperty name, type, timestamp;
    private final Status status;

    private final List<StringProperty> propertyList = new ArrayList<>();
    private List<String> cList = new ArrayList<>();



    public ObjectTableElement(int id, String name, String type, Status status,String timestamp){
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.type = new SimpleStringProperty(type);
        this.status = status;
        this.timestamp = new SimpleStringProperty(timestamp);

        propertyList.add(this.name);
        propertyList.add(this.type);
        propertyList.add(new SimpleStringProperty(this.status.getName()));
        propertyList.add(this.timestamp);
        cList.add("Name");
        cList.add("Type");
        cList.add("Status");
        cList.add("Date");
    }

    public final StringProperty idProperty(){
        System.out.println("id");
        return allProperty();
    }
    public final StringProperty nameProperty() {
        System.out.println("name");
        return allProperty();
    }
    public final StringProperty typeProperty() {
        System.out.println("type");
        return allProperty();
    }
    public final StringProperty statusProperty() {
        System.out.println("status");
        return allProperty();
    }
    public final StringProperty dateProperty(){
        System.out.println("date");
        return allProperty();
    }
    public final StringProperty allProperty(){      //hackiest stuff i've ever written. literal magic do not touch!
        /*
        sortColumn = singelton.getController().objectTable.getSortOrder().get(0);
        if(!sortColumn.getSortType().toString().equals(singelton.getController().sortType) || !sortColumn.equals(singelton.getController().sortColumn)) {
            singelton.getController().sortColumn = sortColumn;
            singelton.getController().sortType = sortColumn.getSortType().toString();
            singelton.sorting = true;
        }



        if(!singelton.sorting) {
            sortColumnIndex = 0;
            System.out.println(columnIndex);
            StringProperty property = propertyList.get(columnIndex);
            columnIndex++;
            if (columnIndex == propertyList.size())
                columnIndex = 0;
            return property;
        }
        else {
            String column = singelton.getController().objectTable.getSortOrder().get(0).getText();
            columnIndex = 0;
            System.out.println(sortColumnIndex++);
            if(sortColumnIndex == 76)
                singelton.sorting=false;

            return propertyList.get(cList.indexOf(column));
        }

         */
        return null;
    }


    public  int getId(){
        return id.get();
    }
    public String getName(){
        return name.get();
    }
    public String getType(){
        return type.get();
    }

    public String getStatus(){
        return status.getTableName();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public void setStatus(String status) {
        this.status.setStatus(status);
    }



}
