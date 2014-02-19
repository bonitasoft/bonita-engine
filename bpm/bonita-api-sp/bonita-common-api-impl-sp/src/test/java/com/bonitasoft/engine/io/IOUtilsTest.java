package com.bonitasoft.engine.io;

import static org.assertj.core.api.Assertions.assertThat;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import com.bonitasoft.engine.bdm.BOMBuilder;
import com.bonitasoft.engine.bdm.BusinessObjectModel;

public class IOUtilsTest {

    @Test
    public void marshallAndUnmarshallShouldReturnTheSameObject() throws Exception {
        final BusinessObjectModel expected = new BOMBuilder().buildDefaultBOM();
        final byte[] xml = IOUtils.marshallObjectToXML(expected);
        final BusinessObjectModel actual = IOUtils.unmarshallXMLtoObject(xml, BusinessObjectModel.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void marshallANullObjectReturnsNull() throws Exception {
        final byte[] xml = IOUtils.marshallObjectToXML(null);

        assertThat(xml).isNull();
    }

    @Test(expected = JAXBException.class)
    public void marshallAnObjectWithoutJAXBAnnotationsThrowsAJAXBException() throws Exception {
        final String message = "JAXB or not JAXB?";
        IOUtils.marshallObjectToXML(message);
    }

    @Test
    public void unmarshallANullObjectReturnsNull() throws Exception {
        final BusinessObjectModel object = IOUtils.unmarshallXMLtoObject(null, BusinessObjectModel.class);

        assertThat(object).isNull();
    }

    @Test(expected = JAXBException.class)
    public void unmarshallAnObjectWithoutJAXBAnnotationsThrowsAJAXBException() throws Exception {
        final String xml = "something";
        IOUtils.unmarshallXMLtoObject(xml.getBytes(), BusinessObjectModel.class);
    }

}
