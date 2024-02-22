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
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;

/**
 * @author Séverin Moussel
 */
public class ProcessConnectorDefinition extends ItemDefinition {

    /**
     * Singleton
     */
    public static ProcessConnectorDefinition get() {
        return (ProcessConnectorDefinition) Definitions.get(TOKEN);
    }

    public static final String TOKEN = "processconnector";

    private static final String API_URL = "../API/bpm/processConnector";

    @Override
    public String defineToken() {
        return TOKEN;
    }

    @Override
    protected String defineAPIUrl() {
        return API_URL;
    }

    @Override
    protected void definePrimaryKeys() {
        setPrimaryKeys(
                ProcessConnectorItem.ATTRIBUTE_PROCESS_ID,
                ProcessConnectorItem.ATTRIBUTE_NAME,
                ProcessConnectorItem.ATTRIBUTE_VERSION);
    }

    @Override
    protected void defineAttributes() {
        createAttribute(ProcessConnectorItem.ATTRIBUTE_NAME, ItemAttribute.TYPE.STRING)
                .isMandatory();
        createAttribute(ProcessConnectorItem.ATTRIBUTE_VERSION, ItemAttribute.TYPE.STRING)
                .isMandatory();
        createAttribute(ProcessConnectorItem.ATTRIBUTE_PROCESS_ID, ItemAttribute.TYPE.ITEM_ID)
                .isMandatory();
        createAttribute(ProcessConnectorItem.ATTRIBUTE_IMPLEMENTATION_NAME, ItemAttribute.TYPE.STRING)
                .isMandatory();
        createAttribute(ProcessConnectorItem.ATTRIBUTE_IMPLEMENTATION_VERSION, ItemAttribute.TYPE.STRING)
                .isMandatory();
        createAttribute(ProcessConnectorItem.ATTRIBUTE_CLASSNAME, ItemAttribute.TYPE.STRING)
                .isMandatory();
    }

    @Override
    protected void defineDeploys() {
        declareDeployable(ProcessConnectorItem.ATTRIBUTE_PROCESS_ID, ProcessDefinition.get());
    }

    @Override
    protected IItem _createItem() {
        return new ProcessConnectorItem();
    }
}
