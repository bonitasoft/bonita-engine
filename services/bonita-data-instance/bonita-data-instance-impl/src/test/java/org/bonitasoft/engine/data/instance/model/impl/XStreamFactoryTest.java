package org.bonitasoft.engine.data.instance.model.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.thoughtworks.xstream.XStream;

public class XStreamFactoryTest {

    @Test
    public void should_provide_xstream() throws Exception {
        //given
        final XStreamFactory xStreamFactory = new XStreamFactory();

        //when
        final XStream xStream = xStreamFactory.getXStream();

        //then
        assertThat(xStream).as("should provide xstream instance").isNotNull();
    }

    @Test
    public void should_create_xstream_only_once() throws Exception {
        //given
        final XStreamFactory xStreamFactory = new XStreamFactory();

        //when
        final XStream xStream = xStreamFactory.getXStream();
        final XStream xStream2 = xStreamFactory.getXStream();

        //then
        assertThat(xStream2).as("should provide xstream instance").isSameAs(xStream);
    }
}
