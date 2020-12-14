package solartracer.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import solartracer.utils.Constants;
import solartracer.utils.DataUtils;
import solartracer.utils.ExceptionUtils;

/**
 * Helper class to define a datapoint.
 *
 * @author javanerd
 */
public class DataPoint {

  /** Timestamp when this datapoint was created. */
  private final long timestamp;

  /** Battery Voltage */
  private final float batteryVoltage;

  /** Photovoltic Voltage */
  private final float pvVoltage;

  /** Load Current */
  private final float loadCurrent;

  /** Over Discharge */
  private final float overDischarge;

  /** Battery Max */
  private final float batteryMax;

  /** Battery Full */
  private final float full;

  /** Battery Charging */
  private final float charging;

  /** Battery Temperature */
  private final float batteryTemp;

  /** Charging Current */
  private final float chargeCurrent;

  /** Load on or off */
  private final float loadOnOff;

  /**
   * Create a new DataPoint. With timestamp created upon construction.
   *
   * @param data the data.
   */
  public DataPoint(float[] data) {
    this(new Date(Constants.getCurrentTimeMillis()).getTime(), data);
  }

  /**
   * Create a new DataPoint
   *
   * @param timestamp the timestamp for this DataPoint
   * @param data the data of the string.
   */
  public DataPoint(long timestamp, float... data) {
    this.timestamp = timestamp;

    if (data != null) {
      batteryVoltage = data[0];
      pvVoltage = data[1];
      loadCurrent = data[2];
      overDischarge = data[3];
      batteryMax = data[4];
      full = data[5];
      charging = data[6];
      batteryTemp = data[7];
      chargeCurrent = data[8];
      loadOnOff = data[9];
    } else {
      batteryVoltage = 0f;
      pvVoltage = 0f;
      loadCurrent = 0f;
      overDischarge = 0f;
      batteryMax = 0f;
      full = 0f;
      charging = 0f;
      batteryTemp = 0f;
      chargeCurrent = 0f;
      loadOnOff = 0f;
    }
  }

  public long getTime() {
    return timestamp;
  }

  /**
   * Return the string formatted time ('yyyy-MM-dd HH:mm:ss.SSS')
   *
   * @return the time as a formated time string.
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
    return batteryVoltage;
  }

  public float getPvVoltage() {
    return pvVoltage;
  }

  public float getLoadCurrent() {
    return loadCurrent;
  }

  public float getOverDischarge() {
    return overDischarge;
  }

  public float getBatteryMax() {
    return batteryMax;
  }

  public float getBatteryFull() {
    return full;
  }

  public float getCharging() {
    return charging;
  }

  public float getBatteryTemp() {
    return batteryTemp;
  }

  public float getChargeCurrent() {
    return chargeCurrent;
  }

  public float getLoadOnoff() {
    return loadOnOff;
  }

  @Override
  public String toString() {
    return batteryVoltage
        + Constants.COLON
        + pvVoltage
        + Constants.COLON
        + loadCurrent
        + Constants.COLON
        + overDischarge
        + Constants.COLON
        + batteryMax
        + Constants.COLON
        + full
        + Constants.COLON
        + charging
        + Constants.COLON
        + batteryTemp
        + Constants.COLON
        + chargeCurrent
        + Constants.COLON
        + loadOnOff
        + Constants.NEWLINE;
  }
}
