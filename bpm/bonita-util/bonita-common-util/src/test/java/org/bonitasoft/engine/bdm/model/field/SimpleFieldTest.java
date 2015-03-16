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

import static org.bonitasoft.engine.bdm.model.assertion.FieldAssert.assertThat;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Colin PUY
 */
public class SimpleFieldTest {

    @Test
    @Ignore
    public void should_not_be_marshallizable_without_name() {
        final SimpleField field = new SimpleField();
        field.setType(FieldType.BOOLEAN);

        assertThat(field).cannotBeMarshalled();
    }

    @Test
    public void should_not_be_marshallizable_without_type() {
        final SimpleField field = new SimpleField();
        field.setName("aName");

        assertThat(field).cannotBeMarshalled();
    }

    @Test
    public void should_be_marshallizable_with_name_and_type() {
        final SimpleField field = new SimpleField();
        field.setName("aName");
        field.setType(FieldType.BOOLEAN);

        assertThat(field).canBeMarshalled();
    }

    @Test
    public void should_have_other_optionnal_attributes() {
        final SimpleField field = new SimpleField();
        field.setName("aName");
        field.setType(FieldType.BOOLEAN);
        field.setCollection(true);
        field.setLength(123);
        field.setNullable(true);

        assertThat(field).canBeMarshalled();
    }
}
