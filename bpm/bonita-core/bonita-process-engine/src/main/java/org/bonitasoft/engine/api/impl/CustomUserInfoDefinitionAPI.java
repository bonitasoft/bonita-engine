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

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.identity.CustomUserInfoDefinition;
import org.bonitasoft.engine.identity.CustomUserInfoDefinitionCreator;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoDefinitionBuilder;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoDefinitionBuilderFactory;

/**
 * @author Vincent Elcrin
 */
public class CustomUserInfoDefinitionAPI {

    private IdentityService service;

    private final CustomUserInfoConverter converter = new CustomUserInfoConverter();

    public CustomUserInfoDefinitionAPI(IdentityService service) {
        this.service = service;
    }

    public CustomUserInfoDefinition create(SCustomUserInfoDefinitionBuilderFactory factory, CustomUserInfoDefinitionCreator creator) throws CreationException {
        if (creator == null) {
            throw new CreationException("Can not create null custom user details.");
        }

        final SCustomUserInfoDefinitionBuilder builder = factory.createNewInstance();
        builder.setName(creator.getName());
        builder.setDisplayName(creator.getDisplayName());
        builder.setDescription(creator.getDescription());
        try {
            return converter.convert(service.createCustomUserInfoDefinition(builder.done()));
        } catch (SIdentityException e) {
            throw new CreationException(e);
        }
    }

    public CustomUserInfoDefinition delete(long id) throws SIdentityException {
        SCustomUserInfoDefinition definition = service.getCustomUserInfoDefinition(id);
        service.deleteCustomUserInfoDefinition(definition);
        definition.setId(-1L);
        return converter.convert(definition);
    }

    public List<CustomUserInfoDefinition> list(int startIndex, int maxResult) throws SIdentityException {
        List<CustomUserInfoDefinition> definitions = new ArrayList<CustomUserInfoDefinition>();
        for (SCustomUserInfoDefinition sDefinition : service.getCustomUserInfoDefinitions(startIndex, maxResult)) {
            definitions.add(converter.convert(sDefinition));
        }
        return definitions;
    }
}
