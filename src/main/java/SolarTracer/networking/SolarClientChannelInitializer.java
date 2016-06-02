package SolarTracer.networking;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.ssl.SslContext;

public class SolarClientChannelInitializer extends ChannelInitializer<SocketChannel> {

	  private final SslContext sslCtx;
	  private final String host;
	  private final int port;
	  private final SolarClient client; 

	  /**
	   * OnyxClientChannelInitializer.
	   * @param solarClient 
	   * 
	   * @param sslCtx the sslCtx
	   */
	  public SolarClientChannelInitializer(SolarClient solarClient, SslContext sslCtx,
	      final String host, final int port) {
	    this.sslCtx = sslCtx;
	    this.host = host;
	    this.port = port;
	    this.client = solarClient;
	  }

	  @Override
	  public void initChannel(SocketChannel ch) throws Exception {
	    ChannelPipeline pipeline = ch.pipeline();
	    pipeline.addLast(sslCtx.newHandler(ch.alloc(), host, port));
	    pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(getClass().getClassLoader())));
	    pipeline.addLast(new ObjectEncoder());
	    pipeline.addLast(new SolarClientChannelHandler(client));
	  }

}
