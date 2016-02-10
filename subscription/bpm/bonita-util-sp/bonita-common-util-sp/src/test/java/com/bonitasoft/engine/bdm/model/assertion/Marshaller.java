/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.model.assertion;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import com.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;

public class Marshaller {

    public static BusinessObjectModel marshallUnmarshall(final BusinessObjectModel bom) throws JAXBException, IOException, SAXException {
        BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        byte[] marshall = convertor.marshall(bom);
        // System.out.println(IOUtils.toString(marshall));
        return convertor.unmarshall(marshall);
    }
}
