package nl.vandenzen.iot.util;

public class MutableLong {
    volatile long value;

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    // Assume value is in minutes
    public long getMilliSeconds() {
        return 60 * 1000 * value;
    }
}
