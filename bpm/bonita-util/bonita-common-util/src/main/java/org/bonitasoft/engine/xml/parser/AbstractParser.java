/**
 * Copyright (C) 2017 BonitaSoft S.A.
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
package org.bonitasoft.engine.xml.parser;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.SchemaFactory;

/**
 * @author Adrien
 */
public abstract class AbstractParser<T> {

    private final Unmarshaller unmarshaller;
    private final Marshaller marshaller;

    public AbstractParser() {
        try {
            JAXBContext jaxbContext = initJAXBContext();
            unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(initSchemaURL()));
            marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract JAXBContext initJAXBContext() throws JAXBException;

    protected abstract URL initSchemaURL();

    public T convert(String xml) throws JAXBException {
        return (T) unmarshaller.unmarshal(new StringReader(xml));
    }

    public String convert(T model) throws JAXBException {
        StringWriter writer = new StringWriter();
        marshaller.marshal(model, writer);
        return writer.toString();
    }

}
