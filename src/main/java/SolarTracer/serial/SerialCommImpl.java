package SolarTracer.serial;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import SolarTracer.gui.GuiController;
import SolarTracer.utils.Constants;
import SolarTracer.utils.DataPoint;
import SolarTracer.utils.DataRecvListener;
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
    private InputStream input = null;
    private OutputStream output = null;

    //just a boolean flag that i use for enabling
    //and disabling buttons depending on whether the program
    //is connected to a serial port or not
    private boolean bConnected = false;

    //the timeout value for connecting with the port
    final static int TIMEOUT = 2000;

    //some ascii values for for certain things
    final static int SPACE_ASCII = 32;
    final static int DASH_ASCII = 45;
    
    private LinkedList<String> stringQueue;
    private StringBuilder charStack;
    private ArrayList<DataRecvListener> listeners;
    
    /**
     * Serial Communication Constructor.
     */
    public SerialCommImpl() {
        stringQueue = new LinkedList<String>();
        charStack = new StringBuilder();
        listeners = new ArrayList<DataRecvListener>();
    }

    @Override
    public void serialEvent(SerialPortEvent ev) {
        if (ev.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
            try {
            	while (input.available() > 0) {
	                byte singleData = (byte)input.read();
	
	                if (singleData != Constants.NEWLINE_ASCII) {
	                    charStack.append(new String(new byte[]{ singleData }, Constants.CHARSET));
	                } else {
	                    charStack.append(Constants.NEWLINE);
	                    stringQueue.add(charStack.toString());
	                    updateListeners(charStack.toString());
	                    charStack = new StringBuilder();
	                }
            	}
            } catch (Exception e) {
                LOGGER.debug("Failed to read data.");
                ExceptionUtils.log(getClass(), e);
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
        listeners.parallelStream().forEach(l -> l.dataRecieved(data));
    }

    @Override
    public void writeData(String data) {
        try {
            output.write(data.getBytes());
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
    	serialPort = SerialPort.getCommPort(port);
    	bConnected = serialPort.openPort();
        input = serialPort.getInputStream();
        output = serialPort.getOutputStream();
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);
        serialPort.addDataListener(this);
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
    public List<String> getStrings() {
        ArrayList<String> returnStrings = new ArrayList<String>();
        returnStrings.addAll(stringQueue);
        stringQueue.removeAll(returnStrings);
        return Collections.synchronizedList(returnStrings);
    }

    @Override
    public void sendDataPoint(final DataPoint data) {
        writeData(data.toString());
    }

    @Override
    public void addDataRecvListener(DataRecvListener dl) {
        if (dl == null) {
            throw new NullPointerException("DataRecvListener cannot be null.");
        } else {
            listeners.add(dl);
        }
    }
    
    @Override
    public boolean removeDataRecvListener(DataRecvListener dl) {
        if (dl == null) {
            throw new NullPointerException("DataRecvListener cannot be null.");
        } else {
            return listeners.remove(dl);
        }
    }
    
	@Override
	public int getListeningEvents() {
		return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
	}
}
