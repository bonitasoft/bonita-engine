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
package org.bonitasoft.web.rest.server.datastore.applicationmenu;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.business.application.ApplicationMenuSearchDescriptor;
import org.bonitasoft.web.rest.model.applicationmenu.ApplicationMenuItem;
import org.bonitasoft.web.rest.server.datastore.converter.AttributeConverter;
import org.bonitasoft.web.toolkit.client.common.util.MapUtil;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;

/**
 * @author Julien Mege
 */
public class ApplicationMenuSearchDescriptorConverter implements AttributeConverter {

    private final Map<String, String> mapping;

    ApplicationMenuSearchDescriptorConverter() {
        mapping = createMapping();
    }

    private Map<String, String> createMapping() {
        final Map<String, String> mapping = new HashMap<>();
        mapping.put(ApplicationMenuItem.ATTRIBUTE_ID, ApplicationMenuSearchDescriptor.ID);
        mapping.put(ApplicationMenuItem.ATTRIBUTE_DISPLAY_NAME, ApplicationMenuSearchDescriptor.DISPLAY_NAME);
        mapping.put(ApplicationMenuItem.ATTRIBUTE_APPLICATION_ID, ApplicationMenuSearchDescriptor.APPLICATION_ID);
        mapping.put(ApplicationMenuItem.ATTRIBUTE_APPLICATION_PAGE_ID,
                ApplicationMenuSearchDescriptor.APPLICATION_PAGE_ID);
        mapping.put(ApplicationMenuItem.ATTRIBUTE_MENU_INDEX, ApplicationMenuSearchDescriptor.INDEX);
        mapping.put(ApplicationMenuItem.ATTRIBUTE_PARENT_MENU_ID, ApplicationMenuSearchDescriptor.PARENT_ID);
        return mapping;
    }

    @Override
    public String convert(final String attribute) {
        return MapUtil.getMandatory(mapping, attribute);
    }

    @Override
    public Map<String, ItemAttribute.TYPE> getValueTypeMapping() {
        return Collections.emptyMap();
    }

}
