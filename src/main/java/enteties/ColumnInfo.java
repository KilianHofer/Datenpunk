package enteties;

public class ColumnInfo {
    public String table, name;
    public Boolean discrete ,colored;
    public Integer position;


    public ColumnInfo(String table, String name, boolean discrete,boolean colored,Integer position){
        this.table = table;
        this.name = name;
        this.discrete = discrete;
        this.colored = colored;
        this.position = position;
    }
}
