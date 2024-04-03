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
package org.bonitasoft.web.rest.server.datastore.bpm.flownode;

import java.util.Map;

import org.bonitasoft.engine.bpm.flownode.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.UserTaskInstance;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.bpm.flownode.UserTaskDefinition;
import org.bonitasoft.web.rest.model.bpm.flownode.UserTaskItem;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Séverin Moussel
 */
public class AbstractUserTaskDatastore<CONSOLE_ITEM extends UserTaskItem, ENGINE_ITEM extends UserTaskInstance>
        extends AbstractHumanTaskDatastore<CONSOLE_ITEM, ENGINE_ITEM> {

    public AbstractUserTaskDatastore(final APISession engineSession) {
        super(engineSession);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // C.R.U.D.S
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    @Override
    public CONSOLE_ITEM get(final APIID id) {
        try {

            // FIXME replace by getUserTaskInstance
            final HumanTaskInstance humanTaskInstance = getProcessAPI().getHumanTaskInstance(id.toLong());
            if (!(humanTaskInstance instanceof UserTaskInstance)) {
                throw new APIItemNotFoundException("User task", id);
            }

            return convertEngineToConsoleItem((ENGINE_ITEM) humanTaskInstance);
        } catch (final ActivityInstanceNotFoundException e) {
            throw new APIItemNotFoundException(UserTaskDefinition.TOKEN, id);
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    @Override
    protected SearchOptionsBuilder makeSearchOptionBuilder(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {

        final SearchOptionsBuilder builder = super.makeSearchOptionBuilder(page, resultsByPage, search, orders,
                filters);

        builder.filter(HumanTaskInstanceSearchDescriptor.PARENT_ACTIVITY_INSTANCE_ID, 0);

        return builder;
    }
}
