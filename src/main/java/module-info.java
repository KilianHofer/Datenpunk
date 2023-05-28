module com.example.datenpunk {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;
    requires json.simple;
    requires grpc.netty.shaded;
    requires grpc.protobuf;
    requires grpc.stub;
    requires grpc.api;
    requires grpc.core;


    opens com.main.datenpunk to javafx.fxml;
    exports com.main.datenpunk;
    exports enteties;
    exports database;
    opens enteties to javafx.fxml;
}