/*
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.service;

import java.util.List;

import org.bonitasoft.engine.bpm.flownode.ActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.UserTaskDefinition;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;

public class FormRequiredAnalyzer {

    private ProcessDefinitionService processDefinitionService;

    public FormRequiredAnalyzer(ProcessDefinitionService processDefinitionService) {
        this.processDefinitionService = processDefinitionService;
    }

    /**
     * Is a form required to start case / execute task: if there is a contract, a form is required (by the web part only, the Engine never needs a form mapping
     * to start a case / execute a task)
     *
     * @param sFormMapping
     * @return true if a form is required for this form mapping, false otherwise.
     */
    public boolean isFormRequired(SFormMapping sFormMapping) {
        if (sFormMapping.getType() == SFormMapping.TYPE_PROCESS_OVERVIEW) {
            return false;
        }
        try {
            final DesignProcessDefinition designProcessDefinition = processDefinitionService.getDesignProcessDefinition(sFormMapping.getProcessDefinitionId());
            if (designProcessDefinition != null) {
                if (sFormMapping.getType() == SFormMapping.TYPE_PROCESS_START) {
                    return designProcessDefinition.getContract() != null && designProcessDefinition.getContract().getInputs() != null && !designProcessDefinition.getContract().getInputs().isEmpty();
                } else // if (sFormMapping.getType() == TYPE_TASK)
                {
                    final List<ActivityDefinition> activities = designProcessDefinition.getFlowElementContainer().getActivities();
                    final UserTaskDefinition userTaskDefinition = findActivityWithName(activities, sFormMapping.getTask());
                    return userTaskDefinition != null && userTaskDefinition.getContract() != null && userTaskDefinition.getContract().getInputs() != null && !userTaskDefinition.getContract().getInputs().isEmpty();
                }
            }
        } catch (SBonitaException e) {
        }
        return false;
    }

    protected UserTaskDefinition findActivityWithName(List<ActivityDefinition> activities, String task) {
        if (task != null && activities != null) {
            for (ActivityDefinition activity : activities) {
                if (task.equals(activity.getName()) && activity instanceof UserTaskDefinition) {
                    return (UserTaskDefinition) activity;
                }
            }
        }
        return null;
    }
}
