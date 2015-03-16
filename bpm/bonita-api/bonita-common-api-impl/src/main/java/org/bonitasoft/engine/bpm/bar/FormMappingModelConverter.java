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
package org.bonitasoft.engine.bpm.bar;

import java.io.IOException;
import java.net.URL;

import javax.xml.bind.JAXBException;

import org.bonitasoft.engine.bpm.bar.form.model.FormMappingModel;
import org.bonitasoft.engine.io.IOUtil;
import org.xml.sax.SAXException;

/**
 * @author Emmanuel Duchastenier
 */
public class FormMappingModelConverter {

    private static final String XSD_MODEL = "/form-mapping.xsd";

    private final URL xsdUrl;

    public FormMappingModelConverter() {
        xsdUrl = FormMappingModel.class.getResource(XSD_MODEL);
    }

    public byte[] serializeToXML(final FormMappingModel model) throws IOException, JAXBException, SAXException {
        return marshall(model);
    }

    protected byte[] marshall(final FormMappingModel model) throws JAXBException, IOException, SAXException {
        return IOUtil.marshallObjectToXML(model, xsdUrl);
    }

    public FormMappingModel deserializeFromXML(final byte[] xmlModel) throws IOException, JAXBException, SAXException {
        return unmarshall(xmlModel);
    }

    protected FormMappingModel unmarshall(final byte[] model) throws JAXBException, IOException, SAXException {
        return IOUtil.unmarshallXMLtoObject(model, FormMappingModel.class, xsdUrl);
    }

}
