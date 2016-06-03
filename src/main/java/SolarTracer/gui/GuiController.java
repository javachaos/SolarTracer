package SolarTracer.gui;

import static jssc.SerialPort.MASK_RXCHAR;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import SolarTracer.main.Main;
import SolarTracer.networking.SolarClient;
import SolarTracer.networking.SolarServer;
import SolarTracer.utils.Constants;
import SolarTracer.utils.DataPoint;
import SolarTracer.utils.DataUtils;
import SolarTracer.utils.DatabaseUtils;
import SolarTracer.utils.ExceptionUtils;
import SolarTracer.utils.SolarException;
import SolarTracer.utils.StatusUtils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class GuiController implements EventHandler<WindowEvent>, SerialPortEventListener, Runnable {


	/**
	 * Logger.
	 */
	public static final Logger LOGGER = LoggerFactory.getLogger(GuiController.class);

	  
	/**
	 * IP Regex.
	 */
	private static final String IPADDRESS_PATTERN =
	    "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
	        + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	
	/**
	 * The update sleep time of the arduino device.
	 */
    private int arduinoSleepTime = 1000;

	@FXML
    private CategoryAxis chargingAxis;

    @FXML
    private Label battMaxLabel;

    @FXML
    private LineChart<String, Float> loadGraph;

    @FXML
    private CategoryAxis loadCurrentAxis;

    @FXML
    private TextField ipField;

    @FXML
    private Label overChargeLabel;

    @FXML
    private Tab loadCurrentTab;

    @FXML
    private CategoryAxis pvVoltageAxis;

    @FXML
    private Button connectButton;

    @FXML
    private Label loadLabel;

    @FXML
    private Tab pvVoltageTab;

    @FXML
    private Label chargeCurrentLabel;

    @FXML
    private CategoryAxis batteryLevelAxis;
    
    @FXML
    private NumberAxis batteryLevelNumAxis;

    @FXML
    private CategoryAxis loadAxis;

    @FXML
    private LineChart<String, Float> pvVoltGraph;

    @FXML
    private Tab loadTab;

    @FXML
    private LineChart<String, Float> batteryLevelGraph;

    @FXML
    private Tab battTempTab;

    @FXML
    private Label chargingLabel;

    @FXML
    private Tab batteryLevelTab;

    @FXML
    private Label loadCurrentLabel;

    @FXML
    private ComboBox<String> comList;
    
    @FXML
    private ComboBox<Integer> updateFreqComboBox;

    @FXML
    private Tab chargingTab;

    @FXML
    private CategoryAxis batteryTempAxis;

    @FXML
    private Label battLevelLabel;

    @FXML
    private Label battFullLabel;

    @FXML
    private Label battTempLabel;

    @FXML
    private LineChart<String, Float> loadCurrentGraph;

    @FXML
    private LineChart<String, Float> chargeCurrentGraph;

    @FXML
    private LineChart<String, Float> battTempGraph;

    @FXML
    private LineChart<String, Float> chargingGraph;

    @FXML
    private CategoryAxis chargeCurrentAxis;

    @FXML
    private Label pvVoltLabel;
    
    @FXML
    private Label updateFreqLabel;

    @FXML
    private Tab chargeCurrentTab;
    
    @FXML
    private ToggleButton loadOn;
    
    @FXML
    private ToggleButton loadOff;
    
    private ToggleGroup toggleGroup;
    
    // Graph series
    private Series<String, Float> loadSeries;
	private Series<String, Float> loadCurrentSeries;
	private Series<String, Float> battLevelSeries;
	private Series<String, Float> battTempSeries;
	private Series<String, Float> pvVoltSeries;
	private Series<String, Float> chargingSeries;
	private Series<String, Float> chargeCurrentSeries;
	private ObservableList<Integer> updateFreqList;
    
    @FXML
    void initialize() {
    	toggleGroup = new ToggleGroup();
        assert loadGraph != null : "fx:id=\"loadGraph\" was not injected: check your FXML file 'commander.fxml'.";
        assert ipField != null : "fx:id=\"ipField\" was not injected: check your FXML file 'commander.fxml'.";
        assert loadCurrentTab != null : "fx:id=\"loadCurrentTab\" was not injected: check your FXML file 'commander.fxml'.";
        assert connectButton != null : "fx:id=\"connectButton\" was not injected: check your FXML file 'commander.fxml'.";
        assert loadLabel != null : "fx:id=\"loadLabel\" was not injected: check your FXML file 'commander.fxml'.";
        assert pvVoltageTab != null : "fx:id=\"pvVoltageTab\" was not injected: check your FXML file 'commander.fxml'.";
        assert chargeCurrentLabel != null : "fx:id=\"chargeCurrentLabel\" was not injected: check your FXML file 'commander.fxml'.";
        assert pvVoltGraph != null : "fx:id=\"pvVoltGraph\" was not injected: check your FXML file 'commander.fxml'.";
        assert loadTab != null : "fx:id=\"loadTab\" was not injected: check your FXML file 'commander.fxml'.";
        assert batteryLevelGraph != null : "fx:id=\"batteryLevelGraph\" was not injected: check your FXML file 'commander.fxml'.";
        assert battTempTab != null : "fx:id=\"battTempTab\" was not injected: check your FXML file 'commander.fxml'.";
        assert chargingLabel != null : "fx:id=\"chargingLabel\" was not injected: check your FXML file 'commander.fxml'.";
        assert batteryLevelTab != null : "fx:id=\"batteryLevelTab\" was not injected: check your FXML file 'commander.fxml'.";
        assert loadCurrentLabel != null : "fx:id=\"loadCurrentLabel\" was not injected: check your FXML file 'commander.fxml'.";
        assert comList != null : "fx:id=\"comList\" was not injected: check your FXML file 'commander.fxml'.";
        assert chargingTab != null : "fx:id=\"chargingTab\" was not injected: check your FXML file 'commander.fxml'.";
        assert battLevelLabel != null : "fx:id=\"battLevelLabel\" was not injected: check your FXML file 'commander.fxml'.";
        assert battFullLabel != null : "fx:id=\"battFullLabel\" was not injected: check your FXML file 'commander.fxml'.";
        assert battTempLabel != null : "fx:id=\"battTempLabel\" was not injected: check your FXML file 'commander.fxml'.";
        assert loadCurrentGraph != null : "fx:id=\"loadCurrentGraph\" was not injected: check your FXML file 'commander.fxml'.";
        assert chargeCurrentGraph != null : "fx:id=\"chargeCurrentGraph\" was not injected: check your FXML file 'commander.fxml'.";
        assert battTempGraph != null : "fx:id=\"battTempGraph\" was not injected: check your FXML file 'commander.fxml'.";
        assert chargingGraph != null : "fx:id=\"chargingGraph\" was not injected: check your FXML file 'commander.fxml'.";
        assert pvVoltLabel != null : "fx:id=\"pvVoltLabel\" was not injected: check your FXML file 'commander.fxml'.";
        assert chargeCurrentTab != null : "fx:id=\"chargeCurrentTab\" was not injected: check your FXML file 'commander.fxml'.";

        loadOn.setToggleGroup(toggleGroup);
        loadOff.setToggleGroup(toggleGroup);

        loadOn.setUserData("LON");
        loadOff.setUserData("LOFF");
        
        toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            public void changed(ObservableValue<? extends Toggle> ov,
                Toggle toggle, Toggle new_toggle) {
                    if (new_toggle == null) {
                    	log("Toggle is Null.");
                    } else {
                    	sendCommand((String)new_toggle.getUserData());
                    }
                 }
        });

        batteryLevelNumAxis.setAutoRanging(false);
        batteryLevelNumAxis.setUpperBound(16.0);
        batteryLevelNumAxis.setLowerBound(11.0);
        batteryLevelNumAxis.setTickUnit(0.5);
        batteryLevelNumAxis.setTickMarkVisible(true);

        detectPort();

        updateFreqList = FXCollections.observableArrayList();
        updateFreqList.add(1000);    // 1 second
        updateFreqList.add(2000);    // 2 seconds
        updateFreqList.add(3000);    // 3 seconds
        updateFreqList.add(4000);    // 4 seconds
        updateFreqList.add(5000);    // 5 seconds
        updateFreqList.add(10000);   // 10 seconds
        updateFreqList.add(25000);   // 25 seconds
        updateFreqList.add(30000);   // 30 seconds
        updateFreqList.add(60000);   // 1 min
        updateFreqList.add(300000);  // 5 mins
        updateFreqList.add(600000);  // 10 mins
        updateFreqList.add(3600000); // 1 hour
        updateFreqList.add(18000000);// 5 hours

        updateFreqComboBox.setItems(updateFreqList);
        updateFreqComboBox.setConverter(new StringConverter<Integer>() {
          @Override
          public String toString(Integer integer) {
        	  switch (integer) {
        	  case 1000:
        		  return "1 second";
        	  case 2000:
        		  return "2 seconds";
        	  case 3000:
        		  return "3 seconds";
        	  case 4000:
        		  return "4 seconds";
        	  case 5000:
        		  return "5 seconds";
        	  case 10000:
        		  return "10 seconds";
        	  case 25000:
        		  return "25 seconds";
        	  case 30000:
        		  return "30 seconds";
        	  case 60000:
        		  return "1 min";
        	  case 300000:
        		  return "5 minutes";
        	  case 600000:
        		  return "10 minutes";
        	  case 1800000:
        		  return "30 minutes";
        	  case 3600000:
        		  return "1 hour";
        	  case 18000000:
        		  return "5 hours";
              default:
        		  return "";
        	  }
          }
          
          @Override 
          public Integer fromString(String data) {
        	  switch (data) {
        	  case "1 second":
        		  return 1000;
        	  case "2 seconds":
        		  return 2000;
        	  case "3 seconds":
        		  return 3000;
        	  case "4 seconds":
        		  return 4000;
        	  case "5 seconds":
        		  return 5000;
        	  case "10 seconds":
        		  return 10000;
        	  case "25 seconds":
        		  return 25000;
        	  case "30 seconds":
        		  return 30000;
        	  case "1 min":
        		  return 60000;
        	  case "5 minutes":
        		  return 300000;
        	  case "10 minutes":
        		  return 600000;
        	  case "30 minutes":
        		  return 1800000;
        	  case "1 hour":
        		  return 3600000;
        	  case "5 hours":
        		  return 18000000;
              default:
        		  return 0;
        	  }
          }
        });

        updateFreqComboBox.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, 
            		Integer oldValue, Integer newValue) {
            	arduinoSleepTime = newValue;
            	append(updateFreqLabel, (newValue / 1000) + " seconds.");
            }
        });
        
        comList.setItems(portList);
        comList.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, 
              String oldValue, String newValue) {
                disconnectArduino();
                connectArduino(newValue);
            }
        });

        // Setup series
        loadSeries = new XYChart.Series<String, Float>();
        loadSeries.setName("Load Series");
        loadCurrentSeries = new XYChart.Series<String, Float>();
        loadCurrentSeries.setName("Load Current Series");
        battLevelSeries = new XYChart.Series<String, Float>();
        battLevelSeries.setName("Battery Level Series");
        battTempSeries = new XYChart.Series<String, Float>();
        battTempSeries.setName("Battery Temp Series");
        pvVoltSeries = new XYChart.Series<String, Float>();
        pvVoltSeries.setName("PV Voltage Series");
        chargingSeries = new XYChart.Series<String, Float>();
        chargingSeries.setName("Charging Series");
        chargeCurrentSeries = new XYChart.Series<String, Float>();
        chargeCurrentSeries.setName("Charge Current Series");

        // Setup graphs
        loadGraph.getData().add(loadSeries);
        loadCurrentGraph.getData().add(loadCurrentSeries);
        batteryLevelGraph.getData().add(battLevelSeries);
        battTempGraph.getData().add(battTempSeries);
        pvVoltGraph.getData().add(pvVoltSeries);
        chargeCurrentGraph.getData().add(chargeCurrentSeries);
        chargingGraph.getData().add(chargingSeries);
    }

    public GuiController() {
    }

    SerialPort arduinoPort = null;
    ObservableList<String> portList;
	private StringBuilder info = new StringBuilder();
	private String data = "";

	private int clockUpdateCtr;

	private boolean isRunning = true;

	private SolarClient client;

	private SolarServer solarServer;

    private void detectPort() {
        portList = FXCollections.observableArrayList();
        String[] serialPortNames = SerialPortList.getPortNames();
        for(final String name : serialPortNames){
            log("Adding " + name + " to port list." + System.lineSeparator());
            portList.add(name);
        }
    }
    
    /**
     * Send a command to arduino controller.
     * @param cmd
     */
	public void sendCommand(String cmd) {
		if (client != null && client.isConnected()) {
		//We're connected via IP.
			client.addOutMessage(cmd);
		} else if (arduinoPort != null && arduinoPort.isOpened()) {
		//We're connected directly via Serial Port.
			try {
				if (arduinoPort.writeString(cmd + '\n')) {
					StatusUtils.showGeneralInfo("Command sent successfully.");
				} else {
					StatusUtils.showGeneralInfo("Command send failed!");
				}
			} catch (SerialPortException e) {
				ExceptionUtils.log(getClass(), e);
			}
		} else {
			ExceptionUtils.showAlert("Could not send command, we're not connected.");
			toggleGroup.selectToggle(null);
		}
	}
    
    /**
     * Connect event.
     * 
     * @param event the action event.
     */
    @FXML
    protected void connect(final ActionEvent event) {
    	disconnectArduino();
    	String ip = ipField.getText();
        if (ip.matches(IPADDRESS_PATTERN)) {
          client = new SolarClient(ip, Constants.PORT);
          LOGGER.debug("Connecting to " + ip);
          Main.COORDINATOR.submit(client);
        } else {
          LOGGER.debug("Cannot connect non valid IP address.");
          ExceptionUtils.showAlert(new SolarException("Cannot connect non valid IP address."));
        }
    }
    
    protected void disconnect() {
    	if(client != null && client.isConnected()) {
    		client.shutdown();
    	}
    }
    
    public boolean connectArduino(final String port) {
    	disconnect();
    	log("Connecting to Arduino...");
        boolean success = false;
        arduinoPort = new SerialPort(port);
        try {
        	arduinoPort.openPort();
        	arduinoPort.setParams(
                    SerialPort.BAUDRATE_57600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
        	arduinoPort.setEventsMask(MASK_RXCHAR);
        	arduinoPort.addEventListener(this);
            success = true;
        } catch (SerialPortException ex) {
            ExceptionUtils.log(getClass(), ex);
        }
        return success;
    }
    
    private void append(Label lbl, String txt) {
    	lbl.setText(lbl.getText().substring(0, lbl.getText().indexOf(':') + 1) + txt);
    }
    
    public void disconnectArduino() {
        if(arduinoPort != null){
            try {
                arduinoPort.removeEventListener();
                if(arduinoPort.isOpened()){
                    arduinoPort.closePort();
                }
            } catch (SerialPortException ex) {
                ExceptionUtils.log(getClass(), ex);
            }
        }
    }
    
	@Override
	public void handle(WindowEvent event) {
		shutdown();
	}
	
	public void shutdown() {
		LOGGER.debug("Shutting down...");
		isRunning = false;
		disconnectArduino();
		DatabaseUtils.shutdown();
		Main.COORDINATOR.shutdown();
		Platform.exit();
	}

	@Override
	public void serialEvent(SerialPortEvent serialPortEvent) {
        if(serialPortEvent.isRXCHAR()) {
          try {
              String s = arduinoPort.readString(serialPortEvent.getEventValue());
  	          log("Raw: " + s);
  	          sendCommand(
  	        		  new String(ByteBuffer.allocate(4).putInt(arduinoSleepTime).array(),
  	        				  Constants.CHARSET));
              if (solarServer != null) {
                  solarServer.sendMessage(s);
              }
              submitMessage(s);
          } catch (SerialPortException ex) {
              ExceptionUtils.log(getClass(), ex);
          }
        }
    }

	public void submitMessage(String rawMsg) {
  	  String[] tmp = null;
	  if (rawMsg.contains(System.lineSeparator())) {
        tmp = rawMsg.split(System.lineSeparator());
        if (tmp != null && tmp.length > 0) {
          info.append(tmp[0]);
          data = info.toString();
          info = new StringBuilder();
          for (int j = 0; j < tmp.length; j++) {
            if (j > 1) {
              info.append(tmp[j]);
              info.append("#");
            }
          }
          data += ":" + new Date(Constants.getCurrentTimeMillis()).getTime();//Add time...
          DatabaseUtils.insertData(data);
          if (data.split(":").length == Constants.DEFAULT_DATA_LENGTH) {
            updateGraphs(data);
          }
      	}
      } else {
        info.append(rawMsg);
      }
	}
	
	private void updateGraphs(String dataPoint) {
		Platform.runLater(() -> {
			DataPoint d = DataUtils.parseDataPoint(dataPoint);
	        append(loadLabel, d.getLoadOnoff()+"");
	        append(loadCurrentLabel, d.getLoadCurrent()+"");
	        append(battLevelLabel, d.getBatteryVoltage()+"");
	        append(battFullLabel, d.getBatteryFull()+"");
	        append(battTempLabel, d.getBatteryTemp()+"");
	        append(pvVoltLabel, d.getPvVoltage()+"");
	        append(chargingLabel, d.getCharging()+"");
	        append(chargeCurrentLabel, d.getChargeCurrent()+"");
	        append(battMaxLabel, d.getBatteryMax()+"");
	        append(overChargeLabel, d.getOverDischarge()+"");
	        loadSeries.getData().add(new XYChart.Data<String, Float>(d.getTimeFormatted(), d.getLoadOnoff()));
	        if (loadSeries.getData().size() >= Constants.DATA_WINDOW_SIZE) {
	        	loadSeries.getData().remove(0);//Remove the first element.
	        }
	        loadCurrentSeries.getData().add(new XYChart.Data<String, Float>(d.getTimeFormatted(), d.getLoadCurrent()));
	        if (loadCurrentSeries.getData().size() >= Constants.DATA_WINDOW_SIZE) {
	        	loadCurrentSeries.getData().remove(0);//Remove the first element.
	        }
	        battLevelSeries.getData().add(new XYChart.Data<String, Float>(d.getTimeFormatted(), d.getBatteryVoltage()));
	        if (battLevelSeries.getData().size() >= Constants.DATA_WINDOW_SIZE) {
	        	battLevelSeries.getData().remove(0);//Remove the first element.
	        }
	        battTempSeries.getData().add(new XYChart.Data<String, Float>(d.getTimeFormatted(), d.getBatteryTemp()));
	        if (battTempSeries.getData().size() >= Constants.DATA_WINDOW_SIZE) {
	        	battTempSeries.getData().remove(0);//Remove the first element.
	        }
	        pvVoltSeries.getData().add(new XYChart.Data<String, Float>(d.getTimeFormatted(), d.getPvVoltage()));
	        if (pvVoltSeries.getData().size() >= Constants.DATA_WINDOW_SIZE) {
	        	pvVoltSeries.getData().remove(0);//Remove the first element.
	        }
	        chargingSeries.getData().add(new XYChart.Data<String, Float>(d.getTimeFormatted(), d.getCharging()));
	        if (chargingSeries.getData().size() >= Constants.DATA_WINDOW_SIZE) {
	        	chargingSeries.getData().remove(0);//Remove the first element.
	        }
	        chargeCurrentSeries.getData().add(new XYChart.Data<String, Float>(d.getTimeFormatted(), d.getChargeCurrent()));
	        if (chargeCurrentSeries.getData().size() >= Constants.DATA_WINDOW_SIZE) {
	        	chargeCurrentSeries.getData().remove(0);//Remove the first element.
	        }
	    });
	}
	
	private void log(String s) {
		LOGGER.debug(s);
	}

	@Override
	public void run() {
	  if (isRunning ) {
		try {
        if (clockUpdateCtr++ >= Constants.UPDATE_CLOCK_FREQUENCY) {
          clockUpdateCtr = 0;
          LOGGER.debug("Updating clock.");
          Constants.updateTimeoffset();
        }
        if (client != null && client.isConnected()) {
          submitMessage(client.getNextMessage());
        }
        LOGGER.debug("GUI Heartbeat: " + clockUpdateCtr);
		} catch (Throwable t1) {
		  ExceptionUtils.log(getClass(), t1);
		}
	  }
	}

	/**
	 * Set the server.
	 * @param solarServer
	 */
	public void setServer(SolarServer solarServer) {
		this.solarServer = solarServer;
	}
}

