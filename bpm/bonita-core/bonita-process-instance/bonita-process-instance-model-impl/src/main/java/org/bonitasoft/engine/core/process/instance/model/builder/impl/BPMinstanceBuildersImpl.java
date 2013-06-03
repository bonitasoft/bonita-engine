/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.instance.model.builder.impl;

import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAAutomaticTaskInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SACallActivityInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAConnectorInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAGatewayInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SALoopActivityInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAManualTaskInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAMultiInstanceActivityInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAProcessInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAReceiveTaskInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SASendTaskInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SASubProcessActivityInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SATransitionInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAUserTaskInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.event.SAEndEventInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.event.SAStartEventInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.event.impl.SAEndEventInstanceBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.event.impl.SAStartEventInstanceBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.impl.SAAutomaticTaskInstanceBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.impl.SACallActivityInstanceBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.impl.SAConnectorInstanceBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.impl.SAGatewayInstanceBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.impl.SALoopActivityInstanceBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.impl.SAManualTaskInstanceBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.impl.SAMultiInstanceActivityInstanceBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.impl.SAProcessInstanceBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.impl.SAReceiveTaskInstanceBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.impl.SASendTaskInstanceBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.impl.SASubProcessActivityInstanceBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.impl.SATransitionInstanceBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.impl.SAUserTaskInstanceBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.builder.GatewayInstanceLogBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.ProcessInstanceLogBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SAutomaticTaskInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SCallActivityInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SConnectorInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SConnectorInstanceLogBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SFlowNodeInstanceLogBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SGatewayInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SHiddenTaskInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SHiddenTaskInstanceLogBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SLoopActivityInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SManualTaskInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SMultiInstanceActivityInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SPendingActivityMappingBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SPendingActivityMappingLogBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceUpdateBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SReceiveTaskInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SSendTaskInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SSubProcessActivityInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.STokenBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.STransitionInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.STransitionInstanceLogBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SBoundaryEventInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SEndEventInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SIntermediateCatchEventInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SIntermediateThrowEventInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SStartEventInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SMessageInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingErrorEventBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingEventLogBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingMessageEventBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingSignalEventBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.impl.SMessageInstanceBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.impl.SWaitingErrorEventBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.impl.SWaitingEventLogBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.impl.SWaitingMessageEventBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.impl.SWaitingSignalEventBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.builder.event.impl.SBoundaryEventInstanceBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.builder.event.impl.SEndEventInstanceBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.builder.event.impl.SIntermediateCatchEventInstanceBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.builder.event.impl.SIntermediateThrowEventInstanceBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.builder.event.impl.SStartEventInstanceBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.SEventTriggerInstanceLogBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.SThrowErrorEventTriggerInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.SThrowMessageEventTriggerInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.SThrowSignalEventTriggerInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.STimerEventTriggerInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.impl.SThrowErrorEventTriggerInstanceBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.impl.SThrowMessageEventTriggerInstanceBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.impl.SThrowSignalEventTriggerInstanceBuilderImpl;
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.impl.STimerEventTriggerInstanceBuilderImpl;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Hongwen Zang
 * @author Celine Souchet
 */
public class BPMinstanceBuildersImpl implements BPMInstanceBuilders {

    @Override
    public SProcessInstanceBuilder getSProcessInstanceBuilder() {
        return new SProcessInstanceBuilderImpl();
    }

    @Override
    public STokenBuilder getSTokenBuilder() {
        return new STokenBuilderImpl();
    }

    @Override
    public SUserTaskInstanceBuilder getSUserTaskInstanceBuilder() {
        return new SUserTaskInstanceBuilderImpl();
    }

    @Override
    public SAutomaticTaskInstanceBuilder getSAutomaticTaskInstanceBuilder() {
        return new SAutomaticTaskInstanceBuilderImpl();
    }

    @Override
    public SReceiveTaskInstanceBuilder getSReceiveTaskInstanceBuilder() {
        return new SReceiveTaskInstanceBuilderImpl();
    }

    @Override
    public SManualTaskInstanceBuilder getSManualTaskInstanceBuilder() {
        return new SManualTaskInstanceBuilderImpl();
    }

