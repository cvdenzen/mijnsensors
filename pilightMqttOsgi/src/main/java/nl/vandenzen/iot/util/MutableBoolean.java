package nl.vandenzen.iot.util;

import java.util.logging.Logger;

public class MutableBoolean {
    private static final Logger LOG = Logger.getLogger(MutableBoolean.class.getName());
    volatile boolean value;


    public MutableBoolean() {
        this(false);
    }
    public MutableBoolean(boolean value) {
        this.value = value;
        LOG.info(this.toString() + " created with value " + this.value);
    }

    public boolean isValue() {
        return value;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        LOG.info(this.toString() + " changed from " + this.value + " to " + value);
        this.value = value;
    }
}
