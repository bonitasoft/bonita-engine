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

/**
 * @author Vincent Elcrin
 */
public class CustomUserInfoItem extends Item {

    public static final String FILTER_USER_ID = "userId";

    public static final String ATTRIBUTE_DEFINITION_ID = "definitionId";

    public static final String ATTRIBUTE_USER_ID = "userId";

    public static final String ATTRIBUTE_VALUE = "value";

    @Override
    public CustomUserInfoDefinition getItemDefinition() {
        return new CustomUserInfoDefinition();
    }

    public void setUserId(long userId) {
        setAttribute(ATTRIBUTE_USER_ID, userId);
    }

    public void setDefinition(APIID id) {
        setAttribute(ATTRIBUTE_DEFINITION_ID, id);
    }

    public void setDefinition(CustomUserInfoDefinitionItem definition) {
        setDefinition(definition.getId());
        setDeploy(ATTRIBUTE_DEFINITION_ID, definition);
    }

    public void setValue(String value) {
        setAttribute(ATTRIBUTE_VALUE, value);
    }

    public long getUserId() {
        return getAttributeValueAsLong(ATTRIBUTE_USER_ID);
    }

    public CustomUserInfoDefinitionItem getDefinition() {
        return (CustomUserInfoDefinitionItem) getDeploy(ATTRIBUTE_DEFINITION_ID);
    }

    public String getValue() {
        return getAttributeValue(ATTRIBUTE_VALUE);
    }
}
