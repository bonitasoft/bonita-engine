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
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.bonitasoft.engine.exception.CreationException;

import com.bonitasoft.engine.io.IOUtils;

/**
 * @author Matthieu Chaffotte
 */
public class BusinessObjectModelConvertor {

    private static final String BOM_XML = "bom.xml";

    public byte[] zip(final BusinessObjectModel bom) throws IOException, CreationException {
        try {
            final byte[] bomXML = IOUtils.marshallObjectToXML(bom);
            return IOUtils.zip(BOM_XML, bomXML);
        } catch (final JAXBException jaxbe) {
            throw new CreationException(jaxbe);
        }
    }

    public BusinessObjectModel unzip(final byte[] zippedBOM) throws IOException, CreationException {
        try {
            final Map<String, byte[]> files = IOUtils.unzip(zippedBOM);
            final byte[] bomXML = files.get(BOM_XML);
            return IOUtils.unmarshallXMLtoObject(bomXML, BusinessObjectModel.class);
        } catch (final JAXBException jaxbe) {
            throw new CreationException(jaxbe);
        }
    }

}
