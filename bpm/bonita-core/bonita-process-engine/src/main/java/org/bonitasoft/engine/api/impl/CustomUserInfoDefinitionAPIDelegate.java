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
package org.bonitasoft.engine.api.impl;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.identity.CustomUserInfoDefinition;
import org.bonitasoft.engine.identity.CustomUserInfoDefinitionCreator;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SCustomUserInfoDefinitionAlreadyExistsException;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoDefinitionBuilder;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoDefinitionBuilderFactory;
import org.bonitasoft.engine.service.ModelConvertor;

/**
 * @author Vincent Elcrin
 * @author Elias Ricken de Medeiros
 */
public class CustomUserInfoDefinitionAPIDelegate {

    private static final int MAX_NAME_LENGHT = 75;

    private final IdentityService service;

    public CustomUserInfoDefinitionAPIDelegate(final IdentityService service) {
        this.service = service;
    }

    public CustomUserInfoDefinition create(final SCustomUserInfoDefinitionBuilderFactory factory, final CustomUserInfoDefinitionCreator creator)
            throws CreationException {
        checkParameter(creator);

        final SCustomUserInfoDefinitionBuilder builder = factory.createNewInstance();
        builder.setName(creator.getName());
        builder.setDescription(creator.getDescription());
        try {
            return ModelConvertor.convert(service.createCustomUserInfoDefinition(builder.done()));
        } catch (SCustomUserInfoDefinitionAlreadyExistsException e) {
            throw new AlreadyExistsException(e.getMessage());
        } catch (SIdentityException e) {
            throw new CreationException(e);
        }
    }

    private void checkParameter(final CustomUserInfoDefinitionCreator creator) throws CreationException {
        if (creator == null) {
            throw new CreationException("Can not create null custom user details.");
        }
        if (creator.getName() == null || creator.getName().trim().isEmpty()) {
            throw new CreationException("The definition name cannot be null or empty.");
        }
        if (creator.getName().length() > MAX_NAME_LENGHT) {
            throw new CreationException("The definition name cannot be longer then 75 characters.");
        }

    }

    public void delete(final long id) throws DeletionException {
        try {
            service.deleteCustomUserInfoDefinition(id);
        } catch (SIdentityException e) {
            throw new DeletionException(e);
        }
    }

    public List<CustomUserInfoDefinition> list(final int startIndex, final int maxResult) {
        try {
            List<CustomUserInfoDefinition> definitions = new ArrayList<CustomUserInfoDefinition>();
            for (SCustomUserInfoDefinition sDefinition : service.getCustomUserInfoDefinitions(startIndex, maxResult)) {
                definitions.add(ModelConvertor.convert(sDefinition));
            }
            return definitions;
        } catch (SIdentityException e) {
            throw new RetrieveException(e);
        }
    }

    public long count() {
        try {
            return service.getNumberOfCustomUserInfoDefinition();
        } catch (SIdentityException e) {
            throw new RetrieveException(e);
        }
    }
}
