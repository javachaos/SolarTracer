package solartracer.serial;

import javafx.collections.ObservableList;
import solartracer.data.DataPointListener;
import solartracer.utils.SolarException;

/**
 * Serial connection interface. 
 * 
 * @author fred
 *
 */
public interface SerialConnection {
	
	/**
	 * Receive data from this connection.
	 * @param dl
	 *     the data to be provided to the caller.
	 */
	void addDataPointListener(DataPointListener dl) throws SolarException;

	/**
	 * Remove the data receive listener dl.
	 * @param dl
	 * 		the data listener to remove.
	 * @return
	 * 		the success of the operation as a boolean.
	 */
	@SuppressWarnings("unused")
	boolean removeDataPointListener(DataPointListener dl) throws SolarException;
	

	/**
	 * Write data to the connected serial device.
	 * @param data
	 *      the string data to be written to the serial device.
	 */
	void writeData(String data);
	
	/**
	 * Connect to the gievn serial communications port.
	 * 
	 * @param port
	 * 		the port to connect to.
	 */
	void connect(String port);

	/**
	 * Disconnect the currently connected serial port.
	 */
	void disconnect();
	
	/**
	 * Return true if the connection is still alive.
	 * @return
	 * 		true if the connection is still alive.
	 */
	boolean isConnected();
    
    /**
     * Get the set of Port names.
     * @return an observable list of port names
     */
    ObservableList<String> getPortNames();

}
