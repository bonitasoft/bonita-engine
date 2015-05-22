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

package org.bonitasoft.engine.operation;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.commons.Container;
import org.junit.Test;

public class BusinessDataContextTest {

    @Test
    public void should_create_a_consistent_context() throws Exception {
        //when
        BusinessDataContext context = new BusinessDataContext("address", new Container(2L, "process"));

        //then
        assertThat(context.getName()).isEqualTo("address");
        assertThat(context.getContainer().getId()).isEqualTo(2L);
        assertThat(context.getContainer().getType()).isEqualTo("process");
    }

}
