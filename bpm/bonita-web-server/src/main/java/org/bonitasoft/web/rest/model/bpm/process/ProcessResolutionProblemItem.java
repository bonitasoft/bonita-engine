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
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Séverin Moussel
 */
public class ProcessResolutionProblemItem extends Item {

    public ProcessResolutionProblemItem() {
        super();
    }

    public ProcessResolutionProblemItem(final IItem item) {
        super(item);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ATTRIBUTES
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String ATTRIBUTE_MESSAGE = "message";

    public static final String ATTRIBUTE_TARGET_TYPE = "target_type";

    public static final String ATTRIBUTE_RESSOURCE_ID = "ressource_id";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ATTRIBUTES VALUES
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String VALUE_STATE_TARGET_TYPE_CONNECTOR = "connector";

    public static final String VALUE_STATE_TARGET_TYPE_ACTOR = "actor";

    public static final String VALUE_STATE_TARGET_TYPE_PARAMETER = "parameter";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FILTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String FILTER_PROCESS_ID = "process_id";

    public String getMessage() {
        return getAttributeValue(ATTRIBUTE_MESSAGE);
    }

    public void setMessage(final String text) {
        setAttribute(ATTRIBUTE_MESSAGE, text);
    }

    public String getTargetType() {
        return getAttributeValue(ATTRIBUTE_TARGET_TYPE);
    }

    public void setTargetType(final String type) {
        setAttribute(ATTRIBUTE_TARGET_TYPE, type);
    }

    public String getRessourceId() {
        return getAttributeValue(ATTRIBUTE_RESSOURCE_ID);
    }

    public void setRessourceId(final String ressourceId) {
        setAttribute(ATTRIBUTE_RESSOURCE_ID, ressourceId);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UTILS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public ItemDefinition getItemDefinition() {
        return Definitions.get(ProcessResolutionProblemDefinition.TOKEN);
    }

}
