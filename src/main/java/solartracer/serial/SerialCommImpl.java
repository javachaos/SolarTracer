package solartracer.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import solartracer.data.DataPointListener;
import solartracer.utils.Constants;
import solartracer.utils.DataUtils;
import solartracer.utils.ExceptionUtils;
import solartracer.utils.SolarException;

public class SerialCommImpl implements SerialPortDataListener, SerialConnection {

  /** Logger. */
  private static final Logger LOGGER = LogManager.getLogger(SerialCommImpl.class);

  /** RS232 Parity field */
  private static final int PARITY = 0;

  /** RS232 Data Bits field */
  private static final int DATA_BITS = 8;

  /** RS232 Stop Bits field */
  private static final int STOP_BITS = 1;

  /** RS232 new write timeout. */
  private static final int WRITE_TIMEOUT = 1000;

  private SerialPort serialPort;

  // input and output streams for sending and receiving data
  private BufferedReader input;
  private BufferedWriter output;

  // just a boolean flag that i use for enabling
  // and disabling buttons depending on whether the program
  // is connected to a serial port or not
  private boolean bConnected = false;

  /**
   * List of data point listeners.
   */
  private final List<DataPointListener> dataPointListeners;

  /** Serial Communication Constructor. */
  public SerialCommImpl() {
    dataPointListeners = new ArrayList<>();
  }

  @Override
  public void serialEvent(SerialPortEvent ev) {
    if (ev.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
      try {
        updateListeners(input.readLine());
      } catch (IOException e) {
        ExceptionUtils.logSilent(getClass(), e, "Failed to read data: ");
      }
    }
    if (ev.getEventType() == SerialPort.LISTENING_EVENT_DATA_WRITTEN) {
      LOGGER.debug("All bytes were successfully transmitted!");
    }
  }

  /**
   * Update all listeners with data.
   *
   * @param data the data
   */
  private void updateListeners(String data) {
    String info =
        data + ":" + new Date(Constants.getCurrentTimeMillis()).getTime(); // Add Timestamp.
    dataPointListeners
        .forEach(l -> l.dataPointReceived(DataUtils.parseDataPoint(info)));
  }

  @Override
  public void writeData(String data) {
    try {
      output.write(data);
      output.flush();
    } catch (IOException e) {
      LOGGER.error("Failed to write data.");
      ExceptionUtils.log(getClass(), e);
    }
  }

  @Override
  public ObservableList<String> getPortNames() {
    ObservableList<String> portNames = FXCollections.observableArrayList();
    for (SerialPort s : SerialPort.getCommPorts()) {
      portNames.add(s.getSystemPortName());
    }
    return portNames;
  }

  @Override
  public void connect(String port) {
      serialPort = SerialPort.getCommPort(port);
      serialPort.setComPortTimeouts(
          SerialPort.TIMEOUT_READ_SEMI_BLOCKING, Constants.SERIAL_TIMEOUT, WRITE_TIMEOUT);
      serialPort.setParity(PARITY);
      serialPort.setNumDataBits(DATA_BITS);
      serialPort.setNumStopBits(STOP_BITS);
      serialPort.setBaudRate(Constants.BAUD_RATE);
      bConnected = serialPort.openPort();
      input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
      output = new BufferedWriter(new OutputStreamWriter(serialPort.getOutputStream()));
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
    } catch (IOException e) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Failed to close {}", serialPort.getSystemPortName());
      }
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
  public void addDataPointListener(DataPointListener dl) throws SolarException {
    if (dl == null) {
      throw new SolarException("DataPointListener cannot be null.");
    } else {
      dataPointListeners.add(dl);
    }
  }

  @Override
  public boolean removeDataPointListener(DataPointListener dl) throws SolarException {
    if (dl == null) {
      throw new SolarException("DataPointListener cannot be null.");
    } else {
      return dataPointListeners.remove(dl);
    }
  }
}
