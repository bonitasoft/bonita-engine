/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.instance.model.builder.event.impl;

import org.bonitasoft.engine.core.process.instance.model.builder.event.SBoundaryEventInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.event.SEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.impl.SBoundaryEventInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SFlowNodeInstanceImpl;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class SBoundaryEventInstanceBuilderImpl extends SEventInstanceBuilderImpl implements SBoundaryEventInstanceBuilder {

    private SBoundaryEventInstanceImpl entity = null;

    @Override
    public SBoundaryEventInstanceBuilder createNewBoundaryEventInstance(final String name, final boolean isInterrupting, final long flowNodeDefinitionId,
            final long rootContainerId, final long parentContainerId, final long processDefinitionId, final long rootProcessInstanceId,
            final long parentProcessInstanceId, final long activityInstanceId) {
        entity = new SBoundaryEventInstanceImpl(name, flowNodeDefinitionId, rootContainerId, parentContainerId, processDefinitionId, rootProcessInstanceId);
        entity.setLogicalGroup(PARENT_PROCESS_INSTANCE_INDEX, parentProcessInstanceId);
        entity.setActivityInstanceId(activityInstanceId);
        entity.setInterrupting(isInterrupting);
        return this;
    }

    @Override
    protected SFlowNodeInstanceImpl getEntity() {
        return entity;
    }

    @Override
    public SEventInstance done() {
        return entity;
    }

}
