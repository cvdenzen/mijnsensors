package nl.vandenzen.pilightmqttosgi;


import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.wiringpi.Gpio;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import javax.jms.*;
import javax.jms.ConnectionFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PirSensor {


    //@Resource(lookup = "jms/ConnectionFactory")
    //private static ConnectionFactory connectionFactory;

    public PirSensor() {
        logger.info("Start Gpio.wiringPiSetupGpio pir");
        Gpio.wiringPiSetupGpio();
        try {
            Thread.sleep(2000L);
        } catch (Exception ex) {
        }
        ;
        // create gpio controller
        gpio = GpioFactory.getInstance();


    }

    volatile int eventnr=0;
    public void init() {
        logger.info("Start gpio.provisionDigitalInputPin GPIO_04 pir");
        gpioPir = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04, "PIR"); // 02 and 03 conflicts with i2c?
        logger.info("End gpio.provisionDigitalInputPin GPIO_04 pir");
        gpioPir.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                // display pin state on console
                logger.info(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = "
                        + event.getState());
                // send to jms queue


                //Creates a Connection and a Session:

                // A lot of problems with spurious pin changes 20200330
                if (++eventnr % 1 == 0) {
                    try {
                        // Send payload
                        MqttClient client = new MqttClient("tcp://127.0.0.1:1883", "pahomqttpublish1");
                        client.connect();
                        MqttMessage message = new MqttMessage();
                        message.setPayload(event.getState().toString().getBytes());
                        logger.info("Sending message: " + message.toString());
                        client.publish("f0/nnw/pir", message);
                        client.disconnect();
                        client.close();
                    } catch (MqttException ex1) {
                        logger.log(Level.SEVERE, "PIR send message to paho mqtt error", ex1);
                    }
                }
            }
        });
    }

    public void destroy() {
        gpioPir.removeAllListeners();
        gpio.unprovisionPin(gpioPir);
    }

    public GpioPinDigitalInput gpioPir;

    final static Logger logger = Logger.getLogger(PirSensor.class.toString());
    // create gpio controller
    final GpioController gpio;
}