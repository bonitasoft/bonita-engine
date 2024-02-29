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

import org.bonitasoft.web.rest.model.bpm.cases.CaseDefinition;
import org.bonitasoft.web.rest.model.bpm.process.ProcessDefinition;
import org.bonitasoft.web.rest.model.identity.UserDefinition;
import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;
import org.bonitasoft.web.toolkit.client.data.item.attribute.modifier.ReplaceRegexpModifier;
import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.EnumValidator;
import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.StringRegexpValidator;

/**
 * @author Séverin Moussel
 */
public class FlowNodeDefinition extends ItemDefinition {

    public static final String TOKEN = "flownode";

    public static final String API_URL = "../API/bpm/flowNode";

    private static final String ATTRIBUTE_NAME_FORBIDDEN_CHARACTERS = ":/\\?#\\[\\]@!\\$&'\\(\\)\\*\\+,;=";

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
        createAttribute(FlowNodeItem.ATTRIBUTE_ID, ItemAttribute.TYPE.ITEM_ID);

        createAttribute(FlowNodeItem.ATTRIBUTE_NAME, ItemAttribute.TYPE.STRING)
                .isMandatory()
                .addValidator(new StringRegexpValidator(ATTRIBUTE_NAME_FORBIDDEN_CHARACTERS, true))
                .addInputModifier(new ReplaceRegexpModifier(ATTRIBUTE_NAME_FORBIDDEN_CHARACTERS, "_"));

        createAttribute(FlowNodeItem.ATTRIBUTE_DISPLAY_NAME, ItemAttribute.TYPE.STRING);

        createAttribute(FlowNodeItem.ATTRIBUTE_DESCRIPTION, ItemAttribute.TYPE.TEXT);

        createAttribute(FlowNodeItem.ATTRIBUTE_DISPLAY_DESCRIPTION, ItemAttribute.TYPE.TEXT);

        createAttribute(FlowNodeItem.ATTRIBUTE_STATE, ItemAttribute.TYPE.ENUM)
                .setDefaultValue(FlowNodeItem.VALUE_STATE_READY)
                .addValidator(new EnumValidator(
                        FlowNodeItem.VALUE_STATE_READY,
                        FlowNodeItem.VALUE_STATE_COMPLETED,
                        FlowNodeItem.VALUE_STATE_FAILED,
                        FlowNodeItem.VALUE_STATE_REPLAY,
                        FlowNodeItem.VALUE_STATE_SKIPPED));

        createAttribute(FlowNodeItem.ATTRIBUTE_CASE_ID, ItemAttribute.TYPE.ITEM_ID)
                .isMandatory();

        createAttribute(FlowNodeItem.ATTRIBUTE_ROOT_CONTAINER_ID, ItemAttribute.TYPE.ITEM_ID);

        createAttribute(FlowNodeItem.ATTRIBUTE_PROCESS_ID, ItemAttribute.TYPE.ITEM_ID)
                .isMandatory();

        createAttribute(FlowNodeItem.ATTRIBUTE_TYPE, ItemAttribute.TYPE.ENUM)
                .addValidator(new EnumValidator(
                        FlowNodeItem.VALUE_TYPE_USER_TASK,
                        FlowNodeItem.VALUE_TYPE_AUTOMATIC_TASK,
                        FlowNodeItem.VALUE_TYPE_MANUAL_TASK,
                        FlowNodeItem.VALUE_TYPE_BOUNDARY_EVENT,
                        FlowNodeItem.VALUE_TYPE_CALL_ACTIVITY,
                        FlowNodeItem.VALUE_TYPE_END_EVENT,
                        FlowNodeItem.VALUE_TYPE_GATEWAY,
                        FlowNodeItem.VALUE_TYPE_INTERMEDIATE_CATCH_EVENT,
                        FlowNodeItem.VALUE_TYPE_INTERMEDIATE_THROW_EVENT,
                        FlowNodeItem.VALUE_TYPE_LOOP_ACTIVITY,
                        FlowNodeItem.VALUE_TYPE_START_EVENT));

        createAttribute(FlowNodeItem.ATTRIBUTE_EXECUTED_BY_USER_ID, ItemAttribute.TYPE.ITEM_ID);
        createAttribute(FlowNodeItem.ATTRIBUTE_EXECUTED_BY_SUBSTITUTE_USER_ID, ItemAttribute.TYPE.ITEM_ID);
    }

    @Override
    protected void defineDeploys() {
        super.defineDeploys();
        declareDeployable(FlowNodeItem.ATTRIBUTE_PROCESS_ID, Definitions.get(ProcessDefinition.TOKEN));
        declareDeployable(FlowNodeItem.ATTRIBUTE_CASE_ID, Definitions.get(CaseDefinition.TOKEN));
        declareDeployable(FlowNodeItem.ATTRIBUTE_ROOT_CONTAINER_ID, Definitions.get(ProcessDefinition.TOKEN));
        declareDeployable(FlowNodeItem.ATTRIBUTE_EXECUTED_BY_USER_ID, UserDefinition.get());
        declareDeployable(FlowNodeItem.ATTRIBUTE_EXECUTED_BY_SUBSTITUTE_USER_ID, UserDefinition.get());
    }

    @Override
    protected void definePrimaryKeys() {
        setPrimaryKeys(FlowNodeItem.ATTRIBUTE_ID);
    }

    @Override
    protected IFlowNodeItem _createItem() {
        return new FlowNodeItem();
    }

    public static FlowNodeDefinition get() {
        return (FlowNodeDefinition) Definitions.get(TOKEN);
    }

}
