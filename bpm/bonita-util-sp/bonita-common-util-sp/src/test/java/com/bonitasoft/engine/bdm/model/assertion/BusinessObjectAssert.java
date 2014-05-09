/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.model.assertion;

import static com.bonitasoft.engine.bdm.BOMBuilder.aBOM;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.xml.sax.SAXException;

import com.bonitasoft.engine.bdm.BOMBuilder;
import com.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.model.field.Field;
import com.bonitasoft.engine.bdm.model.field.RelationField;

/**
 * @author Colin PUY
 */
public class BusinessObjectAssert extends AbstractAssert<BusinessObjectAssert, BusinessObject> {

    protected BusinessObjectAssert(BusinessObject actual) {
        super(actual, BusinessObjectAssert.class);
    }

    public static BusinessObjectAssert assertThat(BusinessObject actual) {
        return new BusinessObjectAssert(actual);
    }

    public BusinessObjectAssert canBeMarshalled() {
        try {
            BusinessObjectModel bom = marshallUnmarshall(actual);
            Assertions.assertThat(bom.getBusinessObjects().get(0)).isNotNull();
            isEqualTo(bom.getBusinessObjects().get(0));
        } catch (Exception e) {
            failWithMessage("Expected <%s> to be marshallizable : <%s>", actual, e.getCause());
        }
        return this;
    }

    public BusinessObjectAssert cannotBeMarshalled() {
        try {
            marshallUnmarshall(actual);
            failWithMessage("Expected <%s> not to be marshallizable", actual);
        } catch (Exception e) {
        }
        return this;
    }

    private BusinessObjectModel marshallUnmarshall(BusinessObject bo) throws JAXBException, IOException, SAXException {
        BOMBuilder model = aBOM().withBO(bo);
        for (Field field : bo.getFields()) {
            if (field instanceof RelationField) {
                model.withBO(((RelationField) field).getReference());
            }
        }
        BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        byte[] marshall = convertor.marshall(model.build());
        return convertor.unmarshall(marshall);
    }
}
