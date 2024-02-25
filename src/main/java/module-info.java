module solartracer {
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires java.logging;
    requires com.fazecast.jSerialComm;
    requires java.sql;
    requires io.vertx.core;
    requires io.vertx.mqtt;
    requires org.xerial.sqlitejdbc;
    requires io.netty.codec.mqtt;

    opens solartracer.gui to javafx.fxml;
}