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
package org.bonitasoft.engine.api.impl.transaction.process;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoUpdater;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoUpdater.ProcessDeploymentInfoField;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.builder.SProcessDefinitionDeployInfoUpdateBuilder;
import org.bonitasoft.engine.core.process.definition.model.builder.SProcessDefinitionDeployInfoUpdateBuilderFactory;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Yanyan Liu
 * @author Zhang Bole
 * @author Celine Souchet
 */
public class UpdateProcessDeploymentInfo implements TransactionContent {

    private final ProcessDefinitionService processDefinitionService;

    private final long processId;

    private final ProcessDeploymentInfoUpdater processDeploymentInfoUpdateDescriptor;

    public UpdateProcessDeploymentInfo(final ProcessDefinitionService processDefinitionService,
            final long processId,
            final ProcessDeploymentInfoUpdater processDeploymentInfoUpdateDescriptor) {
        this.processDefinitionService = processDefinitionService;
        this.processId = processId;
        this.processDeploymentInfoUpdateDescriptor = processDeploymentInfoUpdateDescriptor;
    }

    @Override
    public void execute() throws SBonitaException {
        processDefinitionService.updateProcessDefinitionDeployInfo(processId, buildDescriptor());
    }

    private EntityUpdateDescriptor buildDescriptor() {
        final SProcessDefinitionDeployInfoUpdateBuilder processDeploymentInfoUpdateBuilder = BuilderFactory.get(SProcessDefinitionDeployInfoUpdateBuilderFactory.class).createNewInstance();
        final Map<ProcessDeploymentInfoField, Serializable> updatedFieldsMap = processDeploymentInfoUpdateDescriptor.getFields();
        for (final Entry<ProcessDeploymentInfoField, Serializable> field : updatedFieldsMap.entrySet()) {
            switch (field.getKey()) {
                case DISPLAY_NAME:
                    processDeploymentInfoUpdateBuilder.updateDisplayName((String) field.getValue());
                    break;
                case DISPLAY_DESCRIPTION:
                    processDeploymentInfoUpdateBuilder.updateDisplayDescription((String) field.getValue());
                    break;
                case ICONPATH:
                    processDeploymentInfoUpdateBuilder.updateIconPath((String) field.getValue());
                    break;
                default:
                    break;
            }
        }
        return processDeploymentInfoUpdateBuilder.done();
    }

}
