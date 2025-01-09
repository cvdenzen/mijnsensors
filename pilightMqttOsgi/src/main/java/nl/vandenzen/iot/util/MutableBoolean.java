package nl.vandenzen.iot.util;
import org.apache.camel.Exchange;
public class MutableBoolean extends AbstractMutableAny<Boolean> {
    public MutableBoolean() {
        super();
    }

    public MutableBoolean(String s) {
        super(Boolean.valueOf(s));
    }

    /**
     * jan 2025: Camel error
     * Unable to start container for blueprint bundle nl.vandenzen.pilightMqttOsgi/1.0.0.SNAPSHOT
     * org.osgi.service.blueprint.container.ComponentDefinitionException:
     * Error setting property: PropertyDescriptor <name: value,
     * getter: class nl.vandenzen.iot.util.MutableBoolean.getValue(),
     * setter: [class nl.vandenzen.iot.util.AbstractMutableAny.setValue(class java.lang.Object)]
     *
     * @param o
     */

    public void setValue(Exchange exchange, Boolean value) {
        super.setValue(exchange, value);
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
    public Boolean getValue() {
        return super.getValue();
    }
    public void setValue(Boolean value) {
        setValue(null,value);
    }
}