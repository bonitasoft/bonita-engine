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

import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.data.impl.DataDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.CorrelationDefinition;
import org.bonitasoft.engine.bpm.flownode.ThrowMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Elias Ricken de Medeiros
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ThrowMessageEventTriggerDefinitionImpl extends MessageEventTriggerDefinitionImpl implements ThrowMessageEventTriggerDefinition {

    private static final long serialVersionUID = -1678256136944568540L;
    @XmlElement(type = ExpressionImpl.class)
    private Expression targetProcess;
    @XmlElement(type = ExpressionImpl.class)
    private Expression targetFlowNode;
    @XmlElement(type = DataDefinitionImpl.class)
    private final List<DataDefinition> dataDefinitions;

    public ThrowMessageEventTriggerDefinitionImpl() {
        super();
        dataDefinitions = new ArrayList<>();
    }

    public ThrowMessageEventTriggerDefinitionImpl(final String messageName) {
        super(messageName);
        dataDefinitions = new ArrayList<>();
    }

    public ThrowMessageEventTriggerDefinitionImpl(final String name, final Expression targetProcess, final Expression targetFlowNode) {
        super(name);
        this.targetProcess = targetProcess;
        this.targetFlowNode = targetFlowNode;
        dataDefinitions = new ArrayList<>();
    }

    public ThrowMessageEventTriggerDefinitionImpl(final String name, final Expression targetProcess) {
        super(name);
        this.targetProcess = targetProcess;
        targetFlowNode = null;
        dataDefinitions = new ArrayList<>();
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
    public void accept(ModelFinderVisitor visitor, long modelId) {
        super.accept(visitor, modelId);
        visitor.find(this, modelId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        ThrowMessageEventTriggerDefinitionImpl that = (ThrowMessageEventTriggerDefinitionImpl) o;
        return Objects.equals(targetProcess, that.targetProcess) &&
                Objects.equals(targetFlowNode, that.targetFlowNode) &&
                Objects.equals(dataDefinitions, that.dataDefinitions);
    }
}
