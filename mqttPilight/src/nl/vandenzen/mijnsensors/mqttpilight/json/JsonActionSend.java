package nl.vandenzen.mijnsensors.mqttpilight.json;
// To Pilight
// send action: send low level code
public class JsonActionSend {
    public String action;
    public ActionCode code;

    public class ActionCode {
        public String[] protocol;
        public Integer id;
        public Integer unit;
        public Integer off;
    }
}
