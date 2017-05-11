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
package org.bonitasoft.engine.session.model.impl;

import java.util.Date;

import org.bonitasoft.engine.session.model.SSession;

/**
 * @author Elias Ricken de Medeiros
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 */
public class SSessionImpl implements SSession {

    private static final long serialVersionUID = 1L;

    private long tenantId;

    private final long id;

    private Date creationDate;

    private long duration;

    private Date lastRenewDate;

    private String userName;

    private long userId;

    private String clientIP;

    private String clusterNode;

    private String applicationName;

    private String clientApplicationName;

    private boolean technicalUser;

    public SSessionImpl(final long id, final long tenantId, final String userName, final String applicationName, final long userId) {
        this.id = id;
        this.tenantId = tenantId;
        this.userName = userName;
        this.applicationName = applicationName;
        this.userId = userId;
    }

    public SSessionImpl(final SSession session) {
        id = session.getId();
        tenantId = session.getTenantId();
        creationDate = session.getCreationDate();
        duration = session.getDuration();
        lastRenewDate = session.getLastRenewDate();
        userName = session.getUserName();
        userId = session.getUserId();
        technicalUser = session.isTechnicalUser();
        clientIP = session.getClientIP();
        clusterNode = session.getClusterNode();
        applicationName = session.getApplicationName();
        clientApplicationName = session.getClientApplicationName();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public Date getLastRenewDate() {
        return lastRenewDate;
    }

    public void setLastRenewDate(final Date lastRenewDate) {
        this.lastRenewDate = lastRenewDate;
    }

    @Override
    public long getDuration() {
        return duration;
    }

    public void setDuration(final long duration) {
        this.duration = duration;
    }

    @Override
    public Date getExpirationDate() {
        return new Date(lastRenewDate.getTime() + duration);
    }

    @Override
    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    @Override
    public long getTenantId() {
        return tenantId;
    }

    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public String getClientIP() {
        return clientIP;
    }

    public void setClientIP(final String clientIP) {
        this.clientIP = clientIP;
    }

    @Override
    public String getClusterNode() {
        return clusterNode;
    }

    public void setClusterNode(final String clusterNode) {
        this.clusterNode = clusterNode;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(final String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public String getClientApplicationName() {
        return clientApplicationName;
    }

    public void setClientApplicationName(final String clientApplicationName) {
        this.clientApplicationName = clientApplicationName;
    }

    @Override
    public boolean isValid() {
        return getExpirationDate().getTime() > System.currentTimeMillis();
    }

    @Override
    public long getUserId() {
        return userId;
    }

    public void setUserId(final long userId) {
        this.userId = userId;
    }

    @Override
    public boolean isTechnicalUser() {
        return technicalUser;
    }

    public void setTechnicalUser(final boolean technicalUser) {
        this.technicalUser = technicalUser;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (applicationName == null ? 0 : applicationName.hashCode());
        result = prime * result + (clientApplicationName == null ? 0 : clientApplicationName.hashCode());
        result = prime * result + (clientIP == null ? 0 : clientIP.hashCode());
        result = prime * result + (clusterNode == null ? 0 : clusterNode.hashCode());
        result = prime * result + (creationDate == null ? 0 : creationDate.hashCode());
        result = prime * result + (int) (duration ^ duration >>> 32);
        result = prime * result + (int) (id ^ id >>> 32);
        result = prime * result + (lastRenewDate == null ? 0 : lastRenewDate.hashCode());
        result = prime * result + (technicalUser ? 1231 : 1237);
        result = prime * result + (int) (tenantId ^ tenantId >>> 32);
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
        final SSessionImpl other = (SSessionImpl) obj;
        if (applicationName == null) {
            if (other.applicationName != null) {
                return false;
            }
        } else if (!applicationName.equals(other.applicationName)) {
            return false;
        }
        if (clientApplicationName == null) {
            if (other.clientApplicationName != null) {
                return false;
            }
        } else if (!clientApplicationName.equals(other.clientApplicationName)) {
            return false;
        }
        if (clientIP == null) {
            if (other.clientIP != null) {
                return false;
            }
        } else if (!clientIP.equals(other.clientIP)) {
            return false;
        }
        if (clusterNode == null) {
            if (other.clusterNode != null) {
                return false;
            }
        } else if (!clusterNode.equals(other.clusterNode)) {
            return false;
        }
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
        if (technicalUser != other.technicalUser) {
            return false;
        }
        if (tenantId != other.tenantId) {
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
