/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
 **/
package org.bonitasoft.engine.core.process.definition.model.builder.impl;

import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.core.process.definition.model.builder.SProcessDefinitionDeployInfoUpdateBuilder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Yanyan Liu
 * @author Zhang Bole
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class SProcessDefinitionDeployInfoUpdateBuilderImpl implements SProcessDefinitionDeployInfoUpdateBuilder {

    private final EntityUpdateDescriptor descriptor;

    public SProcessDefinitionDeployInfoUpdateBuilderImpl(final EntityUpdateDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

    @Override
    public SProcessDefinitionDeployInfoUpdateBuilder updateDisplayName(final String displayName) {
        descriptor.addField(SProcessDefinitionDeployInfo.DISPLAY_NAME_KEY, displayName);
        return this;
    }

    @Override
    public SProcessDefinitionDeployInfoUpdateBuilder updateDisplayDescription(final String description) {
        descriptor.addField(SProcessDefinitionDeployInfo.DISPLAY_DESCRIPTION_KEY, description);
        return this;
    }

    @Override
    public SProcessDefinitionDeployInfoUpdateBuilder updateActivationState(final ActivationState activationState) {
        descriptor.addField(SProcessDefinitionDeployInfo.ACTIVATION_STATE_KEY, activationState.name());
        return this;
    }

    @Override
    public SProcessDefinitionDeployInfoUpdateBuilder updateConfigurationState(
            final ConfigurationState configurationState) {
        descriptor.addField(SProcessDefinitionDeployInfo.CONFIGURATION_STATE_KEY, configurationState.name());
        return this;
    }

    @Override
    public SProcessDefinitionDeployInfoUpdateBuilder updateIconPath(final String iconPath) {
        descriptor.addField(SProcessDefinitionDeployInfo.ICON_PATH, iconPath);
        return this;
    }

    @Override
    public SProcessDefinitionDeployInfoUpdateBuilder updateDesignContent(String processDefinitionAsXMLString) {
        descriptor.addField(SProcessDefinitionDeployInfo.DESIGN_CONTENT, processDefinitionAsXMLString);
        return this;
    }
}
