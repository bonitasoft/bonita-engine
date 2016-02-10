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
 * Represents a group inside the organization.
 *
 * @author Lu Kai, Bole Zhang, Matthieu Chaffotte
 * @see org.bonitasoft.engine.api.GroupAPI
 * @since 6.0.0
 */
public interface Group extends BonitaObject {

    /**
     * @return The identifier of the group.
     */
    long getId();

    /**
     * @return The name of the group.
     */
    String getName();

    /**
     * @return The identifier of the group to display.
     */
    String getDisplayName();

    /**
     * @return The description of the group.
     */
    String getDescription();

    /**
     * @return The group's icon name
     */
    String getIconName();

    /**
     * @return The group's icon file path
     */
    String getIconPath();

    /**
     * @return The identifier of the user that created the group
     */
    long getCreatedBy();

    /**
     * @return The group's creation's date
     */
    Date getCreationDate();

    /**
     * @return The group's last update date
     */
    Date getLastUpdate();

    /**
     * @return The group's path
     */
    String getParentPath();

    /**
     * @return The group's full path (i.e. with its parent path)
     */
    String getPath();

}
