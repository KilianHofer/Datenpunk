package enteties;

public class ColumnInfo {
    public String table, name;
    public Boolean discrete ,colored;


    public ColumnInfo(String table, String name, boolean discrete,boolean colored){
        this.table = table;
        this.name = name;
        this.discrete = discrete;
        this.colored = colored;
    }
}
