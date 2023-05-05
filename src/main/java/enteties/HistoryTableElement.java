package enteties;


import database.DAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class HistoryTableElement {

    private final StringProperty timestamp;
    private final Status status;
    DAO dao = DAO.getInstance();

    public HistoryTableElement(String status, String timestamp){

        this.status = dao.selectStatus(status);
        this.timestamp = new SimpleStringProperty(timestamp);
    }

    public String getStatus(){
        return status.getTableName();
    }

    public final StringProperty statusProperty(){
        return new SimpleStringProperty(status.getName());
    }
    public final StringProperty timestampProperty(){
        return timestamp;
    }
    public String getTimestamp(){
        return timestamp.get();
    }
}
