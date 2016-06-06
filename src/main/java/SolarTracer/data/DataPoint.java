package SolarTracer.data;

import java.text.SimpleDateFormat;
import java.util.Date;

import SolarTracer.utils.Constants;
import SolarTracer.utils.DataUtils;
import SolarTracer.utils.ExceptionUtils;

/**
 * Helper class to define a datapoint.
 * @author javanerd
 *
 */
public class DataPoint {
	
	/**
	 * Timestamp when this datapoint was created.
	 */
	private final long timestamp;
	
	/**
	 * Battery Voltage
	 */
	private final float battery_voltage;
	
	/**
	 * Photovoltic Voltage
	 */
	private final float pv_voltage;
	
	/**
	 * Load Current
	 */
	private final float load_current;
	
	/**
	 * Over Discharge
	 */
	private final float over_discharge;
	
	/**
	 * Battery Max
	 */
	private final float battery_max;
	
	/**
	 * Battery Full
	 */
	private final float full;
	
	/**
	 * Battery Charging
	 */
	private final float charging;
	
	/**
	 * Battery Temperature
	 */
	private final float battery_temp;
	
	/**
	 * Charging Current
	 */
	private final float charge_current;
	
	/**
	 * Load on or off
	 */
	private final float load_onoff;

	/**
	 * Create a new DataPoint. With timestamp created upon construction.
	 * @param data
	 * 		the data.
	 */
	public DataPoint(float[] data) {
		this(new Date(Constants.getCurrentTimeMillis()).getTime(), data);
	}
	
	/**
	 * Create a datapoint with auto generated timestamp, created on construction.
	 * @param data
	 * 		the data of the string.
	 */
	public DataPoint(long timestamp, float[] data) {
		this.timestamp = timestamp;
		
		if (data != null) {
	        battery_voltage = data[0];
	        pv_voltage      = data[1];
	        load_current    = data[2];
	        over_discharge  = data[3];
	        battery_max     = data[4];
	        full            = data[5];
	        charging        = data[6];
	        battery_temp    = data[7];
	        charge_current  = data[8];
	        load_onoff      = data[9];
		} else {
	        battery_voltage = 0f;
	        pv_voltage      = 0f;
	        load_current    = 0f;
	        over_discharge  = 0f;
	        battery_max     = 0f;
	        full            = 0f;
	        charging        = 0f;
	        battery_temp    = 0f;
	        charge_current  = 0f;
	        load_onoff      = 0f;
		}
	}
	
	/**
	 * Create a datapoint.
	 * @param bv
	 * 		battery voltage (V)
	 * @param pv
	 *      photovoltaic voltage (V)
	 * @param lc
	 *      load current (A)
	 * @param od
	 * 		over discharge (Boolean)
	 * @param bm
	 * 		battery max (Boolean)
	 * @param fl
	 * 		battery full (Boolean)
	 * @param ch
	 *      charging (Boolean)
	 * @param bt
	 *      battery temp (*C)
	 * @param cc
	 *      charge current (A)
	 * @param lof
	 *      load on off (Boolean)
	 */
	public DataPoint(long timestamp, float bv, float pv, float lc, float od, float bm, float fl, float ch, float bt, float cc, float lof) {
        this.timestamp = timestamp;
        battery_voltage = bv;
        pv_voltage      = pv;
        load_current    = lc;
        over_discharge  = od;
        battery_max     = bm;
        full            = fl;
        charging        = ch;
        battery_temp    = bt;
        charge_current  = cc;
        load_onoff      = lof;
	}
	
	public long getTime() {
		return timestamp;
	}
	
	/**
	 * Return the string formatted time ('yyyy-MM-dd HH:mm:ss.SSS')
	 * @return
	 * 		the time as a formated time string.
	 */
	public String getTimeFormatted() {
		String time = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		try {
			time = dateFormat.format(new Date(timestamp));
		} catch (Exception e) {
            ExceptionUtils.log(DataUtils.class, e);
		}
		return time;
	}

	public float getBatteryVoltage() {
		return battery_voltage;
	}

	public float getPvVoltage() {
		return pv_voltage;
	}

	public float getLoadCurrent() {
		return load_current;
	}

	public float getOverDischarge() {
		return over_discharge;
	}

	public float getBatteryMax() {
		return battery_max;
	}

	public float getBatteryFull() {
		return full;
	}

	public float getCharging() {
		return charging;
	}

	public float getBatteryTemp() {
		return battery_temp;
	}

	public float getChargeCurrent() {
		return charge_current;
	}

	public float getLoadOnoff() {
		return load_onoff;
	}
	
	@Override
	public String toString() {
		return battery_voltage + Constants.COLON +
               pv_voltage      + Constants.COLON +
               load_current    + Constants.COLON +
               over_discharge  + Constants.COLON +
               battery_max     + Constants.COLON +
               full            + Constants.COLON +
               charging        + Constants.COLON +
               battery_temp    + Constants.COLON +
               charge_current  + Constants.COLON +
               load_onoff      + Constants.NEWLINE;
	}
}
