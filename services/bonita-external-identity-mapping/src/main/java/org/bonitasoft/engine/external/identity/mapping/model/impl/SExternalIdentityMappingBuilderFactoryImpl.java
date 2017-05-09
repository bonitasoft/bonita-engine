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
package org.bonitasoft.engine.external.identity.mapping.model.impl;

import org.bonitasoft.engine.external.identity.mapping.model.SExternalIdentityMappingBuilder;
import org.bonitasoft.engine.external.identity.mapping.model.SExternalIdentityMappingBuilderFactory;

/**
 * @author Emmanuel Duchastenier
 */
public class SExternalIdentityMappingBuilderFactoryImpl implements SExternalIdentityMappingBuilderFactory {

    private static final String ID_KEY = "id";

    private static final String KIND_KEY = "kind";

    private static final String USER_ID_KEY = "userId";

    private static final String GROUP_ID_KEY = "groupId";

    private static final String ROLE_ID_KEY = "roleId";

    private static final String EXTERNAL_ID_KEY = "externalId";

    @Override
    public SExternalIdentityMappingBuilder createNewInstance(final String externalId) {
        final SExternalIdentityMappingImpl mapping = new SExternalIdentityMappingImpl(externalId);
        return new SExternalIdentityMappingBuilderImpl(mapping);
    }

    @Override
    public String getIdKey() {
        return ID_KEY;
    }

    @Override
    public String getKindKey() {
        return KIND_KEY;
    }

    @Override
    public String getUserIdKey() {
        return USER_ID_KEY;
    }

    @Override
    public String getExternalIdKey() {
        return EXTERNAL_ID_KEY;
    }

    @Override
    public String getGroupIdKey() {
        return GROUP_ID_KEY;
    }

    @Override
    public String getRoleIdKey() {
        return ROLE_ID_KEY;
    }

    @Override
    public String getDisplayNamePart1Key() {
        return "displayNamePart1";
    }

    @Override
    public String getDisplayNamePart2Key() {
        return "displayNamePart2";
    }

    @Override
    public String getDisplayNamePart3Key() {
        return "displayNamePart3";
    }

}
