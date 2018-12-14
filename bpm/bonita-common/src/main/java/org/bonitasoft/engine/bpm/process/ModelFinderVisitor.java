/*
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 */
package org.bonitasoft.engine.bpm.process;

import org.bonitasoft.engine.bpm.businessdata.BusinessDataDefinition;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinition;
import org.bonitasoft.engine.bpm.context.ContextEntry;
import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.document.DocumentDefinition;
import org.bonitasoft.engine.bpm.document.DocumentListDefinition;
import org.bonitasoft.engine.bpm.flownode.ActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.CallActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.CatchEventDefinition;
import org.bonitasoft.engine.bpm.flownode.CatchMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.CorrelationDefinition;
import org.bonitasoft.engine.bpm.flownode.EventDefinition;
import org.bonitasoft.engine.bpm.flownode.FlowElementContainerDefinition;
import org.bonitasoft.engine.bpm.flownode.FlowNodeDefinition;
import org.bonitasoft.engine.bpm.flownode.HumanTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.MessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.MultiInstanceLoopCharacteristics;
import org.bonitasoft.engine.bpm.flownode.ReceiveTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.SendTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.StandardLoopCharacteristics;
import org.bonitasoft.engine.bpm.flownode.ThrowEventDefinition;
import org.bonitasoft.engine.bpm.flownode.ThrowMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.TransitionDefinition;
import org.bonitasoft.engine.bpm.flownode.UserTaskDefinition;
import org.bonitasoft.engine.bpm.userfilter.UserFilterDefinition;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.operation.Operation;

/**
 * author Emmanuel Duchastenier
 */
public interface ModelFinderVisitor {

    void find(Expression expression, long modelId);

    void find(final DesignProcessDefinition designProcessDefinition, final long modelId);

    void find(final FlowNodeDefinition flowNodeDefinition, final long modelId);

    void find(FlowElementContainerDefinition flowElementContainerDefinition, long modelId);

    void find(ActivityDefinition activityDefinition, long modelId);

    void find(HumanTaskDefinition humanTaskDefinition, long modelId);

    void find(UserFilterDefinition userFilterDefinition, long modelId);

    void find(UserTaskDefinition userTaskDefinition, long modelId);

    void find(SendTaskDefinition sendTaskDefinition, long modelId);

    void find(ReceiveTaskDefinition receiveTaskDefinition, long modelId);

    void find(SubProcessDefinition subProcessDefinition, long modelId);

    void find(CallActivityDefinition callActivityDefinition, long modelId);

    void find(Operation operation, long modelId);

    void find(TransitionDefinition transition, long modelId);

    void find(StandardLoopCharacteristics standardLoopCharacteristics, long expressionDefinitionId);

    void find(MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics, long expressionDefinitionId);

    void find(BusinessDataDefinition businessDataDefinition, long modelId);

    void find(DataDefinition dataDefinition, long modelId);

    void find(CorrelationDefinition correlationDefinition, long modelId);

    void find(CatchMessageEventTriggerDefinition catchMessageEventTriggerDefinition, long modelId);

    void find(ThrowMessageEventTriggerDefinition throwMessageEventTriggerDefinition, long modelId);

    void find(MessageEventTriggerDefinition messageEventTriggerDefinition, long modelId);

    void find(TimerEventTriggerDefinition timerEventTriggerDefinition, long modelId);

    void find(ContextEntry contextEntry, long modelId);

    void find(EventDefinition eventDefinition, long modelId);

    void find(ThrowEventDefinition throwEventDefinition, long modelId);

    void find(CatchEventDefinition catchEventDefinition, long modelId);

    void find(DocumentDefinition documentDefinition, long modelId);

    void find(DocumentListDefinition documentListDefinition, long modelId);

    void find(ConnectorDefinition connectorDefinition, long modelId);
}
