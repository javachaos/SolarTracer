package SolarTracer.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jssc.SerialPortList;

public class SerialUtils {
	
	/**
	 * Logger.
	 */
	public static final Logger LOGGER = LoggerFactory.getLogger(SerialUtils.class);


	public static ObservableList<String> detectPorts() {
		ObservableList<String> portList = FXCollections.observableArrayList();
	        String[] serialPortNames = SerialPortList.getPortNames();
	        for(final String name : serialPortNames){
	            LOGGER.debug("Adding " + name + " to port list." + System.lineSeparator());
	            portList.add(name);
	        }
	        return portList;
	}
}
