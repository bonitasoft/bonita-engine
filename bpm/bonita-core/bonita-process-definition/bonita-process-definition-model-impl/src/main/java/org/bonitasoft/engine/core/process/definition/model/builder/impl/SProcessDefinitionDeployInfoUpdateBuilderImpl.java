/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.definition.model.builder.impl;

import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.core.process.definition.model.builder.ProcessDefinitionDeployInfoBuilder;
import org.bonitasoft.engine.core.process.definition.model.builder.SProcessDefinitionDeployInfoUpdateBuilder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Yanyan Liu
 * @author Zhang Bole
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class SProcessDefinitionDeployInfoUpdateBuilderImpl implements SProcessDefinitionDeployInfoUpdateBuilder {

    private final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();

    private final ProcessDefinitionDeployInfoBuilder processDefinitionDeployInfoBuilder;

    /**
     * @param processDefinitionDeployInfoBuilder
     */
    public SProcessDefinitionDeployInfoUpdateBuilderImpl(final ProcessDefinitionDeployInfoBuilder processDefinitionDeployInfoBuilder) {
        this.processDefinitionDeployInfoBuilder = processDefinitionDeployInfoBuilder;
    }

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

    @Override
    public SProcessDefinitionDeployInfoUpdateBuilder updateDisplayName(final String displayName) {
        descriptor.addField(processDefinitionDeployInfoBuilder.getDisplayNameKey(), displayName);
        return this;
    }

    @Override
    public SProcessDefinitionDeployInfoUpdateBuilder updateDisplayDescription(final String description) {
        descriptor.addField(processDefinitionDeployInfoBuilder.getDisplayDescriptionKey(), description);
        return this;
    }

    @Override
    public SProcessDefinitionDeployInfoUpdateBuilder updateActivationState(final ActivationState activationState) {
        descriptor.addField(processDefinitionDeployInfoBuilder.getActivationStateKey(), activationState.name());
        return this;
    }

    @Override
    public SProcessDefinitionDeployInfoUpdateBuilder updateConfigurationState(final ConfigurationState configurationState) {
        descriptor.addField(processDefinitionDeployInfoBuilder.getConfigurationStateKey(), configurationState.name());
        return this;
    }

    @Override
    public SProcessDefinitionDeployInfoUpdateBuilder updateIconPath(final String iconPath) {
        descriptor.addField(processDefinitionDeployInfoBuilder.getIconPathKey(), iconPath);
        return this;
    }
}
