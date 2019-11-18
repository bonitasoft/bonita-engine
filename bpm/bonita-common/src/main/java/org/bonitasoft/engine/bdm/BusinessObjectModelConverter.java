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
package org.bonitasoft.engine.bdm;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.business.data.InvalidBusinessDataModelException;
import org.bonitasoft.engine.io.IOUtils;
import org.xml.sax.SAXException;

/**
 * @author Matthieu Chaffotte
 */
public class BusinessObjectModelConverter {

    private static final String BOM_XSD = "/bom.xsd";

    private static final String BOM_XML = "bom.xml";

    private final URL xsdUrl;

    public BusinessObjectModelConverter() {
        xsdUrl = BusinessObjectModel.class.getResource(BOM_XSD);
    }

    public byte[] zip(final BusinessObjectModel bom) throws IOException, JAXBException, SAXException {
        return IOUtils.zip(BOM_XML, marshall(bom));
    }

    public byte[] marshall(final BusinessObjectModel bom) throws JAXBException, IOException, SAXException {
        final String modelVersion = bom.getModelVersion();
        if (modelVersion == null || modelVersion.isEmpty()) {
            bom.setModelVersion(BusinessObjectModel.CURRENT_MODEL_VERSION);
        }
        final String productVersion = bom.getProductVersion();
        if (productVersion == null || productVersion.isEmpty()) {
            bom.setProductVersion(BusinessObjectModel.CURRENT_PRODUCT_VERSION);
        }
        return IOUtils.marshallObjectToXML(bom, xsdUrl);
    }

    public BusinessObjectModel unzip(final byte[] zippedBOM) throws InvalidBusinessDataModelException {
        final Map<String, byte[]> files;
        try {
            files = IOUtils.unzip(zippedBOM);
        } catch (IOException e) {
            throw new InvalidBusinessDataModelException("Unable to unzip the content of the bdm.zip file", e);
        }
        final byte[] bomXML = files.get(BOM_XML);
        if (bomXML == null) {
            throw new InvalidBusinessDataModelException("the file " + BOM_XML + " is missing in the zip");
        }
        return unmarshall(bomXML);
    }

    public BusinessObjectModel unmarshall(final byte[] bomXML) throws InvalidBusinessDataModelException {
        try {
            return IOUtils.unmarshallXMLtoObject(bomXML, BusinessObjectModel.class, xsdUrl);
        } catch (Exception e) {
            throw new InvalidBusinessDataModelException("Unable to unmarshal businees object model", e);
        }
    }

}
