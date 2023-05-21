module com.example.datenpunk {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;


    opens com.main.datenpunk to javafx.fxml;
    exports com.main.datenpunk;
    exports enteties;
    exports database;
    opens enteties to javafx.fxml;
}