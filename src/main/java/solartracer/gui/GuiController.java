package solartracer.gui;

import java.nio.ByteBuffer;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import solartracer.data.DataPoint;
import solartracer.data.DataPointListener;
import solartracer.main.Main;
import solartracer.serial.SerialConnection;
import solartracer.serial.SerialFactory;
import solartracer.utils.Constants;
import solartracer.utils.DatabaseUtils;
import solartracer.utils.ExceptionUtils;
import solartracer.utils.FreqListStringConverter;
import solartracer.utils.StatusUtils;

/**
 * GUI Controller class
 * 
 * @author fred
 *
 */
public class GuiController implements EventHandler<WindowEvent>, DataPointListener, Runnable {

  /** Logger. */
  public static final Logger LOGGER = LogManager.getLogger(GuiController.class);

  /** The update sleep time of the arduino device. */
  private int arduinoSleepTime = 1000;

  /**
   * FXML components
   */
  @FXML private CategoryAxis chargingAxis;
  @FXML private Label battMaxLabel;
  @FXML private LineChart<String, Float> loadGraph;
  @FXML private CategoryAxis loadCurrentAxis;
  @FXML private Label overChargeLabel;
  @FXML private Tab loadCurrentTab;
  @FXML private CategoryAxis pvVoltageAxis;
  @FXML private Label loadLabel;
  @FXML private Tab pvVoltageTab;
  @FXML private Label chargeCurrentLabel;
  @FXML private CategoryAxis batteryLevelAxis;
  @FXML private NumberAxis batteryLevelNumAxis;
  @FXML private CategoryAxis loadAxis;
  @FXML private LineChart<String, Float> pvVoltGraph;
  @FXML private Tab loadTab;
  @FXML private LineChart<String, Float> batteryLevelGraph;
  @FXML private Tab battTempTab;
  @FXML private Label chargingLabel;
  @FXML private Tab batteryLevelTab;
  @FXML private Label loadCurrentLabel;
  @FXML private ComboBox<String> comList;
  @FXML private ComboBox<Integer> updateFreqComboBox;
  @FXML private Tab chargingTab;
  @FXML private CategoryAxis batteryTempAxis;
  @FXML private Label battLevelLabel;
  @FXML private Label battFullLabel;
  @FXML private Label battTempLabel;
  @FXML private LineChart<String, Float> loadCurrentGraph;
  @FXML private LineChart<String, Float> chargeCurrentGraph;
  @FXML private LineChart<String, Float> battTempGraph;
  @FXML private LineChart<String, Float> chargingGraph;
  @FXML private CategoryAxis chargeCurrentAxis;
  @FXML private Label pvVoltLabel;
  @FXML private Label updateFreqLabel;
  @FXML private Tab chargeCurrentTab;
  @FXML private ToggleButton loadOn;
  @FXML private ToggleButton loadOff;
  @FXML private Button refreshComs;


  /**
   *  Graph series
   */
  private Series<String, Float> loadSeries;
  private Series<String, Float> loadCurrentSeries;
  private Series<String, Float> battLevelSeries;
  private Series<String, Float> battTempSeries;
  private Series<String, Float> pvVoltSeries;
  private Series<String, Float> chargingSeries;
  private Series<String, Float> chargeCurrentSeries;

  /**
   * Port list
   */
  ObservableList<String> portList;
  
  /**
   * Clock counter
   */
  private int clockUpdateCtr;

  private boolean isRunning = true;
  
  /**
   * Serial connection
   */
  private SerialConnection serial;

  /** GuiController constructor. */
  public GuiController() {
    // Unused
  }

  @FXML
  void initialize(GuiController guiController) {
    initComms();
    sanityCheck();
    setupToggle();
    batteryLevelNumAxis.setAutoRanging(false);
    batteryLevelNumAxis.setUpperBound(16.0);
    batteryLevelNumAxis.setLowerBound(11.0);
    batteryLevelNumAxis.setTickUnit(0.5);
    batteryLevelNumAxis.setTickMarkVisible(true);
    setupFreqList();
    setupSeries();
    setupGraphs();
  }

