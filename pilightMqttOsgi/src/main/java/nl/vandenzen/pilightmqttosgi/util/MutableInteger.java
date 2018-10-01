package nl.vandenzen.pilightmqttosgi.util;

public class MutableInteger {
    volatile int value;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
