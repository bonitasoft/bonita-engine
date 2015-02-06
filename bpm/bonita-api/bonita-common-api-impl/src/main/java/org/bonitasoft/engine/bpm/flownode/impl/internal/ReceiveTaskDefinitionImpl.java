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

import org.bonitasoft.engine.bpm.flownode.CatchMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.ReceiveTaskDefinition;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.operation.Operation;

/**
 * @author Julien Molinaro
 * @author Celine Souchet
 */
public class ReceiveTaskDefinitionImpl extends TaskDefinitionImpl implements ReceiveTaskDefinition {

    private static final long serialVersionUID = -5793747387538282891L;

    private final CatchMessageEventTriggerDefinitionImpl trigger;

    public ReceiveTaskDefinitionImpl(final String name, final String messageName) {
        super(name);
        trigger = new CatchMessageEventTriggerDefinitionImpl(messageName);
    }

    public ReceiveTaskDefinitionImpl(final long id, final String name, final CatchMessageEventTriggerDefinition catchMessageEventTriggerDefinition) {
        super(id, name);
        trigger = new CatchMessageEventTriggerDefinitionImpl(catchMessageEventTriggerDefinition);
    }

    public void addCorrelation(final Expression key, final Expression value) {
        trigger.addCorrelation(key, value);
    }

    public void addMessageOperation(final Operation operation) {
        trigger.addOperation(operation);
    }

    @Override
    public CatchMessageEventTriggerDefinition getTrigger() {
        return trigger;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        final ReceiveTaskDefinitionImpl other = (ReceiveTaskDefinitionImpl) obj;
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
