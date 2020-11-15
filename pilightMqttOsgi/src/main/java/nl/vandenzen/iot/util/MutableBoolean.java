package nl.vandenzen.iot.util;

public class MutableBoolean {
    volatile boolean value;

    public boolean isValue() {
        return value;
    }

    public boolean getValue() {
        return value;
    }
    public void setValue(boolean value) {
        this.value = value;
    }
}
