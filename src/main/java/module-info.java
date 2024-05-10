module solartracer {
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires java.logging;
    requires com.fazecast.jSerialComm;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    opens solartracer.gui to javafx.fxml;
    exports solartracer.main to javafx.graphics;
}