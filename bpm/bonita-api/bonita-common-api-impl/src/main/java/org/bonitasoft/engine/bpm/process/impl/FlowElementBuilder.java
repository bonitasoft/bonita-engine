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

import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.flownode.AutomaticTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.CallActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.EndEventDefinition;
import org.bonitasoft.engine.bpm.flownode.GatewayDefinition;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.flownode.IntermediateCatchEventDefinition;
import org.bonitasoft.engine.bpm.flownode.IntermediateThrowEventDefinition;
import org.bonitasoft.engine.bpm.flownode.ManualTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.ReceiveTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.SendTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.StartEventDefinition;
import org.bonitasoft.engine.bpm.flownode.UserTaskDefinition;
import org.bonitasoft.engine.bpm.process.SubProcessDefinition;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Matthieu Chaffotte
 */
public interface FlowElementBuilder {

    /**
     * Adds a integer data on this element
     * 
     * @param name
     *            data name
     * @param defaultValue
     *            expression representing the default value
     * @return
     */
    DataDefinitionBuilder addIntegerData(String name, Expression defaultValue);

    /**
     * Adds a long data on this element
     * 
     * @param name
     *            data name
     * @param defaultValue
     *            expression representing the default value
     * @return
     */
    DataDefinitionBuilder addLongData(String name, Expression defaultValue);

    /**
     * Adds a short text data (up to 255 characters) on this element
     * 
     * @param name
     *            data name
     * @param defaultValue
     *            expression representing the default value
     * @return
     */
    DataDefinitionBuilder addShortTextData(String name, Expression defaultValue);

    /**
     * Adds a long text data (more than 255 characters) on this element
     * 
     * @param name
     *            data name
     * @param defaultValue
     *            expression representing the default value
     * @return
     */
    TextDataDefinitionBuilder addLongTextData(String name, Expression defaultValue);

    /**
     * Adds a double data on this element
     * 
     * @param name
     *            data name
     * @param defaultValue
     *            expression representing the default value
     * @return
     */
    DataDefinitionBuilder addDoubleData(String name, Expression defaultValue);

    /**
     * Adds a float data on this element
     * 
     * @param name
     *            data name
     * @param defaultValue
     *            expression representing the default value
     * @return
     */
    DataDefinitionBuilder addFloatData(String name, Expression defaultValue);

    /**
     * Adds a date data on this element
     * 
     * @param name
     *            data name
     * @param defaultValue
     *            expression representing the default value
     * @return
     */
    DataDefinitionBuilder addDateData(String name, Expression defaultValue);

    /**
     * Adds a XML data on this element
     * 
     * @param name
     *            data name
     * @param defaultValue
     *            expression representing the default value
     * @return
     */
    XMLDataDefinitionBuilder addXMLData(String name, Expression defaultValue);

    /**
     * Adds a blob data on this element
     * 
     * @param name
     *            data name
     * @param defaultValue
     *            expression representing the default value
     * @return
     */
    DataDefinitionBuilder addBlobData(String name, Expression defaultValue);

    /**
     * Adds a boolean data on this element
     * 
     * @param name
     *            data name
     * @param defaultValue
     *            expression representing the default value
     * @return
     */
    DataDefinitionBuilder addBooleanData(String name, Expression defaultValue);

    /**
     * Adds a data on this element
     * 
     * @param name
     *            data name
     * @param className
     *            data type class name (i.e. java.lang.String)
     * @param defaultValue
     *            expression representing the default value
     * @return
     */
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

    /**
     * Adds an {@link UserTaskDefinition} on this element
     * 
     * @param taskName
     *            task name
     * @param actorName
     *            name of the actor that will perform this task
     * @return
     */
    UserTaskDefinitionBuilder addUserTask(String taskName, String actorName);

    /**
     * Adds {@link AutomaticTaskDefinition}
     * 
     * @param taskName
     *            task name
     * @return
     */
    AutomaticTaskDefinitionBuilder addAutomaticTask(String taskName);

    /**
     * Adds a {@link ReceiveTaskDefinition} on this element
     * 
     * @param taskName
     *            task name
     * @param messageName
     *            name of message to be received
     * @return
     */
    ReceiveTaskDefinitionBuilder addReceiveTask(String taskName, String messageName);

    /**
     * Adds a {@link SendTaskDefinition} on this element
     * 
     * @param taskName
     *            task name
     * @param messageName
     *            name of message to be sent
     * @param targetProcess
     *            expression representing the target process name
     * @return
     */
    SendTaskDefinitionBuilder addSendTask(String taskName, String messageName, Expression targetProcess);

    /**
     * Adds a {@link ManualTaskDefinition} on this element
     * 
     * @param name
     *            task name
     * @param actorName
     *            name of the actor that will perform this task
     * @return
     */
    ManualTaskDefinitionBuilder addManualTask(String name, String actorName);

    /**
     * Adds a transition on this element. Source and target elements must be previously added
     * 
     * @param source
     *            source element name
     * @param target
     *            target element name
     * @return
     */
    TransitionDefinitionBuilder addTransition(String source, String target);

    /**
     * Adds a conditional transition on this element. Source and target elements must be previously added
     * 
     * @param source
     *            source element name
     * @param target
     *            target element name
     * @param condition
     *            transition condition. The transition is taken if and only if the expression is evaluated to {@link Boolean#TRUE}.
     * @return
     */
    TransitionDefinitionBuilder addTransition(String source, String target, Expression condition);

    /**
     * Adds a {@link GatewayDefinition} on this element
     * 
     * @param name
     *            gateway name
     * @param gatewayType
     *            gateway type
     * @return
     */
    GatewayDefinitionBuilder addGateway(String name, GatewayType gatewayType);

    /**
     * Adds a {@link StartEventDefinition} on this element
     * 
     * @param name
     *            start event name
     * @return
     */
    StartEventDefinitionBuilder addStartEvent(String name);

    /**
     * Adds an {@link EndEventDefinition} on this element
     * 
     * @param name
     *            end event name
     * @return
     */
    EndEventDefinitionBuilder addEndEvent(String name);

    /**
     * Adds an {@link IntermediateCatchEventDefinition} on this element
     * 
     * @param name
     *            intermediate catch event name
     * @return
     */
    IntermediateCatchEventDefinitionBuilder addIntermediateCatchEvent(String name);

    /**
     * Adds an {@link IntermediateThrowEventDefinition} on this element
     * 
     * @param name
     *            the intermediate throw event name
     * @return
     */
    IntermediateThrowEventDefinitionBuilder addIntermediateThrowEvent(String name);

    /**
     * Adds a {@link CallActivityDefinition} on this element
     * 
     * @param name
     *            the call activity name
     * @param callableElement
     *            expression representing the name of process to be called
     * @param callableElementVersion
     *            expression representing the version of process to be called
     * @return
     */
    CallActivityBuilder addCallActivity(String name, Expression callableElement, Expression callableElementVersion);

    /**
     * Adds a {@link SubProcessDefinition} on this element
     * 
     * @param name
     *            the sub process name
     * @param triggeredByEvent
     *            true if it's an event sub-process; false if it's a subprocess
     * @return
     */
    SubProcessActivityDefinitionBuilder addSubProcess(String name, boolean triggeredByEvent);

    /**
     * Adds a default transition on this element
     * 
     * @param source
     *            source element name
     * @param target
     *            target element name
     * @return
     */
    TransitionDefinitionBuilder addDefaultTransition(String source, String target);

}
