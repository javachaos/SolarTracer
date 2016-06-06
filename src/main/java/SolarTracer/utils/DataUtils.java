package SolarTracer.utils;

public class DataUtils {

	/**
	 * Parse the datastring into a DataPoint.
	 * 
	 * @param dataPoint
	 *     the datapoint, Expected datastring format: 
	 *     "batt_voltage:pv_voltage:load_current:over_discharge:batt_max:batt_full:charging:batt_temp:charge_current:load_onoff"
	 *     example: "0.0:1:1:12.0:1:1:23.0:1.5:1"
	 * @return
	 *     a DataPoint.
	 */
	public static final DataPoint parseDataPoint(String dataPoint) {		
        String[] d = dataPoint.split(":");
	    if (d.length == Constants.DEFAULT_DATA_LENGTH) {
	        float[] returnArr = new float[11];
	        for(int i = 0; i < returnArr.length; i++) {
	        	returnArr[i] = Float.parseFloat(d[i]);
	        }
	        return new DataPoint(returnArr);
		} else {
			throw new IllegalArgumentException("Invalid data format.");
		}
	}
}
