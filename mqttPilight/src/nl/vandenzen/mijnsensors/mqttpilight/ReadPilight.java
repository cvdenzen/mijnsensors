package nl.vandenzen.mijnsensors.mqttpilight;

import com.google.gson.*;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;

import org.eclipse.paho.client.mqttv3.*;
import nl.vandenzen.mijnsensors.mqttpilight.json.*;

public class ReadPilight extends Thread {

    public ReadPilight(Socket pilightSocket, MqttClient mqttClient, String mqttTopic) {
        this.pilightSocket = pilightSocket;
        this.mqttClient = mqttClient;
        this.mqttTopic = mqttTopic;
    }

    //
    // Start thread to read pilight
    public void run() {
        PrintWriter out = null;
        BufferedReader in = null;
        // open pilight
        if (pilightSocket != null) {
            try {
                out = new PrintWriter(pilightSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(pilightSocket.getInputStream()));
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Exception", ex);
            }
            // Send identification to Pilight
            out.println(new JsonIdentification("identify", 0, 1, 0, 0, "0000-7c-dd-90-a8e0c2", "all"));
            out.flush();
        } else {
            LOGGER.log(Level.INFO, "No connection. Test mode?");
        }
        // Read input json
        String s;
        StringBuilder s1;
        while (true) {
            s1 = new StringBuilder();
            try {
                if (in != null) {
                    while (in.ready()) {
                        // This readLine call is not guaranteed not to block, but
                        // we might hope that Pilight always adds a new line to the end of
                        // a message.
                        s = in.readLine();
                        s1.append(s);
                    }
                } else {
                    s1.append("{}");
                }
                // Split the responses from Pilight in separate Json elements
                ArrayList<JsonElement> jsonElements = pilightJsonSplit(s1.toString());
                //String mqttTopic = mqttTopic(s1.toString());
                // String payload = parsePilightJson(s1.toString());
                for (JsonElement jsonElement : jsonElements) {
                    // parse pilight receiver and publish to mqtt
                    parsePilightJson(jsonElement);
                }

            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Exception", ex);
            }
        }
    }

    // Topic will be null if not a wanted message.
    // Messages from the receiver are wanted. origin=receiver

    /**
     * Split a multiple top level json into multiple JsonElements
     *
     * @param jsonInput
     * @return
     */
    public ArrayList<JsonElement> pilightJsonSplit(String jsonInput) {
        LOGGER.fine("jsonInput=" + jsonInput);

        ArrayList<JsonElement> jsonElements = new ArrayList<>();
        GsonBuilder builder = new GsonBuilder();
        // Allow multiple top level elements
        builder.setLenient();

        JsonStreamParser parser = new JsonStreamParser(jsonInput);

        while (parser.hasNext()) {
            JsonElement jelement = parser.next();
            jsonElements.add(jelement);
            LOGGER.finest("toplevelelement=" + jelement.toString());
        }
        return jsonElements;
    }

    String parsePilightJson(JsonElement s) {
        //s1.append(s);
        // Try whether we have a valid json object
        // It should be, pilight always ends with new line
        JsonElement jsonInput = s;

        // Try messages
        JsonReceiverResponse jsonReceiverResponse = new Gson().fromJson(jsonInput, JsonReceiverResponse.class);
        if ("receiver".equals(jsonReceiverResponse.origin) && (jsonReceiverResponse.message != null)) {
            mqttTopic = String.format(mqttTopic + "/status/" + jsonReceiverResponse.protocol + "_%s_%s", jsonReceiverResponse.message.unit, jsonReceiverResponse.message.id);
            String mqttPayload = jsonReceiverResponse.message.state;
            publishToMqtt(mqttTopic, mqttPayload);
        } else {
            JsonStatusResponse jsonStatusResponse = new Gson().fromJson(jsonInput,JsonStatusResponse.class);
            if ("status".equals(jsonStatusResponse)) {
                // Only log failure. Ignore "success" response
                if (!"success".equals(jsonStatusResponse.status)) {
                    LOGGER.log(Level.SEVERE,"Pilight sent non-success status:" +jsonStatusResponse.status);
                } else {
                    LOGGER.log(Level.INFO,"Pilight sent status:" +jsonStatusResponse.status);
                }
            }
        }
        return " test";
    }

    void publishToMqtt(String mqttTopic, String mqttPayload) {
        int qos = 0;
        boolean retain = false;

        try {
            mqttClient.publish(mqttTopic, mqttPayload.getBytes("utf-8"), qos, retain);
        } catch (MqttException ex) {
            LOGGER.log(Level.SEVERE, "While publishing to mqtt", ex);
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE, "While publishing to mqtt", ex);
        }
    }


    private String mqttPayload;
    final static Logger LOGGER = Logger.getLogger("nl.vandenzen.mijnsensors.MqttPilight");

    Socket pilightSocket;
    final MqttClient mqttClient;
    String mqttTopic;
}
