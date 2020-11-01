package nl.vandenzen.pilightmqttosgi;

import nl.vandenzen.pilightmqttosgi.json.JsonIdentification;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.spi.Registry;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.osgi.framework.BundleContext;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

//import javafx.scene.effect.Light;

/**
 * A Camel Java DSL Router
 */
public class MyCamelController {

    private final BundleContext bundleContext;

    public MyCamelController(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
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

    CamelContext context;
    Registry registry;

    public void start() {

        String msg = "<initial value for msg>";


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


            context.addRoutes(new MyRouteBuilder(context));
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
            Thread.sleep(2000);
            // See https://access.redhat.com/documentation/en-us/red_hat_jboss_fuse/6.1/html/apache_camel_development_guide/basicprinciples-startupshutdown


            for (int i = 2; i > 0; i--) {
                msg = dateFormat.format(new Date()) + " " + i + " seconds before starting route from PahoToPilight";
                logger.info(msg);
                Thread.sleep(1000);
            }
            JsonIdentification ji = new JsonIdentification("identify", 0, 0, 0, 0, "0000-d0-63-03-000001", "all");
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
            logger.log(Level.SEVERE, msg, ex);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
    // Better:
    final static Logger logger = Logger.getLogger(MyCamelController.class.toString());
    final static org.slf4j.Logger log1 = org.slf4j.LoggerFactory.getLogger(logger.getName());

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
