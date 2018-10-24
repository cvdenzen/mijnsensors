package nl.vandenzen.pilightmqttosgi.beans;


import org.apache.camel.EndpointInject;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Timer;
import java.util.TimerTask

public class ExtendableDelay {

    // Inject a dummy uri. This is the easiest way to initialize a producer.
    @EndpointInject(uri="activemq:foo.bar")
    ProducerTemplate producer;

    public ExtendableDelay(String uri) {
        this.uri = uri;
    }
    public void restartTime() {
        if (timer!=null) timer.cancel();
        timer=new Timer();
        timer.schedule(timerTask,this.delay);

    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    private Timer timer;
    private TimerTask timerTask = new TimerTask() {
        public void run() {
            producer.send(uri,exchange);
        }
    }
    final private String uri;
    private long delay=0;
    private Exchange exchange;
    final static Logger logger = Logger.getLogger(ExtendableDelay.class.toString());
}
