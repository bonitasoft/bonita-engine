package com.bonitasoft.engine.bdm;

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

}
