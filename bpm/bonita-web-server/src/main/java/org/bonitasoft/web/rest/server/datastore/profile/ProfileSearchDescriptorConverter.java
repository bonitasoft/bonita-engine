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
package org.bonitasoft.web.rest.server.datastore.profile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.profile.ProfileSearchDescriptor;
import org.bonitasoft.web.rest.model.portal.profile.ProfileItem;
import org.bonitasoft.web.rest.server.datastore.converter.AttributeConverter;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute.TYPE;

/**
 * @author Vincent Elcrin
 */
public class ProfileSearchDescriptorConverter implements AttributeConverter {

    private static final Map<String, String> mapping = new HashMap<>();

    static {
        mapping.put(ProfileItem.ATTRIBUTE_ID, ProfileSearchDescriptor.ID);
        mapping.put(ProfileItem.ATTRIBUTE_NAME, ProfileSearchDescriptor.NAME);
    }

    @Override
    public String convert(String attribute) {
        String descriptor = mapping.get(attribute);
        if (descriptor == null) {
            throw new RuntimeException(attribute + " has no valid search descriptor");
        }
        return descriptor;
    }

    @Override
    public Map<String, TYPE> getValueTypeMapping() {
        return Collections.emptyMap();
    }
}
