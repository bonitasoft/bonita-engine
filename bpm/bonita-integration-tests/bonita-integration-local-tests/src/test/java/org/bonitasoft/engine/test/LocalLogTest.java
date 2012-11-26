/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.test;

import static org.junit.Assert.assertEquals;

import org.bonitasoft.engine.CommonAPISPTest;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class LocalLogTest extends CommonAPISPTest {

    @Before
    public void setUp() throws BonitaException {
        login();
    }

    @After
    public void tearDown() throws BonitaException {
        logout();
    }

    // run this test in local test suite only, otherwise it's necessary to use a command to set the system property on the server side
    @Test
    public void testDisableLogs() throws Exception {
        final int initNumberOfLogs = getLogAPI().getNumberOfLogs();
        User user1 = getIdentityAPI().createUser("user1WrongSortKey", "bpm");
        getIdentityAPI().deleteUser(user1.getId());
        int numberOfLogs = getLogAPI().getNumberOfLogs();
        assertEquals("Number of logs should have increase of 1!", initNumberOfLogs + 2, numberOfLogs);

        System.setProperty("org.bonitasoft.engine.services.queryablelog.disable", "true");

        user1 = getIdentityAPI().createUser("user1WrongSortKey", "bpm");
        getIdentityAPI().deleteUser(user1.getId());
        numberOfLogs = getLogAPI().getNumberOfLogs();

        assertEquals("Number of logs should not have changed!", initNumberOfLogs + 2, numberOfLogs);

        System.clearProperty("org.bonitasoft.engine.services.queryablelog.disable");
    }

}
