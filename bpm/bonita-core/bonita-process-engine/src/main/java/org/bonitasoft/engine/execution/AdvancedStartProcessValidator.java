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
package org.bonitasoft.engine.execution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.contract.validation.ContractValidator;
import org.bonitasoft.engine.bpm.contract.validation.ContractValidatorFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SUserTaskDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SContractViolationException;
import org.bonitasoft.engine.expression.ExpressionService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * @author Elias Ricken de Medeiros
 */
public class AdvancedStartProcessValidator {

    private final ProcessDefinitionService processDefinitionService;
    private final long processDefinitionId;
    private TechnicalLoggerService technicalLoggerService;
    private ExpressionService expressionService;

    public AdvancedStartProcessValidator(ProcessDefinitionService processDefinitionService, long processDefinitionId,
            TechnicalLoggerService technicalLoggerService, ExpressionService expressionService) {
        this.processDefinitionService = processDefinitionService;
        this.processDefinitionId = processDefinitionId;
        this.technicalLoggerService = technicalLoggerService;
        this.expressionService = expressionService;
    }

    public List<String> validate(List<String> flowNodeNames, Map<String, Serializable> processContractInputs) throws SBonitaException {
        List<String> problems = new ArrayList<>();
        if (flowNodeNames.isEmpty()) {
            problems.add("The list of activity names to start cannot be empty!");
        }
        List<String> foundFlowNodes = new ArrayList<>(flowNodeNames.size());
        SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
        problems.addAll(checkFlowNodesAreSupported(flowNodeNames, foundFlowNodes, processDefinition));
        problems.addAll(checkForNotFoundFlowNodes(flowNodeNames, foundFlowNodes, processDefinition));
        if (!problems.isEmpty()) {
            //check contract only if flow nodes are ok
            return problems;
        }
        problems.addAll(checkProcessContract(processContractInputs, processDefinition));
        return problems;
    }

    private List<String> checkProcessContract(Map<String, Serializable> processContractInputs, SProcessDefinition processDefinition) {
        return validateContract(processContractInputs, processDefinition.getContract(), processDefinition.getName());
    }

    private List<String> validateContract(Map<String, Serializable> inputs, SContractDefinition contract, String element) {
        if (contract == null) {
            return Collections.emptyList();
        }
        final ContractValidator validator = new ContractValidatorFactory().createContractValidator(technicalLoggerService,
                expressionService);
        try {
            validator.validate(processDefinitionId, contract, inputs);
        } catch (SContractViolationException e) {
            return e.getExplanations().isEmpty() ? Collections.singletonList(e.getSimpleMessage() + " on " + element) : appendElement(e, element);
        }
        return Collections.emptyList();
    }

    private List<String> appendElement(SContractViolationException e, String element) {
        ArrayList<String> strings = new ArrayList<>();
        for (String explanation : e.getExplanations()) {
            strings.add(explanation + " on " + element);
        }
        return strings;
    }

    private List<String> checkFlowNodesContracts(List<String> flowNodeNames, Map<String, Map<String, Serializable>> activitiesContractInputs,
            SProcessDefinition processDefinition) {
        List<String> problems = new ArrayList<>();
        for (String flowNodeName : flowNodeNames) {
            SFlowNodeDefinition flowNode = processDefinition.getProcessContainer().getFlowNode(flowNodeName);
            if (flowNode instanceof SUserTaskDefinition && ((SUserTaskDefinition) flowNode).getContract() != null) {
                problems.addAll(validateContract(
                        activitiesContractInputs == null ? Collections.<String, Serializable> emptyMap() : activitiesContractInputs.get(flowNodeName),
                        ((SUserTaskDefinition) flowNode).getContract(), flowNodeName));
            }
        }
        return problems;
    }

    private List<String> checkFlowNodesAreSupported(List<String> flowNodeNames, List<String> foundFlowNodes, SProcessDefinition processDefinition) {
        List<String> problems = new ArrayList<>();
        for (SFlowNodeDefinition flowNode : processDefinition.getProcessContainer().getFlowNodes()) {
            boolean invalidType = SFlowNodeType.BOUNDARY_EVENT.equals(flowNode.getType()) || SFlowNodeType.SUB_PROCESS.equals(flowNode.getType())
                    || SFlowNodeType.GATEWAY.equals(flowNode.getType());
            if (flowNodeNames.contains(flowNode.getName())) {
                foundFlowNodes.add(flowNode.getName());
                if (invalidType) {
                    problems.add(buildInvalidTypeErrorMessage(processDefinition, flowNode));
                }
            }
        }
        return problems;
    }

    private List<String> checkForNotFoundFlowNodes(List<String> flowNodeNames, List<String> foundFlowNodes, SProcessDefinition processDefinition) {
        List<String> problems = new ArrayList<>();
        for (String flowNodeName : flowNodeNames) {
            if (!foundFlowNodes.contains(flowNodeName)) {
                problems.add(buildFlowNodeNotFoundErroMessage(processDefinition, flowNodeName));
            }
        }
        return problems;
    }

    private String buildInvalidTypeErrorMessage(SProcessDefinition processDefinition, SFlowNodeDefinition flowNode) {
        return "'" +
                flowNode.getName() +
                "' is not a valid start point for the process " +
                buildProcessContext(processDefinition) +
                " You cannot start a process from a gateway, a boundary event or an event sub-process";
    }

    private String buildFlowNodeNotFoundErroMessage(SProcessDefinition processDefinition, String flowNodeName) {
        return "No flownode named '" +
                flowNodeName +
                "' was found in the process" +
                buildProcessContext(processDefinition);
    }

    private String buildProcessContext(SProcessDefinition processDefinition) {
        return "<id: " +
                processDefinitionId +
                ", name: " +
                processDefinition.getName() +
                ", version: " +
                processDefinition.getVersion() +
                ">.";
    }

}