  @FXML
  private void initComms() {
    serial = SerialFactory.getSerial();
    serial.addDataPointListener(this);
    portList = serial.getPortNames();
    comList.setItems(portList);
    LOGGER.debug("Searching for comm ports, found: {}", portList);
    comList
            .valueProperty()
            .addListener(
                    (observable, oldValue, newValue) -> {
                      serial.disconnect();
                      serial.connect(newValue);
                    });
  }

  /**
   * Setup graphs.
   */
  private void setupGraphs() {
    loadGraph.setAnimated(false);
    loadCurrentGraph.setAnimated(false);
    batteryLevelGraph.setAnimated(false);
    battTempGraph.setAnimated(false);
    pvVoltGraph.setAnimated(false);
    chargeCurrentGraph.setAnimated(false);
    chargingGraph.setAnimated(false);

    loadGraph.setCreateSymbols(false);
    loadCurrentGraph.setCreateSymbols(false);
    batteryLevelGraph.setCreateSymbols(false);
    battTempGraph.setCreateSymbols(false);
    pvVoltGraph.setCreateSymbols(false);
    chargeCurrentGraph.setCreateSymbols(false);
    chargingGraph.setCreateSymbols(false);

    // Setup graphs
    loadGraph.getData().add(loadSeries);
    loadCurrentGraph.getData().add(loadCurrentSeries);
    batteryLevelGraph.getData().add(battLevelSeries);
    battTempGraph.getData().add(battTempSeries);
    pvVoltGraph.getData().add(pvVoltSeries);
    chargeCurrentGraph.getData().add(chargeCurrentSeries);
    chargingGraph.getData().add(chargingSeries);
  }

  /**
   * Setup chart series 
   */
  private void setupSeries() {
    // Setup series
    loadSeries = new XYChart.Series<>();
    loadSeries.setName("Load Series");
    loadCurrentSeries = new XYChart.Series<>();
    loadCurrentSeries.setName("Load Current Series");
    battLevelSeries = new XYChart.Series<>();
    battLevelSeries.setName("Battery Level Series");
    battTempSeries = new XYChart.Series<>();
    battTempSeries.setName("Battery Temp Series");
    pvVoltSeries = new XYChart.Series<>();
    pvVoltSeries.setName("PV Voltage Series");
    chargingSeries = new XYChart.Series<>();
    chargingSeries.setName("Charging Series");
    chargeCurrentSeries = new XYChart.Series<>();
    chargeCurrentSeries.setName("Charge Current Series");
  }

  /**
   * Initialize frequency list combo box.
   */
  private void setupFreqList() {
    ObservableList<Integer> updateFreqList = FXCollections.observableArrayList();
    updateFreqList.add(1000); // 1 second
    updateFreqList.add(2000); // 2 seconds
    updateFreqList.add(3000); // 3 seconds
    updateFreqList.add(4000); // 4 seconds
    updateFreqList.add(5000); // 5 seconds
    updateFreqList.add(10000); // 10 seconds
    updateFreqList.add(25000); // 25 seconds
    updateFreqList.add(30000); // 30 seconds
    updateFreqList.add(60000); // 1 min
    updateFreqList.add(300000); // 5 mins
    updateFreqList.add(600000); // 10 mins
    updateFreqList.add(1800000); // 30 mins
    updateFreqList.add(3600000); // 1 hour
    updateFreqList.add(18000000); // 5 hours
    updateFreqComboBox.setItems(updateFreqList);
    updateFreqComboBox.setConverter(new FreqListStringConverter());
    updateFreqComboBox
        .valueProperty()
        .addListener(
                (observable, oldValue, newValue) -> {
                  arduinoSleepTime = newValue;
                  append(updateFreqLabel, (newValue / 1000) + " seconds.");
                  sendData(
                      new String(
                          ByteBuffer.allocate(4).putInt(arduinoSleepTime).array(),
                          Constants.CHARSET));
                });
  }

  /**
   * Setup the toggle button.
   */
  private void setupToggle() {
    ToggleGroup toggleGroup;
    toggleGroup = new ToggleGroup();
    loadOn.setToggleGroup(toggleGroup);
    loadOff.setToggleGroup(toggleGroup);
    loadOn.setUserData("LON");
    loadOff.setUserData("LOFF");
    toggleGroup
        .selectedToggleProperty()
        .addListener(
                (ov, toggle, newToggle) -> {
                  if (newToggle == null) {
                    LOGGER.debug("Toggle is Null.");
                  } else {
                    sendData((String) newToggle.getUserData());
                  }
                });
  }

