/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.identity;

import java.util.Date;

import org.bonitasoft.engine.api.UserAPI;
import org.bonitasoft.engine.bpm.BonitaObject;

/**
 * represents a User inside the organization.
 *
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @see UserAPI
 * @since 6.0.0
 */
public interface User extends BonitaObject {

    /**
     * @return the user's id
     */
    long getId();

    /**
     * deprecated since 6.3.1
     *
     * @return the user's password
     */
    @Deprecated
    String getPassword();

    /**
     * @return the user's firstname
     */
    String getFirstName();

    /**
     * @return the user's lastname
     */
    String getLastName();

    /**
     * @return the user's username
     */
    String getUserName();

    /**
     * @return the user's icon name
     */
    String getIconName();

    /**
     * @return the user's icon path
     */
    String getIconPath();

    /**
     * @return the user's title
     */
    String getTitle();

    /**
     * @return the user's job title
     */
    String getJobTitle();

    /**
     * @return the user's creation date
     */
    Date getCreationDate();

    /**
     * @return the user's id that created the user
     */
    long getCreatedBy();

    /**
     * @return the user's last update date
     */
    Date getLastUpdate();

    /**
     * @return the user's last connection date
     */
    Date getLastConnection();

    /**
     * @return the user's manager id
     */
    long getManagerUserId();

    /**
     * @return true if the user is enabled
     */
    boolean isEnabled();

    /**
     * @deprecated As of 6.0 Use {@link #getManagerUserId()} instead
     */
    //FIXME Remove ASAP
    @Deprecated
    String getManagerUserName();

}
