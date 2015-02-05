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

import org.bonitasoft.engine.core.process.instance.model.SHiddenTaskInstance;

/**
 * @author Emmanuel Duchastenier
 */
public class SHiddenTaskInstanceImpl extends SPersistenceObjectImpl implements SHiddenTaskInstance {

    private static final long serialVersionUID = 4741308445943943461L;

    private long activityId;

    private long userId;

    public SHiddenTaskInstanceImpl() {
    }

    public SHiddenTaskInstanceImpl(final long activityId, final long userId) {
        this.activityId = activityId;
        this.userId = userId;
    }

    @Override
    public long getActivityId() {
        return activityId;
    }

    @Override
    public long getUserId() {
        return userId;
    }

    @Override
    public String getDiscriminator() {
        return SHiddenTaskInstanceImpl.class.getName();
    }

    public void setActivityId(final long activityId) {
        this.activityId = activityId;
    }

    public void setUserId(final long userId) {
        this.userId = userId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (activityId ^ activityId >>> 32);
        result = prime * result + (int) (userId ^ userId >>> 32);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SHiddenTaskInstanceImpl other = (SHiddenTaskInstanceImpl) obj;
        if (activityId != other.activityId) {
            return false;
        }
        if (userId != other.userId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("SHiddenTaskInstanceImpl [Id=");
        builder.append(getId());
        builder.append(", tenantId=");
        builder.append(getTenantId());
        builder.append(", activityId=");
        builder.append(activityId);
        builder.append(", userId=");
        builder.append(userId);
        builder.append("]");
        return builder.toString();
    }

}
