/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.core.process.instance.model.builder.impl;

import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.core.process.instance.model.SUserTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilderFactory;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SUserTaskInstanceBuilderFactoryImpl extends SHumanTaskInstanceBuilderFactoryImpl
        implements SUserTaskInstanceBuilderFactory {

    @Override
    public SUserTaskInstanceBuilder createNewUserTaskInstance(final String name, final long flowNodeDefinitionId,
            final long rootContainerId,
            final long parentContainerId, final long actorId, final long processDefinitionId,
            final long rootProcessInstanceId,
            final long parentProcessInstanceId) {
        final SUserTaskInstance activityInst = new SUserTaskInstance(name, flowNodeDefinitionId, rootContainerId,
                parentContainerId, actorId, STaskPriority.NORMAL,
                processDefinitionId, rootProcessInstanceId);
        activityInst.setLogicalGroup(PARENT_PROCESS_INSTANCE_INDEX, parentProcessInstanceId);
        return new SUserTaskInstanceBuilderImpl(activityInst);
    }

}
