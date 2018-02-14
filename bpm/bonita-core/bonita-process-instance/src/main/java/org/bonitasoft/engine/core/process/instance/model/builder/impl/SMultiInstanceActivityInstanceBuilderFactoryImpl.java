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
package org.bonitasoft.engine.core.process.instance.model.builder.impl;

import org.bonitasoft.engine.core.process.instance.model.builder.SMultiInstanceActivityInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SMultiInstanceActivityInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.impl.SMultiInstanceActivityInstanceImpl;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class SMultiInstanceActivityInstanceBuilderFactoryImpl extends SActivityInstanceBuilderFactoryImpl implements SMultiInstanceActivityInstanceBuilderFactory {

    @Override
    public SMultiInstanceActivityInstanceBuilder createNewOuterTaskInstance(final String name, final long flowNodeDefinitionId, final long rootContainerId,
            final long parentContainerId, final long processDefinitionId, final long rootProcessInstanceId, final long parentProcessInstanceId,
            final boolean isSequential) {
        final SMultiInstanceActivityInstanceImpl entity = new SMultiInstanceActivityInstanceImpl(name, flowNodeDefinitionId, rootContainerId, parentContainerId, processDefinitionId,
                rootProcessInstanceId, isSequential);
        entity.setLogicalGroup(PARENT_PROCESS_INSTANCE_INDEX, parentProcessInstanceId);
        return new SMultiInstanceActivityInstanceBuilderImpl(entity);
    }

    @Override
    public String getLoopCardinalityKey() {
        return "loopCardinality";
    }

    @Override
    public String getNumberOfActiveInstancesKey() {
        return "numberOfActiveInstances";
    }

    @Override
    public String getNumberOfCompletedInstancesKey() {
        return "numberOfCompletedInstances";
    }

    @Override
    public String getNumberOfTerminatedInstancesKey() {
        return "numberOfTerminatedInstances";
    }

}
