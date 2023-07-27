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
package org.bonitasoft.engine.bdm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.xml.sax.SAXException;

/**
 * @author Matthieu Chaffotte
 */
public class BusinessObjectModelConverter {

    private static final String BOM_XSD = "/bom.xsd";

    private static final String BOM_XML = "bom.xml";

    private static final String BDM_NAMESPACE = "http://documentation.bonitasoft.com/bdm-xml-schema/1.0";

    private final URL xsdUrl;

    public BusinessObjectModelConverter() {
        xsdUrl = BusinessObjectModel.class.getResource(BOM_XSD);
    }

    public byte[] zip(final BusinessObjectModel bom) throws IOException, JAXBException, SAXException {
        return zipBom(marshall(bom));
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
        return marshallObjectToXML(bom);
    }

    public BusinessObjectModel unzip(final byte[] zippedBOM) throws IOException, JAXBException, SAXException {
        final Map<String, byte[]> files = unzipBom(zippedBOM);
        final byte[] bomXML = files.get(BOM_XML);
        if (bomXML == null) {
            throw new IOException("the file " + BOM_XML + " is missing in the zip");
        }
        return unmarshall(bomXML);
    }

    public BusinessObjectModel unmarshall(final byte[] bomXML) throws JAXBException, IOException, SAXException {
        return unmarshallXMLtoObject(addNamespace(bomXML));
    }

    private byte[] addNamespace(byte[] content) {
        String contentStr = new String(content);
        if (!contentStr.contains(BDM_NAMESPACE)) {
            String tagToFind = "<businessObjectModel";
            contentStr = contentStr.replace(tagToFind, String.format("%s xmlns=\"%s\"", tagToFind, BDM_NAMESPACE));
        }
        return contentStr.getBytes();
    }

    private byte[] marshallObjectToXML(final BusinessObjectModel jaxbModel)
            throws JAXBException, IOException, SAXException {
        if (jaxbModel == null) {
            return new byte[0];
        }
        final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final Schema schema = sf.newSchema(xsdUrl);
        try (final ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            final JAXBContext contextObj = JAXBContext.newInstance(jaxbModel.getClass());
            final Marshaller m = contextObj.createMarshaller();
            m.setSchema(schema);
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            m.marshal(jaxbModel, stream);
            return stream.toByteArray();
        }
    }

    private BusinessObjectModel unmarshallXMLtoObject(final byte[] xmlObject)
            throws JAXBException, IOException,
            SAXException {
        if (xmlObject == null) {
            return null;
        }
        final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final Schema schema = sf.newSchema(xsdUrl);
        final JAXBContext contextObj = JAXBContext.newInstance(BusinessObjectModel.class);
        final Unmarshaller um = contextObj.createUnmarshaller();
        um.setSchema(schema);
        try (final ByteArrayInputStream stream = new ByteArrayInputStream(xmlObject)) {
            final JAXBElement<BusinessObjectModel> jaxbElement = um.unmarshal(new StreamSource(stream),
                    BusinessObjectModel.class);
            return jaxbElement.getValue();
        }
    }

    private byte[] zipBom(final byte[] fileContent) throws IOException {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try (final ZipOutputStream zos = new ZipOutputStream(stream)) {
            zos.putNextEntry(new ZipEntry(BusinessObjectModelConverter.BOM_XML));
            zos.write(fileContent);
        }
        return stream.toByteArray();
    }

    private Map<String, byte[]> unzipBom(final byte[] zippedContent) throws IOException {
        final Map<String, byte[]> resources = new HashMap<>();
        try (final ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zippedContent))) {
            ZipEntry entry = zis.getNextEntry();
            while (entry != null) {
                try (final ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                    int len;
                    final byte[] buffer = new byte[1024];
                    while ((len = zis.read(buffer)) > 0) {
                        stream.write(buffer, 0, len);
                    }
                    resources.put(entry.getName(), stream.toByteArray());
                }
                entry = zis.getNextEntry();
            }
        }
        return resources;
    }

}
