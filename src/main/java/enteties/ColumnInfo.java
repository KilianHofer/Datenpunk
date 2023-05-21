package enteties;

public class ColumnInfo {
    public String table, name;
    public Boolean discrete;

    public ColumnInfo(String table, String name, boolean discrete){
        this.table = table;
        this.name = name;
        this.discrete = discrete;
    }
}
