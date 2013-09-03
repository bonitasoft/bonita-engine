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
package org.bonitasoft.engine.core.process.instance.model.builder;

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
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.SEventTriggerInstanceLogBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.SThrowErrorEventTriggerInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.SThrowMessageEventTriggerInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.SThrowSignalEventTriggerInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.STimerEventTriggerInstanceBuilder;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @author Hongwen Zang
 * @author Celine Souchet
 */
public interface BPMInstanceBuilders {

    SAutomaticTaskInstanceBuilder getSAutomaticTaskInstanceBuilder();

    SAAutomaticTaskInstanceBuilder getSAAutomaticTaskInstanceBuilder();

    SReceiveTaskInstanceBuilder getSReceiveTaskInstanceBuilder();

    SAReceiveTaskInstanceBuilder getSAReceiveTaskInstanceBuilder();

    SASendTaskInstanceBuilder getSASendTaskInstanceBuilder();

    SSendTaskInstanceBuilder getSSendTaskInstanceBuilder();

    // SASendTaskInstanceBuilder getSASendTaskInstanceBuilder();

    SUserTaskInstanceBuilder getSUserTaskInstanceBuilder();

    SManualTaskInstanceBuilder getSManualTaskInstanceBuilder();

    SCallActivityInstanceBuilder getSCallActivityInstanceBuilder();

    SACallActivityInstanceBuilder getSACallActivityInstanceBuilder();

    SSubProcessActivityInstanceBuilder getSSubProcessActivityInstanceBuilder();

    SASubProcessActivityInstanceBuilder getSASubProcessActivityInstanceBuilder();

    SAUserTaskInstanceBuilder getSAUserTaskInstanceBuilder();

    SAManualTaskInstanceBuilder getSAManualTaskInstanceBuilder();

    SProcessInstanceBuilder getSProcessInstanceBuilder();

    STokenBuilder getSTokenBuilder();

    SAProcessInstanceBuilder getSAProcessInstanceBuilder();

    SFlowNodeInstanceLogBuilder getActivityInstanceLogBuilder();

    SALoopActivityInstanceBuilder getSALoopActivitynstanceBuilder();

    SConnectorInstanceLogBuilder getConnectorInstanceLofBuilder();

    ProcessInstanceLogBuilder getProcessInstanceLogBuilder();

    GatewayInstanceLogBuilder getGatewayInstanceLogBuilder();

    SGatewayInstanceBuilder getSGatewayInstanceBuilder();

    /**
     * Get a builder for create new archive gateway instance
     * 
     * @return A builder for create new archive gateway instance
     * @since 6.0
     */
    SAGatewayInstanceBuilder getSAGatewayInstanceBuilder();

    STransitionInstanceLogBuilder getSTransitionInstanceLogBuilder();

    SConnectorInstanceBuilder getSConnectorInstanceBuilder();

    SStartEventInstanceBuilder getSStartEventInstanceBuilder();

    SEndEventInstanceBuilder getSEndEventInstanceBuilder();

    SIntermediateCatchEventInstanceBuilder getSIntermediateCatchEventInstanceBuilder();

    SBoundaryEventInstanceBuilder getSBoundaryEventInstanceBuilder();

    SIntermediateThrowEventInstanceBuilder getSIntermediateThrowEventInstanceBuilder();

    SAStartEventInstanceBuilder getSArchiveStartEventInstanceBuilder();

    SAEndEventInstanceBuilder getSArchiveEndEventInstanceBuilder();

    STimerEventTriggerInstanceBuilder getSTimerEventTriggerInstanceBuilder();

    SEventTriggerInstanceLogBuilder getSEventTriggerInstanceLogBuilder();

    SWaitingEventLogBuilder getSStartEventTriggerInstanceLogBuilder();

    SWaitingMessageEventBuilder getSWaitingMessageEventBuilder();

    SThrowMessageEventTriggerInstanceBuilder getSThrowMessageEventTriggerInstanceBuilder();

    SMessageInstanceBuilder getSMessageInstanceBuilder();

    SWaitingSignalEventBuilder getSWaitingSignalEventBuilder();

    SThrowSignalEventTriggerInstanceBuilder getSThrowSignalEventTriggerInstanceBuilder();

    SThrowErrorEventTriggerInstanceBuilder getSThrowErrorEventTriggerInstanceBuilder();

    SWaitingErrorEventBuilder getSWaitingErrorEventBuilder();

    SUserTaskInstanceBuilder getUserTaskInstanceBuilder();

    SPendingActivityMappingBuilder getSPendingActivityMappingBuilder();

    SPendingActivityMappingLogBuilder getPendingActivityMappingLogBuilder();

    SLoopActivityInstanceBuilder getSLoopActivityInstanceBuilder();

    SMultiInstanceActivityInstanceBuilder getSMultiInstanceActivityInstanceBuilder();

    SProcessInstanceUpdateBuilder getProcessInstanceUpdateBuilder();

    /**
     * Get a builder for create new archive transition instance
     * 
     * @return A builder for create new archive transition instance
     * @since 6.0
     */
    SATransitionInstanceBuilder getSATransitionInstanceBuilder();

    SHiddenTaskInstanceBuilder getSHiddenTaskInstanceBuilder();

    SHiddenTaskInstanceLogBuilder getHiddenTaskInstanceLogBuilder();

    SAConnectorInstanceBuilder getSAConnectorInstanceBuilder();

    SAMultiInstanceActivityInstanceBuilder getSAMultiInstanceActivitynstanceBuilder();

}
