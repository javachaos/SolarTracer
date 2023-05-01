package solartracer.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Exception utils to help handle exception logging.
 * 
 * @author fred
 *
 */
public class ExceptionUtils {
	
  /**
   * Private constructor.
   */
  private ExceptionUtils() {}
  
  /**
   * Log the error and set the Application state to ERROR state.
   *
   * @param clazz the class for the logger.
   * @param ex the exception thrown.
   */
  public static void log(final Class<?> clazz, final Throwable ex) {
    Logger logger = LogManager.getLogger(clazz);
    logger.error(ex.getMessage());
    Platform.runLater(() -> showAlert(ex));
  }

  public static void logSilent(final Class<?> clazz, final Throwable ex, final String prefixMsg) {
    Logger logger = LogManager.getLogger(clazz);
    String log = prefixMsg + ex.getMessage();
    logger.error(log);
  }

  /**
   * Show an alert dialog.
   *
   * @param ex a throwable
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
    textArea.setFont(Font.font("System", Constants.ALERT_TEXT_FONT_SIZE));
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
}
