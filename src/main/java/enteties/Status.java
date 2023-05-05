package enteties;

import database.DAO;

public class Status{

    private final int sortOrder;
    private String name;
    private final String color;


    public Status(String name, int sortOrder, String color){
        this.sortOrder = sortOrder;
        this.name = name;
        this.color = color;
    }




    public String getTableName() {
        StringBuilder tmpName = new StringBuilder();


        int max = DAO.getInstance().getMaxStatusSortOrder();
        tmpName.append("\u200B".repeat(Math.max(0, max - sortOrder))); //adds zero width spaces to influence sorting order

        tmpName.append(name);
        return tmpName.toString();
    }

    public String getName(){
        return name;
    }

    public void setStatus(String name){
        this.name = name;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public String getColor() {
        return color;
    }
}