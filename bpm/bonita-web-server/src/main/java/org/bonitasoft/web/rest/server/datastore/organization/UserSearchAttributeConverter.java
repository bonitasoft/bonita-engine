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
package org.bonitasoft.web.rest.server.datastore.organization;

import static org.bonitasoft.web.rest.model.identity.UserItem.*;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.identity.UserSearchDescriptor;
import org.bonitasoft.web.rest.server.datastore.converter.AttributeConverter;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute.TYPE;

/**
 * @author Colin PUY, Anthony Birembaut
 */
public class UserSearchAttributeConverter implements AttributeConverter {

    protected static final Map<String, String> mapping = new HashMap<>();
    private static final Map<String, TYPE> valueTypeMapping = new HashMap<>();

    static {
        addAttributeConverterItem(ATTRIBUTE_ID, UserSearchDescriptor.ID);
        addAttributeConverterItem(ATTRIBUTE_FIRSTNAME, UserSearchDescriptor.FIRST_NAME);
        addAttributeConverterItem(ATTRIBUTE_LASTNAME, UserSearchDescriptor.LAST_NAME);
        addAttributeConverterItem(ATTRIBUTE_USERNAME, UserSearchDescriptor.USER_NAME);
        addAttributeConverterItem(ATTRIBUTE_ENABLED, UserSearchDescriptor.ENABLED, TYPE.BOOLEAN);
        addAttributeConverterItem(ATTRIBUTE_MANAGER_ID, UserSearchDescriptor.MANAGER_USER_ID);
        addAttributeConverterItem(FILTER_GROUP_ID, UserSearchDescriptor.GROUP_ID);
        addAttributeConverterItem(FILTER_ROLE_ID, UserSearchDescriptor.ROLE_ID);
    }

    @Override
    public String convert(final String attribute) {
        return mapping.get(attribute);
    }

    private static void addAttributeConverterItem(String webSearchKey, String engineSearchKey, TYPE attributeType) {
        mapping.put(webSearchKey, engineSearchKey);
        valueTypeMapping.put(webSearchKey, attributeType);
    }

    private static void addAttributeConverterItem(String webSearchKey, String engineSearchKey) {
        addAttributeConverterItem(webSearchKey, engineSearchKey, TYPE.STRING);
    }

    @Override
    public Map<String, TYPE> getValueTypeMapping() {
        return valueTypeMapping;
    }

}
