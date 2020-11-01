package nl.vandenzen.pilightmqttosgi;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.apache.camel.component.netty.ClientModeTCPNettyServerBootstrapFactory;

import static nl.vandenzen.pilightmqttosgi.MyCamelController.pilightIdentify;
/*
Vervangen door ServerInitializerFactory
 */
@Deprecated
public class PilightServerBootstrapFactory extends ClientModeTCPNettyServerBootstrapFactory {
    @Override
    protected Channel openChannel(ChannelFuture cf) throws Exception {
        Channel channel=super.openChannel(cf);
        //return channel;


        // And now send subscription data to the pilight server
        ChannelFuture channelFuture=channel.writeAndFlush(pilightIdentify+" \r" );
        if (channelFuture==null) {
            throw new IllegalArgumentException("channelFuture is null after channel.writerAndFlush in " + this);
        }
        LOG.info("channelFuture={}",channel);
        channelFuture.awaitUninterruptibly();

        return channelFuture.channel();

    }
}
