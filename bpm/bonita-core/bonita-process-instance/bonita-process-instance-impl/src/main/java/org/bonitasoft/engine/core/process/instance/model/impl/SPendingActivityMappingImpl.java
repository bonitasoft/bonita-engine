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
package org.bonitasoft.engine.core.process.instance.model.impl;

import org.bonitasoft.engine.core.process.instance.model.SPendingActivityMapping;

/**
 * @author Baptiste Mesta
 */
public class SPendingActivityMappingImpl extends SPersistenceObjectImpl implements SPendingActivityMapping {

    private static final long serialVersionUID = 5099656536197259953L;

    private long activityId;

    private long actorId;

    private long userId;

    public SPendingActivityMappingImpl() {
    }

    public SPendingActivityMappingImpl(final long activityId) {
        this.activityId = activityId;
    }

    @Override
    public long getActivityId() {
        return activityId;
    }

    @Override
    public long getActorId() {
        return actorId;
    }

    @Override
    public long getUserId() {
        return userId;
    }

    @Override
    public String getDiscriminator() {
        return SPendingActivityMappingImpl.class.getName();
    }

    public void setActivityId(final long activityId) {
        this.activityId = activityId;
    }

    public void setActorId(final long actorId) {
        this.actorId = actorId;
    }

    public void setUserId(final long userId) {
        this.userId = userId;
    }

}
