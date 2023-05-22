package enteties;

import java.time.LocalDate;
import java.util.List;

public class ChartDescriptor {

    public String title,xName,yName;
    public String chartType, xAxis, xMin, xMax, xType, yAxis, yMin, yMax;
    public LocalDate fromDate, toDate;
    public List<String> seriesList;
    public boolean showPoints, isRelative;
    public float stepSize;

    public ChartDescriptor(String title,String xName,String yName,String chartType, LocalDate fromDate, LocalDate toDate, List<String> seriesList,boolean showPoints, boolean isRelative,String xAxis,String xMin,String xMax,String xType,String yAxis, String yMin,String yMax,float stepSize){
        this.title = title;
        this.xName = xName;
        this.yName = yName;
        this.chartType = chartType;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.seriesList = seriesList;
        this.showPoints = showPoints;
        this.isRelative = isRelative;
        this.xAxis = xAxis;
        this.xMin = xMin;
        this.xMax = xMax;
        this.xType = xType;
        this.yAxis = yAxis;
        this.yMin = yMin;
        this.yMax = yMax;
        this.stepSize = stepSize;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setxName(String xName) {
        this.xName = xName;
    }

    public void setyName(String yName) {
        this.yName = yName;
    }

}
