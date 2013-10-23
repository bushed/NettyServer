package nettyserver;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;

public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {
    final private long CHECK_INTERVAL_IN_MILLIS = 1000;

    @Override
     public void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipe = channel.pipeline();
        pipe.addLast("traffic", new ChannelTrafficShapingHandler(CHECK_INTERVAL_IN_MILLIS));  
        pipe.addLast("codec", new HttpServerCodec());         
        pipe.addLast("handler", new HttpServerHandler());
     }
}