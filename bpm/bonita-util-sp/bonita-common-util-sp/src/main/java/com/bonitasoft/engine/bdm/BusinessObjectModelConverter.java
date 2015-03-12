/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.io.IOUtils;

/**
 * @author Matthieu Chaffotte
 * @deprecated from version 7.0.0 on, use {@link org.bonitasoft.engine.bdm.BusinessObjectModelConverter} instead.
 */
@Deprecated
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
        return IOUtils.marshallObjectToXML(bom, xsdUrl);
    }

    public BusinessObjectModel unzip(final byte[] zippedBOM) throws IOException, JAXBException, SAXException {
        final Map<String, byte[]> files = IOUtils.unzip(zippedBOM);
        final byte[] bomXML = files.get(BOM_XML);
        if (bomXML == null) {
            throw new IOException("the file " + BOM_XML + " is missing in the zip");
        }
        return unmarshall(bomXML);
    }

    public BusinessObjectModel unmarshall(final byte[] bomXML) throws JAXBException, IOException, SAXException {
        return IOUtils.unmarshallXMLtoObject(bomXML, BusinessObjectModel.class, xsdUrl);
    }

}
