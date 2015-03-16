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

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.impl.SCustomUserInfoValueAPI;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.identity.ExportedCustomUserInfoValue;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class CustomUserInfoValueImporter {
    
    private final SCustomUserInfoValueAPI userInfoAPI;
    private final Map<String, SCustomUserInfoDefinition> customUserInfoDefinitions;
    
    public CustomUserInfoValueImporter(SCustomUserInfoValueAPI userInfoAPI, Map<String, SCustomUserInfoDefinition> customUserInfoDefinitions) {
        this.userInfoAPI = userInfoAPI;
        this.customUserInfoDefinitions = customUserInfoDefinitions;
    }

    public void imporCustomUserInfoValues(List<ExportedCustomUserInfoValue> customUserInfoValues, long persistedUserId) throws SBonitaException {
        for (ExportedCustomUserInfoValue infoValue : customUserInfoValues) {
            SCustomUserInfoDefinition infoDefinition = customUserInfoDefinitions.get(infoValue.getName());
            if (infoDefinition == null) {
                String message = "The XML file is inconsistent. A custom user info value is refenced with name '" + infoValue.getName()
                        + "', but no custom user info definition is defined with this name.";
                throw new SImportOrganizationException(message);
            }
            userInfoAPI.set(infoDefinition.getId(), persistedUserId, infoValue.getValue());
        }
    }
    
}
