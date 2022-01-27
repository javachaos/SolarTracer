package solartracer.data;

/**
 * A simple data point listener interface.
 * 
 * @author fred
 *
 */
public interface DataPointListener {
	
	/**
	 * Callback function, when a data point is received.
	 * 
	 * @param dataPoint the datapoint to handle
	 */
	void dataPointReceived(final DataPoint dataPoint);
}
