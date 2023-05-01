package solartracer.utils;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Date;
import java.util.logging.Logger;

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
   * Upper bound for battery voltage level.
   */
  public static final double BATT_LEVEL_UPPER_BOUND = 16.0;

  /**
   * Lower bound for battery voltage level.
   */
  public static final double BATT_LEVEL_LOWER_BOUND = 11.0;

  /**
   * Battery level tick unit.
   */
  public static final double BATT_LEVEL_TICK_UNIT = 0.5;

  /**
   * Number of bytes in an int.
   */
  public static final int BYTES_PER_INT = 4;

  /**
   * Number of milliseconds per second.
   */
  public static final Integer MS_PER_SEC = 1000;

  /**
   * Font size for alert texts.
   */
  public static final double ALERT_TEXT_FONT_SIZE = 12;

  /**
   * Time format string.
   */
  public static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

  /**
   * Database insert prepared statement.
   */
  public static final String DB_INSERT_STMT =
          "INSERT INTO Data("
                  + "battery_voltage, "
                  + "pv_voltage, "
                  + "load_current, "
                  + "over_discharge,"
                  + "battery_max, "
                  + "battery_full, "
                  + "charging, "
                  + "battery_temp, "
                  + "charge_current, "
                  + "load_onoff,"
                  + "time"
                  + ") VALUES(?,?,?,?,?,?,?,?,?,?,?)";

  /**
   * The NTP port number.
   */
  private static final int NTP_PORT = 123;

  /**
   * Private ctor.
   */
  private Constants() {}

  /**
   * Time server URL as a string.
   */
  public static final String TIME_SERVER = "0.ca.pool.ntp.org";

  /**
   * Calculate the current Date using NTP to get an accurate value.
   * 
   * @return the current Date
   */
  private static Date time() {
    try (DatagramSocket socket = new DatagramSocket()) {
        InetAddress address = InetAddress.getByName(TIME_SERVER);
        byte[] buf = new NtpMessage().toByteArray();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, NTP_PORT);
        socket.send(packet);

        // Get response
        socket.receive(packet);
        NtpMessage recvMsg = new NtpMessage(packet.getData());
        Logger.getAnonymousLogger().info(() -> "Timestamp received: " + recvMsg);
        return new Date((long) recvMsg.receiveTimestamp);
    } catch (IOException e) {
        ExceptionUtils.log(Constants.class, e);
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
   * Sleep time to wait for database to shut down before checking if it has shut down.
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
   * Time to wait before termination in millis. (10s)
   */
  public static final long TERMINATION_TIMEOUT = 10000;

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
   * System newline character.
   * MUST BE '\n' not System.lineSeparator()!
   */
  public static final char NEWLINE = '\n';

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
