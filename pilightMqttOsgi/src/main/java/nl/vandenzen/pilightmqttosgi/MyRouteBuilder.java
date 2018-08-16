package nl.vandenzen.pilightmqttosgi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import nl.vandenzen.pilightmqttosgi.json.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnection;
import org.apache.activemq.broker.TransportConnector;
import org.apache.camel.*;
import org.apache.camel.component.paho.PahoConstants;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.model.dataformat.*;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.gson.GsonDataFormat;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.PropertyPlaceholderDelegateRegistry;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.processor.ExchangePatternProcessor;
import org.apache.camel.spi.Registry;
import org.apache.camel.component.netty4.*;
import org.apache.camel.component.stream.*;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.DataFormatDefinition;

import javax.jms.ConnectionFactory;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.apache.camel.component.netty4.NettyServerBootstrapConfiguration;

/**
 * A Camel Java DSL Router
 */
public class MyRouteBuilder {

    public static void main(String[] args) throws Exception {

        MyRouteBuilder myRouteBuilder = new MyRouteBuilder();
        myRouteBuilder.start();
    }
    public MyRouteBuilder() {
    }

    public CamelContext getContext() {
        return context;
    }

    public void setContext(CamelContext context) {
        this.context = context;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    CamelContext context;
    Registry registry;
    public void start() {
        //JsonDataFormat jsonDataFormat = new JacksonDataFormat(JsonReceiverResponse.class);
        XStreamDataFormat xStreamDataFormat = new XStreamDataFormat(); // (JsonReceiverResponse.class);
        //GsonDataFormat gsonDataFormat = new GsonDataFormat(); // (JsonReceiverResponse.class);
        //CamelContext context = new DefaultCamelContext();

        String msg = dateFormat.format(new Date()) + " Start MyRouteBuilder.main()";
        System.out.println(msg);
        logger.info(msg);
        context = new DefaultCamelContext(new SimpleRegistry());

        registry = context.getRegistry();
        if (registry instanceof PropertyPlaceholderDelegateRegistry) {
            registry
                    = ((PropertyPlaceholderDelegateRegistry) registry).getRegistry();
        }

        // bean in simple map
        ((SimpleRegistry) registry).put("jsonReceiverResponse", new JsonReceiverResponse());

        // over the air protocol (433 MHz protocol name, used in mqtt topic name)
        final OTAProtocolExtractor otaProtocolExtractor = new OTAProtocolExtractor();
        ((SimpleRegistry) registry).put("otaProtocolExtractor", otaProtocolExtractor);
        //org.eclipse.paho.client.mqttv3.MqttClient a=new org.eclipse.paho.client.mqttv3.MqttClient("","").

        // The string decoder and encoder for connection to pilight
        StringDecoder stringDecoder=new StringDecoder();
        StringEncoder stringEncoder=new StringEncoder();
        ((SimpleRegistry) registry).put("string-decoder",stringDecoder);
        ((SimpleRegistry) registry).put("string-encoder",stringEncoder);

        // Default timeout is 300s (5 mins), which is too long to wait for in testing situations. Shutdown takes too long.
        getContext().getShutdownStrategy().setTimeUnit(TimeUnit.SECONDS);
        getContext().getShutdownStrategy().setTimeout(5);
        getContext().getShutdownStrategy().setShutdownNowOnTimeout(true);

        try {
            // activemq is http mqtt

            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://amq-broker");
            ActiveMQComponent ac = ActiveMQComponent.activeMQComponent();
            ac.setConnectionFactory(connectionFactory);
            ac.setUsername("karaf");
            ac.setPassword("karaf");
            context.addComponent("activemq", ac);
            context.addComponent("stream", new org.apache.camel.component.stream.StreamComponent());
            context.addComponent("paho", new org.apache.camel.component.paho.PahoComponent());
            context.addComponent("netty4", new org.apache.camel.component.netty4.NettyComponent());


            GsonDataFormat formatPojoStatusResponse = new GsonDataFormat();
            //Type genericType = new TypeToken<List<JsonReceiverResponse>>() { }.getType();
            Type genericTypeStatusResponse = new TypeToken<JsonStatusResponse>() { }.getType();
            formatPojoStatusResponse.setUnmarshalGenericType(genericTypeStatusResponse); // no idea as what the effect is of this


            GsonDataFormat formatPojo = new GsonDataFormat();
            //Type genericType = new TypeToken<List<JsonReceiverResponse>>() { }.getType();
            Type genericType = new TypeToken<JsonReceiverResponse>() { }.getType();
            formatPojo.setUnmarshalGenericType(genericType); // no idea as what the effect is of this


            GsonDataFormat formatPojoOption = new GsonDataFormat();
            //Type genericType = new TypeToken<List<JsonOptionResponse>>() { }.getType();
            Type genericTypeOption = new TypeToken<JsonOptionResponse>() { }.getType();
            formatPojoOption.setUnmarshalGenericType(genericTypeOption); // no idea as what the effect is of this

            GsonDataFormat formatPojoActionSend = new GsonDataFormat();
            //Type genericType = new TypeToken<List<JsonActionSend>>() { }.getType();
            Type genericTypeActionSend = new TypeToken<JsonActionSend>() { }.getType();
            formatPojoActionSend.setUnmarshalGenericType(genericTypeActionSend); // no idea as what the effect is of this

            GsonDataFormat formatPojoIdentification = new GsonDataFormat();
            //Type genericType = new TypeToken<List<JsonIdentification>>() { }.getType();
            Type genericTypeIdentification = new TypeToken<JsonIdentification>() { }.getType();
            formatPojoIdentification.setUnmarshalGenericType(genericTypeIdentification); // no idea as what the effect is of this


            // Configure the pilight listener (a netty4 consumer) to make it send a subscribe (identify) message
            // to the pilight server. Use a custom bootstrapConfiguration to define a custom ServerBootstrapFactory


            //NettyServerBootstrapConfiguration bootstrapConfiguration=new NettyServerBootstrapConfiguration();
            //((SimpleRegistry) registry).put("bsc", bootstrapConfiguration); // 2018-07-02 not used
            ServerInitializerFactory pilightServerInitializerFactory=new PilightServerInitializerFactory();
            ((SimpleRegistry) registry).put("sif", pilightServerInitializerFactory); // Server Initializer Factory
            //bootstrapConfiguration.setPort(5017);

            // misschien beter om hier te kijken: https://stackoverflow.com/questions/49682080/netty-message-on-connect
            // onderstaande code is daar niet op gebaseerd.

            // Client/producer initialisation
            ClientInitializerFactory pilightClientInitializerFactory=new PilightClientInitializerFactory();
            ((SimpleRegistry) registry).put("cif", pilightClientInitializerFactory); // Client Initializer Factory

            // Make pilight server configurable in properties file in karaf etc/pilightmqttosgi.properties
            PropertiesComponent pc= new PropertiesComponent();
            pc.setLocation("file:${karaf.home}/etc/pilightmqttosgi.properties");
            context.addComponent("properties", pc);
            //final String netty4Uri = "netty4:tcp://192.168.2.9:5017?clientMode=true&serverInitializerFactory=#sif";
            final String netty4Uri = "netty4:tcp://{{pilightserver}}:{{pilightport}}";

            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {

                    //from("netty4:tcp://localhost:1883?textline=true")
                    //
                    // Read pilight receiver (the 433 MHz receiver connected to
                    // Raspberry Pi
                    from(netty4Uri + "?clientMode=true&serverInitializerFactory=#sif&sync=false&textline=true")
                            .routeId("PilightListener")
                            .autoStartup(false)
                            .startupOrder(1)
                            .log(LoggingLevel.INFO, log1, "${body}")
                            .setExchangePattern(ExchangePattern.InOnly)
                            .to(ExchangePattern.InOnly, "activemq:queue:test.queue");

                    from("activemq:queue:test.queue")
                            .routeId("ActivemqToPaho")
                            .autoStartup(false)
                            .log(LoggingLevel.INFO, log1, "${body}")
                            // Status response is {"status":"success"}
                            .choice()
                                .when().simple("${body} regex '^.*\\{.*\"status\".*'")
                                    .unmarshal(formatPojoStatusResponse)
                                    .log("Status received: ${body}")
                                // origin: receiver, only protocol arctech_switch_old
                                .endChoice()
                                .when().simple("${body} regex '.*\"origin\" *: *\"receiver\".*' && ${body} regex '.*\"protocol\" *: *\"arctech_switch_old\".*'")
                                    .to("direct:toMqtt")
                                .endChoice()
                                .otherwise()
                                    .unmarshal(formatPojoOption)
                                    .log("origin: sender config core response received: ${body}")
                                .endChoice()
                            .end()
                            //.unmarshal().json(JsonLibrary.Gson(formatPojo)) // setLenient not possible? Not needed if we parse on cr
                            //.to("stream:out")
                            //.marshal().json(JsonLibrary.Gson)
                            //.filter().simple("${bodyAs(JsonReceiverResponse.class)}")
                    ;
                    from("direct:toMqtt")
                            .routeId("toMqtt")
                            .autoStartup(true)
                            .unmarshal(formatPojo)
                            .log("Receiver response received: ${body}")
                            .bean(otaProtocolExtractor, "storeMqttTopic")
                            // Try to extract protocol, message.id, message.unit and message.state ("down" "up" ? )
                            // set command in exchange
                            .bean(otaProtocolExtractor, "replaceInBodyWithCommand")
                            //.to("stream:out")
                            .log(LoggingLevel.INFO, log1, "??aa")
                            .recipientList(simple("paho:${header.mqttTopic}?brokerUrl=tcp://{{mqttserver}}:{{mqttport}}"))
                    //.to(ExchangePattern.InOnly, "stream:out")
                    // must be origin: sender config core response
                    ;
                    //
                    // Listen to mqtt for commands and send these commands to pilight server.
                    // mqtt topic is f0/protocol/${id}/${unit}/cmd
                    //
                    from("paho:f0/kaku_switch_old/+/+/cmd")
                            .routeId("fromPahoToPilight")
                            .autoStartup(false)
                            .log("Received mqtt message on topic ${header.CamelMqttTopic}")
                            .log("Received mqtt message ${body}")
                            .setHeader("messagetype",constant("mqtt"))
                            // extract id and unit
                            .process(new Processor() {
                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // split into protocol, id, unit
                                    String[] parts=((String)(exchange.getIn().getHeader(PahoConstants.MQTT_TOPIC))).split("\\/");
                                    JsonActionSend jas=new JsonActionSend();
                                    JsonActionSend.ActionCode jasac=jas.new ActionCode();
                                    jas.action="send";
                                    jas.code=jasac;
                                    if (parts.length>3) {
                                        jas.code.protocol=new String[] {parts[1]};
                                        jas.code.unit=new Integer(parts[2]);
                                        jas.code.id=new Integer(parts[3]);
                                        String payload=(exchange.getIn().getBody(String.class));
                                        if ("on".equals(payload.toLowerCase())) {
                                            jas.code.off=null;
                                            jas.code.on=1;
                                        } else if ("off".equals(payload.toLowerCase())) {
                                            jas.code.off=1;
                                            jas.code.on=null;
                                        }
                                        exchange.getOut().setBody(jas);
                                    } else {
                                        log.error("Received mqtt message topic length<3: "+exchange.getIn().getHeader(PahoConstants.MQTT_TOPIC));
                                    }

                                }
                            })
                            .marshal(formatPojoActionSend)
                            .to("direct:toPilight");
                    //
                    // Send identification to Pilight (the connection that sends commands to Pilight)
                    //
                    from("direct:pilightIdentify")
                            .marshal(formatPojoIdentification)
                            .to("direct:toPilight");

                    from("direct:toPilight")
                            //.transform(body().append("\r")) // append the line ending
                            .log("Sending to pilight: ${body}")
                            // With disconnect=true, the reply is the same as the sent message
                            // disconnect=false&decoder=#string-decoder&encoder=#string-encoder&sync=true&

//                            Pilight socket_write does s+\n+\0:
//			/* Change the delimiter into regular newlines */
//                    sendBuff[n-(len-1)] = '\0';
//                    sendBuff[n-(len)] = '\n';
//
                            // &clientInitializerFactory=#cif
                            .to(netty4Uri+"?disconnect=false&synchronous=true&textline=true")
                            .log("Reply from pilight: ${body}")
                            // Strip new line and null from string
                            .transform(body().regexReplaceAll("\n\0",""))
                    ;
                    from("direct:trash").stop()
                            ;
                }
            });
            // Broker started via karaf, feature activemq-broker
            //BrokerService broker = new BrokerService();
            //TransportConnector tc=broker.addConnector("mqtt+nio://localhost:1883");
            //tc.setName("mqtt");
            //broker.start();
            //System.out.println(broker.toString());
            //Thread.sleep(2000);

