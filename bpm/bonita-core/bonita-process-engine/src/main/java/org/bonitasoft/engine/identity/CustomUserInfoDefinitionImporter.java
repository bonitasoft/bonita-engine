/**
 * Copyright (C) 2016 Bonitasoft S.A.
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
package org.bonitasoft.engine.identity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.xml.ExportedCustomUserInfoDefinition;

/**
 * @author Elias Ricken de Medeiros
 */
public class CustomUserInfoDefinitionImporter {

    private final IdentityService identityService;
    private final ImportOrganizationStrategy strategy;

    public CustomUserInfoDefinitionImporter(IdentityService identityService,
            final ImportOrganizationStrategy strategy) {
        this.strategy = strategy;
        this.identityService = identityService;
    }

    public Map<String, SCustomUserInfoDefinition> importCustomUserInfoDefinitions(
            List<ExportedCustomUserInfoDefinition> userInfoDefinitionCreators)
            throws SIdentityException, ImportDuplicateInOrganizationException {
        Map<String, SCustomUserInfoDefinition> nameToDefinition = new HashMap<>(userInfoDefinitionCreators.size());
        for (ExportedCustomUserInfoDefinition creator : userInfoDefinitionCreators) {
            String name = creator.getName();
            SCustomUserInfoDefinition userInfoDef;
            if (identityService.hasCustomUserInfoDefinition(name)) {
                userInfoDef = identityService.getCustomUserInfoDefinitionByName(name);
                strategy.foundExistingCustomUserInfoDefinition(userInfoDef, creator);
            } else {
                userInfoDef = addCustomUserInfoDefinition(creator);
            }
            nameToDefinition.put(name, userInfoDef);
        }
        return nameToDefinition;
    }

    private SCustomUserInfoDefinition addCustomUserInfoDefinition(ExportedCustomUserInfoDefinition creator)
            throws SCustomUserInfoDefinitionAlreadyExistsException, SCustomUserInfoDefinitionCreationException {
        final SCustomUserInfoDefinition.SCustomUserInfoDefinitionBuilder userInfoBuilder = SCustomUserInfoDefinition
                .builder();
        userInfoBuilder.name(creator.getName());
        userInfoBuilder.description(creator.getDescription());
        return identityService.createCustomUserInfoDefinition(userInfoBuilder.build());
    }

}
