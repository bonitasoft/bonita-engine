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
package org.bonitasoft.engine.api.impl.transaction.event;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.model.event.SEventInstance;
import org.bonitasoft.engine.persistence.OrderByType;

/**
 * @author Elias Ricken de Medeiros
 */
public final class GetEventInstances implements TransactionContentWithResult<List<SEventInstance>> {

    private final long rootContainerId;

    private final EventInstanceService eventInstanceService;

    private final int fromIndex;

    private final int maxResults;

    private final String fieldName;

    private final OrderByType orderByType;

    private List<SEventInstance> eventInstances;

    public GetEventInstances(final EventInstanceService eventInstanceService, final long rootContainerId, final int fromIndex, final int maxResults,
            final String fieldName, final OrderByType orderByType) {
        this.rootContainerId = rootContainerId;
        this.eventInstanceService = eventInstanceService;
        this.fromIndex = fromIndex;
        this.maxResults = maxResults;
        this.fieldName = fieldName;
        this.orderByType = orderByType;
    }

    @Override
    public void execute() throws SBonitaException {
        this.eventInstances = this.eventInstanceService.getEventInstances(rootContainerId, fromIndex, maxResults, fieldName, orderByType);
    }

    @Override
    public List<SEventInstance> getResult() {
        return this.eventInstances;
    }

}
