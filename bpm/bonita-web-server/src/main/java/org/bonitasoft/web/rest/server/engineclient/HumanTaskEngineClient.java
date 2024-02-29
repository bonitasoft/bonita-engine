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
package org.bonitasoft.web.rest.server.engineclient;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.web.rest.model.bpm.flownode.HumanTaskItem;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;

/**
 * @author Colin PUY
 * @author Elias Ricken de Medeiros
 */
public class HumanTaskEngineClient {

    private final ProcessAPI processAPI;

    public HumanTaskEngineClient(final ProcessAPI processAPI) {
        this.processAPI = processAPI;
    }

    public long countOpenedHumanTasks() {
        final SearchOptions search = new SearchOptionsBuilder(0, 0)
                .filter(HumanTaskInstanceSearchDescriptor.STATE_NAME, HumanTaskItem.VALUE_STATE_READY).done();
        try {
            return processAPI.searchHumanTaskInstances(search).getCount();
        } catch (final BonitaException e) {
            throw new APIException("Error when counting opened cases", e);
        }
    }

}
