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

package org.bonitasoft.engine.profile;

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
import org.bonitasoft.engine.profile.impl.ExportedProfiles;

/**
 * @author Baptiste Mesta
 */
public class ProfilesParser {

    private final JAXBContext jaxbContext;
    private final Schema schema;

    public ProfilesParser() {
        try {
            jaxbContext = JAXBContext.newInstance(ExportedProfiles.class);
            URL schemaURL = Organization.class.getResource("/profiles.xsd");
            final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schema = sf.newSchema(schemaURL);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    public ExportedProfiles convert(String profilesXml) throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setSchema(schema);
        return (ExportedProfiles) unmarshaller.unmarshal(new StringReader(profilesXml));
    }

    private Marshaller getMarshaller() throws JAXBException {
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        return marshaller;
    }

    public String convert(ExportedProfiles exportedProfiles) throws JAXBException {
        StringWriter writer = new StringWriter();
        getMarshaller().marshal(exportedProfiles, writer);
        return writer.toString();
    }
}
