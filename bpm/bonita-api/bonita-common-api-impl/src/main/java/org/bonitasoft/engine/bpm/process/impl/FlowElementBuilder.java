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
package org.bonitasoft.engine.bpm.process.impl;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @version 6.3.5
 * @since 6.0.0
 */
public interface FlowElementBuilder {

    /**
     * Adds a integer data on this element
     *
     * @param name
     *        The data name
     * @param defaultValue
     *        The expression representing the default value
     * @return
     */
    DataDefinitionBuilder addIntegerData(String name, Expression defaultValue);

    /**
     * Adds a long data on this element
     *
     * @param name
     *        The data name
     * @param defaultValue
     *        The expression representing the default value
     * @return
     */
    DataDefinitionBuilder addLongData(String name, Expression defaultValue);

    /**
     * Adds a short text data (up to 255 characters) on this element
     *
     * @param name
     *        The data name
     * @param defaultValue
     *        The expression representing the default value
     * @return
     */
    DataDefinitionBuilder addShortTextData(String name, Expression defaultValue);

    /**
     * Adds a long text data (more than 255 characters) on this element
     *
     * @param name
     *        The data name
     * @param defaultValue
     *        The expression representing the default value
     * @return
     */
    TextDataDefinitionBuilder addLongTextData(String name, Expression defaultValue);

    /**
     * Adds a double data on this element
     *
     * @param name
     *        The data name
     * @param defaultValue
     *        The expression representing the default value
     * @return
     */
    DataDefinitionBuilder addDoubleData(String name, Expression defaultValue);

    /**
     * Adds a float data on this element
     *
     * @param name
     *        The data name
     * @param defaultValue
     *        The expression representing the default value
     * @return
     */
    DataDefinitionBuilder addFloatData(String name, Expression defaultValue);

    /**
     * Adds a date data on this element
     *
     * @param name
     *        The data name
     * @param defaultValue
     *        The expression representing the default value
     * @return
     */
    DataDefinitionBuilder addDateData(String name, Expression defaultValue);

    /**
     * Adds a XML data on this element
     *
     * @param name
     *        The data name
     * @param defaultValue
     *        The expression representing the default value
     * @return
     */
    XMLDataDefinitionBuilder addXMLData(String name, Expression defaultValue);

    /**
     * Adds a blob data on this element
     *
     * @param name
     *        The data name
     * @param defaultValue
     *        The expression representing the default value
     * @return
     */
    DataDefinitionBuilder addBlobData(String name, Expression defaultValue);

    /**
     * Adds a boolean data on this element
     *
     * @param name
     *        The data name
     * @param defaultValue
     *        The expression representing the default value
     * @return
     */
    DataDefinitionBuilder addBooleanData(String name, Expression defaultValue);

    /**
     * Adds a data on this element
     *
     * @param name
     *        The data name
     * @param className
     *        The data type class name (i.e. java.lang.String)
     * @param defaultValue
     *        The expression representing the default value
     * @return
     */
    DataDefinitionBuilder addData(String name, String className, Expression defaultValue);

    /**
     * Add a connector on this element
     * <p>
     * Must also add connector dependencies and connector implementation descriptor
     * <p>
     * 
     * @see org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder#addClasspathResource(org.bonitasoft.engine.bpm.bar.BarResource)
     * @see org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder#addConnectorImplementation(org.bonitasoft.engine.bpm.bar.BarResource)
     * @param name
     *        The name the connector instance will use
     * @param connectorId
     *        The id of the Connector definition (connector implementation added in {@link org.bonitasoft.engine.bpm.bar.BusinessArchive} must match this id in
     *        definitionId
     * @param version
     *        The version of the Connector definition
     * @param activationEvent
     *        {@link ConnectorEvent} on which the connector will be triggered
     * @return
     */
    ConnectorDefinitionBuilder addConnector(String name, String connectorId, String version, ConnectorEvent activationEvent);

    /**
     * Adds an {@link org.bonitasoft.engine.bpm.flownode.UserTaskDefinition} on this element
     *
     * @param taskName
     *        The task name
     * @param actorName
     *        The name of the actor that will perform this task
     * @return
     */
    UserTaskDefinitionBuilder addUserTask(String taskName, String actorName);

