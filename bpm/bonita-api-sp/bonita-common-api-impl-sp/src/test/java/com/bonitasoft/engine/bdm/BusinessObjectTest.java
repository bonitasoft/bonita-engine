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

}
