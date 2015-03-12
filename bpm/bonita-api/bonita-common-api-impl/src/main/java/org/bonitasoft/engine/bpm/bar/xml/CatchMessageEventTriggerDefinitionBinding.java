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
package org.bonitasoft.engine.bpm.bar.xml;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bpm.flownode.impl.internal.CatchMessageEventTriggerDefinitionImpl;
import org.bonitasoft.engine.operation.Operation;

/**
 * @author Elias Ricken de Medeiros
 */
public class CatchMessageEventTriggerDefinitionBinding extends MessageEventTriggerDefinitionBinding {

    private final List<Operation> operations = new ArrayList<Operation>();

    @Override
    public Object getObject() {
        final CatchMessageEventTriggerDefinitionImpl messageEventTrigger = new CatchMessageEventTriggerDefinitionImpl(getMessageName());
        fillNode(messageEventTrigger);
        return messageEventTrigger;
    }

    @Override
    public String getElementTag() {
        return XMLProcessDefinition.CATCH_MESSAGE_EVENT_TRIGGER_NODE;
    }

    protected void fillNode(final CatchMessageEventTriggerDefinitionImpl messageEventTrigger) {
        super.fillNode(messageEventTrigger);
        for (final Operation operation : operations) {
            messageEventTrigger.addOperation(operation);
        }
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        super.setChildObject(name, value);
        if (XMLProcessDefinition.OPERATION_NODE.equals(name)) {
            operations.add((Operation) value);
        }
    }

}
