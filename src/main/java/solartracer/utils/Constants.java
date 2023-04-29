package solartracer.utils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Date;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

/**
 * Constants class.
 * 
 * @author fred
 *
 */
public class Constants {

  /**
   * Number of data points to collect in memory before writing to database (disk).
   * Each datapoint is roughly ~44 bytes.
   */
  public static final int DATA_THRESHOLD = 4096;

    /**
   * Private ctor.
   */
  private Constants() {}

  /**
   * Time server URL as a string.
   */
  public static final String TIME_SERVER = "time.nist.gov";

  /**
   * Calculate the current Date using NTP to get an accurate value.
   * 
   * @return the current Date
   */
  private static Date time() {
    TimeInfo timeInfo = null;
    try {
      NTPUDPClient timeClient = new NTPUDPClient();
      InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
      timeInfo = timeClient.getTime(inetAddress);
    } catch (IOException e) {
      ExceptionUtils.log(Constants.class, e);
    }
    if (timeInfo != null) {
      long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();
      return new Date(returnTime);
    }
    return new Date();
  }

  /**
   * Update the current time offset with time from NTP
   */
  public static void updateTimeoffset() {
    Date t = time();
    timeOffset = t.getTime() - System.currentTimeMillis();
  }

  /**
   * Offset from true NTP time from the system time.
   */
  private static long timeOffset;

  static {
    time();
    timeOffset = -1L * time().getTime() + System.currentTimeMillis();
  }

  /**
   * Sleep time. Used to delay the state monitor.
   */
  public static final long SLEEP_TIME = 200;

  /**
   * Database file name. 
   */
  public static final String DATABASE_NAME = "solar_data.db";

  /**
   *  SQLite embedded database driver class.
   */
  public static final String DRIVER = "org.sqlite.JDBC";

  /**
   * Database file. 
   */
  public static final File DATABASE_FILE = new File(Constants.DATABASE_NAME);

  /** 
   * Default size of a data point.
   */
  public static final int DEFAULT_DATA_LENGTH = 11;

  /**
   * Number of values to show in graph view. 
   */
  public static final int DATA_WINDOW_SIZE = 10;

  /**
   *  Synchronize the system clock every 5 minutes with NTP time. 
   *  Frequency in seconds. 
   */
  public static final long UPDATE_CLOCK_FREQUENCY = 300;

  /**
   * Time to wait before termination in millis. 
   */
  public static final long TERMINATION_TIMEOUT = 1000;

  /** 
   * Number of threads available. 
   */
  public static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();

  /**
   * GUI Update frequency. 
   */
  public static final long GUI_SLEEPTIME = 1000;

  /**
   * Charset used for all files in the program.
   */
  public static final Charset CHARSET = StandardCharsets.UTF_8;

  /**
   * System independant newline character.
   */
  public static final String NEWLINE = System.lineSeparator();

  /**
   * Colon character.
   */
  public static final String COLON = ":";

  /**
   * Baud rate for serial comms.
   */
  public static final int BAUD_RATE = 57600;

  /**
   *  Serial Timeout. 
   */
  public static final int SERIAL_TIMEOUT = 1000;

  /**
   * Database creation constant
   */
  public static final String DATABASE_CREATE_STMT = "CREATE TABLE IF NOT EXISTS Data "
		+ "(ID INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT"
        + " NULL,battery_voltage DOUBLE, pv_voltage DOUBLE, load_current DOUBLE,"
        + " over_discharge DOUBLE,battery_max DOUBLE, battery_full BOOLEAN, charging"
        + " BOOLEAN, battery_temp DOUBLE,charge_current DOUBLE, load_onoff BOOLEAN, time"
        + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

  /**
   * Get the current time in milliseconds.
   * 
   * @return the current time in milliseconds
   */
  public static long getCurrentTimeMillis() {
    return Clock.offset(Clock.systemDefaultZone(), Duration.ofMillis(timeOffset)).millis();
  }
}
