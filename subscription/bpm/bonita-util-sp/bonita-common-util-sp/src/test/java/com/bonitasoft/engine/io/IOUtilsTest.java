/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.io;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import com.bonitasoft.engine.bdm.builder.BusinessObjectModelBuilder;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;

public class IOUtilsTest {

    private final URL resource = BusinessObjectModel.class.getResource("/bom.xsd");

    @Test
    public void marshallAndUnmarshallShouldReturnTheSameObject() throws Exception {
        final BusinessObjectModel expected = new BusinessObjectModelBuilder().buildDefaultBOM();
        final byte[] xml = IOUtils.marshallObjectToXML(expected, resource);
        final BusinessObjectModel actual = IOUtils.unmarshallXMLtoObject(xml, BusinessObjectModel.class, resource);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void marshallANullObjectReturnsNull() throws Exception {
        final byte[] xml = IOUtils.marshallObjectToXML(null, resource);

        assertThat(xml).isNull();
    }

    @Test(expected = JAXBException.class)
    public void marshallAnObjectWithoutJAXBAnnotationsThrowsAJAXBException() throws Exception {
        final String message = "JAXB or not JAXB?";
        IOUtils.marshallObjectToXML(message, resource);
    }

    @Test
    public void unmarshallANullObjectReturnsNull() throws Exception {
        final BusinessObjectModel object = IOUtils.unmarshallXMLtoObject(null, BusinessObjectModel.class, resource);

        assertThat(object).isNull();
    }

    @Test(expected = JAXBException.class)
    public void unmarshallAnObjectWithoutJAXBAnnotationsThrowsAJAXBException() throws Exception {
        final String xml = "something";
        IOUtils.unmarshallXMLtoObject(xml.getBytes(), BusinessObjectModel.class, resource);
    }

}
