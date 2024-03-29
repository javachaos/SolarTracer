package solartracer.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import solartracer.data.DataPoint;
import solartracer.serial.ShutdownListener;

/**
 *  @author fred
 */
public final class SQLiteDatabase implements ShutdownListener {

  /**
   * Database connection instance.
   */
  private static Connection conn;

  /**
   * True if the database driver is loaded.
   */
  private static boolean isLoaded = false;

  private static final List<DataPoint> dataPointList = new ArrayList<>();

  /**
   * Private default ctor.
   */
  public SQLiteDatabase() {
    //unused
  }

  /** 
   * Loads driver.
   */
  private synchronized void loadDriver() {

    try {
      Class.forName(Constants.DRIVER);
      isLoaded = true;
    } catch (ClassNotFoundException cnfe) {
      ExceptionUtils.log(SQLiteDatabase.class, cnfe);
    }
  }

  /**
   * Returns a connection to the embedded sqlite database.
   *
   * @return a connection to the embedded sqlite database.
   */
  public synchronized Connection getConnection() {

    if (!isLoaded) {
      loadDriver();
    }

    try {
      if (conn == null || conn.isClosed()) {
        conn =
            DriverManager.getConnection("jdbc:sqlite:" + Constants.DATABASE_FILE.getAbsolutePath());
      }
    } catch (SQLException ex) {
      ExceptionUtils.log(SQLiteDatabase.class, ex);
    }
    if (conn == null) {
      throw new SolarException("Database connection was null.");
    }
    return conn;
  }

  /** 
   * Create database tables.
   */
  public synchronized void createTables() {
    getConnection();
    try(Statement stat = conn.createStatement()) {
      stat.addBatch(
          Constants.DATABASE_CREATE_STMT);
      stat.executeBatch();
    } catch (SQLException e) {
      ExceptionUtils.log(SQLiteDatabase.class, e);
    } finally {
      closeItem(conn);
    }
  }

  /**
   * Helper method to close an AutoClosable
   *
   * @param c the AutoClosable to close
   */
  private void closeItem(final AutoCloseable c) {
    try {
      if (c != null) {
        c.close();
      }
    } catch (Exception e) {
      ExceptionUtils.log(SQLiteDatabase.class, e);
    }
  }

  /**
   * Inserts a datapoint into a temporary list which will
   * be written to the database after the size of the list
   * reaches a minimum threshold.
   * This is done in order to limit excessive HD disk head parking
   * on older spinning disk drives and increase the lifespan of the
   * disks.
   *
   * @param data the data point
   */
  public void insertData(final DataPoint data) {
      if (dataPointList.size() > Constants.DATA_THRESHOLD) {
        dataPointList.forEach(this::writeToDb);
        dataPointList.clear();
      }
      dataPointList.add(data);
  }

  /**
   * Insert a data point into the database.
   * 
   * @param data the data point to be added
   */
  private synchronized void writeToDb(final DataPoint data) {
    if (data != null) {
      try (PreparedStatement stat = getConnection()
              .prepareStatement(Constants.DB_INSERT_STMT)) {
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
        ExceptionUtils.log(SQLiteDatabase.class, e);
      }
    }
  }

  /** Shutdown and lock the database file. */
  public synchronized void shutdown() {
    try {
      if (conn != null) {
        conn.close();
        while (!conn.isClosed()) {
          waitForShutdown();
        }
      }
    } catch (SQLException e) {
      ExceptionUtils.log(SQLiteDatabase.class, e);
    } finally {
      conn = null;
    }
  }

  private void waitForShutdown() {
    try {
      Thread.sleep(Constants.SLEEP_TIME);
    } catch (InterruptedException e) {
      ExceptionUtils.log(SQLiteDatabase.class, e);
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Return true if the database exists.
   *
   * @return true if the database exists.
   */
  public boolean databaseExists() {
    return Constants.DATABASE_FILE.exists();
  }
}
