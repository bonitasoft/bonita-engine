/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
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
