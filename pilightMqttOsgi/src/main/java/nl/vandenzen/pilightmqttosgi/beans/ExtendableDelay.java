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

    /**
     *
     * @param uri The uri to call when delay has finished
     */
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
                // already started, cancel to start new period
                    timer.cancel();
        }
        finally {
            lock.unlock();
        }
        // Only start a timer if light is off or already started.
        // In the last case, the time will be extended.
        if (!lightState || (timer!=null)) {
            timer = new Timer();

            TimerTask timerTask = new TimerTask() {
                public void run() {
                    try {
                        lock.lock();
                        producer.send(uri, exchange);
                        logger.log(Level.INFO,"ExtendableDelay: turn off light: "+uri);
                        timer = null; // signal for restartTimer
                    } finally {
                        lock.unlock();
                    }
                }
            };
            timer.schedule(timerTask, this.delay);
        }
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

    public boolean isBg_w_tv_w() {
        return bg_w_tv_w;
    }

    public void setBg_w_tv_w(boolean bg_w_tv_w) {
        this.bg_w_tv_w = bg_w_tv_w;
    }

    public boolean isBg_x_imac3() {
        return bg_x_imac3;
    }

    public void setBg_x_imac3(boolean bg_x_imac3) {
        this.bg_x_imac3 = bg_x_imac3;
    }

    private Timer timer;

    final private String uri;
    private long delay=0;
    private Exchange exchange;
    final static Logger logger = Logger.getLogger(ExtendableDelay.class.toString());
    final ReentrantLock lock=new ReentrantLock();
    private boolean lightState;
    private boolean bg_w_tv_w;
    private boolean bg_x_imac3;
}
