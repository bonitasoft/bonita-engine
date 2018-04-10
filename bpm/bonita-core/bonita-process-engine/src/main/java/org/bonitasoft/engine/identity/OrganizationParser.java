/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.identity.xml.Organization;

/**
 * @author Baptiste Mesta
 */
public class OrganizationParser {

    private final JAXBContext jaxbContext;
    private final Schema schema;

    public OrganizationParser() {
        try {
            jaxbContext = JAXBContext.newInstance(Organization.class);
            URL schemaURL = Organization.class.getResource("/organization.xsd");
            final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schema = sf.newSchema(schemaURL);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    public Organization convert(String organizationContent) throws JAXBException {
        if (!organizationContent.contains("http://documentation.bonitasoft.com/organization-xml-schema/1.1")) {
            organizationContent = organizationContent.replace("http://documentation.bonitasoft.com/organization-xml-schema", "http://documentation.bonitasoft.com/organization-xml-schema/1.1");
        }
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setSchema(schema);
        return (Organization) unmarshaller.unmarshal(new StringReader(organizationContent));
    }

    private Marshaller getMarshaller() throws JAXBException {
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        return marshaller;
    }

    public String convert(Organization organization) throws JAXBException {
        StringWriter writer = new StringWriter();
        getMarshaller().marshal(organization, writer);
        return writer.toString();
    }
}
