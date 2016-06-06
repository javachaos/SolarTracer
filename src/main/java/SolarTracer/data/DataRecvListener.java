package SolarTracer.data;

public interface DataRecvListener {

	/**
	 * Called to update all observers of data received for a connection.
	 * @param data
	 * 		the data received.
	 */
	public void dataRecieved(String data);
}
