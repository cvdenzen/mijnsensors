package nl.vandenzen.pilightmqttosgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.logging.Logger;

public class Activator implements BundleActivator {

    public void start(BundleContext context) throws Exception {
        String msg = "Hallo, dit is Activator voor pilight-mqtt, context=" + context;
        System.out.println(msg);
        logger.info(msg);
    }

    public void stop(BundleContext context) throws Exception {
        String msg = "Hallo, dit is Activator.stop() pilight-mqtt";
        System.out.println(msg);

    }

    final static Logger logger = Logger.getLogger(Activator.class.getName());
}
