package nl.vandenzen.mijnsensors.mqttpilight;

import com.google.gson.*;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;

import org.eclipse.paho.client.mqttv3.*;

/**
 * Assume pilight has not started yet when this object is created
 */
@Deprecated
public class ReadMqtt {
    private final MqttClient mqttClient;
    private final String mqttTopic;

    public ReadMqtt(MqttClient mqttClient, String mqttTopic) {
        this.mqttClient=mqttClient;
        this.mqttTopic=mqttTopic;
    }

    final static Logger LOGGER = Logger.getLogger("nl.vandenzen.mijnsensors.MqttPilight");

}
