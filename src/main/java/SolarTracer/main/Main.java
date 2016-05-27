package SolarTracer.main;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import SolarTracer.gui.GuiController;
import SolarTracer.utils.Constants;
import SolarTracer.utils.DatabaseUtils;
import SolarTracer.utils.ShutdownHook;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Main extends Application {
	
	/**
	 * Thread coordinator.
	 */
	public static final ScheduledExecutorService COORDINATOR =
	      Executors.newScheduledThreadPool(Constants.NUM_THREADS);

	private GuiController guiController;
	
	/**
	 * Logger.
	 */
	public static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	@Override
	public void start(Stage primaryStage) throws IOException {
		primaryStage.setTitle("Solar MPPT Tracer");
		FXMLLoader fxmlLoader = new FXMLLoader();
		AnchorPane myPane =
		    (AnchorPane) fxmlLoader.load(getClass().getResource("/commander.fxml").openStream());
	    guiController = (SolarTracer.gui.GuiController) fxmlLoader.getController();
		COORDINATOR.scheduleAtFixedRate(guiController, 0, Constants.GUI_SLEEPTIME, TimeUnit.MILLISECONDS);
		addHook();
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
		launch(args);
	}
	
	/**
	 * Shutdown hook.
	 */
	private static void addHook() {
	  Runtime.getRuntime().addShutdownHook(new ShutdownHook(COORDINATOR, Thread.currentThread()));
	}
}
