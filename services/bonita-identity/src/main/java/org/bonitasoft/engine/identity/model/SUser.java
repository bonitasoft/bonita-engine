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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;

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
@Entity
@Table(name = "user_")
@IdClass(PersistentObjectId.class)
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
    @Id
    private long id;
    @Id
    private long tenantId;
    @Column
    private String firstName;
    @Column
    private String lastName;
    @Column
    private String password;
    @Column
    private String userName;
    @Column
    private long managerUserId;
    @Column
    private String title;
    @Column
    private String jobTitle;
    @Column
    private long creationDate;
    @Column
    private long createdBy;
    @Column
    private long lastUpdate;
    @Column
    private boolean enabled;
    @OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private SUserLogin sUserLogin;
    @Column(name = "iconid")
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
