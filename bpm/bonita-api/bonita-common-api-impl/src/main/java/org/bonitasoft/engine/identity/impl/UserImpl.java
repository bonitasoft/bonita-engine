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
package org.bonitasoft.engine.identity.impl;

import java.util.Date;

import org.bonitasoft.engine.identity.User;

/**
 * @author Matthieu Chaffotte
 * @author Feng Hui
 * @author Yanyan Liu
 * @author Celine Souchet
 */
public class UserImpl implements User {

    private static final long serialVersionUID = 4201480654772781891L;

    private final long id;

    private String firstName;

    private String lastName;

    private final String password;

    private final String userName;

    private String iconName;

    private String iconPath;

    private String title;

    private String jobTitle;

    private Date creationDate;

    private long createdBy;

    private Date lastUpdate;

    private Date lastConnection;

    private String managerUserName;

    private long managerUserId;

    private boolean enabled;

    public UserImpl(final long id, final String userName, final String password) {
        this.id = id;
        this.userName = userName;
        this.password = password;
    }

    public UserImpl(final User user) {
        id = user.getId();
        userName = user.getUserName();
        firstName = user.getFirstName();
        lastName = user.getLastName();
        password = user.getPassword();
        iconName = user.getIconName();
        iconPath = user.getIconPath();
        title = user.getTitle();
        jobTitle = user.getJobTitle();
        creationDate = user.getCreationDate();
        createdBy = user.getCreatedBy();
        lastUpdate = user.getLastUpdate();
        lastConnection = user.getLastConnection();
        managerUserId = user.getManagerUserId();
        managerUserName = user.getManagerUserName();
        enabled = user.isEnabled();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    @Deprecated
    public String getPassword() {
        return password;
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
    public Date getCreationDate() {
        return creationDate;
    }

    @Override
    public long getCreatedBy() {
        return createdBy;
    }

    @Override
    public Date getLastUpdate() {
        return lastUpdate;
    }

    @Override
    public Date getLastConnection() {
        return lastConnection;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getIconName() {
        return iconName;
    }

    @Override
    public String getIconPath() {
        return iconPath;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public void setJobTitle(final String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setCreatedBy(final long createdBy) {
        this.createdBy = createdBy;
    }

    public void setLastUpdate(final Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setLastConnection(final Date lastConnection) {
        this.lastConnection = lastConnection;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public void setIconName(final String iconName) {
        this.iconName = iconName;
    }

    public void setIconPath(final String iconPath) {
        this.iconPath = iconPath;
    }

    public void setManagerUserId(final long managerUserId) {
        this.managerUserId = managerUserId;
    }

    @Override
    public long getManagerUserId() {
        return managerUserId;
    }

    @Override
    public String getManagerUserName() {
        return managerUserName;
    }

    public void setManagerUserName(final String managerUserName) {
        this.managerUserName = managerUserName;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", userName=" + userName + ", managerUserId=" + managerUserId + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (createdBy ^ createdBy >>> 32);
        result = prime * result + (creationDate == null ? 0 : creationDate.hashCode());
        result = prime * result + (enabled ? 1231 : 1237);
        result = prime * result + (firstName == null ? 0 : firstName.hashCode());
        result = prime * result + (iconName == null ? 0 : iconName.hashCode());
        result = prime * result + (iconPath == null ? 0 : iconPath.hashCode());
        result = prime * result + (int) (id ^ id >>> 32);
        result = prime * result + (jobTitle == null ? 0 : jobTitle.hashCode());
        result = prime * result + (lastName == null ? 0 : lastName.hashCode());
        result = prime * result + (lastUpdate == null ? 0 : lastUpdate.hashCode());
        result = prime * result + (int) (managerUserId ^ managerUserId >>> 32);
        result = prime * result + (password == null ? 0 : password.hashCode());
        result = prime * result + (title == null ? 0 : title.hashCode());
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
        final UserImpl other = (UserImpl) obj;
        if (createdBy != other.createdBy) {
            return false;
        }
        if (creationDate == null) {
            if (other.creationDate != null) {
                return false;
            }
        } else if (!creationDate.equals(other.creationDate)) {
            return false;
        }
        if (enabled != other.enabled) {
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
        if (id != other.id) {
            return false;
        }
        if (jobTitle == null) {
            if (other.jobTitle != null) {
                return false;
            }
        } else if (!jobTitle.equals(other.jobTitle)) {
            return false;
        }
        if (lastName == null) {
            if (other.lastName != null) {
                return false;
            }
        } else if (!lastName.equals(other.lastName)) {
            return false;
        }
        if (lastUpdate == null) {
            if (other.lastUpdate != null) {
                return false;
            }
        } else if (!lastUpdate.equals(other.lastUpdate)) {
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
        return true;
    }

}
