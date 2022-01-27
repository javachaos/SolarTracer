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
  	  switch (integer) {
  	  case 1000:
  		  return "1 second";
  	  case 2000:
  		  return "2 seconds";
  	  case 3000:
  		  return "3 seconds";
  	  case 4000:
  		  return "4 seconds";
  	  case 5000:
  		  return "5 seconds";
  	  case 10000:
  		  return "10 seconds";
  	  case 25000:
  		  return "25 seconds";
  	  case 30000:
  		  return "30 seconds";
  	  case 60000:
  		  return "1 min";
  	  case 300000:
  		  return "5 minutes";
  	  case 600000:
  		  return "10 minutes";
  	  case 1800000:
  		  return "30 minutes";
  	  case 3600000:
  		  return "1 hour";
  	  case 18000000:
  		  return "5 hours";
        default:
  		  return "";
  	  }
    }
    
    @Override 
    public Integer fromString(String data) {
  	  switch (data) {
  	  case "1 second":
  		  return 1000;
  	  case "2 seconds":
  		  return 2000;
  	  case "3 seconds":
  		  return 3000;
  	  case "4 seconds":
  		  return 4000;
  	  case "5 seconds":
  		  return 5000;
  	  case "10 seconds":
  		  return 10000;
  	  case "25 seconds":
  		  return 25000;
  	  case "30 seconds":
  		  return 30000;
  	  case "1 min":
  		  return 60000;
  	  case "5 minutes":
  		  return 300000;
  	  case "10 minutes":
  		  return 600000;
  	  case "30 minutes":
  		  return 1800000;
  	  case "1 hour":
  		  return 3600000;
  	  case "5 hours":
  		  return 18000000;
        default:
  		  return 0;
  	  }
    }

}
