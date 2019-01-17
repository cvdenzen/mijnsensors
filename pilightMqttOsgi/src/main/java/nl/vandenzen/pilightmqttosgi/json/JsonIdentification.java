package nl.vandenzen.pilightmqttosgi.json;

// To Pilight
public class JsonIdentification {
    public JsonIdentification(String action, Integer core, Integer receiver, Integer config, Integer forward, String uuid, String media) {
        this.action = action;
        JsonIdentification.IdentificationOptions options = this.new IdentificationOptions(core, receiver, config, forward);
        this.options = options;
        this.uuid = uuid;
        this.media = media;
    }

    public String action;
    public IdentificationOptions options;
    public String uuid;
    public String media;

    public class IdentificationOptions {
        public IdentificationOptions(Integer core, Integer receiver, Integer config, Integer forward) {
            this.core = core;
            this.receiver = receiver;
            this.config = config;
            this.forward = forward;
        }

        public Integer core;
        public Integer receiver;
        public Integer config;
        public Integer forward;
    }
}

/**
 * pilightIdentify without newlines because it is input to pilight. I am not
 * sure whether newlines are allowed.
 */
    /*
    final static String pilightIdentify = "{" +
            "\"action\":\"identify\"," +
            "\"options\":{" +
            "\"core\":1," +
            "\"receiver\": 1," +
            "\"config\":1," +
            "\"forward\":0" +
            "}," +
            "\"uuid\":\"0000-d0-63-03-000000\"," +
            "\"media\":\"all\"" +
            "}";
*/