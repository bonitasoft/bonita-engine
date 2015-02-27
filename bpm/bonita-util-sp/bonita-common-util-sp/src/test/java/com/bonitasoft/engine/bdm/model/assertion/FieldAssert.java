/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.model.assertion;

import static com.bonitasoft.engine.bdm.builder.BusinessObjectBuilder.aBO;
import static com.bonitasoft.engine.bdm.builder.BusinessObjectModelBuilder.aBOM;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.xml.sax.SAXException;

import com.bonitasoft.engine.bdm.builder.BusinessObjectModelBuilder;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.model.field.Field;
import com.bonitasoft.engine.bdm.model.field.RelationField;

public class FieldAssert extends AbstractAssert<FieldAssert, Field> {

    protected FieldAssert(final Field actual) {
        super(actual, FieldAssert.class);
    }

    public static FieldAssert assertThat(final Field actual) {
        return new FieldAssert(actual);
    }

    public FieldAssert canBeMarshalled() {
        try {
            final BusinessObjectModel bom = marshallUnmarshall(actual);
            Assertions.assertThat(bom.getBusinessObjects().get(0)).isNotNull();
            Assertions.assertThat(bom.getBusinessObjects().get(0).getFields().get(0)).isNotNull();
            isEqualTo(bom.getBusinessObjects().get(0).getFields().get(0));
        } catch (final Exception e) {
            failWithMessage("Expected <%s> to be marshallizable but wasn't : <%s>", actual, e.getCause());
        }
        return this;
    }

    public FieldAssert cannotBeMarshalled() {
        try {
            marshallUnmarshall(actual);
            failWithMessage("Expected <%s> not to be marshallizable", actual);
        } catch (final Exception e) {
            // OK
        }
        return this;
    }

    private BusinessObjectModel marshallUnmarshall(final Field field) throws JAXBException, IOException, SAXException {
        final BusinessObjectModelBuilder bom = aBOM().withBO(aBO("someUglyNameMightNotAppear").withField(field).build());
        addReferencedBoToBom(field, bom);
        return Marshaller.marshallUnmarshall(bom.build());
    }

    private void addReferencedBoToBom(final Field field, final BusinessObjectModelBuilder bom) {
        if (field instanceof RelationField) {
            final RelationField f = (RelationField) field;
            bom.withBO(f.getReference());
        }
    }
}
