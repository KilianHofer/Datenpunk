package enteties;

import database.DAO;
import javafx.scene.control.TableCell;

public class ColoredObjectTableCell extends TableCell<ObjectTableElement, String> {

    @Override
    protected  void updateItem(String item, boolean empty){
        if(empty || getTableRow() == null){
            setText(null);
            setGraphic(null);
            setStyle("-fx-background: transparent");

        }
        else{
            setText(item);
            DAO dao = DAO.getInstance();
            try {
                setStyle("-fx-background-color: " + dao.selectStatus(item).getColor());
            }catch (Exception e){
                System.out.println(item);
                System.out.println(e.getMessage());
            }
        }
    }
}

