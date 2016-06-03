package SolarTracer.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class StatusUtils {
	
	public static void showInfo(String title, String header, String info) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(info);
		alert.showAndWait();
	}
	
	public static void showGeneralInfo(String info) {
		showInfo("General Info", "", info);
	}
	
	public static void showErrorInfo(String info) {
		showInfo("Error", "", info);
	}

}