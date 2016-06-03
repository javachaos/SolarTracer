package SolarTracer.networking;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;

public class SolarServerChannelInitializer extends ChannelInitializer<SocketChannel> {

	/**
	 * SSL Context.
	 */
	private SslContext sslCtx;
	
	/**
	 * Solar Channel Handler.
	 */
	private SimpleChannelInboundHandler<String> solarChannelHandler;

	public SolarServerChannelInitializer(SslContext sslCtx, SimpleChannelInboundHandler<String> handler) {
		this.sslCtx = sslCtx;
		this.solarChannelHandler = handler;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
	    ChannelPipeline pipeline = ch.pipeline();
	    pipeline.addLast(sslCtx.newHandler(ch.alloc()));
	    pipeline.addLast(new StringDecoder());
	    pipeline.addLast(new StringEncoder());
	    pipeline.addLast(solarChannelHandler);
	}

}
