package nl.vandenzen.mijnsensors.mqttpilight.json;

// To Pilight
public class JsonIdentification {
    public JsonIdentification(String action, Integer core, Integer receiver, Integer config, Integer forward, String uuid, String media) {
        this.action = action;
        this.options.core = core;
        this.options.receiver = receiver;
        this.options.config = config;
        this.options.forward = forward;
        this.options = options;
        this.uuid = uuid;
        this.media = media;
    }

    public String action;
    public IdentificationOptions options;
    public String uuid;
    public String media;

    public class IdentificationOptions {
        public Integer core;
        public Integer receiver;
        public Integer config;
        public Integer forward;
    }
}
