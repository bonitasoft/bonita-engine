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

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class AdvancedStartProcessValidator implements Validator<List<String>> {
    
    
    private final ProcessDefinitionService processDefinitionService;
    private final long processDefinitionId;

    public AdvancedStartProcessValidator(ProcessDefinitionService processDefinitionService, long processDefinitionId) {
        this.processDefinitionService = processDefinitionService;
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    public List<String> validate(List<String> flowNodeNames) throws SBonitaException {
        List<String> problems = new ArrayList<String>();
        if(flowNodeNames.isEmpty()) {
            problems.add("The list of activity names to start cannot be empty!");
        }
        List<String> foundFlowNodes = new ArrayList<String>(flowNodeNames.size());
        SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
        for (SFlowNodeDefinition flowNode : processDefinition.getProcessContainer().getFlowNodes()) {
            boolean invalidType = SFlowNodeType.BOUNDARY_EVENT.equals(flowNode.getType()) || SFlowNodeType.SUB_PROCESS.equals(flowNode.getType())
                    || SFlowNodeType.GATEWAY.equals(flowNode.getType());
            if (flowNodeNames.contains(flowNode.getName())) {
                foundFlowNodes.add(flowNode.getName());
                if (invalidType) {
                    problems.add(buildInvalidTypeErroMessage(processDefinition, flowNode));
                }
            }
        }
        problems.addAll(checkForNotFoundFlowNodes(flowNodeNames, foundFlowNodes, processDefinition));
        return problems;
    }

    private List<String> checkForNotFoundFlowNodes(List<String> flowNodeNames, List<String> foundFlowNodes, SProcessDefinition processDefinition) {
        List<String> problems = new ArrayList<String>();
        for (String flowNodeName : flowNodeNames) {
            if(!foundFlowNodes.contains(flowNodeName)) {
                problems.add(buildFlowNodeNotFoundErroMessage(processDefinition, flowNodeName));
            }
        }
        return problems;
    }

    private String buildInvalidTypeErroMessage(SProcessDefinition processDefinition, SFlowNodeDefinition flowNode) {
        StringBuilder stb = new StringBuilder();
        stb.append("'");
        stb.append(flowNode.getName());
        stb.append("' is not a valide start point for the process ");
        stb.append(buildProcessContext(processDefinition));
        stb.append(" You cannot start a process from a gateway, a boundary event or an event sub-process");
        return stb.toString();
    }

    private String buildFlowNodeNotFoundErroMessage(SProcessDefinition processDefinition, String flowNodeName) {
        StringBuilder stb = new StringBuilder();
        stb.append("No flownode named '");
        stb.append(flowNodeName);
        stb.append("' was found in the process");
        stb.append(buildProcessContext(processDefinition));
        return stb.toString();
    }

    private String buildProcessContext(SProcessDefinition processDefinition) {
        StringBuilder stb = new StringBuilder();
        stb.append("<id: ");
        stb.append(processDefinitionId);
        stb.append(", name: ");
        stb.append(processDefinition.getName());
        stb.append(", version: ");
        stb.append(processDefinition.getVersion());
        stb.append(">.");
        return stb.toString();
    }

}
