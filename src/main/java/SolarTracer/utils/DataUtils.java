package SolarTracer.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataUtils {
	
	/**
	 * Logger.
	 */
	public static final Logger LOGGER = LoggerFactory.getLogger(DataUtils.class);
	
	/**
	 * Parse the datastring into a DataPoint.
	 * 
	 * @param dataPoint
	 *     the datapoint, Expected datastring format: 
	 *     "batt_voltage:pv_voltage:load_current:over_discharge:batt_max:batt_full:charging:batt_temp:charge_current:load_onoff:timestamp"
	 *     example: "0.0:1:1:12.0:1:1:23.0:1.5:1"
	 * @return
	 *     a DataPoint.
	 */
	public static final DataPoint parseDataPoint(String dataPoint) {		
        String[] d = dataPoint.split(":");
	    if (d.length == Constants.DEFAULT_DATA_LENGTH) {
	        float[] returnArr = new float[Constants.DEFAULT_DATA_LENGTH - 1];
	        for(int i = 0; i < returnArr.length; i++) {
	        	returnArr[i] = Float.parseFloat(d[i]);
	        }
	        return new DataPoint(Long.parseLong(d[Constants.DEFAULT_DATA_LENGTH]), returnArr);
		} else {
			LOGGER.error("Invalid data format.");
			return new DataPoint(null);
		}
	}
}
