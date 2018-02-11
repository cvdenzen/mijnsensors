/**
 * Gateway between Pilight (json, ip connection) and mqtt (mqtt, ip connection)
 * Options
 * usage: __main__.py [-h] [--version] [--mqtt-server MQTT_SERVER]
 [--mqtt-port MQTT_PORT] [--mqtt-topic MQTT_TOPIC]
 [--pilight-server PILIGHT_SERVER]
 [--pilight-port PILIGHT_PORT] [--debug] [--verbose]
 [--pid-file path_to_pid_file]

 pilight2mqtt: Translate pilight events to MQTT.

 optional arguments:
 -h, --help            show this help message and exit
 --version             show program's version number and exit
 --mqtt-server MQTT_SERVER
 Address of the MQTT server to talk to.
 --mqtt-port MQTT_PORT
 Port of the MQTT server to talk to.
 --mqtt-topic MQTT_TOPIC
 MQTT topic to use.
 --pilight-server PILIGHT_SERVER
 Set the address of the pilight server to use. If not
 specified will try to auto discover
 --pilight-port PILIGHT_PORT
 Port of the pilight server. Only used when pilight-
 server is also specified
 --debug               Start pilight2mqtt in debug mode
 --verbose             Start pilight2mqtt in verbose mode
 --pid-file path_to_pid_file
 Path to PID file useful for running as daemon
 */
package nl.vandenzen.mijnsensors.mqttPilight;
import org.apache.commons.cli.*;
import paho;
public class mqttPilight {
    public mqttPilight() {
    }
    public static void main() {
        // Parse parameters, 'stolen' from Oliver van Porten, pilight2mqtt
        // create Options object
        Options options = new Options();

// add t option
        options.addOption("h", "help", false, "display help message");
        options.addOption("v","version", false, "display version");
        options.addOption("","mqtt-server", true, "Address of th MQTT server to talk to");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse( options, args);
        // get an option value
        String countryCode = cmd.getOptionValue("c");

        if(countryCode == null) {
            // print default date
        }
        else {
            // print date for country specified by countryCode
        }
    }
    private
}
