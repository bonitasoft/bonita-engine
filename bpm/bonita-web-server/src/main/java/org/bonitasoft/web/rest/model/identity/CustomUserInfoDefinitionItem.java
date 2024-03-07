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
package org.bonitasoft.web.rest.model.identity;

import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasUniqueId;

/**
 * @author Vincent Elcrin
 */
public class CustomUserInfoDefinitionItem extends Item implements ItemHasUniqueId {

    public static final String ATTRIBUTE_NAME = "name";

    public static final String ATTRIBUTE_DESCRIPTION = "description";

    @Override
    public CustomUserInfoDefinitionDefinition getItemDefinition() {
        return new CustomUserInfoDefinitionDefinition();
    }

    @Override
    public void setId(String id) {
        setId(APIID.makeAPIID(id));
    }

    @Override
    public void setId(Long id) {
        setId(APIID.makeAPIID(id));
    }

    public String getName() {
        return getAttributeValue(ATTRIBUTE_NAME);
    }

    public void setName(String name) {
        setAttribute(ATTRIBUTE_NAME, name);
    }

    public String getDescription() {
        return getAttributeValue(ATTRIBUTE_DESCRIPTION);
    }

    public void setDescription(String description) {
        setAttribute(ATTRIBUTE_DESCRIPTION, description);
    }
}
