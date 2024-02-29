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
package org.bonitasoft.web.rest.model.bpm.flownode;

import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;
import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.EnumValidator;

/**
 * User definition
 *
 * @author Séverin Moussel
 */
public class ActivityDefinition extends FlowNodeDefinition {

    public static final String TOKEN = "activity";

    protected static final String API_URL = "../API/bpm/activity";

    @Override
    public String defineToken() {
        return TOKEN;
    }

    @Override
    protected String defineAPIUrl() {
        return API_URL;
    }

    @Override
    protected void defineAttributes() {
        super.defineAttributes();

        // Restrict type to activities
        getAttribute(TaskItem.ATTRIBUTE_TYPE)
                .removeValidator(EnumValidator.class.getName())
                .addValidator(new EnumValidator(
                        TaskItem.VALUE_TYPE_USER_TASK,
                        TaskItem.VALUE_TYPE_AUTOMATIC_TASK,
                        TaskItem.VALUE_TYPE_MANUAL_TASK,
                        TaskItem.VALUE_TYPE_CALL_ACTIVITY,
                        TaskItem.VALUE_TYPE_LOOP_ACTIVITY));

        createAttribute(TaskItem.ATTRIBUTE_LAST_UPDATE_DATE, ItemAttribute.TYPE.DATETIME);

        createAttribute(TaskItem.ATTRIBUTE_REACHED_STATE_DATE, ItemAttribute.TYPE.DATETIME);

    }

    @Override
    public IActivityItem _createItem() {
        return new ActivityItem();
    }

    public static ActivityDefinition get() {
        return (ActivityDefinition) Definitions.get(TOKEN);
    }
}
