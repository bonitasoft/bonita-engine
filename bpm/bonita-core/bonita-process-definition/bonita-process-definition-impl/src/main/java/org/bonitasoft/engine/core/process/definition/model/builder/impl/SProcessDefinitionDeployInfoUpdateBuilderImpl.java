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
package org.bonitasoft.engine.core.process.definition.model.builder.impl;

import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.process.definition.model.builder.SProcessDefinitionDeployInfoBuilderFactory;
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
        descriptor.addField(BuilderFactory.get(SProcessDefinitionDeployInfoBuilderFactory.class).getDisplayNameKey(), displayName);
        return this;
    }

    @Override
    public SProcessDefinitionDeployInfoUpdateBuilder updateDisplayDescription(final String description) {
        descriptor.addField(BuilderFactory.get(SProcessDefinitionDeployInfoBuilderFactory.class).getDisplayDescriptionKey(), description);
        return this;
    }

    @Override
    public SProcessDefinitionDeployInfoUpdateBuilder updateActivationState(final ActivationState activationState) {
        descriptor.addField(BuilderFactory.get(SProcessDefinitionDeployInfoBuilderFactory.class).getActivationStateKey(), activationState.name());
        return this;
    }

    @Override
    public SProcessDefinitionDeployInfoUpdateBuilder updateConfigurationState(final ConfigurationState configurationState) {
        descriptor.addField(BuilderFactory.get(SProcessDefinitionDeployInfoBuilderFactory.class).getConfigurationStateKey(), configurationState.name());
        return this;
    }

    @Override
    public SProcessDefinitionDeployInfoUpdateBuilder updateIconPath(final String iconPath) {
        descriptor.addField(BuilderFactory.get(SProcessDefinitionDeployInfoBuilderFactory.class).getIconPathKey(), iconPath);
        return this;
    }
}
