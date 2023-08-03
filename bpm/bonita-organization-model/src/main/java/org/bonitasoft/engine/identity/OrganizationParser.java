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
package org.bonitasoft.engine.identity;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.SchemaFactory;

import org.bonitasoft.engine.identity.xml.Organization;
import org.xml.sax.SAXException;

/**
 * @author Baptiste Mesta
 */
public class OrganizationParser {

    public static final String ORGANIZATION_XML_SCHEMA = "http://documentation.bonitasoft.com/organization-xml-schema";
    public static final String ORGANIZATION_XML_VERSION = "1.1";
    public static final String ORGANIZATION_NAMESPACE = ORGANIZATION_XML_SCHEMA + "/" + ORGANIZATION_XML_VERSION;

    private static final String ORGANIZATION_XSD = "/organization.xsd";

    private final JAXBContext jaxbContext;

    public OrganizationParser() {
        try {
            jaxbContext = JAXBContext.newInstance(Organization.class);
        } catch (final Exception e) {
            throw new DataBindingException(e);
        }
    }

    public Organization convert(String organizationContent) throws JAXBException {
        if (!organizationContent.contains(ORGANIZATION_NAMESPACE)) {
            organizationContent = organizationContent.replace(ORGANIZATION_XML_SCHEMA, ORGANIZATION_NAMESPACE);
        }
        return (Organization) createUnmarshaller().unmarshal(new StringReader(organizationContent));
    }

    public String convert(Organization organization) throws JAXBException {
        StringWriter writer = new StringWriter();
        createMarshaller().marshal(organization, writer);
        return writer.toString();
    }

    private Unmarshaller createUnmarshaller() throws JAXBException {
        try {
            // TODO use osgi ResourceFinder when moved to bonita-artifacts-model
            URL schemaURL = Organization.class.getResource(ORGANIZATION_XSD);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            jaxbUnmarshaller.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                    .newSchema(schemaURL));
            return jaxbUnmarshaller;
        } catch (SAXException e) {
            throw new JAXBException(e);
        }
    }

    private Marshaller createMarshaller() throws JAXBException {
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
        return jaxbMarshaller;
    }
}
