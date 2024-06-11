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
package org.bonitasoft.web.rest.server.datastore.application;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.business.application.ApplicationSearchDescriptor;
import org.bonitasoft.web.rest.model.application.AbstractApplicationItem;
import org.bonitasoft.web.rest.model.application.ApplicationItem;
import org.bonitasoft.web.rest.server.datastore.converter.AttributeConverter;
import org.bonitasoft.web.toolkit.client.common.util.MapUtil;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;

public class ApplicationSearchDescriptorConverter implements AttributeConverter {

    private final Map<String, String> mapping;

    private final Map<String, ItemAttribute.TYPE> valueTypeMapping = new HashMap<>();

    public ApplicationSearchDescriptorConverter() {
        mapping = createMapping();
    }

    private Map<String, String> createMapping() {
        final Map<String, String> mapping = new HashMap<>();
        mapping.put(AbstractApplicationItem.ATTRIBUTE_ID, ApplicationSearchDescriptor.ID);
        mapping.put(AbstractApplicationItem.ATTRIBUTE_ADVANCED, ApplicationSearchDescriptor.ADVANCED);
        valueTypeMapping.put(AbstractApplicationItem.ATTRIBUTE_ADVANCED, ItemAttribute.TYPE.BOOLEAN);
        mapping.put(AbstractApplicationItem.ATTRIBUTE_TOKEN, ApplicationSearchDescriptor.TOKEN);
        mapping.put(AbstractApplicationItem.ATTRIBUTE_DISPLAY_NAME, ApplicationSearchDescriptor.DISPLAY_NAME);
        mapping.put(AbstractApplicationItem.ATTRIBUTE_STATE, ApplicationSearchDescriptor.STATE);
        mapping.put(AbstractApplicationItem.ATTRIBUTE_CREATED_BY, ApplicationSearchDescriptor.CREATED_BY);
        mapping.put(AbstractApplicationItem.ATTRIBUTE_CREATION_DATE, ApplicationSearchDescriptor.CREATION_DATE);
        mapping.put(AbstractApplicationItem.ATTRIBUTE_LAST_UPDATE_DATE, ApplicationSearchDescriptor.LAST_UPDATE_DATE);
        mapping.put(AbstractApplicationItem.ATTRIBUTE_UPDATED_BY, ApplicationSearchDescriptor.UPDATED_BY);
        mapping.put(AbstractApplicationItem.ATTRIBUTE_VERSION, ApplicationSearchDescriptor.VERSION);
        mapping.put(AbstractApplicationItem.ATTRIBUTE_PROFILE_ID, ApplicationSearchDescriptor.PROFILE_ID);
        mapping.put(AbstractApplicationItem.FILTER_USER_ID, ApplicationSearchDescriptor.USER_ID);

        mapping.put(ApplicationItem.ATTRIBUTE_LAYOUT_ID, ApplicationSearchDescriptor.LAYOUT_ID);
        mapping.put(ApplicationItem.ATTRIBUTE_THEME_ID, ApplicationSearchDescriptor.THEME_ID);

        return mapping;
    }

    @Override
    public String convert(final String attribute) {
        return MapUtil.getMandatory(mapping, attribute);
    }

    @Override
    public Map<String, ItemAttribute.TYPE> getValueTypeMapping() {
        return valueTypeMapping;
    }

}
