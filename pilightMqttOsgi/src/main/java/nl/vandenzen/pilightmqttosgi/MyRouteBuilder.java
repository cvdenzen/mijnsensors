package nl.vandenzen.pilightmqttosgi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nl.vandenzen.pilightmqttosgi.json.JsonReceiverResponse;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.*;
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
import java.util.logging.Logger;


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
        try {
            // activemq is http mqtt

            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost");
            ActiveMQComponent ac = ActiveMQComponent.activeMQComponent();
            ac.setConnectionFactory(connectionFactory);
            ac.setUsername("karaf");
            ac.setPassword("karaf");
            context.addComponent("activemq", ac);
            context.addComponent("stream", new org.apache.camel.component.stream.StreamComponent());
            context.addComponent("paho", new org.apache.camel.component.paho.PahoComponent());
            context.addComponent("netty4", new org.apache.camel.component.netty4.NettyComponent());


            //Populate data formats
            JsonDataFormat jsonDataFormat = new JsonDataFormat(JsonLibrary.Gson);
            jsonDataFormat.setUseList(true);

            /* This part is not easily integrated in a Camel context
            Gson gson = new GsonBuilder().setLenient().create();
            final GsonDataFormat gsonDataFormatReceiverResponse = new GsonDataFormat(gson, JsonReceiverResponse.class);
            DataFormatDefinition dataFormatDefinition=new DataFormatDefinition();
            */



            context.setDataFormats(Collections.singletonMap(jsonDataFormat.getDataFormatName(), jsonDataFormat));
            msg = dateFormat.format(new Date()) + " " + "jsonDataFormat.getDataFormatName() is " + jsonDataFormat.getDataFormatName();
            System.out.println(msg);



            final String netty4Uri = "netty4:tcp://192.168.2.9:5017?clientMode=true&disconnect=false";

            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {

                    //from("netty4:tcp://localhost:1883?textline=true")
                    //
                    // Read pilight receiver (the 433 MHz receiver connected to
                    // Raspberry Pi
                    from("activemq:queue:test1.queue")
                            .to("stream:out")
                            .process(new Processor() {
                                public void process(Exchange exchange) {
                                    String message = (String) exchange.getIn().getBody();
                                    String messageIn = message + " hoiIn1 ";
                                    exchange.getIn().setBody(messageIn);
                                    String messageOut = message + " hoiOut1 ";
                                    exchange.getOut().setBody(messageOut);
                                }
                            })
                            .to("stream:out")
                            .to(netty4Uri);

/*

                    .from(netty4Uri + "&textline=true")
                            .process(new Processor() {
                                public void process(Exchange exchange) {
                                    String message = (String) exchange.getIn().getBody();
                                    String messageIn = message + " hoiIn ";
                                    exchange.getIn().setBody(messageIn);
                                    String messageOut = message + " hoiOut ";
                                    exchange.getOut().setBody(messageOut);
                                }
                            })
                            .to("stream:out")
                            .setExchangePattern(ExchangePattern.InOnly)
                            .to(ExchangePattern.InOnly, "activemq:queue:test.queue");
*/

                    from("activemq:queue:test.queue")
                            .to("stream:out")
                            .unmarshal().json(JsonLibrary.Gson,JsonReceiverResponse.class)
                            //.unmarshal(gsonDataFormatReceiverResponse)
                            .to("stream:out")
                            //.marshal().json(JsonLibrary.Gson)
                            .to("stream:out")
                            //.filter().simple("${bodyAs(JsonReceiverResponse.class)}")
                            .bean(otaProtocolExtractor, "storeMqttTopic")
                            // Try to extract protocol, message.id, message.unit and message.state ("down" "up" ? )
                            .to("stream:out")
                            // set command in exchange
                            .bean(otaProtocolExtractor, "replaceInBodyWithCommand")
                            .to("stream:out")
                            .toF("paho:test/%s/some/target/queue?brokerUrl=tcp://192.168.2.9:1883",
                                    "${header.mqttTopic}")
                            .to(ExchangePattern.InOnly, "stream:out");
                }
            });
            ProducerTemplate template = context.createProducerTemplate();
            context.start();
            msg = dateFormat.format(new Date()) + " main: context started";
            logger.info(msg);
            System.out.println(msg);
            Thread.sleep(2000);
            template.sendBody("activemq:queue:test1.queue", pilightIdentify+"\n");
            //template.sendBody("activemq:queue:test.queue", json[0]);
            msg = dateFormat.format(new Date()) + " main: sendBody done";
            System.out.println(msg);
            Thread.sleep(2000);
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
