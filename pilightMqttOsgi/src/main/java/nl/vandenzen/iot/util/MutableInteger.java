package nl.vandenzen.iot.util;

import org.apache.camel.Exchange;

public class MutableInteger extends AbstractMutableAny<Integer> {
    public void setValue(Exchange exchange, Integer value) {
        super.setValue(exchange, value);
    }

    public void setValue(Integer value) {
        super.setValue(value);
    }

    /**
     * Camel is complaining if no getter available (jan 2025):
     * Unable to start container for blueprint bundle nl.vandenzen.pilightMqttOsgi/1.0.0.SNAPSHOT
     * org.osgi.service.blueprint.container.ComponentDefinitionException:
     * Error setting property: PropertyDescriptor <name: value,
     * getter: class nl.vandenzen.iot.util.AbstractMutableAny.getValue(),
     * setter: [class nl.vandenzen.iot.util.MutableInteger.setValue(class java.lang.Integer)]
     *
     * @return
     */
    public Integer getValue() {
        return super.getValue();
    }
}
