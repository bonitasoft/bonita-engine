/*
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
 */
package org.bonitasoft.engine.core.process.instance.model.archive.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.core.process.instance.model.impl.SUserTaskInstanceImpl;
import org.junit.Test;

/**
 * author Emmanuel Duchastenier
 */
public class SAHumanTaskInstanceImplTest {

    @Test
    public void creatingArchiveShouldCopyAllRelevantAttibute() {
        final SUserTaskInstanceImpl taskInstance = new SUserTaskInstanceImpl("taskName", 147L, 2565412L, 874555L, 7L, STaskPriority.UNDER_NORMAL, 7777L, 8888L);
        final long claimedDate = System.currentTimeMillis();
        taskInstance.setClaimedDate(claimedDate);
        taskInstance.setStateId(3);
        taskInstance.setStateName("estado");
        taskInstance.setAssigneeId(8412L);
        taskInstance.setDescription("tarea de usuario");
        taskInstance.setDisplayDescription("tarea del usuario Pepo");
        taskInstance.setDisplayName("task for everyone");
        taskInstance.setExecutedBy(987987L);
        taskInstance.setExecutedBySubstitute(11111L);
        final long expectedEndDate = System.currentTimeMillis() + 10;
        taskInstance.setExpectedEndDate(expectedEndDate);
        final SAUserTaskInstanceImpl archivedTaskInstance = new SAUserTaskInstanceImpl(taskInstance);

        assertThat(archivedTaskInstance.getType()).isEqualTo(taskInstance.getType());
        assertThat(archivedTaskInstance.getActorId()).isEqualTo(7L);
        assertThat(archivedTaskInstance.getAssigneeId()).isEqualTo(8412L);
        assertThat(archivedTaskInstance.getExpectedEndDate()).isEqualTo(expectedEndDate);
        assertThat(archivedTaskInstance.getPriority()).isEqualTo(STaskPriority.UNDER_NORMAL);
        assertThat(archivedTaskInstance.getClaimedDate()).isEqualTo(claimedDate);
        assertThat(archivedTaskInstance.getDescription()).isEqualTo("tarea de usuario");
        assertThat(archivedTaskInstance.getDisplayDescription()).isEqualTo("tarea del usuario Pepo");
        assertThat(archivedTaskInstance.getDisplayName()).isEqualTo("task for everyone");
        assertThat(archivedTaskInstance.getExecutedBy()).isEqualTo(987987L);
        assertThat(archivedTaskInstance.getExecutedBySubstitute()).isEqualTo(11111L);
        assertThat(archivedTaskInstance.getName()).isEqualTo("taskName");
        assertThat(archivedTaskInstance.getSourceObjectId()).isEqualTo(taskInstance.getId());
        assertThat(archivedTaskInstance.getStateName()).isEqualTo("estado");
        assertThat(archivedTaskInstance.getStateId()).isEqualTo(3);
        assertThat(archivedTaskInstance.getParentActivityInstanceId()).isEqualTo(taskInstance.getParentActivityInstanceId());
        assertThat(archivedTaskInstance.getParentContainerId()).isEqualTo(874555L);
        assertThat(archivedTaskInstance.getParentProcessInstanceId()).isEqualTo(taskInstance.getParentProcessInstanceId());
        assertThat(archivedTaskInstance.getRootContainerId()).isEqualTo(2565412L);
        assertThat(archivedTaskInstance.getFlowNodeDefinitionId()).isEqualTo(147L);
        assertThat(archivedTaskInstance.getLogicalGroup(0)).isEqualTo(7777L);
        assertThat(archivedTaskInstance.getLogicalGroup(1)).isEqualTo(8888L);
    }

}
