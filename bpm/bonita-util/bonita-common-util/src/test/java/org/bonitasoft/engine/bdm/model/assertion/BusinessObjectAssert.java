/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.bdm.model.assertion;

import static org.bonitasoft.engine.bdm.builder.BusinessObjectModelBuilder.aBOM;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.xml.sax.SAXException;

import org.bonitasoft.engine.bdm.builder.BusinessObjectModelBuilder;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.field.Field;
import org.bonitasoft.engine.bdm.model.field.RelationField;

/**
 * @author Colin PUY
 */
public class BusinessObjectAssert extends AbstractAssert<BusinessObjectAssert, BusinessObject> {

    protected BusinessObjectAssert(final BusinessObject actual) {
        super(actual, BusinessObjectAssert.class);
    }

    public static BusinessObjectAssert assertThat(final BusinessObject actual) {
        return new BusinessObjectAssert(actual);
    }

    public BusinessObjectAssert canBeMarshalled() {
        try {
            final BusinessObjectModel bom = marshallUnmarshall(actual);
            Assertions.assertThat(bom.getBusinessObjects().get(0)).isNotNull();
            isEqualTo(bom.getBusinessObjects().get(0));
        } catch (final Exception e) {
            failWithMessage("Expected <%s> to be marshallizable : <%s>", actual, e.getCause());
        }
        return this;
    }

    public BusinessObjectAssert cannotBeMarshalled() {
        try {
            marshallUnmarshall(actual);
            failWithMessage("Expected <%s> not to be marshallizable", actual);
        } catch (final Exception e) {
            // OK
        }
        return this;
    }

    private BusinessObjectModel marshallUnmarshall(final BusinessObject bo) throws JAXBException, IOException, SAXException {
        final BusinessObjectModelBuilder bom = aBOM().withBO(bo);
        addReferencedBoToBom(bo, bom);
        return Marshaller.marshallUnmarshall(bom.build());
    }

    private void addReferencedBoToBom(final BusinessObject bo, final BusinessObjectModelBuilder bom) {
        for (final Field field : bo.getFields()) {
            if (field instanceof RelationField && !bo.equals(((RelationField) field).getReference())) {
                bom.withBO(((RelationField) field).getReference());
            }
        }
    }
}
