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
package org.bonitasoft.web.rest.model.applicationpage;

import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;

/**
 * @author Julien Mege
 */
public class ApplicationPageDefinition extends ItemDefinition<ApplicationPageItem> {

    public static final String TOKEN = "applicationpage";

    @Override
    protected String defineToken() {
        return TOKEN;
    }

    @Override
    protected String defineAPIUrl() {
        return "../API/living/application-page";
    }

    @Override
    protected void defineAttributes() {
        createAttribute(ApplicationPageItem.ATTRIBUTE_ID, ItemAttribute.TYPE.ITEM_ID);
        createAttribute(ApplicationPageItem.ATTRIBUTE_TOKEN, ItemAttribute.TYPE.STRING);
        createAttribute(ApplicationPageItem.ATTRIBUTE_APPLICATION_ID, ItemAttribute.TYPE.ITEM_ID);
        createAttribute(ApplicationPageItem.ATTRIBUTE_PAGE_ID, ItemAttribute.TYPE.ITEM_ID);
    }

    @Override
    protected void definePrimaryKeys() {
        setPrimaryKeys(ApplicationPageItem.ATTRIBUTE_ID);
    }

    @Override
    protected ApplicationPageItem _createItem() {
        return new ApplicationPageItem();
    }

    public static ApplicationPageDefinition get() {
        return (ApplicationPageDefinition) Definitions.get(TOKEN);
    }

}
