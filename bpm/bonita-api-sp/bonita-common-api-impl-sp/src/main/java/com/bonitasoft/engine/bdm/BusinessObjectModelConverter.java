/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.bonitasoft.engine.exception.CreationException;
import org.xml.sax.SAXException;

import com.bonitasoft.engine.io.IOUtils;

/**
 * @author Matthieu Chaffotte
 */
public class BusinessObjectModelConverter {

    private static final String BOM_XML = "bom.xml";

    public byte[] zip(final BusinessObjectModel bom) throws IOException, CreationException {
        try {
            final URL resource = BusinessObjectModel.class.getResource("/bom.xsd");
            final byte[] bomXML = IOUtils.marshallObjectToXML(bom, resource);
            return IOUtils.zip(BOM_XML, bomXML);
        } catch (final JAXBException jaxbe) {
            throw new CreationException(jaxbe);
        } catch (final SAXException saxe) {
            throw new CreationException(saxe);
        }
    }

    public BusinessObjectModel unzip(final byte[] zippedBOM) throws IOException, CreationException {
        try {
            final Map<String, byte[]> files = IOUtils.unzip(zippedBOM);
            final byte[] bomXML = files.get(BOM_XML);
            if (bomXML == null) {
                throw new CreationException("the file" + BOM_XML + " is missing in the zip");
            }
            final URL resource = BusinessObjectModel.class.getResource("/bom.xsd");
            return IOUtils.unmarshallXMLtoObject(bomXML, BusinessObjectModel.class, resource);
        } catch (final JAXBException jaxbe) {
            throw new CreationException(jaxbe);
        } catch (final SAXException saxe) {
            throw new CreationException(saxe);
        }
    }

}