    @Override
    public SCallActivityInstanceBuilder getSCallActivityInstanceBuilder() {
        return new SCallActivityInstanceBuilderImpl();
    }

    @Override
    public SACallActivityInstanceBuilder getSACallActivityInstanceBuilder() {
        return new SACallActivityInstanceBuilderImpl();
    }

    @Override
    public SSubProcessActivityInstanceBuilder getSSubProcessActivityInstanceBuilder() {
        return new SSubProcessActivityInstanceBuilderImpl();
    }

    @Override
    public SASubProcessActivityInstanceBuilder getSASubProcessActivityInstanceBuilder() {
        return new SASubProcessActivityInstanceBuilderImpl();
    }

    @Override
    public SAManualTaskInstanceBuilder getSAManualTaskInstanceBuilder() {
        return new SAManualTaskInstanceBuilderImpl();
    }

    @Override
    public SAProcessInstanceBuilder getSAProcessInstanceBuilder() {
        return new SAProcessInstanceBuilderImpl();
    }

    @Override
    public SFlowNodeInstanceLogBuilder getActivityInstanceLogBuilder() {
        return new SFlowNodeInstanceLogBuilderImpl();
    }

    @Override
    public ProcessInstanceLogBuilder getProcessInstanceLogBuilder() {
        return new ProcessInstanceLogBuilderImpl();
    }

    @Override
    public SAAutomaticTaskInstanceBuilder getSAAutomaticTaskInstanceBuilder() {
        return new SAAutomaticTaskInstanceBuilderImpl();
    }

    @Override
    public SAReceiveTaskInstanceBuilder getSAReceiveTaskInstanceBuilder() {
        return new SAReceiveTaskInstanceBuilderImpl();
    }

    @Override
    public SAUserTaskInstanceBuilder getSAUserTaskInstanceBuilder() {
        return new SAUserTaskInstanceBuilderImpl();
    }

    @Override
    public GatewayInstanceLogBuilder getGatewayInstanceLogBuilder() {
        return new GatewayInstanceLogBuilderImpl();
    }

    @Override
    public SGatewayInstanceBuilder getSGatewayInstanceBuilder() {
        return new SGatewayInstanceBuilderImpl();
    }

    @Override
    public STransitionInstanceLogBuilder getSTransitionInstanceLogBuilder() {
        return new STransitionInstanceLogBuilderImpl();
    }

    @Override
    public STransitionInstanceBuilder getSTransitionInstanceBuilder() {
        return new STransitionInstanceBuilderImpl();
    }

    @Override
    public SConnectorInstanceBuilder getSConnectorInstanceBuilder() {
        return new SConnectorInstanceBuilderImpl();
    }

    @Override
    public SStartEventInstanceBuilder getSStartEventInstanceBuilder() {
        return new SStartEventInstanceBuilderImpl();
    }

    @Override
    public SEndEventInstanceBuilder getSEndEventInstanceBuilder() {
        return new SEndEventInstanceBuilderImpl();
    }

    @Override
    public SAStartEventInstanceBuilder getSArchiveStartEventInstanceBuilder() {
        return new SAStartEventInstanceBuilderImpl();
    }

    @Override
    public SAEndEventInstanceBuilder getSArchiveEndEventInstanceBuilder() {
        return new SAEndEventInstanceBuilderImpl();
    }

    @Override
    public STimerEventTriggerInstanceBuilder getSTimerEventTriggerInstanceBuilder() {
        return new STimerEventTriggerInstanceBuilderImpl();
    }

    @Override
    public SConnectorInstanceLogBuilder getConnectorInstanceLofBuilder() {
        return new SConnectorInstanceLogBuilderImpl();
    }

    @Override
    public SEventTriggerInstanceLogBuilder getSEventTriggerInstanceLogBuilder() {
        return new SEventTriggerInstanceLogBuilderImpl();
    }

    @Override
    public SThrowErrorEventTriggerInstanceBuilder getSThrowErrorEventTriggerInstanceBuilder() {
        return new SThrowErrorEventTriggerInstanceBuilderImpl();
    }

    @Override
    public SWaitingErrorEventBuilder getSWaitingErrorEventBuilder() {
        return new SWaitingErrorEventBuilderImpl();
    }

