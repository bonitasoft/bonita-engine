package org.bonitasoft.engine.data.instance.model.impl;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class XStreamFactory {

    private final Object lock = new Object();
    private static XStream xstream;

    public XStream getXStream() {
        synchronized (lock) {
            if (xstream == null) {
                createXStream();
            }
        }
        return xstream;
    }

    private void createXStream() {
        xstream = new XStream(new StaxDriver());
    }

}
