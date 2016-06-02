package SolarTracer.networking;

import java.security.cert.CertificateException;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import SolarTracer.gui.GuiController;
import SolarTracer.utils.Constants;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class SolarServer implements Runnable {

	/**
	 * Logger.
	 */
	public static final Logger LOGGER = LoggerFactory.getLogger(SolarServer.class);
	
	/**
	 * Boss group.
	 */
	final EventLoopGroup bossGroup = new NioEventLoopGroup(Constants.NUM_NIO_THREADS);

	/**
	 * Worker group.
	 */
	final EventLoopGroup workerGroup = new NioEventLoopGroup();
	
	private SolarServerChannelHandler handler;
	private SolarServerChannelInitializer initializer;
	
	private Channel ch;
	
	private GuiController controller;
	
	/**
	 * Server Port.
	 */
	static final int PORT = Constants.PORT;
	
	public SolarServer(GuiController controller) {
		this.controller = controller;
		controller.setServer(this);
	}

	@Override
	public void run() {
	    LOGGER.debug("Starting SolarServer.");
	    try {
	      SelfSignedCertificate ssc = new SelfSignedCertificate();
	      SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
	      handler = new SolarServerChannelHandler(this);
	      initializer = new SolarServerChannelInitializer(sslCtx, handler);
	      final ServerBootstrap b = new ServerBootstrap();
	      b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
	          .handler(new LoggingHandler(LogLevel.INFO))
	          .childHandler(initializer)
	          .option(ChannelOption.SO_BACKLOG, 128)
	          .childOption(ChannelOption.SO_KEEPALIVE, true);
	      ch = b.bind(PORT).sync().channel();
	      ch.closeFuture().sync();
	      LOGGER.debug("SolarServer Started.");
	    } catch (final InterruptedException | SSLException | CertificateException e1) {
	    	LOGGER.error(e1.getMessage());
	    } finally {
	      workerGroup.shutdownGracefully();
	      bossGroup.shutdownGracefully();
	    }
	}

	/**
	 * Handle a message recieved from a client.
	 * @param msg
	 */
	public void handleMessage(String msg) {
		controller.submitMessage(msg);
	}

	/**
	 * Send message s to connected clients.
	 * @param s
	 */
	public void sendMessage(String s) {
		handler.addData(s);
	}
	
}
