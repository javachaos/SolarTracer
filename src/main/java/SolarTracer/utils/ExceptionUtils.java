package SolarTracer.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class ExceptionUtils {
	
	/**
	 * Log the error and set the Application state to ERROR state.
	 * 
	 * @param clazz the class for the logger.
	 * @param ex the exception thrown.
	 */
	public static void log(final Class<?> clazz, final Throwable ex) {
	  Logger logger = LoggerFactory.getLogger(clazz);
	  logger.error(ex.getMessage());
	  Platform.runLater(() -> showAlert(ex));
	}
	
	/**
	 * Show an alert dialog.
	 * @param ex
	 */
	private static void showAlert(Throwable ex) {

		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Exception Alert");
		alert.setHeaderText("Exception Dialog");
		alert.setContentText(ex.getMessage());
		// Create expandable Exception.
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		String exceptionText = sw.toString();
		Label label = new Label("Exception stacktrace:");
		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);
		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);
		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);
		// Set expandable Exception into the dialog pane.
		alert.getDialogPane().setExpandableContent(expContent);
		alert.show();
	}
	
	/**
	 * Show an alert dialog.
	 * @param ex
	 */
	public static void showAlert(String msg) {
		Platform.runLater(() -> showAlert(new SolarException(msg)));
	}
}
