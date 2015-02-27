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
package org.bonitasoft.engine.platform.session.model.impl;

import java.util.Date;

import org.bonitasoft.engine.platform.session.model.SPlatformSession;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class SPlatformSessionImpl implements SPlatformSession {

    private static final long serialVersionUID = 1L;

    private final long id;

    private Date creationDate;

    private long duration;

    private Date lastRenewDate;

    private final String userName;

    private long userId;

    public SPlatformSessionImpl(final long id, final String username) {
        this.id = id;
        this.userName = username;
    }

    public SPlatformSessionImpl(final SPlatformSession session) {
        id = session.getId();
        creationDate = session.getCreationDate();
        duration = session.getDuration();
        lastRenewDate = session.getLastRenewDate();
        userName = session.getUserName();
        userId = session.getUserId();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    @Override
    public Date getLastRenewDate() {
        return lastRenewDate;
    }

    @Override
    public long getDuration() {
        return duration;
    }

    @Override
    public Date getExpirationDate() {
        return new Date(this.lastRenewDate.getTime() + duration);
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public long getUserId() {
        return userId;
    }

    public void setLastRenewDate(final Date lastRenewDate) {
        this.lastRenewDate = lastRenewDate;
    }

    public void setDuration(final long duration) {
        this.duration = duration;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (creationDate == null ? 0 : creationDate.hashCode());
        result = prime * result + (int) (duration ^ duration >>> 32);
        result = prime * result + (int) (id ^ id >>> 32);
        result = prime * result + (lastRenewDate == null ? 0 : lastRenewDate.hashCode());
        result = prime * result + (int) (userId ^ userId >>> 32);
        result = prime * result + (userName == null ? 0 : userName.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SPlatformSessionImpl other = (SPlatformSessionImpl) obj;
        if (creationDate == null) {
            if (other.creationDate != null) {
                return false;
            }
        } else if (!creationDate.equals(other.creationDate)) {
            return false;
        }
        if (duration != other.duration) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (lastRenewDate == null) {
            if (other.lastRenewDate != null) {
                return false;
            }
        } else if (!lastRenewDate.equals(other.lastRenewDate)) {
            return false;
        }
        if (userId != other.userId) {
            return false;
        }
        if (userName == null) {
            if (other.userName != null) {
                return false;
            }
        } else if (!userName.equals(other.userName)) {
            return false;
        }
        return true;
    }

}
