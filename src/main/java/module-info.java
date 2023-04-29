module com.example.datenpunk {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.main.datenpunk to javafx.fxml;
    exports com.main.datenpunk;
    exports enteties;
}