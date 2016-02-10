/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.model.field;

import static com.bonitasoft.engine.bdm.model.assertion.FieldAssert.assertThat;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Colin PUY
 */
public class SimpleFieldTest {

    @Test
    @Ignore
    public void should_not_be_marshallizable_without_name() {
        final SimpleField field = new SimpleField();
        field.setType(FieldType.BOOLEAN);

        assertThat(field).cannotBeMarshalled();
    }

    @Test
    public void should_not_be_marshallizable_without_type() {
        final SimpleField field = new SimpleField();
        field.setName("aName");

        assertThat(field).cannotBeMarshalled();
    }

    @Test
    public void should_be_marshallizable_with_name_and_type() {
        final SimpleField field = new SimpleField();
        field.setName("aName");
        field.setType(FieldType.BOOLEAN);

        assertThat(field).canBeMarshalled();
    }

    @Test
    public void should_have_other_optionnal_attributes() {
        final SimpleField field = new SimpleField();
        field.setName("aName");
        field.setType(FieldType.BOOLEAN);
        field.setCollection(true);
        field.setLength(123);
        field.setNullable(true);

        assertThat(field).canBeMarshalled();
    }
}
