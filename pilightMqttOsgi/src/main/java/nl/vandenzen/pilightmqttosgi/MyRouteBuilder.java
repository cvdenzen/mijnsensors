package nl.vandenzen.pilightmqttosgi;

import nl.vandenzen.pilightmqttosgi.json.JsonReceiverResponse;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.gson.GsonDataFormat;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.PropertyPlaceholderDelegateRegistry;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.model.dataformat.XStreamDataFormat;
import org.apache.camel.spi.Registry;
import org.apache.camel.component.netty4.*;
import org.apache.camel.component.stream.*;


/**
 * A Camel Java DSL Router
 */
public class MyRouteBuilder {

    public static void main(String[] args) throws Exception {
        //JsonDataFormat jsonDataFormat = new JacksonDataFormat(JsonReceiverResponse.class);
        XStreamDataFormat xStreamDataFormat = new XStreamDataFormat(); // (JsonReceiverResponse.class);
        GsonDataFormat gsonDataFormat = new GsonDataFormat(); // (JsonReceiverResponse.class);
        //CamelContext context = new DefaultCamelContext();

        CamelContext context = new DefaultCamelContext(new SimpleRegistry());

        Registry registry = context.getRegistry();
        if (registry instanceof PropertyPlaceholderDelegateRegistry) {
            registry
                    = ((PropertyPlaceholderDelegateRegistry) registry).getRegistry();
        }

        // bean in simple map
        ((SimpleRegistry)registry).put("jsonReceiverResponse", new JsonReceiverResponse());
        
        // over the air protocol (433 MHz protocol name, used in mqtt topic name)
        final OTAProtocolExtractor otaProtocolExtractor=new OTAProtocolExtractor();
        ((SimpleRegistry)registry).put("otaProtocolExtractor",otaProtocolExtractor);
        //org.eclipse.paho.client.mqttv3.MqttClient a=new org.eclipse.paho.client.mqttv3.MqttClient("","").
        try {
            // activemq is http mqtt
            context.addComponent("activemq", ActiveMQComponent.activeMQComponent("vm://localhost?broker.persistent=false"));


            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {

                    //from("netty4:tcp://localhost:1883?textline=true")
                    //from("netty4:tcp://192.168.2.9:5017?textline=true")
                    //        .to("stream:out");

                    from("activemq:queue:test.queue")
                            .to("stream:out")
                            .unmarshal().json(JsonLibrary.Gson, JsonReceiverResponse.class)
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
                            .to("stream:out");
                }
            });
            ProducerTemplate template = context.createProducerTemplate();
            context.start();
            String msg = "main: context started";
            System.out.println(msg);
            Thread.sleep(2000);
            template.sendBody("activemq:test.queue", json[0]);
            msg = "main: sendBody done";
            System.out.println(msg);
            Thread.sleep(2000);
        } catch (Exception ex) {
            String msg=ex.toString();
            System.out.println(msg);
        } finally {
            context.stop();
            String msg = "main: context stopped";
            System.out.println(msg);

        }
    }

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

}
