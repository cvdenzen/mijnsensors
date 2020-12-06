package nl.vandenzen.iot.beans;


import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExtendableDelay {

    // Inject a dummy uri. This is the easiest way to initialize a producer.
    @EndpointInject(uri="direct:foo.bar")
    ProducerTemplate producer;

    /**
     *
     * @param uri The uri to call when delay has finished
     */
    public ExtendableDelay(String uri) {
        this.uri = uri;
        logger.log(Level.INFO,"Extendable delay, Bean created with uri "+uri);
    }

    /**
     * Start or restart timer.
     * @param exchange
     */
    public void restartTimer(Exchange exchange) {
        logger.log(Level.INFO,"ExtendableDelay.restartTimer started with delay of "+this.delay);
        this.exchange=exchange;

        TimerTask timerTask = new TimerTask() {
            public void run() {
                try {
                    lock.lock();
                    if (timer!=null) {
                        producer = exchange.getContext().createProducerTemplate();
                        logger.log(Level.INFO, "ExtendableDelay tripped, created producer: " + producer);
                        producer.send(uri, exchange);
                        logger.log(Level.INFO, "ExtendableDelay tripped and started: " + uri);
                        timer = null; // signal for restartTimer
                    }
                } finally {
                    lock.unlock();
                }
            }
        };

        try {
            lock.lock();
            if (timer != null)
                // already started, cancel to start new period
                    timer.cancel();
            // Start a timer unconditionally.
            timer = new Timer();
            timer.schedule(timerTask, this.delay);
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Start a new timer, but only if not yet started
     */
    public void schedule(Exchange exchange) {
        logger.log(Level.INFO,"Start method schedule timer, lock="+lock+", timer="+timer);
        try {
            lock.lock();
            if (timer==null) {
                logger.log(Level.INFO,"Schedule timer");
                restartTimer(exchange);
            }
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Extend time, but only if timer already has been started
     * @param exchange
     */
    public void extend(Exchange exchange) {
        logger.log(Level.INFO,"Start method extend timer, lock="+lock+", timer="+timer);
        try {
            lock.lock();
            if (timer!=null) {
                logger.log(Level.INFO,"Extend timer");
                restartTimer(exchange);
            } else {
                logger.log(Level.INFO,"timer is not running, hence no action");
            }
        }
        finally {
            lock.unlock();
        }

    }

    /**
     * Cancel the timer
     */
    public void cancel() {
        logger.log(Level.INFO,"Start method cancel timer, lock="+lock+", timer="+timer);
        try {
            lock.lock();
            if (timer!=null) {
                logger.log(Level.INFO, "Cancel timer");
                timer.cancel();
                timer=null; // signal nothing is running
            }
        }
        finally {
            lock.unlock();
        }
    }
    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    private Timer timer;

    final private String uri;
    private long delay=0;
    private Exchange exchange;
    final static Logger logger = Logger.getLogger(ExtendableDelay.class.toString());
    final ReentrantLock lock=new ReentrantLock();
}
