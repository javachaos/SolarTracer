package SolarTracer.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author fred
 *
 */
public final class DatabaseUtils {


	/**
	 * Logger.
	 */
	public static final Logger LOGGER = LoggerFactory.getLogger(DatabaseUtils.class);
	  
    /**
     * Database connection instance.
     */
    private static volatile Connection conn;

    /**
     * True if the database driver is loaded.
     */
    private static boolean isLoaded = false;
    
    /**
     * Private default ctor.
     */
    private DatabaseUtils() {
    }
    
    /**
     * Loads driver.
     */
    private static synchronized void loadDriver() {

        try {
            Class.forName(Constants.DRIVER);
            isLoaded = true;
        } catch (ClassNotFoundException cnfe) {
        	LOGGER.error(cnfe.getMessage());
        }

    }
    
    /**
     * Returns a connection to the embedded derby database.
     * 
     * @return
     *      a connection to the embedded derby database.
     */
    public static synchronized Connection getConnection() {
        
        if (!isLoaded) {
            loadDriver();
        }
        
        try {
            if (conn == null || conn.isClosed()) {
                conn = DriverManager
                    .getConnection(
                        "jdbc:sqlite:" 
                    + Constants.DATABASE_FILE.getAbsolutePath());
            }
        } catch (SQLException ex) {
        	LOGGER.error(ex.getMessage());
        }
        
        return conn;
    }
    
    /**
     * Create database tables.
     */
    public static synchronized void createTables() {
        getConnection();
        try {
            Statement stat = conn.createStatement();
            stat.addBatch("CREATE TABLE IF NOT EXISTS Data (ID INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL,"
            		     + "battery_voltage DOUBLE, pv_voltage DOUBLE, load_current DOUBLE, over_discharge DOUBLE,"
            		     + "battery_max DOUBLE, battery_full BOOLEAN, charging BOOLEAN, battery_temp DOUBLE,"
            		     + "charge_current DOUBLE, load_onoff BOOLEAN, time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            stat.executeBatch();
            conn.close();
        } catch (SQLException e) {
        	LOGGER.error(e.getMessage());
        }
    }
    
    public static synchronized int getNumRecords() {
    	ResultSet rs = null;
		Statement s;
		try {
			s = DatabaseUtils.getConnection().createStatement();
			rs = s.executeQuery("SELECT Count(*) FROM Data");
			if (rs.next()) {
				return rs.getInt(1);
			}			
		} catch (SQLException e) {
			LOGGER.error(e.getMessage());
		}
		return -1;
    }
    
    public static synchronized ArrayList<String> getData(Date first, Date second) {
    	if (first != null && first.getTime() > 0 && second != null && second.getTime() > 0) {
	        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	        ArrayList<String> returnData = new ArrayList<String>(Constants.DATA_WINDOW_SIZE);
	        StringBuilder sb = new StringBuilder();
			ResultSet rs = null;
			Statement s;
			try {
				s = DatabaseUtils.getConnection().createStatement();
				rs = s.executeQuery("SELECT * FROM Data WHERE time >= Datetime('"
				    + dateFormat.format(first)
				    + "') AND time <= Datetime('"+dateFormat.format(second)+"') ORDER BY ID DESC LIMIT "
				    + Constants.DATA_WINDOW_SIZE);
				while (rs.next()) {
			        float battery_voltage = rs.getFloat("battery_voltage");
			        float pv_voltage      = rs.getFloat("pv_voltage");
			        float load_current    = rs.getFloat("load_current");
			        float over_discharge  = rs.getFloat("over_discharge");
			        float battery_max     = rs.getFloat("battery_max");
			        float full            = rs.getFloat("full");
			        float charging        = rs.getFloat("charging");
			        float battery_temp    = rs.getFloat("battery_temp");
			        float charge_current  = rs.getFloat("charge_current");
			        float load_onoff      = rs.getFloat("load_onoff");
			        Date datetime             = rs.getDate("time");
			        sb.append(battery_voltage + ":");
			        sb.append(pv_voltage + ":");
			        sb.append(load_current + ":");
			        sb.append(over_discharge + ":");
			        sb.append(battery_max + ":");
			        sb.append(full + ":");
			        sb.append(charging + ":");
			        sb.append(battery_temp + ":");
			        sb.append(charge_current + ":");
			        sb.append(load_onoff + ":");
			        sb.append(datetime.getTime());
			        returnData.add(sb.toString());
			        sb = new StringBuilder();
				}
			} catch (SQLException e) {
				LOGGER.error(e.getMessage());
			}
			return returnData;
    	}
		return null;
    }
    
    public static synchronized void insertData(String dataPoint) {
    	if (dataPoint != null && dataPoint.length() > 0) {
	        String[] d = dataPoint.split(":");
	        if (d.length != Constants.DEFAULT_DATA_LENGTH) {
	        	return;
	        }
	        float battery_voltage = Float.parseFloat(d[0]);
	        float pv_voltage      = Float.parseFloat(d[1]);
	        float load_current    = Float.parseFloat(d[2]);
	        float over_discharge  = Float.parseFloat(d[3]);
	        float battery_max     = Float.parseFloat(d[4]);
	        float full            = Float.parseFloat(d[5]);
	        float charging        = Float.parseFloat(d[6]);
	        float battery_temp    = Float.parseFloat(d[7]);
	        float charge_current  = Float.parseFloat(d[8]);
	        float load_onoff      = Float.parseFloat(d[9]);
	        long  datetime        = Long.parseLong(d[10]);
	
	        try {
	            Statement stat = getConnection().createStatement();
	            stat.executeUpdate("INSERT INTO Data("
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
	            		+ ")"
	            		
	            		+ "VALUES("
	            		+ battery_voltage + ", "
	            		+ pv_voltage +      ", "
	                    + load_current +    ", "
	                    + over_discharge +  ", "
	                    + battery_max +     ", "
	                    + full +            ", "
	                    + charging +        ", "
	                    + battery_temp +    ", "
	                    + charge_current +  ", "
	                    + load_onoff +      ", "
	                    + datetime
	            		+ ")");
	        } catch (SQLException e) {
	        	LOGGER.error(e.getMessage());
	        }
    	}
    }
    
    /**
     * Shutdown and lock the database file.
     */
    public static void shutdown() {
        try {
        	if (conn != null) {
	            conn.close();
	            while (!conn.isClosed()) {
	                try {
	                    Thread.sleep(Constants.SLEEP_TIME);
	                } catch (InterruptedException e) {
	                	LOGGER.error(e.getMessage());
	                }
	            }
        	}
        } catch (SQLException e) {
        	LOGGER.error(e.getMessage());
        } finally {
            conn = null;
        }
    }
    
    /**
     * Return true if the database exists.
     * @return
     *      true if the database exists.
     */
    public static boolean databaseExists() {
        return Constants.DATABASE_FILE.exists();
    }
}