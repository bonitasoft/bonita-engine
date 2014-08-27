/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.login;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.BPMRemoteTests;
import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.PlatformCommandAPI;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.session.SessionNotFoundException;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class PlatformLoginAPITest extends CommonAPITest {

    private static final String COMMAND_NAME = "deletePlatformSession";

    private static final String COMMAND_DEPENDENCY_NAME = "deletePlatformSessionCommand";

    private static PlatformLoginAPI platformLoginAPI;

    @Before
    public void before() throws BonitaException {
        platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
    }

    @Test(expected = SessionNotFoundException.class)
    public void testSessionNotFoundExceptionIsThrownAfterSessionDeletion() throws Exception {
        // login to create a session
        final PlatformSession sessionToDelete = platformLoginAPI.login("platformAdmin", "platform");

        // delete the session created by the login
        deleteSession(sessionToDelete.getId());

        // will throw SessionNotFoundException
        platformLoginAPI.logout(sessionToDelete);
    }

    private void deleteSession(final long sessionId) throws Exception {
        // create a new session to deploy and execute commands
        final PlatformSession session = platformLoginAPI.login("platformAdmin", "platform");
        final PlatformCommandAPI platformCommandAPI = PlatformAPIAccessor.getPlatformCommandAPI(session);

        // deploy and execute a command to delete a session
        final InputStream stream = BPMRemoteTests.class.getResourceAsStream("/session-commands.jar.bak");
        assertNotNull(stream);
        final byte[] byteArray = IOUtils.toByteArray(stream);
        stream.close();
        platformCommandAPI.addDependency(COMMAND_DEPENDENCY_NAME, byteArray);
        platformCommandAPI.register(COMMAND_NAME, "Delete a platform session", "org.bonitasoft.engine.command.DeletePlatformSessionCommand");
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("sessionId", sessionId);
        platformCommandAPI.execute(COMMAND_NAME, parameters);
        platformCommandAPI.unregister(COMMAND_NAME);
        platformCommandAPI.removeDependency(COMMAND_DEPENDENCY_NAME);

        // logout
        platformLoginAPI.logout(session);
    }
}
