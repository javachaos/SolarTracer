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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solartracer.gui.GuiController;
import solartracer.networking.SolarWebServer;
import solartracer.utils.Constants;
import solartracer.utils.DatabaseUtils;
import solartracer.utils.ShutdownHook;

public class Main extends Application {

  /** Thread coordinator. */
  public static final ScheduledExecutorService COORDINATOR =
      Executors.newScheduledThreadPool(Constants.NUM_THREADS);

  /** Logger. */
  public static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

  @Override
  public void start(Stage primaryStage) throws IOException {
    primaryStage.setTitle("Solar MPPT Tracer");
    URL fxmlLocation = getClass().getResource("/commander.fxml");
    FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation, null);
    AnchorPane myPane = fxmlLoader.load();
    GuiController guiController = fxmlLoader.getController();
    COORDINATOR.scheduleAtFixedRate(
        guiController, 0, Constants.GUI_SLEEPTIME, TimeUnit.MILLISECONDS);
    SolarWebServer server = new SolarWebServer();
    COORDINATOR.schedule(server, 0, TimeUnit.SECONDS);
    guiController.setServer(server);
    primaryStage.setOnCloseRequest(guiController);
    Scene myScene = new Scene(myPane);
    myScene.setRoot(myPane);
    primaryStage.setScene(myScene);
    primaryStage.show();
  }

  public static void main(String[] args) {
    if (!DatabaseUtils.databaseExists()) {
      DatabaseUtils.createTables();
      LOGGER.debug("Created new database file.");
    }
    addHook();
    launch();
  }

  /** Shutdown hook. */
  private static void addHook() {
    Runtime.getRuntime().addShutdownHook(new ShutdownHook(COORDINATOR, Thread.currentThread()));
  }
}
