package nl.vandenzen.pilightmqttosgi;


import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListener;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.camel.component.ActiveMQComponent;

import javax.annotation.Resource;
import javax.jms.*;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;

import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PirSensor {

    // create gpio controller
    final GpioController gpio = GpioFactory.getInstance();


    //@Resource(lookup = "jms/ConnectionFactory")
    //private static ConnectionFactory connectionFactory;

    public PirSensor() {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://amq-broker");

        ActiveMQComponent ac = ActiveMQComponent.activeMQComponent();
        ac.setConnectionFactory(connectionFactory);
        ac.setUsername("karaf");
        ac.setPassword("karaf");
        gpioPir = gpio.provisionDigitalInputPin(RaspiPin.GPIO_03, "PIR");
        gpioPir.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                // display pin state on console
                logger.info(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = "
                        + event.getState());
                // send to jms queue


                //Creates a Connection and a Session:

                try {
                    Connection connection = connectionFactory.createConnection();
                    connection.start();
                    Session session = connection.createSession(
                            false,
                            Session.AUTO_ACKNOWLEDGE);

                    //Creates a MessageProducer and a TextMessage:
                    // Create the destination (Topic or Queue). Topic floor 0, north west west, pir sensor
                    Destination destination = session.createTopic("f0.nnw.pir");
                    MessageProducer producer = session.createProducer(destination);
                    TextMessage message = session.createTextMessage();

                    //Sends one or more messages to the destination:

                    message.setText(event.getState().toString());
                    logger.info("Sending message: " + message.getText());
                    producer.send(message);

                    //Sends an empty control message to indicate the end of the message stream:

                    //producer.send(session.createMessage());
                    connection.stop();
                    connection.close();
                } catch (JMSException ex) {
                    logger.log(Level.SEVERE, "PIR send message to jms queue error", ex);
                }
            }
        });
    }

    public final GpioPinDigitalInput gpioPir;

    final static Logger logger = Logger.getLogger(MyRouteBuilder.class.toString());
}
