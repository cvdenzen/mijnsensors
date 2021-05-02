package nl.vandenzen.iot.rpidisplay;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.Date;

class Clock implements ContentProvider {

    Clock() {
    }

    public String getContent() {
        return new Date().toString();
    }
}

public class ClockManager implements BundleActivator {
    private ServiceRegistration<ContentProvider> registration;

    public void start(BundleContext context) {
        registration = context.registerService(ContentProvider.class, new Clock(), null);
    }

    public void stop(BundleContext context) {
    }
}