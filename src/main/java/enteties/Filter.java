package enteties;

import java.util.List;

public class Filter {

    public String start;
    public String end;
    public List<String> order;
    public List<Boolean> visible;
    public List<Double> widths;
    public List<List<String>> whitelist;
    public List<List<String>> blacklist;

    public Filter(String start, String end, List<String> order, List<Boolean> visible, List<Double> widths, List<List<String>> whitelist, List<List<String>> blacklist){
        this.start = start;
        this.end = end;
        this.order = order;
        this.visible = visible;
        this.widths = widths;
        this.whitelist = whitelist;
        this.blacklist = blacklist;
    }


}
