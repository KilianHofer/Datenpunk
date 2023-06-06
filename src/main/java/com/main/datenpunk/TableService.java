package com.main.datenpunk;

public class TableService /*extends Service<List<ObjectTableElement>>*/{

    /*
    DAO dao = DAO.getInstance();

    List<ObjectTableElement> elements;

    TableView<ObjectTableElement> table;
    TableColumn<ObjectTableElement,String> column;
    LocalDate fromDate,toDate;
    ObservableList<ListView<String>> listViews;

    Singleton singleton = Singleton.getInstance();

    public TableService(TableView<ObjectTableElement> table, LocalDate fromDate, LocalDate toDate, ObservableList<ListView<String>> listViews,TableColumn<ObjectTableElement,String> column){

        this.table = table;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.listViews = listViews;
        this.column = column;


        setOnSucceeded(workerStateEvent -> {
            table.getItems().setAll(elements);

            ObservableList<TableColumn<ObjectTableElement,?>> sortColumns = FXCollections.observableArrayList();
            if(table.getSortOrder().size()>0) {
                sortColumns = FXCollections.observableArrayList(table.getSortOrder());
            }
            else{
                sortColumns.add(column);
            }
            table.getSortOrder().setAll(sortColumns);

            singleton.sorting = false;

        });
    }

    @Override
    protected Task<List<ObjectTableElement>> createTask() {
        return new Task<>() {
            @Override
            protected List<ObjectTableElement> call() {
                //elements = dao.selectMain(fromDate, toDate, listViews);
                return null;//elements;
            }
        };
    }

     */
}
