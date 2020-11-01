package nl.vandenzen.pilightmqttosgi;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;

import java.util.logging.Logger;

public class MyRouteBuilder extends RouteBuilder {
    public MyRouteBuilder(CamelContext context) {
        super(context);

    }

    @Override
    public void configure() throws Exception {


        from("quartz://groep/timerTestJavaDSL?cron=0/10+*+*+*+*+?")
                .log("Dit is quartz")

        ;


    }

    //final String netty4Uri = "netty4:tcp://192.168.2.9:5017?clientMode=true&serverInitializerFactory=#sif";
    final String pilightServerUri = "netty:tcp://{{pilightserver}}:{{pilightport}}";
    final static Logger logger = Logger.getLogger(MyCamelController.class.toString());
    final static org.slf4j.Logger log1=org.slf4j.LoggerFactory.getLogger(logger.getName());
}