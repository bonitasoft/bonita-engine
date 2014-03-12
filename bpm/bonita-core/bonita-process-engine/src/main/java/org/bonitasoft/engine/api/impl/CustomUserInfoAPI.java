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

import org.bonitasoft.engine.identity.CustomUserInfo;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SIdentityException;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;

/**
 * @author Vincent Elcrin
 */
public class CustomUserInfoAPI {

    private IdentityService service;

    private final CustomUserInfoDefinitionConverter converter = new CustomUserInfoDefinitionConverter();

    public CustomUserInfoAPI(IdentityService service) {
        this.service = service;
    }

    public List<CustomUserInfo> list(long userId, int startIndex, int maxResult) throws SIdentityException {
        List<CustomUserInfo> definitions = new ArrayList<CustomUserInfo>();
        for (SCustomUserInfoDefinition sDefinition : service.getCustomUserInfoDefinitions(startIndex, maxResult)) {
            definitions.add(new CustomUserInfo(converter.convert(sDefinition)));
        }
        return definitions;
    }
}
