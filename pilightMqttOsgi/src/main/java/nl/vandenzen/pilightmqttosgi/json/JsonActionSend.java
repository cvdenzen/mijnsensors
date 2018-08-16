package nl.vandenzen.pilightmqttosgi.json;
// To Pilight
// send action: send low level code
/*
{
  "action": "send",
  "code": {
    "protocol": [ "kaku_switch" ],
    "id": 1234,
    "unit": 0,
    "off": 1
  }
}
 */


public class JsonActionSend {
    public String action;
    public ActionCode code;

    public class ActionCode {
        public String[] protocol;
        public Integer id;
        public Integer unit;
        public Integer off;
        public Integer on;
    }
}
