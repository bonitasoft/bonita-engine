/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General License for more details.
 * You should have received a copy of the GNU Lesser General License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.bpm.process.impl;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Matthieu Chaffotte
 */
public interface FlowElementBuilder {

    DataDefinitionBuilder addIntegerData(String name, Expression defaultValue);

    DataDefinitionBuilder addLongData(String name, Expression defaultValue);

    DataDefinitionBuilder addShortTextData(String name, Expression defaultValue);

    TextDataDefinitionBuilder addLongTextData(String name, Expression defaultValue);

    DataDefinitionBuilder addDoubleData(String name, Expression defaultValue);

    DataDefinitionBuilder addFloatData(String name, Expression defaultValue);

    DataDefinitionBuilder addDateData(String name, Expression defaultValue);

    XMLDataDefinitionBuilder addXMLData(String name, Expression defaultValue);

    DataDefinitionBuilder addBlobData(String name, Expression defaultValue);

    DataDefinitionBuilder addBooleanData(String name, Expression defaultValue);

    DataDefinitionBuilder addData(String name, String className, Expression defaultValue);

    /**
     * Add a connector on this element
     * <p>
     * Must also add connector dependencies and connector implementation descriptor
     * <p>
     * 
     * @see BusinessArchiveBuilder#addClasspathResource(org.bonitasoft.engine.bpm.bar.BarResource)
     * @see BusinessArchiveBuilder#addConnectorImplementation(org.bonitasoft.engine.bpm.bar.BarResource)
     * @param name
     *            name the connector instance will use
     * @param connectorId
     *            id of the Connector definition (connector implementation added in {@link BusinessArchive} must match this id in definitionId
     * @param version
     *            version of the Connector definition
     * @param activationEvent
     *            {@link ConnectorEvent} on which the connector will be triggered
     * @return
     */
    ConnectorDefinitionBuilder addConnector(String name, String connectorId, String version, ConnectorEvent activationEvent);

    UserTaskDefinitionBuilder addUserTask(String activityName, String actorName);

    AutomaticTaskDefinitionBuilder addAutomaticTask(String activityName);

    ReceiveTaskDefinitionBuilder addReceiveTask(String activityName, String messageName);

    SendTaskDefinitionBuilder addSendTask(String taskName, String messageName, Expression targetProcess);

    ManualTaskDefinitionBuilder addManualTask(String name, String actorName);

    TransitionDefinitionBuilder addTransition(String source, String target);

    TransitionDefinitionBuilder addTransition(String source, String target, Expression expression);

    GatewayDefinitionBuilder addGateway(String name, GatewayType gatewayType);

    StartEventDefinitionBuilder addStartEvent(String name);

    EndEventDefinitionBuilder addEndEvent(String name);

    IntermediateCatchEventDefinitionBuilder addIntermediateCatchEvent(String name);

    IntermediateThrowEventDefinitionBuilder addIntermediateThrowEvent(String name);

    CallActivityBuilder addCallActivity(String name, Expression callableElement, Expression callableElementVersion);

    SubProcessActivityDefinitionBuilder addSubProcess(String name, boolean triggeredByEvent);

    TransitionDefinitionBuilder addDefaultTransition(String source, String target);

}
