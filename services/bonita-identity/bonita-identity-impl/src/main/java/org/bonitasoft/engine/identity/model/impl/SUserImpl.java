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

import java.util.Objects;

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

    private long managerUserId;

    private String title;

    private String jobTitle;

    private long creationDate;

    private long createdBy;

    private long lastUpdate;

    private boolean enabled;

    private SUserLogin sUserLogin;

    private Long iconId;

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
        createdBy = user.getCreatedBy();
        creationDate = user.getCreationDate();
        lastUpdate = user.getLastUpdate();
        title = user.getTitle();
        enabled = user.isEnabled();
        iconId = user.getIconId();
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
    public Long getIconId() {
        return iconId;
    }

    public void setIconId(Long iconId) {
        this.iconId = iconId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        SUserImpl sUser = (SUserImpl) o;
        return managerUserId == sUser.managerUserId &&
                creationDate == sUser.creationDate &&
                createdBy == sUser.createdBy &&
                lastUpdate == sUser.lastUpdate &&
                enabled == sUser.enabled &&
                Objects.equals(firstName, sUser.firstName) &&
                Objects.equals(lastName, sUser.lastName) &&
                Objects.equals(password, sUser.password) &&
                Objects.equals(userName, sUser.userName) &&
                Objects.equals(title, sUser.title) &&
                Objects.equals(jobTitle, sUser.jobTitle) &&
                Objects.equals(sUserLogin, sUser.sUserLogin) &&
                Objects.equals(iconId, sUser.iconId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), firstName, lastName, password, userName, managerUserId, title, jobTitle, creationDate,
                createdBy, lastUpdate, enabled, sUserLogin, iconId);
    }

    @Override
    public String toString() {
        return "SUserImpl{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", userName='" + userName + '\'' +
                ", managerUserId=" + managerUserId +
                ", title='" + title + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", creationDate=" + creationDate +
                ", createdBy=" + createdBy +
                ", lastUpdate=" + lastUpdate +
                ", enabled=" + enabled +
                ", sUserLogin=" + sUserLogin +
                ", iconId=" + iconId +
                '}';
    }
}
