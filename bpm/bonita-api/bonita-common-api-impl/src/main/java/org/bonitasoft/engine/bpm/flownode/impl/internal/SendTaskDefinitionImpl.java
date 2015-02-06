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
import org.bonitasoft.engine.bpm.flownode.SendTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.ThrowMessageEventTriggerDefinition;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SendTaskDefinitionImpl extends TaskDefinitionImpl implements SendTaskDefinition {

    private static final long serialVersionUID = -3069440054837402115L;

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

    public void setTargetFlowNode(final Expression targetFlowNode) {
        trigger.setTargetFlowNode(targetFlowNode);
    }

    public void addMessageData(final DataDefinition datadefiniton) {
        trigger.addDataDefinition(datadefiniton);
    }

    public void addCorrelation(final Expression key, final Expression value) {
        trigger.addCorrelation(key, value);
    }

    @Override
    public ThrowMessageEventTriggerDefinition getMessageTrigger() {
        return trigger;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (trigger == null ? 0 : trigger.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SendTaskDefinitionImpl other = (SendTaskDefinitionImpl) obj;
        if (trigger == null) {
            if (other.trigger != null) {
                return false;
            }
        } else if (!trigger.equals(other.trigger)) {
            return false;
        }
        return true;
    }

}
