package solartracer.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Status utility class.
 * 
 * @author fred
 *
 */
public class StatusUtils {

  /**
   * Private constructor.
   */
  private StatusUtils() {}

  /**
   * Show info alert.
   * 
   * @param title the title for the alert
   * @param header the header for the alert
   * @param info the info message
   */
  public static void showInfo(String title, String header, String info) {
    Platform.runLater(
        () -> {
          Alert alert = new Alert(AlertType.INFORMATION);
          alert.setTitle(title);
          alert.setHeaderText(header);
          alert.setContentText(info);
          alert.show();
        });
  }

  /**
   * Show a general info message.
   * 
   * @param info the info message to display
   */
  public static void showGeneralInfo(String info) {
    showInfo("General Info", "", info);
  }

  public static void showErrorInfo(String info) {
    showInfo("Error", "", info);
  }
}
