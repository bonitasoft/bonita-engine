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

import java.io.Serializable;
import java.util.Date;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Matthieu Chaffotte
 */
public class FlowElementContainerBuilder implements FlowElementBuilder {

    private final FlowElementContainerDefinitionImpl container;

    private final ProcessDefinitionBuilder processDefinitionBuilder;

    public FlowElementContainerBuilder(final FlowElementContainerDefinitionImpl container, final ProcessDefinitionBuilder processDefinitionBuilder) {
        super();
        this.container = container;
        this.processDefinitionBuilder = processDefinitionBuilder;
    }

    /**
     * Validates the process consistency and return it
     * 
     * @return
     *         the process being build
     * @throws InvalidProcessDefinitionException
     *             when the process definition is inconsistent. The exception contains causes
     */
    public DesignProcessDefinition getProcess() throws InvalidProcessDefinitionException {
        return processDefinitionBuilder.done();
    }

    @Override
    public UserTaskDefinitionBuilder addUserTask(final String taskName, final String actorName) {
        return new UserTaskDefinitionBuilder(processDefinitionBuilder, container, taskName, actorName);
    }

    @Override
    public AutomaticTaskDefinitionBuilder addAutomaticTask(final String taskName) {
        return new AutomaticTaskDefinitionBuilder(processDefinitionBuilder, container, taskName);
    }

    @Override
    public ReceiveTaskDefinitionBuilder addReceiveTask(final String activityName, final String messageName) {
        return new ReceiveTaskDefinitionBuilder(processDefinitionBuilder, container, activityName, messageName);
    }

    @Override
    public SendTaskDefinitionBuilder addSendTask(final String taskName, final String messageName, final Expression targetProcess) {
        return new SendTaskDefinitionBuilder(processDefinitionBuilder, container, taskName, messageName, targetProcess);
    }

    @Override
    public ManualTaskDefinitionBuilder addManualTask(final String name, final String actorName) {
        return new ManualTaskDefinitionBuilder(processDefinitionBuilder, container, name, actorName);
    }

    @Override
    public TransitionDefinitionBuilder addTransition(final String source, final String target) {
        return new TransitionDefinitionBuilder(processDefinitionBuilder, container, source, target, false);
    }

    @Override
    public TransitionDefinitionBuilder addTransition(final String source, final String target, final Expression condition) {
        return new TransitionDefinitionBuilder(processDefinitionBuilder, container, source, target, condition, false);
    }

    @Override
    public TransitionDefinitionBuilder addDefaultTransition(final String source, final String target) {
        return new TransitionDefinitionBuilder(processDefinitionBuilder, container, source, target, null, true);
    }

    @Override
    public GatewayDefinitionBuilder addGateway(final String name, final GatewayType gatewayType) {
        return new GatewayDefinitionBuilder(processDefinitionBuilder, container, name, gatewayType);
    }

    @Override
    public StartEventDefinitionBuilder addStartEvent(final String name) {
        return new StartEventDefinitionBuilder(processDefinitionBuilder, container, name);
    }

    @Override
    public EndEventDefinitionBuilder addEndEvent(final String name) {
        return new EndEventDefinitionBuilder(processDefinitionBuilder, container, name);
    }

    @Override
    public IntermediateCatchEventDefinitionBuilder addIntermediateCatchEvent(final String name) {
        return new IntermediateCatchEventDefinitionBuilder(processDefinitionBuilder, container, name);
    }

    @Override
    public IntermediateThrowEventDefinitionBuilder addIntermediateThrowEvent(final String name) {
        return new IntermediateThrowEventDefinitionBuilder(processDefinitionBuilder, container, name);
    }

    @Override
    public CallActivityBuilder addCallActivity(final String name, final Expression callableElement, final Expression callableElementVersion) {
        return new CallActivityBuilder(processDefinitionBuilder, container, name, callableElement, callableElementVersion);
    }

    @Override
    public SubProcessActivityDefinitionBuilder addSubProcess(final String name, final boolean triggeredByEvent) {
        return new SubProcessActivityDefinitionBuilder(processDefinitionBuilder, container, name, triggeredByEvent);
    }

    @Override
    public ConnectorDefinitionBuilder addConnector(final String name, final String connectorId, final String version, final ConnectorEvent activationEvent) {
        return new ConnectorDefinitionBuilder(processDefinitionBuilder, container, name, connectorId, version, activationEvent);
    }

    @Override
    public DataDefinitionBuilder addIntegerData(final String name, final Expression defaultValue) {
        final String className = Integer.class.getName();
        return new DataDefinitionBuilder(processDefinitionBuilder, container, name, className, defaultValue);
    }

    @Override
    public DataDefinitionBuilder addLongData(final String name, final Expression defaultValue) {
        final String className = Long.class.getName();
        return new DataDefinitionBuilder(processDefinitionBuilder, container, name, className, defaultValue);
    }

    @Override
    public DataDefinitionBuilder addShortTextData(final String name, final Expression defaultValue) {
        final String className = String.class.getName();
        return new TextDataDefinitionBuilder(processDefinitionBuilder, container, name, className, defaultValue);
    }

    @Override
    public TextDataDefinitionBuilder addLongTextData(final String name, final Expression defaultValue) {
        final String className = String.class.getName();
        return new TextDataDefinitionBuilder(processDefinitionBuilder, container, name, className, defaultValue).isLongText();
    }

    @Override
    public DataDefinitionBuilder addDoubleData(final String name, final Expression defaultValue) {
        final String className = Double.class.getName();
        return new DataDefinitionBuilder(processDefinitionBuilder, container, name, className, defaultValue);
    }

    @Override
    public DataDefinitionBuilder addFloatData(final String name, final Expression defaultValue) {
        final String className = Float.class.getName();
        return new DataDefinitionBuilder(processDefinitionBuilder, container, name, className, defaultValue);
    }

    @Override
    public DataDefinitionBuilder addDateData(final String name, final Expression defaultValue) {
        final String className = Date.class.getName();
        return new DataDefinitionBuilder(processDefinitionBuilder, container, name, className, defaultValue);
    }

    @Override
    public XMLDataDefinitionBuilder addXMLData(final String name, final Expression defaultValue) {
        final String className = String.class.getName();
        return new XMLDataDefinitionBuilder(processDefinitionBuilder, container, name, className, defaultValue);
    }

    @Override
    public DataDefinitionBuilder addBlobData(final String name, final Expression defaultValue) {
        final String className = Serializable.class.getName();
        return new DataDefinitionBuilder(processDefinitionBuilder, container, name, className, defaultValue);
    }

    @Override
    public DataDefinitionBuilder addBooleanData(final String name, final Expression defaultValue) {
        final String className = Boolean.class.getName();
        return new DataDefinitionBuilder(processDefinitionBuilder, container, name, className, defaultValue);
    }

    @Override
    public DataDefinitionBuilder addData(final String name, final String className, final Expression defaultValue) {
        return new DataDefinitionBuilder(processDefinitionBuilder, container, name, className, defaultValue);
    }


    protected FlowElementContainerDefinitionImpl getContainer() {
        return container;
    }

    protected ProcessDefinitionBuilder getProcessBuilder() {
        return processDefinitionBuilder;
    }

}
