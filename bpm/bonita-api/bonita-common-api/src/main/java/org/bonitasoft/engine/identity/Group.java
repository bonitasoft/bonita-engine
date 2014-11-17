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

import org.bonitasoft.engine.bpm.BonitaObject;

/**
 * represents a Group inside the organization.
 *
 * @author Lu Kai, Bole Zhang, Matthieu Chaffotte
 * @see GroupAPI
 * @since 6.0.0
 */
public interface Group extends BonitaObject {

    /**
     * @return the group's id
     */
    long getId();

    /**
     * @return the group's name
     */
    String getName();

    /**
     * @return the group's name to display
     */
    String getDisplayName();

    /**
     * @return the group's description
     */
    String getDescription();

    /**
     * @return the group's icon name
     */
    String getIconName();

    /**
     * @return the group's icon file path
     */
    String getIconPath();

    /**
     * @return the id of the user that created the group
     */
    long getCreatedBy();

    /**
     * @return the group's creation's date
     */
    Date getCreationDate();

    /**
     * @return the group's last update date
     */
    Date getLastUpdate();

    /**
     * @return the group's path
     */
    String getParentPath();

    /**
     * @return the group's full path (i.e. with its parent path)
     */
    String getPath();

}
