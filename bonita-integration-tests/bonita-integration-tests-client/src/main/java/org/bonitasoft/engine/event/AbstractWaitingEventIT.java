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
package org.bonitasoft.engine.event;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.bpm.flownode.WaitingEvent;
import org.bonitasoft.engine.bpm.flownode.WaitingEventSearchDescriptor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;

/**
 * @author Elias Ricken de Medeiros
 */
public abstract class AbstractWaitingEventIT extends AbstractEventIT {

    private static final String SEARCH_WAITING_EVENTS_COMMAND = "searchWaitingEventsCommand";

    private static final String SEARCH_OPTIONS_KEY = "searchOptions";

    protected void checkNumberOfWaitingEvents(final String errorMessage, final String flowNodeName, final long expectedNbOfWaitingEvents)
            throws BonitaException {
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.filter(WaitingEventSearchDescriptor.FLOW_NODE_NAME, flowNodeName);

        final Map<String, Serializable> parameters = new HashMap<String, Serializable>(1);
        parameters.put(SEARCH_OPTIONS_KEY, searchOptionsBuilder.done());

        @SuppressWarnings("unchecked")
        final SearchResult<WaitingEvent> searchResult = (SearchResult<WaitingEvent>) getCommandAPI().execute(SEARCH_WAITING_EVENTS_COMMAND, parameters);
        assertEquals(errorMessage, expectedNbOfWaitingEvents, searchResult.getCount());
    }

    protected void checkNumberOfWaitingEvents(final String flowNodeName, final long expectedNbOfWaitingEvents) throws BonitaException {
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.filter(WaitingEventSearchDescriptor.FLOW_NODE_NAME, flowNodeName);

        final Map<String, Serializable> parameters = new HashMap<String, Serializable>(1);
        parameters.put(SEARCH_OPTIONS_KEY, searchOptionsBuilder.done());

        @SuppressWarnings("unchecked")
        final SearchResult<WaitingEvent> searchResult = (SearchResult<WaitingEvent>) getCommandAPI().execute(SEARCH_WAITING_EVENTS_COMMAND, parameters);
        assertEquals(expectedNbOfWaitingEvents, searchResult.getCount());
    }

    protected void checkNumberOfWaitingEventsInProcess(final String processName, final long expectedNbOfWaitingEvents) throws BonitaException {
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.filter(WaitingEventSearchDescriptor.PROCESS_NAME, processName);

        final Map<String, Serializable> parameters = new HashMap<String, Serializable>(1);
        parameters.put(SEARCH_OPTIONS_KEY, searchOptionsBuilder.done());

        @SuppressWarnings("unchecked")
        final SearchResult<WaitingEvent> searchResult = (SearchResult<WaitingEvent>) getCommandAPI().execute(SEARCH_WAITING_EVENTS_COMMAND, parameters);
        assertEquals(expectedNbOfWaitingEvents, searchResult.getCount());
    }

}
