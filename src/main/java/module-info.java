module com.example.datenpunk {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.datenpunk to javafx.fxml;
    exports com.example.datenpunk;
}