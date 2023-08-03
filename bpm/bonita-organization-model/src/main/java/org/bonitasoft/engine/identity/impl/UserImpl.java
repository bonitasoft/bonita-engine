/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
import java.util.StringJoiner;

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

    private final String userName;

    private Long iconId;

    private String title;

    private String jobTitle;

    private Date creationDate;

    private long createdBy;

    private Date lastUpdate;

    private Date lastConnection;

    private long managerUserId;

    private boolean enabled;

    public UserImpl(final long id, final String userName) {
        this.id = id;
        this.userName = userName;
    }

    public UserImpl(final User user) {
        id = user.getId();
        userName = user.getUserName();
        firstName = user.getFirstName();
        lastName = user.getLastName();
        title = user.getTitle();
        jobTitle = user.getJobTitle();
        creationDate = user.getCreationDate();
        createdBy = user.getCreatedBy();
        lastUpdate = user.getLastUpdate();
        lastConnection = user.getLastConnection();
        managerUserId = user.getManagerUserId();
        enabled = user.isEnabled();
    }

    @Override
    public long getId() {
        return id;
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
    public Long getIconId() {
        return iconId;
    }

    public void setIconId(Long iconId) {
        this.iconId = iconId;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", UserImpl.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("firstName='" + firstName + "'")
                .add("lastName='" + lastName + "'")
                .add("userName='" + userName + "'")
                .add("iconId=" + iconId)
                .add("title='" + title + "'")
                .add("jobTitle='" + jobTitle + "'")
                .add("creationDate=" + creationDate)
                .add("createdBy=" + createdBy)
                .add("lastUpdate=" + lastUpdate)
                .add("lastConnection=" + lastConnection)
                .add("managerUserId=" + managerUserId)
                .add("enabled=" + enabled)
                .toString();
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
                Objects.equals(userName, user.userName) &&
                Objects.equals(iconId, user.iconId) &&
                Objects.equals(title, user.title) &&
                Objects.equals(jobTitle, user.jobTitle) &&
                Objects.equals(creationDate, user.creationDate) &&
                Objects.equals(lastUpdate, user.lastUpdate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, userName, iconId, title, jobTitle, creationDate, createdBy,
                lastUpdate, lastConnection, managerUserId, enabled);
    }
}
