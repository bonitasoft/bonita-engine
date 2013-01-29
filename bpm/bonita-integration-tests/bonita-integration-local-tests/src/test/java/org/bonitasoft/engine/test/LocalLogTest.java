package org.bonitasoft.engine.test;

import static org.junit.Assert.assertEquals;

import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;

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
    @Ignore("This test fails because Property 'org.bonitasoft.engine.services.queryablelog.disable' is only read at startup, so change is not taken into account")
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