            ProducerTemplate template = context.createProducerTemplate();
            context.start();
            msg = dateFormat.format(new Date()) + " main: context started";
            logger.info(msg);
            System.out.println(msg);
            Thread.sleep(2000);
            // See https://access.redhat.com/documentation/en-us/red_hat_jboss_fuse/6.1/html/apache_camel_development_guide/basicprinciples-startupshutdown

            if (false) {
                // Next routes are from pilight to mqtt
                // Goal: commands from remote control must be sent to mqtt.
                // Status August 10, 2018: working.
                context.startRoute("PilightListener");
                context.startRoute("ActivemqToPaho"); // waited for mqtt broker to start
            }
            for (int i=20;i>0;i--) {
                msg=dateFormat.format(new Date()) + " "+i+" seconds before starting route from PahoToPilight";
                System.out.println(msg);
                Thread.sleep(1000);
            }
            JsonIdentification ji=new JsonIdentification("identify",0,0,0,0,"0000-d0-63-03-000001","all");
            template.sendBody("direct:pilightIdentify", ji);
            msg = dateFormat.format(new Date()) + " main: sendBody identification to pilight through camel netty4 done";
            System.out.println(msg);
            Thread.sleep(2000);
            context.startRoute("fromPahoToPilight"); // first identify, then start listening to mqtt broker
            template.sendBody("paho:fo/arctech_switch_old/3/0/rc","on");
            msg = dateFormat.format(new Date()) + " main: sendBody paho:fo/arctech_switch_old/3/0/rc done";
            System.out.println(msg);