    /**
     * Adds {@link org.bonitasoft.engine.bpm.flownode.AutomaticTaskDefinition}
     *
     * @param taskName
     *        The task name
     * @return
     */
    AutomaticTaskDefinitionBuilder addAutomaticTask(String taskName);

    /**
     * Adds a {@link org.bonitasoft.engine.bpm.flownode.ReceiveTaskDefinition} on this element
     *
     * @param taskName
     *        The task name
     * @param messageName
     *        The name of message to be received
     * @return
     */
    ReceiveTaskDefinitionBuilder addReceiveTask(String taskName, String messageName);

    /**
     * Adds a {@link org.bonitasoft.engine.bpm.flownode.SendTaskDefinition} on this element
     *
     * @param taskName
     *        The task name
     * @param messageName
     *        The name of message to be sent
     * @param targetProcess
     *        The expression representing the target process name
     * @return
     */
    SendTaskDefinitionBuilder addSendTask(String taskName, String messageName, Expression targetProcess);

    /**
     * Adds a {@link org.bonitasoft.engine.bpm.flownode.ManualTaskDefinition} on this element
     *
     * @param name
     *        The task name
     * @param actorName
     *        The name of the actor that will perform this task
     * @return
     */
    ManualTaskDefinitionBuilder addManualTask(String name, String actorName);

    /**
     * Adds a transition on this element. Source and target elements must be previously added
     *
     * @param source
     *        The source element name
     * @param target
     *        The target element name
     * @return
     */
    TransitionDefinitionBuilder addTransition(String source, String target);

    /**
     * Adds a conditional transition on this element. Source and target elements must be previously added
     *
     * @param source
     *        The source element name
     * @param target
     *        The target element name
     * @param condition
     *        The transition condition. The transition is taken if and only if the expression is evaluated to {@link Boolean#TRUE}.
     * @return
     */
    TransitionDefinitionBuilder addTransition(String source, String target, Expression condition);

    /**
     * Adds a {@link org.bonitasoft.engine.bpm.flownode.GatewayDefinition} on this element
     *
     * @param name
     *        The gateway name
     * @param gatewayType
     *        The gateway type
     * @return
     */
    GatewayDefinitionBuilder addGateway(String name, GatewayType gatewayType);

    /**
     * Adds a {@link org.bonitasoft.engine.bpm.flownode.StartEventDefinition} on this element
     *
     * @param name
     *        The start event name
     * @return
     */
    StartEventDefinitionBuilder addStartEvent(String name);

    /**
     * Adds an {@link org.bonitasoft.engine.bpm.flownode.EndEventDefinition} on this element
     *
     * @param name
     *        The end event name
     * @return
     */
    EndEventDefinitionBuilder addEndEvent(String name);

    /**
     * Adds an {@link org.bonitasoft.engine.bpm.flownode.IntermediateCatchEventDefinition} on this element
     *
     * @param name
     *        The intermediate catch event name
     * @return
     */
    IntermediateCatchEventDefinitionBuilder addIntermediateCatchEvent(String name);

    /**
     * Adds an {@link org.bonitasoft.engine.bpm.flownode.IntermediateThrowEventDefinition} on this element
     *
     * @param name
     *        The intermediate throw event name
     * @return
     */
    IntermediateThrowEventDefinitionBuilder addIntermediateThrowEvent(String name);

    /**
     * Adds a {@link org.bonitasoft.engine.bpm.flownode.CallActivityDefinition} on this element
     *
     * @param name
     *        The call activity name
     * @param callableElement
     *        The expression representing the name of process to be called
     * @param callableElementVersion
     *        The expression representing the version of process to be called
     * @return
     */
    CallActivityBuilder addCallActivity(String name, Expression callableElement, Expression callableElementVersion);

    /**
     * Adds a {@link org.bonitasoft.engine.bpm.process.SubProcessDefinition} on this element
     *
     * @param name
     *        The sub process name
     * @param triggeredByEvent
     *        true if it's an event sub-process; false if it's a subprocess
     * @return
     */
    SubProcessActivityDefinitionBuilder addSubProcess(String name, boolean triggeredByEvent);

    /**
     * Adds a default transition on this element
     *
     * @param source
     *        The source element name
     * @param target
     *        The target element name
     * @return
     */
    TransitionDefinitionBuilder addDefaultTransition(String source, String target);

}
