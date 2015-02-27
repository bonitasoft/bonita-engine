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
package org.bonitasoft.engine.platform.session.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Elias Ricken de Medeiros
 */
public interface SPlatformSession extends Serializable {

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
     * Gets the user id associated to this session
     * 
     * @return the user Id for this session
     */
    long getUserId();

}
