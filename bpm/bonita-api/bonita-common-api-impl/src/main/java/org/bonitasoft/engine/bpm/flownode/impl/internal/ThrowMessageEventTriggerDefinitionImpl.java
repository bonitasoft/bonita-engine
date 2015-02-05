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
package org.bonitasoft.engine.bpm.flownode.impl.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.flownode.CorrelationDefinition;
import org.bonitasoft.engine.bpm.flownode.ThrowMessageEventTriggerDefinition;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Elias Ricken de Medeiros
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 */
public class ThrowMessageEventTriggerDefinitionImpl extends MessageEventTriggerDefinitionImpl implements ThrowMessageEventTriggerDefinition {

    private static final long serialVersionUID = -1678256136944568540L;

    private Expression targetProcess;

    private Expression targetFlowNode;

    private final List<DataDefinition> dataDefinitions;

    public ThrowMessageEventTriggerDefinitionImpl(final String messageName) {
        super(messageName);
        dataDefinitions = new ArrayList<DataDefinition>();
    }

    public ThrowMessageEventTriggerDefinitionImpl(final String name, final Expression targetProcess, final Expression targetFlowNode) {
        super(name);
        this.targetProcess = targetProcess;
        this.targetFlowNode = targetFlowNode;
        dataDefinitions = new ArrayList<DataDefinition>();
    }

    public ThrowMessageEventTriggerDefinitionImpl(final String name, final Expression targetProcess) {
        super(name);
        this.targetProcess = targetProcess;
        targetFlowNode = null;
        dataDefinitions = new ArrayList<DataDefinition>();
    }

    public ThrowMessageEventTriggerDefinitionImpl(final String name, final Expression targetProcess, final Expression targetFlowNode,
            final List<DataDefinition> dataDefinitions, final List<CorrelationDefinition> correlations) {
        super(name, correlations);
        this.targetProcess = targetProcess;
        this.targetFlowNode = targetFlowNode;
        this.dataDefinitions = dataDefinitions;
    }

    public ThrowMessageEventTriggerDefinitionImpl(final ThrowMessageEventTriggerDefinition trigger) {
        super(trigger);
        targetFlowNode = trigger.getTargetFlowNode();
        targetProcess = trigger.getTargetProcess();
        dataDefinitions = trigger.getDataDefinitions();
    }

    @Override
    public Expression getTargetProcess() {
        return targetProcess;
    }

    @Override
    public Expression getTargetFlowNode() {
        return targetFlowNode;
    }

    @Override
    public List<DataDefinition> getDataDefinitions() {
        return Collections.unmodifiableList(dataDefinitions);
    }

    public void setTargetProcess(final Expression targetProcess) {
        this.targetProcess = targetProcess;
    }

    public void setTargetFlowNode(final Expression targetFlowNode) {
        this.targetFlowNode = targetFlowNode;
    }

    public void addDataDefinition(final DataDefinition datadefiniton) {
        dataDefinitions.add(datadefiniton);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (dataDefinitions == null ? 0 : dataDefinitions.hashCode());
        result = prime * result + (targetFlowNode == null ? 0 : targetFlowNode.hashCode());
        result = prime * result + (targetProcess == null ? 0 : targetProcess.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ThrowMessageEventTriggerDefinitionImpl other = (ThrowMessageEventTriggerDefinitionImpl) obj;
        if (dataDefinitions == null) {
            if (other.dataDefinitions != null) {
                return false;
            }
        } else if (!dataDefinitions.equals(other.dataDefinitions)) {
            return false;
        }
        if (targetFlowNode == null) {
            if (other.targetFlowNode != null) {
                return false;
            }
        } else if (!targetFlowNode.equals(other.targetFlowNode)) {
            return false;
        }
        if (targetProcess == null) {
            if (other.targetProcess != null) {
                return false;
            }
        } else if (!targetProcess.equals(other.targetProcess)) {
            return false;
        }
        return true;
    }

}
