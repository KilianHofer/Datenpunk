package enteties;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TableElement {

    private final int id;
    private final StringProperty name, type;
    private final Status status;

    public TableElement(int id,String name, String type, String status, int sortorder){
        this.id = id;
        this.name = new SimpleStringProperty(name);
        this.type = new SimpleStringProperty(type);
        this.status = new Status(status,sortorder);
    }

    public final StringProperty nameProperty() {
        return name;
    }
    public final StringProperty typeProperty() {
        return type;
    }
    public final StringProperty statusProperty() {

        return new SimpleStringProperty(status.getTableName());
    }

    public  int getId(){
        return id;
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
