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

import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchEventTriggerInstanceDescriptor;
import org.bonitasoft.engine.service.ModelConvertor;

/**
 * @author Celine Souchet
 * @version 6.4.0
 * @since 6.4.0
 */
public class SearchTimerEventTriggerInstances extends AbstractSearchEntity<TimerEventTriggerInstance, STimerEventTriggerInstance> {

    private final EventInstanceService eventInstanceService;

    private final long processInstanceId;

    public SearchTimerEventTriggerInstances(final EventInstanceService eventInstanceService,
            final SearchEventTriggerInstanceDescriptor searchEventTriggerInstanceDescriptor, final long processInstanceId, final SearchOptions searchOptions) {
        super(searchEventTriggerInstanceDescriptor, searchOptions);
        this.eventInstanceService = eventInstanceService;
        this.processInstanceId = processInstanceId;
    }

    @Override
    public long executeCount(final QueryOptions searchOptions) throws SBonitaReadException {
        return eventInstanceService.getNumberOfTimerEventTriggerInstances(processInstanceId, searchOptions);
    }

    @Override
    public List<STimerEventTriggerInstance> executeSearch(final QueryOptions searchOptions) throws SBonitaReadException {
        return eventInstanceService.searchTimerEventTriggerInstances(processInstanceId, searchOptions);
    }

    @Override
    public List<TimerEventTriggerInstance> convertToClientObjects(final List<STimerEventTriggerInstance> serverObjects) {
        return ModelConvertor.toTimerEventTriggerInstances(serverObjects);
    }

}
