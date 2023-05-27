package enteties;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class ProjectTableElement {


    private final StringProperty name,lastVisited,createdAt,host,location;
    boolean local;

    public ProjectTableElement(String name, String lastVisited, String createdAt,String host, String location, boolean local){
        this.name = new SimpleStringProperty(name);
        this.lastVisited = new SimpleStringProperty(lastVisited.substring(0,9));
        this.createdAt = new SimpleStringProperty(createdAt.substring(0,9));
        this.host = new SimpleStringProperty(host);
        this.location = new SimpleStringProperty(location);
        this.local = local;
    }

    public StringProperty nameProperty(){
        return name;
    }
    public StringProperty lastVisitedProperty(){
        return lastVisited;
    }
    public StringProperty createdAtProperty(){
        return createdAt;
    }
    public StringProperty hostProperty(){
        return host;
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
