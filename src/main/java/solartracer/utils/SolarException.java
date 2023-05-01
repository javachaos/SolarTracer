package solartracer.utils;

import java.io.Serial;

/**
 * Class to handle solar exception.
 * 
 * @author fred
 *
 */
public class SolarException extends Exception {

	/**
	 * Serial version ID.
	 */
	@Serial
	private static final long serialVersionUID = -1771393745253848732L;
	
	/**
	 * Solar Exception constructor.
	 * 
	 * @param msg the exception message
	 */
	public SolarException(final String msg) {
		super(msg);
	}

}
