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
package org.bonitasoft.engine.api.impl.form;

import static org.bonitasoft.engine.form.mapping.FormMappingType.PROCESS_OVERVIEW;
import static org.bonitasoft.engine.form.mapping.FormMappingType.PROCESS_START;

import java.util.List;

import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.form.model.FormMappingDefinition;
import org.bonitasoft.engine.bpm.flownode.ActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.HumanTaskDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeployException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.form.mapping.FormMappingType;

/**
 * @author Emmanuel Duchastenier
 */
public class FormMappingDeployer {

    private final FormMappingService formMappingService;

    public FormMappingDeployer(final FormMappingService formMappingService) {
        this.formMappingService = formMappingService;
    }

    public void deployFormMappings(final BusinessArchive businessArchive, final long processDefinitionId) throws ProcessDeployException {
        final List<FormMappingDefinition> formMappings = businessArchive.getFormMappingModel().getFormMappings();
        final List<ActivityDefinition> activities = businessArchive.getProcessDefinition().getFlowElementContainer().getActivities();
        try {
            // Deals with human tasks declared in process definition:
            for (final ActivityDefinition activity : activities) {
                if (isHumanTask(activity)) {
                    final FormMappingDefinition formMappingForHumanTask = getFormMappingForHumanTask(activity.getName(), formMappings);
                    // create mapping as declared in form mapping:
                    if (formMappingForHumanTask != null) {
                        formMappingService.create(processDefinitionId, formMappingForHumanTask.getTaskname(), formMappingForHumanTask.getForm(),
                                formMappingForHumanTask.isExternal(), formMappingForHumanTask.getType().name());
                    }
                    // create empty mapping for human task with no form declared:
                    else {
                        formMappingService.create(processDefinitionId, activity.getName(), null, false, FormMappingType.TASK.name());
                    }
                }
            }
            // Deals with the process start / process overview forms:
            for (final FormMappingDefinition formMappingForProcess : formMappings) {
                if (formMappingForProcess.getType() == PROCESS_START || formMappingForProcess.getType() == PROCESS_OVERVIEW) {
                    formMappingService.create(processDefinitionId, null, formMappingForProcess.getForm(), formMappingForProcess.isExternal(),
                            formMappingForProcess.getType().name());
                }
            }
        } catch (final SObjectCreationException e) {
            throw new ProcessDeployException(e);
        }
    }

    private boolean isHumanTask(final ActivityDefinition activity) {
        return activity instanceof HumanTaskDefinition;
    }

    /**
     * @return the found mapping for the given human task, or null is not found
     */
    private FormMappingDefinition getFormMappingForHumanTask(final String name, final List<FormMappingDefinition> formMappings) {
        for (final FormMappingDefinition formMapping : formMappings) {
            if (name.equals(formMapping.getTaskname())) {
                return formMapping;
            }
        }
        return null;
    }
}