  /**
   * Send data over serial. Appends Newline.
   * This method does not check userData, as it is
   * a bytearray encoded string. Care must be taken therefor
   * to ensure this method is called correctly.
   *
   * @param userData the user data string
   */
  protected void sendData(String userData) {
      if (serial != null && serial.isConnected()) {
        serial.writeData(userData + Constants.NEWLINE);
        StatusUtils.showGeneralInfo("Sent data: " + userData);
      }
  }

  /** Check all fxml objects. */
  private void sanityCheck() {
    assert loadGraph != null
        : "fx:id=\"loadGraph\" was not injected: check your FXML file 'solar_tracer_ui.fxml'.";
    assert loadCurrentTab != null
        : "fx:id=\"loadCurrentTab\" was not injected: check your FXML file 'solar_tracer_ui.fxml'.";
    assert loadLabel != null
        : "fx:id=\"loadLabel\" was not injected: check your FXML file 'solar_tracer_ui.fxml'.";
    assert pvVoltageTab != null
        : "fx:id=\"pvVoltageTab\" was not injected: check your FXML file 'solar_tracer_ui.fxml'.";
    assert chargeCurrentLabel != null
        : "fx:id=\"chargeCurrentLabel\" was not injected: check your FXML file 'solar_tracer_ui.fxml'.";
    assert pvVoltGraph != null
        : "fx:id=\"pvVoltGraph\" was not injected: check your FXML file 'solar_tracer_ui.fxml'.";
    assert loadTab != null
        : "fx:id=\"loadTab\" was not injected: check your FXML file 'solar_tracer_ui.fxml'.";
    assert batteryLevelGraph != null
        : "fx:id=\"batteryLevelGraph\" was not injected: check your FXML file 'solar_tracer_ui.fxml'.";
    assert battTempTab != null
        : "fx:id=\"battTempTab\" was not injected: check your FXML file 'solar_tracer_ui.fxml'.";
    assert chargingLabel != null
        : "fx:id=\"chargingLabel\" was not injected: check your FXML file 'solar_tracer_ui.fxml'.";
    assert batteryLevelTab != null
        : "fx:id=\"batteryLevelTab\" was not injected: check your FXML file 'solar_tracer_ui.fxml'.";
    assert loadCurrentLabel != null
        : "fx:id=\"loadCurrentLabel\" was not injected: check your FXML file 'solar_tracer_ui.fxml'.";
    assert comList != null
        : "fx:id=\"comList\" was not injected: check your FXML file 'solar_tracer_ui.fxml'.";
    assert chargingTab != null
        : "fx:id=\"chargingTab\" was not injected: check your FXML file 'solar_tracer_ui.fxml'.";
    assert battLevelLabel != null
        : "fx:id=\"battLevelLabel\" was not injected: check your FXML file 'solar_tracer_ui.fxml'.";
    assert battFullLabel != null
        : "fx:id=\"battFullLabel\" was not injected: check your FXML file 'solar_tracer_ui.fxml'.";
    assert battTempLabel != null
        : "fx:id=\"battTempLabel\" was not injected: check your FXML file 'solar_tracer_ui.fxml'.";
    assert loadCurrentGraph != null
        : "fx:id=\"loadCurrentGraph\" was not injected: check your FXML file 'solar_tracer_ui.fxml'.";
    assert chargeCurrentGraph != null
        : "fx:id=\"chargeCurrentGraph\" was not injected: check your FXML file 'solar_tracer_ui.fxml'.";
    assert battTempGraph != null
        : "fx:id=\"battTempGraph\" was not injected: check your FXML file 'solar_tracer_ui.fxml'.";
    assert chargingGraph != null
        : "fx:id=\"chargingGraph\" was not injected: check your FXML file 'solar_tracer_ui.fxml'.";
    assert pvVoltLabel != null
        : "fx:id=\"pvVoltLabel\" was not injected: check your FXML file 'solar_tracer_ui.fxml'.";
    assert chargeCurrentTab != null
        : "fx:id=\"chargeCurrentTab\" was not injected: check your FXML file 'solar_tracer_ui.fxml'.";
  }

