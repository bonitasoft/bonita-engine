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
package org.bonitasoft.web.rest.server.api.bpm.connector;

import org.bonitasoft.engine.bpm.connector.ConnectorInstanceCriterion;
import org.bonitasoft.web.rest.model.bpm.connector.ConnectorInstanceDefinition;
import org.bonitasoft.web.rest.model.bpm.connector.ConnectorInstanceItem;
import org.bonitasoft.web.rest.server.api.ConsoleAPI;
import org.bonitasoft.web.rest.server.datastore.bpm.connector.ConnectorInstanceDatastore;
import org.bonitasoft.web.rest.server.framework.api.APIHasSearch;
import org.bonitasoft.web.rest.server.framework.api.APIHasUpdate;
import org.bonitasoft.web.rest.server.framework.api.Datastore;
import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Vincent Elcrin
 */
public class APIConnectorInstance extends ConsoleAPI<ConnectorInstanceItem>
        implements APIHasSearch<ConnectorInstanceItem>, APIHasUpdate<ConnectorInstanceItem> {

    @Override
    protected ItemDefinition defineItemDefinition() {
        return Definitions.get(ConnectorInstanceDefinition.TOKEN);
    }

    @Override
    public String defineDefaultSearchOrder() {
        return ConnectorInstanceCriterion.NAME_ASC.name();
    }

    @Override
    protected Datastore defineDefaultDatastore() {
        return new ConnectorInstanceDatastore(getEngineSession());
    }
}
