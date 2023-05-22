package com.main.datenpunk;

import database.DAO;
import enteties.ChartDescriptor;
import enteties.ColumnInfo;
import javafx.scene.Node;
import javafx.scene.chart.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class Singelton {

    DAO dao = DAO.getInstance();
    MainController controller;

    private String currentProject;
    private final String workingDirectory = System.getProperty("user.home") + "\\Datenpunk";
    private static Singelton instance = null;

    private List<ColumnInfo> columnInfo;

    public void setController(MainController controller){
        this.controller = controller;
    }
    public MainController getController(){
        return controller;
    }

    public void setColumnInfo(){
        columnInfo = new ArrayList<>();
        columnInfo.add(new ColumnInfo("objects","id", false));
        columnInfo.addAll(dao.selectTableColumns("objects"));
        columnInfo.addAll(dao.selectTableColumns("history"));
    }

    public List<ColumnInfo> getColumnInfo(){
        return columnInfo;
    }


    private Singelton() {
    }

    public static Singelton getInstance() {
        if (instance == null)
            instance = new Singelton();
        return instance;
    }

    public void setCurrentProject(String name) {
        currentProject = name;
    }

    public String getCurrentProject() {
        return currentProject;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    private Chart setChartByType(String type) {

        Chart chart;
        switch (type) {
            case "Line Chart" -> chart = new LineChart<>(new CategoryAxis(), new NumberAxis());
            case "Area Chart" -> chart = new AreaChart<>(new CategoryAxis(), new NumberAxis());
            case "Stacked Area Chart" -> chart = new StackedAreaChart<>(new CategoryAxis(), new NumberAxis());
            case "Bar Chart" -> chart = new BarChart<>(new CategoryAxis(), new NumberAxis());
            case "Stacked Bar Chart" -> chart = new StackedBarChart<>(new CategoryAxis(), new NumberAxis());
            default -> chart = new PieChart();

        }
        return chart;
    }

    public Chart generateChart(ChartDescriptor c) {

        Chart chart = setChartByType(c.chartType);
        chart.setTitle(c.title);

        long startDataTimestamp, endDataTimestamp;

        if (c.fromDate != null) {
            startDataTimestamp = ZonedDateTime.of(c.fromDate.atStartOfDay(), ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            startDataTimestamp = 0;
            c.fromDate = Instant.ofEpochMilli(0).atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (c.toDate != null) {
            c.toDate = c.toDate.plusDays(1);
            endDataTimestamp = ZonedDateTime.of(c.toDate.atStartOfDay(), ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            endDataTimestamp = Long.MAX_VALUE;
        }

        if (!chart.getClass().equals(PieChart.class)) {
            ((XYChart) chart).getData().clear();
            ((XYChart)chart).getXAxis().setLabel(c.xName);
            ((XYChart)chart).getYAxis().setLabel(c.yName);

            if(chart.getClass().equals(LineChart.class))
                ((LineChart)chart).setCreateSymbols(c.showPoints);
            else if(chart.getClass().equals(AreaChart.class))
                ((AreaChart)chart).setCreateSymbols(c.showPoints);
            else if(chart.getClass().equals(StackedAreaChart.class))
            ((StackedAreaChart)chart).setCreateSymbols(c.showPoints);

            for (String series : c.seriesList) {

                String value = series.substring(series.indexOf(":") + 2, series.lastIndexOf(" "));

                Boolean discrete = null;
                String seriesX = c.xAxis,seriesY = c.yAxis;
                for (ColumnInfo column : columnInfo) {

                    if (column.name.equals(c.xAxis)) {
                        seriesX = column.table + "." + c.xAxis;
                        discrete = column.discrete;
                    }
                    if (column.name.equals(c.yAxis))
                        seriesY = column.table + "." + c.yAxis;
                }
                String comparator = series.substring(series.lastIndexOf(" ") + 1, series.lastIndexOf("("));
                if (discrete != null) {
                    XYChart.Series<String, Float> plot = new XYChart.Series<>();
                    plot.setName(series.substring(0, series.lastIndexOf(":")));
                    if (discrete) {
                        for (String category : dao.selectColumnEntries(seriesX)) {
                            float total = 1f;
                            if (c.isRelative) {
                                total = dao.getXYValues(null, null, category, seriesY, "All", startDataTimestamp, endDataTimestamp, comparator, seriesX);
                            }
                            if (total > 0)
                                plot.getData().add(new XYChart.Data<>(category, dao.getXYValues(null, null, category, seriesY, value, startDataTimestamp, endDataTimestamp, comparator, seriesX) / total));
                            else plot.getData().add(new XYChart.Data<>(category, 0f));
                        }
                    } else {

                        String xMinValue = c.xMin,xMaxValue = c.xMax;
                        if (c.xMin.equals(""))
                            xMinValue = dao.getFirstOrLastValue(true, seriesX);
                        if (c.xMax.equals(""))
                            xMaxValue = dao.getFirstOrLastValue(false, seriesX);

                        if (seriesX.equals("history.timestamp")) {

                            if (c.xMin.equals(""))
                                xMinValue = Instant.ofEpochMilli(Long.parseLong(xMinValue)).atZone(ZoneId.systemDefault()).toLocalDate().toString();
                            if (c.xMax.equals(""))
                                xMaxValue = Instant.ofEpochMilli(Long.parseLong(xMaxValue)).atZone(ZoneId.systemDefault()).toLocalDate().toString();

                            LocalDate startDate, endDate;

                            startDate = LocalDate.parse(xMinValue);
                            endDate = LocalDate.parse(xMaxValue);

                            List<LocalDate> xValues = startDate.datesUntil(endDate.plusDays(1)).toList();
                            for (int i = 0; i < xValues.size(); i += c.stepSize) {

                                float total = 1f;
                                if (c.xType.equals("Accumulative")) {
                                    if (c.isRelative) {
                                        total = dao.getValuesByTime(c.fromDate, xValues.get(i).plusDays((long) c.stepSize), seriesY, "All", startDataTimestamp, endDataTimestamp, comparator);
                                    }
                                    if (total > 0)
                                        plot.getData().add(new XYChart.Data<>(xValues.get(i).toString().substring(0, 10), dao.getValuesByTime(c.fromDate, xValues.get(i).plusDays((long) c.stepSize), seriesY, value, startDataTimestamp, endDataTimestamp, comparator) / total));
                                    else
                                        plot.getData().add(new XYChart.Data<>(xValues.get(i).toString().substring(0, 10), 0f));
                                } else {
                                    if (c.isRelative) {
                                        total = dao.getValuesByTime(xValues.get(i), xValues.get(i).plusDays((long) c.stepSize), seriesY, "All", startDataTimestamp, endDataTimestamp, comparator);
                                    }
                                    if (total > 0)
                                        plot.getData().add(new XYChart.Data<>(xValues.get(i).toString().substring(0, 10), dao.getValuesByTime(xValues.get(i), xValues.get(i).plusDays((long) c.stepSize), seriesY, value, startDataTimestamp, endDataTimestamp, comparator) / total));
                                    else
                                        plot.getData().add(new XYChart.Data<>(xValues.get(i).toString().substring(0, 10), 0f));
                                }
                            }
                        } else {


                            float xMinF = Float.parseFloat(xMinValue);
                            float xMaxF = Float.parseFloat(xMaxValue);

                            for (float i = xMinF; i < xMaxF; i += c.stepSize) {

                                float total = 1;
                                if (c.xType.equals("Accumulative")) {
                                    if (c.isRelative) {
                                        total = dao.getXYValues(xMinF, i + c.stepSize, null, seriesY, "All", startDataTimestamp, endDataTimestamp, comparator, seriesX);
                                    }
                                    if (total > 0)
                                        plot.getData().add(new XYChart.Data<>(String.valueOf(i), dao.getXYValues(xMinF, i + c.stepSize, null, seriesY, value, startDataTimestamp, endDataTimestamp, comparator, seriesX) / total));
                                    else plot.getData().add(new XYChart.Data<>(String.valueOf(i), 0f));
                                } else {
                                    if (c.isRelative) {
                                        total = dao.getXYValues(i, i + c.stepSize, null, seriesY, "All", startDataTimestamp, endDataTimestamp, comparator, seriesX);
                                    }
                                    if (total > 0)
                                        plot.getData().add(new XYChart.Data<>(String.valueOf(i), dao.getXYValues(i, i + c.stepSize, null, seriesY, value, startDataTimestamp, endDataTimestamp, comparator, seriesX) / total));
                                    else plot.getData().add(new XYChart.Data<>(String.valueOf(i), 0f));
                                }
                            }

                        }
                    }
                    ((XYChart<String, Float>) chart).getData().add(plot);
                    chart.setAnimated(false);
                    setChartBounds((XYChart)chart,c.yMin,c.yMax);
                }

            }

        } else {
            ((PieChart) chart).getData().clear();

            String column = c.yAxis;
            Boolean discrete = null;
            for (ColumnInfo info : columnInfo) {
                if (column.equals(info.name)) {
                    column = info.table + "." + column;
                    discrete = info.discrete;
                    break;
                }
            }
            for (String category : c.seriesList) {
                String comparator = "";
                if (Boolean.FALSE.equals(discrete))
                    comparator = category.substring(category.lastIndexOf(" ") + 1, category.lastIndexOf("("));
                String name = category.substring(0, category.lastIndexOf(":"));
                String value = category.substring(category.indexOf(":") + 2, category.lastIndexOf(" "));
                PieChart.Data data;

                data = new PieChart.Data(name, dao.getPieValues(column, value, startDataTimestamp, endDataTimestamp, comparator));


                ((PieChart) chart).getData().add(data);
            }
            if (c.isRelative) {
                double sum = 0f;
                for (PieChart.Data data : ((PieChart) chart).getData()) {
                    sum += data.getPieValue();
                }
                double result = dao.getPieValues(column, "All", startDataTimestamp, endDataTimestamp, "") - sum;

                if (result <= 0) {
                    result = 0f;
                }
                PieChart.Data rest = new PieChart.Data("REST", result);
                ((PieChart) chart).getData().add(rest);

            }
        }
        return chart;
    }

    public void setChartColors(Chart chart, List<String> seriesList, boolean showPoints){
        if(chart.getClass().equals(PieChart.class)) {
            List<String> colors = new ArrayList<>();
            for (PieChart.Data data : ((PieChart) chart).getData()) {
                String color;
                if (!data.getName().equals("REST")) {
                    int index = ((PieChart) chart).getData().indexOf(data);
                    String series = seriesList.get(index);
                    color = series.substring(series.lastIndexOf("(") + 1, series.lastIndexOf(")"));
                } else {
                    color = "#444444";
                }
                colors.add(color);
                data.getNode().setStyle("-fx-pie-color: " + color + ";");
            }
            chart.applyCss();
            for (Node node : chart.lookupAll(".chart-legend-item-symbol")) {
                for (String styleClass : node.getStyleClass()) {
                    if (styleClass.startsWith("data")) {
                        final int index = Integer.parseInt(styleClass.substring(4));

                        node.setStyle("-fx-background-color: " + colors.get(index) + ";");
                    }
                }
            }
        }
        else{
            List<String> colors = new ArrayList<>();

            for (XYChart.Series<?, ?> plot : ((XYChart<?, ?>) chart).getData()) {         //sets line and symbol colors
                int index = ((XYChart<?, ?>) chart).getData().indexOf(plot);          //I hate all of this but at least it's somewhat readable
                String series = seriesList.get(index);
                String color = series.substring(series.lastIndexOf("(") + 1, series.lastIndexOf(")"));
                colors.add(color);

                Class<? extends Chart> chartClass = chart.getClass();


                if (chartClass.equals(LineChart.class)) {
                    plot.getNode().lookup(".chart-series-line").setStyle("-fx-stroke: " + color);
                    if (showPoints)
                        for (XYChart.Data<?, ?> data : plot.getData())
                            data.getNode().lookup(".chart-line-symbol").setStyle("-fx-background-color: " + color + ",whitesmoke;");
                } else if (chartClass.equals(AreaChart.class) || chartClass.equals(StackedAreaChart.class)) {

                    plot.getNode().lookup(".chart-series-area-line").setStyle("-fx-stroke: " + color);
                    plot.getNode().lookup(".chart-series-area-fill").setStyle("-fx-fill: rgba(" + Integer.parseInt(color.substring(1, 3), 16) + "," + Integer.parseInt(color.substring(3, 5), 16) + "," + Integer.parseInt(color.substring(5), 16) + ",0.15);");
                    if (showPoints)
                        for (XYChart.Data<?, ?> data : plot.getData())
                            data.getNode().lookup(".chart-area-symbol").setStyle("-fx-background-color: " + color + ",whitesmoke;");
                } else if (chartClass.equals(BarChart.class) || chartClass.equals(StackedBarChart.class)) {
                    for (XYChart.Data<?, ?> data : plot.getData()) {
                        data.getNode().lookup(".chart-bar").setStyle("-fx-bar-fill: " + color);
                    }
                }
            }
            chart.applyCss();               //generates legend
            for (Node node : chart.lookupAll(".chart-legend-item-symbol")) {        //sets legend colors
                for (String styleClass : node.getStyleClass()) {
                    if (styleClass.startsWith("series")) {
                        final int index = Integer.parseInt(styleClass.substring(6));
                        String inside = ";";
                        if (!chart.getClass().equals(BarChart.class) && !chart.getClass().equals(StackedBarChart.class))
                            inside = ",whitesmoke;";
                        node.setStyle("-fx-background-color: " + colors.get(index) + inside);
                    }
                }
            }
        }
    }

    private void setChartBounds(XYChart chart, String yMin, String yMax) {
        if (yMax.equals(yMin) && yMax.equals(""))
            chart.getYAxis().setAutoRanging(true);
        else{
            Axis<Number> axis = ((XYChart<String,Number>) chart).getYAxis();
            axis.setAutoRanging(false);
            if (axis.getClass().equals(NumberAxis.class)) {
                if (!yMin.equals("")) {
                    ((NumberAxis) axis).setLowerBound(Double.parseDouble(yMin));
                }
                if (!yMax.equals("")) {
                    ((NumberAxis) axis).setUpperBound(Double.parseDouble(yMax));
                }
            }
        }
    }
}
