package solartracer.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import solartracer.data.DataPoint;

/**
 * Utility class to manipulate data.
 * 
 * @author fred
 *
 */
public class DataUtils {

  /**
   * Private constructor
   */
  private DataUtils() {}
  
  /** Logger. */
  public static final Logger LOGGER = LogManager.getLogger(DataUtils.class);

  /**
   * Parse the datastring into a DataPoint.
   *
   * @param dataPoint the datapoint, Expected datastring format:
   *     "batt_voltage:pv_voltage:load_current:over_discharge:batt_max:batt_full:charging:batt_temp:charge_current:load_onoff:timestamp"
   *     example: "0.0:1:1:12.0:1:1:23.0:1.5:1:1635692864"
   * @return a DataPoint.
   */
  public static DataPoint parseDataPoint(final String dataPoint) {
    String[] d = dataPoint.split(Constants.COLON);
    if (d.length == Constants.DEFAULT_DATA_LENGTH) {
      float[] returnArr = new float[Constants.DEFAULT_DATA_LENGTH - 1];
      for (int i = 0; i < returnArr.length; i++) {
        returnArr[i] = Float.parseFloat(d[i]);
      }
      return new DataPoint(Long.parseLong(d[Constants.DEFAULT_DATA_LENGTH - 1]), returnArr);
    } else {
      LOGGER.error("Invalid data format.");
      return new DataPoint(null);
    }
  }
}
