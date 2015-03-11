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
package org.bonitasoft.engine.bdm.model.field;

import static org.bonitasoft.engine.bdm.builder.BusinessObjectBuilder.aBO;
import static org.bonitasoft.engine.bdm.builder.FieldBuilder.aBooleanField;
import static org.bonitasoft.engine.bdm.model.assertion.FieldAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Ignore;
import org.junit.Test;

import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.field.RelationField.FetchType;
import org.bonitasoft.engine.bdm.model.field.RelationField.Type;

/**
 * @author Colin PUY
 */
public class RelationFieldTest {

    private final BusinessObject aBo = aBO("boName").withField(aBooleanField("aField")).build();

    @Test
    public void should_not_be_marshallizable_without_reference() {
        final RelationField field = new RelationField();
        field.setName("aName");
        field.setType(Type.AGGREGATION);

        assertThat(field).cannotBeMarshalled();
    }

    @Test
    @Ignore
    public void should_not_be_marshallizable_without_name() {
        final RelationField field = new RelationField();
        field.setType(Type.AGGREGATION);
        field.setReference(aBo);

        assertThat(field).cannotBeMarshalled();
    }

    @Test
    public void should_not_be_marshallizable_without_type() {
        final RelationField field = new RelationField();
        field.setReference(aBo);
        field.setName("aName");

        assertThat(field).cannotBeMarshalled();
    }

    @Test
    public void should_be_marshallizable_with_only_name_type_and_reference() {
        final RelationField field = new RelationField();
        field.setName("aName");
        field.setType(Type.AGGREGATION);
        field.setReference(aBo);

        assertThat(field).canBeMarshalled();
    }

    @Test
    public void should_not_be_marshallizable_whitout_fetchType() {
        final RelationField field = new RelationField();
        field.setName("aName");
        field.setType(Type.AGGREGATION);
        field.setReference(aBo);

        field.setFetchType(null);

        assertThat(field).cannotBeMarshalled();
    }

    @Test
    public void should_have_a_default_fetchType_to_eager() {
        final RelationField field = new RelationField();

        assertThat(field.getFetchType()).isEqualTo(FetchType.EAGER);
    }

    @Test
    public void can_be_lazy() {
        final RelationField field = new RelationField();
        field.setFetchType(FetchType.LAZY);

        assertThat(field.getFetchType()).isEqualTo(FetchType.LAZY);
    }
}
