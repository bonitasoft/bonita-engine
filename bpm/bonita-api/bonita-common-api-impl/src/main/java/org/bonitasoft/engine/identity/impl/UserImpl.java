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
import java.util.Objects;

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

    private Long iconId;

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
        return iconId != null ? iconId.toString() : "";
    }

    @Override
    public String getIconPath() {
        return iconId != null ? iconId.toString() : "";
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
    public Long getIconId() {
        return iconId;
    }

    public void setIconId(Long iconId) {
        this.iconId = iconId;
    }

    @Override
    public String toString() {
        return "UserImpl{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", userName='" + userName + '\'' +
                ", iconId=" + iconId +
                ", title='" + title + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", creationDate=" + creationDate +
                ", createdBy=" + createdBy +
                ", lastUpdate=" + lastUpdate +
                ", lastConnection=" + lastConnection +
                ", managerUserName='" + managerUserName + '\'' +
                ", managerUserId=" + managerUserId +
                ", enabled=" + enabled +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserImpl user = (UserImpl) o;
        return id == user.id &&
                createdBy == user.createdBy &&
                managerUserId == user.managerUserId &&
                enabled == user.enabled &&
                Objects.equals(firstName, user.firstName) &&
                Objects.equals(lastName, user.lastName) &&
                Objects.equals(password, user.password) &&
                Objects.equals(userName, user.userName) &&
                Objects.equals(iconId, user.iconId) &&
                Objects.equals(title, user.title) &&
                Objects.equals(jobTitle, user.jobTitle) &&
                Objects.equals(creationDate, user.creationDate) &&
                Objects.equals(lastUpdate, user.lastUpdate) &&
                Objects.equals(managerUserName, user.managerUserName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, password, userName, iconId, title, jobTitle, creationDate, createdBy, lastUpdate, lastConnection,
                managerUserName, managerUserId, enabled);
    }
}
