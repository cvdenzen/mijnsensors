package nl.vandenzen.pilightmqttosgi;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.logging.Level;
import java.util.logging.Logger;

import static nl.vandenzen.pilightmqttosgi.MyCamelController.pilightIdentify;

public class SubscribeHandler extends ChannelInboundHandlerAdapter {

    /**
     * the channelActive() method will be invoked when a connection is established and ready to generate traffic.
     * this method is called after a connect. Send the subscription string to the pilight server.
     * @param ctx
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws java.lang.Exception {
//        if (LOG!=null) {
//            throw new IllegalArgumentException("dummy exception");
//        }
        // Send it just once ...
        LOG.log(Level.INFO,"before writeAndFlush ctx={}",ctx);
        final ChannelFuture f = ctx.writeAndFlush(pilightIdentify+"\r");
        LOG.log(Level.INFO,"after writeAndFlush f={}",f);

        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws java.lang.Exception {
                assert f == future;
                LOG.log(Level.INFO,"f operationComplete");
                //ctx.close();
                // SubscribeHandler.super.channelActive(ctx);
            }
        });

    }
    final static Logger LOG=Logger.getLogger(SubscribeHandler.class.toString());

}
