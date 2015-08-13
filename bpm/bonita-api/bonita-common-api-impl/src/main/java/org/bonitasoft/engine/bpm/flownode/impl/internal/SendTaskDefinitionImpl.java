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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.engine.bpm.flownode.SendTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.ThrowMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.expression.Expression;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.Objects;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SendTaskDefinitionImpl extends TaskDefinitionImpl implements SendTaskDefinition {

    private static final long serialVersionUID = -3069440054837402115L;
    @XmlElement(name = "throwMessageEventTrigger")
    private final ThrowMessageEventTriggerDefinitionImpl trigger;

    public SendTaskDefinitionImpl(final String name, final String messageName, final Expression targetProcess) {
        super(name);
        trigger = new ThrowMessageEventTriggerDefinitionImpl(messageName);
        trigger.setTargetProcess(targetProcess);
    }

    public SendTaskDefinitionImpl(final long id, final String name, final ThrowMessageEventTriggerDefinition trigger) {
        super(id, name);
        this.trigger = new ThrowMessageEventTriggerDefinitionImpl(trigger);
    }

    public SendTaskDefinitionImpl() {
        super();
        trigger = new ThrowMessageEventTriggerDefinitionImpl();
    }

    public void setTargetFlowNode(final Expression targetFlowNode) {
        trigger.setTargetFlowNode(targetFlowNode);
    }

    public void addCorrelation(final Expression key, final Expression value) {
        trigger.addCorrelation(key, value);
    }

    @Override
    public ThrowMessageEventTriggerDefinition getMessageTrigger() {
        return trigger;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        SendTaskDefinitionImpl that = (SendTaskDefinitionImpl) o;
        return Objects.equals(trigger, that.trigger);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), trigger);
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        super.accept(visitor, modelId);
        visitor.find(this, modelId);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("trigger", trigger)
                .toString();
    }
}
