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
package org.bonitasoft.engine.core.login;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.model.SSession;

/**
 * @author Matthieu Chaffotte
 */
public interface LoginService {

    /**
     * generic login approach to handle outer authentication service like CAS or OAuth or whatever...
     * 
     * @param credentials
     *            the parameters to use to login
     * @return the session created if login succeeds
     * @throws SLoginException
     *             if login fails
     * @throws SUserNotFoundException 
     *             if the user does not exist in the database
     */
    SSession login(Map<String, Serializable> credentials) throws SLoginException, SUserNotFoundException;

    boolean isValid(final long sessionId);

    void logout(final long sessionId) throws SLoginException, SSessionNotFoundException;

}
