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
 */
package org.bonitasoft.engine.bpm.bar.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bonitasoft.engine.bpm.actor.ActorDefinition;
import org.bonitasoft.engine.bpm.businessdata.BusinessDataDefinition;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinition;
import org.bonitasoft.engine.bpm.connector.FailAction;
import org.bonitasoft.engine.bpm.context.ContextEntry;
import org.bonitasoft.engine.bpm.contract.ConstraintDefinition;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.contract.InputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.data.TextDataDefinition;
import org.bonitasoft.engine.bpm.data.XMLDataDefinition;
import org.bonitasoft.engine.bpm.document.DocumentDefinition;
import org.bonitasoft.engine.bpm.document.DocumentListDefinition;
import org.bonitasoft.engine.bpm.flownode.ActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.AutomaticTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.BoundaryEventDefinition;
import org.bonitasoft.engine.bpm.flownode.CallActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.CatchErrorEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.CatchEventDefinition;
import org.bonitasoft.engine.bpm.flownode.CatchMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.CatchSignalEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.CorrelationDefinition;
import org.bonitasoft.engine.bpm.flownode.EndEventDefinition;
import org.bonitasoft.engine.bpm.flownode.FlowNodeDefinition;
import org.bonitasoft.engine.bpm.flownode.GatewayDefinition;
import org.bonitasoft.engine.bpm.flownode.IntermediateCatchEventDefinition;
import org.bonitasoft.engine.bpm.flownode.IntermediateThrowEventDefinition;
import org.bonitasoft.engine.bpm.flownode.LoopCharacteristics;
import org.bonitasoft.engine.bpm.flownode.ManualTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.ReceiveTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.SendTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.StartEventDefinition;
import org.bonitasoft.engine.bpm.flownode.TerminateEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.ThrowErrorEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.ThrowEventDefinition;
import org.bonitasoft.engine.bpm.flownode.ThrowMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.ThrowSignalEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.TransitionDefinition;
import org.bonitasoft.engine.bpm.flownode.UserTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.FlowElementContainerDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.HumanTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.internal.MultiInstanceLoopCharacteristics;
import org.bonitasoft.engine.bpm.flownode.impl.internal.StandardLoopCharacteristics;
import org.bonitasoft.engine.bpm.parameter.ParameterDefinition;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.SubProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.internal.SubProcessDefinitionImpl;
import org.bonitasoft.engine.bpm.userfilter.UserFilterDefinition;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.io.xml.XMLNode;
import org.bonitasoft.engine.operation.LeftOperand;
import org.bonitasoft.engine.operation.Operation;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class XMLProcessDefinition {

    public static final String CORRELATION_VALUE = "value";

    public static final String CORRELATION_KEY = "key";

    public static final String NAMESPACE = "http://www.bonitasoft.org/ns/process/client/6.3";

    public static final String PROCESS_NODE = "processDefinition";

    public static final String TRANSITIONS_NODE = "transitions";

    public static final String FLOW_ELEMENTS_NODE = "flowElements";

    public static final String FLOW_NODES_NODE = "flowNodes";

    public static final String CONNECTORS_NODE = "connectors";

    public static final String DATA_DEFINITIONS_NODE = "dataDefinitions";

    public static final String DOCUMENT_DEFINITIONS_NODE = "documentDefinitions";

    public static final String DOCUMENT_DEFINITION_NODE = "documentDefinition";

    public static final String DOCUMENT_LIST_DEFINITIONS_NODE = "documentListDefinitions";

    public static final String DOCUMENT_LIST_DEFINITION_NODE = "documentListDefinition";

    public static final String DOCUMENT_DEFINITION_URL = "url";

    public static final String DOCUMENT_DEFINITION_FILE = "file";

    public static final String DOCUMENT_DEFINITION_FILE_NAME = "fileName";

    public static final String DOCUMENT_DEFINITION_MIME_TYPE = "mimeType";

    public static final String OPERATIONS_NODE = "operations";

    public static final String DEPENDENCIES_NODE = "dependencies";

    public static final String ACTORS_NODE = "actors";

    public static final String PARAMETERS_NODE = "parameters";

    public static final String ACTOR_NODE = "actor";

    public static final String INITIATOR_NODE = "actorInitiator";

    public static final String CONNECTOR_NODE = "connector";

    public static final String USER_FILTER_NODE = "userFilter";

    public static final String DATA_DEFINITION_NODE = "dataDefinition";

    public static final String USER_TASK_NODE = "userTask";

    public static final String MANUAL_TASK_NODE = "manualTask";

    public static final String AUTOMATIC_TASK_NODE = "automaticTask";

    public static final String RECEIVE_TASK_NODE = "receiveTask";

    public static final String SEND_TASK_NODE = "sendTask";

    public static final String CALL_ACTIVITY_NODE = "callActivity";

    public static final String DATA_INPUT_OPERATION_NODE = "dataInputOperation";

    public static final String DATA_OUTPUT_OPERATION_NODE = "dataOutputOperation";

    public static final String CALLABLE_ELEMENT_NODE = "callableElement";

    public static final String CALLABLE_ELEMENT_VERSION_NODE = "callableElementVersion";

    public static final String CALLABLE_ELEMENT_TYPE = "callableElementType";

    public static final String GATEWAY_NODE = "gateway";

    public static final String PARAMETER_NODE = "parameter";

    public static final String START_EVENT_NODE = "startEvent";

    public static final String INTERMEDIATE_CATCH_EVENT_NODE = "intermediateCatchEvent";

    public static final String INTERMEDIATE_THROW_EVENT_NODE = "intermediateThrowEvent";

    public static final String END_EVENT_NODE = "endEvent";

    public static final String BOUNDARY_EVENTS_NODE = "boundaryEvents";

    public static final String BOUNDARY_EVENT_NODE = "boundaryEvent";

    public static final String INTERRUPTING = "interrupting";

    public static final String NAME = "name";

    public static final String ID = "id";

    public static final String IDREF = "idref";

    public static final String VERSION = "version";

    public static final String DESCRIPTION = "description";

    public static final String DISPLAY_NAME = "displayName";

    public static final String DISPLAY_DESCRIPTION = "displayDescription";

    public static final String STRING_INDEXES = "stringIndexes";

    public static final String STRING_INDEX = "stringIndex";

    public static final String BUSINESS_DATA_DEFINITION_NODE = "businessDataDefinition";

    public static final String BUSINESS_DATA_DEFINITIONS_NODE = "businessDataDefinitions";

    public static final String BUSINESS_DATA_DEFINITION_CLASS = "className";

    public static final String BUSINESS_DATA_DEFINITION_IS_MULTIPLE = "multiple";

    public static final String INDEX = "index";

    public static final String LABEL = "label";

    public static final String DISPLAY_DESCRIPTION_AFTER_COMPLETION = "displayDescriptionAfterCompletion";

    public static final String INCOMING_TRANSITION = "incomingTransition";

    public static final String OUTGOING_TRANSITION = "outgoingTransition";

    public static final String TRANSITION_NODE = "transition";

    public static final String TRANSITION_SOURCE = "source";

    public static final String TRANSITION_TARGET = "target";

    public static final String TRANSITION_CONDITION = "condition";

    public static final String ACTOR_NAME = "actorName";

    public static final String CONNECTOR_ID = "connectorId";

    public static final String CONNECTOR_ACTIVATION_EVENT = "activationEvent";

    public static final String CONNECTOR_FAIL_ACTION = "failAction";

    public static final String CONNECTOR_ERROR_CODE = "errorCode";

    public static final String CONNECTOR_INPUT = "input";

    public static final String CONNECTOR_INPUT_NAME = "inputName";

    public static final String USER_FILTER_ID = "userFilterId";

    public static final String DATA_DEFINITION_CLASS = "className";

    public static final String DATA_DEFINITION_TRANSIENT = "transient";

    public static final String EXPRESSION_NODE = "expression";

    public static final String EXPRESSION_TYPE = "expressionType";

    public static final String EXPRESSION_RETURN_TYPE = "returnType";

    public static final String EXPRESSION_INTERPRETER = "interpreter";

    public static final String EXPRESSION_CONTENT = "content";

    public static final String GATEWAY_TYPE = "gatewayType";

    public static final String DEFAULT_TRANSITION = "defaultTransition";

    public static final String PARAMETER_TYPE = "type";

    public static final String BOS_VERSION = "bos_version";

    private static final String BOS_CURRENT_VERSION = "6.0-SNAPSHOT";

    public static final String DEFAULT_VALUE_NODE = "defaultValue";

    public static final String TIMER_EVENT_TRIGGER_NODE = "timerEventTrigger";

    public static final String CATCH_MESSAGE_EVENT_TRIGGER_NODE = "catchMessageEventTrigger";

    public static final String THROW_MESSAGE_EVENT_TRIGGER_NODE = "throwMessageEventTrigger";

    public static final String CATCH_SIGNAL_EVENT_TRIGGER_NODE = "catchSignalEventTrigger";

    public static final String CATCH_ERROR_EVENT_TRIGGER_NODE = "catchErrorEventTrigger";

    public static final String THROW_SIGNAL_EVENT_TRIGGER_NODE = "throwSignalEventTrigger";

    public static final String THROW_ERROR_EVENT_TRIGGER_NODE = "throwErrorEventTrigger";

    public static final String TERMINATE_EVENT_TRIGGER_NODE = "terminateEventTrigger";

    public static final String TIMER_EVENT_TRIGGER_TIMER_TYPE = "type";

    public static final String ERROR_CODE = "errorCode";

    public static final String CORRELATION_NODE = "correlation";

    public static final String TARGET_PROCESS = "targetProcess";

    public static final String TARGET_FLOW_NODE = "targetFlowNode";

    public static final String OPERATION_NODE = "operation";

    public static final String OPERATION_RIGHT_OPERAND = "rightOperand";

    public static final String OPERATION_LEFT_OPERAND = "leftOperand";

    public static final String OPERATION_OPERATOR_TYPE = "operatorType";

    public static final String OPERATION_OPERATOR = "operator";

    public static final String LEFT_OPERAND_NAME = "name";

    public static final String LEFT_OPERAND_TYPE = "type";

    public static final String CONNECTOR_INPUTS_NODE = "inputs";

    public static final String CONNECTOR_OUTPUTS_NODE = "outputs";

    public static final String CONNECTOR_VERSION = "version";

    public static final String STANDARD_LOOP_CHARACTERISTICS_NODE = "standardLoopCharacteristics";

    public static final String LOOP_CONDITION = "loopCondition";

    public static final String TEST_BEFORE = "testBefore";

    public static final String LOOP_MAX = "loopMax";

    public static final String MULTI_INSTANCE_LOOP_CHARACTERISTICS_NODE = "multiInstanceLoopCharacteristics";

    public static final String MULTI_INSTANCE_IS_SEQUENTIAL = "isSequential";

    public static final String MULTI_INSTANCE_DATA_INPUT_ITEM_REF = "dataInputItemRef";

    public static final String MULTI_INSTANCE_DATA_OUTPUT_ITEM_REF = "dataOutputItemRef";

    public static final String MULTI_INSTANCE_LOOP_DATA_INPUT = "loopDataInputRef";

    public static final String MULTI_INSTANCE_LOOP_DATA_OUTPUT = "loopDataOutputRef";

    public static final String MULTI_INSTANCE_LOOP_CARDINALITY = "loopCardinality";

    public static final String MULTI_INSTANCE_COMPLETION_CONDITION = "completionCondition";

    public static final String PRIORITY = "priority";

    public static final String EXPECTED_DURATION = "expectedDuration";

    public static final String XML_DATA_DEFINITION_NAMESPACE = "namespace";

    public static final String XML_DATA_DEFINITION_ELEMENT = "element";

    public static final String XML_DATA_DEFINITION_NODE = "xmlDataDefinition";

    public static final String TEXT_DATA_DEFINITION_NODE = "textDataDefinition";

    public static final String TEXT_DATA_DEFINITION_LONG = "longText";

    public static final String SUB_PROCESS = "subProcess";

    public static final String TRIGGERED_BY_EVENT = "triggeredByEvent";

    public static final String CONTRACT_NODE = "contract";

    private static final String CONTRACT_INPUTS_NODE = "inputDefinitions";

    public static final String CONTRACT_INPUT_NODE = "inputDefinition";

    public static final String TYPE = "type";

    public static final String CONSTRAINT_TYPE = "type";

    public static final String MULTIPLE = "multiple";

    public static final String CONTRACT_CONSTRAINTS_NODE = "constraintDefinitions";

    public static final String CONTRACT_CONSTRAINT_NODE = "constraintDefinition";

    public static final String CONSTRAINT_EXPRESSION = "conditionalExpression";

    public static final String CONSTRAINT_EXPLANATION = "explanation";

    public static final String INPUT_NAMES = "inputDefinitionNames";

    public static final String INPUT_NAME = "inputDefinitionName";

    public static final String CONTEXT_NODE = "context";

    public static final String CONTEXT_ENTRY_NODE = "contextEntry";

    public static final String CONTEXT_ENTRY_KEY = "key";

    public Map<Object, String> objectToId = new HashMap<Object, String>();

    public static final class BEntry<K, V> implements Map.Entry<K, V> {

        private final K k;

        private V v;

        public BEntry(final K k, final V v) {
            this.k = k;
            this.v = v;
        }

        @Override
        public K getKey() {
            return k;
        }

        @Override
        public V getValue() {
            return v;
        }

        @Override
        public V setValue(final V value) {
            v = value;
            return v;
        }
    }

    public XMLNode getXMLProcessDefinition(final DesignProcessDefinition processDefinition) {
        final XMLNode rootNode = new XMLNode(PROCESS_NODE);
        rootNode.addAttribute(NAME, processDefinition.getName());
        rootNode.addAttribute(DESCRIPTION, processDefinition.getDescription());
        rootNode.addAttribute(DISPLAY_NAME, processDefinition.getDisplayName());
        rootNode.addAttribute(DISPLAY_DESCRIPTION, processDefinition.getDisplayDescription());
        rootNode.addAttribute(VERSION, processDefinition.getVersion());
        rootNode.addAttribute(BOS_VERSION, BOS_CURRENT_VERSION);
        rootNode.addAttribute("xmlns", NAMESPACE);

        final XMLNode stringIndexes = new XMLNode(STRING_INDEXES);
        for (int i = 1; i <= 5; i++) {
            final XMLNode xmlNode = new XMLNode(STRING_INDEX);
            xmlNode.addAttribute(INDEX, String.valueOf(i));
            xmlNode.addAttribute(LABEL, processDefinition.getStringIndexLabel(i));
            final Expression stringIndexValue = processDefinition.getStringIndexValue(i);
            if (stringIndexValue != null) {
                final XMLNode value = new XMLNode(EXPRESSION_NODE);
                fillExpressionNode(value, stringIndexValue, false);
                xmlNode.addChild(value);
            }
            stringIndexes.addChild(xmlNode);
        }
        rootNode.addChild(stringIndexes);

        createAndFillFlowElements(rootNode, processDefinition.getProcessContainer());

        final XMLNode dependencies = new XMLNode(DEPENDENCIES_NODE);
        rootNode.addChild(dependencies);

        final XMLNode parameters = new XMLNode(PARAMETERS_NODE);
        dependencies.addChild(parameters);
        for (final ParameterDefinition parameter : processDefinition.getParameters()) {
            final XMLNode parameterNode = new XMLNode(PARAMETER_NODE);
            fillParameterNode(parameterNode, parameter);
            parameters.addChild(parameterNode);
        }

        final XMLNode actors = new XMLNode(ACTORS_NODE);
        dependencies.addChild(actors);
        for (final ActorDefinition actor : processDefinition.getActorsList()) {
            final XMLNode actorNode = new XMLNode(ACTOR_NODE);
            fillActorNode(actorNode, actor);
            actors.addChild(actorNode);
        }

        final ActorDefinition actorInitiator = processDefinition.getActorInitiator();
        if (actorInitiator != null) {
            final XMLNode initiatorNode = new XMLNode(INITIATOR_NODE);
            rootNode.addChild(initiatorNode);
            fillInitiatorActorNode(initiatorNode, actorInitiator);
        }
        final ContractDefinition contract = processDefinition.getContract();
        if (contract != null) {
            rootNode.addChild(createContractNode(contract));
        }
        rootNode.addChild(createContextNode(processDefinition.getContext()));
        return rootNode;
    }

    private void createAndFillFlowElements(final XMLNode containerNode, final FlowElementContainerDefinition containerDefinition) {
        final XMLNode flowElements = new XMLNode(FLOW_ELEMENTS_NODE);
        containerNode.addChild(flowElements);
        final XMLNode transitionsNode = new XMLNode(TRANSITIONS_NODE);
        flowElements.addChild(transitionsNode);

        for (final TransitionDefinition transition : containerDefinition.getTransitions()) {
            final XMLNode transitionNode = new XMLNode(TRANSITION_NODE);
            final String id = String.valueOf(Math.abs(UUID.randomUUID().getMostSignificantBits()));
            objectToId.put(transition, id);
            transitionNode.addAttribute(ID, id);
            fillTransitionNode(transitionNode, transition);
            transitionsNode.addChild(transitionNode);
        }

        final XMLNode connectorsNode = new XMLNode(CONNECTORS_NODE);
        flowElements.addChild(connectorsNode);
        for (final ConnectorDefinition connector : containerDefinition.getConnectors()) {
            final XMLNode connectorNode = new XMLNode(CONNECTOR_NODE);
            fillConnectorNode(connectorNode, connector);
            connectorsNode.addChild(connectorNode);
        }

        addBusinessDataDefinitionNodes(containerDefinition.getBusinessDataDefinitions(), flowElements);

        final XMLNode dataDefinitionsNode = new XMLNode(DATA_DEFINITIONS_NODE);
        flowElements.addChild(dataDefinitionsNode);
        for (final DataDefinition dataDefinition : containerDefinition.getDataDefinitions()) {
            final XMLNode dataDefinitionNode = getDataDefinitionNode(dataDefinition);
            dataDefinitionsNode.addChild(dataDefinitionNode);
        }
        final XMLNode documentDefinitionsNode = new XMLNode(DOCUMENT_DEFINITIONS_NODE);
        flowElements.addChild(documentDefinitionsNode);
        for (final DocumentDefinition document : containerDefinition.getDocumentDefinitions()) {
            final XMLNode documentDefinitionNode = new XMLNode(DOCUMENT_DEFINITION_NODE);
            fillDocumentDefinitionNode(documentDefinitionNode, document);
            documentDefinitionsNode.addChild(documentDefinitionNode);
        }
        final XMLNode documentListDefinitionsNode = new XMLNode(DOCUMENT_LIST_DEFINITIONS_NODE);
        flowElements.addChild(documentListDefinitionsNode);
        for (final DocumentListDefinition documentList : containerDefinition.getDocumentListDefinitions()) {
            final XMLNode documentListDefinitionNode = new XMLNode(DOCUMENT_LIST_DEFINITION_NODE);
            fillDocumentListDefinitionNode(documentListDefinitionNode, documentList);
            documentListDefinitionsNode.addChild(documentListDefinitionNode);
        }

        createAndFillFlowNodes(containerDefinition, flowElements);
    }

    private void createAndFillFlowNodes(final FlowElementContainerDefinition containerDefinition, final XMLNode flowElements) {
        final XMLNode flowNodes = new XMLNode(FLOW_NODES_NODE);
        flowElements.addChild(flowNodes);
        for (final ActivityDefinition activity : containerDefinition.getActivities()) {
            final String xmlNodeName = getXmlNodeName(activity);
            final XMLNode activityNode = new XMLNode(xmlNodeName);
            if (activity instanceof HumanTaskDefinition) {
                fillHumanTask(activity, activityNode);
            }
            fillFlowNode(activityNode, activity);
            addDataDefinitionNodes(activity, activityNode);
            addBusinessDataDefinitionNodes(activity.getBusinessDataDefinitions(), activityNode);
            addOperationNodes(activity, activityNode);
            addLoopCharacteristics(activity, activityNode);
            addBoundaryEventDefinitionsNode(activity, activityNode);
            if (activity instanceof HumanTaskDefinition) {
                final HumanTaskDefinition humanTaskDefinition = (HumanTaskDefinition) activity;
                if (humanTaskDefinition.getUserFilter() != null) {
                    final XMLNode userFilterNode = new XMLNode(USER_FILTER_NODE);
                    fillUserFilterNode(userFilterNode, humanTaskDefinition.getUserFilter());
                    activityNode.addChild(userFilterNode);
                }
                if (humanTaskDefinition instanceof UserTaskDefinition) {
                    if (((UserTaskDefinition) humanTaskDefinition).getContract() != null) {
                        activityNode.addChild(createContractNode(((UserTaskDefinition) humanTaskDefinition).getContract()));
                    }
                    activityNode.addChild(createContextNode(((UserTaskDefinition) humanTaskDefinition).getContext()));
                }
            } else if (activity instanceof CallActivityDefinition) {
                fillCallActivity((CallActivityDefinition) activity, activityNode);
            } else if (activity instanceof SubProcessDefinition) {
                fillSubProcess((SubProcessDefinition) activity, activityNode);
            } else if (activity instanceof ReceiveTaskDefinition) {
                fillReceiveTask((ReceiveTaskDefinition) activity, activityNode);
            } else if (activity instanceof SendTaskDefinition) {
                fillSendTask((SendTaskDefinition) activity, activityNode);
            }

            flowNodes.addChild(activityNode);
        }
        for (final GatewayDefinition gateway : containerDefinition.getGatewaysList()) {
            final XMLNode gatewayNode = new XMLNode(GATEWAY_NODE);
            fillGatewayNode(gatewayNode, gateway);
            flowNodes.addChild(gatewayNode);
        }
        // events
        createAndFillStartEvents(containerDefinition, flowNodes);
        createAndfillIntermediateCatchEvents(containerDefinition, flowNodes);
        createAndFillIntermediateThrowEvents(containerDefinition, flowNodes);
        createAndFillEndEvents(containerDefinition, flowNodes);
    }

    private XMLNode createContractNode(final ContractDefinition contract) {
        final XMLNode contractNode = new XMLNode(CONTRACT_NODE);
        final XMLNode inputsNode = new XMLNode(CONTRACT_INPUTS_NODE);
        if (!contract.getInputs().isEmpty()) {
            for (final InputDefinition input : contract.getInputs()) {
                inputsNode.addChild(createInputNode(input));
            }
        }
        if (!inputsNode.getChildNodes().isEmpty()) {
            contractNode.addChild(inputsNode);
        }
        final List<ConstraintDefinition> constraints = contract.getConstraints();
        if (!constraints.isEmpty()) {
            final XMLNode rulesNode = new XMLNode(CONTRACT_CONSTRAINTS_NODE);
            contractNode.addChild(rulesNode);
            for (final ConstraintDefinition constraintDefinition : constraints) {
                rulesNode.addChild(createConstraintNode(constraintDefinition));
            }
        }
        return contractNode;
    }

    private XMLNode createContextNode(final List<ContextEntry> context) {
        final XMLNode contextNode = new XMLNode(CONTEXT_NODE);
        for (final ContextEntry contextEntry : context) {
            final XMLNode node = new XMLNode(CONTEXT_ENTRY_NODE);
            node.addAttribute(CONTEXT_ENTRY_KEY, contextEntry.getKey());
            addExpressionNode(node, EXPRESSION_NODE, contextEntry.getExpression());
            contextNode.addChild(node);
        }
        return contextNode;
    }

    private XMLNode createConstraintNode(final ConstraintDefinition constraintDefinition) {
        final XMLNode xmlNode = new XMLNode(CONTRACT_CONSTRAINT_NODE);
        xmlNode.addAttribute(NAME, constraintDefinition.getName());
        xmlNode.addChild(CONSTRAINT_EXPRESSION, constraintDefinition.getExpression());
        xmlNode.addChild(CONSTRAINT_EXPLANATION, constraintDefinition.getExplanation());
        final XMLNode namesNode = new XMLNode(INPUT_NAMES);
        xmlNode.addChild(namesNode);
        for (final String inputName : constraintDefinition.getInputNames()) {
            namesNode.addChild(INPUT_NAME, inputName);
        }
        return xmlNode;
    }

    private XMLNode createInputNode(final InputDefinition input) {
        final XMLNode inputNode = new XMLNode(CONTRACT_INPUT_NODE);
        inputNode.addAttribute(NAME, input.getName());
        inputNode.addAttribute(MULTIPLE, String.valueOf(input.isMultiple()));
        inputNode.addAttribute(DESCRIPTION, input.getDescription());
        final Type type = input.getType();
        if (type != null) {
            inputNode.addAttribute(TYPE, type.toString());
        }
        for (final InputDefinition inputDefinition : input.getInputs()) {
            inputNode.addChild(createInputNode(inputDefinition));
        }
        return inputNode;
    }

    private void addBusinessDataDefinitionNodes(final List<BusinessDataDefinition> businessDataDefinitions, final XMLNode containerNode) {
        if (!businessDataDefinitions.isEmpty()) {
            final XMLNode businessDataDefinitionsNode = new XMLNode(BUSINESS_DATA_DEFINITIONS_NODE);
            containerNode.addChild(businessDataDefinitionsNode);
            for (final BusinessDataDefinition businessDataDefinition : businessDataDefinitions) {
                final XMLNode businessDataDefinitionNode = getBusinessDataDefinitionNode(businessDataDefinition);
                businessDataDefinitionsNode.addChild(businessDataDefinitionNode);
            }
        }
    }

    private void addBoundaryEventDefinitionsNode(final ActivityDefinition activity, final XMLNode activityNode) {
        final XMLNode boundaryEventsNode = new XMLNode(BOUNDARY_EVENTS_NODE);
        activityNode.addChild(boundaryEventsNode);
        addBoundaryEventDefinitionNodes(activity, boundaryEventsNode);
    }

    private void addBoundaryEventDefinitionNodes(final ActivityDefinition activity, final XMLNode boundaryEventsNode) {
        for (final BoundaryEventDefinition boundaryEvent : activity.getBoundaryEventDefinitions()) {
            final XMLNode boundaryEventNode = new XMLNode(BOUNDARY_EVENT_NODE);
            fillFlowNode(boundaryEventNode, boundaryEvent);
            fillCatchEventDefinition(boundaryEventNode, boundaryEvent);
            boundaryEventsNode.addChild(boundaryEventNode);
        }
    }

    private String getXmlNodeName(final ActivityDefinition activity) {
        String xmlNodeName = null;
        if (activity instanceof CallActivityDefinition) {
            xmlNodeName = CALL_ACTIVITY_NODE;
        } else if (activity instanceof AutomaticTaskDefinition) {
            xmlNodeName = AUTOMATIC_TASK_NODE;
        } else if (activity instanceof ReceiveTaskDefinition) {
            xmlNodeName = RECEIVE_TASK_NODE;
        } else if (activity instanceof SendTaskDefinition) {
            xmlNodeName = SEND_TASK_NODE;
        } else if (activity instanceof ManualTaskDefinition) {
            xmlNodeName = MANUAL_TASK_NODE;
        } else if (activity instanceof UserTaskDefinition) {
            xmlNodeName = USER_TASK_NODE;
        } else if (activity instanceof SubProcessDefinitionImpl) {
            xmlNodeName = SUB_PROCESS;
        }
        // TODO else throw exception
        return xmlNodeName;
    }

    private void fillSubProcess(final SubProcessDefinition activity, final XMLNode activityNode) {
        activityNode.addAttribute(TRIGGERED_BY_EVENT, String.valueOf(activity.isTriggeredByEvent()));
        createAndFillFlowElements(activityNode, activity.getSubProcessContainer());
    }

    private void fillCallActivity(final CallActivityDefinition activity, final XMLNode activityNode) {
        addExpressionNode(activityNode, CALLABLE_ELEMENT_NODE, activity.getCallableElement());
        addExpressionNode(activityNode, CALLABLE_ELEMENT_VERSION_NODE, activity.getCallableElementVersion());
        createAndfillOperations(activityNode, activity.getDataInputOperations(), DATA_INPUT_OPERATION_NODE);
        createAndfillOperations(activityNode, activity.getDataOutputOperations(), DATA_OUTPUT_OPERATION_NODE);
        activityNode.addAttribute(CALLABLE_ELEMENT_TYPE, activity.getCallableElementType().name());
    }

    private void fillReceiveTask(final ReceiveTaskDefinition receiveTask, final XMLNode activityNode) {
        createAndfillMessageEventTriggers(activityNode, receiveTask.getTrigger());
    }

    private void fillSendTask(final SendTaskDefinition sendTask, final XMLNode activityNode) {
        createAndfillMessageEventTrigger(activityNode, sendTask.getMessageTrigger());
    }

    private void fillHumanTask(final ActivityDefinition activity, final XMLNode activityNode) {
        final HumanTaskDefinition humanTaskDefinition = (HumanTaskDefinition) activity;
        final String actorName = humanTaskDefinition.getActorName();
        activityNode.addAttribute(ACTOR_NAME, actorName);
        final String priority = humanTaskDefinition.getPriority();
        activityNode.addAttribute(PRIORITY, priority);
        final Long expectedDuration = humanTaskDefinition.getExpectedDuration();
        if (expectedDuration != null) {
            activityNode.addAttribute(EXPECTED_DURATION, String.valueOf(expectedDuration));
        }
    }

    private void createAndFillIntermediateThrowEvents(final FlowElementContainerDefinition containerDefinition, final XMLNode flowNodes) {
        for (final IntermediateThrowEventDefinition intermediateThrowEvent : containerDefinition.getIntermediateThrowEvents()) {
            final XMLNode intermediateThrowEventNode = new XMLNode(INTERMEDIATE_THROW_EVENT_NODE);
            fillFlowNode(intermediateThrowEventNode, intermediateThrowEvent);
            fillThrowEventDefinition(intermediateThrowEventNode, intermediateThrowEvent);
            flowNodes.addChild(intermediateThrowEventNode);
        }
    }

    private void createAndFillStartEvents(final FlowElementContainerDefinition containerDefinition, final XMLNode flowNodes) {
        for (final StartEventDefinition startEvent : containerDefinition.getStartEvents()) {
            final XMLNode startEventNode = new XMLNode(START_EVENT_NODE);
            fillFlowNode(startEventNode, startEvent);
            fillCatchEventDefinition(startEventNode, startEvent);
            flowNodes.addChild(startEventNode);
        }
    }

    private void createAndFillEndEvents(final FlowElementContainerDefinition containerDefinition, final XMLNode flowNodes) {
        for (final EndEventDefinition endEvent : containerDefinition.getEndEvents()) {
            final XMLNode endEventNode = new XMLNode(END_EVENT_NODE);
            fillFlowNode(endEventNode, endEvent);
            fillThrowEventDefinition(endEventNode, endEvent);
            createAndFillTerminateEventTrigger(endEventNode, endEvent);
            createAndfillErrorEventTriggers(endEventNode, endEvent);
            flowNodes.addChild(endEventNode);
        }
    }

    private void createAndFillTerminateEventTrigger(final XMLNode eventNode, final EndEventDefinition event) {
        final TerminateEventTriggerDefinition terminateEventTriggerDefinition = event.getTerminateEventTriggerDefinition();
        if (terminateEventTriggerDefinition != null) {
            final XMLNode terminateEventTriggerNode = new XMLNode(TERMINATE_EVENT_TRIGGER_NODE);
            eventNode.addChild(terminateEventTriggerNode);
        }
    }

    private void fillThrowEventDefinition(final XMLNode eventNode, final ThrowEventDefinition event) {
        createAndfillMessageEventTriggers(eventNode, event);
        createAndfillSignalEventTriggers(eventNode, event);
    }

    private void createAndfillSignalEventTriggers(final XMLNode eventNode, final ThrowEventDefinition event) {
        for (final ThrowSignalEventTriggerDefinition signalEventTrigger : event.getSignalEventTriggerDefinitions()) {
            final XMLNode signalEventTriggerNode = new XMLNode(THROW_SIGNAL_EVENT_TRIGGER_NODE);
            signalEventTriggerNode.addAttribute(NAME, signalEventTrigger.getSignalName());
            eventNode.addChild(signalEventTriggerNode);
        }
    }

    private void createAndfillErrorEventTriggers(final XMLNode eventNode, final EndEventDefinition event) {
        for (final ThrowErrorEventTriggerDefinition errorEventTrigger : event.getErrorEventTriggerDefinitions()) {
            final XMLNode errorEventTriggerNode = new XMLNode(THROW_ERROR_EVENT_TRIGGER_NODE);
            errorEventTriggerNode.addAttribute(ERROR_CODE, errorEventTrigger.getErrorCode());
            eventNode.addChild(errorEventTriggerNode);
        }
    }

    private void createAndfillMessageEventTriggers(final XMLNode eventNode, final ThrowEventDefinition event) {
        for (final ThrowMessageEventTriggerDefinition messageEventTrigger : event.getMessageEventTriggerDefinitions()) {
            createAndfillMessageEventTrigger(eventNode, messageEventTrigger);
        }
    }

    private void createAndfillMessageEventTrigger(final XMLNode eventNode, final ThrowMessageEventTriggerDefinition messageEventTrigger) {
        final XMLNode messageEventTriggerNode = new XMLNode(THROW_MESSAGE_EVENT_TRIGGER_NODE);
        messageEventTriggerNode.addAttribute(NAME, messageEventTrigger.getMessageName());
        addExpressionNode(messageEventTriggerNode, TARGET_PROCESS, messageEventTrigger.getTargetProcess());
        final Expression targetFlowNode = messageEventTrigger.getTargetFlowNode();
        if (targetFlowNode != null) {
            addExpressionNode(messageEventTriggerNode, TARGET_FLOW_NODE, targetFlowNode);
        }
        createAndFillDataDefinitions(messageEventTriggerNode, messageEventTrigger);
        createAndfillCorrelations(messageEventTriggerNode, messageEventTrigger.getCorrelations());
        eventNode.addChild(messageEventTriggerNode);
    }

    private void createAndFillDataDefinitions(final XMLNode messageEventTriggerNode, final ThrowMessageEventTriggerDefinition messageEventTrigger) {
        for (final DataDefinition dataDefinition : messageEventTrigger.getDataDefinitions()) {
            final XMLNode dataDefinitionNode = getDataDefinitionNode(dataDefinition);
            messageEventTriggerNode.addChild(dataDefinitionNode);
        }
    }

    private void addDataDefinitionNodes(final ActivityDefinition activity, final XMLNode activityNode) {
        final XMLNode dataDefinitionsNodeFromActivity = new XMLNode(DATA_DEFINITIONS_NODE);
        activityNode.addChild(dataDefinitionsNodeFromActivity);
        for (final DataDefinition dataDefinition : activity.getDataDefinitions()) {
            final XMLNode dataDefinitionNode = getDataDefinitionNode(dataDefinition);
            dataDefinitionsNodeFromActivity.addChild(dataDefinitionNode);
        }
    }

    private void addOperationNodes(final ActivityDefinition activity, final XMLNode activityNode) {
        final XMLNode operationsNode = new XMLNode(OPERATIONS_NODE);
        activityNode.addChild(operationsNode);
        createAndfillOperations(operationsNode, activity.getOperations(), OPERATION_NODE);
    }

    private void addLoopCharacteristics(final ActivityDefinition activity, final XMLNode activityNode) {
        final LoopCharacteristics loopCharacteristics = activity.getLoopCharacteristics();
        if (loopCharacteristics != null) {
            final XMLNode loopNode;
            if (loopCharacteristics instanceof StandardLoopCharacteristics) {
                loopNode = new XMLNode(STANDARD_LOOP_CHARACTERISTICS_NODE);
                final StandardLoopCharacteristics loop = (StandardLoopCharacteristics) loopCharacteristics;
                loopNode.addAttribute(TEST_BEFORE, String.valueOf(loop.isTestBefore()));
                final Expression loopMax = loop.getLoopMax();
                final XMLNode expressionNode = new XMLNode(LOOP_CONDITION);
                fillExpressionNode(expressionNode, loop.getLoopCondition(), true);
                loopNode.addChild(expressionNode);
                if (loopMax != null) {
                    final XMLNode loopMaxNode = new XMLNode(LOOP_MAX);
                    fillExpressionNode(loopMaxNode, loopMax, false);
                    loopNode.addChild(loopMaxNode);
                }
            } else {
                loopNode = new XMLNode(MULTI_INSTANCE_LOOP_CHARACTERISTICS_NODE);
                final MultiInstanceLoopCharacteristics loop = (MultiInstanceLoopCharacteristics) loopCharacteristics;
                loopNode.addAttribute(MULTI_INSTANCE_IS_SEQUENTIAL, String.valueOf(loop.isSequential()));
                loopNode.addAttribute(MULTI_INSTANCE_DATA_INPUT_ITEM_REF, loop.getDataInputItemRef());
                loopNode.addAttribute(MULTI_INSTANCE_DATA_OUTPUT_ITEM_REF, loop.getDataOutputItemRef());
                loopNode.addAttribute(MULTI_INSTANCE_LOOP_DATA_INPUT, loop.getLoopDataInputRef());
                loopNode.addAttribute(MULTI_INSTANCE_LOOP_DATA_OUTPUT, loop.getLoopDataOutputRef());
                final Expression loopCardinality = loop.getLoopCardinality();
                if (loopCardinality != null) {
                    final XMLNode expressionNode = new XMLNode(MULTI_INSTANCE_LOOP_CARDINALITY);
                    fillExpressionNode(expressionNode, loopCardinality, false);
                    loopNode.addChild(expressionNode);
                }
                final Expression completionCondition = loop.getCompletionCondition();
                if (completionCondition != null) {
                    final XMLNode expressionNode = new XMLNode(MULTI_INSTANCE_COMPLETION_CONDITION);
                    fillExpressionNode(expressionNode, completionCondition, true);
                    loopNode.addChild(expressionNode);
                }
            }
            activityNode.addChild(loopNode);
        }
    }

    private void createAndfillIntermediateCatchEvents(final FlowElementContainerDefinition containerDefinition, final XMLNode flowNodes) {
        for (final IntermediateCatchEventDefinition intermediateEvent : containerDefinition.getIntermediateCatchEvents()) {
            final XMLNode intermediateCatchEventNode = new XMLNode(INTERMEDIATE_CATCH_EVENT_NODE);
            fillFlowNode(intermediateCatchEventNode, intermediateEvent);
            fillCatchEventDefinition(intermediateCatchEventNode, intermediateEvent);
            flowNodes.addChild(intermediateCatchEventNode);
        }
    }

    private void fillCatchEventDefinition(final XMLNode catchEventNode, final CatchEventDefinition catchEvent) {
        createAndFillTimerEventTriggers(catchEventNode, catchEvent);
        createAndfillMessageEventTriggers(catchEventNode, catchEvent);
        createAndfillSignalEventTriggers(catchEventNode, catchEvent);
        createAndfillErrorEventTriggers(catchEventNode, catchEvent);
        catchEventNode.addAttribute(INTERRUPTING, String.valueOf(catchEvent.isInterrupting()));
    }

    private void createAndFillTimerEventTriggers(final XMLNode catchEventNode, final CatchEventDefinition catchEvent) {
        for (final TimerEventTriggerDefinition timerEventTrigger : catchEvent.getTimerEventTriggerDefinitions()) {
            final XMLNode timerEventTriggerNode = new XMLNode(TIMER_EVENT_TRIGGER_NODE);
            timerEventTriggerNode.addAttribute(TIMER_EVENT_TRIGGER_TIMER_TYPE, timerEventTrigger.getTimerType().toString());
            final XMLNode expression = new XMLNode(EXPRESSION_NODE);
            fillExpressionNode(expression, timerEventTrigger.getTimerExpression(), false);
            timerEventTriggerNode.addChild(expression);
            catchEventNode.addChild(timerEventTriggerNode);
        }
    }

    private void createAndfillSignalEventTriggers(final XMLNode catchEventNode, final CatchEventDefinition catchEvent) {
        for (final CatchSignalEventTriggerDefinition signalEventTrigger : catchEvent.getSignalEventTriggerDefinitions()) {
            final XMLNode signalEventTriggerNode = new XMLNode(CATCH_SIGNAL_EVENT_TRIGGER_NODE);
            signalEventTriggerNode.addAttribute(NAME, signalEventTrigger.getSignalName());
            catchEventNode.addChild(signalEventTriggerNode);
        }
    }

    private void createAndfillErrorEventTriggers(final XMLNode catchEventNode, final CatchEventDefinition catchEvent) {
        for (final CatchErrorEventTriggerDefinition errorEventTrigger : catchEvent.getErrorEventTriggerDefinitions()) {
            final XMLNode errorEventTriggerNode = new XMLNode(CATCH_ERROR_EVENT_TRIGGER_NODE);
            errorEventTriggerNode.addAttribute(ERROR_CODE, errorEventTrigger.getErrorCode());
            catchEventNode.addChild(errorEventTriggerNode);
        }
    }

    private void createAndfillMessageEventTriggers(final XMLNode catchEventNode, final CatchEventDefinition catchEvent) {
        for (final CatchMessageEventTriggerDefinition messageEventTrigger : catchEvent.getMessageEventTriggerDefinitions()) {
            createAndfillMessageEventTriggers(catchEventNode, messageEventTrigger);
        }
    }

    private void createAndfillMessageEventTriggers(final XMLNode activityNode, final CatchMessageEventTriggerDefinition trigger) {
        final XMLNode messageEventTriggerNode = new XMLNode(CATCH_MESSAGE_EVENT_TRIGGER_NODE);
        messageEventTriggerNode.addAttribute(NAME, trigger.getMessageName());
        createAndfillCorrelations(messageEventTriggerNode, trigger.getCorrelations());
        createAndfillOperations(messageEventTriggerNode, trigger.getOperations(), OPERATION_NODE);
        activityNode.addChild(messageEventTriggerNode);
    }

    private void createAndfillCorrelations(final XMLNode messageEventTriggerNode, final List<CorrelationDefinition> correlations) {
        for (final CorrelationDefinition correlation : correlations) {
            final XMLNode correlationNode = new XMLNode(CORRELATION_NODE);
            final XMLNode keyNode = new XMLNode(CORRELATION_KEY);
            fillExpressionNode(keyNode, correlation.getKey(), false);
            correlationNode.addChild(keyNode);
            final XMLNode valueNode = new XMLNode(CORRELATION_VALUE);
            fillExpressionNode(valueNode, correlation.getValue(), false);
            correlationNode.addChild(valueNode);
            messageEventTriggerNode.addChild(correlationNode);
        }
    }

    private void createAndfillOperations(final XMLNode parentNode, final List<Operation> operations, final String operationNodeName) {
        for (final Operation operation : operations) {
            createAndFillOperation(parentNode, operation, operationNodeName);
        }
    }

    private void createAndFillOperation(final XMLNode parentNode, final Operation operation, final String operationNodeName) {
        final XMLNode operationNode = new XMLNode(operationNodeName);
        operationNode.addAttribute(OPERATION_OPERATOR_TYPE, operation.getType().name());
        operationNode.addAttribute(OPERATION_OPERATOR, operation.getOperator());
        final XMLNode leftOperand = new XMLNode(OPERATION_LEFT_OPERAND);
        fillLeftOperandNode(leftOperand, operation.getLeftOperand());
        operationNode.addChild(leftOperand);
        final Expression rightOperandExpression = operation.getRightOperand();
        if (rightOperandExpression != null) {
            final XMLNode expressionNode = new XMLNode(OPERATION_RIGHT_OPERAND);
            fillExpressionNode(expressionNode, operation.getRightOperand(), false);
            operationNode.addChild(expressionNode);
        }
        parentNode.addChild(operationNode);
    }

    private void fillConnectorNode(final XMLNode connectorNode, final ConnectorDefinition connector) {
        // connectorNode.addAttribute(ID, String.valueOf(connector.getId())); TODO : Implement generation of id
        connectorNode.addAttribute(NAME, connector.getName());
        connectorNode.addAttribute(CONNECTOR_ID, connector.getConnectorId());
        connectorNode.addAttribute(CONNECTOR_VERSION, connector.getVersion());
        connectorNode.addAttribute(CONNECTOR_ACTIVATION_EVENT, connector.getActivationEvent().toString());
        connectorNode.addAttribute(CONNECTOR_FAIL_ACTION, connector.getFailAction().toString());
        if (connector.getFailAction() == FailAction.ERROR_EVENT) {
            connectorNode.addAttribute(CONNECTOR_ERROR_CODE, connector.getErrorCode());
        }
        final Map<String, Expression> inputs = connector.getInputs();
        createAndFillInputsNode(connectorNode, inputs);
        final XMLNode outputsNode = new XMLNode(CONNECTOR_OUTPUTS_NODE);
        createAndfillOperations(outputsNode, connector.getOutputs(), OPERATION_NODE);
        connectorNode.addChild(outputsNode);
    }

    private void createAndFillInputsNode(final XMLNode connectorNode, final Map<String, Expression> inputs) {
        final XMLNode inputsNode = new XMLNode(CONNECTOR_INPUTS_NODE);
        for (final Entry<String, Expression> input : inputs.entrySet()) {
            final XMLNode inputNode = new XMLNode(CONNECTOR_INPUT);
            inputNode.addAttribute(NAME, input.getKey());
            final XMLNode expressionNode = new XMLNode(EXPRESSION_NODE);
            fillExpressionNode(expressionNode, input.getValue(), false);
            inputNode.addChild(expressionNode);
            inputsNode.addChild(inputNode);
        }
        connectorNode.addChild(inputsNode);
    }

    private void fillLeftOperandNode(final XMLNode rightOperandNode, final LeftOperand leftOperand) {
        rightOperandNode.addAttribute(LEFT_OPERAND_NAME, leftOperand.getName());
        rightOperandNode.addAttribute(LEFT_OPERAND_TYPE, leftOperand.getType());
    }

    private void fillUserFilterNode(final XMLNode userFilterNode, final UserFilterDefinition userFilter) {
        userFilterNode.addAttribute(NAME, userFilter.getName());
        userFilterNode.addAttribute(USER_FILTER_ID, userFilter.getUserFilterId());
        userFilterNode.addAttribute(CONNECTOR_VERSION, userFilter.getVersion());
        final Map<String, Expression> inputs = userFilter.getInputs();
        createAndFillInputsNode(userFilterNode, inputs);
    }

    private XMLNode getBusinessDataDefinitionNode(final BusinessDataDefinition businessDataDefinition) {
        XMLNode businessDataDefinitionNode = null;
        businessDataDefinitionNode = new XMLNode(BUSINESS_DATA_DEFINITION_NODE);
        businessDataDefinitionNode.addAttribute(NAME, businessDataDefinition.getName());
        businessDataDefinitionNode.addAttribute(DATA_DEFINITION_CLASS, businessDataDefinition.getClassName());
        businessDataDefinitionNode.addAttribute(BUSINESS_DATA_DEFINITION_IS_MULTIPLE, String.valueOf(businessDataDefinition.isMultiple()));
        if (businessDataDefinition.getDescription() != null) {
            businessDataDefinitionNode.addChild(DESCRIPTION, businessDataDefinition.getDescription());
        }
        Expression defaultValueExpression;
        if ((defaultValueExpression = businessDataDefinition.getDefaultValueExpression()) != null) {
            final XMLNode defaultValueNode = new XMLNode(DEFAULT_VALUE_NODE);
            fillExpressionNode(defaultValueNode, defaultValueExpression, false);
            businessDataDefinitionNode.addChild(defaultValueNode);
        }
        return businessDataDefinitionNode;
    }

    private XMLNode getDataDefinitionNode(final DataDefinition dataDefinition) {
        XMLNode dataDefinitionNode = null;
        if (dataDefinition instanceof XMLDataDefinition) {
            dataDefinitionNode = new XMLNode(XML_DATA_DEFINITION_NODE);
            fillDataDefinitionNode(dataDefinitionNode, dataDefinition);
            final XMLDataDefinition xmlData = (XMLDataDefinition) dataDefinition;
            final String namespace = xmlData.getNamespace();
            if (namespace != null) {
                dataDefinitionNode.addChild(XML_DATA_DEFINITION_NAMESPACE, namespace);
            }
            final String element = xmlData.getElement();
            if (element != null) {
                dataDefinitionNode.addChild(XML_DATA_DEFINITION_ELEMENT, element);
            }
        } else if (dataDefinition instanceof TextDataDefinition) {
            dataDefinitionNode = new XMLNode(TEXT_DATA_DEFINITION_NODE);
            dataDefinitionNode.addAttribute(TEXT_DATA_DEFINITION_LONG, String.valueOf(((TextDataDefinition) dataDefinition).isLongText()));
            fillDataDefinitionNode(dataDefinitionNode, dataDefinition);
        } else {
            dataDefinitionNode = new XMLNode(DATA_DEFINITION_NODE);
            fillDataDefinitionNode(dataDefinitionNode, dataDefinition);
        }
        return dataDefinitionNode;
    }

    private void fillDataDefinitionNode(final XMLNode dataDefinitionNode, final DataDefinition dataDefinition) {
        dataDefinitionNode.addAttribute(NAME, dataDefinition.getName());
        dataDefinitionNode.addAttribute(DATA_DEFINITION_CLASS, dataDefinition.getClassName());
        dataDefinitionNode.addAttribute(DATA_DEFINITION_TRANSIENT, String.valueOf(dataDefinition.isTransientData()));
        if (dataDefinition.getDescription() != null) {
            dataDefinitionNode.addChild(DESCRIPTION, dataDefinition.getDescription());
        }
        Expression defaultValueExpression;
        if ((defaultValueExpression = dataDefinition.getDefaultValueExpression()) != null) {
            final XMLNode defaultValueNode = new XMLNode(DEFAULT_VALUE_NODE);
            fillExpressionNode(defaultValueNode, defaultValueExpression, false);
            dataDefinitionNode.addChild(defaultValueNode);
        }
    }

    private void fillDocumentDefinitionNode(final XMLNode documentDefinitionNode, final DocumentDefinition documentDefinition) {
        documentDefinitionNode.addAttribute(NAME, documentDefinition.getName());
        documentDefinitionNode.addAttribute(DOCUMENT_DEFINITION_MIME_TYPE, documentDefinition.getContentMimeType());
        if (documentDefinition.getFileName() != null) {
            documentDefinitionNode.addChild(DOCUMENT_DEFINITION_FILE_NAME, documentDefinition.getFileName());
        }
        if (documentDefinition.getDescription() != null) {
            documentDefinitionNode.addChild(DESCRIPTION, documentDefinition.getDescription());
        }
        if (documentDefinition.getUrl() != null) {
            documentDefinitionNode.addChild(DOCUMENT_DEFINITION_URL, documentDefinition.getUrl());
        }
        if (documentDefinition.getFile() != null) {
            documentDefinitionNode.addChild(DOCUMENT_DEFINITION_FILE, documentDefinition.getFile());
        }
    }

    private void fillDocumentListDefinitionNode(final XMLNode documentListDefinitionNode, final DocumentListDefinition documentListDefinition) {
        documentListDefinitionNode.addAttribute(NAME, documentListDefinition.getName());
        if (documentListDefinition.getDescription() != null) {
            documentListDefinitionNode.addChild(DESCRIPTION, documentListDefinition.getDescription());
        }
        if (documentListDefinition.getExpression() != null) {
            final XMLNode value = new XMLNode(EXPRESSION_NODE);
            fillExpressionNode(value, documentListDefinition.getExpression(), false);
            documentListDefinitionNode.addChild(value);
        }
    }

    private void fillActorNode(final XMLNode actorNode, final ActorDefinition actor) {
        actorNode.addAttribute(NAME, actor.getName());
        if (actor.getDescription() != null) {
            actorNode.addChild(DESCRIPTION, actor.getDescription());
        }
    }

    private void fillInitiatorActorNode(final XMLNode actorNode, final ActorDefinition actor) {
        actorNode.addAttribute(NAME, actor.getName());
    }

    private void fillParameterNode(final XMLNode parameterNode, final ParameterDefinition parameter) {
        parameterNode.addAttribute(NAME, parameter.getName());
        parameterNode.addAttribute(PARAMETER_TYPE, parameter.getType());
        if (parameter.getDescription() != null) {
            parameterNode.addChild(DESCRIPTION, parameter.getDescription());
        }
    }

    private void fillGatewayNode(final XMLNode gatewayNode, final GatewayDefinition gateway) {
        fillFlowNode(gatewayNode, gateway);
        gatewayNode.addAttribute(GATEWAY_TYPE, gateway.getGatewayType().toString());
    }

    private void fillFlowNode(final XMLNode node, final FlowNodeDefinition flowNode) {
        node.addAttribute(ID, String.valueOf(flowNode.getId()));
        node.addAttribute(NAME, flowNode.getName());
        node.addAttribute(DESCRIPTION, flowNode.getDescription());
        addExpressionNode(node, DISPLAY_NAME, flowNode.getDisplayName());
        addExpressionNode(node, DISPLAY_DESCRIPTION, flowNode.getDisplayDescription());
        addExpressionNode(node, DISPLAY_DESCRIPTION_AFTER_COMPLETION, flowNode.getDisplayDescriptionAfterCompletion());
        for (final TransitionDefinition transition : flowNode.getIncomingTransitions()) {
            final XMLNode transitionNode = new XMLNode(INCOMING_TRANSITION);
            fillTransitionRefNode(transitionNode, transition);
            node.addChild(transitionNode);
        }
        for (final TransitionDefinition transition : flowNode.getOutgoingTransitions()) {
            final XMLNode transitionNode = new XMLNode(OUTGOING_TRANSITION);
            fillTransitionRefNode(transitionNode, transition);
            node.addChild(transitionNode);
        }
        if (flowNode.getDefaultTransition() != null) {
            final XMLNode defaultTransition = new XMLNode(DEFAULT_TRANSITION);
            fillTransitionRefNode(defaultTransition, flowNode.getDefaultTransition());
            node.addChild(defaultTransition);
        }
        for (final ConnectorDefinition connector : flowNode.getConnectors()) {
            final XMLNode connectorNode = new XMLNode(CONNECTOR_NODE);
            fillConnectorNode(connectorNode, connector);
            node.addChild(connectorNode);
        }
    }

    private void addExpressionNode(final XMLNode parentNode, final String expressionNodeName, final Expression expression) {
        if (expression != null) {
            final XMLNode xmlNode = new XMLNode(expressionNodeName);
            fillExpressionNode(xmlNode, expression, false);
            parentNode.addChild(xmlNode);
        }
    }

    private void fillTransitionRefNode(final XMLNode transitionNode, final TransitionDefinition transition) {
        transitionNode.addAttribute(IDREF, objectToId.get(transition));
    }

    private void fillExpressionNode(final XMLNode expressionNode, final Expression expression, final boolean isCondition) {
        if (isCondition && !"java.lang.Boolean".equals(expression.getReturnType())) {
            throw new BonitaRuntimeException("The return type of the expression must be Boolean");
        }

        expressionNode.addAttribute(EXPRESSION_TYPE, expression.getExpressionType());
        expressionNode.addAttribute(NAME, expression.getName());
        expressionNode.addAttribute(EXPRESSION_RETURN_TYPE, expression.getReturnType());
        expressionNode.addAttribute(EXPRESSION_INTERPRETER, expression.getInterpreter());
        expressionNode.addAttribute(ID, String.valueOf(expression.getId()));
        expressionNode.addChild(EXPRESSION_CONTENT, expression.getContent());
        for (final Expression dependency : expression.getDependencies()) {
            final XMLNode dependencyExpression = new XMLNode(EXPRESSION_NODE);
            fillExpressionNode(dependencyExpression, dependency, false);
            expressionNode.addChild(dependencyExpression);
        }
    }

    private void fillTransitionNode(final XMLNode transitionNode, final TransitionDefinition transition) {
        transitionNode.addAttribute(NAME, transition.getName());
        transitionNode.addAttribute(TRANSITION_SOURCE, String.valueOf(transition.getSource()));
        transitionNode.addAttribute(TRANSITION_TARGET, String.valueOf(transition.getTarget()));
        if (transition.getCondition() != null) {
            final XMLNode condition = new XMLNode(TRANSITION_CONDITION);
            fillExpressionNode(condition, transition.getCondition(), true);
            transitionNode.addChild(condition);
        }
    }

}
