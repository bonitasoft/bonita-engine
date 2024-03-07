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
 * @author Gai Cuisha
 */
public class ProcessConnectorDependencyDefinition extends ItemDefinition {

    /**
     * Singleton
     */
    public static ProcessConnectorDependencyDefinition get() {
        return (ProcessConnectorDependencyDefinition) Definitions.get(TOKEN);
    }

    public static final String TOKEN = "processconnectordependency";

    /**
     * the URL of user resource
     */
    private static final String API_URL = "../API/bpm/processConnectorDependency";

    @Override
    public String defineToken() {
        return TOKEN;
    }

    @Override
    protected void definePrimaryKeys() {
        setPrimaryKeys(
                ProcessConnectorDependencyItem.ATTRIBUTE_PROCESS_ID,
                ProcessConnectorDependencyItem.ATTRIBUTE_CONNECTOR_NAME,
                ProcessConnectorDependencyItem.ATTRIBUTE_CONNECTOR_VERSION,
                ProcessConnectorDependencyItem.ATTRIBUTE_FILENAME);
    }

    @Override
    protected String defineAPIUrl() {
        return API_URL;
    }

    @Override
    protected void defineAttributes() {
        createAttribute(ProcessConnectorDependencyItem.ATTRIBUTE_PROCESS_ID, ItemAttribute.TYPE.ITEM_ID).isMandatory();
        createAttribute(ProcessConnectorDependencyItem.ATTRIBUTE_CONNECTOR_NAME, ItemAttribute.TYPE.STRING)
                .isMandatory();
        createAttribute(ProcessConnectorDependencyItem.ATTRIBUTE_CONNECTOR_VERSION, ItemAttribute.TYPE.STRING)
                .isMandatory();
        createAttribute(ProcessConnectorDependencyItem.ATTRIBUTE_FILENAME, ItemAttribute.TYPE.STRING).isMandatory();
    }

    @Override
    protected IItem _createItem() {
        return new ProcessConnectorDependencyItem();
    }
}
