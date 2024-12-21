package nl.vandenzen.iot.util;


import java.util.logging.Logger;

public class MutableFloat {
    private static final Logger LOG = Logger.getLogger(MutableFloat.class.getName());

    volatile float value;

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        LOG.info("MutableFloat changed from " + this.value+" to " + value+": "+this.toString());
        this.value = value;
    }
}
