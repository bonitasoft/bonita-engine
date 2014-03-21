package com.bonitasoft.engine.bdm;

import org.junit.Test;

public class BusinessObjectTest {

    @Test
    public void setQualifiedNameShouldWorkIfAValidQualifiedName() {
        final BusinessObject object = new BusinessObject();
        object.setQualifiedName("com.Employee");
    }

    @Test
    public void setQualifiedNameShouldWorkIfAValidQualifiedNameInLowercase() {
        final BusinessObject object = new BusinessObject();
        object.setQualifiedName("com.employee");
    }

    @Test
    public void setQualifiedNameShouldWorkWithoutPackageName() {
        final BusinessObject object = new BusinessObject();
        object.setQualifiedName("Employee");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setQualifiedNameShouldTrowAnExceptionWhenTheClassNameStartsWithADigit() {
        final BusinessObject object = new BusinessObject();
        object.setQualifiedName("com.2Employee");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addUniqueConstraintUsingAnUnknownFieldShouldThrowAnException() throws Exception {
        final BusinessObject object = new BusinessObject();
        object.addUniqueConstraint("unique", "unknownField");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addUniqueConstraintUsingANullFieldShouldThrowAnException() throws Exception {
        final BusinessObject object = new BusinessObject();
        final String fieldName = null;
        object.addUniqueConstraint("unique", fieldName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addUniqueConstraintUsingAnUnknownFieldAmongFieldsShouldThrowAnException() throws Exception {
        final Field field = new Field();
        field.setName("field");
        field.setType(FieldType.STRING);

        final BusinessObject object = new BusinessObject();
        object.addField(field);
        object.addUniqueConstraint("unique", "unknownField");
    }

    @Test
    public void addUniqueConstraintShouldWorkIfTheFieldExists() throws Exception {
        final Field field = new Field();
        field.setName("field");
        field.setType(FieldType.STRING);

        final BusinessObject object = new BusinessObject();
        object.addField(field);
        object.addUniqueConstraint("unique", "field");
    }

}
