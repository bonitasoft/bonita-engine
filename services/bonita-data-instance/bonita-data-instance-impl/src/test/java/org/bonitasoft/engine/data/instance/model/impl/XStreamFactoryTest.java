package org.bonitasoft.engine.data.instance.model.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.thoughtworks.xstream.XStream;

public class XStreamFactoryTest {

    @Test
    public void should_provide_xstream() throws Exception {
        //when
        final XStream xStream = XStreamFactory.getXStream();

        //then
        assertThat(xStream).as("should provide xstream instance").isNotNull();
    }

    @Test
    public void should_create_xstream_only_once_in_same_classLoader() throws Exception {
        //when
        final XStream xStream = XStreamFactory.getXStream();
        final XStream xStream2 = XStreamFactory.getXStream();

        //then
        assertThat(xStream2).as("should provide the xstream instance when we are in the same classloader").isSameAs(xStream);
    }
}
