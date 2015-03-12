package org.bonitasoft.engine.bdm.model.assertion;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import org.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;

public class Marshaller {

    public static BusinessObjectModel marshallUnmarshall(final BusinessObjectModel bom) throws JAXBException, IOException, SAXException {
        BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        byte[] marshall = convertor.marshall(bom);
        // System.out.println(IOUtils.toString(marshall));
        return convertor.unmarshall(marshall);
    }
}
