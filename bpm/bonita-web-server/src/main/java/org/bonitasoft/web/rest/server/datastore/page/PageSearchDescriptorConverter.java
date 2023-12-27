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
package org.bonitasoft.web.rest.server.datastore.page;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.page.PageSearchDescriptor;
import org.bonitasoft.web.rest.model.portal.page.PageItem;
import org.bonitasoft.web.rest.server.datastore.converter.AttributeConverter;
import org.bonitasoft.web.toolkit.client.common.util.MapUtil;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute.TYPE;

/**
 * @author Emmanuel Duchastenier
 */
public class PageSearchDescriptorConverter implements AttributeConverter {

    private static final Map<String, String> attributeNameMapping = new HashMap<>();
    private static final Map<String, TYPE> valueTypeMapping = new HashMap<>();

    static {
        createMappings();
    }

    public Map<String, TYPE> getValueTypeMapping() {
        return valueTypeMapping;
    }

    private static void createMappings() {
        addAttributeConverterItem(PageItem.ATTRIBUTE_ID, PageSearchDescriptor.ID, TYPE.STRING);
        addAttributeConverterItem(PageItem.ATTRIBUTE_URL_TOKEN, PageSearchDescriptor.NAME, TYPE.STRING);
        addAttributeConverterItem(PageItem.ATTRIBUTE_DISPLAY_NAME, PageSearchDescriptor.DISPLAY_NAME, TYPE.STRING);
        addAttributeConverterItem(PageItem.ATTRIBUTE_IS_PROVIDED, PageSearchDescriptor.PROVIDED, TYPE.BOOLEAN);
        addAttributeConverterItem(PageItem.ATTRIBUTE_CREATED_BY_USER_ID, PageSearchDescriptor.INSTALLED_BY,
                TYPE.STRING);
        addAttributeConverterItem(PageItem.ATTRIBUTE_CREATION_DATE, PageSearchDescriptor.INSTALLATION_DATE,
                TYPE.STRING);
        addAttributeConverterItem(PageItem.ATTRIBUTE_LAST_UPDATE_DATE, PageSearchDescriptor.LAST_MODIFICATION_DATE,
                TYPE.STRING);
        //CONTENT_TYPE is managed differently in order to accept a OR with form and page
        //addAttributeConverterItem(PageItem.FILTER_CONTENT_TYPE, PageSearchDescriptor.CONTENT_TYPE, TYPE.STRING);
        addAttributeConverterItem(PageItem.ATTRIBUTE_PROCESS_ID, PageSearchDescriptor.PROCESS_DEFINITION_ID,
                TYPE.STRING);
    }

    @Override
    public String convert(final String attribute) {
        if (PageItem.FILTER_CONTENT_TYPE.equals(attribute)) {
            return MapUtil.getValue(attributeNameMapping, attribute, "");
        } else {
            return MapUtil.getMandatory(attributeNameMapping, attribute);
        }
    }

    private static void addAttributeConverterItem(String webSearchKey, String engineSearchKey, TYPE attributeType) {
        attributeNameMapping.put(webSearchKey, engineSearchKey);
        valueTypeMapping.put(webSearchKey, attributeType);
    }

}
