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
package org.bonitasoft.engine.core.migration.model.impl.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.core.migration.model.SConnectorDefinitionWithEnablement;
import org.bonitasoft.engine.core.migration.model.SMigrationMapping;
import org.bonitasoft.engine.core.migration.model.SMigrationPlan;
import org.bonitasoft.engine.core.migration.model.SOperationWithEnablement;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.xml.XMLNode;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class XMLSMigrationPlan {

    public static final String NAMESPACE = "http://www.bonitasoft.org/ns/migration/instance/6.0";

    public static final String MIGRATION_PLAN_NODE = "migrationPlan";

    public static final String EXPRESSION_TYPE = "expressionType";

    public static final String EXPRESSION_RETURN_TYPE = "returnType";

    public static final String EXPRESSION_INTERPRETER = "interpreter";

    public static final String EXPRESSION_CONTENT = "content";

    public static final String EXPRESSION_NODE = "expression";

    public static final String BOS_VERSION = "bos_version";

    public static final String BOS_CURRENT_VERSION = "6.0-SNAPSHOT";

    public static final String MAPPING_OPERATION_NODE = "mappingOperation";

    public static final String OPERATION_NODE = "operation";

    public static final String OPERATION_RIGHT_OPERAND = "rightOperand";

    public static final String OPERATION_LEFT_OPERAND = "leftOperand";

    public static final String OPERATION_OPERATOR_TYPE = "operatorType";

    public static final String OPERATION_OPERATOR = "operator";

    public static final String LEFT_OPERAND_NAME = "name";

    public static final String NAME = "name";

    public static final String MAPPINGS_NODE = "mappings";

    public static final String MAPPING_NODE = "mapping";

    public static final String OPERATIONS_NODE = "operations";

    public static final String CONNECTOR_NODE = "connector";

    public static final String ENABLEMENT = "enablement";

    public static final String CONNECTOR_ID = "connectorId";

    public static final String CONNECTOR_INPUT = "input";

    public static final String CONNECTOR_INPUT_NAME = "inputName";

    public static final String CONNECTORS_NODE = "connectors";

    public static final String CONNECTOR_INPUTS_NODE = "inputs";

    public static final String CONNECTOR_OUTPUTS_NODE = "outputs";

    public static final String CONNECTOR_VERSION = "version";

    public static final String SOURCE_NAME = "source_name";

    public static final String SOURCE_STATE = "source_state";

    public static final String TARGET_NAME = "target_name";

    public static final String TARGET_STATE = "target_state";

    public static final String SOURCE_VERSION = "source_version";

    public static final String TARGET_VERSION = "target_version";

    public static final String DESCRIPTION = "description";

    public Map<Object, String> objectToId = new HashMap<Object, String>();

    public XMLNode getXMLMigrationPlan(final SMigrationPlan migrationPlan) {
        final XMLNode rootNode = new XMLNode(MIGRATION_PLAN_NODE);
        rootNode.addAttribute(BOS_VERSION, BOS_CURRENT_VERSION);
        rootNode.addAttribute("xmlns", NAMESPACE);
        createAndFillMigrationPlan(migrationPlan, rootNode);
        return rootNode;
    }

    private void createAndFillMigrationPlan(final SMigrationPlan migrationPlan, final XMLNode migrationPlanNode) {
        createAndFillMappingsNode(migrationPlan, migrationPlanNode);
        createAndFillConnectorsNode(migrationPlanNode, migrationPlan.getConnectors());
        createAndFillOperationsNode(migrationPlanNode, migrationPlan.getOperations());
        migrationPlanNode.addAttribute(DESCRIPTION, migrationPlan.getDescription());
        migrationPlanNode.addAttribute(SOURCE_NAME, migrationPlan.getSourceName());
        migrationPlanNode.addAttribute(SOURCE_VERSION, migrationPlan.getSourceVersion());
        migrationPlanNode.addAttribute(TARGET_NAME, migrationPlan.getTargetName());
        migrationPlanNode.addAttribute(TARGET_VERSION, migrationPlan.getTargetVersion());
    }

    private void createAndFillMappingsNode(final SMigrationPlan migrationPlan, final XMLNode migrationPlanNode) {
        final XMLNode mappings = new XMLNode(MAPPINGS_NODE);
        migrationPlanNode.addChild(mappings);
        for (final SMigrationMapping mapping : migrationPlan.getMappings()) {
            XMLNode mappingNode;
            mappingNode = new XMLNode(MAPPING_NODE);
            fillMappingNode(mappingNode, mapping);
            mappings.addChild(mappingNode);
        }
    }

    private void createAndFillOperationsNode(final XMLNode parent, final List<SOperationWithEnablement> operations) {
        final XMLNode operationsNode = new XMLNode(OPERATIONS_NODE);
        for (final SOperationWithEnablement operationWithEnablement : operations) {
            final XMLNode operationNode = new XMLNode(OPERATION_NODE);
            fillOperationNode(operationNode, operationWithEnablement);
            operationsNode.addChild(operationNode);
        }
        parent.addChild(operationsNode);
    }

    private void fillOperationNode(final XMLNode operationNode, final SOperationWithEnablement operationWithEnablement) {
        final SOperation operation = operationWithEnablement.getOperation();
        operationNode.addAttribute(OPERATION_OPERATOR_TYPE, operation.getType().name());
        operationNode.addAttribute(OPERATION_OPERATOR, operation.getOperator());
        final XMLNode rightOperand = new XMLNode(OPERATION_LEFT_OPERAND);
        fillLeftOperandNode(rightOperand, operation.getLeftOperand());
        operationNode.addChild(rightOperand);
        final XMLNode expressionNode = new XMLNode(OPERATION_RIGHT_OPERAND);
        fillExpressionNode(expressionNode, operation.getRightOperand());
        operationNode.addChild(expressionNode);
        final XMLNode enablement = new XMLNode(ENABLEMENT);
        fillExpressionNode(enablement, operationWithEnablement.getEnablement());
        operationNode.addChild(enablement);
    }

    private void createAndFillConnectorsNode(final XMLNode parent, final List<SConnectorDefinitionWithEnablement> connectors) {
        final XMLNode connectorsNode = new XMLNode(CONNECTORS_NODE);
        for (final SConnectorDefinitionWithEnablement connectorWithEnablement : connectors) {
            final XMLNode connectorNode = new XMLNode(CONNECTOR_NODE);
            fillConnectorNode(connectorNode, connectorWithEnablement);
            connectorsNode.addChild(connectorNode);
        }
        parent.addChild(connectorsNode);
    }

    private void fillMappingNode(final XMLNode mappingNode, final SMigrationMapping mapping) {
        createAndFillConnectorsNode(mappingNode, mapping.getConnectors());
        createAndFillOperationsNode(mappingNode, mapping.getOperations());
        mappingNode.addAttribute(SOURCE_NAME, mapping.getSourceName());
        mappingNode.addAttribute(SOURCE_STATE, String.valueOf(mapping.getSourceState()));
        mappingNode.addAttribute(TARGET_NAME, mapping.getTargetName());
        mappingNode.addAttribute(TARGET_STATE, String.valueOf(mapping.getTargetState()));
    }

    private void fillConnectorNode(final XMLNode connectorNode, final SConnectorDefinitionWithEnablement connectorWithEnablement) {
        final SConnectorDefinition connector = connectorWithEnablement.getConnector();
        // connectorNode.addAttribute(ID, connector.getId().toString()); TODO : Uncomment when generate id
        connectorNode.addAttribute(NAME, connector.getName());
        connectorNode.addAttribute(CONNECTOR_ID, connector.getConnectorId());
        connectorNode.addAttribute(CONNECTOR_VERSION, connector.getVersion());
        final Map<String, SExpression> inputs = connector.getInputs();
        createAndFillInputsNode(connectorNode, inputs);
        final XMLNode outputsNode = new XMLNode(CONNECTOR_OUTPUTS_NODE);
        createAndfillOperations(outputsNode, connector.getOutputs(), OPERATION_NODE);
        connectorNode.addChild(outputsNode);
        final XMLNode enablement = new XMLNode(ENABLEMENT);
        fillExpressionNode(enablement, connectorWithEnablement.getEnablement());
        connectorNode.addChild(enablement);
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
        final XMLNode rightOperand = new XMLNode(OPERATION_LEFT_OPERAND);
        fillLeftOperandNode(rightOperand, operation.getLeftOperand());
        operationNode.addChild(rightOperand);
        final XMLNode expressionNode = new XMLNode(OPERATION_RIGHT_OPERAND);
        fillExpressionNode(expressionNode, operation.getRightOperand());
        operationNode.addChild(expressionNode);
        parentNode.addChild(operationNode);
    }

    private void createAndFillInputsNode(final XMLNode connectorNode, final Map<String, SExpression> inputs) {
        final XMLNode inputsNode = new XMLNode(CONNECTOR_INPUTS_NODE);
        for (final Entry<String, SExpression> input : inputs.entrySet()) {
            final XMLNode inputNode = new XMLNode(CONNECTOR_INPUT);
            inputNode.addAttribute(NAME, input.getKey());
            final XMLNode expressionNode = new XMLNode(EXPRESSION_NODE);
            fillExpressionNode(expressionNode, input.getValue());
            inputNode.addChild(expressionNode);
            inputsNode.addChild(inputNode);
        }
        connectorNode.addChild(inputsNode);
    }

    private void fillLeftOperandNode(final XMLNode rightOperandNode, final SLeftOperand sLeftOperand) {
        rightOperandNode.addAttribute(LEFT_OPERAND_NAME, sLeftOperand.getName());
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

}
