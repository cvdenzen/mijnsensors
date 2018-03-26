package nl.vandenzen.pilightmqttosgi;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    public void start(BundleContext context) throws Exception {
        String msg = "Hallo, dit is Activator voor pilight-mqtt";
        System.out.println(msg);
        logger.info(msg);

        //MyRouteBuilder mrb=new MyRouteBuilder();
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    String msg = "Hallo, dit is thread.run() pilight-mqtt";
                    System.out.println(msg);
                    MyRouteBuilder.main(null);
                    msg = "Hallo, dit is thread.run() na pilight-mqtt";
                    System.out.println(msg);
                } catch (Exception ex) {
                    Logger.getLogger(Activator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        msg = "Hallo, dit is voor thread.starrt pilight-mqtt";
        System.out.println(msg);
        thread.start();
        msg = "Hallo, dit is na thread.start pilight-mqtt";
        System.out.println(msg);
    }

    public void stop(BundleContext context) throws Exception {
        // TODO add deactivation code here
        String msg = "Hallo, dit is Activator.stop() pilight-mqtt";
        System.out.println(msg);
    }
    final static Logger logger = Logger.getLogger(Activator.class.getName());
}
