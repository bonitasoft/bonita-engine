/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.identity.model.builder.impl;

import org.bonitasoft.engine.identity.model.builder.ProfileMetadataDefinitionUpdateBuilder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class ProfileMetadataDefinitionUpdateBuilderImpl implements ProfileMetadataDefinitionUpdateBuilder {

    private final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();

    @Override
    public EntityUpdateDescriptor done() {
        return this.descriptor;
    }

    @Override
    public ProfileMetadataDefinitionUpdateBuilder updateName(final String name) {
        this.descriptor.addField(ProfileMetadataDefinitionBuilderImpl.NAME, name);
        return this;
    }

    @Override
    public ProfileMetadataDefinitionUpdateBuilder updateDisplayName(final String displayName) {
        this.descriptor.addField(ProfileMetadataDefinitionBuilderImpl.DISPLAY_NAME, displayName);
        return this;
    }

    @Override
    public ProfileMetadataDefinitionUpdateBuilder updateDescription(final String description) {
        this.descriptor.addField(ProfileMetadataDefinitionBuilderImpl.DESCRIPTION, description);
        return this;
    }

}
