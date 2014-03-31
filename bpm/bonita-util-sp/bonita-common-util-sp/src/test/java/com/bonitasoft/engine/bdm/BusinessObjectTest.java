package com.bonitasoft.engine.bdm;

import static org.assertj.core.api.Assertions.assertThat;

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
		BusinessObject businessObject = new BusinessObject();
		Query query = businessObject.addQuery("userByName","Select u FROM User u WHERE u.name='romain'",List.class.getName());
		assertThat(businessObject.getQueries()).containsExactly(query);
	}

}
