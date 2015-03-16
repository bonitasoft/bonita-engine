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
package org.bonitasoft.engine.identity.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.identity.CustomUserInfoDefinitionCreator;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SCustomUserInfoDefinitionAlreadyExistsException;
import org.bonitasoft.engine.identity.SCustomUserInfoDefinitionCreationException;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoDefinitionBuilder;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoDefinitionBuilderFactory;
import org.bonitasoft.engine.service.TenantServiceAccessor;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class CustomUserInfoDefinitionImporter {
    
    
    private final IdentityService identityService;
    private final ImportOrganizationStrategy strategy;

    public CustomUserInfoDefinitionImporter(TenantServiceAccessor serviceAccessor, final ImportOrganizationStrategy strategy) {
        this.strategy = strategy;
        identityService = serviceAccessor.getIdentityService();
    }
    
    public Map<String, SCustomUserInfoDefinition> importCustomUserInfoDefinitions(List<CustomUserInfoDefinitionCreator> userInfoDefinitionCreators) throws SIdentityException, ImportDuplicateInOrganizationException {
        Map<String, SCustomUserInfoDefinition> nameToDefinition = new HashMap<String, SCustomUserInfoDefinition>(userInfoDefinitionCreators.size());
        for (CustomUserInfoDefinitionCreator creator : userInfoDefinitionCreators) {
            String name = creator.getName();
            SCustomUserInfoDefinition userInfoDef = null;
            if(identityService.hasCustomUserInfoDefinition(name)) {
                userInfoDef = identityService.getCustomUserInfoDefinitionByName(name);
                strategy.foundExistingCustomUserInfoDefinition(userInfoDef, creator);
            } else {
                userInfoDef = addCustomUserInfoDefinition(creator);
            }
            nameToDefinition.put(name, userInfoDef);
        }
        return nameToDefinition;
    }

    private SCustomUserInfoDefinition addCustomUserInfoDefinition(CustomUserInfoDefinitionCreator creator) throws SCustomUserInfoDefinitionAlreadyExistsException, SCustomUserInfoDefinitionCreationException {
        final SCustomUserInfoDefinitionBuilder userInfoBuilder = BuilderFactory.get(SCustomUserInfoDefinitionBuilderFactory.class).createNewInstance();
        userInfoBuilder.setName(creator.getName());
        userInfoBuilder.setDescription(creator.getDescription());
        return identityService.createCustomUserInfoDefinition(userInfoBuilder.done());
    }

}
