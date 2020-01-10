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
package org.bonitasoft.engine.session;

import java.io.Serializable;
import java.util.Date;

/**
 * Informations concerning the connected user
 *
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface Session extends Serializable {

    /**
     * @return The identifier of the session
     */
    long getId();

    /**
     * @return The creation date of the session
     */
    Date getCreationDate();

    /**
     * @return The duration of the session
     */
    long getDuration();

    /**
     * @return The user name associated to this session
     */
    String getUserName();

    /**
     * @return The identifier of the user associated to this session, if available (-1 if not)
     */
    long getUserId();

    /**
     * @return True if the logged user is the technical one, False otherwise.
     */
    boolean isTechnicalUser();

}
