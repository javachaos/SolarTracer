module solartracer {
    requires java.sql;
    requires com.fazecast.jSerialComm;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires org.apache.logging.log4j;

    exports solartracer.main to javafx.graphics;
    opens solartracer.gui to javafx.fxml;
}