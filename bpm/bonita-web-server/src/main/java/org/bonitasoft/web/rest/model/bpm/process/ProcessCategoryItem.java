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

import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Séverin Moussel
 */
public class ProcessCategoryItem extends Item {

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ATTRIBUTES
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String ATTRIBUTE_PROCESS_ID = "process_id";

    public static final String ATTRIBUTE_CATEGORY_ID = "category_id";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SETTERS AND GETTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // SETTERS

    public final void setProcessId(final APIID id) {
        setAttribute(ATTRIBUTE_PROCESS_ID, id);
    }

    public final void setProcessId(final String id) {
        setAttribute(ATTRIBUTE_PROCESS_ID, id);
    }

    public final void setProcessId(final Long id) {
        setAttribute(ATTRIBUTE_PROCESS_ID, id);
    }

    public final void setCategoryId(final APIID id) {
        setAttribute(ATTRIBUTE_CATEGORY_ID, id);
    }

    public final void setCategoryId(final String id) {
        setAttribute(ATTRIBUTE_CATEGORY_ID, id);
    }

    public final void setCategoryId(final Long id) {
        setAttribute(ATTRIBUTE_CATEGORY_ID, id);
    }

    // GETTERS

    public final APIID getProcessId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_PROCESS_ID);
    }

    public final APIID getCategoryId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_CATEGORY_ID);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DEPLOYS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public final ProcessItem getProcess() {
        return new ProcessItem(getDeploy(ATTRIBUTE_PROCESS_ID));
    }

    public final CategoryItem getCategory() {
        return new CategoryItem(getDeploy(ATTRIBUTE_CATEGORY_ID));
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UTILS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public final ItemDefinition getItemDefinition() {
        return ProcessCategoryDefinition.get();
    }

}
