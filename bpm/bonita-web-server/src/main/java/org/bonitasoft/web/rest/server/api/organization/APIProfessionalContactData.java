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
package org.bonitasoft.web.rest.server.api.organization;

import org.bonitasoft.web.rest.model.identity.ProfessionalContactDataDefinition;
import org.bonitasoft.web.rest.model.identity.ProfessionalContactDataItem;
import org.bonitasoft.web.rest.server.api.ConsoleAPI;
import org.bonitasoft.web.rest.server.datastore.organization.ProfessionalContactDataDatastore;
import org.bonitasoft.web.rest.server.framework.api.APIHasAdd;
import org.bonitasoft.web.rest.server.framework.api.APIHasGet;
import org.bonitasoft.web.rest.server.framework.api.APIHasUpdate;
import org.bonitasoft.web.rest.server.framework.api.Datastore;
import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Paul AMAR
 */
public class APIProfessionalContactData extends ConsoleAPI<ProfessionalContactDataItem>
        implements APIHasGet<ProfessionalContactDataItem>,
        APIHasUpdate<ProfessionalContactDataItem>, APIHasAdd<ProfessionalContactDataItem> {

    @Override
    protected ItemDefinition defineItemDefinition() {
        return Definitions.get(ProfessionalContactDataDefinition.TOKEN);
    }

    @Override
    protected Datastore defineDefaultDatastore() {
        return new ProfessionalContactDataDatastore(getEngineSession());
    }

    @Override
    public ProfessionalContactDataItem add(final ProfessionalContactDataItem item) {
        return super.add(item);
    }

}
