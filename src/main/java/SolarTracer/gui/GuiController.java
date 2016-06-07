package SolarTracer.gui;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import SolarTracer.data.DataPoint;
import SolarTracer.data.DataPointListener;
import SolarTracer.main.Main;
import SolarTracer.networking.SolarWebServer;
import SolarTracer.serial.SerialConnection;
import SolarTracer.serial.SerialFactory;
import SolarTracer.utils.Constants;
import SolarTracer.utils.DatabaseUtils;
import SolarTracer.utils.ExceptionUtils;
import SolarTracer.utils.FreqListStringConverter;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.WindowEvent;

public class GuiController implements EventHandler<WindowEvent>, DataPointListener, Runnable {
    
    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(GuiController.class);
    
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
    private Label overChargeLabel;
    @FXML
    private Tab loadCurrentTab;
    @FXML
    private CategoryAxis pvVoltageAxis;
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

    ObservableList<String> portList;
    private int clockUpdateCtr;

    private boolean isRunning = true;
    private SolarWebServer solarServer;
    private SerialConnection serial;
    
    /**
     * GuiController Ctor.
     */
    public GuiController() {
    }
    
    @FXML
    void initialize() {
    	serial = SerialFactory.getSerial();
    	serial.addDataPointListener(this);
        sanityCheck();
        setupToggle();
        batteryLevelNumAxis.setAutoRanging(false);
        batteryLevelNumAxis.setUpperBound(16.0);
        batteryLevelNumAxis.setLowerBound(11.0);
        batteryLevelNumAxis.setTickUnit(0.5);
        batteryLevelNumAxis.setTickMarkVisible(true);
        portList = serial.getPortNames();
        setupFreqList();
        comList.setItems(portList);
        comList.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, 
              String oldValue, String newValue) {
                serial.disconnect();
                serial.connect(newValue);
                serial.addDataPointListener(solarServer);
            }
        });
        setupSeries();
        setupGraphs();
    }

    private void setupGraphs() {
        // Setup graphs
        loadGraph.getData().add(loadSeries);
        loadCurrentGraph.getData().add(loadCurrentSeries);
        batteryLevelGraph.getData().add(battLevelSeries);
        battTempGraph.getData().add(battTempSeries);
        pvVoltGraph.getData().add(pvVoltSeries);
        chargeCurrentGraph.getData().add(chargeCurrentSeries);
        chargingGraph.getData().add(chargingSeries);
    }

    private void setupSeries() {
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
    }

    private void setupFreqList() {
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
        updateFreqComboBox.setConverter(new FreqListStringConverter());
        updateFreqComboBox.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, 
                    Integer oldValue, Integer newValue) {
                arduinoSleepTime = newValue;
                append(updateFreqLabel, (newValue / 1000) + " seconds.");
            }
        });
    }

    private void setupToggle() {
        toggleGroup = new ToggleGroup();
        loadOn.setToggleGroup(toggleGroup);
        loadOff.setToggleGroup(toggleGroup);
        loadOn.setUserData("LON");
        loadOff.setUserData("LOFF");
        toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
          public void changed(ObservableValue<? extends Toggle> ov,
          Toggle toggle, Toggle newToggle) {
            if (newToggle == null) {
              log("Toggle is Null.");
              toggleGroup.selectToggle(null);
            } else {
              serial.writeData((String)newToggle.getUserData() + Constants.NEWLINE);
            }
          }
        });
    }

    /**
     * Check all fxml objects.
     */
    private void sanityCheck() {
        assert loadGraph != null : "fx:id=\"loadGraph\" was not injected: check your FXML file 'commander.fxml'.";
        assert loadCurrentTab != null : "fx:id=\"loadCurrentTab\" was not injected: check your FXML file 'commander.fxml'.";
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
    }
    
    /**
     * Append txt to the lbl provided.
     * 
     * @param lbl
     *         the label to append txt to.
     * @param txt
     *         the text to append to lbl.
     */
    private void append(Label lbl, String txt) {
        lbl.setText(lbl.getText().substring(0, lbl.getText().indexOf(':') + 1) + txt);
    }
    
    @Override
    public void handle(WindowEvent event) {
        shutdown();
    }
    
    /**
     * Shutdown the GUI.
     */
    public void shutdown() {
        LOGGER.debug("Shutting down...");
        isRunning = false;
        serial.disconnect();
        solarServer.shutdown();
        DatabaseUtils.shutdown();
        Main.COORDINATOR.shutdown();
        Platform.exit();
    }    

    /**
     * Parse and store the raw message contain in the string.
     * @param rawMsg
     *         parse and store this raw message.
     */
    public void storeData(DataPoint data) {
      DatabaseUtils.insertData(data);
      updateGraphs(data);
    }
    
	
	/**
	 * Update graphs.
	 * @param d
	 */
    private void updateGraphs(DataPoint d) {
		Platform.runLater(() -> {
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
	  if (isRunning) {
		try {
	        if (clockUpdateCtr++ >= Constants.UPDATE_CLOCK_FREQUENCY) {
	          clockUpdateCtr = 0;
	          LOGGER.debug("Updating clock.");
	          Constants.updateTimeoffset();
	        }
	        if (serial.isConnected()) {
	        	serial.writeData(new String(ByteBuffer.allocate(4).putInt(arduinoSleepTime).array(),
	                Constants.CHARSET) + Constants.NEWLINE);
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
	public void setServer(SolarWebServer solarServer) {
		this.solarServer = solarServer;
	}
	
	@Override
	public void dataPointReceived(DataPoint dataPoint) {
		storeData(dataPoint);
	}
}

