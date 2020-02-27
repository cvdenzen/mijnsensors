package nl.vandenzen.pilightmqttosgi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.List;

import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.wiringpi.Gpio;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
//import javafx.scene.effect.Light;
import nl.vandenzen.pilightmqttosgi.json.*;
import org.apache.camel.*;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.component.paho.PahoConstants;
import org.apache.camel.spi.PropertiesComponent;
import org.apache.camel.model.dataformat.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.gson.GsonDataFormat;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultRegistry;
import org.apache.camel.support.SimpleRegistry;
import org.apache.camel.processor.ExchangePatternProcessor;
import org.apache.camel.spi.Registry;
import org.apache.camel.component.netty.*;
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
import java.util.Properties;

import org.apache.camel.component.netty.NettyServerBootstrapConfiguration;


import com.pi4j.io.i2c.I2CBus;
import org.apache.camel.support.ExpressionAdapter;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

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
    MqttConnectOptions connectOptions() {
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setUserName("hab");
        connectOptions.setPassword("J51bTmPbaza4".toCharArray());
        return connectOptions;
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

        logger.info("MyRouteBuilder start wiringPiSetupGpio");
        Gpio.wiringPiSetupGpio();
        logger.info("MyRouteBuilder end wiringPiSetupGpio");

        //JsonDataFormat jsonDataFormat = new JacksonDataFormat(JsonReceiverResponse.class);
        XStreamDataFormat xStreamDataFormat = new XStreamDataFormat(); // (JsonReceiverResponse.class);
        //GsonDataFormat gsonDataFormat = new GsonDataFormat(); // (JsonReceiverResponse.class);
        //CamelContext context = new DefaultCamelContext();

        String msg = dateFormat.format(new Date()) + " Start MyRouteBuilder.main()";
        System.out.println(msg);
        logger.info(msg);
        context = new DefaultCamelContext(new DefaultRegistry());

        registry = (DefaultRegistry)context.getRegistry();
        if (registry instanceof DefaultRegistry) {
            registry
                    = ((DefaultRegistry) registry).getFallbackRegistry();
        }

        // bean in simple map
        registry.bind("jsonReceiverResponse", new JsonReceiverResponse());

        // over the air protocol (433 MHz protocol name, used in mqtt topic name)
        final OTAProtocolExtractor otaProtocolExtractor = new OTAProtocolExtractor();
        registry.bind("otaProtocolExtractor", otaProtocolExtractor);
        //org.eclipse.paho.client.mqttv3.MqttClient a=new org.eclipse.paho.client.mqttv3.MqttClient("","").

        // The string decoder and encoder for connection to pilight
        StringDecoder stringDecoder=new StringDecoder();
        StringEncoder stringEncoder=new StringEncoder();
        registry.bind("string-decoder",stringDecoder);
        registry.bind("string-encoder",stringEncoder);

        MqttConnectOptions mqttConnectOptions=connectOptions();
        registry.bind("dummyName",mqttConnectOptions);


        // Default timeout is 300s (5 mins), which is too long to wait for in testing situations. Shutdown takes too long.
        getContext().getShutdownStrategy().setTimeUnit(TimeUnit.SECONDS);
        getContext().getShutdownStrategy().setTimeout(5);
        getContext().getShutdownStrategy().setShutdownNowOnTimeout(true);

        try {
            context.addComponent("stream", new org.apache.camel.component.stream.StreamComponent());
            context.addComponent("paho", new org.apache.camel.component.paho.PahoComponent());
            context.addComponent("netty", new org.apache.camel.component.netty.NettyComponent());
            context.addComponent("quartz", new org.apache.camel.component.quartz.QuartzComponent());
            context.addComponent("http", new org.apache.camel.component.http.HttpComponent());


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

            // Jackson supports unmarshal to List<Map>
            JacksonDataFormat format = new JacksonDataFormat();
            format.useList();

            // Configure the pilight listener (a netty4 consumer) to make it send a subscribe (identify) message
            // to the pilight server. Use a custom bootstrapConfiguration to define a custom ServerBootstrapFactory


            //NettyServerBootstrapConfiguration bootstrapConfiguration=new NettyServerBootstrapConfiguration();
            //((SimpleRegistry) registry).put("bsc", bootstrapConfiguration); // 2018-07-02 not used
            ServerInitializerFactory pilightServerInitializerFactory=new PilightServerInitializerFactory();
            registry.bind("sif", pilightServerInitializerFactory); // Server Initializer Factory
            //bootstrapConfiguration.setPort(5017);

            // misschien beter om hier te kijken: https://stackoverflow.com/questions/49682080/netty-message-on-connect
            // onderstaande code is daar niet op gebaseerd.

            // Client/producer initialisation
            ClientInitializerFactory pilightClientInitializerFactory=new PilightClientInitializerFactory();
            registry.bind("cif", pilightClientInitializerFactory); // Client Initializer Factory

            // Make pilight server configurable in properties file in karaf etc/pilightmqttosgi.properties
            PropertiesComponent pc= context.getPropertiesComponent();
            //pc.setLocation("file:${karaf.home}/etc/pilightmqttosgi.properties");
            pc.setLocation("file:${karaf.home}/etc/pilightmqttosgi.properties");
            //context.addComponent("properties", pc);
            //final String netty4Uri = "netty4:tcp://192.168.2.9:5017?clientMode=true&serverInitializerFactory=#sif";
            final String pilightServerUri = "netty:tcp://{{pilightserver}}:{{pilightport}}";

            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {

                    //from("netty4:tcp://localhost:1883?textline=true")
                    //
                    // Read pilight receiver (the 433 MHz receiver connected to
                    // Raspberry Pi)
                    from(pilightServerUri + "?clientMode=true&serverInitializerFactory=#sif&sync=false&textline=true")
                            .routeId("PilightListener")
                            .autoStartup(false)
                            .startupOrder(1)
                            .log(LoggingLevel.INFO, log1, "${body}")
                            .setExchangePattern(ExchangePattern.InOnly)
                            .to(ExchangePattern.InOnly, "direct:pilight");

                    from("direct:pilight")
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
                    // Listen to mqtt for commands and send these commands to pilight server
                    // mqtt topic is f0/protocol/cmd/${id}/${unit}
                    //               0  1        2   3     4
                    //from("paho:{{mqtt.topic.kaku.cmd}}/+/+")
                    from("direct:fromPahoToPilight")
                            .routeId("fromPahoToPilight")
                            .autoStartup(true)
                            .log("Received mqtt message on topic ${header.CamelMqttTopic}, command ${body}")
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
                                        //jas.code.protocol=new String[] {parts[1]};
                                        //logger.info("pc="+pc);
                                        //exchange.getIn().setHeader("pilight.kaku.protocol",simple("${properties:pilight.kaku.protocol}"));
                                        //logger.info("exchange header pili...="+simple("${header.pilight.kaku.protocol}").getText());
                                        // null: logger.info("pc.getOverrideProperties="+pc.getOverrideProperties());
                                        //logger.info("pc.getOverrideProperties().getProperty="+pc.getOverrideProperties().getProperty("pilight.kaku.protocol"));
                                        //logger.info("pc.getInitialProperties="+pc.getInitialProperties());
                                        //logger.info("pc.getInitialProperties().getProperty="+pc.getInitialProperties().getProperty("pilight.kaku.protocol"));
                                        //jas.code.protocol=new String[] {pc.getInitialProperties().getProperty("pilight.kaku.protocol",
                                        //        "propertyNotFoundPilight.kaku.protocol")};
                                        jas.code.protocol=new String[] {"kaku_switch_old"}; // noodgreep, properties is too difficult in Camel
                                        jas.code.unit=new Integer(parts[3]);
                                        jas.code.id=new Integer(parts[4]);
                                        String payload=(exchange.getIn().getBody(String.class));
                                        if ("on".equals(payload.toLowerCase())) {
                                            jas.code.off=null;
                                            jas.code.on=1;
                                        } else if ("off".equals(payload.toLowerCase())) {
                                            jas.code.off=1;
                                            jas.code.on=null;
                                        }
                                        exchange.getIn().setBody(jas);
                                    } else {
                                        log.error("Received mqtt message topic length<4: "+exchange.getIn().getHeader(PahoConstants.MQTT_TOPIC));
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
                            .to(pilightServerUri+"?disconnect=false&synchronous=true&textline=true")
                            .log("Reply from pilight: ${body}")
                            // Strip new line and null from string
                            .transform(body().regexReplaceAll("\n\0",""))
                    ;
                    from("direct:trash").stop()
                            ;
                    //
                    // Listen to mqtt for commands and rebroadcast them as state command
                    // mqtt topic is f0/protocol/cmd/${id}/${unit}
                    // mqtt topic is f0/protocol/rc/${id}/${unit}
                    //               0  1        2  3     4
                    // We also will receive "state" messages, but we will ignore them ???
                    from("paho:{{mqtt.topic.kaku.cmd}}/+/+")
                            .routeId("fromTopicKakuCmd")
                            .startupOrder(300)
                            .recipientList(simple("direct:toMqttSetState,direct:fromPahoToPilight"),",");
                    from("paho:{{mqtt.topic.kaku.rc}}/+/+").to("direct:toMqttSetState");
                    from("direct:toMqttSetState")
                            .routeId("toMqttSetState")
                            .autoStartup(true)
                            // extract id and unit
                            .process(new Processor() {
                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    // split into protocol, id, unit
                                    String[] parts=((String)(exchange.getIn().getHeader(PahoConstants.MQTT_TOPIC))).split("\\/");
                                    if ((parts.length>4) && ("cmd".equals(parts[2]) || "rc".equals(parts[2]))) {
                                        parts[2] = "state";
                                    } else {
                                        log.error("Received mqtt message topic length<4 or part[2]<>cmd and part[2]<>rc: "
                                                + exchange.getIn().getHeader(PahoConstants.MQTT_TOPIC));
                                    }
                                    // reassemble topic
                                    StringBuilder sb=new StringBuilder(100);
                                    for (int i=0;i<parts.length;i++) {
                                        sb.append(parts[i]);
                                        if (i<parts.length-1) sb.append("/");
                                    }
                                    exchange.getIn().setHeader(PahoConstants.MQTT_TOPIC,sb.toString());
                                }
                            })
                            .log("Broadcasting topic ${header.CamelMqttTopic}, command ${body}")
                            .recipientList(simple("paho:${header.CamelMqttTopic}?brokerUrl=tcp://{{mqttserver}}:{{mqttport}}"))
                    ;

                    // Light measurement
                    from("quartz://lightreadertimer?cron=0/10+*+*+*+*+?")

                            .routeId("lightsensor")
                            .autoStartup(true)
                            .startupOrder(100)
                            .setHeader("bh1750",constant(bh1750))
                            .process(new Processor() {
                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    Float light=bh1750.read();
                                    logger.info("bh1750.read result " + light);
                                    exchange.getIn().setBody(light.toString());
                                    // next line is useless
                                    //exchange.getIn().setHeader(PahoConstants.MQTT_TOPIC, simple("{{mqtt.topic.lightsensor.local.i2c.23}}"));
                                }
                            })
                            .recipientList(simple("paho:{{mqtt.topic.lightsensor.local.i2c.23}}?brokerUrl=tcp://{{mqttserver}}:{{mqttport}}"));


                }
            });
            // Broker started via karaf, feature activemq-broker
            //BrokerService broker = new BrokerService();
            //TransportConnector tc=broker.addConnector("mqtt+nio://localhost:1883");
            //tc.setName("mqtt");
            //broker.start();
            //System.out.println(broker.toString());
            //Thread.sleep(2000);

            msg = dateFormat.format(new Date()) + " start pir";
            logger.info(msg);
            // PirSensor
            try {
                pirSensor = new PirSensor();
                pirSensor.init();
            }
            catch (Exception ex) {
                logger.log(Level.SEVERE,"Error initialising PirSensor",ex);
            }
            msg = dateFormat.format(new Date()) + " started pir";
            logger.info(msg);



            // Light sensor init
            try {
                msg = dateFormat.format(new Date()) + " start i2cbus lightsensor";
                logger.info(msg);
                // - I2CBus.BUS_1 are pins 3 and 5 (bij RP A en B rev. 1 this is BUS_0)
                // - I2CBus.BUS_2 uses header pin CON6:3 as SDA and header pin CON6:5 as SCL
                // - I2CBus.BUS_3 uses header pin CON6:27 as SDA and header pin CON6:28 as SCL
                I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
                bh1750 = new LightSensorReaderBH1750(bus);
                bh1750.init();
                Thread.sleep(300);
                msg = dateFormat.format(new Date()) + " started route lightsensor";
                logger.info(msg);
            }
            catch (Exception ex) {
                logger.log(Level.SEVERE,"Error initialising light sensor bh1750",ex);
            }


            ProducerTemplate template = context.createProducerTemplate();
            context.start();
            msg = dateFormat.format(new Date()) + " main: context started";
            logger.info(msg);
            Thread.sleep(2000);
            // See https://access.redhat.com/documentation/en-us/red_hat_jboss_fuse/6.1/html/apache_camel_development_guide/basicprinciples-startupshutdown


            for (int i=2;i>0;i--) {
                msg=dateFormat.format(new Date()) + " "+i+" seconds before starting route from PahoToPilight";
                logger.info(msg);
                Thread.sleep(1000);
            }
            JsonIdentification ji=new JsonIdentification("identify",0,0,0,0,"0000-d0-63-03-000001","all");
            template.sendBody("direct:pilightIdentify", ji);
            msg = dateFormat.format(new Date()) + " main: sendBody identification to pilight through camel netty4 done";
            logger.info(msg);
            Thread.sleep(2000);
            context.getRouteController().startRoute("fromPahoToPilight"); // first identify, then start listening to mqtt broker

            // Start pilight listener

            if (true) {
                // Next routes are from pilight to mqtt
                // Goal: commands from remote control must be sent to mqtt.
                // Status August 10, 2018: working.
                context.getRouteController().startRoute("PilightListener");
                context.getRouteController().startRoute("ActivemqToPaho"); // waited for mqtt broker to start
                msg = dateFormat.format(new Date()) + " PilightListener and ActivemqToPaho started";
                logger.info(msg);
            }


        } catch (Exception ex) {
            msg = dateFormat.format(new Date()) + " " + ex.toString();
            logger.log(Level.SEVERE,msg,ex);
            //System.out.println(msg);
            //ex.printStackTrace();
        } finally {
            //context.stop();
            msg = dateFormat.format(new Date()) + " main: End of main, Camel context should still be running";
            System.out.println(msg);

        }
    }
    public void stop() {
        try {
            context.stop();
            if (upsPico!=null) upsPico.destroy();
            if (pirSensor!=null) pirSensor.destroy();
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

    LightSensorReaderBH1750 bh1750;
    UPSPIco upsPico;
    PirSensor pirSensor;
}
