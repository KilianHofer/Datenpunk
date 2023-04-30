package enteties;


import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class HistoryElement {

    StringProperty timestamp;
    StringProperty status;

    public HistoryElement(String status, String timestamp){

        this.status = new SimpleStringProperty(status);
        this.timestamp = new SimpleStringProperty(timestamp);
    }

    public final StringProperty statusProperty(){
        return status;
    }
    public final StringProperty timestampProperty(){
        return timestamp;
    }
    public String getStatus(){
        return statusProperty().get();
    }
    public String getTimestamp(){
        return timestampProperty().get();
    }


}
