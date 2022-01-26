//package solartracer.networking;
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.eclipse.jetty.server.Request;
//import org.eclipse.jetty.server.Server;
//import org.eclipse.jetty.server.handler.AbstractHandler;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import solartracer.data.DataPoint;
//import solartracer.data.DataPointListener;
//import solartracer.utils.Constants;
//import solartracer.utils.ExceptionUtils;
//
//import java.io.IOException;


//public class SolarWebServer extends AbstractHandler implements Runnable, DataPointListener {
//
//  /** Logger. */
//  public static final Logger LOGGER = LoggerFactory.getLogger(SolarWebServer.class);
//
//  /** Most current datapoint. */
//  private DataPoint current = new DataPoint(null);
//
//  /** HTML Tags for Table data */
//  private static final String TDTR = "</td></tr>";
//
//  /** HTML Tags for Table data */
//  private static final String TRTD = "<tr><td>";
//
//  /** HTML Tags for Table data */
//  private static final String TDTD = "</td><td>";
//
//  /** Server port. */
//  private int port = Constants.PORT;
//
//  private Server server;
//
//  public SolarWebServer() {
//    super();
//  }
//
//  public void run() {
//    server = new Server(port);
//    server.setHandler(this);
//    try {
//      server.start();
//      server.join();
//    } catch (Exception e) {
//      ExceptionUtils.log(getClass(), e);
//    }
//  }
//
//  public void shutdown() {
//    LOGGER.debug("Shutting down.");
//    try {
//      server.stop();
//    } catch (Exception e) {
//      ExceptionUtils.log(getClass(), e);
//    }
//    LOGGER.debug("Shutdown Complete.");
//  }
//
//  public boolean isConnected() {
//    return server.isRunning();
//  }
//
//  @Override
//  public void handle(
//      String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
//      throws IOException, ServletException {
//    response.setContentType("text/html;charset=utf-8");
//    response.setStatus(HttpServletResponse.SC_OK);
//    baseRequest.setHandled(true);
//    response
//        .getWriter()
//        .println(
//            "<h1 style=\"text-align: center;\"><span style\"text-decoration:"
//                + " underline;\"><strong>Solar Tracer Data</strong></span></h1><table"
//                + " style=\"margin-left: auto; margin-right: auto; height: 182px;\" width=\"194\">"
//                + "<tbody><tr><td>Battery Voltage"
//                + TDTD
//                + current.getBatteryVoltage()
//                + TDTR
//                + TRTD
//                + "Photovoltaic Voltage"
//                + TDTD
//                + current.getPvVoltage()
//                + TDTR
//                + TRTD
//                + "Load Current"
//                + TDTD
//                + current.getLoadCurrent()
//                + TDTR
//                + TRTD
//                + "Over Discharge"
//                + TDTD
//                + current.getOverDischarge()
//                + TDTR
//                + TRTD
//                + "Battery Max"
//                + TDTD
//                + current.getBatteryMax()
//                + TDTR
//                + TRTD
//                + "Battery Full"
//                + TDTD
//                + current.getBatteryFull()
//                + TDTR
//                + TRTD
//                + "Battery Charging"
//                + TDTD
//                + current.getCharging()
//                + TDTR
//                + TRTD
//                + "Charge Current"
//                + TDTD
//                + current.getChargeCurrent()
//                + TDTR
//                + TRTD
//                + "Load on/off"
//                + TDTD
//                + current.getLoadOnoff()
//                + TDTR
//                + TRTD
//                + "Time"
//                + TDTD
//                + current.getTimeFormatted()
//                + TDTR
//                + "</tbody></table>");
//  }
//
//  @Override
//  public void dataPointReceived(DataPoint dataPoint) {
//    if (dataPoint != null) {
//      current = dataPoint;
//    }
//  }
//}
