package solartracer.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import solartracer.utils.Constants;

import static solartracer.utils.Constants.COLON;

/**
 * Helper class to define a datapoint.
 *
 * @author javachaos
 */
public class DataPoint implements Comparable<DataPoint> {

  private static final String DATAPOINT_REGEX = "(([+-]?(\\d*[.])?\\d+):){9}(\\d{1,19})";

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
    this(new Date().getTime(), data);
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

  public static DataPoint fromString(String dataStr) {
    if (!dataStr.matches(DATAPOINT_REGEX)) {
      throw new RuntimeException("Invalid data string.");
    }
    String[] data = dataStr.split(COLON);
    return new DataPoint(Long.parseLong(data[10]),
    Float.parseFloat(data[0]),
    Float.parseFloat(data[1]),
    Float.parseFloat(data[2]),
    Float.parseFloat(data[3]),
    Float.parseFloat(data[4]),
    Float.parseFloat(data[5]),
    Float.parseFloat(data[6]),
    Float.parseFloat(data[7]),
    Float.parseFloat(data[8]),
    Float.parseFloat(data[9]));

  }

  /**
   * Get the time for this datapoint typically when this datapoint was created.
   * 
   * @return the timestamp for this datapoint
   */
  public long getTime() {
    return timestamp;
  }

  /**
   * Return the string formatted time ('yyyy-MM-dd HH:mm:ss.SSS')
   *
   * @return the time as a formatted time string.
   */
  public String getTimeFormatted() {
    SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.TIME_FORMAT);
    return dateFormat.format(new Date(timestamp));
  }

  /**
   * Get the battery voltage (volts)
   * @return the battery voltage
   */
  public float getBatteryVoltage() {
    return batteryVoltage;
  }

  /**
   * Get the PV Voltage (photovoltaic voltage) for this datapoint (volts)
   * @return the PV Voltage
   */
  public float getPvVoltage() {
    return pvVoltage;
  }

  /**
   * Get the Load current (amps).
   * 
   * @return the load current
   */
  public float getLoadCurrent() {
    return loadCurrent;
  }

  /**
   * Get the over discharge value (boolean, 1 or 0).
   * 
   * @return 1 if there as an over discharge 0 if not
   */
  public float getOverDischarge() {
    return overDischarge;
  }

  /**
   * Max battery voltage.
   * 
   * @return max battery voltage
   */
  public float getBatteryMax() {
    return batteryMax;
  }

  /**
   * If the battery is full return 1 else return 0.
   * 
   * @return 1 if the battery is full else 0
   */
  public float getBatteryFull() {
    return full;
  }

  /**
   * Return 1 of the battery is charging, 0 if not.
   * 
   * @return 1 of the battery is charging 0 if not
   */
  public float getCharging() {
    return charging;
  }

  /**
   * Temp of the battery in degrees celcius
   * 
   * @return the battery temperature in degrees celcius
   */
  public float getBatteryTemp() {
    return batteryTemp;
  }

  /**
   * Get the charge current in amps
   * 
   * @return the charge current
   */
  public float getChargeCurrent() {
    return chargeCurrent;
  }

  /**
   * Get if there is a load connected or not (boolean).
   * 
   * @return 1 if there is a load 0 if not
   */
  public float getLoadOnoff() {
    return loadOnOff;
  }

  @Override
  public String toString() {
    return batteryVoltage
        + COLON
        + pvVoltage
        + COLON
        + loadCurrent
        + COLON
        + overDischarge
        + COLON
        + batteryMax
        + COLON
        + full
        + COLON
        + charging
        + COLON
        + batteryTemp
        + COLON
        + chargeCurrent
        + COLON
        + loadOnOff
        + COLON
        + timestamp
        + Constants.NEWLINE;
  }

  /**
   * Compare two DataPoints.
   *
   * @param o the object to be compared.
   */
  @Override
  public int compareTo(DataPoint o) {
    return Long.compare(timestamp, o.timestamp);
  }
}
