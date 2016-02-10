/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.model.field;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * @author Colin PUY
 */
public class FieldTest {

    @Test
    public void should_be_nullable_by_default() {
        Field field = aField();

        assertThat(field.isNullable()).isTrue();
    }

    @Test
    public void should_not_be_a_collection_by_default() {
        Field field = aField();

        assertThat(field.isCollection()).isFalse();
    }

    private Field aField() {
        return new FakeField();
    }

    private class FakeField extends Field {
    }
}
