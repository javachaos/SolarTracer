package SolarTracer.serial;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import SolarTracer.data.DataPointListener;
import SolarTracer.gui.GuiController;
import SolarTracer.utils.Constants;
import SolarTracer.utils.DataUtils;
import SolarTracer.utils.ExceptionUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SerialCommImpl implements SerialPortDataListener, SerialConnection {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GuiController.class);

    private SerialPort[] serialPorts = SerialPort.getCommPorts();
    private SerialPort serialPort;

    //input and output streams for sending and receiving data
    private BufferedReader input;
    private BufferedWriter output;

    //just a boolean flag that i use for enabling
    //and disabling buttons depending on whether the program
    //is connected to a serial port or not
    private boolean bConnected = false;

    //the timeout value for connecting with the port
    final static int TIMEOUT = 2000;

    //some ascii values for for certain things
    final static int SPACE_ASCII = 32;
    final static int DASH_ASCII = 45;
    
	private ArrayList<DataPointListener> dataPointListeners;
    
    /**
     * Serial Communication Constructor.
     */
    public SerialCommImpl() {
        dataPointListeners = new ArrayList<DataPointListener>();
    }

    @Override
    public void serialEvent(SerialPortEvent ev) {
        if (ev.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
            try {
            	updateListeners(input.readLine());
            } catch (Exception e) {
                LOGGER.error("Failed to read data. (" + e.getMessage() + ").");
            }
        }
        if (ev.getEventType() == SerialPort.LISTENING_EVENT_DATA_WRITTEN) {
            LOGGER.debug("All bytes were successfully transmitted!");
        }
    }

    /**
     * Update all listeners with data.
     * @param data
     */
    private void updateListeners(String data) {
    	String info = data + ":" + new Date(Constants.getCurrentTimeMillis()).getTime();// Add Timestamp.
        dataPointListeners.parallelStream().forEach(l -> l.dataPointReceived(DataUtils.parseDataPoint(info)));
    }

    @Override
    public void writeData(String data) {
        try {
        	output.write(data);
            output.flush();
        } catch (Exception e) {
            LOGGER.error("Failed to write data.");
            ExceptionUtils.log(getClass(), e);
        }
    }
    
    @Override
    public ObservableList<String> getPortNames() {
    	ObservableList<String> portNames = FXCollections.observableArrayList();
    	for( SerialPort s : serialPorts) {
    	    portNames.add(s.getSystemPortName());
    	}
        return portNames;
    }
    

    @Override
    public void connect(String port) {
    	try {
	    	serialPort = SerialPort.getCommPort(port);
	    	serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, Constants.SERIAL_TIMEOUT, 0);
	        serialPort.setParity(0);
	        serialPort.setNumDataBits(8);
	        serialPort.setNumStopBits(1);
	        serialPort.setBaudRate(Constants.BAUD_RATE);
	    	bConnected = serialPort.openPort();
	        input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
	        output = new BufferedWriter(new OutputStreamWriter(serialPort.getOutputStream()));
	        serialPort.addDataListener(this);
    	} catch (Exception e) {
    		LOGGER.error("Failed to connect.");
    		ExceptionUtils.log(getClass(), e);
    	}
    }
    
    @Override
    public void disconnect() {
        try {
        	if (isConnected()) {
	            serialPort.removeDataListener();
	            bConnected = !serialPort.closePort();
	            input.close();
	            output.close();
	            LOGGER.info("Serial Disconnected.");
        	}
        } catch (Exception e) {
            LOGGER.debug("Failed to close " + serialPort.getSystemPortName());
        }
    }

    @Override
    public boolean isConnected() {
        return bConnected;
    }
    
	@Override
	public int getListeningEvents() {
		return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
	}

	@Override
	public void addDataPointListener(DataPointListener dl) {
        if (dl == null) {
            throw new NullPointerException("DataPointListener cannot be null.");
        } else {
            dataPointListeners.add(dl);
        }
	}

	@Override
	public boolean removeDataPointListener(DataPointListener dl) {
        if (dl == null) {
            throw new NullPointerException("DataPointListener cannot be null.");
        } else {
            return dataPointListeners.remove(dl);
        }
	}
}
