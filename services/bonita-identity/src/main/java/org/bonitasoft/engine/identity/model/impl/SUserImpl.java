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

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
@Data
@ToString(exclude = "password")
@NoArgsConstructor
public class SUserImpl implements SUser {

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

    // called by reflection
    public void setLastConnection(final Long lastConnection) {
        sUserLogin.setLastConnection(lastConnection);
    }
}
