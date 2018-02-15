package nl.vandenzen.mijnsensors.mqttpilight;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;

import org.eclipse.paho.client.mqttv3.*;


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
        try {
            out = new PrintWriter(pilightSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(pilightSocket.getInputStream()));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Exception", ex);
        }
        // Read input json
        String s;
        StringBuilder s1;
        try {
            while (true) {
                s1 = new StringBuilder();
                while (in.ready()) {
                    // This is not guaranteed not to block
                    s = in.readLine();
                    s1.append(s);
                }
                String mqttTopic = mqttTopic(s1.toString());
                String payload = mqttPayload(s1.toString());


                int qos = 0;
                boolean retain = false;
                try {
                    mqttClient.publish(mqttTopic, payload.getBytes("utf-8"), qos, retain);
                } catch (MqttException ex) {
                    LOGGER.log(Level.SEVERE, "While publishing to mqtt", ex);
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Exception", ex);
        }
    }

    // Topic will be null if not a wanted message.
    // Messages from the receiver are wanted. origin=receiver

    private String mqttTopic(String s) {
        return ("dit is test-uitvoer");
    }

    /**
     * Split a multiple top level json into multiple JsonElements
     *
     * @param jsonInput
     * @return
     */
    ArrayList<JsonElement> mqttSplit(String jsonInput) {
        LOGGER.info("jsonInput=" + jsonInput);

        ArrayList<JsonElement> jsonElements = new ArrayList<>();
        GsonBuilder builder = new GsonBuilder();
        // Allow multiple top level elements
        builder.setLenient();

        JsonStreamParser parser = new JsonStreamParser(jsonInput);

        while (parser.hasNext()) {
            JsonElement jelement = parser.next();
            jsonElements.add(jelement);
            LOGGER.info("toplevelelement=" + jelement.toString());
        }
        return jsonElements;
    }

    String mqttPayload(String s) {
        //s1.append(s);
        // Try whether we have a valid json object
        // It should be, pilight always ends with new line
        String jsonInput = s;
        LOGGER.info("jsonInput=" + s);

        GsonBuilder builder = new GsonBuilder();
        builder.setLenient();
        //JsonReader reader=builder.create().newJsonReader(new StringReader(jsonInput));
        JsonStreamParser parser = new JsonStreamParser(jsonInput);

        while (parser.hasNext()) {
            JsonElement jelement = parser.next();
            JsonObject jobject = jelement.getAsJsonObject();
            JsonElement jsonOrigin = jobject.get("origin");
            JsonElement jsonProtocol = jobject.get("protocol");
            String sProtocol = jsonProtocol.getAsString();
            LOGGER.info(" sProtocol=" + sProtocol);
            // loop over code array (id, unit, off, ...)
            //
//            evt_dct = json.loads(evt.decode('utf-8'))
//            if evt_dct.get('origin', '') == 'update':
//                evt_type = evt_dct.get('type', None)
//                if evt_type == 1: # switch
//                    for device in evt_dct.get('devices', []):
//                        self._send_mqtt_msg(device,
//                                            self._mktopic(device, 'STATE'),
//                                            evt_dct['values']['state'])
//                elif evt_type == 3:
//                    for device in evt_dct.get('devices', []):
//                        self._send_mqtt_msg(device,
//                                            self._mktopic(device, 'HUMIDITY'),
//                                            evt_dct['values']['humidity'])
//                        self._send_mqtt_msg(device,
//                                            self._mktopic(device, 'TEMPERATURE'),
//                                            evt_dct['values']['temperature'])
//                else:
//                    raise RuntimeError('Unsupported event type %d' % evt_type)
//        except Exception as ex:  # pylint: disable=broad-except
//            self.log.error('%s: %s', ex.__class__.__name__, ex)
        }
        return " test";
    }

    final static Logger LOGGER = Logger.getLogger("nl.vandenzen.mijnsensors.MqttPilight");

    Socket pilightSocket;
    MqttClient mqttClient;
    String mqttTopic;
}