    @Override
    public SUserTaskInstanceBuilder getUserTaskInstanceBuilder() {
        return new SUserTaskInstanceBuilderImpl();
    }

    @Override
    public SIntermediateCatchEventInstanceBuilder getSIntermediateCatchEventInstanceBuilder() {
        return new SIntermediateCatchEventInstanceBuilderImpl();
    }

    @Override
    public SBoundaryEventInstanceBuilder getSBoundaryEventInstanceBuilder() {
        return new SBoundaryEventInstanceBuilderImpl();
    }

    @Override
    public SIntermediateThrowEventInstanceBuilder getSIntermediateThrowEventInstanceBuilder() {
        return new SIntermediateThrowEventInstanceBuilderImpl();
    }

    @Override
    public SWaitingEventLogBuilder getSStartEventTriggerInstanceLogBuilder() {
        return new SWaitingEventLogBuilderImpl();
    }

    @Override
    public SWaitingMessageEventBuilder getSWaitingMessageEventBuilder() {
        return new SWaitingMessageEventBuilderImpl();
    }

    @Override
    public SMessageInstanceBuilder getSMessageInstanceBuilder() {
        return new SMessageInstanceBuilderImpl();
    }

    @Override
    public SThrowMessageEventTriggerInstanceBuilder getSThrowMessageEventTriggerInstanceBuilder() {
        return new SThrowMessageEventTriggerInstanceBuilderImpl();
    }

    @Override
    public SWaitingSignalEventBuilder getSWaitingSignalEventBuilder() {
        return new SWaitingSignalEventBuilderImpl();
    }

    @Override
    public SThrowSignalEventTriggerInstanceBuilder getSThrowSignalEventTriggerInstanceBuilder() {
        return new SThrowSignalEventTriggerInstanceBuilderImpl();
    }

    @Override
    public SPendingActivityMappingBuilder getSPendingActivityMappingBuilder() {
        return new SPendingActivityMappingBuilderImpl();
    }

    @Override
    public SPendingActivityMappingLogBuilder getPendingActivityMappingLogBuilder() {
        return new SPendingActivityMappingLogBuilderImpl();
    }

    @Override
    public SLoopActivityInstanceBuilder getSLoopActivityInstanceBuilder() {
        return new SLoopActivityInstanceBuilderImpl();
    }

    @Override
    public SProcessInstanceUpdateBuilder getProcessInstanceUpdateBuilder() {
        return new SProcessInstanceUpdateBuilderImpl();
    }

    @Override
    public SALoopActivityInstanceBuilder getSALoopActivitynstanceBuilder() {
        return new SALoopActivityInstanceBuilderImpl();
    }

    @Override
    public SMultiInstanceActivityInstanceBuilder getSMultiInstanceActivityInstanceBuilder() {
        return new SMultiInstanceActivityInstanceBuilderImpl();
    }

    @Override
    public SAGatewayInstanceBuilder getSAGatewayInstanceBuilder() {
        return new SAGatewayInstanceBuilderImpl();
    }

    @Override
    public SATransitionInstanceBuilder getSATransitionInstanceBuilder() {
        return new SATransitionInstanceBuilderImpl();
    }

    @Override
    public SHiddenTaskInstanceBuilder getSHiddenTaskInstanceBuilder() {
        return new SHiddenTaskInstanceBuilderImpl();
    }

    @Override
    public SHiddenTaskInstanceLogBuilder getHiddenTaskInstanceLogBuilder() {
        return new SHiddenTaskInstanceLogBuilderImpl();
    }

    @Override
    public SAConnectorInstanceBuilder getSAConnectorInstanceBuilder() {
        return new SAConnectorInstanceBuilderImpl();
    }

    @Override
    public SAMultiInstanceActivityInstanceBuilder getSAMultiInstanceActivitynstanceBuilder() {
        return new SAMultiInstanceActivityInstanceBuilderImpl();
    }

    @Override
    public SSendTaskInstanceBuilder getSSendTaskInstanceBuilder() {
        return new SSendTaskInstanceBuilderImpl();
    }

    @Override
    public SASendTaskInstanceBuilder getSASendTaskInstanceBuilder() {
        return new SASendTaskInstanceBuilderImpl();
    }

}
