package nl.vandenzen.iot.util;


import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.spi.Registry;

import java.util.Map;
import java.util.logging.Logger;

public class AbstractMutableAny<T> {
    private static final Logger LOG = Logger.getLogger(AbstractMutableAny.class.getName());

    public AbstractMutableAny() {
    }

    public AbstractMutableAny(T value) {
        this.value = value;
    }

    ;
    volatile T value;

    protected T getValue() {
        return value;
    }

    protected void setValue(Exchange exchange, T value) {
        // Find the name if it is not known yet
        if (name == null && exchange != null) {
            CamelContext camelContext = exchange.getContext();
            if (camelContext != null) {
                Registry registry = camelContext.getRegistry();
                if (registry != null) {
                    Map<String, AbstractMutableAny> beans = registry.findByTypeWithName(AbstractMutableAny.class);
                    // Find our bean
                    if (beans != null) {
                        for (Map.Entry<String, AbstractMutableAny> e : beans.entrySet()) {
                            if (e.getValue() == this) {
                                name = e.getKey();
                            }
                        }
                    }
                }
            }
        }
        if (value!=null && value.equals(this.value)) {
            LOG.info(name + " no change:" + value);
        } else {
            LOG.info(name + " changed from " + this.value + " to " + value);
        }
        this.value = value;
    }

    protected void setValue(T value) {
        this.value = value;
    }

    private String name;
}
