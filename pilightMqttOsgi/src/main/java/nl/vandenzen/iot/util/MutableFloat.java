package nl.vandenzen.iot.util;

public class MutableFloat {
    volatile float value;

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
