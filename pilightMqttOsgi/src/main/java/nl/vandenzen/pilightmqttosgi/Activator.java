package nl.vandenzen.pilightmqttosgi;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Activator implements BundleActivator {

    final boolean useBlueprint = false; // blueprint.xml is used anyway.
    RouteBuilder myRouteBuilder;
    MyCamelController myCamelController;
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
                        myCamelController = new MyCamelController(context);
                        myCamelController.setContext(new DefaultCamelContext());
                        myCamelController.start();
                        msg = "Hallo, dit is thread.run() na myRouteBuilder.start()";
                        logger.info(msg);
                        System.out.println(msg);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(Activator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        msg = "Hallo, dit is voor thread.start pilight-mqtt";
        System.out.println(msg);
        if (!useBlueprint) {
            thread.start();
        }
        msg = "Hallo, dit is na thread.start pilight-mqtt";
        System.out.println(msg);
    }

    public void stop(BundleContext context) throws Exception {
        // TODO add deactivation code here
        if (!useBlueprint) {
            myCamelController.stop();
        }
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
