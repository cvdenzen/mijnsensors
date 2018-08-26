package nl.vandenzen.pilightmqttosgi;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class BH1750Processor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {

        Float light=bh1750.read();
        logger.info("bh1750.read result from BH1750Processor:" + light);
        exchange.getIn().setBody(light.toString());
    }
}
