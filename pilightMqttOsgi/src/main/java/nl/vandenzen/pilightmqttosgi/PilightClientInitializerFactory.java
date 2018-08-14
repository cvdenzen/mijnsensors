package nl.vandenzen.pilightmqttosgi;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.apache.camel.component.netty4.ClientInitializerFactory;
import org.apache.camel.component.netty4.NettyConsumer;
import org.apache.camel.component.netty4.NettyProducer;
import org.apache.camel.component.netty4.ServerInitializerFactory;
import org.apache.camel.component.netty4.codec.DelimiterBasedFrameDecoder;
import org.apache.camel.component.netty4.handlers.ClientChannelHandler;
import org.apache.camel.component.netty4.handlers.ServerChannelHandler;

public class PilightClientInitializerFactory extends ClientInitializerFactory {
    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline channelPipeline = ch.pipeline();

        channelPipeline.addLast("encoder-SD", new StringEncoder(CharsetUtil.UTF_8));
        channelPipeline.addLast("decoder-DELIM", new DelimiterBasedFrameDecoder(maxLineSize, true, Delimiters.lineDelimiter()));
        channelPipeline.addLast("decoder-SD", new StringDecoder(CharsetUtil.UTF_8));
        // here we add the handler that emits the subscribe/initialise message to Pilight
        channelPipeline.addLast("subscribe-handler",new SubscribeHandler());
        // here we add the default Camel ServerChannelHandler for the producer, to allow Camel to route the message etc.
        channelPipeline.addLast("handler", new ClientChannelHandler(producer));
    }

    /**
     * Creates a new {@link ServerInitializerFactory} using the given {@link NettyConsumer}
     *
     * @param producer the associated producer
     * @return the {@link ClientInitializerFactory} associated to the given producer.
     */
    @Override
    public ClientInitializerFactory createPipelineFactory(NettyProducer producer) {
        this.producer =producer;
        return this;
    };


    private int maxLineSize = 32768;
    NettyProducer producer;
}
