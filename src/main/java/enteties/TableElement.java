package enteties;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TableElement {

    private final int id;
    private final StringProperty name, type, status;

    public TableElement(int id,String name, String type, String status){
        this.id = id;
        this.name = new SimpleStringProperty(name);
        this.type = new SimpleStringProperty(type);
        this.status = new SimpleStringProperty(status);
    }

    public final StringProperty nameProperty() {
        return name;
    }
    public final StringProperty typeProperty() {
        return type;
    }
    public final StringProperty statusProperty() {
        return status;
    }

    public  int getId(){
        return id;
    }
    public String getName(){
        return nameProperty().get();
    }
    public String getType(){
        return typeProperty().get();
    }
    public String getStatus(){
        return statusProperty().get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public void setStatus(String status) {
        this.status.set(status);
    }



}
