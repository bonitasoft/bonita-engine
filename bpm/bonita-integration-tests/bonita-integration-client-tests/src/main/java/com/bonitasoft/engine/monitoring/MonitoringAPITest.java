package com.bonitasoft.engine.monitoring;

import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.api.MonitoringAPI;

import static org.junit.Assert.assertEquals;

/**
 * @author Elias Ricken de Medeiros
 */
public class MonitoringAPITest extends CommonAPISPTest {

    @After
    public void afterTest() throws BonitaException {
        logout();
    }

    @Before
    public void beforeTest() throws BonitaException {
        login();
    }

    @Cover(classes = MonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Monitoring", "User" }, story = "Get number of users.")
    @Test
    public void getNumberOfUsers() throws Exception {
        long numberOfUsers = getMonitoringAPI().getNumberOfUsers();
        assertEquals(0L, numberOfUsers);

        getIdentityAPI().createUser("user", "pwsd");
        numberOfUsers = getMonitoringAPI().getNumberOfUsers();
        assertEquals(1L, numberOfUsers);

        getIdentityAPI().deleteUser("user");

        numberOfUsers = getMonitoringAPI().getNumberOfUsers();
        assertEquals(0L, numberOfUsers);
    }

    // FIXME: add this test when APIs use multi-tenancy
    // @Test
    // public void testGetNumberOfUsersMultiTenancy() throws Exception {
    // long numberOfUsers = getMonitoringAPI().getNumberOfUsers();
    // long t2NumberOfUsers = t2MonitoringAPI.getNumberOfUsers();
    // assertEquals(0L, numberOfUsers);
    // assertEquals(0L, t2NumberOfUsers);
    //
    // identityAPI.createUser("user", "pwsd");
    // numberOfUsers = getMonitoringAPI().getNumberOfUsers();
    // t2NumberOfUsers = t2MonitoringAPI.getNumberOfUsers();
    // assertEquals(1L, numberOfUsers);
    // assertEquals(0L, t2NumberOfUsers);
    //
    // t2identityAPI.createUser("usert2", "pswd");
    // numberOfUsers = getMonitoringAPI().getNumberOfUsers();
    // t2NumberOfUsers = t2MonitoringAPI.getNumberOfUsers();
    // assertEquals(1L, numberOfUsers);
    // assertEquals(1L, t2NumberOfUsers);
    //
    // identityAPI.deleteUser("user");
    //
    // numberOfUsers = getMonitoringAPI().getNumberOfUsers();
    // t2NumberOfUsers = t2MonitoringAPI.getNumberOfUsers();
    // assertEquals(0L, numberOfUsers);
    // assertEquals(1L, t2NumberOfUsers);
    //
    // t2identityAPI.deleteUser("usert2");
    //
    // numberOfUsers = getMonitoringAPI().getNumberOfUsers();
    // t2NumberOfUsers = t2MonitoringAPI.getNumberOfUsers();
    // assertEquals(0L, numberOfUsers);
    // assertEquals(0L, t2NumberOfUsers);
    // }

    @Cover(classes = MonitoringAPITest.class, concept = BPMNConcept.NONE, keywords = { "Monitoring", "Executing process" }, story = "Get number of executing processes.")
    @Test
    public void getNumberOfExecutingProcesses() throws BonitaException {
        final long numberOfActiveTransactions = getMonitoringAPI().getNumberOfExecutingProcesses();
        assertEquals(0, numberOfActiveTransactions);
    }

    @Cover(classes = MonitoringAPITest.class, concept = BPMNConcept.NONE, keywords = { "Monitoring", "Active transaction" }, story = "Get number of active transaction.")
    @Test
    public void getNumberOfActiveTransaction() throws BonitaException {
        final long numberOfActiveTransactions = getMonitoringAPI().getNumberOfActiveTransactions();
        assertEquals(0L, numberOfActiveTransactions);
    }

}
