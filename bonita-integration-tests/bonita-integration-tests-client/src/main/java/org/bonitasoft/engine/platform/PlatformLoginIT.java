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
package org.bonitasoft.engine.platform;

import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.SessionNotFoundException;
import org.bonitasoft.engine.session.impl.PlatformSessionImpl;
import org.junit.Test;

public class PlatformLoginIT extends CommonAPIIT {

    @Test(expected = InvalidPlatformCredentialsException.class)
    public void login_with_bad_credentials_should_throw_InvalidPlatformCredentialsException() throws BonitaException {
        PlatformAPIAccessor.getPlatformLoginAPI().login("bad", "bad");
    }

    @Test(expected = SessionNotFoundException.class)
    public void logout_with_bad_session_should_throw_SessionNotFound() throws BonitaException {
        PlatformAPIAccessor.getPlatformLoginAPI().logout(new PlatformSessionImpl(123L, null, -1L, null, -1L));
    }

}
