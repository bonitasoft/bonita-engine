package com.bonitasoft.engine.bdm.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.bonitasoft.engine.bdm.model.Field;

public class FieldTest {

    @Test
    public void setNameShouldWorksWithARightName() {
        final Field field = new Field();
        field.setName("fistName");
    }

    @Test
    public void setNameShouldWorksWithARightNameFirstLetterInUpperCase() {
        final Field field = new Field();
        field.setName("FistName");
    }

    @Test
    public void hashCodeIsBasedOnFieldClassAttributes() throws Exception {
        Field field = new Field();
        Field other = new Field();

        assertThat(field.hashCode()).isEqualTo(other.hashCode());
    }
}
