package solartracer.serial;

import javafx.collections.ObservableList;
import solartracer.data.DataPointListener;

public interface SerialConnection {
	
	/**
	 * Receive data from this connection.
	 * @param dl
	 *     the data to be provided to the caller.
	 */
	void addDataPointListener(DataPointListener dl);
	
	
	/**
	 * Remove the data receive listener dl.
	 * @param dl
	 * 		the data listener to remove.
	 * @return
	 * 		the success of the operation as a boolean.
	 */
	boolean removeDataPointListener(DataPointListener dl);
	

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
	public void connect(String port);

	/**
	 * Disconnect the currently connected serial port.
	 */
	public void disconnect();
	
	/**
	 * Return true if the connection is still alive.
	 * @return
	 * 		true if the connection is still alive.
	 */
	public boolean isConnected();
    
    /**
     * Get the set of Port names.
     * @return
     */
    public ObservableList<String> getPortNames();

}
