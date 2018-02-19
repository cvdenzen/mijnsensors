package nl.vandenzen.mijnsensors.mqttpilight.json;


// To Pilight
// control action: send high level code to device
public class JsonActionControl {
    public JsonActionControl(String action, String device, String state, Integer dimlevel) {
        this.action = action;
        this.code = new ControlCode(device, state, new ControlCodes(dimlevel));
    }

    String action;
    ControlCode code;

    public class ControlCode {
        public ControlCode(String device, String state, ControlCodes values) {
            this.device = device;
            this.state = state;
            this.values = values;
        }

        public String device;
        public String state; // on of off
        public ControlCodes values;
    }

    public class ControlCodes {
        public ControlCodes(Integer dimlevel) {
            this.dimlevel = dimlevel;
        }

        public Integer dimlevel;
    }

}