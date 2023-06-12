package enteties;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class ProjectTableElement {


    private final StringProperty name,lastVisited,createdAt,location;
    boolean local;

    public ProjectTableElement(String name, String lastVisited, String createdAt, String location, boolean local){
        this.name = new SimpleStringProperty(name);
        this.lastVisited = new SimpleStringProperty(lastVisited);
        this.createdAt = new SimpleStringProperty(createdAt);
        this.location = new SimpleStringProperty(location);
        this.local = local;
    }

    public StringProperty nameProperty(){       //these property functions are used by the JavaFX table, they are not unused!
        return name;
    }
    public StringProperty lastVisitedProperty(){
        return lastVisited;
    }
    public StringProperty createdAtProperty(){
        return createdAt;
    }
    public StringProperty locationProperty(){
        return location;
    }

    public boolean isLocal() {
        return local;
    }

    public String getName() {
        return name.get();
    }
    public String getLocation() {
        return  location.get();
    }
}
