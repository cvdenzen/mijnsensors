package nl.vandenzen.pilightmqttosgi;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.apache.camel.component.netty4.ClientModeTCPNettyServerBootstrapFactory;

import static nl.vandenzen.pilightmqttosgi.MyRouteBuilder.pilightIdentify;

public class PilightServerBootstrapFactory extends ClientModeTCPNettyServerBootstrapFactory {
    @Override
    protected Channel openChannel(ChannelFuture cf) throws Exception {
        Channel channel=super.openChannel(cf);
        // And now send subscription data to the pilight server
        ChannelFuture channelFuture=channel.writeAndFlush(pilightIdentify+" \r" );
        if (channelFuture==null) {
            throw new IllegalArgumentException("channelFuture is null after channel.writerAndFlush in " + this);
        }
        return channelFuture.channel();

    }
}
