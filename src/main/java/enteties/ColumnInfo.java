package enteties;

public class ColumnInfo {
    public String table, name;
    public Boolean discrete ,colored;
    public String type;
    public Integer position, length;
    public boolean required;


    public ColumnInfo(String table, String name, boolean discrete, boolean colored, Integer position, String type, boolean required, int length){
        this.table = table;
        this.name = name;
        this.discrete = discrete;
        this.colored = colored;
        this.position = position;
        this.type = type;
        this.required = required;
        this.length = length;
    }
}
