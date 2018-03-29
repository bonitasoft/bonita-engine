/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.io;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import org.bonitasoft.engine.bdm.builder.BusinessObjectModelBuilder;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;

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
