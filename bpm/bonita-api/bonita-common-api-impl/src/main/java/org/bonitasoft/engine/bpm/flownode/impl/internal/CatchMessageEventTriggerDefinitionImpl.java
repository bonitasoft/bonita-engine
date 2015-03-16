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

import org.bonitasoft.engine.bpm.flownode.CatchMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.CorrelationDefinition;
import org.bonitasoft.engine.operation.Operation;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class CatchMessageEventTriggerDefinitionImpl extends MessageEventTriggerDefinitionImpl implements CatchMessageEventTriggerDefinition {

    private static final long serialVersionUID = -8667216649689173514L;

    private final List<Operation> operations;

    public CatchMessageEventTriggerDefinitionImpl(final String messageName) {
        super(messageName);
        operations = new ArrayList<Operation>(1);
    }

    public CatchMessageEventTriggerDefinitionImpl(final String messageName, final List<CorrelationDefinition> correlations) {
        super(messageName, correlations);
        operations = new ArrayList<Operation>(1);
    }

    public CatchMessageEventTriggerDefinitionImpl(final CatchMessageEventTriggerDefinition catchMessageEventTriggerDefinition) {
        super(catchMessageEventTriggerDefinition);
        operations = catchMessageEventTriggerDefinition.getOperations();
    }

    @Override
    public List<Operation> getOperations() {
        return Collections.unmodifiableList(operations);
    }

    public void addOperation(final Operation operation) {
        operations.add(operation);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((operations == null) ? 0 : operations.hashCode());
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
        final CatchMessageEventTriggerDefinitionImpl other = (CatchMessageEventTriggerDefinitionImpl) obj;
        if (operations == null) {
            if (other.operations != null) {
                return false;
            }
        } else if (!operations.equals(other.operations)) {
            return false;
        }
        return true;
    }

}
