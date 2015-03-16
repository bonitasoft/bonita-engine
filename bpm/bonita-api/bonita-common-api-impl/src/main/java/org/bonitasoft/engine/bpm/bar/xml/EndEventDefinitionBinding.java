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

import org.bonitasoft.engine.bpm.flownode.TerminateEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.ThrowErrorEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.internal.EndEventDefinitionImpl;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class EndEventDefinitionBinding extends ThrowEventDefinitionBinding {

    private TerminateEventTriggerDefinition terminateEventTrigger;

    private final List<ThrowErrorEventTriggerDefinition> errorEventTriggers;

    public EndEventDefinitionBinding() {
        super();
        errorEventTriggers = new ArrayList<ThrowErrorEventTriggerDefinition>(1);
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        super.setChildObject(name, value);
        if (XMLProcessDefinition.TERMINATE_EVENT_TRIGGER_NODE.equals(name)) {
            terminateEventTrigger = (TerminateEventTriggerDefinition) value;
        }
        if (XMLProcessDefinition.THROW_ERROR_EVENT_TRIGGER_NODE.equals(name)) {
            errorEventTriggers.add((ThrowErrorEventTriggerDefinition) value);
        }
    }

    @Override
    public Object getObject() {
        final EndEventDefinitionImpl endEventDefinitionImpl = new EndEventDefinitionImpl(id, name);
        fillNode(endEventDefinitionImpl);
        endEventDefinitionImpl.setTerminateEventTriggerDefinition(terminateEventTrigger);
        for (final ThrowErrorEventTriggerDefinition errorEventTrigger : errorEventTriggers) {
            endEventDefinitionImpl.addErrorEventTriggerDefinition(errorEventTrigger);
        }
        return endEventDefinitionImpl;
    }

    @Override
    public String getElementTag() {
        return XMLProcessDefinition.END_EVENT_NODE;
    }

}
