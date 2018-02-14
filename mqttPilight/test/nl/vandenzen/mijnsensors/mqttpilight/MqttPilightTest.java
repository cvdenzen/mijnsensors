package nl.vandenzen.mijnsensors.mqttpilight;


import org.junit.jupiter.api.Test;

class MqttPilightTest {
    @Test
    void testmqttPayload() {

        MqttPilight mqttPilight = new MqttPilight(new String[]{});

        String[] json = {"{" +
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


                // Message as received from a remote control, received by pilight

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


        for (String s : json) {
            String payload = mqttPilight.mqttPayload(s);
        }

    }
}