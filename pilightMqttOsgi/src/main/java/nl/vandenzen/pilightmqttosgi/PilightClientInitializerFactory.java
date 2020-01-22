package nl.vandenzen.pilightmqttosgi;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;
import org.apache.camel.component.netty.ClientInitializerFactory;
import org.apache.camel.component.netty.NettyConsumer;
import org.apache.camel.component.netty.NettyProducer;
import org.apache.camel.component.netty.ServerInitializerFactory;
import org.apache.camel.component.netty.codec.DelimiterBasedFrameDecoder;
import org.apache.camel.component.netty.handlers.ClientChannelHandler;

public class PilightClientInitializerFactory extends ClientInitializerFactory {
    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline channelPipeline = ch.pipeline();

        channelPipeline.addLast("log8", new LoggingHandler(LogLevel.TRACE));
//        channelPipeline.addLast("decoder-DELIM", new DelimiterBasedFrameDecoder(maxLineSize, true,
//                Delimiters.lineDelimiter())); // this line is not what we want
//                new ByteBuf[] {Unpooled.wrappedBuffer(new byte[] { 0x0D, 0X0A }) , Unpooled.wrappedBuffer(new byte[] { 0X0A }),
//                        Unpooled.wrappedBuffer(new byte[] { 0x00 })}));
        //channelPipeline.addLast("log5", new LoggingHandler(LogLevel.TRACE));
        //channelPipeline.addLast("decoder-SD", new StringDecoder(CharsetUtil.UTF_8));
        //channelPipeline.addLast("log3", new LoggingHandler(LogLevel.TRACE));
        //channelPipeline.addLast("encoder-SD", new StringEncoder(CharsetUtil.UTF_8));
        // here we add the default Camel ClientChannelHandler for the producer, to allow Camel to route the message etc.

        //channelPipeline.addLast("log1", new LoggingHandler(LogLevel.TRACE));
        channelPipeline.addLast("handler", new ClientChannelHandler(producer));
    }

    /**
     * Creates a new {@link ClientInitializerFactory} using the given {@link NettyProducer}
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
