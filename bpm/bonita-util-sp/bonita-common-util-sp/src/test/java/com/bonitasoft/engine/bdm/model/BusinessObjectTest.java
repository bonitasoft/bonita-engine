package com.bonitasoft.engine.bdm.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import com.bonitasoft.engine.bdm.model.BusinessObject;

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
        final SimpleField field = new SimpleField();
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

}
