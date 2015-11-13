/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

package org.bonitasoft.engine.bdm.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class QueryTest {

    @Test
    public void should_be_multiple_if_return_type_is_list() throws Exception {
        assertThat(buildQuery(List.class).hasMultipleResults()).isTrue();
    }

    @Test
    public void should_not_be_multiple_if_return_type_is_not_a_list() throws Exception {
        assertThat(buildQuery(String.class).hasMultipleResults()).isFalse();
    }

    private Query buildQuery(Class<?> returnTypeClass) {
        Query query = new Query();
        query.setReturnType(returnTypeClass.getName());
        return query;
    }
}
