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
package org.bonitasoft.engine.search.descriptor;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstanceSearchDescriptor;
import org.junit.Test;

public class SearchArchivedHumanTaskInstanceDescriptorTest {

    @Test
    public void getEntityKey_should_map_process_instance_to_the_logical_group_2_which_is_the_root_process_instance() {
        final SearchArchivedHumanTaskInstanceDescriptor descriptor = new SearchArchivedHumanTaskInstanceDescriptor();

        final FieldDescriptor fieldDescriptor = descriptor.getEntityKeys().get(ArchivedHumanTaskInstanceSearchDescriptor.PROCESS_INSTANCE_ID);

        assertThat(fieldDescriptor.getValue()).isEqualTo("logicalGroup2");
    }

    @Test
    public void getEntityKey_should_map_root_process_instance_to_the_logical_group_2_which_is_the_root_process_instance() {
        final SearchArchivedHumanTaskInstanceDescriptor descriptor = new SearchArchivedHumanTaskInstanceDescriptor();

        final FieldDescriptor fieldDescriptor = descriptor.getEntityKeys().get(ArchivedHumanTaskInstanceSearchDescriptor.ROOT_PROCESS_INSTANCE_ID);

        assertThat(fieldDescriptor.getValue()).isEqualTo("logicalGroup2");
    }

    @Test
    public void getEntityKey_should_map_parent_process_instance_to_the_logical_group_2_which_is_the_parent_process_instance() {
        final SearchArchivedHumanTaskInstanceDescriptor descriptor = new SearchArchivedHumanTaskInstanceDescriptor();

        final FieldDescriptor fieldDescriptor = descriptor.getEntityKeys().get(ArchivedHumanTaskInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID);

        assertThat(fieldDescriptor.getValue()).isEqualTo("logicalGroup4");
    }

}
