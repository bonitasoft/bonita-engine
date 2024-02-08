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
package org.bonitasoft.web.rest.model.bpm.process;

import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute.TYPE;

/**
 * @author Séverin Moussel
 */
public class ProcessCategoryDefinition extends ItemDefinition {

    /**
     * Singleton
     */
    public static ProcessCategoryDefinition get() {
        return (ProcessCategoryDefinition) Definitions.get(TOKEN);
    }

    public static final String TOKEN = "processcategory";

    public static final String API_URL = "../API/bpm/processCategory";

    @Override
    protected String defineToken() {
        return TOKEN;
    }

    @Override
    protected String defineAPIUrl() {
        return API_URL;
    }

    @Override
    protected void defineAttributes() {
        createAttribute(ProcessCategoryItem.ATTRIBUTE_PROCESS_ID, TYPE.ITEM_ID).isMandatory();
        createAttribute(ProcessCategoryItem.ATTRIBUTE_CATEGORY_ID, TYPE.ITEM_ID).isMandatory();
    }

    @Override
    protected void definePrimaryKeys() {
        setPrimaryKeys(
                ProcessCategoryItem.ATTRIBUTE_PROCESS_ID,
                ProcessCategoryItem.ATTRIBUTE_CATEGORY_ID);
    }

    @Override
    protected IItem _createItem() {
        return new ProcessCategoryItem();
    }
}
