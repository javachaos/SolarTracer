package solartracer.utils;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import solartracer.data.DataPoint;

/** @author fred */
public final class DatabaseUtils {

  /** Database connection instance. */
  private static Connection conn;

  /** True if the database driver is loaded. */
  private static boolean isLoaded = false;

  /** Private default ctor. */
  private DatabaseUtils() {}

  /** Loads driver. */
  private static synchronized void loadDriver() {

    try {
      Class.forName(Constants.DRIVER);
      isLoaded = true;
    } catch (ClassNotFoundException cnfe) {
      ExceptionUtils.log(DatabaseUtils.class, cnfe);
    }
  }

  /**
   * Returns a connection to the embedded derby database.
   *
   * @return a connection to the embedded derby database.
   */
  public static synchronized Connection getConnection() {

    if (!isLoaded) {
      loadDriver();
    }

    try {
      if (conn == null || conn.isClosed()) {
        conn =
            DriverManager.getConnection("jdbc:sqlite:" + Constants.DATABASE_FILE.getAbsolutePath());
      }
    } catch (SQLException ex) {
      ExceptionUtils.log(DatabaseUtils.class, ex);
    }
    if (conn == null) {
      throw new NullPointerException();
    }
    return conn;
  }

  /** Create database tables. */
  public static synchronized void createTables() {
    getConnection();
    Statement stat = null;
    try {
      stat = conn.createStatement();
      stat.addBatch(
          "CREATE TABLE IF NOT EXISTS Data (ID INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT"
              + " NULL,battery_voltage DOUBLE, pv_voltage DOUBLE, load_current DOUBLE,"
              + " over_discharge DOUBLE,battery_max DOUBLE, battery_full BOOLEAN, charging"
              + " BOOLEAN, battery_temp DOUBLE,charge_current DOUBLE, load_onoff BOOLEAN, time"
              + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
      stat.executeBatch();
    } catch (SQLException e) {
      ExceptionUtils.log(DatabaseUtils.class, e);
    } finally {
      closeItem(stat);
      closeItem(conn);
    }
  }

  /**
   * Helper method to close an AutoClosable
   *
   * @param c the AutoClosable to close
   */
  private static void closeItem(AutoCloseable c) {
    try {
      if (c != null) {
        c.close();
      }
    } catch (Exception e) {
      ExceptionUtils.log(DatabaseUtils.class, e);
    }
  }

  public static synchronized int getNumRecords() {
    ResultSet rs = null;
    Statement s = null;
    int numRecs = -1;
    try {
      Connection c = DatabaseUtils.getConnection();
      if (c != null) {
        s = c.createStatement();
        rs = s.executeQuery("SELECT Count(*) FROM Data");
        if (rs.next()) {
          numRecs = rs.getInt(1);
        }
      }
    } catch (SQLException e) {
      ExceptionUtils.log(DatabaseUtils.class, e);
    } finally {
      closeItem(rs);
      closeItem(s);
    }
    return numRecs;
  }

  public static synchronized ArrayList<String> getData(Date first, Date second) {
    if (first != null && first.getTime() > 0 && second != null && second.getTime() > 0) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
      ArrayList<String> returnData = new ArrayList<>(Constants.DATA_WINDOW_SIZE);
      StringBuilder sb = new StringBuilder();
      ResultSet rs = null;
      PreparedStatement prepStat = null;
      try {
        prepStat =
            getConnection()
                .prepareStatement(
                    "SELECT * FROM Data WHERE time >= "
                        + "Datetime('?') AND time <= Datetime('?') ORDER BY ID DESC LIMIT ?");
        prepStat.setTimestamp(1, Timestamp.valueOf(dateFormat.format(first)));
        prepStat.setTimestamp(2, Timestamp.valueOf(dateFormat.format(second)));
        prepStat.setInt(3, Constants.DATA_WINDOW_SIZE);
        rs = prepStat.executeQuery();
        while (rs.next()) {
          float batteryVoltage = rs.getFloat("battery_voltage");
          float pvVoltage = rs.getFloat("pv_voltage");
          float loadCurrent = rs.getFloat("load_current");
          float overDischarge = rs.getFloat("over_discharge");
          float batteryMax = rs.getFloat("battery_max");
          float full = rs.getFloat("full");
          float charging = rs.getFloat("charging");
          float batteryTemp = rs.getFloat("battery_temp");
          float chargeCurrent = rs.getFloat("charge_current");
          float loadOnOff = rs.getFloat("load_onoff");
          Date datetime = rs.getDate("time");
          sb.append(batteryVoltage);
          sb.append(":");
          sb.append(pvVoltage);
          sb.append(":");
          sb.append(loadCurrent);
          sb.append(":");
          sb.append(overDischarge);
          sb.append(":");
          sb.append(batteryMax);
          sb.append(":");
          sb.append(full);
          sb.append(":");
          sb.append(charging);
          sb.append(":");
          sb.append(batteryTemp);
          sb.append(":");
          sb.append(chargeCurrent);
          sb.append(":");
          sb.append(loadOnOff);
          sb.append(":");
          sb.append(datetime.getTime());
          returnData.add(sb.toString());
          sb = new StringBuilder();
        }
      } catch (SQLException e) {
        ExceptionUtils.log(DatabaseUtils.class, e);
      } finally {
        closeItem(rs);
        closeItem(prepStat);
      }
      return returnData;
    }
    return new ArrayList<>();
  }

  public static synchronized void insertData(DataPoint data) {
    if (data != null) {
      PreparedStatement stat = null;
      try {
        stat =
            getConnection()
                .prepareStatement(
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
                        + ") VALUES(?,?,?,?,?,?,?,?,?,?,?)");
        stat.setFloat(1, data.getBatteryVoltage());
        stat.setFloat(2, data.getPvVoltage());
        stat.setFloat(3, data.getLoadCurrent());
        stat.setFloat(4, data.getOverDischarge());
        stat.setFloat(5, data.getBatteryMax());
        stat.setFloat(6, data.getBatteryFull());
        stat.setFloat(7, data.getCharging());
        stat.setFloat(8, data.getBatteryTemp());
        stat.setFloat(9, data.getChargeCurrent());
        stat.setFloat(10, data.getLoadOnoff());
        stat.setLong(11, data.getTime());
        stat.executeUpdate();
      } catch (SQLException e) {
        ExceptionUtils.log(DatabaseUtils.class, e);
      } finally {
        closeItem(stat);
      }
    }
  }

  /** Shutdown and lock the database file. */
  public static void shutdown() {
    try {
      if (conn != null) {
        conn.close();
        while (!conn.isClosed()) {
          waitForShutdown();
        }
      }
    } catch (SQLException e) {
      ExceptionUtils.log(DatabaseUtils.class, e);
    } finally {
      conn = null;
    }
  }

  private static void waitForShutdown() {
    try {
      Thread.sleep(Constants.SLEEP_TIME);
    } catch (InterruptedException e) {
      ExceptionUtils.log(DatabaseUtils.class, e);
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Return true if the database exists.
   *
   * @return true if the database exists.
   */
  public static boolean databaseExists() {
    return Constants.DATABASE_FILE.exists();
  }
}
