package enteties;

import java.util.Date;

public class HistoryElement {

    long timestamp;
    String status;

    public HistoryElement(String status){

        this.status = status;
        Date date = new Date();
        timestamp = date.getTime();

    }

}
