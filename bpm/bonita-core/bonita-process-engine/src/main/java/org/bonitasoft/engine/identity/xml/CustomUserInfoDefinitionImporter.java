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
 **/
package org.bonitasoft.engine.identity.xml;

import java.util.List;

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
    
    
    private IdentityService identityService;
    private ImportOrganizationStrategy strategy;

    public CustomUserInfoDefinitionImporter(TenantServiceAccessor serviceAccessor, final ImportOrganizationStrategy strategy) {
        this.strategy = strategy;
        identityService = serviceAccessor.getIdentityService();
    }
    
    public void importCustomUserInfoDefinitions(List<CustomUserInfoDefinitionCreator> userInfoDefinitionCreators) throws SIdentityException, ImportDuplicateInOrganizationException {
        for (CustomUserInfoDefinitionCreator creator : userInfoDefinitionCreators) {
            String name = creator.getName();
            if(identityService.hasCustomUserInfoDefinition(name)) {
                SCustomUserInfoDefinition existingUserInfoDef = identityService.getCustomUserInfoDefinitionByName(name);
                strategy.foundExistingCustomUserInfoDefinition(existingUserInfoDef, creator);
            } else {
                addCustomUserInfoDefinition(creator);
            }
        }
        
    }

    private void addCustomUserInfoDefinition(CustomUserInfoDefinitionCreator creator) throws SCustomUserInfoDefinitionAlreadyExistsException, SCustomUserInfoDefinitionCreationException {
        final SCustomUserInfoDefinitionBuilder userInfoBuilder = BuilderFactory.get(SCustomUserInfoDefinitionBuilderFactory.class).createNewInstance();
        userInfoBuilder.setName(creator.getName());
        userInfoBuilder.setDescription(creator.getDescription());
        identityService.createCustomUserInfoDefinition(userInfoBuilder.done());
    }

}
