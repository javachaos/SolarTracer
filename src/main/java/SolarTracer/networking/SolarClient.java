package SolarTracer.networking;

import java.util.concurrent.ArrayBlockingQueue;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import SolarTracer.utils.Constants;
import SolarTracer.utils.ExceptionUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

public class SolarClient implements Runnable {
	
	/**
	 * Logger.
	 */
	public static final Logger LOGGER = LoggerFactory.getLogger(SolarClient.class);

	/**
	 * Server hostname.
	 */
	private String host;

	/**
	 * Server port.
	 */
	private int port;

	/**
	 * SSL Context.
	 */
	private SslContext sslCtx;
	  
	/**
	 * True if connected.
	 */
	private boolean isConnected;
	  
	/**
	 * The last command sent to the out bound queue.
	 */
	private String lastOutMsg;
	  
	/**
	 * Out bound Command queue.
	 */
	private ArrayBlockingQueue<String> outMsgs;
	  
	/**
	 * In bound Command queue.
	 */
	private ArrayBlockingQueue<String> inMsgs;
	
	public SolarClient(final String servHost, final int servPort) {
	    this.host = servHost;
	    this.port = servPort;
	    outMsgs = new ArrayBlockingQueue<String>(Constants.NETWORK_BUFFER_SIZE);
	    inMsgs = new ArrayBlockingQueue<String>(Constants.NETWORK_BUFFER_SIZE);
	}
	  
	@Override
	public void run() {
	    EventLoopGroup workerGroup = new NioEventLoopGroup();
	    try {
	      sslCtx =
	          SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
	      Bootstrap boot = new Bootstrap();
	      boot.group(workerGroup);
	      boot.channel(NioSocketChannel.class);
	      boot.option(ChannelOption.SO_KEEPALIVE, true);
	      boot.handler(new SolarClientChannelInitializer(this, sslCtx, host, port));
	      Channel ch = boot.connect(host, port).sync().channel();

	      ChannelFuture lastFuture = null;
	      for (;;) {
	        // Poll the next command from the queue
	        final String next = outMsgs.poll();
	        
	        // if the next command is not null
	        if (next != null) {
	          // write the command over the pipe and save the channel future
	          lastFuture = ch.writeAndFlush(next);
	        }
	        if (ch.isActive()) {
	          isConnected = true;
	        } else {
	          isConnected = false;
	        }
	        
	        // If the last command we sent out is a CLOSE command
	        if (lastOutMsg != null && lastOutMsg.equals("CLOSE")) {
	          // Close the channel
	          ch.closeFuture().sync();
	          isConnected = false;
	          break;
	        }

	      }

	      // if the connection is closed and the last future isn't null
	      if (lastFuture != null) {
	        // sync the last future and wait for the server to close the channel.
	        lastFuture.sync();
	      }

	    } catch (SSLException | InterruptedException e1) {
          ExceptionUtils.log(getClass(), e1);
	      isConnected = false;
	    } finally {
	      workerGroup.shutdownGracefully();
	      isConnected = false;
	    }
	}
	
	/**
	 * Shutdown the connection.
	 */
	public void shutdown() {
	  outMsgs.offer(Constants.CLOSE_CMD);
	}

	/**
	 * Add a message to the inMsg stack.
	 * @param msg
	 *    the next Input message from the server.
	 */
	public void addInMessage(String msg) {
	  inMsgs.offer(msg);
    }
	
	public void addOutMessage(String msg) {
		outMsgs.offer(msg);
	}
	
	public String getNextMessage() {
		return inMsgs.poll();
	}

	/**
	 * Return true if isConnected.
	 * @return
	 */
	public boolean isConnected() {
		return isConnected;
	}

}
