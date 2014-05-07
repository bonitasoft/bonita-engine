package com.bonitasoft.engine.bdm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

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

    @Test
    public void should_addQuery() throws Exception {
        final BusinessObject businessObject = new BusinessObject();
        final Query query = businessObject.addQuery("userByName", "SELECT u FROM User u WHERE u.name='romain'", List.class.getName());
        assertThat(businessObject.getQueries()).containsExactly(query);
    }

    @Test
    public void addIndexShouldWorkIfTheFieldExists() throws Exception {
        final Field field = new Field();
        field.setName("field");
        field.setType(FieldType.STRING);

        final Index expected = new Index();
        expected.setName("unique");
        expected.setFieldNames(Arrays.asList("field"));

        final BusinessObject object = new BusinessObject();
        object.addField(field);
        final Index index = object.addIndex("unique", "field");

        assertThat(index).isEqualTo(expected);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addIndexShouldThrowAnExceptionIfNoFieldIsSet() throws Exception {
        final BusinessObject object = new BusinessObject();
        object.addIndex("unique", (String[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addIndexShouldThrowAnExceptionIfTheListOfFieldsIsEmpty() throws Exception {
        final BusinessObject object = new BusinessObject();
        object.addIndex("unique");
    }

}
