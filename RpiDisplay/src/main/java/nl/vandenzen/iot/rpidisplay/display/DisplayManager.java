package nl.vandenzen.iot.rpidisplay.display;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import nl.vandenzen.iot.rpidisplay.ContentProvider;

import java.util.logging.Logger;

public class DisplayManager implements BundleActivator, Runnable {
    private volatile Thread thread;
    private ServiceTracker<ContentProvider, ContentProvider> tracker;

    public void start(BundleContext context) {
        tracker = new ServiceTracker<>(context, ContentProvider.class, null);
        tracker.open();
        thread = new Thread(this, "DisplayManager Whiteboard");
        thread.start();
    }

    public void stop(BundleContext context) {
        tracker.close();
        thread = null;
    }

    public void run() {
        Thread current = Thread.currentThread();
        int n = 0;
        log.info("WHITEBOARD: Start run()");
        while (current == thread) {
            log.info("WHITEBOARD: Start while loop, n=" +n);
            ContentProvider[] providers = tracker.getServices(new ContentProvider[0]);
            if (providers.length > 0) {
                if (n >= providers.length)
                    n = 0;
                ContentProvider cp = providers[n++];
                log.info("WHITEBOARD " + cp.getContent());
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }
        }
    }
    private static Logger log = Logger.getLogger(DisplayManager.class.getSimpleName());
}