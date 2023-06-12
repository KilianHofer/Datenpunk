package enteties;

public class Status{

    private final int sortOrder;
    private final String name;
    private final String color;


    public Status(String name, int sortOrder, String color){
        this.sortOrder = sortOrder;
        this.name = name;
        this.color = color;
    }



    public String getName(){
        return name;
    }


    public Integer getSortOrder() {
        return sortOrder;
    }

    public String getColor() {
        return color;
    }
}