package nl.vandenzen.iot.rpidisplay.contentprovider;

import nl.vandenzen.iot.rpidisplay.ContentProvider;

public class ContentProvider1 implements ContentProvider {

    @Override
    public String getContent() {
        return "Dit is ContentProvider1";
    }
}
