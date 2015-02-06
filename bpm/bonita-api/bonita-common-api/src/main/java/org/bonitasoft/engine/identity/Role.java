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
package org.bonitasoft.engine.identity;

import java.util.Date;

import org.bonitasoft.engine.bpm.BonitaObject;

/**
 * Represents a role inside the organization.
 *
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @see org.bonitasoft.engine.api.RoleAPI
 * @since 6.0.0
 */
public interface Role extends BonitaObject {

    /**
     * @return the role id
     */
    long getId();

    /**
     * @return the role's name
     */
    String getName();

    /**
     * @return the role's name to display
     */
    String getDisplayName();

    /**
     * @return the role's description
     */
    String getDescription();

    /**
     * @return the role's icon name
     */
    String getIconName();

    /**
     * @return the role's icon file path
     */
    String getIconPath();

    /**
     * @return the id of the user that created the role
     */
    long getCreatedBy();

    /**
     * @return the role's creation's date
     */
    Date getCreationDate();

    /**
     * @return the role's last update date
     */
    Date getLastUpdate();
}
