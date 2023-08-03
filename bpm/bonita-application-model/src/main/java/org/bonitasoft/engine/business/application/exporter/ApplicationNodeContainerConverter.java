/**
 * Copyright (C) 2023 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.business.application.exporter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.bonitasoft.engine.business.application.xml.ApplicationNodeContainer;
import org.xml.sax.SAXException;

public class ApplicationNodeContainerConverter {

    private static final String APPLICATION_XSD = "/application.xsd";

    public byte[] marshallToXML(final ApplicationNodeContainer applicationNodeContainer)
            throws JAXBException, IOException, SAXException {
        return marshallObjectToXML(applicationNodeContainer);
    }

    public ApplicationNodeContainer unmarshallFromXML(final byte[] applicationXML)
            throws JAXBException, IOException, SAXException {
        return unmarshallXMLtoObject(applicationXML);
    }

    private byte[] marshallObjectToXML(final ApplicationNodeContainer jaxbModel)
            throws JAXBException, IOException, SAXException {
        if (jaxbModel == null) {
            return new byte[0];
        }
        final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final Schema schema = sf.newSchema(ApplicationNodeContainer.class.getResource(APPLICATION_XSD));
        try (final ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            final JAXBContext contextObj = JAXBContext.newInstance(jaxbModel.getClass());
            final Marshaller m = contextObj.createMarshaller();
            m.setSchema(schema);
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
            m.marshal(jaxbModel, stream);
            return stream.toByteArray();
        }
    }

    private ApplicationNodeContainer unmarshallXMLtoObject(final byte[] xmlObject)
            throws JAXBException, IOException, SAXException {
        if (xmlObject == null) {
            return null;
        }
        final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final Schema schema = sf.newSchema(ApplicationNodeContainer.class.getResource(APPLICATION_XSD));
        final JAXBContext contextObj = JAXBContext.newInstance(ApplicationNodeContainer.class);
        final Unmarshaller um = contextObj.createUnmarshaller();
        um.setSchema(schema);
        try (final ByteArrayInputStream stream = new ByteArrayInputStream(xmlObject)) {
            final JAXBElement<ApplicationNodeContainer> jaxbElement = um.unmarshal(new StreamSource(stream),
                    ApplicationNodeContainer.class);
            return jaxbElement.getValue();
        }
    }
}
