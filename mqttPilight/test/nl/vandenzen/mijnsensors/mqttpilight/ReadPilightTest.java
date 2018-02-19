package nl.vandenzen.mijnsensors.mqttpilight;

import com.google.gson.JsonElement;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ReadPilightTest {

    @org.junit.jupiter.api.Test
    void pilightJsonSplit() {
    }

    @org.junit.jupiter.api.Test
    void parsePilightJson() {
        ReadPilight readPilight = new ReadPilight(null, null, null);
        for (String s : json) {
            ArrayList<JsonElement> jsonElements = readPilight.pilightJsonSplit(s);
            for (JsonElement jsonElement : jsonElements) {
                readPilight.parsePilightJson(jsonElement);
            }
        }
    }

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