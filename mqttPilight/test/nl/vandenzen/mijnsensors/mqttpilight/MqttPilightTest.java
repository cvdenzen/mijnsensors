package nl.vandenzen.mijnsensors.mqttpilight;


import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

class MqttPilightTest {
    @Test
    void testmqttPayload() {

        MqttPilight mqttPilight = new MqttPilight(new String[]{});

        String[] json = {"{" + // this is from (old) api docs?
                "  \"origin\": \"receiver\"," +
                "  \"protocol\": \"kaku_switch\"," +
                "  \"code\": {" +
                "    \"id\": 1234," +
                "    \"unit\": 0," +
                "    \"off\": 1" +
                "  }" +
                "}\n" +
                "{" +
                "  \"origin\": \"sender\"," +
                "  \"protocol\": \"kaku_switch\"," +
                "  \"code\": {" +
                "    \"id\": 1234," +
                "    \"unit\": 0," +
                "    \"off\": 1" +
                "  }" +
                "}"


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
                + "}\n"
                ,
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

    @Test
    void messageArrived() {
        MqttMessage m=new MqttMessage("on".getBytes());
        MqttPilight mp=new MqttPilight(new String[] {});
        try {
            mp.messageArrived("home/test/devicename_3_0", m);
        }
        catch (Exception ex) {
            LOGGER.log(Level.SEVERE,"Fout");
        }

    }

    final static Logger LOGGER = Logger.getLogger("nl.vandenzen.mijnsensors.MqttPilight");
}