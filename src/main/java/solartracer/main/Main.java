package solartracer.main;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import solartracer.anomalydetection.AnomilyDetector;
import solartracer.gui.GuiController;
import solartracer.utils.Constants;
import solartracer.utils.ShutdownHook;

/**
 * Main application class.
 * 
 * @author fred
 *
 */
public class Main extends Application {

  /** 
   * Thread coordinator.
   */
  public static final ScheduledExecutorService COORDINATOR =
      Executors.newScheduledThreadPool(Constants.NUM_THREADS);

  /** 
   * Logger.
   */
  public static final Logger LOGGER = LogManager.getLogger(Main.class);

  @Override
  public void start(Stage primaryStage) throws IOException {
    if (Constants.TRAIN) {
      AnomilyDetector anomilyDetector = new AnomilyDetector(true);
      anomilyDetector.train();
    }
    primaryStage.setTitle("Solar MPPT Tracer");
    URL fxmlLocation = getClass().getResource("/solar_tracer_ui.fxml");
    FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation, null);
    AnchorPane myPane = fxmlLoader.load();
    GuiController guiController = fxmlLoader.getController();
    primaryStage.setOnCloseRequest(guiController);
    Scene myScene = new Scene(myPane);
    myScene.setRoot(myPane);
    primaryStage.setScene(myScene);
    primaryStage.show();

  }

  /**
   * Main program execution start. 
   * 
   * @param args unused
   */
  public static void main(final String[] args) {
    addHook();
    launch();
  }

  /**
   *  Shutdown hook.
   */
  private static void addHook() {
    Runtime.getRuntime().addShutdownHook(new ShutdownHook(COORDINATOR, Thread.currentThread()));
  }
}
