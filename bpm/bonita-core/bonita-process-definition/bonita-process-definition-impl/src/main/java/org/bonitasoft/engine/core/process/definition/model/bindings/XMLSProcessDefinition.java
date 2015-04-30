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
package org.bonitasoft.engine.core.process.definition.model.bindings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bonitasoft.engine.bpm.connector.FailAction;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SActorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SAutomaticTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.SBusinessDataDefinition;
import org.bonitasoft.engine.core.process.definition.model.SCallActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SConstraintDefinition;
import org.bonitasoft.engine.core.process.definition.model.SContextEntry;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SDocumentDefinition;
import org.bonitasoft.engine.core.process.definition.model.SDocumentListDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SGatewayDefinition;
import org.bonitasoft.engine.core.process.definition.model.SHumanTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.SInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SLoopCharacteristics;
import org.bonitasoft.engine.core.process.definition.model.SManualTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.SMultiInstanceLoopCharacteristics;
import org.bonitasoft.engine.core.process.definition.model.SParameterDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SReceiveTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSendTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSubProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.SType;
import org.bonitasoft.engine.core.process.definition.model.SUserFilterDefinition;
import org.bonitasoft.engine.core.process.definition.model.SUserTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SBoundaryEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SCatchEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEndEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SIntermediateCatchEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SIntermediateThrowEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SStartEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SThrowEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SCatchErrorEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SCatchMessageEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SCatchSignalEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SCorrelationDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.STerminateEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SThrowErrorEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SThrowMessageEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SThrowSignalEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.STimerEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SStandardLoopCharacteristicsImpl;
import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.data.definition.model.STextDataDefinition;
import org.bonitasoft.engine.data.definition.model.SXMLDataDefinition;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.xml.XMLNode;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class XMLSProcessDefinition {

    public static final String CORRELATION_VALUE = "value";

    public static final String CORRELATION_KEY = "key";

    public static final String NAMESPACE = "http://www.bonitasoft.org/ns/process/server/6.3";

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

    public static final String BUSINESS_DATA_DEFINITION_NODE = "businessDataDefinition";

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

    public static final String MULTIPLE = "multiple";

    public static final String ID = "id";

    public static final String IDREF = "idref";

    public static final String VERSION = "version";

    public static final String DESCRIPTION = "description";

    public static final String DISPLAY_NAME = "displayName";

    public static final String DISPLAY_DESCRIPTION = "displayDescription";

    public static final String STRING_INDEXES = "stringIndexes";

    public static final String STRING_INDEX = "stringIndex";

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

    public static final String BUSINESS_DATA_DEFINITION_CLASS = "className";

    public static final String BUSINESS_DATA_DEFINITION_IS_MULTIPLE = "multiple";

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

    private static final String BUSINESS_DATA_DEFINITIONS_NODE = "businessDataDefinitions";

    public static final String CONTRACT_NODE = "contract";

    public static final String CONTRACT_INPUTS_NODE = "inputDefinitions";

    public static final String CONTRACT_INPUT_NODE = "inputDefinition";

    public static final String TYPE = "type";

    public static final String CONSTRAINT_TYPE = "type";

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

    public XMLNode getXMLProcessDefinition(final SProcessDefinition definition) {
        final XMLNode rootNode = new XMLNode(PROCESS_NODE);
        rootNode.addAttribute(ID, String.valueOf(definition.getId()));
        rootNode.addAttribute(NAME, definition.getName());
        rootNode.addAttribute(VERSION, definition.getVersion());
        rootNode.addAttribute(BOS_VERSION, BOS_CURRENT_VERSION);
        rootNode.addAttribute(DESCRIPTION, definition.getDescription());
        final XMLNode stringIndexes = new XMLNode(STRING_INDEXES);
        for (int i = 1; i <= 5; i++) {
            final XMLNode xmlNode = new XMLNode(STRING_INDEX);
            xmlNode.addAttribute(INDEX, String.valueOf(i));
            xmlNode.addAttribute(LABEL, definition.getStringIndexLabel(i));
            final SExpression stringIndexValue = definition.getStringIndexValue(i);
            if (stringIndexValue != null) {
                final XMLNode value = new XMLNode(EXPRESSION_NODE);
                fillExpressionNode(value, stringIndexValue);
                xmlNode.addChild(value);
            }
            stringIndexes.addChild(xmlNode);
        }
        rootNode.addChild(stringIndexes);
        rootNode.addAttribute("xmlns", NAMESPACE);

        createAndFillFlowElements(rootNode, definition.getProcessContainer());

        final XMLNode dependencies = new XMLNode(DEPENDENCIES_NODE);
        rootNode.addChild(dependencies);

        final XMLNode parameters = new XMLNode(PARAMETERS_NODE);
        dependencies.addChild(parameters);
        for (final SParameterDefinition parameter : definition.getParameters()) {
            final XMLNode parameterNode = new XMLNode(PARAMETER_NODE);
            fillParameterNode(parameterNode, parameter);
            parameters.addChild(parameterNode);
        }

        final XMLNode actors = new XMLNode(ACTORS_NODE);
        dependencies.addChild(actors);
        for (final SActorDefinition actor : definition.getActors()) {
            final XMLNode actorNode = new XMLNode(ACTOR_NODE);
            fillActorNode(actorNode, actor);
            actors.addChild(actorNode);
        }

        final SActorDefinition actorInitiator = definition.getActorInitiator();
        if (actorInitiator != null) {
            final XMLNode initiatorNode = new XMLNode(INITIATOR_NODE);
            rootNode.addChild(initiatorNode);
            fillActorNode(initiatorNode, actorInitiator);
        }
        final SContractDefinition contract = definition.getContract();
        if (contract != null) {
            rootNode.addChild(createContractNode(contract));
        }
        rootNode.addChild(createContextNode(definition.getContext()));
        return rootNode;
    }

    private void createAndFillFlowElements(final XMLNode containerNode, final SFlowElementContainerDefinition container) {
        final XMLNode flowElements = new XMLNode(FLOW_ELEMENTS_NODE);
        containerNode.addChild(flowElements);
        final XMLNode transitionsNode = new XMLNode(TRANSITIONS_NODE);
        flowElements.addChild(transitionsNode);

        for (final STransitionDefinition transition : container.getTransitions()) {
            final XMLNode transitionNode = new XMLNode(TRANSITION_NODE);
            final String id = String.valueOf(Math.abs(UUID.randomUUID().getMostSignificantBits()));
            objectToId.put(transition, id);
            transitionNode.addAttribute(ID, id);
            fillTransitionNode(transitionNode, transition);
            transitionsNode.addChild(transitionNode);
        }

        final XMLNode connectorsNode = new XMLNode(CONNECTORS_NODE);
        flowElements.addChild(connectorsNode);
        for (final SConnectorDefinition connector : container.getConnectors()) {
            final XMLNode connectorNode = new XMLNode(CONNECTOR_NODE);
            fillConnectorNode(connectorNode, connector);
            connectorsNode.addChild(connectorNode);
        }

        addBusinessDataDefinitionNodes(container.getBusinessDataDefinitions(), flowElements);

        final XMLNode dataDefinitionsNode = new XMLNode(DATA_DEFINITIONS_NODE);
        flowElements.addChild(dataDefinitionsNode);
        for (final SDataDefinition dataDefinition : container.getDataDefinitions()) {
            final XMLNode dataDefinitionNode = getDataDefinitionNode(dataDefinition);
            dataDefinitionsNode.addChild(dataDefinitionNode);
        }
        final XMLNode documentDefinitionsNode = new XMLNode(DOCUMENT_DEFINITIONS_NODE);
        flowElements.addChild(documentDefinitionsNode);
        for (final SDocumentDefinition document : container.getDocumentDefinitions()) {
            final XMLNode documentDefinitionNode = new XMLNode(DOCUMENT_DEFINITION_NODE);
            fillDocumentDefinitionNode(documentDefinitionNode, document);
            documentDefinitionsNode.addChild(documentDefinitionNode);
        }
        final XMLNode documentListDefinitionsNode = new XMLNode(DOCUMENT_LIST_DEFINITIONS_NODE);
        flowElements.addChild(documentListDefinitionsNode);
        for (final SDocumentListDefinition documentList : container.getDocumentListDefinitions()) {
            final XMLNode documentListDefinitionNode = new XMLNode(DOCUMENT_LIST_DEFINITION_NODE);
            fillDocumentListDefinitionNode(documentListDefinitionNode, documentList);
            documentListDefinitionsNode.addChild(documentListDefinitionNode);
        }

        createAndFillFlowNodes(container, flowElements);
    }

    protected XMLNode createBusinessDataDefinitionNode(final SBusinessDataDefinition businessDataDefinition) {
        final XMLNode businessDataDefinitionNode = new XMLNode(BUSINESS_DATA_DEFINITION_NODE);
        businessDataDefinitionNode.addAttribute(NAME, businessDataDefinition.getName());
        businessDataDefinitionNode.addAttribute(BUSINESS_DATA_DEFINITION_CLASS, businessDataDefinition.getClassName());
        businessDataDefinitionNode.addAttribute(BUSINESS_DATA_DEFINITION_IS_MULTIPLE, String.valueOf(businessDataDefinition.isMultiple()));
        if (businessDataDefinition.getDescription() != null) {
            businessDataDefinitionNode.addChild(DESCRIPTION, businessDataDefinition.getDescription());
        }
        SExpression defaultValueExpression;
        if ((defaultValueExpression = businessDataDefinition.getDefaultValueExpression()) != null) {
            final XMLNode defaultValueNode = new XMLNode(DEFAULT_VALUE_NODE);
            fillExpressionNode(defaultValueNode, defaultValueExpression);
            businessDataDefinitionNode.addChild(defaultValueNode);
        }
        return businessDataDefinitionNode;
    }

    private void createAndFillFlowNodes(final SFlowElementContainerDefinition container, final XMLNode flowElements) {
        final XMLNode flowNodes = new XMLNode(FLOW_NODES_NODE);
        flowElements.addChild(flowNodes);
        for (final SActivityDefinition activity : container.getActivities()) {
            XMLNode activityNode;
            if (activity instanceof SCallActivityDefinition) {
                activityNode = new XMLNode(CALL_ACTIVITY_NODE);
            } else if (activity instanceof SAutomaticTaskDefinition) {
                activityNode = new XMLNode(AUTOMATIC_TASK_NODE);
            } else if (activity instanceof SReceiveTaskDefinition) {
                activityNode = new XMLNode(RECEIVE_TASK_NODE);
            } else if (activity instanceof SSendTaskDefinition) {
                activityNode = new XMLNode(SEND_TASK_NODE);
            } else if (activity instanceof SSubProcessDefinition) {
                activityNode = new XMLNode(SUB_PROCESS);
            } else {
                if (activity instanceof SManualTaskDefinition) {
                    activityNode = new XMLNode(MANUAL_TASK_NODE);
                } else {
                    activityNode = new XMLNode(USER_TASK_NODE);
                }
                fillHumanTask(activity, activityNode);
            }
            fillFlowNode(activityNode, activity);
            addDataDefinitionNodes(activity, activityNode);
            addBusinessDataDefinitionNodes(activity.getBusinessDataDefinitions(), activityNode);
            addOperationNodes(activity, activityNode);
            addLoopCharacteristics(activity, activityNode);
            addBoundaryEventDefinitionsNode(activity, activityNode);
            if (activity instanceof SHumanTaskDefinition) {
                final SHumanTaskDefinition humanTaskDefinition = (SHumanTaskDefinition) activity;
                if (humanTaskDefinition.getSUserFilterDefinition() != null) {
                    final XMLNode userFilterNode = new XMLNode(USER_FILTER_NODE);
                    fillUserFilterNode(userFilterNode, humanTaskDefinition.getSUserFilterDefinition());
                    activityNode.addChild(userFilterNode);
                }
                if (humanTaskDefinition instanceof SUserTaskDefinition) {
                    if (((SUserTaskDefinition) humanTaskDefinition).getContract() != null) {
                        activityNode.addChild(createContractNode(((SUserTaskDefinition) humanTaskDefinition).getContract()));
                    }
                    activityNode.addChild(createContextNode(((SUserTaskDefinition) humanTaskDefinition).getContext()));
                }
            } else if (activity instanceof SCallActivityDefinition) {
                fillCallActivity((SCallActivityDefinition) activity, activityNode);
            } else if (activity instanceof SSubProcessDefinition) {
                fillSubProcess((SSubProcessDefinition) activity, activityNode);
            } else if (activity instanceof SReceiveTaskDefinition) {
                fillReceiveTask((SReceiveTaskDefinition) activity, activityNode);
            } else if (activity instanceof SSendTaskDefinition) {
                fillSendTask((SSendTaskDefinition) activity, activityNode);
            }

            flowNodes.addChild(activityNode);
        }
        for (final SGatewayDefinition gateway : container.getGateways()) {
            final XMLNode gatewayNode = new XMLNode(GATEWAY_NODE);
            fillGatewayNode(gatewayNode, gateway);
            flowNodes.addChild(gatewayNode);
        }

        createAndfillStartEvents(container.getStartEvents(), flowNodes);
        createAndfillIntermediateCatchEvents(container.getIntermediateCatchEvents(), flowNodes);
        createAndfillIntermediatThrowEvents(container.getIntermdiateThrowEvents(), flowNodes);
        createAndfillEndEvents(container.getEndEvents(), flowNodes);
    }

    private XMLNode createContractNode(final SContractDefinition contract) {
        final XMLNode contractNode = new XMLNode(CONTRACT_NODE);
        final XMLNode inputsNode = new XMLNode(CONTRACT_INPUTS_NODE);
        for (final SInputDefinition input : contract.getInputDefinitions()) {
            inputsNode.addChild(createInputNode(input));
        }
        if (!inputsNode.getChildNodes().isEmpty()) {
            contractNode.addChild(inputsNode);
        }
        final List<SConstraintDefinition> constraints = contract.getConstraints();
        if (!constraints.isEmpty()) {
            final XMLNode rulesNode = new XMLNode(CONTRACT_CONSTRAINTS_NODE);
            contractNode.addChild(rulesNode);
            for (final SConstraintDefinition constraint : constraints) {
                rulesNode.addChild(createContractConstraintNode(constraint));
            }
        }
        return contractNode;
    }

    private XMLNode createContextNode(final List<SContextEntry> context) {
        final XMLNode contextNode = new XMLNode(CONTEXT_NODE);
        for (final SContextEntry contextEntry : context) {
            final XMLNode node = new XMLNode(CONTEXT_ENTRY_NODE);
            node.addAttribute(CONTEXT_ENTRY_KEY, contextEntry.getKey());
            addExpressionNode(node, EXPRESSION_NODE, contextEntry.getExpression());
            contextNode.addChild(node);
        }
        return contextNode;
    }

    private XMLNode createContractConstraintNode(final SConstraintDefinition constraintDefinition) {
        final XMLNode constraintNode = new XMLNode(CONTRACT_CONSTRAINT_NODE);
        constraintNode.addAttribute(NAME, constraintDefinition.getName());
        constraintNode.addChild(CONSTRAINT_EXPRESSION, constraintDefinition.getExpression());
        constraintNode.addChild(CONSTRAINT_EXPLANATION, constraintDefinition.getExplanation());
        final XMLNode namesNode = new XMLNode(INPUT_NAMES);
        constraintNode.addChild(namesNode);
        for (final String inputName : constraintDefinition.getInputNames()) {
            namesNode.addChild(INPUT_NAME, inputName);
        }
        return constraintNode;
    }

    private XMLNode createInputNode(final SInputDefinition input) {
        final XMLNode inputNode = new XMLNode(CONTRACT_INPUT_NODE);
        inputNode.addAttribute(NAME, input.getName());
        inputNode.addAttribute(MULTIPLE, input.isMultiple());
        final SType type = input.getType();
        if (type != null) {
            inputNode.addAttribute(TYPE, type.toString());
        }
        inputNode.addAttribute(DESCRIPTION, input.getDescription());
        for (final SInputDefinition sInputDefinition : input.getInputDefinitions()) {
            inputNode.addChild(createInputNode(sInputDefinition));
        }
        return inputNode;
    }

    private void addBusinessDataDefinitionNodes(final List<SBusinessDataDefinition> businessDataDefinitions, final XMLNode containerNode) {
        if (!businessDataDefinitions.isEmpty()) {
            final XMLNode businessDataDefinitionsNode = new XMLNode(BUSINESS_DATA_DEFINITIONS_NODE);
            containerNode.addChild(businessDataDefinitionsNode);
            for (final SBusinessDataDefinition businessDataDefinition : businessDataDefinitions) {
                final XMLNode businessDataDefinitionNode = createBusinessDataDefinitionNode(businessDataDefinition);
                businessDataDefinitionsNode.addChild(businessDataDefinitionNode);
            }
        }
    }

    private void addBoundaryEventDefinitionsNode(final SActivityDefinition activity, final XMLNode activityNode) {
        final XMLNode boundaryEventsNode = new XMLNode(BOUNDARY_EVENTS_NODE);
        activityNode.addChild(boundaryEventsNode);
        addBoundaryEventDefinitionNodes(activity, boundaryEventsNode);
    }

    private void addBoundaryEventDefinitionNodes(final SActivityDefinition activity, final XMLNode boundaryEventsNode) {
        for (final SBoundaryEventDefinition boundaryEvent : activity.getBoundaryEventDefinitions()) {
            final XMLNode boundaryEventNode = new XMLNode(BOUNDARY_EVENT_NODE);
            fillFlowNode(boundaryEventNode, boundaryEvent);
            fillCatchEventDefinition(boundaryEventNode, boundaryEvent);
            boundaryEventsNode.addChild(boundaryEventNode);
        }
    }

    private void fillSubProcess(final SSubProcessDefinition activity, final XMLNode activityNode) {
        activityNode.addAttribute(TRIGGERED_BY_EVENT, String.valueOf(activity.isTriggeredByEvent()));
        createAndFillFlowElements(activityNode, activity.getSubProcessContainer());
    }

    private void fillCallActivity(final SCallActivityDefinition activity, final XMLNode activityNode) {
        addExpressionNode(activityNode, CALLABLE_ELEMENT_NODE, activity.getCallableElement());
        addExpressionNode(activityNode, CALLABLE_ELEMENT_VERSION_NODE, activity.getCallableElementVersion());
        createAndfillOperations(activityNode, activity.getDataInputOperations(), DATA_INPUT_OPERATION_NODE);
        createAndfillOperations(activityNode, activity.getDataOutputOperations(), DATA_OUTPUT_OPERATION_NODE);
        activityNode.addAttribute(CALLABLE_ELEMENT_TYPE, activity.getCallableElementType().name());
    }

    private void fillReceiveTask(final SReceiveTaskDefinition receiveTask, final XMLNode activityNode) {
        createAndfillMessageEventTriggers(activityNode, receiveTask.getTrigger());
    }

    private void fillSendTask(final SSendTaskDefinition sendTask, final XMLNode activityNode) {
        createAndfillMessageEventTrigger(activityNode, sendTask.getMessageTrigger());
    }

    private void fillHumanTask(final SActivityDefinition activity, final XMLNode activityNode) {
        final SHumanTaskDefinition humanTaskDefinition = (SHumanTaskDefinition) activity;
        final String actorName = humanTaskDefinition.getActorName();
        activityNode.addAttribute(ACTOR_NAME, actorName);
        final String priority = humanTaskDefinition.getPriority();
        activityNode.addAttribute(PRIORITY, priority);
        final Long expectedDuration = humanTaskDefinition.getExpectedDuration();
        if (expectedDuration != null) {
            activityNode.addAttribute(EXPECTED_DURATION, String.valueOf(expectedDuration));
        }
    }

    private void createAndfillStartEvents(final List<SStartEventDefinition> startEvents, final XMLNode flowNodes) {
        for (final SStartEventDefinition startEvent : startEvents) {
            final XMLNode startEventNode = new XMLNode(START_EVENT_NODE);
            fillFlowNode(startEventNode, startEvent);
            fillCatchEventDefinition(startEventNode, startEvent);
            flowNodes.addChild(startEventNode);
        }
    }

    private void createAndfillEndEvents(final List<SEndEventDefinition> endEvents, final XMLNode flowNodes) {
        for (final SEndEventDefinition endEvent : endEvents) {
            final XMLNode endEventNode = new XMLNode(END_EVENT_NODE);
            fillFlowNode(endEventNode, endEvent);
            fillThrowEventDefinition(endEventNode, endEvent);
            createAndFillTerminateEventTrigger(endEventNode, endEvent);
            createAndfillErrorEventTriggers(endEventNode, endEvent);
            flowNodes.addChild(endEventNode);
        }
    }

    private void createAndFillTerminateEventTrigger(final XMLNode eventNode, final SEndEventDefinition event) {
        final STerminateEventTriggerDefinition terminateEventTriggerDefinition = event.getTerminateEventTriggerDefinition();
        if (terminateEventTriggerDefinition != null) {
            final XMLNode terminateEventTriggerNode = new XMLNode(TERMINATE_EVENT_TRIGGER_NODE);
            eventNode.addChild(terminateEventTriggerNode);
        }
    }

    private void createAndfillIntermediatThrowEvents(final List<SIntermediateThrowEventDefinition> throwEvents, final XMLNode flowNodes) {
        for (final SIntermediateThrowEventDefinition event : throwEvents) {
            final XMLNode eventNode = new XMLNode(INTERMEDIATE_THROW_EVENT_NODE);
            fillFlowNode(eventNode, event);
            fillThrowEventDefinition(eventNode, event);
            flowNodes.addChild(eventNode);
        }
    }

    private void fillThrowEventDefinition(final XMLNode eventNode, final SThrowEventDefinition event) {
        createAndfillMessageEventTriggers(eventNode, event);
        createAndfillSignalEventTriggers(eventNode, event);
    }

    private void createAndfillSignalEventTriggers(final XMLNode eventNode, final SThrowEventDefinition event) {
        for (final SThrowSignalEventTriggerDefinition signalEventTrigger : event.getSignalEventTriggerDefinitions()) {
            final XMLNode signalEventTriggerNode = new XMLNode(THROW_SIGNAL_EVENT_TRIGGER_NODE);
            signalEventTriggerNode.addAttribute(NAME, signalEventTrigger.getSignalName());
            eventNode.addChild(signalEventTriggerNode);
        }
    }

    private void createAndfillErrorEventTriggers(final XMLNode eventNode, final SEndEventDefinition event) {
        for (final SThrowErrorEventTriggerDefinition errorEventTrigger : event.getErrorEventTriggerDefinitions()) {
            final XMLNode errorEventTriggerNode = new XMLNode(THROW_ERROR_EVENT_TRIGGER_NODE);
            errorEventTriggerNode.addAttribute(ERROR_CODE, errorEventTrigger.getErrorCode());
            eventNode.addChild(errorEventTriggerNode);
        }
    }

    private void createAndfillMessageEventTriggers(final XMLNode eventNode, final SThrowEventDefinition event) {
        for (final SThrowMessageEventTriggerDefinition messageEventTrigger : event.getMessageEventTriggerDefinitions()) {
            createAndfillMessageEventTrigger(eventNode, messageEventTrigger);
        }
    }

    private void createAndfillMessageEventTrigger(final XMLNode eventNode, final SThrowMessageEventTriggerDefinition messageEventTrigger) {
        final XMLNode messageEventTriggerNode = new XMLNode(THROW_MESSAGE_EVENT_TRIGGER_NODE);
        messageEventTriggerNode.addAttribute(NAME, messageEventTrigger.getMessageName());
        addExpressionNode(messageEventTriggerNode, TARGET_PROCESS, messageEventTrigger.getTargetProcess());
        final SExpression targetFlowNode = messageEventTrigger.getTargetFlowNode();
        if (targetFlowNode != null) {
            addExpressionNode(messageEventTriggerNode, TARGET_FLOW_NODE, targetFlowNode);
        }
        createAndFillDataDefinitions(messageEventTriggerNode, messageEventTrigger);
        createAndfillCorrelations(messageEventTriggerNode, messageEventTrigger.getCorrelations());
        eventNode.addChild(messageEventTriggerNode);
    }

    private void createAndFillDataDefinitions(final XMLNode messageEventTriggerNode, final SThrowMessageEventTriggerDefinition messageEventTrigger) {
        for (final SDataDefinition dataDefinition : messageEventTrigger.getDataDefinitions()) {
            final XMLNode dataDefinitionNode = getDataDefinitionNode(dataDefinition);
            messageEventTriggerNode.addChild(dataDefinitionNode);
        }
    }

    private void addDataDefinitionNodes(final SActivityDefinition activity, final XMLNode activityNode) {
        final XMLNode dataDefinitionsNodeFromActivity = new XMLNode(DATA_DEFINITIONS_NODE);
        activityNode.addChild(dataDefinitionsNodeFromActivity);
        for (final SDataDefinition dataDefinition : activity.getSDataDefinitions()) {
            final XMLNode dataDefinitionNode = getDataDefinitionNode(dataDefinition);
            dataDefinitionsNodeFromActivity.addChild(dataDefinitionNode);
        }
    }

    private void addOperationNodes(final SActivityDefinition activity, final XMLNode activityNode) {
        final XMLNode operationsNode = new XMLNode(OPERATIONS_NODE);
        activityNode.addChild(operationsNode);
        createAndfillOperations(operationsNode, activity.getSOperations(), OPERATION_NODE);
    }

    private void addLoopCharacteristics(final SActivityDefinition activity, final XMLNode activityNode) {
        final SLoopCharacteristics loopCharacteristics = activity.getLoopCharacteristics();
        if (loopCharacteristics != null) {
            final XMLNode loopNode;
            if (loopCharacteristics instanceof SStandardLoopCharacteristicsImpl) {
                loopNode = new XMLNode(STANDARD_LOOP_CHARACTERISTICS_NODE);
                final SStandardLoopCharacteristicsImpl loop = (SStandardLoopCharacteristicsImpl) loopCharacteristics;
                loopNode.addAttribute(TEST_BEFORE, String.valueOf(loop.isTestBefore()));
                final XMLNode expressionNode = new XMLNode(LOOP_CONDITION);
                fillExpressionNode(expressionNode, loop.getLoopCondition());
                loopNode.addChild(expressionNode);
                final SExpression loopMax = loop.getLoopMax();
                if (loopMax != null) {
                    final XMLNode loopMaxNode = new XMLNode(LOOP_MAX);
                    fillExpressionNode(loopMaxNode, loopMax);
                    loopNode.addChild(loopMaxNode);
                }
            } else {
                loopNode = new XMLNode(MULTI_INSTANCE_LOOP_CHARACTERISTICS_NODE);
                final SMultiInstanceLoopCharacteristics loop = (SMultiInstanceLoopCharacteristics) loopCharacteristics;
                loopNode.addAttribute(MULTI_INSTANCE_IS_SEQUENTIAL, String.valueOf(loop.isSequential()));
                loopNode.addAttribute(MULTI_INSTANCE_DATA_INPUT_ITEM_REF, loop.getDataInputItemRef());
                loopNode.addAttribute(MULTI_INSTANCE_DATA_OUTPUT_ITEM_REF, loop.getDataOutputItemRef());
                loopNode.addAttribute(MULTI_INSTANCE_LOOP_DATA_INPUT, loop.getLoopDataInputRef());
                loopNode.addAttribute(MULTI_INSTANCE_LOOP_DATA_OUTPUT, loop.getLoopDataOutputRef());
                final SExpression loopCardinality = loop.getLoopCardinality();
                if (loopCardinality != null) {
                    final XMLNode expressionNode = new XMLNode(MULTI_INSTANCE_LOOP_CARDINALITY);
                    fillExpressionNode(expressionNode, loopCardinality);
                    loopNode.addChild(expressionNode);
                }
                final SExpression completionCondition = loop.getCompletionCondition();
                if (completionCondition != null) {
                    final XMLNode expressionNode = new XMLNode(MULTI_INSTANCE_COMPLETION_CONDITION);
                    fillExpressionNode(expressionNode, completionCondition);
                    loopNode.addChild(expressionNode);
                }

            }
            activityNode.addChild(loopNode);
        }
    }

    private void createAndfillIntermediateCatchEvents(final List<SIntermediateCatchEventDefinition> catchEvents, final XMLNode flowNodes) {
        for (final SIntermediateCatchEventDefinition intermediateCatchEvent : catchEvents) {
            final XMLNode intermediateCatchEventNode = new XMLNode(INTERMEDIATE_CATCH_EVENT_NODE);
            fillFlowNode(intermediateCatchEventNode, intermediateCatchEvent);
            fillCatchEventDefinition(intermediateCatchEventNode, intermediateCatchEvent);
            flowNodes.addChild(intermediateCatchEventNode);
        }
    }

    private void fillCatchEventDefinition(final XMLNode catchEventNode, final SCatchEventDefinition catchEvent) {
        createAndfillTimerEventTriggers(catchEventNode, catchEvent);
        createAndfillMessageEventTriggers(catchEventNode, catchEvent);
        createAndfillSignalEventTriggers(catchEventNode, catchEvent);
        createAndfillErrorEventTriggers(catchEventNode, catchEvent);
        catchEventNode.addAttribute(INTERRUPTING, String.valueOf(catchEvent.isInterrupting()));
    }

    private void createAndfillSignalEventTriggers(final XMLNode catchEventNode, final SCatchEventDefinition catchEvent) {
        for (final SCatchSignalEventTriggerDefinition signalEventTrigger : catchEvent.getSignalEventTriggerDefinitions()) {
            final XMLNode signalEventTriggerNode = new XMLNode(CATCH_SIGNAL_EVENT_TRIGGER_NODE);
            signalEventTriggerNode.addAttribute(NAME, signalEventTrigger.getSignalName());
            catchEventNode.addChild(signalEventTriggerNode);
        }
    }

    private void createAndfillErrorEventTriggers(final XMLNode catchEventNode, final SCatchEventDefinition catchEvent) {
        for (final SCatchErrorEventTriggerDefinition errorEventTrigger : catchEvent.getErrorEventTriggerDefinitions()) {
            final XMLNode errorEventTriggerNode = new XMLNode(CATCH_ERROR_EVENT_TRIGGER_NODE);
            errorEventTriggerNode.addAttribute(ERROR_CODE, errorEventTrigger.getErrorCode());
            catchEventNode.addChild(errorEventTriggerNode);
        }
    }

    private void createAndfillMessageEventTriggers(final XMLNode catchEventNode, final SCatchEventDefinition catchEvent) {
        for (final SCatchMessageEventTriggerDefinition messageEventTrigger : catchEvent.getMessageEventTriggerDefinitions()) {
            createAndfillMessageEventTriggers(catchEventNode, messageEventTrigger);
        }
    }

    private void createAndfillMessageEventTriggers(final XMLNode activityNode, final SCatchMessageEventTriggerDefinition trigger) {
        final XMLNode messageEventTriggerNode = new XMLNode(CATCH_MESSAGE_EVENT_TRIGGER_NODE);
        messageEventTriggerNode.addAttribute(NAME, trigger.getMessageName());
        createAndfillCorrelations(messageEventTriggerNode, trigger.getCorrelations());
        createAndfillOperations(messageEventTriggerNode, trigger.getOperations(), OPERATION_NODE);
        activityNode.addChild(messageEventTriggerNode);
    }

    private void createAndfillOperations(final XMLNode parentNode, final List<SOperation> list, final String operationNodeName) {
        for (final SOperation operation : list) {
            createAndFillOperation(parentNode, operation, operationNodeName);
        }
    }

    private void createAndFillOperation(final XMLNode parentNode, final SOperation operation, final String operationNodeName) {
        final XMLNode operationNode = new XMLNode(operationNodeName);
        operationNode.addAttribute(OPERATION_OPERATOR_TYPE, operation.getType().name());
        operationNode.addAttribute(OPERATION_OPERATOR, operation.getOperator());
        final XMLNode leftOperand = new XMLNode(OPERATION_LEFT_OPERAND);
        fillLeftOperandNode(leftOperand, operation.getLeftOperand());
        operationNode.addChild(leftOperand);
        final SExpression rightOperandExpression = operation.getRightOperand();
        if (rightOperandExpression != null) {
            final XMLNode expressionNode = new XMLNode(OPERATION_RIGHT_OPERAND);
            fillExpressionNode(expressionNode, rightOperandExpression);
            operationNode.addChild(expressionNode);
        }
        parentNode.addChild(operationNode);
    }

    private void fillConnectorNode(final XMLNode connectorNode, final SConnectorDefinition connector) {
        // connectorNode.addAttribute(ID, String.valueOf(connector.getId())); TODO : Uncomment when generate id
        connectorNode.addAttribute(NAME, connector.getName());
        connectorNode.addAttribute(CONNECTOR_ID, connector.getConnectorId());
        connectorNode.addAttribute(CONNECTOR_VERSION, connector.getVersion());
        connectorNode.addAttribute(CONNECTOR_ACTIVATION_EVENT, connector.getActivationEvent().toString());
        connectorNode.addAttribute(CONNECTOR_FAIL_ACTION, connector.getFailAction().toString());
        if (connector.getFailAction() == FailAction.ERROR_EVENT) {
            connectorNode.addAttribute(CONNECTOR_ERROR_CODE, connector.getErrorCode());
        }
        final Map<String, SExpression> inputs = connector.getInputs();
        createAndFillInputsNode(connectorNode, inputs);
        final XMLNode outputsNode = new XMLNode(CONNECTOR_OUTPUTS_NODE);
        createAndfillOperations(outputsNode, connector.getOutputs(), OPERATION_NODE);
        connectorNode.addChild(outputsNode);
    }

    private void fillUserFilterNode(final XMLNode userFilterNode, final SUserFilterDefinition userFilter) {
        userFilterNode.addAttribute(NAME, userFilter.getName());
        userFilterNode.addAttribute(USER_FILTER_ID, userFilter.getUserFilterId());
        userFilterNode.addAttribute(CONNECTOR_VERSION, userFilter.getVersion());
        final Map<String, SExpression> inputs = userFilter.getInputs();
        createAndFillInputsNode(userFilterNode, inputs);
    }

    private void createAndFillInputsNode(final XMLNode connectorNode, final Map<String, SExpression> inputs) {
        final XMLNode inputsNode = new XMLNode(CONNECTOR_INPUTS_NODE);
        for (final Entry<String, SExpression> input : inputs.entrySet()) {
            if (input.getValue() != null) {
                final XMLNode inputNode = new XMLNode(CONNECTOR_INPUT);
                inputNode.addAttribute(NAME, input.getKey());
                final XMLNode expressionNode = new XMLNode(EXPRESSION_NODE);
                fillExpressionNode(expressionNode, input.getValue());
                inputNode.addChild(expressionNode);
                inputsNode.addChild(inputNode);
            }
        }
        connectorNode.addChild(inputsNode);
    }

    private void fillLeftOperandNode(final XMLNode rightOperandNode, final SLeftOperand sLeftOperand) {
        rightOperandNode.addAttribute(LEFT_OPERAND_NAME, sLeftOperand.getName());
        rightOperandNode.addAttribute(LEFT_OPERAND_TYPE, sLeftOperand.getType());
    }

    private void createAndfillCorrelations(final XMLNode messageEventTriggerNode, final List<SCorrelationDefinition> correlations) {
        for (final SCorrelationDefinition correlation : correlations) {
            final XMLNode correlationNode = new XMLNode(CORRELATION_NODE);
            final XMLNode keyNode = new XMLNode(CORRELATION_KEY);
            fillExpressionNode(keyNode, correlation.getKey());
            correlationNode.addChild(keyNode);
            final XMLNode valueNode = new XMLNode(CORRELATION_VALUE);
            fillExpressionNode(valueNode, correlation.getValue());
            correlationNode.addChild(valueNode);
            messageEventTriggerNode.addChild(correlationNode);
        }
    }

    private void createAndfillTimerEventTriggers(final XMLNode catchEventNode, final SCatchEventDefinition catchEvent) {
        for (final STimerEventTriggerDefinition timerEventTrigger : catchEvent.getTimerEventTriggerDefinitions()) {
            final XMLNode timerEventTriggerNode = new XMLNode(TIMER_EVENT_TRIGGER_NODE);
            timerEventTriggerNode.addAttribute(TIMER_EVENT_TRIGGER_TIMER_TYPE, timerEventTrigger.getTimerType().toString());
            final XMLNode expression = new XMLNode(EXPRESSION_NODE);
            fillExpressionNode(expression, timerEventTrigger.getTimerExpression());
            timerEventTriggerNode.addChild(expression);
            catchEventNode.addChild(timerEventTriggerNode);
        }
    }

    private XMLNode getDataDefinitionNode(final SDataDefinition dataDefinition) {
        XMLNode dataDefinitionNode = null;
        if (dataDefinition instanceof SXMLDataDefinition) {
            dataDefinitionNode = new XMLNode(XML_DATA_DEFINITION_NODE);
            fillDataDefinitionNode(dataDefinitionNode, dataDefinition);
            final SXMLDataDefinition xmlData = (SXMLDataDefinition) dataDefinition;
            final String namespace = xmlData.getNamespace();
            if (namespace != null) {
                dataDefinitionNode.addChild(XML_DATA_DEFINITION_NAMESPACE, namespace);
            }
            final String element = xmlData.getElement();
            if (element != null) {
                dataDefinitionNode.addChild(XML_DATA_DEFINITION_ELEMENT, element);
            }
        } else if (dataDefinition instanceof STextDataDefinition) {
            dataDefinitionNode = new XMLNode(TEXT_DATA_DEFINITION_NODE);
            dataDefinitionNode.addAttribute(TEXT_DATA_DEFINITION_LONG, String.valueOf(((STextDataDefinition) dataDefinition).isLongText()));
            fillDataDefinitionNode(dataDefinitionNode, dataDefinition);
        } else {
            dataDefinitionNode = new XMLNode(DATA_DEFINITION_NODE);
            fillDataDefinitionNode(dataDefinitionNode, dataDefinition);
        }
        return dataDefinitionNode;
    }

    private void fillDataDefinitionNode(final XMLNode dataDefinitionNode, final SDataDefinition dataDefinition) {
        dataDefinitionNode.addAttribute(NAME, dataDefinition.getName());
        dataDefinitionNode.addAttribute(DATA_DEFINITION_CLASS, dataDefinition.getClassName());
        dataDefinitionNode.addAttribute(DATA_DEFINITION_TRANSIENT, String.valueOf(dataDefinition.isTransientData()));
        if (dataDefinition.getDescription() != null) {
            dataDefinitionNode.addChild(DESCRIPTION, dataDefinition.getDescription());
        }
        SExpression defaultValueExpression;
        if ((defaultValueExpression = dataDefinition.getDefaultValueExpression()) != null) {
            final XMLNode defaultValueNode = new XMLNode(DEFAULT_VALUE_NODE);
            fillExpressionNode(defaultValueNode, defaultValueExpression);
            dataDefinitionNode.addChild(defaultValueNode);
        }
    }

    private void fillDocumentDefinitionNode(final XMLNode documentDefinitionNode, final SDocumentDefinition documentDefinition) {
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

    private void fillDocumentListDefinitionNode(final XMLNode documentListDefinitionNode, final SDocumentListDefinition documentListDefinition) {
        documentListDefinitionNode.addAttribute(NAME, documentListDefinition.getName());
        if (documentListDefinition.getDescription() != null) {
            documentListDefinitionNode.addChild(DESCRIPTION, documentListDefinition.getDescription());
        }
        if (documentListDefinition.getExpression() != null) {
            final XMLNode value = new XMLNode(EXPRESSION_NODE);
            fillExpressionNode(value, documentListDefinition.getExpression());
            documentListDefinitionNode.addChild(value);
        }
    }

    private void fillActorNode(final XMLNode actorNode, final SActorDefinition actor) {
        actorNode.addAttribute(NAME, actor.getName());
        actorNode.addChild(DESCRIPTION, actor.getDescription());
    }

    private void fillParameterNode(final XMLNode parameterNode, final SParameterDefinition parameter) {
        parameterNode.addAttribute(NAME, parameter.getName());
        parameterNode.addAttribute(PARAMETER_TYPE, parameter.getType());
        if (parameter.getDescription() != null) {
            parameterNode.addChild(DESCRIPTION, parameter.getDescription());
        }
    }

    private void fillGatewayNode(final XMLNode gatewayNode, final SGatewayDefinition gateway) {
        fillFlowNode(gatewayNode, gateway);
        gatewayNode.addAttribute(GATEWAY_TYPE, gateway.getGatewayType().toString());
    }

    private void fillFlowNode(final XMLNode node, final SFlowNodeDefinition flowNode) {
        node.addAttribute(ID, String.valueOf(flowNode.getId()));
        node.addAttribute(NAME, flowNode.getName());
        node.addAttribute(DESCRIPTION, flowNode.getDescription());
        addExpressionNode(node, DISPLAY_NAME, flowNode.getDisplayName());
        addExpressionNode(node, DISPLAY_DESCRIPTION, flowNode.getDisplayDescription());
        addExpressionNode(node, DISPLAY_DESCRIPTION_AFTER_COMPLETION, flowNode.getDisplayDescriptionAfterCompletion());
        for (final STransitionDefinition transition : flowNode.getIncomingTransitions()) {
            final XMLNode transitionNode = new XMLNode(INCOMING_TRANSITION);
            fillTransitionRefNode(transitionNode, transition);
            node.addChild(transitionNode);
        }
        for (final STransitionDefinition transition : flowNode.getOutgoingTransitions()) {
            final XMLNode transitionNode = new XMLNode(OUTGOING_TRANSITION);
            fillTransitionRefNode(transitionNode, transition);
            node.addChild(transitionNode);
        }
        if (flowNode.getDefaultTransition() != null) {
            final XMLNode defaultTransition = new XMLNode(DEFAULT_TRANSITION);
            fillTransitionRefNode(defaultTransition, flowNode.getDefaultTransition());
            node.addChild(defaultTransition);
        }
        for (final SConnectorDefinition connector : flowNode.getConnectors()) {
            final XMLNode connectorNode = new XMLNode(CONNECTOR_NODE);
            fillConnectorNode(connectorNode, connector);
            node.addChild(connectorNode);
        }
    }

    private void addExpressionNode(final XMLNode node, final String expressionNodeName, final SExpression expression) {
        if (expression != null) {
            final XMLNode xmlNode = new XMLNode(expressionNodeName);
            fillExpressionNode(xmlNode, expression);
            node.addChild(xmlNode);
        }
    }

    private void fillTransitionRefNode(final XMLNode transitionNode, final STransitionDefinition transition) {
        transitionNode.addAttribute(IDREF, objectToId.get(transition));
    }

    private void fillExpressionNode(final XMLNode expressionNode, final SExpression sExpression) {
        expressionNode.addAttribute(NAME, sExpression.getName());
        expressionNode.addAttribute(EXPRESSION_TYPE, sExpression.getExpressionType());
        expressionNode.addAttribute(EXPRESSION_RETURN_TYPE, sExpression.getReturnType());
        expressionNode.addAttribute(EXPRESSION_INTERPRETER, sExpression.getInterpreter());
        expressionNode.addChild(EXPRESSION_CONTENT, sExpression.getContent());
        for (final SExpression dependency : sExpression.getDependencies()) {
            final XMLNode dependencyExpression = new XMLNode(EXPRESSION_NODE);
            fillExpressionNode(dependencyExpression, dependency);
            expressionNode.addChild(dependencyExpression);
        }
    }

    private void fillTransitionNode(final XMLNode transitionNode, final STransitionDefinition transition) {
        transitionNode.addAttribute(NAME, transition.getName());
        transitionNode.addAttribute(TRANSITION_SOURCE, String.valueOf(transition.getSource()));
        transitionNode.addAttribute(TRANSITION_TARGET, String.valueOf(transition.getTarget()));
        if (transition.getCondition() != null) {
            final XMLNode condition = new XMLNode(TRANSITION_CONDITION);
            fillExpressionNode(condition, transition.getCondition());
            transitionNode.addChild(condition);
        }
    }

}
