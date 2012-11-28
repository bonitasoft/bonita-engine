/*
 * Copyright (C) 2011-2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package org.bonitasoft.engine.bpm.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.bpm.model.event.BoundaryEventDefinition;
import org.bonitasoft.engine.bpm.model.event.EndEventDefinition;
import org.bonitasoft.engine.bpm.model.event.StartEventDefinition;
import org.bonitasoft.engine.bpm.model.impl.DesignProcessDefinitionImpl;
import org.bonitasoft.engine.bpm.model.impl.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.exception.InvalidProcessDefinitionException;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Baptiste Mesta
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public final class ProcessDefinitionBuilder implements DescriptionBuilder, ContainerBuilder {

    private final static String DOUBLE_HYPHEN = "--";

    private DesignProcessDefinitionImpl process;

    private List<String> designErrors;

    public ProcessDefinitionBuilder createNewInstance(final String name, final String version) {
        designErrors = new ArrayList<String>(5);
        if (name == null || name.isEmpty()) {
            designErrors.add("name of the process is null or empty");
        }
        process = new DesignProcessDefinitionImpl(name, version);
        process.setProcessContainer(new FlowElementContainerDefinitionImpl());
        return this;
    }

    public DesignProcessDefinition done() throws InvalidProcessDefinitionException {
        validateProcess();
        if (designErrors.isEmpty()) {
            return process;
        } else {
            throw new InvalidProcessDefinitionException(designErrors);
        }
    }

    private void validateProcess() {
        validateProcessAttributes();
        validateGateways();
        validateDocuments();
        validateMultiInstances();
        validateEvents();
        validateEventsSubProcess();
        validateActivities();
    }

    private void validateProcessAttributes() {
        if (process.getName() != null && process.getName().indexOf(DOUBLE_HYPHEN) != -1) {
            addError("'" + DOUBLE_HYPHEN + "' is fordidden in the process name");
        }
    }

    private void validateActivities() {
        final FlowElementContainerDefinition processContainer = process.getProcessContainer();
        for (final ActivityDefinition activity : processContainer.getActivities()) {
            if (activity instanceof CallActivityDefinition) {
                if (((CallActivityDefinition) activity).getCallableElement() == null) {
                    addError("The call activity " + activity.getName() + "has a null callable element");
                }
            }
        }

    }

    private void validateGateways() {
        final FlowElementContainerDefinition processContainer = process.getProcessContainer();
        for (final GatewayDefinition gateway : processContainer.getGateways()) {
            for (final TransitionDefinition transition : gateway.getOutgoingTransitions()) {
                switch (gateway.getGatewayType()) {
                    case PARALLEL:
                        if (transition.getCondition() != null) {
                            designErrors.add("The parallel gateway can't have conditional outgoing transitions: " + gateway);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * 
     */
    private void validateEventsSubProcess() {
        final FlowElementContainerDefinition processContainer = process.getProcessContainer();
        for (final ActivityDefinition activity : processContainer.getActivities()) {
            if (activity instanceof SubProcessDefinition) {
                if (((SubProcessDefinition) activity).isTriggeredByEvent()) {
                    final FlowElementContainerDefinition subProcessContainer = ((SubProcessDefinition) activity).getSubProcessContainer();
                    boolean haveStartEvent = false;
                    for (final StartEventDefinition element : subProcessContainer.getStartEvents()) {
                        if (!element.getEventTriggers().isEmpty()) {
                            haveStartEvent = true;
                            break;
                        }
                    }
                    if (!haveStartEvent) {
                        designErrors.add("the event sub process have no start event with a not NONE trigger: " + activity);
                    }
                }
            }
        }
    }

    /**
     * 
     */
    private void validateEvents() {
        final FlowElementContainerDefinition processContainer = process.getProcessContainer();
        for (final StartEventDefinition startEvent : processContainer.getStartEvents()) {
            if (!startEvent.getIncomingTransitions().isEmpty()) {
                designErrors.add("A start event can't have incomming transitions: on start event" + startEvent);
            }
        }
        for (final EndEventDefinition endEvent : processContainer.getEndEvents()) {
            if (!endEvent.getOutgoingTransitions().isEmpty()) {
                designErrors.add("An end event can't have outgoing transitions: on end event" + endEvent);
            }
        }
        validateBoundaryEvents();
    }

    private void validateMultiInstances() {
        final FlowElementContainerDefinition processContainer = process.getProcessContainer();
        for (final ActivityDefinition activity : processContainer.getActivities()) {
            if (activity.getLoopCharacteristics() != null && activity.getLoopCharacteristics() instanceof MultiInstanceLoopCharacteristics) {
                final MultiInstanceLoopCharacteristics loopCharacteristics = (MultiInstanceLoopCharacteristics) activity.getLoopCharacteristics();
                if (loopCharacteristics.getLoopDataInputRef() != null && !loopCharacteristics.getLoopDataInputRef().isEmpty()
                        && (loopCharacteristics.getLoopDataInputRef() == null || loopCharacteristics.getLoopDataInputRef().isEmpty())) {
                    designErrors.add("The multi instance havs got a data input reference but has not got a loop data input: on activity" + activity.getName());
                }
                if (loopCharacteristics.getDataOutputItemRef() != null && !loopCharacteristics.getDataOutputItemRef().isEmpty()
                        && (loopCharacteristics.getLoopDataOutputRef() == null || loopCharacteristics.getLoopDataOutputRef().isEmpty())) {
                    designErrors.add("The multi instance has got a data output reference but has not got a loop data output: on activity" + activity.getName());
                }
                // TODO add validation on data existence
            }
        }
    }

    private void validateDocuments() {
        final FlowElementContainerDefinition processContainer = process.getProcessContainer();
        for (final DocumentDefinition document : processContainer.getDocumentDefinitions()) {
            if (document.getFileName() == null || document.getFileName().isEmpty()) {
                designErrors.add("the document has not got a file name:" + document.getName());
            }
        }
    }

    private void validateBoundaryEvents() {
        final FlowElementContainerDefinition processContainer = process.getProcessContainer();
        for (final ActivityDefinition activity : processContainer.getActivities()) {
            for (final BoundaryEventDefinition boundaryEvent : activity.getBoundaryEventDefinitions()) {
                if (boundaryEvent.getOutgoingTransitions().isEmpty()) {
                    designErrors.add("A boundary event must have outgoing transitions: " + boundaryEvent.getName());
                } else {
                    validateBoundaryOutgoingTransitions(boundaryEvent);
                }
                if (!boundaryEvent.getIncomingTransitions().isEmpty()) {
                    designErrors.add("A boundary event must not have incoming transitions: " + boundaryEvent.getName());
                }
                if (boundaryEvent.getEventTriggers().isEmpty()) {
                    designErrors.add("A boundary event must have a trigger (it cannot be a NONE event): " + boundaryEvent.getName());
                }
            }
        }
    }

    private void validateBoundaryOutgoingTransitions(final BoundaryEventDefinition boundaryEvent) {
        for (final TransitionDefinition transition : boundaryEvent.getOutgoingTransitions()) {
            if (transition.getCondition() != null) {
                designErrors.add("A boundary event must have got inconditional transitions: " + transition.getSource() + "->" + transition.getTarget());
            }
        }
    }

    void addError(final String error) {
        designErrors.add(error);
    }

    @Override
    public DocumentDefinitionBuilder addDocumentDefinition(final String name, final String fileName) {
        return new DocumentDefinitionBuilder(this, (FlowElementContainerDefinitionImpl) process.getProcessContainer(), name, fileName);
    }

    @Override
    public ProcessDefinitionBuilder addDescription(final String description) {
        process.setDescription(description);
        return this;
    }

    public ProcessDefinitionBuilder addDisplayName(final String name) {
        process.setDisplayName(name);
        return this;
    }

    public ProcessDefinitionBuilder addDisplayDescription(final String description) {
        process.setDisplayDescription(description);
        return this;
    }

    void checkExpression(final String context, final Expression e) {
        if (e != null && (e.getContent() == null || e.getContent().isEmpty())) {
            addError("the expression " + e + " in " + context + " has got a null or empty content");
        }
    }

    @Override
    public ConnectorDefinitionBuilder addConnector(final String name, final String connectorId, final String version, final ConnectorEvent activationEvent) {
        return new ConnectorDefinitionBuilder(this, (FlowElementContainerDefinitionImpl) process.getProcessContainer(), name, connectorId, version,
                activationEvent);
    }

    @Override
    public UserTaskDefinitionBuilder addUserTask(final String activityName, final String actorName) {
        return new UserTaskDefinitionBuilder(this, (FlowElementContainerDefinitionImpl) process.getProcessContainer(), activityName, actorName);
    }

    @Override
    public AutomaticTaskDefinitionBuilder addAutomaticTask(final String activityName) {
        return new AutomaticTaskDefinitionBuilder(this, (FlowElementContainerDefinitionImpl) process.getProcessContainer(), activityName);
    }

    @Override
    public ManualTaskDefinitionBuilder addManualTask(final String name, final String actorName) {
        return new ManualTaskDefinitionBuilder(this, (FlowElementContainerDefinitionImpl) process.getProcessContainer(), name, actorName);
    }

    @Override
    public TransitionDefinitionBuilder addTransition(final String source, final String target) {
        return new TransitionDefinitionBuilder(this, (FlowElementContainerDefinitionImpl) process.getProcessContainer(), source, target, false);
    }

    @Override
    public TransitionDefinitionBuilder addTransition(final String source, final String target, final Expression expression) {
        return new TransitionDefinitionBuilder(this, (FlowElementContainerDefinitionImpl) process.getProcessContainer(), source, target, expression, false);
    }

    @Override
    public GatewayDefinitionBuilder addGateway(final String name, final GatewayType gatewayType) {
        return new GatewayDefinitionBuilder(this, (FlowElementContainerDefinitionImpl) process.getProcessContainer(), name, gatewayType);
    }

    @Override
    public StartEventDefinitionBuilder addStartEvent(final String name) {
        return new StartEventDefinitionBuilder(this, (FlowElementContainerDefinitionImpl) process.getProcessContainer(), name);
    }

    @Override
    public EndEventDefinitionBuilder addEndEvent(final String name) {
        return new EndEventDefinitionBuilder(this, (FlowElementContainerDefinitionImpl) process.getProcessContainer(), name);
    }

    @Override
    public IntermediateCatchEventDefinitionBuilder addIntermediateCatchEvent(final String name) {
        return new IntermediateCatchEventDefinitionBuilder(this, (FlowElementContainerDefinitionImpl) process.getProcessContainer(), name);
    }

    @Override
    public IntermediateThrowEventDefinitionBuilder addIntermediateThrowEvent(final String name) {
        return new IntermediateThrowEventDefinitionBuilder(this, (FlowElementContainerDefinitionImpl) process.getProcessContainer(), name);
    }

    @Override
    public CallActivityBuilder addCallActivity(final String name, final Expression callableElement, final Expression callableElementVersion) {
        return new CallActivityBuilder(this, (FlowElementContainerDefinitionImpl) process.getProcessContainer(), name, callableElement, callableElementVersion);
    }

    @Override
    public SubProcessActivityDefinitionBuilder addSubProcess(final String name, final boolean triggeredByEvent) {
        return new SubProcessActivityDefinitionBuilder(this, (FlowElementContainerDefinitionImpl) process.getProcessContainer(), name, triggeredByEvent);
    }

    @Override
    public DataDefinitionBuilder addIntegerData(final String name, final Expression defaultValue) {
        final String className = Integer.class.getName();
        return new DataDefinitionBuilder(this, (FlowElementContainerDefinitionImpl) process.getProcessContainer(), name, className, defaultValue);
    }

    @Override
    public DataDefinitionBuilder addLongData(final String name, final Expression defaultValue) {
        final String className = Long.class.getName();
        return new DataDefinitionBuilder(this, (FlowElementContainerDefinitionImpl) process.getProcessContainer(), name, className, defaultValue);
    }

    @Override
    public DataDefinitionBuilder addStringData(final String name, final Expression defaultValue) {
        final String className = String.class.getName();
        return new DataDefinitionBuilder(this, (FlowElementContainerDefinitionImpl) process.getProcessContainer(), name, className, defaultValue);
    }

    @Override
    public TextDataDefinitionBuilder addTextData(final String name, final Expression defaultValue) {
        final String className = String.class.getName();
        return new TextDataDefinitionBuilder(this, (FlowElementContainerDefinitionImpl) process.getProcessContainer(), name, className, defaultValue);
    }

    @Override
    public DataDefinitionBuilder addDoubleData(final String name, final Expression defaultValue) {
        final String className = Double.class.getName();
        return new DataDefinitionBuilder(this, (FlowElementContainerDefinitionImpl) process.getProcessContainer(), name, className, defaultValue);
    }

    @Override
    public DataDefinitionBuilder addDateData(final String name, final Expression defaultValue) {
        final String className = Date.class.getName();
        return new DataDefinitionBuilder(this, (FlowElementContainerDefinitionImpl) process.getProcessContainer(), name, className, defaultValue);
    }

    @Override
    public XMLDataDefinitionBuilder addXMLData(final String name, final Expression defaultValue) {
        final String className = String.class.getName();
        return new XMLDataDefinitionBuilder(this, (FlowElementContainerDefinitionImpl) process.getProcessContainer(), name, className, defaultValue);
    }

    @Override
    public DataDefinitionBuilder addBlobData(final String name, final Expression defaultValue) {
        final String className = Serializable.class.getName();
        return new DataDefinitionBuilder(this, (FlowElementContainerDefinitionImpl) process.getProcessContainer(), name, className, defaultValue);
    }

    @Override
    public DataDefinitionBuilder addBooleanData(final String name, final Expression defaultValue) {
        final String className = Boolean.class.getName();
        return new DataDefinitionBuilder(this, (FlowElementContainerDefinitionImpl) process.getProcessContainer(), name, className, defaultValue);
    }

    @Override
    public DataDefinitionBuilder addData(final String name, final String className, final Expression defaultValue) {
        return new DataDefinitionBuilder(this, (FlowElementContainerDefinitionImpl) process.getProcessContainer(), name, className, defaultValue);
    }

    public ParameterDefinitionBuilder addParameter(final String parameterName, final String type) {
        return new ParameterDefinitionBuilder(this, process, parameterName, type);
    }

    public ActorDefinitionBuilder addActor(final String actorName) {
        return new ActorDefinitionBuilder(this, process, actorName, false);
    }

    public ActorDefinitionBuilder addActor(final String name, final boolean initiator) {
        return new ActorDefinitionBuilder(this, process, name, initiator);
    }

    public ActorDefinitionBuilder setActorInitiator(final String actorName) {
        return new ActorDefinitionBuilder(this, process, actorName, true);
    }

    public TransitionDefinitionBuilder addDefaultTransition(final String source, final String target) {
        return new TransitionDefinitionBuilder(this, (FlowElementContainerDefinitionImpl) process.getProcessContainer(), source, target, null, true);
    }

    public DesignProcessDefinition getProcess() throws InvalidProcessDefinitionException {
        return done();
    }
}
