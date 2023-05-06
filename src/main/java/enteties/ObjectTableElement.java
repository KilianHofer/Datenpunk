package enteties;

import javafx.beans.property.*;

public class ObjectTableElement {

    private final IntegerProperty id;
    private final StringProperty name, type, timestamp;
    private final Status status;

    public ObjectTableElement(int id, String name, String type, Status status,String timestamp){
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.type = new SimpleStringProperty(type);
        this.status = status;
        this.timestamp = new SimpleStringProperty(timestamp);
    }

    public final IntegerProperty idProperty(){
        return  id;
    }
    public final StringProperty nameProperty() {
        return name;
    }
    public final StringProperty typeProperty() {
        return type;
    }
    public final StringProperty statusProperty() {
        return new SimpleStringProperty(status.getName());
    }
    public final StringProperty timestampProperty(){
        return timestamp;
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
