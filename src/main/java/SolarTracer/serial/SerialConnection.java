package SolarTracer.serial;

import java.util.List;

import SolarTracer.utils.DataPoint;
import SolarTracer.utils.DataRecvListener;
import javafx.collections.ObservableList;

public interface SerialConnection {
	
	/**
	 * Send a data point over this connection.
	 * @param data
	 * 		the data to send over the connection.
	 */
	void sendDataPoint(final DataPoint data);
	
	/**
	 * Receive data from this connection. This function is an asynchronous callback.
	 * @param dl
	 *     the data to be provided to the caller.
	 */
	void addDataRecvListener(DataRecvListener dl);
	
	/**
	 * Remove the data receive listener dl.
	 * @param dl
	 * 		the data listener to remove.
	 * @return
	 * 		the success of the operation as a boolean.
	 */
	boolean removeDataRecvListener(DataRecvListener dl);
	

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
     * Get the list of strings. Received from last call to getStrings.
     * @return
     * 		the list of strings read from input
     */
    public List<String> getStrings();
    
    /**
     * Get the set of Port names.
     * @return
     */
    public ObservableList<String> getPortNames();

}