  /**
   * Append txt to the lbl provided.
   *
   * @param lbl the label to append txt to.
   * @param txt the text to append to lbl.
   */
  private void append(Label lbl, String txt) {
    lbl.setText(lbl.getText().substring(0, lbl.getText().indexOf(':') + 1) + txt);
  }

  @Override
  public void handle(WindowEvent event) {
    shutdown();
  }

  /** Shutdown the GUI. */
  public void shutdown() {
    try {
      LOGGER.debug("Shutting down...");
      isRunning = false;
      if (serial != null) {
        serial.disconnect();
      }
      DatabaseUtils.shutdown();
    } finally {
      Main.COORDINATOR.shutdown();
      Platform.exit();
    }
  }

  /**
   * Parse and store the raw message contain in the string.
   *
   * @param data parse and store this raw message.
   */
  public void storeData(DataPoint data) {
    DatabaseUtils.insertData(data);
    updateGraphs(data);
  }

  /**
   * Update graphs.
   *
   * @param d the datapoint
   */
  private void updateGraphs(DataPoint d) {
    Platform.runLater(
        () -> {
          append(loadLabel, String.valueOf(d.getLoadOnoff()));
          append(loadCurrentLabel, String.valueOf(d.getLoadCurrent()));
          append(battLevelLabel, String.valueOf(d.getBatteryVoltage()));
          append(battFullLabel, String.valueOf(d.getBatteryFull()));
          append(battTempLabel, String.valueOf(d.getBatteryTemp()));
          append(pvVoltLabel, String.valueOf(d.getPvVoltage()));
          append(chargingLabel, String.valueOf(d.getCharging()));
          append(chargeCurrentLabel, String.valueOf(d.getChargeCurrent()));
          append(battMaxLabel, String.valueOf(d.getBatteryMax()));
          append(overChargeLabel, String.valueOf(d.getOverDischarge()));
          loadSeries.getData().add(new XYChart.Data<>(d.getTimeFormatted(), d.getLoadOnoff()));
          if (loadSeries.getData().size() >= Constants.DATA_WINDOW_SIZE) {
            loadSeries.getData().remove(0); // Remove the first element.
          }
          loadCurrentSeries
              .getData()
              .add(new XYChart.Data<>(d.getTimeFormatted(), d.getLoadCurrent()));
          if (loadCurrentSeries.getData().size() >= Constants.DATA_WINDOW_SIZE) {
            loadCurrentSeries.getData().remove(0); // Remove the first element.
          }
          battLevelSeries
              .getData()
              .add(new XYChart.Data<>(d.getTimeFormatted(), d.getBatteryVoltage()));
          if (battLevelSeries.getData().size() >= Constants.DATA_WINDOW_SIZE) {
            battLevelSeries.getData().remove(0); // Remove the first element.
          }
          battTempSeries
              .getData()
              .add(new XYChart.Data<>(d.getTimeFormatted(), d.getBatteryTemp()));
          if (battTempSeries.getData().size() >= Constants.DATA_WINDOW_SIZE) {
            battTempSeries.getData().remove(0); // Remove the first element.
          }
          pvVoltSeries.getData().add(new XYChart.Data<>(d.getTimeFormatted(), d.getPvVoltage()));
          if (pvVoltSeries.getData().size() >= Constants.DATA_WINDOW_SIZE) {
            pvVoltSeries.getData().remove(0); // Remove the first element.
          }
          chargingSeries.getData().add(new XYChart.Data<>(d.getTimeFormatted(), d.getCharging()));
          if (chargingSeries.getData().size() >= Constants.DATA_WINDOW_SIZE) {
            chargingSeries.getData().remove(0); // Remove the first element.
          }
          chargeCurrentSeries
              .getData()
              .add(new XYChart.Data<>(d.getTimeFormatted(), d.getChargeCurrent()));
          if (chargeCurrentSeries.getData().size() >= Constants.DATA_WINDOW_SIZE) {
            chargeCurrentSeries.getData().remove(0); // Remove the first element.
          }
        });
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
      } catch (Exception t1) {
        ExceptionUtils.log(getClass(), t1);
      }
    }
  }

  @Override
  public void dataPointReceived(DataPoint dataPoint) {
    storeData(dataPoint);
  }
}
