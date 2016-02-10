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
package org.bonitasoft.engine.search.events.trigger;

import java.util.List;

import org.bonitasoft.engine.bpm.flownode.WaitingEvent;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;
import org.bonitasoft.engine.service.ModelConvertor;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class SearchWaitingEvents extends AbstractSearchEntity<WaitingEvent, SWaitingEvent> {

    private final EventInstanceService eventInstanceService;

    public SearchWaitingEvents(final SearchEntityDescriptor searchDescriptor, final SearchOptions options, final EventInstanceService eventInstanceService) {
        super(searchDescriptor, options);
        this.eventInstanceService = eventInstanceService;
    }

    @Override
    public long executeCount(final QueryOptions searchOptions) throws SBonitaReadException {
        return eventInstanceService.getNumberOfWaitingEvents(SWaitingEvent.class, searchOptions);
    }

    @Override
    public List<SWaitingEvent> executeSearch(final QueryOptions searchOptions) throws SBonitaReadException {
        return eventInstanceService.searchWaitingEvents(SWaitingEvent.class, searchOptions);
    }

    @Override
    public List<WaitingEvent> convertToClientObjects(final List<SWaitingEvent> serverObjects) {
        return ModelConvertor.toWaitingEvents(serverObjects);
    }

}
