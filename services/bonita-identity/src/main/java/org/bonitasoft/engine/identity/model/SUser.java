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
package org.bonitasoft.engine.identity.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Anthony Birembaut
 * @author Baptiste Mesta
 * @author Yanyan Liu
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
@Data
@ToString(exclude = "password")
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SUser implements PersistentObject, SHavingIcon {

    public static final String ID = "id";
    public static final String MANAGER_USER_ID = "managerUserId";
    public static final String JOB_TITLE = "jobTitle";
    public static final String TITLE = "title";
    public static final String LAST_NAME = "lastName";
    public static final String FIRST_NAME = "firstName";
    public static final String USER_NAME = "userName";
    public static final String PASSWORD = "password";
    public static final String LAST_UPDATE = "lastUpdate";
    public static final String LAST_CONNECTION = "lastConnection";
    public static final String CREATED_BY = "createdBy";
    public static final String CREATION_DATE = "creationDate";
    public static final String ENABLED = "enabled";
    private long id;
    private long tenantId;
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

    public SUser(final SUser user) {
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

    // called by reflection
    public void setLastConnection(final Long lastConnection) {
        sUserLogin.setLastConnection(lastConnection);
    }

}
