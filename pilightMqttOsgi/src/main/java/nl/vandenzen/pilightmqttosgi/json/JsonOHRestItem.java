package nl.vandenzen.pilightmqttosgi.json;

/**
 * Class that is returned by OpenHAB rest service.
 * {"link":"http://127.0.0.1:8080/rest/items/LightsOnIntensityThreshold",
 * "state":"7",
 * "stateDescription":{"pattern":"%.1f", "readOnly":false," options":[]},
 * "editable":false,
 * "type":"Number",
 * "name":"LightsOnIntensityThreshold",
 * "label":"Lichtintensiteit waarbij lichten worden ingeschakeld",
 * "tags":[],
 * "groupNames":["LightSensor","gInitializeZero"]}
 */
public class JsonOHRestItem {

    private String link;
    private String state; // not type safe
    private StateDescription stateDescription;
    private boolean editable;
    private String type;
    private String name;
    private String label;
    private String[] tags;
    private String[] groupNames;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public StateDescription getStateDescription() {
        return stateDescription;
    }

    public void setStateDescription(StateDescription stateDescription) {
        this.stateDescription = stateDescription;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String[] getGroupNames() {
        return groupNames;
    }

    public void setGroupNames(String[] groupNames) {
        this.groupNames = groupNames;
    }

    public class StateDescription {
        private String pattern;
        private boolean readOnly;
        String[] options;

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public boolean isReadOnly() {
            return readOnly;
        }

        public void setReadOnly(boolean readOnly) {
            this.readOnly = readOnly;
        }

        public String[] getOptions() {
            return options;
        }

        public void setOptions(String[] options) {
            this.options = options;
        }

        public String toString() {
            return this.getClass().getName()
                    + "{ "
                    + "pattern=" + pattern + ", "
                    + "readOnly=" + readOnly + ", "
                    + "options=" + options + "}\n";
        }
    }

    public String toString() {
        return this.getClass().getName()
                + "{\n"
                + "link=" + link + ", "
                + "state=" + state + ", "
                + "stateDescription=" + stateDescription
                + "editable=" + editable + ", "
                + "type=" + type + ", "
                + "name=" + name + ", "
                + "label=" + label + ", "
                + "tags=" + tags + ", "
                + "groupNames=" + groupNames
                + "}";

    }
}
