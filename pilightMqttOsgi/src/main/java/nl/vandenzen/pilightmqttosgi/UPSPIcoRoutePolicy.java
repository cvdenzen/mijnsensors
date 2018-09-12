package nl.vandenzen.pilightmqttosgi;

import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.apache.camel.support.RoutePolicySupport;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UPSPIcoRoutePolicy extends RoutePolicySupport{

    @Override
    public void onStart(Route route) {
        // TODO Auto-generated method stub
        super.onStart(route);
    }

    @Override
    public void onStop(Route route) {
        // TODO Auto-generated method stub
        super.onStop(route);
        Set upsPicos=route.getRouteContext().getCamelContext().getRegistry().findByType(UPSPIco.class);
        if (upsPicos != null) {
            if (upsPicos.size() != 1) {
                logger.log(Level.SEVERE, "There is not exactly one bean of type {} in the registry, number is {}",
                        new Object[]{UPSPIco.class, upsPicos.size()});
            } else {
                logger.log(Level.INFO,"Bean found in registry, bean type is {}, invoking stop() method",UPSPIco.class);
                UPSPIco upsPico;
                Object[] o=upsPicos.toArray();
                upsPico=(UPSPIco) o[0];
                upsPico.stop();
                logger.log(Level.INFO,"Bean found in registry, bean type is {}, invoked stop() method",UPSPIco.class);
            }
        } else {
            logger.log(Level.SEVERE,"No bean {} found in registry",UPSPIco.class);
        }
    }

    @Override
    public void onExchangeBegin(Route route, Exchange exchange) {
        // TODO Auto-generated method stub
        super.onExchangeBegin(route, exchange);
    }


    final static Logger logger = Logger.getLogger(UPSPIcoRoutePolicy.class.toString());
}
