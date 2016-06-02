package SolarTracer.utils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.time.Clock;
import java.time.Duration;
import java.util.Date;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

public class Constants {

    /**
     * Private ctor.
     */
    private Constants() {
    }
    
    /**
     * Init the constants.
     */
    public static void init() {
    	new Constants();
    }

    public static final String TIME_SERVER = "time.nist.gov";
    
    private static final Date time() {
        TimeInfo timeInfo = null;
		try {
	    	NTPUDPClient timeClient = new NTPUDPClient();
	        InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
			timeInfo = timeClient.getTime(inetAddress);
		} catch (IOException e) {
			ExceptionUtils.log(Constants.class, e);
		}
        long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();
        return new Date(returnTime);
    }
    
    public static void updateTimeoffset() {
    	TIME_OFFSET = time().getTime() - System.currentTimeMillis();
    }
    
    /**
     * Offset from true NTP time from the system time.
     */
    public static long TIME_OFFSET = time().getTime() - System.currentTimeMillis();
    
	/**
	 * Sleep time. Used to delay the state monitor.
	 */
	public static final long SLEEP_TIME = 200;
    
	/**
     * Database file name.
     */
    public static final String DATABASE_NAME = "solar_data.db";

    /**
     * SQLite embedded database driver class.
     */
    public static final String DRIVER = "org.sqlite.JDBC";
	
    /**
     * Database file.
     */
    public static final File DATABASE_FILE = 
    		new File(Constants.DATABASE_NAME);

    /**
     * Default size of a data point.
     */
    public static final int DEFAULT_DATA_LENGTH = 11;

    /**
     * Number of values to show in graph view.
     */
	public static final int DATA_WINDOW_SIZE = 50;

	/**
	 * Synchronize the system clock every 5 minutes with NTP time.
	 * Frequency in seconds.
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

	public static final int NUM_NIO_THREADS = 1;

	/**
	 * Application PORT.
	 */
	public static final int PORT = 55555;

	public static final String CLOSE_CMD = "CLOSE";
	
    static {
    	init();
    }

    public static long getCurrentTimeMillis() {
    	return Clock.offset(Clock.systemDefaultZone(), Duration.ofMillis(TIME_OFFSET)).millis();
    }
}
