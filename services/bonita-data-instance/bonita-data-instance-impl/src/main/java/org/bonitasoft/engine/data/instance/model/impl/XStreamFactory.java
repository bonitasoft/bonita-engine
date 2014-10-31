package org.bonitasoft.engine.data.instance.model.impl;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class XStreamFactory {

    private static XStream xstream;

    public XStream getXStream() {
        if (xstream == null) {
            createXStream();
        }
        return xstream;
    }

    private void createXStream() {
        xstream = new XStream(new StaxDriver());
    }

}
