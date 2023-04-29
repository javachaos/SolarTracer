package solartracer.utils;

import javafx.util.StringConverter;

/**
 * Frequency list String converter. 
 * 
 * @author fred
 *
 */
public class FreqListStringConverter extends StringConverter<Integer> {

    @Override
    public String toString(Integer integer) {
    	if (integer == null) {
    		return "";
		}
		return switch (integer) {
			case 1000 -> "1 second";
			case 2000 -> "2 seconds";
			case 3000 -> "3 seconds";
			case 4000 -> "4 seconds";
			case 5000 -> "5 seconds";
			case 10000 -> "10 seconds";
			case 25000 -> "25 seconds";
			case 30000 -> "30 seconds";
			case 60000 -> "1 min";
			case 300000 -> "5 minutes";
			case 600000 -> "10 minutes";
			case 1800000 -> "30 minutes";
			case 3600000 -> "1 hour";
			case 18000000 -> "5 hours";
			default -> "";
		};
    }
    
    @Override 
    public Integer fromString(String data) {
		return switch (data) {
			case "1 second" -> 1000;
			case "2 seconds" -> 2000;
			case "3 seconds" -> 3000;
			case "4 seconds" -> 4000;
			case "5 seconds" -> 5000;
			case "10 seconds" -> 10000;
			case "25 seconds" -> 25000;
			case "30 seconds" -> 30000;
			case "1 min" -> 60000;
			case "5 minutes" -> 300000;
			case "10 minutes" -> 600000;
			case "30 minutes" -> 1800000;
			case "1 hour" -> 3600000;
			case "5 hours" -> 18000000;
			default -> 0;
		};
    }

}
