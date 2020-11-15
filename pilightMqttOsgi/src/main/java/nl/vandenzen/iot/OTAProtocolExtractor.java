/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.vandenzen.iot;

import nl.vandenzen.iot.json.JsonReceiverResponse;
import org.apache.camel.Exchange;

/**
 *
 * @author carl
 */
public class OTAProtocolExtractor {

    public OTAProtocolExtractor() {

    }

    public void storeMqttTopic(Exchange exchange) {
        // f0=floor 0
        JsonReceiverResponse jrr = ((JsonReceiverResponse) (exchange.getIn().getBody()));
        if ((jrr!=null) && (jrr.getMessage()!=null) && (jrr.getMessage().getUnit()!=null) && (jrr.getMessage().getId()!=null)) {
            // f0=floor 0, rc=remote control
            exchange.getIn().setHeader("mqttTopic", "{{mqtt.topic.kaku.rc}}"
                    //+"/"+jrr.getProtocol()
                    + "/" + jrr.getMessage().getUnit() + "/" + jrr.getMessage().getId());
        } else {
            exchange.getIn().setHeader("mqttTopic", "f0/protocolUnknown/" + "unitUnknown/idUnknown");
        }
    }
    
    // Set on/off command in body
    public void replaceInBodyWithCommand(Exchange exchange) {
        JsonReceiverResponse jrr = ((JsonReceiverResponse) (exchange.getIn().getBody()));
        // Message is a subclass of the json message as sent by pilight
        if ((jrr!=null) && (jrr.getMessage()!=null) && (jrr.getMessage().getUnit()!=null) && (jrr.getMessage().getId()!=null)) {
            String otaCommand = jrr.getMessage().getState();
            //exchange.getIn().setHeader("messageState", otaCommand);
            if (otaCommand!=null) {
                exchange.getIn().setBody(otaCommand);
            } else {
                exchange.getIn().setBody("otaCommandIsNull");
            }
        }
    }
}
