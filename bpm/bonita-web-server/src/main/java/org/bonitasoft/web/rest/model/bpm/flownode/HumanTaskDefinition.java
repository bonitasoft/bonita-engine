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

import org.bonitasoft.web.rest.model.bpm.process.ActorDefinition;
import org.bonitasoft.web.rest.model.identity.UserDefinition;
import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;
import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.EnumValidator;

/**
 * @author Séverin Moussel
 */
public class HumanTaskDefinition extends TaskDefinition {

    public static final String TOKEN = "humantask";

    private static final String API_URL = "../API/bpm/humanTask";

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

        // Restrict type to Humantasks
        getAttribute(TaskItem.ATTRIBUTE_TYPE)
                .removeValidator(EnumValidator.class.getName())
                .addValidator(new EnumValidator(
                        TaskItem.VALUE_TYPE_USER_TASK,
                        TaskItem.VALUE_TYPE_MANUAL_TASK));

        createAttribute(HumanTaskItem.ATTRIBUTE_ACTOR_ID, ItemAttribute.TYPE.ITEM_ID);

        createAttribute(HumanTaskItem.ATTRIBUTE_ASSIGNED_USER_ID, ItemAttribute.TYPE.ITEM_ID);

        createAttribute(HumanTaskItem.ATTRIBUTE_ASSIGNED_DATE, ItemAttribute.TYPE.DATETIME);

        createAttribute(HumanTaskItem.ATTRIBUTE_PRIORITY, ItemAttribute.TYPE.ENUM)
                .setDefaultValue(HumanTaskItem.VALUE_PRIORITY_NORMAL)
                .addValidator(new EnumValidator(
                        HumanTaskItem.VALUE_PRIORITY_LOWEST,
                        HumanTaskItem.VALUE_PRIORITY_UNDER_NORMAL,
                        HumanTaskItem.VALUE_PRIORITY_NORMAL,
                        HumanTaskItem.VALUE_PRIORITY_ABOVE_NORMAL,
                        HumanTaskItem.VALUE_PRIORITY_HIGHEST));

        createAttribute(HumanTaskItem.ATTRIBUTE_DUE_DATE, ItemAttribute.TYPE.DATETIME);
    }

    @Override
    protected void defineDeploys() {
        super.defineDeploys();
        declareDeployable(HumanTaskItem.ATTRIBUTE_ASSIGNED_USER_ID, Definitions.get(UserDefinition.TOKEN));
        declareDeployable(HumanTaskItem.ATTRIBUTE_ACTOR_ID, Definitions.get(ActorDefinition.TOKEN));
    }

    @Override
    public IHumanTaskItem _createItem() {
        return new HumanTaskItem();
    }

    public static HumanTaskDefinition get() {
        return (HumanTaskDefinition) Definitions.get(TOKEN);
    }
}
