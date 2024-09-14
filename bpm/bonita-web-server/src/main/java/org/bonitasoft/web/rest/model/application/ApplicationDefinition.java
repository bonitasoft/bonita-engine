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
package org.bonitasoft.web.rest.model.application;

import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;

/**
 * Item definition for a Legacy Bonita Living Application for the REST API.
 */
public class ApplicationDefinition extends AbstractApplicationDefinition<ApplicationItem> {

    public static final String TOKEN = "application";

    @Override
    protected String defineToken() {
        return TOKEN;
    }

    @Override
    protected void defineAttributes() {
        super.defineAttributes();
        createAttribute(ApplicationItem.ATTRIBUTE_HOME_PAGE_ID, ItemAttribute.TYPE.ITEM_ID);
        createAttribute(ApplicationItem.ATTRIBUTE_LAYOUT_ID, ItemAttribute.TYPE.ITEM_ID);
        createAttribute(ApplicationItem.ATTRIBUTE_THEME_ID, ItemAttribute.TYPE.ITEM_ID);
    }

    @Override
    protected ApplicationItem _createItem() {
        return new ApplicationItem();
    }

    public static ApplicationDefinition get() {
        return (ApplicationDefinition) Definitions.get(TOKEN);
    }

}
