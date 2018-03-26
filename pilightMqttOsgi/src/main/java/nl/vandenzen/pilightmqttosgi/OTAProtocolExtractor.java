/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.vandenzen.pilightmqttosgi;

import nl.vandenzen.pilightmqttosgi.json.JsonReceiverResponse;
import org.apache.camel.Exchange;

/**
 *
 * @author carl
 */
public class OTAProtocolExtractor {

    public OTAProtocolExtractor() {

    }

    public void storeMqttTopic(Exchange exchange) {
        JsonReceiverResponse jrr = ((JsonReceiverResponse) (exchange.getIn().getBody()));
        String otaProtocol = jrr.getProtocol();
        exchange.getIn().setHeader("mqttTopic", otaProtocol);
    }
    
    // Set on/off command in body
    public void replaceInBodyWithCommand(Exchange exchange) {
        JsonReceiverResponse jrr = ((JsonReceiverResponse) (exchange.getIn().getBody()));
        // Message is a subclass of the json message as sent by pilight
        String otaCommand = jrr.getMessage().getState();
        //exchange.getIn().setHeader("messageState", otaCommand);
        exchange.getIn().setBody(otaCommand);
    }
}
