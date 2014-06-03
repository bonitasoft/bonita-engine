package com.bonitasoft.engine.bdm.model.field;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * @author Colin PUY
 */
public class FieldTest {

    @Test
    public void should_be_nullable_by_default() throws Exception {
        Field field = aField();

        assertThat(field.isNullable()).isTrue();
    }

    @Test
    public void should_not_be_a_collection_by_default() throws Exception {
        Field field = aField();

        assertThat(field.isCollection()).isFalse();
    }

    private Field aField() {
        return new FakeField();
    }

    private class FakeField extends Field {
    }
}
