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
 * --mqtt-topic MQTT_TOPIC
 * MQTT topic to use.
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
import org.apache.commons.cli.*;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.internal.ClientComms;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Handler;

public class MqttPilight implements MqttCallback {
    public MqttPilight(String[] args) {
        // Parse parameters, 'stolen' from Oliver van Porten, pilight2mqtt
        // create Options object
        Options options = new Options();
        String mqtt_topic;


        options.addOption("h", "help", false, "display help message");
        options.addOption("v", "version", false, "display version");
        options.addOption("s", "mqtt-server", true, "Address of th MQTT server to talk to.");
        options.addOption("x", "mqtt-port", true, "Port of the MQTT server to talk to.");
        options.addOption("t", "mqtt-topic", true, "MQTT topic to use.");
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
        mqtt_topic = cmd.getOptionValue("mqtt-topic");
        if (mqtt_topic == null) {
            mqtt_topic = "pilight";
        }
        finalTopic = mqtt_topic;

        // get an option value
        String pilight_server = cmd.getOptionValue("pilight_server");
        if (pilight_server == null) {
            pilight_server = "localhost";
        }
        finalPilightServer = pilight_server;
        // get an option value
        String pilight_port = cmd.getOptionValue("pilight_port");
        if (pilight_port == null) {
            pilight_port = "5000";
        }
        int tempPort = 0;
        try {
            tempPort = Integer.parseInt(pilight_port);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error in pilight port number, option pilight_port", ex);
        }
        finalPilightPort = tempPort;
        if (cmd.hasOption("d")) {
            // automatically generate the help statement
            debug = true;

        }
        if (cmd.hasOption("V")) {
            verbose = true;
            LOGGER.setLevel(Level.FINEST);
            for (Handler h:LOGGER.getHandlers()) {
                h.setLevel(Level.FINEST);
            }
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
                m2pPSocket = new Socket(InetAddress.getByName(pilight_server), Integer.parseInt(pilight_port));
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Create socket", ex);
            }
            // Start mqtt->pilight thread
            try {
                mqttClient = new MqttClient("tcp://" + mqtt_server + ":" + mqtt_port, "Pilight2MqttGateway");
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "new MqttClient", ex);
            }
            final MqttClient finalClient = mqttClient;

            try {
                mqttClient.connect();
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "mqttClient.connect", ex);
            }
            try {
                mqttClient.setCallback(this);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "mqttClient.connect", ex);
            }
            try {
                mqttClient.subscribe(mqtt_topic);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "mqttClient.connect", ex);
            }
            Socket pilightSocket = null;
            try {
                pilightSocket = new Socket(finalPilightServer, finalPilightPort);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Exception", ex);
            }
            Thread readPilight = new ReadPilight(pilightSocket, mqttClient, mqtt_topic);
            readPilight.start(); // start the thread
        } else {
            LOGGER.warning("Not starting because not both mqtt-server and pilight-server are defined");
        }

    }

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

    // Make final vars to use in thread
    boolean debug = false;
    boolean verbose = false;
    final String finalTopic;
    final String finalPilightServer;
    final int finalPilightPort;

    Socket m2pPSocket;
    MqttClient mqttClient = null;
    final static Logger LOGGER = Logger.getLogger("nl.vandenzen.mijnsensors.MqttPilight");
}
