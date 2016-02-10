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
package org.bonitasoft.engine.api.impl.transaction.actor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;

/**
 * @author Feng Hui
 */
public class GetActorInitiators implements TransactionContentWithResult<List<SActor>> {

    private final ActorMappingService actorMappingService;

    private final Set<Long> actorIds;

    private final List<SActor> sActors = new ArrayList<SActor>();

    public GetActorInitiators(final ActorMappingService actorMappingService, final Set<Long> actorIds) {
        this.actorMappingService = actorMappingService;
        this.actorIds = actorIds;
    }

    @Override
    public void execute() throws SBonitaException {
        for (final Iterator<Long> iterator = actorIds.iterator(); iterator.hasNext();) {
            final SActor sActor = actorMappingService.getActor(iterator.next());
            if (sActor.isInitiator()) {
                sActors.add(sActor);
            }
        }
    }

    @Override
    public List<SActor> getResult() {
        return sActors;
    }

}
