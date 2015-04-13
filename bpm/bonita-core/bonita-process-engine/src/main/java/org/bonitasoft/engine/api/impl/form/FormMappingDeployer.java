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

import static org.bonitasoft.engine.form.FormMappingType.PROCESS_OVERVIEW;
import static org.bonitasoft.engine.form.FormMappingType.PROCESS_START;

import java.util.List;

import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.form.model.FormMappingDefinition;
import org.bonitasoft.engine.bpm.flownode.ActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.HumanTaskDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeployException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.form.FormMappingType;
import org.bonitasoft.engine.persistence.SBonitaReadException;

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
                    // create mapping as declared in form mapping:
                    createFormMapping(processDefinitionId, getFormMappingForHumanTask(activity.getName(), formMappings), FormMappingType.TASK.getId(), activity.getName());
                }
            }
            // Deals with the process start / process overview forms:
            createFormMapping(processDefinitionId, getFormMappingForType(formMappings, PROCESS_START), PROCESS_START.getId(), null);
            createFormMapping(processDefinitionId, getFormMappingForType(formMappings, PROCESS_OVERVIEW), PROCESS_OVERVIEW.getId(), null);
        } catch (final SObjectCreationException | SBonitaReadException e) {
            throw new ProcessDeployException(e);
        }
    }

    private void createFormMapping(long processDefinitionId, FormMappingDefinition processOverviewformMapping, Integer type, String name) throws SObjectCreationException, SBonitaReadException {
        if (processOverviewformMapping != null) {
            createSFormMapping(processDefinitionId, processOverviewformMapping);
        } else {
            formMappingService.create(processDefinitionId, name, type, null, null);
        }
    }

    private SFormMapping createSFormMapping(long processDefinitionId, FormMappingDefinition formMappingDefinition) throws SObjectCreationException,
            SBonitaReadException {
        return formMappingService.create(processDefinitionId, formMappingDefinition.getTaskname(), formMappingDefinition.getType().getId(),
                formMappingDefinition.getTarget().name(), formMappingDefinition.getForm());
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

    private FormMappingDefinition getFormMappingForType(final List<FormMappingDefinition> formMappings, final FormMappingType type) {
        for (final FormMappingDefinition formMapping : formMappings) {
            if (type == formMapping.getType()) {
                return formMapping;
            }
        }
        return null;
    }
}
