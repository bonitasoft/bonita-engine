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
