/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.session;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public interface Session extends Serializable {

    /**
     * Gets the session's identifier
     * 
     * @return
     */
    long getId();

    /**
     * Gets the session's creation date
     * 
     * @return
     */
    Date getCreationDate();

    /**
     * Gets the session's duration
     * 
     * @return
     */
    long getDuration();

    /**
     * Gets the user name associated to this session
     * 
     * @return
     */
    String getUserName();

    /**
     * Gets the user id associated to this session, if available (-1 if not)
     * 
     * @return the Id of the user
     */
    long getUserId();

    /**
     * Is the logged in user, the special technical user?
     * 
     * @return true if the user is the technical one, false otherwise.
     */
    boolean isTechnicalUser();

}
