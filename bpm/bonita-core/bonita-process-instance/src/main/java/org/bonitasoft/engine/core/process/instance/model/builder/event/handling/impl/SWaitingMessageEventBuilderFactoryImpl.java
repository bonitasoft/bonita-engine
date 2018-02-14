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
package org.bonitasoft.engine.core.process.instance.model.builder.event.handling.impl;

import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingMessageEventBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingMessageEventBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SBPMEventType;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.impl.SWaitingMessageEventImpl;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class SWaitingMessageEventBuilderFactoryImpl extends SWaitingEventKeyProviderBuilderFactoryImpl implements SWaitingMessageEventBuilderFactory {

    @Override
    public SWaitingMessageEventBuilder createNewWaitingMessageStartEventInstance(final long processdefinitionId, final String messageName,
            final String processName, final long flowNodeDefinitionId, final String flowNodeName) {
        final SWaitingMessageEventImpl entity = new SWaitingMessageEventImpl(SBPMEventType.START_EVENT, processdefinitionId, processName, flowNodeDefinitionId, flowNodeName, messageName);
        return new SWaitingMessageEventBuilderImpl(entity);
    }

    @Override
    public SWaitingMessageEventBuilder createNewWaitingMessageEventSubProcInstance(final long processdefinitionId, final long parentProcessInstanceId,
            final long rootProcessInstanceId, final String messageName, final String processName, final long flowNodeDefinitionId, final String flowNodeName,
            final long subProcessId) {
        final SWaitingMessageEventImpl entity = new SWaitingMessageEventImpl(SBPMEventType.EVENT_SUB_PROCESS, processdefinitionId, processName, flowNodeDefinitionId, flowNodeName,
                messageName);
        entity.setRootProcessInstanceId(rootProcessInstanceId);
        entity.setParentProcessInstanceId(parentProcessInstanceId);
        entity.setSubProcessId(subProcessId);
        return new SWaitingMessageEventBuilderImpl(entity);
    }

    @Override
    public SWaitingMessageEventBuilder createNewWaitingMessageIntermediateEventInstance(final long processdefinitionId, final long rootProcessInstanceId, final long processInstanceId,
            final long flowNodeInstanceId, final String messageName, final String processName, final long flowNodeDefinitionId, final String flowNodeName) {
        final SWaitingMessageEventImpl entity = new SWaitingMessageEventImpl(SBPMEventType.INTERMEDIATE_CATCH_EVENT, processdefinitionId, processName, flowNodeDefinitionId, flowNodeName,
                messageName);
        entity.setRootProcessInstanceId(rootProcessInstanceId);
        entity.setParentProcessInstanceId(processInstanceId);
        entity.setFlowNodeInstanceId(flowNodeInstanceId);
        return new SWaitingMessageEventBuilderImpl(entity);
    }

    @Override
    public SWaitingMessageEventBuilder createNewWaitingMessageBoundaryEventInstance(final long processdefinitionId, final long rootProcessInstanceId, final long processInstanceId,
            final long flowNodeInstanceId, final String messageName, final String processName, final long flowNodeDefinitionId, final String flowNodeName) {
        final SWaitingMessageEventImpl entity = new SWaitingMessageEventImpl(SBPMEventType.BOUNDARY_EVENT, processdefinitionId, processName, flowNodeDefinitionId, flowNodeName, messageName);
        entity.setRootProcessInstanceId(rootProcessInstanceId);
        entity.setParentProcessInstanceId(processInstanceId);
        entity.setFlowNodeInstanceId(flowNodeInstanceId);
        return new SWaitingMessageEventBuilderImpl(entity);
    }

    @Override
    public SWaitingMessageEventBuilder createNewInstance(final SWaitingMessageEvent waitingMessage) {
        final SWaitingMessageEventImpl entity = new SWaitingMessageEventImpl(waitingMessage.getEventType(), waitingMessage.getProcessDefinitionId(), waitingMessage.getProcessName(),
                waitingMessage.getFlowNodeDefinitionId(), waitingMessage.getFlowNodeName(), waitingMessage.getMessageName());
        entity.setRootProcessInstanceId(waitingMessage.getRootProcessInstanceId());
        entity.setParentProcessInstanceId(waitingMessage.getParentProcessInstanceId());
        entity.setFlowNodeInstanceId(waitingMessage.getFlowNodeInstanceId());
        return new SWaitingMessageEventBuilderImpl(entity);
    }

    @Override
    public String getMessageNameKey() {
        return "messageName";
    }

    @Override
    public String getLockedKey() {
        return "locked";
    }

    @Override
    public String getProgressKey() {
        return "progress";
    }

}
