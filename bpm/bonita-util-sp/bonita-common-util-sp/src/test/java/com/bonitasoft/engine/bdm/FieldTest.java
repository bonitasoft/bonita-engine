package com.bonitasoft.engine.bdm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

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

    @Test(expected = IllegalArgumentException.class)
    public void setNameShouldThrowAnExceptionWhenNameIsAJavaKeyword() {
        final Field field = new Field();
        field.setName("if");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setNameShouldThrowAnExceptionWhenNameStartWithADigit() {
        final Field field = new Field();
        field.setName("9firstName");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setNameShouldThrowAnExceptionWhenNameIsPersistenceId() {
        final Field field = new Field();
        field.setName("persistenceId");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setNameShouldThrowAnExceptionWhenNameIsPersistenceIdInAnyCase() {
        final Field field = new Field();
        field.setName("PersIstenceId");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setNameShouldThrowAnExceptionWhenNameIsPersistenceVersion() {
        final Field field = new Field();
        field.setName("persistenceVersion");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setNameShouldThrowAnExceptionWhenNameIsPersistenceVersionInAnyCase() {
        final Field field = new Field();
        field.setName("PersIstenCeVersiOn");
    }

    @Test
    public void hashCodeIsBasedOnFieldClassAttributes() throws Exception {
        Field field = new Field();
        Field other = new Field();

        assertThat(field.hashCode()).isEqualTo(other.hashCode());
    }
}
