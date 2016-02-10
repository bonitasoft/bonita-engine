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
package org.bonitasoft.engine.session.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Elias Ricken de Medeiros
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 */
public interface SSession extends Serializable {

    /**
     * Gets the tenantId associated to this session
     * 
     * @return
     */
    long getTenantId();

    /**
     * Gets the session's id
     * 
     * @return
     */
    long getId();

    /**
     * Gets the session's creation date (GMT+0)
     * 
     * @return
     */
    Date getCreationDate();

    /**
     * Gets the session's last renew date (GMT+0)
     * 
     * @return
     */
    Date getLastRenewDate();

    /**
     * Gets the session's duration
     * 
     * @return
     */
    long getDuration();

    /**
     * Gets the session's expiration date (GMT+0)
     * 
     * @return
     */
    Date getExpirationDate();

    /**
     * Gets the user name associated to this session
     * 
     * @return
     */
    String getUserName();

    /**
     * @return the Id of the user
     */
    long getUserId();

    /**
     * Checks whether the user is the technical user.
     * 
     * @return true if the user is the technical user: false otherwise
     */
    boolean isTechnicalUser();

    /**
     * Get client's IP associated to this session
     * 
     * @return
     */
    String getClientIP();

    /**
     * Get the cluster node when the engine is running in a cluster
     * 
     * @return
     */
    String getClusterNode();

    /**
     * Get the application's name. For instance, BPM, Case, RULES
     * 
     * @return
     */
    String getApplicationName();

    /**
     * Get the client application's name. For instance, User XP
     * 
     * @return
     */
    String getClientApplicationName();

    boolean isValid();

}
