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
package org.bonitasoft.engine.identity.model.impl;

import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.SUserLogin;

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

    private String title;

    private String jobTitle;

    private long creationDate;

    private long createdBy;

    private long lastUpdate;

    private boolean enabled;

    private SUserLogin sUserLogin;

    public SUserImpl() {
        super();
    }

    public SUserImpl(final SUser user) {
        firstName = user.getFirstName();
        lastName = user.getLastName();
        password = user.getPassword();
        userName = user.getUserName();
        jobTitle = user.getJobTitle();
        managerUserId = user.getManagerUserId();
        iconName = user.getIconName();
        iconPath = user.getIconPath();
        createdBy = user.getCreatedBy();
        creationDate = user.getCreationDate();
        lastUpdate = user.getLastUpdate();
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
    public SUserLogin getSUserLogin() {
        return sUserLogin;
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

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public void setsUserLogin(final SUserLogin sUserLogin) {
        this.sUserLogin = sUserLogin;
    }

    public void setLastConnection(final Long lastConnection) {
        sUserLogin.setLastConnection(lastConnection);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SUserImpl)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final SUserImpl sUser = (SUserImpl) o;

        if (createdBy != sUser.createdBy) {
            return false;
        }
        if (creationDate != sUser.creationDate) {
            return false;
        }
        if (enabled != sUser.enabled) {
            return false;
        }
        if (lastUpdate != sUser.lastUpdate) {
            return false;
        }
        if (managerUserId != sUser.managerUserId) {
            return false;
        }
        if (firstName != null ? !firstName.equals(sUser.firstName) : sUser.firstName != null) {
            return false;
        }
        if (iconName != null ? !iconName.equals(sUser.iconName) : sUser.iconName != null) {
            return false;
        }
        if (iconPath != null ? !iconPath.equals(sUser.iconPath) : sUser.iconPath != null) {
            return false;
        }
        if (jobTitle != null ? !jobTitle.equals(sUser.jobTitle) : sUser.jobTitle != null) {
            return false;
        }
        if (lastName != null ? !lastName.equals(sUser.lastName) : sUser.lastName != null) {
            return false;
        }
        if (password != null ? !password.equals(sUser.password) : sUser.password != null) {
            return false;
        }
        if (sUserLogin != null ? !sUserLogin.equals(sUser.sUserLogin) : sUser.sUserLogin != null) {
            return false;
        }
        if (title != null ? !title.equals(sUser.title) : sUser.title != null) {
            return false;
        }
        if (userName != null ? !userName.equals(sUser.userName) : sUser.userName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (iconName != null ? iconName.hashCode() : 0);
        result = 31 * result + (iconPath != null ? iconPath.hashCode() : 0);
        result = 31 * result + (int) (managerUserId ^ managerUserId >>> 32);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (jobTitle != null ? jobTitle.hashCode() : 0);
        result = 31 * result + (int) (creationDate ^ creationDate >>> 32);
        result = 31 * result + (int) (createdBy ^ createdBy >>> 32);
        result = 31 * result + (int) (lastUpdate ^ lastUpdate >>> 32);
        result = 31 * result + (enabled ? 1 : 0);
        result = 31 * result + (sUserLogin != null ? sUserLogin.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SUserImpl{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", userName='" + userName + '\'' +
                ", iconName='" + iconName + '\'' +
                ", iconPath='" + iconPath + '\'' +
                ", managerUserId=" + managerUserId +
                ", title='" + title + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", creationDate=" + creationDate +
                ", createdBy=" + createdBy +
                ", lastUpdate=" + lastUpdate +
                ", enabled=" + enabled +
                ", sUserLogin=" + sUserLogin +
                '}';
    }
}
