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

package org.bonitasoft.engine.commons;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ContainerTest {

    @Test
    public void constructor_should_create_container_based_on_given_information() throws Exception {
        //when
        Container container = new Container(10, "cType");

        //then
        assertThat(container.getId()).isEqualTo(10);
        assertThat(container.getType()).isEqualTo("cType");
    }
}
