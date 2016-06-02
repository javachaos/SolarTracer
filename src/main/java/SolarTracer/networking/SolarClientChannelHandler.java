package SolarTracer.networking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class SolarClientChannelHandler extends SimpleChannelInboundHandler<String> {


	  /**
	   * Logger.
	   */
	  public static final Logger LOGGER = LoggerFactory.getLogger(SolarClientChannelHandler.class);

	  /**
	   * Solar Client.
	   */
	  private final SolarClient client;
	  
	  /**
	   * Onyx Client Comm handler.
	   * @param client
	   *    the client.
	   */
	  public SolarClientChannelHandler(SolarClient client) {
	    this.client = client;
	  }

	  @Override
	  public void channelRead0(ChannelHandlerContext ctx, String msg) {
	    LOGGER.debug(msg.toString());
	    client.addInMessage(msg);
	  }

	  @Override
	  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
	    LOGGER.debug(cause.getMessage());
	    cause.printStackTrace();
	    ctx.close();
	  }

}
