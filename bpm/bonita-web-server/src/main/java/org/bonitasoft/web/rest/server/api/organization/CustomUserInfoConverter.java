/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.api.organization;

import org.bonitasoft.engine.identity.CustomUserInfo;
import org.bonitasoft.engine.identity.CustomUserInfoDefinition;
import org.bonitasoft.engine.identity.CustomUserInfoValue;
import org.bonitasoft.web.rest.model.identity.CustomUserInfoDefinitionItem;
import org.bonitasoft.web.rest.model.identity.CustomUserInfoItem;
import org.bonitasoft.web.rest.server.datastore.converter.ItemConverter;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Vincent Elcrin
 */
public class CustomUserInfoConverter extends ItemConverter<CustomUserInfoItem, CustomUserInfoValue> {

    public CustomUserInfoDefinitionItem convert(CustomUserInfoDefinition definition) {
        CustomUserInfoDefinitionItem item = new CustomUserInfoDefinitionItem();
        item.setId(APIID.makeAPIID(definition.getId()));
        item.setName(definition.getName());
        item.setDescription(definition.getDescription());
        return item;
    }

    public CustomUserInfoItem convert(CustomUserInfo information) {
        CustomUserInfoItem item = new CustomUserInfoItem();
        item.setUserId(information.getUserId());
        item.setDefinition(convert(information.getDefinition()));
        item.setValue(information.getValue());
        return item;
    }

    @Override
    public CustomUserInfoItem convert(CustomUserInfoValue value) {
        CustomUserInfoItem item = new CustomUserInfoItem();
        item.setUserId(value.getUserId());
        item.setDefinition(APIID.makeAPIID(value.getDefinitionId()));
        item.setValue(value.getValue());
        return item;
    }
}
