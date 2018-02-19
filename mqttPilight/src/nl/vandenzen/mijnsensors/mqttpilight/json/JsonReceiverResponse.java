package nl.vandenzen.mijnsensors.mqttpilight.json;

// From Pilight
public class JsonReceiverResponse {
    public Message message;
    public String origin;
    public String protocol;
    public String uuid;
    public Integer repeats;

    public class Message {
        public Integer id;
        public Integer unit;
        public String state;
    }


}
