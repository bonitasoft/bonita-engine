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

import static org.bonitasoft.web.rest.model.bpm.process.ActorItem.ATTRIBUTE_DESCRIPTION;
import static org.bonitasoft.web.rest.model.bpm.process.ActorItem.ATTRIBUTE_PROCESS_ID;
import static org.bonitasoft.web.toolkit.client.data.item.template.ItemHasDualName.ATTRIBUTE_DISPLAY_NAME;
import static org.bonitasoft.web.toolkit.client.data.item.template.ItemHasDualName.ATTRIBUTE_NAME;
import static org.bonitasoft.web.toolkit.client.data.item.template.ItemHasUniqueId.ATTRIBUTE_ID;

import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;

/**
 * @author Haojie Yuan
 * @author Séverin Moussel
 */
public class ActorDefinition extends ItemDefinition {

    /**
     * Singleton
     */
    public static ActorDefinition get() {
        return (ActorDefinition) Definitions.get(TOKEN);
    }

    public static final String TOKEN = "actor";

    @Override
    public String defineToken() {
        return TOKEN;
    }

    private static final String API_URL = "../API/bpm/actor";

    @Override
    protected String defineAPIUrl() {
        return API_URL;
    }

    @Override
    protected void definePrimaryKeys() {
        setPrimaryKeys(ATTRIBUTE_ID);
    }

    /**
     * categoryList
     */
    @Override
    protected void defineAttributes() {
        createAttribute(ATTRIBUTE_ID, ItemAttribute.TYPE.ITEM_ID);
        createAttribute(ATTRIBUTE_NAME, ItemAttribute.TYPE.STRING).isMandatory();
        createAttribute(ATTRIBUTE_DISPLAY_NAME, ItemAttribute.TYPE.STRING);
        createAttribute(ATTRIBUTE_DESCRIPTION, ItemAttribute.TYPE.TEXT);
        createAttribute(ATTRIBUTE_PROCESS_ID, ItemAttribute.TYPE.ITEM_ID).isMandatory();
    }

    @Override
    public IItem _createItem() {
        return new ActorItem();
    }

    @Override
    protected void defineDeploys() {
        declareDeployable(ATTRIBUTE_PROCESS_ID, ProcessDefinition.get());
    }

}
