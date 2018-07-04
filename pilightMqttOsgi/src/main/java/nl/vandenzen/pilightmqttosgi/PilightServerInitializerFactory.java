package nl.vandenzen.pilightmqttosgi;

import io.netty.channel.Channel;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.apache.camel.component.netty4.NettyConsumer;
import org.apache.camel.component.netty4.ServerInitializerFactory;
import io.netty.channel.ChannelPipeline;
import org.apache.camel.component.netty4.codec.DelimiterBasedFrameDecoder;
import org.apache.camel.component.netty4.handlers.ServerChannelHandler;

public class PilightServerInitializerFactory extends ServerInitializerFactory {
    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline channelPipeline = ch.pipeline();

        channelPipeline.addLast("encoder-SD", new StringEncoder(CharsetUtil.UTF_8));
        channelPipeline.addLast("decoder-DELIM", new DelimiterBasedFrameDecoder(maxLineSize, true, Delimiters.lineDelimiter()));
        channelPipeline.addLast("decoder-SD", new StringDecoder(CharsetUtil.UTF_8));
        // here we add the handler that emits the subscribe/initialise message to Pilight
        channelPipeline.addLast("subscribe-handler",new SubscribeHandler());
        // here we add the default Camel ServerChannelHandler for the consumer, to allow Camel to route the message etc.
        channelPipeline.addLast("handler", new ServerChannelHandler(consumer));
    }

    /**
     * Creates a new {@link ServerInitializerFactory} using the given {@link NettyConsumer}
     *
     * @param consumer the associated consumer
     * @return the {@link ServerInitializerFactory} associated to the given consumer.
     */
    @Override
    public ServerInitializerFactory createPipelineFactory(NettyConsumer consumer) {
        this.consumer=consumer;
        return this;
    };


    private int maxLineSize = 32768;
    NettyConsumer consumer;
}
