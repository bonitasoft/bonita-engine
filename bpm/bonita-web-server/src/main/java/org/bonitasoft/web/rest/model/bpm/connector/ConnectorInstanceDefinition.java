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
package org.bonitasoft.web.rest.model.bpm.connector;

import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;

/**
 * @author Vincent Elcrin
 */
public class ConnectorInstanceDefinition extends ItemDefinition {

    public static final String TOKEN = "connectorInstance";

    protected static final String API_URL = "../API/bpm/connectorInstance";

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
        createAttribute(ConnectorInstanceItem.ATTRIBUTE_ID, ItemAttribute.TYPE.ITEM_ID);
        createAttribute(ConnectorInstanceItem.ATTRIBUTE_NAME, ItemAttribute.TYPE.STRING);
        createAttribute(ConnectorInstanceItem.ATTRIBUTE_CONNECTOR_ID, ItemAttribute.TYPE.ITEM_ID);
        createAttribute(ConnectorInstanceItem.ATTRIBUTE_VERSION, ItemAttribute.TYPE.STRING);
        createAttribute(ConnectorInstanceItem.ATTRIBUTE_STATE, ItemAttribute.TYPE.ENUM);
    }

    @Override
    protected void definePrimaryKeys() {
        setPrimaryKeys(ConnectorInstanceItem.ATTRIBUTE_ID);
    }

    @Override
    protected Item _createItem() {
        return new ConnectorInstanceItem();
    }
}
