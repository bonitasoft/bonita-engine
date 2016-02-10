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
package org.bonitasoft.engine.session.impl;

import java.util.Date;

import org.bonitasoft.engine.session.Session;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public abstract class SessionImpl implements Session {

    private static final long serialVersionUID = 6052091753899175734L;

    private long id;

    private Date creationDate;

    private long duration;

    private String userName;

    private long userId;

    private boolean technicalUser = false;

    public SessionImpl(final long id, final Date creationDate, final long duration, final String userName, final long userId) {
        super();
        this.id = id;
        this.creationDate = creationDate;
        this.duration = duration;
        this.userName = userName;
        this.userId = userId;
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
    public long getDuration() {
        return duration;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public long getUserId() {
        return userId;
    }

    @Override
    public boolean isTechnicalUser() {
        return technicalUser;
    }

    public void setTechnicalUser(final boolean technicalUser) {
        this.technicalUser = technicalUser;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setDuration(final long duration) {
        this.duration = duration;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public void setUserId(final long userId) {
        this.userId = userId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
        result = prime * result + (int) (duration ^ (duration >>> 32));
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + (technicalUser ? 1231 : 1237);
        result = prime * result + (int) (userId ^ (userId >>> 32));
        result = prime * result + ((userName == null) ? 0 : userName.hashCode());
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
        final SessionImpl other = (SessionImpl) obj;
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
        if (technicalUser != other.technicalUser) {
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

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("SessionImpl [id=");
        builder.append(id);
        builder.append(", creationDate=");
        builder.append(creationDate);
        builder.append(", duration=");
        builder.append(duration);
        builder.append(", userName=");
        builder.append(userName);
        builder.append(", userId=");
        builder.append(userId);
        builder.append(", technicalUser=");
        builder.append(technicalUser);
        builder.append("]");
        return builder.toString();
    }

}
