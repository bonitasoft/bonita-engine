/**
 * Copyright (C) 2018 Bonitasoft S.A.
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
package org.bonitasoft.engine.search.descriptor;

import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SearchHumanTaskInstanceDescriptorTest {

    @Test
    public void getEntityKey_should_map_process_instance_to_the_logical_group_2_which_is_the_root_process_instance() {
        final SearchHumanTaskInstanceDescriptor descriptor = new SearchHumanTaskInstanceDescriptor();

        final FieldDescriptor fieldDescriptor = descriptor.getEntityKeys().get(HumanTaskInstanceSearchDescriptor.PROCESS_INSTANCE_ID);

        assertThat(fieldDescriptor.getValue()).isEqualTo("logicalGroup2");
    }

    @Test
    public void getEntityKey_should_map_root_process_instance_to_the_logical_group_2_which_is_the_root_process_instance() {
        final SearchHumanTaskInstanceDescriptor descriptor = new SearchHumanTaskInstanceDescriptor();

        final FieldDescriptor fieldDescriptor = descriptor.getEntityKeys().get(HumanTaskInstanceSearchDescriptor.ROOT_PROCESS_INSTANCE_ID);

        assertThat(fieldDescriptor.getValue()).isEqualTo("logicalGroup2");
    }

    @Test
    public void getEntityKey_should_map_parent_process_instance_to_the_logical_group_4_which_is_the_parent_process_instance() {
        final SearchHumanTaskInstanceDescriptor descriptor = new SearchHumanTaskInstanceDescriptor();

        final FieldDescriptor fieldDescriptor = descriptor.getEntityKeys().get(HumanTaskInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID);

        assertThat(fieldDescriptor.getValue()).isEqualTo("logicalGroup4");
    }

}
