/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.bpm.bar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.bonitasoft.engine.bpm.bar.form.model.FormMappingModel;
import org.xml.sax.SAXException;

/**
 * @author Emmanuel Duchastenier
 */
public class FormMappingModelMarshaller {

    private static final String XSD_MODEL = "/form-mapping.xsd";

    private final URL xsdUrl;

    public FormMappingModelMarshaller() {
        xsdUrl = FormMappingModel.class.getResource(XSD_MODEL);
    }

    public byte[] serializeToXML(final FormMappingModel model) throws IOException, JAXBException, SAXException {
        return marshall(model);
    }

    protected byte[] marshall(final FormMappingModel model) throws JAXBException, IOException, SAXException {
        return marshallObjectToXML(model);
    }

    public FormMappingModel deserializeFromXML(final byte[] xmlModel) throws IOException, JAXBException, SAXException {
        return unmarshall(xmlModel);
    }

    protected FormMappingModel unmarshall(final byte[] model) throws JAXBException, IOException, SAXException {
        return unmarshallXMLtoObject(model);
    }

    private byte[] marshallObjectToXML(final Object model)
            throws JAXBException, IOException, SAXException {
        final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final Schema schema = sf.newSchema(xsdUrl);
        try (var baos = new ByteArrayOutputStream()) {
            final JAXBContext contextObj = JAXBContext.newInstance(model.getClass());
            final Marshaller m = contextObj.createMarshaller();
            m.setSchema(schema);
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.marshal(model, baos);
            return baos.toByteArray();
        }
    }

    private FormMappingModel unmarshallXMLtoObject(final byte[] xmlObject)
            throws JAXBException, IOException,
            SAXException {
        if (xmlObject == null) {
            return null;
        }
        final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final Schema schema = sf.newSchema(xsdUrl);
        final JAXBContext contextObj = JAXBContext.newInstance(FormMappingModel.class);
        final Unmarshaller um = contextObj.createUnmarshaller();
        um.setSchema(schema);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(xmlObject)) {
            final StreamSource ss = new StreamSource(bais);
            final JAXBElement<FormMappingModel> jaxbElement = um.unmarshal(ss, FormMappingModel.class);
            return jaxbElement.getValue();
        }
    }

}
