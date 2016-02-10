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

import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingErrorEventBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingErrorEventBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SBPMEventType;
import org.bonitasoft.engine.core.process.instance.model.event.handling.impl.SWaitingErrorEventImpl;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class SWaitingErrorEventBuilderFactoryImpl extends SWaitingEventKeyProviderBuilderFactoryImpl implements SWaitingErrorEventBuilderFactory {

    @Override
    public SWaitingErrorEventBuilder createNewWaitingErrorBoundaryEventInstance(final long processdefinitionId, final long rootProcessInstanceId,
            final long parentProcessInstanceId, final long flowNodeInstanceId, final String errorCode, final String processName,
            final long flowNodeDefinitionId, final String flowNodeName, final long relatedActivityInstanceId) {
        final SWaitingErrorEventImpl entity = new SWaitingErrorEventImpl(SBPMEventType.BOUNDARY_EVENT, processdefinitionId, processName, flowNodeDefinitionId, flowNodeName, errorCode);
        entity.setFlowNodeInstanceId(flowNodeInstanceId);
        entity.setRootProcessInstanceId(rootProcessInstanceId);
        entity.setParentProcessInstanceId(parentProcessInstanceId);
        entity.setRelatedActivityInstanceId(relatedActivityInstanceId);
        return new SWaitingErrorEventBuilderImpl(entity);
    }

    @Override
    public SWaitingErrorEventBuilder createNewWaitingErrorEventSubProcInstance(final long processdefinitionId, final long parentProcessInstanceId,
            final long rootProcessInstanceId, final String errorCode, final String processName, final long flowNodeDefinitionId, final String flowNodeName,
            final long subProcessId) {
        final SWaitingErrorEventImpl entity = new SWaitingErrorEventImpl(SBPMEventType.EVENT_SUB_PROCESS, processdefinitionId, processName, flowNodeDefinitionId, flowNodeName, errorCode);
        entity.setRootProcessInstanceId(rootProcessInstanceId);
        entity.setParentProcessInstanceId(parentProcessInstanceId);
        entity.setSubProcessId(subProcessId);
        return new SWaitingErrorEventBuilderImpl(entity);
    }

    @Override
    public String getErrorCodeKey() {
        return "errorCode";
    }
    
}
