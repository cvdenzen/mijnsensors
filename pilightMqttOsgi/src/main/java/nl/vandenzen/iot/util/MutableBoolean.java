package nl.vandenzen.iot.util;

import java.util.logging.Logger;

public class MutableBoolean {
    private static final Logger LOG = Logger.getLogger(MutableFloat.class.getName());
    volatile boolean value;

    public boolean isValue() {
        return value;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        LOG.info(this.getClass().getName()+" changed from " + this.value + " to " + value + ": " + this.toString());
        this.value = value;
    }
}
