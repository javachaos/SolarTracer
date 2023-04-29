package solartracer.main;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import solartracer.gui.GuiController;
import solartracer.utils.Constants;
import solartracer.utils.DatabaseUtils;
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
    primaryStage.setTitle("Solar MPPT Tracer");
    URL fxmlLocation = getClass().getResource("/commander.fxml");
    FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation, null);
    AnchorPane myPane = fxmlLoader.load();
    GuiController guiController = fxmlLoader.getController();
    COORDINATOR.scheduleAtFixedRate(
        guiController, 0, Constants.GUI_SLEEPTIME, TimeUnit.MILLISECONDS);
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
    LOGGER.debug("Application startup.");
    if (!DatabaseUtils.databaseExists()) {
      DatabaseUtils.createTables();
      LOGGER.debug("Created new database file.");
    }
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
