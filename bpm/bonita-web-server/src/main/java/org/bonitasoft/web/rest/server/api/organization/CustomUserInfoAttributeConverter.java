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

import static org.bonitasoft.engine.identity.CustomUserInfoValueSearchDescriptor.*;
import static org.bonitasoft.web.rest.model.identity.CustomUserInfoItem.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.web.rest.server.datastore.converter.AttributeConverter;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;

/**
 * @author Vincent Elcrin
 */
public class CustomUserInfoAttributeConverter implements AttributeConverter {

    private static final Map<String, String> attributes = new HashMap<>();

    static {
        attributes.put(ATTRIBUTE_USER_ID, USER_ID);
        attributes.put(ATTRIBUTE_DEFINITION_ID, DEFINITION_ID);
        attributes.put(ATTRIBUTE_VALUE, VALUE);
    }

    @Override
    public String convert(String attribute) {
        return attributes.get(attribute);
    }

    @Override
    public Map<String, ItemAttribute.TYPE> getValueTypeMapping() {
        return Collections.emptyMap();
    }
}
