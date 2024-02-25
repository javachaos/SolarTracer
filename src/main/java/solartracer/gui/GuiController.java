package solartracer.gui;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import solartracer.data.DataPoint;
import solartracer.data.DataPointListener;
import solartracer.net.MQTTClient;
import solartracer.net.MQTTServer;
import solartracer.serial.SerialConnection;
import solartracer.serial.SerialFactory;
import solartracer.serial.ShutdownListener;
import solartracer.utils.*;

import static solartracer.main.Main.COORDINATOR;

/**
 * GUI Controller class
 * 
 * @author fred
 *
 */
public class GuiController implements EventHandler<WindowEvent>, DataPointListener, ShutdownListener {

  /** Logger. */
  public static final Logger LOGGER = LogManager.getLogger(GuiController.class);
  private static final int MAX_LINES = 25;

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
  @FXML private Button hideGraphBtn;
  @FXML private TabPane graphPane;
  @FXML private ToggleGroup toggleGroup;
  @FXML private Button ipconnect;
  @FXML private TextField ipAddress;
  @FXML private Button startserver;
  @FXML private Button stopserver;
  @FXML private Circle statusCircle;
  @FXML private TextArea loggingTextArea;
  @FXML private Tab logTab;
  @FXML private ScrollPane logScrollPane;


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
  List<ShutdownListener> shutdownListeners;

  /**
   * Serial connection
   */
  private SerialConnection serial;
  private MQTTServer mqttServer;
  private MQTTClient mqttClient;

  private SQLiteDatabase database;

  /** GuiController constructor. */
  public GuiController() {
      // Unused
  }

  @FXML
  void initialize() {
    LOGGER.debug("Application startup.");
    database = new SQLiteDatabase();
    if (!database.databaseExists()) {
      database.createTables();
      LOGGER.debug("Created new database file.");
    }
    initComms();
    sanityCheck();
    Pattern newline = Pattern.compile(System.lineSeparator());
    loggingTextArea.setTextFormatter(new TextFormatter<>(change ->  {
      String newText = change.getControlNewText();
      // count lines in proposed new text:
      Matcher matcher = newline.matcher(newText);
      int lines = 1 ;
      while (matcher.find()) lines++;
      // if there aren't too many lines just return the changed unmodified:
      if (lines <= MAX_LINES) return change ;

      // drop first (lines - 50) lines and replace all text
      // (there's no other way AFAIK to drop text at the beginning
      // and replace it at the end):
      int linesToDrop = lines - MAX_LINES ;
      int index = 0 ;
      for (int i = 0 ; i < linesToDrop ; i++) {
        index = newText.indexOf(System.lineSeparator(), index);
      }
      change.setRange(0, change.getControlText().length());
      change.setText(newText.substring(index+1));
      return change;
    }));
    LogManager.getContext().putObject("textArea", loggingTextArea);
    setupToggle();
    batteryLevelNumAxis.setAutoRanging(false);
    batteryLevelNumAxis.setUpperBound(Constants.BATT_LEVEL_UPPER_BOUND);
    batteryLevelNumAxis.setLowerBound(Constants.BATT_LEVEL_LOWER_BOUND);
    batteryLevelNumAxis.setTickUnit(Constants.BATT_LEVEL_TICK_UNIT);
    batteryLevelNumAxis.setTickMarkVisible(true);
    setupFreqList();
    setupSeries();
    setupGraphs();
    hideGraphBtn.setOnAction(e -> graphPane.setVisible(!graphPane.isVisible()));
    mqttServer = new MQTTServer(this);
    mqttClient = new MQTTClient(this);
    serial.addDataPointListener(mqttServer);
    shutdownListeners = new ArrayList<>();
    shutdownListeners.add(this);
    shutdownListeners.add(mqttClient);
    shutdownListeners.add(mqttServer);
    shutdownListeners.add(serial);
    shutdownListeners.add(database);
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
    ObservableList<Integer> updateFreqList = getFreqList();
    updateFreqComboBox.setItems(updateFreqList);
    updateFreqComboBox.setConverter(new FreqListStringConverter());
    updateFreqComboBox
        .valueProperty()
        .addListener(
                (observable, oldValue, newValue) -> {
                  arduinoSleepTime = newValue;
                  append(updateFreqLabel, (newValue / Constants.MS_PER_SEC) + " seconds.");
                  sendData(
                      new String(
                          ByteBuffer.allocate(Constants.BYTES_PER_INT).putInt(arduinoSleepTime).array(),
                          Constants.CHARSET));
                });
  }

  private static ObservableList<Integer> getFreqList() {
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
    return updateFreqList;
  }

  /**
   * Setup the toggle button.
   */
  private void setupToggle() {
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
    assert loggingTextArea != null;
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
    if (event.getEventType() == WindowEvent.WINDOW_CLOSE_REQUEST) {
      LOGGER.debug("Window close event received.");
      shutdownListeners.forEach(
              s -> {
                LOGGER.debug("Shutting down {}.", s.getClass().getName());
                s.shutdown();
              });
    } else {
      LOGGER.error("Unexpected window event.");
    }
  }

  /** Shutdown the GUI. */
  public void shutdown() {
      COORDINATOR.shutdown();
      Platform.exit();
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
  public void dataPointReceived(DataPoint dataPoint) {
    database.insertData(dataPoint);
    updateGraphs(dataPoint);
  }

  @FXML
  public void connect() {
    if (serial != null) {
      serial.disconnect();//prevent infinite recursion
    }
    mqttClient.connect(ipAddress.getText());
  }

  @FXML
  public void startServer() {
    mqttServer.connect();
  }

  @FXML
  public void stopServer() {
    mqttServer.shutdown();
  }

  public Circle getCircle() {
    return statusCircle;
  }

}
