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
package org.bonitasoft.engine.test.persistence.builder;

import org.bonitasoft.engine.actor.mapping.model.SActor;

public class ActorBuilder extends PersistentObjectBuilder<SActor, ActorBuilder> {

    private long scopeId;
    private boolean initiator;

    @Override
    ActorBuilder getThisBuilder() {

        return this;
    }

    public ActorBuilder withScopeId(long scopeId) {
        this.scopeId = scopeId;
        return this;
    }

    public ActorBuilder whoIsInitiator() {
        this.initiator = true;
        return this;
    }

    public static ActorBuilder anActor() {
        return new ActorBuilder();
    }

    @Override
    SActor _build() {
        SActor sActor = new SActor();
        sActor.setInitiator(initiator);
        sActor.setScopeId(scopeId);
        return sActor;
    }

}
