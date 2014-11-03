package org.bonitasoft.engine.data.instance.model.impl;

import java.util.Map;
import java.util.WeakHashMap;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class XStreamFactory {

    private static final Map<ClassLoader, XStream> XSTREAM_MAP = new WeakHashMap<ClassLoader, XStream>();

    public static XStream getXStream() {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        XStream xStream = XSTREAM_MAP.get(classLoader);
        if (xStream == null) {
            xStream = new XStream(new StaxDriver());
            XSTREAM_MAP.put(classLoader, xStream);
        }
        return xStream;
    }
}
