/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.command.system;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchWaitingEventSerchDescriptor;
import org.bonitasoft.engine.search.events.trigger.SearchWaitingEvents;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * Search Waiting events
 * Parameters ->
 * searchOptions: the searchOptions
 * 
 * @author Elias Ricken de Medeiros
 */
public class SearchWaitingEventsCommand extends CommandWithParameters {

    private static final String SEARCH_OPTIONS_KEY = "searchOptions";

    /**
     * @param parameters
     *        searchOptions: the searchOptions
     */
    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        final EventInstanceService eventInstanceService = serviceAccessor.getEventInstanceService();
        final SearchOptions searchOptions = getMandatoryParameter(parameters, SEARCH_OPTIONS_KEY, "Missing mandatory field: " + SEARCH_OPTIONS_KEY);
        final SearchWaitingEvents searchWaitingEvents = new SearchWaitingEvents(new SearchWaitingEventSerchDescriptor(), searchOptions,
                eventInstanceService);
        try {
            searchWaitingEvents.execute();
        } catch (final SBonitaException e) {
            throw new SCommandExecutionException(e);
        }
        return searchWaitingEvents.getResult();
    }

}
