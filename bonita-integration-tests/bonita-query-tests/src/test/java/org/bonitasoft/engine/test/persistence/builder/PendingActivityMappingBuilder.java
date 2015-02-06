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
package org.bonitasoft.engine.test.persistence.builder;

import java.util.Random;

import org.bonitasoft.engine.core.process.instance.model.impl.SPendingActivityMappingImpl;

public class PendingActivityMappingBuilder extends PersistentObjectBuilder<SPendingActivityMappingImpl, PendingActivityMappingBuilder> {

    private long activityId = new Random().nextLong();

    private long userId;

    private long actorId;

    public static PendingActivityMappingBuilder aPendingActivityMapping() {
        return new PendingActivityMappingBuilder();
    }

    @Override
    PendingActivityMappingBuilder getThisBuilder() {
        return this;
    }

    @Override
    SPendingActivityMappingImpl _build() {
        SPendingActivityMappingImpl mapping = new SPendingActivityMappingImpl(activityId);
        mapping.setUserId(userId);
        mapping.setActorId(actorId);
        return mapping;
    }

    public PendingActivityMappingBuilder withUserId(final long userId) {
        this.userId = userId;
        return this;
    }

    public PendingActivityMappingBuilder withActorId(final long actorId) {
        this.actorId = actorId;
        return this;
    }

    public PendingActivityMappingBuilder withActivityId(final long activityId) {
        this.activityId = activityId;
        return this;
    }
}
