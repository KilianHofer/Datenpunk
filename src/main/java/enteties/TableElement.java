package enteties;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TableElement {
    private final StringProperty name, type, status;

    public TableElement(String name, String type, String status){
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
}