            // Start pilight listener
        } catch (Exception ex) {
            msg = dateFormat.format(new Date()) + " " + ex.toString();
            System.out.println(msg);
            ex.printStackTrace();
        } finally {
            //context.stop();
            msg = dateFormat.format(new Date()) + " main: End of main, Camel context should still be running";
            System.out.println(msg);

        }
    }
    public void stop() {
        try {
            context.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
    // Better:
    final static Logger logger = Logger.getLogger(MyRouteBuilder.class.toString());
    final static org.slf4j.Logger log1=org.slf4j.LoggerFactory.getLogger(logger.getName());

    final static String arctech_switch_old_send_template = "{"
            + "{"
            + "  \"action\": \"${action}\"," // action should be "send"
            + "  \"protocol\": \"${protocol}\"," // arctech_switch_old? kaku_switch_old?
            + "  \"code\": {"
            + "    \"id\": ${id},"
            + "    \"unit\": ${unit},"
            + "    \"off\": ${off}"
            + "  }"
            + "}";


    static String[] json = {"{"
            + // this is from (old) api docs?
            "  \"origin\": \"receiver\","
            + "  \"protocol\": \"kaku_switch\","
            + "  \"code\": {"
            + "    \"id\": 1234,"
            + "    \"unit\": 0,"
            + "    \"off\": 1"
            + "  }"
            + "}\n"
            + "{"
            + "  \"origin\": \"sender\","
            + "  \"protocol\": \"kaku_switch\","
            + "  \"code\": {"
            + "    \"id\": 1234,"
            + "    \"unit\": 0,"
            + "    \"off\": 1"
            + "  }"
            + "}"
            // Message as received from a remote control, received by pilight, as of year 2018.

            + " {\n"
            + "    \"message\": {\n"
            + "\"id\": 2,\n"
            + "        \"unit\": 3,\n"
            + "       \"state\": \"off\"\n"
            + "},\n"
            + "\"origin\": \"receiver\",\n"
            + "    \"protocol\": \"arctech_switch_old\",\n"
            + "    \"uuid\": \"0000-7c-dd-90-a8e0c1\",\n"
            + "    \"repeats\": 3\n"
            + "}\n",
            " {\n"
                    + "    \"message\": {\n"
                    + "\"id\": 2,\n"
                    + "        \"unit\": 3,\n"
                    + "       \"state\": \"down\"\n"
                    + "},\n"
                    + "\"origin\": \"receiver\",\n"
                    + "    \"protocol\": \"arctech_screen_old\",\n"
                    + "    \"uuid\": \"0000-7c-dd-90-a8e0c1\",\n"
                    + "    \"repeats\": 3\n"
                    + "}\n"

    };
    /**
     * pilightIdentify without newlines because it is input to pilight. I am not
     * sure whether newlines are allowed.
     */
    final static String pilightIdentify = "{" +
            "\"action\":\"identify\"," +
            "\"options\":{" +
            "\"core\":1," +
            "\"receiver\": 1," +
            "\"config\":1," +
            "\"forward\":0" +
            "}," +
            "\"uuid\":\"0000-d0-63-03-000000\"," +
            "\"media\":\"all\"" +
            "}";

}
