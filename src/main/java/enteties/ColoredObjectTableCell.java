package enteties;

import database.DAO;
import javafx.scene.control.TableCell;

public class ColoredObjectTableCell extends TableCell<ObjectTableElement, String> {

    @Override
    protected  void updateItem(String item, boolean empty){
        if(empty || getTableRow() == null){
            setText(null);
            setGraphic(null);
        }
        else{
            setText(item);
            DAO dao = DAO.getInstance();

            setStyle("-fx-background-color: " + dao.selectStatus(item).getColor());
        }
    }
}

