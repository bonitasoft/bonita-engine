/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.bdm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.Index;
import org.bonitasoft.engine.bdm.model.Query;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.SimpleField;

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
    public void addUniqueConstraintShouldWorkIfTheFieldExists() {
        final SimpleField field = new SimpleField();
        field.setName("field");
        field.setType(FieldType.STRING);

        final BusinessObject object = new BusinessObject();
        object.addField(field);
        object.addUniqueConstraint("unique", "field");
    }

    @Test
    public void should_addQuery() {
        final BusinessObject businessObject = new BusinessObject();
        final Query query = businessObject.addQuery("userByName", "SELECT u FROM User u WHERE u.name='romain'", List.class.getName());
        assertThat(businessObject.getQueries()).containsExactly(query);
    }

    @Test
    public void addIndexShouldWorkIfTheFieldExists() {
        final SimpleField field = new SimpleField();
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
    public void addIndexShouldThrowAnExceptionIfNoFieldIsSet() {
        final BusinessObject object = new BusinessObject();
        object.addIndex("unique", (String[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addIndexShouldThrowAnExceptionIfTheListOfFieldsIsEmpty() {
        final BusinessObject object = new BusinessObject();
        object.addIndex("unique");
    }

}
