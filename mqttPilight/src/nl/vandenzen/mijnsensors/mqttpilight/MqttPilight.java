/**
 * Gateway between Pilight (json, ip connection) and mqtt (mqtt, ip connection)
 * Options
 * usage: __main__.py [-h] [--version] [--mqtt-server MQTT_SERVER]
 * [--mqtt-port MQTT_PORT] [--mqtt-finalTopic MQTT_TOPIC]
 * [--pilight-server PILIGHT_SERVER]
 * [--pilight-port PILIGHT_PORT] [--debug] [--verbose]
 * [--pid-file path_to_pid_file]
 * <p>
 * pilight2mqtt: Translate pilight events to MQTT.
 * <p>
 * optional arguments:
 * -h, --help            show this help message and exit
 * --version             show program's version number and exit
 * --mqtt-server MQTT_SERVER
 * Address of the MQTT server to talk to.
 * --mqtt-port MQTT_PORT
 * Port of the MQTT server to talk to.
 * --mqtt-finalTopic MQTT_TOPIC
 * MQTT finalTopic to use.
 * --pilight-server PILIGHT_SERVER
 * Set the address of the pilight server to use. If not
 * specified will try to auto discover
 * --pilight-port PILIGHT_PORT
 * Port of the pilight server. Only used when pilight-
 * server is also specified
 * --debug               Start pilight2mqtt in debug mode
 * --verbose             Start pilight2mqtt in verbose mode
 * --pid-file path_to_pid_file
 * Path to PID file useful for running as daemon
 */
package nl.vandenzen.mijnsensors.mqttpilight;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.apache.commons.cli.*;
import org.eclipse.paho.client.mqttv3.*;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;

public class MqttPilight implements MqttCallback {
    public MqttPilight(String[] args) {
        // Parse parameters, 'stolen' from Oliver van Porten, pilight2mqtt
        // create Options object
        String mqtt_topic;
        Options options = new Options();


        options.addOption("h", "help", false, "display help message");
        options.addOption("v", "version", false, "display version");
        options.addOption("s", "mqtt-server", true, "Address of th MQTT server to talk to.");
        options.addOption("x", "mqtt-port", true, "Port of the MQTT server to talk to.");
        options.addOption("t", "mqtt-finalTopic", true, "MQTT finalTopic to use.");
        options.addOption("p", "pilight-server", true, "Set the address of the pilight server to use.");
        options.addOption("y", "pilight-port", true, "Port of the pilight server. Only used when pilight-\n" +
                " server is also specified");
        options.addOption("d", "debug", true, "Start pilight2mqtt in debug mode");
        options.addOption("V", "verbose", true, "Start pilight2mqtt in verbose mode");
        options.addOption("d", "pid-file", true, "Path to PID file useful for running as daemon");
        CommandLine cmd = null;
        try {
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, args);
        } catch (Exception ex) {
            // automatically generate the help statement
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("MqttPilight", options);
            System.exit(1);
        }
        if (cmd.hasOption("h")) {
            // automatically generate the help statement
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("MqttPilight", options);

        }
        if (cmd.hasOption("v")) {
            // automatically generate the help statement
            System.out.println("Version 1.0");
        }
        // get an option value
        String mqtt_server = cmd.getOptionValue("mqtt-server");
        if (mqtt_server == null) {
            mqtt_server = "localhost";
        }
        // get an option value
        String mqtt_port = cmd.getOptionValue("mqtt-port");
        if (mqtt_port == null) {
            mqtt_port = "1883";
        }
        // get an option value
        mqtt_topic = cmd.getOptionValue("mqtt-finalTopic");
        if (mqtt_topic == null) {
            mqtt_topic = "pilight";
        }
        // get an option value
        String pilight_server = cmd.getOptionValue("pilight_server");
        if (pilight_server == null) {
            pilight_server = "localhost";
        }
        // get an option value
        String pilight_port = cmd.getOptionValue("pilight_port");
        if (pilight_port == null) {
            pilight_port = "5000";
        }
        boolean debug = false;
        if (cmd.hasOption("d")) {
            // automatically generate the help statement
            debug = true;

        }
        boolean verbose = false;
        if (cmd.hasOption("V")) {
            // automatically generate the help statement
            verbose = true;
        }
        // but only if both pilight server and mqtt server are defined, to
        // make it easier to do unit tests
        //
        if (cmd.hasOption("mqtt-server") && cmd.hasOption("pilight-server")) {
            //
            // Create the endpoints for mqtt to pilight.
            // Keep it simple and stupid: make double connections (for mqqt->pilight 2 connections and
            // form pilight-mqtt 2 connections
            //m2pMSocket=new Socket(mqqt_server,mqtt_port);
            try {
                Socket m2pPSocket = new Socket(InetAddress.getByName(pilight_server), Integer.parseInt(pilight_port));
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Create socket", ex);
            }
            MqttClient client = null;
            // Start mqtt->pilight thread
            try {
                client = new MqttClient("tcp://" + mqtt_server + ":" + mqtt_port, "Pilight2MqttGateway");
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "new MqttClient", ex);
            }

            final MqttClient finalClient = client;
            try {
                client.connect();
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "client.connect", ex);
            }
            try {
                client.setCallback(this);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "client.connect", ex);
            }
            try {
                client.subscribe(mqtt_topic);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "client.connect", ex);
            }


            // Make final vars to use in thread
            final String finalTopic = mqtt_topic;
            final String finalPilightServer = pilight_server;
            int tempPort = 0;
            try {
                tempPort = Integer.parseInt(pilight_port);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error in pilight port number, option pilight_port", ex);
            }
            final int finalPilightPort = tempPort;
            //
            // Start thread to read pilight
            Thread readPilight = new Thread() {
                public void run() {
                    PrintWriter out = null;
                    BufferedReader in = null;
                    // open pilight
                    Socket pilightSocket = null;
                    try {
                        pilightSocket = new Socket(finalPilightServer, finalPilightPort);
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Exception", ex);
                    }
                    try {
                        out = new PrintWriter(pilightSocket.getOutputStream(), true);
                        in = new BufferedReader(new InputStreamReader(pilightSocket.getInputStream()));
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Exception", ex);
                    }
                    // Read input json
                    String s;
                    StringBuilder s1 = new StringBuilder();
                    try {
                        while ((s = in.readLine()) != null) {
                            String payload = mqttPayload(s);


                            int qos = 0;
                            boolean retain = false;
                            try {
                                finalClient.publish(finalTopic, payload.getBytes("utf-8"), qos, retain);
                            } catch (MqttException ex) {
                                LOGGER.log(Level.SEVERE, "While publishing to mqtt", ex);
                            }
                        }
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, "Exception", ex);
                    }

                }

                ;

            };
            readPilight.start(); // start the thread
        } else {
            LOGGER.warning("Not starting because not both mqtt-server and pilight-server are defined");
        }

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
            LOGGER.info(" sProtocol=" +sProtocol);
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

    public static void main(String[] args) {
        new MqttPilight(args);
    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        LOGGER.info("Message arrived:" + message.toString());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}
