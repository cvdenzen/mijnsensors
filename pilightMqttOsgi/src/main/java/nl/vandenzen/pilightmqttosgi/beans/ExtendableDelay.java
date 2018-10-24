package nl.vandenzen.pilightmqttosgi.beans;


import org.apache.camel.EndpointInject;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultExchange;

import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Timer;
import java.util.TimerTask;

public class ExtendableDelay {

    // Inject a dummy uri. This is the easiest way to initialize a producer.
    @EndpointInject(uri="activemq:foo.bar")
    ProducerTemplate producer;

    public ExtendableDelay(String uri) {
        this.uri = uri;
        logger.log(Level.INFO,"Bean created with uri "+uri);
    }

    /**
     * Start or restart timer. When first time, also check whether light is on or off.
     * Only start when light is off.
     * @param exchange
     */
    public void restartTimer(Exchange exchange) {
        logger.log(Level.INFO,"ExtendableDelay.restartTimer started");
        this.exchange=exchange;
        try {
            lock.lock();
            if (timer != null)
                    timer.cancel();
        }
        finally {
            lock.unlock();
        }
        // todo:
        // if light is off || (timer!=null)
        timer=new Timer();

        TimerTask timerTask = new TimerTask() {
            public void run() {
                try {
                    lock.lock();
                    producer.send(uri,exchange);
                    timer=null; // signal for restartTimer
                }
                finally {
                    lock.unlock();
                }
            }
        };
        timer.schedule(timerTask,this.delay);

    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public boolean isLightState() {
        return lightState;
    }

    public void setLightState(boolean lightState) {
        this.lightState = lightState;
    }

    private Timer timer;

    final private String uri;
    private long delay=0;
    private Exchange exchange;
    final static Logger logger = Logger.getLogger(ExtendableDelay.class.toString());
    final ReentrantLock lock=new ReentrantLock();
    private boolean lightState;
}
