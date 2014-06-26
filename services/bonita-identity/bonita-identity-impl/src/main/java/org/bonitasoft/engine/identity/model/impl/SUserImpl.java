/**
 * Copyright (C) 2009-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.identity.model.impl;

import org.bonitasoft.engine.identity.model.SUser;

/**
 * @author Anthony Birembaut
 * @author Baptiste Mesta
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public class SUserImpl extends SPersistentObjectImpl implements SUser {

    private static final long serialVersionUID = 9149996775946617264L;

    private String firstName;

    private String lastName;

    private String password;

    private String userName;

    private String iconName;

    private String iconPath;

    private long managerUserId;

    private String delegeeUserName;

    private String title;

    private String jobTitle;

    private long creationDate;

    private long createdBy;

    private long lastUpdate;

    private Long lastConnection;

    private boolean enabled;

    public SUserImpl() {
        super();
    }

    public SUserImpl(final SUser user) {
        firstName = user.getFirstName();
        lastName = user.getLastName();
        password = user.getPassword();
        userName = user.getUserName();
        jobTitle = user.getJobTitle();
        delegeeUserName = user.getDelegeeUserName();
        managerUserId = user.getManagerUserId();
        iconName = user.getIconName();
        iconPath = user.getIconPath();
        createdBy = user.getCreatedBy();
        creationDate = user.getCreationDate();
        lastUpdate = user.getLastUpdate();
        lastConnection = user.getLastConnection();
        title = user.getTitle();
        enabled = user.isEnabled();
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getJobTitle() {
        return jobTitle;
    }

    @Override
    public String getDelegeeUserName() {
        return delegeeUserName;
    }

    @Override
    public String getDiscriminator() {
        return SUser.class.getName();
    }

    @Override
    public long getManagerUserId() {
        return managerUserId;
    }

    @Override
    public String getIconName() {
        return iconName;
    }

    @Override
    public String getIconPath() {
        return iconPath;
    }

    @Override
    public long getCreationDate() {
        return creationDate;
    }

    @Override
    public long getCreatedBy() {
        return createdBy;
    }

    @Override
    public long getLastUpdate() {
        return lastUpdate;
    }

    @Override
    public Long getLastConnection() {
        return lastConnection;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public void setUserName(final String username) {
        userName = username;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public void setJobTitle(final String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public void setManagerUserId(final long managerUserId) {
        this.managerUserId = managerUserId;
    }

    public void setDelegeeUserName(final String delegeeUserName) {
        this.delegeeUserName = delegeeUserName;
    }

    public void setIconName(final String iconName) {
        this.iconName = iconName;
    }

    public void setIconPath(final String iconPath) {
        this.iconPath = iconPath;
    }

    public void setCreationDate(final long creationDate) {
        this.creationDate = creationDate;
    }

    public void setCreatedBy(final long createdBy) {
        this.createdBy = createdBy;
    }

    public void setLastUpdate(final long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setLastConnection(final Long lastConnection) {
        this.lastConnection = lastConnection;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (createdBy ^ createdBy >>> 32);
        result = prime * result + (int) (creationDate ^ creationDate >>> 32);
        result = prime * result + (delegeeUserName == null ? 0 : delegeeUserName.hashCode());
        result = prime * result + (firstName == null ? 0 : firstName.hashCode());
        result = prime * result + (iconName == null ? 0 : iconName.hashCode());
        result = prime * result + (iconPath == null ? 0 : iconPath.hashCode());
        result = prime * result + (jobTitle == null ? 0 : jobTitle.hashCode());
        result = prime * result + (lastConnection == null ? 0 : lastConnection.hashCode());
        result = prime * result + (lastName == null ? 0 : lastName.hashCode());
        result = prime * result + (int) (lastUpdate ^ lastUpdate >>> 32);
        result = prime * result + (int) (managerUserId ^ managerUserId >>> 32);
        result = prime * result + (password == null ? 0 : password.hashCode());
        result = prime * result + (title == null ? 0 : title.hashCode());
        result = prime * result + (userName == null ? 0 : userName.hashCode());
        result = prime * result + Boolean.valueOf(enabled).hashCode();
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
        final SUserImpl other = (SUserImpl) obj;
        if (createdBy != other.createdBy) {
            return false;
        }
        if (creationDate != other.creationDate) {
            return false;
        }
        if (delegeeUserName == null) {
            if (other.delegeeUserName != null) {
                return false;
            }
        } else if (!delegeeUserName.equals(other.delegeeUserName)) {
            return false;
        }
        if (firstName == null) {
            if (other.firstName != null) {
                return false;
            }
        } else if (!firstName.equals(other.firstName)) {
            return false;
        }
        if (iconName == null) {
            if (other.iconName != null) {
                return false;
            }
        } else if (!iconName.equals(other.iconName)) {
            return false;
        }
        if (iconPath == null) {
            if (other.iconPath != null) {
                return false;
            }
        } else if (!iconPath.equals(other.iconPath)) {
            return false;
        }
        if (jobTitle == null) {
            if (other.jobTitle != null) {
                return false;
            }
        } else if (!jobTitle.equals(other.jobTitle)) {
            return false;
        }
        if (lastConnection == null) {
            if (other.lastConnection != null) {
                return false;
            }
        } else if (!lastConnection.equals(other.lastConnection)) {
            return false;
        }
        if (lastName == null) {
            if (other.lastName != null) {
                return false;
            }
        } else if (!lastName.equals(other.lastName)) {
            return false;
        }
        if (lastUpdate != other.lastUpdate) {
            return false;
        }
        if (managerUserId != other.managerUserId) {
            return false;
        }
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        if (title == null) {
            if (other.title != null) {
                return false;
            }
        } else if (!title.equals(other.title)) {
            return false;
        }
        if (userName == null) {
            if (other.userName != null) {
                return false;
            }
        } else if (!userName.equals(other.userName)) {
            return false;
        }
        if (enabled != other.enabled) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SUserImpl (" + getId() + ") [firstName=" + firstName + ", lastName=" + lastName + ", password=" + password + ", userName=" + userName
                + ", iconName=" + iconName + ", iconPath=" + iconPath + ", managerUserId=" + managerUserId + ", delegeeUserName=" + delegeeUserName
                + ", title=" + title + ", jobTitle=" + jobTitle + ", creationDate=" + creationDate + ", createdBy=" + createdBy + ", lastUpdate=" + lastUpdate
                + ", lastConnection=" + lastConnection + ", enabled=" + enabled + "]";
    }

}
