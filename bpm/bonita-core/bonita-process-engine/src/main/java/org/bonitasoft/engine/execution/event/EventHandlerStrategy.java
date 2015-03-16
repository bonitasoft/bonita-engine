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
package org.bonitasoft.engine.execution.event;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEndEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerDefinition;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SCatchEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SThrowEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;

/**
 * Strategy to handle one kind of event: TIMER, ERROR, SIGNAL or MESSAGE
 * 
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public abstract class EventHandlerStrategy {

    public EventHandlerStrategy() {
        super();
    }

    public abstract void handleThrowEvent(SProcessDefinition processDefinition, SEventDefinition eventDefinition, SThrowEventInstance eventInstance,
            SEventTriggerDefinition sEventTriggerDefinition) throws SBonitaException;

    public abstract boolean handlePostThrowEvent(SProcessDefinition processDefinition, SEndEventDefinition sEventDefinition,
            SThrowEventInstance sThrowEventInstance, SEventTriggerDefinition sEventTriggerDefinition, SFlowNodeInstance sFlowNodeInstance)
            throws SBonitaException;

    public abstract void handleThrowEvent(SEventTriggerDefinition sEventTriggerDefinition) throws SBonitaException;

    public abstract void handleCatchEvent(SProcessDefinition processDefinition, SEventDefinition eventDefinition, SCatchEventInstance eventInstance,
            SEventTriggerDefinition sEventTriggerDefinition) throws SBonitaException;

    public abstract void handleEventSubProcess(SProcessDefinition processDefinition, SEventDefinition eventDefinition,
            SEventTriggerDefinition sEventTriggerDefinition, long subProcessId, SProcessInstance parentProcessInstance) throws SBonitaException;

    public abstract void unregisterCatchEvent(SProcessDefinition processDefinition, SEventDefinition eventDefinition,
            SEventTriggerDefinition sEventTriggerDefinition, long subProcessId, SProcessInstance parentProcessIsnstance) throws SBonitaException;

    protected DataInstanceContainer getParentContainerType(final SFlowNodeInstance flowNodeInstance) {
        DataInstanceContainer parentContainerType;
        if (SFlowElementsContainerType.PROCESS.equals(flowNodeInstance.getParentContainerType())) {
            parentContainerType = DataInstanceContainer.PROCESS_INSTANCE;
        } else {
            parentContainerType = DataInstanceContainer.ACTIVITY_INSTANCE;
        }
        return parentContainerType;
    }

    public abstract OperationsWithContext getOperations(SWaitingEvent waitingEvent, Long triggeringElementID) throws SBonitaException;

}
