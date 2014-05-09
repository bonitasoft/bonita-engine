package com.bonitasoft.engine.bdm.model.field;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.bonitasoft.engine.bdm.model.field.SimpleField;

public class SimpleFieldTest {

    @Test
    public void setNameShouldWorksWithARightName() {
        final SimpleField field = new SimpleField();
        field.setName("fistName");
    }

    @Test
    public void setNameShouldWorksWithARightNameFirstLetterInUpperCase() {
        final SimpleField field = new SimpleField();
        field.setName("FistName");
    }

    @Test
    public void hashCodeIsBasedOnFieldClassAttributes() throws Exception {
        SimpleField field = new SimpleField();
        SimpleField other = new SimpleField();

        assertThat(field.hashCode()).isEqualTo(other.hashCode());
    }
}
