/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.impl.transaction.actor;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;

/**
 * @author Matthieu Chaffotte
 */
public class GetActor implements TransactionContentWithResult<SActor> {

    private final ActorMappingService actorMappingService;

    private final long actorId;

    private final String actorName;

    private final long scopeId;

    private SActor actor;

    public GetActor(final ActorMappingService actorMappingService, final long actorId) {
        super();
        this.actorMappingService = actorMappingService;
        this.actorId = actorId;
        actorName = null;
        scopeId = -1;
    }

    public GetActor(final ActorMappingService actorMappingService, final String actorName, final long scopeId) {
        super();
        this.actorMappingService = actorMappingService;
        this.actorName = actorName;
        this.scopeId = scopeId;
        actorId = -1;
    }

    @Override
    public void execute() throws SBonitaException {
        if (actorId > 0) {
            actor = actorMappingService.getActor(actorId);
        } else {
            actor = actorMappingService.getActor(actorName, scopeId);
        }
    }

    @Override
    public SActor getResult() {
        return actor;
    }

}
