package com.bonitasoft.engine.bdm.model.assertion;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import com.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;


public class Marshaller {

    public static BusinessObjectModel marshallUnmarshall(BusinessObjectModel bom) throws JAXBException, IOException, SAXException {
        BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        byte[] marshall = convertor.marshall(bom);
        return convertor.unmarshall(marshall);
    }
}
