package SolarTracer.networking;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import SolarTracer.utils.Constants;
import SolarTracer.utils.DataPoint;
import SolarTracer.utils.ExceptionUtils;

public class SolarWebServer extends AbstractHandler implements Runnable {
	
	/**
	 * Logger.
	 */
	public static final Logger LOGGER = LoggerFactory.getLogger(SolarWebServer.class);
	
	/**
	 * Most current datapoint.
	 */
	private DataPoint current;

	/**
	 * Server port.
	 */
	private int port = Constants.PORT;

	private Server server;
	
	public SolarWebServer() {
	}
	
	public void run() {
        server = new Server(port);
        server.setHandler(this);
        try {
			server.start();
	        server.join();
		} catch (Exception e) {
			ExceptionUtils.log(getClass(), e);
		}
	}
	
	public void shutdown() {
		LOGGER.debug("Shutting down.");
		try {
			server.stop();
		} catch (Exception e) {
			ExceptionUtils.log(getClass(), e);
		}
		LOGGER.debug("Shutdown Complete.");
	}

	public void updateData(DataPoint data) {
		current = data;
	}

	public boolean isConnected() {
		return server.isRunning();
	}

	@Override
    public void handle(String target, Request baseRequest,
            HttpServletRequest request, HttpServletResponse response) 
	throws IOException, ServletException {
		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		response.getWriter().println("<h1 style=\"text-align: center;\">"
				+ "<span style\"text-decoration: underline;\">"
				+ "<strong>Solar Tracer Data</strong></span></h1>"
				+ "<table style=\"margin-left: auto; margin-right: auto; height: 182px;\" width=\"194\">"
				+ "<tbody>"
				+ "<tr><td>Battery Voltage</td><td>"+current.getBatteryVoltage()+"</td></tr>"
				+ "<tr><td>Photovoltaic Voltage</td><td>"+current.getPvVoltage()+"</td></tr>"
				+ "<tr><td>Load Current</td><td>"+current.getLoadCurrent()+"</td></tr>"
				+ "<tr><td>Over Discharge</td><td>"+current.getOverDischarge()+"</td></tr>"
				+ "<tr><td>Battery Max</td><td>"+current.getBatteryMax()+"</td></tr>"
				+ "<tr><td>Battery Full</td><td>"+current.getBatteryFull()+"</td></tr>"
				+ "<tr><td>Battery Charging</td><td>"+current.getCharging()+"</td></tr>"
				+ "<tr><td>Charge Current</td><td>"+current.getChargeCurrent()+"</td></tr>"
				+ "<tr><td>Load on/off</td><td>"+current.getLoadOnoff()+"</td></tr>"
				+ "<tr><td>Time</td><td>"+current.getTimeFormatted()+"</td></tr>"
				+ "</tbody></table>");
		}
}
