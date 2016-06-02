package SolarTracer.networking;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;

public class SolarServerChannelHandler extends SimpleChannelInboundHandler<String>{

	/**
	 * Solar Server.
	 */
	private SolarServer server;
	private String lastCmd;
	
	/**
	 * Logger.
	 */
	public static final Logger LOGGER = LoggerFactory.getLogger(SolarServerChannelHandler.class);
	private static final ChannelGroup channels =
	    new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	
	public SolarServerChannelHandler(SolarServer solarServer) {
		this.server = solarServer;
	}

	  @Override
	  public void channelActive(final ChannelHandlerContext ctx) {
	    ctx.pipeline().get(SslHandler.class).handshakeFuture()
	        .addListener(new GenericFutureListener<Future<Channel>>() {
	          @Override
	          public void operationComplete(Future<Channel> future) throws Exception {
	            channels.add(ctx.channel());
	          }
	        });
	  }

	  @Override
	  public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
	    LOGGER.error(cause.getMessage());
	  }

	  /**
	   * Add Data to each connected channel.
	   * @param msg
	   *    the string data to send.
	   */
	  public synchronized void addData(final String msg) {
	    channels.parallelStream().forEach(e -> e.writeAndFlush(msg));
	  }

	  @Override
	  protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
	    if (msg.equals("CLOSE")) {
	      ctx.close();
	      return;
	    }
	    server.handleMessage(msg);
	    LOGGER.debug(msg.toString());
	    lastCmd = msg;
	  }
	  
	  /**
	   * Get the last command sent to the server.
	   * @return
	   */
	  public String getLastCommand() {
		  return lastCmd;
	  }

}
