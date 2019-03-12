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
package org.bonitasoft.engine.core.process.instance.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;

/**
 * @author Baptiste Mesta
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SMultiInstanceActivityInstance extends SActivityInstance {

    private boolean sequential;
    private String loopDataInputRef;
    private String loopDataOutputRef;
    private String dataInputItemRef;
    private String dataOutputItemRef;
    private int numberOfActiveInstances;
    private int numberOfCompletedInstances;
    private int numberOfTerminatedInstances;
    private int loopCardinality;

    public SMultiInstanceActivityInstance(final String name, final long flowNodeDefinitionId, final long rootContainerId, final long parentContainerId,
                                              final long processDefinitionId, final long rootProcessInstanceId, final boolean isSequential) {
        super(name, flowNodeDefinitionId, rootContainerId, parentContainerId, processDefinitionId, rootProcessInstanceId);
        sequential = isSequential;
    }

    @Override
    public SFlowNodeType getType() {
        return SFlowNodeType.MULTI_INSTANCE_ACTIVITY;
    }

    public int getNumberOfInstances() {
        return numberOfActiveInstances + numberOfCompletedInstances + numberOfTerminatedInstances;
    }

    @Override
    public boolean mustExecuteOnAbortOrCancelProcess() {
        // it's not necessary to execute it because this will be done when the last child reaches the aborted state
        return false;
    }

}
