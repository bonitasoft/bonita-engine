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
package org.bonitasoft.engine.platform.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.PlatformCommandAPI;
import org.bonitasoft.engine.command.CommandDescriptor;
import org.bonitasoft.engine.command.CommandNotFoundException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.PlatformSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PlatformCommandIT extends CommonAPIIT {

    private static PlatformCommandAPI platformCommandAPI;

    private static PlatformSession session;

    @Before
    public void before() throws BonitaException, IOException {
        session = loginOnPlatform();
        platformCommandAPI = PlatformAPIAccessor.getPlatformCommandAPI(session);
    }

    @After
    public void after() throws BonitaException {
        logoutOnPlatform(session);
    }

    @Test
    public void createPlatformCommand() throws BonitaException {
        try {
            platformCommandAPI.addDependency("commands", "jar".getBytes());
            final CommandDescriptor command = platformCommandAPI.register("testPlatformCommand", "command description", "implementation");
            assertNotNull(command);
            assertEquals("testPlatformCommand", command.getName());
            assertEquals("command description", command.getDescription());
        } finally {
            platformCommandAPI.unregister("testPlatformCommand");
            platformCommandAPI.removeDependency("commands");
        }
    }

    @Test
    public void deletePlatformCommand() throws BonitaException {
        platformCommandAPI.addDependency("commands", "jar".getBytes());
        platformCommandAPI.register("platformCommand1", "command description", "implementation");
        final CommandDescriptor command = platformCommandAPI.getCommand("platformCommand1");
        assertNotNull(command);
        platformCommandAPI.unregister("platformCommand1");
        platformCommandAPI.removeDependency("commands");
        try {
            platformCommandAPI.getCommand("platformCommand1");
            fail("command should be deleted");
        } catch (final CommandNotFoundException ignored) {
        }
    }
}
