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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * @author Colin PUY
 */
public class FieldTest {

    @Test
    public void should_be_nullable_by_default() {
        Field field = aField();

        assertThat(field.isNullable()).isTrue();
    }

    @Test
    public void should_not_be_a_collection_by_default() {
        Field field = aField();

        assertThat(field.isCollection()).isFalse();
    }

    private Field aField() {
        return new FakeField();
    }

    private class FakeField extends Field {
    }
}
