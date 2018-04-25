package nl.vandenzen.pilightmqttosgi;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.camel.ProducerTemplate;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    final boolean useBlueprint = false;

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
                    if (!useBlueprint) {
                        MyRouteBuilder.main(null);
                        msg = "Hallo, dit is thread.run() na pilight-mqtt";
                        System.out.println(msg);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(Activator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        msg = "Hallo, dit is voor thread.starrt pilight-mqtt";
        System.out.println(msg);
        if (!useBlueprint) {
            thread.start();
        }
        msg = "Hallo, dit is na thread.start pilight-mqtt";
        System.out.println(msg);
    }

    public void stop(BundleContext context) throws Exception {
        // TODO add deactivation code here
        String msg = "Hallo, dit is Activator.stop() pilight-mqtt";
        System.out.println(msg);
    }


    private void sendMessage() {

   String msg = "main: context started";
        System.out.println(msg);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //template.sendBody("activemq:queue:test.queue", MyRouteBuilder.json[0]);
        msg = "main: sendBody done";
        System.out.println(msg);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    final static Logger logger = Logger.getLogger(Activator.class.getName());
}
