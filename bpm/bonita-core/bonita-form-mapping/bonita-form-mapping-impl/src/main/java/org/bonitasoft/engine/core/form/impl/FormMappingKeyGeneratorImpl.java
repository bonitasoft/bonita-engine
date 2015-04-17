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

package org.bonitasoft.engine.core.form.impl;

import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.core.form.FormMappingKeyGenerator;
import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;

/**
 * @author Baptiste Mesta
 */
public class FormMappingKeyGeneratorImpl implements FormMappingKeyGenerator {

    private final ProcessDefinitionService processDefinitionService;

    public FormMappingKeyGeneratorImpl(ProcessDefinitionService processDefinitionService) {
        this.processDefinitionService = processDefinitionService;
    }

    @Override
    public String generateKey(long processDefinitionId, String task, Integer type) throws SObjectCreationException {
        SProcessDefinition processDefinition;
        try {
            processDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
            if (processDefinition == null) {
                throw new SObjectCreationException("Process with id " + processDefinitionId + " does not exists");
            }
        } catch (SProcessDefinitionNotFoundException | SProcessDefinitionReadException e) {
            throw new SObjectCreationException("Unable to get the process with id " + processDefinitionId, e);
        }
        switch (type) {
            case SFormMapping.TYPE_PROCESS_OVERVIEW:
                return "processInstance/" + processDefinition.getName() + "/" + processDefinition.getVersion();
            case SFormMapping.TYPE_PROCESS_START:
                return "process/" + processDefinition.getName() + "/" + processDefinition.getVersion();
            case SFormMapping.TYPE_TASK:
                if (task == null || task.isEmpty()) {
                    throw new SObjectCreationException("The task name is not set");
                }
                return "taskInstance/" + processDefinition.getName() + "/" + processDefinition.getVersion() + "/" + task;
        }
        throw new SObjectCreationException("Unable to generate the key for the unknown type " + type);
    }

}
