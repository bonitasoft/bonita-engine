/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.api.CustomUserInfoAPI;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.identity.CustomUserInfoDefinition;
import org.bonitasoft.engine.identity.CustomUserInfoDefinitionCreator;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.identity.impl.CustomUserInfoDefinitionImpl;
import org.bonitasoft.engine.identity.model.SProfileMetadataDefinition;
import org.bonitasoft.engine.identity.model.builder.SProfileMetadataDefinitionBuilder;
import org.bonitasoft.engine.identity.model.builder.SProfileMetadataDefinitionBuilderFactory;

/**
 * @author Vincent Elcrin
 */
public class CustomUserInfoAPIImpl implements CustomUserInfoAPI {

    private IdentityService service;

    private SProfileMetadataDefinitionBuilderFactory factory;

    public CustomUserInfoAPIImpl(IdentityService service, SProfileMetadataDefinitionBuilderFactory factory) {
        this.service = service;
        this.factory = factory;
    }

    @Override
    public CustomUserInfoDefinition createCustomUserInfoDefinition(CustomUserInfoDefinitionCreator creator) throws CreationException {
        if (creator == null) {
            throw new CreationException("Can not create null custom user details.");
        }

        final SProfileMetadataDefinitionBuilder builder = factory.createNewInstance();
        builder.setName(creator.getName());
        builder.setDisplayName(creator.getDisplayName());
        builder.setDescription(creator.getDescription());
        try {
            return toCustomUserInfoDefinition(service.createProfileMetadataDefinition(builder.done()));
        } catch (SIdentityException e) {
            throw new CreationException(e);
        }
    }

    private CustomUserInfoDefinition toCustomUserInfoDefinition(SProfileMetadataDefinition sDefinition) {
        CustomUserInfoDefinitionImpl definition = new CustomUserInfoDefinitionImpl();
        definition.setName(sDefinition.getName());
        definition.setDisplayName(sDefinition.getDisplayName());
        definition.setDescription(sDefinition.getDescription());
        return definition;
    }
}
