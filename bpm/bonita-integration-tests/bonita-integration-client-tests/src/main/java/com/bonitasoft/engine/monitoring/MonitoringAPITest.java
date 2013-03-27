package com.bonitasoft.engine.monitoring;

import static org.junit.Assert.assertEquals;

import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.bonitasoft.engine.api.MonitoringAPI;
import com.bonitasoft.engine.api.TenantAPIAccessor;

/**
 * @author Elias Ricken de Medeiros
 */
public class MonitoringAPITest {

    private static MonitoringAPI monitoringAPI;

    private static APISession session;

    private static IdentityAPI identityAPI;

    @BeforeClass
    public static void beforeClass() throws BonitaException {
        APITestUtil.initializeAndStartPlatformWithDefaultTenant(true);
        session = APITestUtil.loginDefaultTenant();
        monitoringAPI = TenantAPIAccessor.getMonitoringAPI(session);
        identityAPI = TenantAPIAccessor.getIdentityAPI(session);
    }

    @AfterClass
    public static void afterClass() throws BonitaException {
        if (session != null) {
            APITestUtil.logoutTenant(session);
        }
        APITestUtil.stopAndCleanPlatformAndTenant(true);
    }

    public MonitoringAPITest() throws BonitaException {
        session = APITestUtil.loginDefaultTenant();
        monitoringAPI = TenantAPIAccessor.getMonitoringAPI(session);
    }

    @Cover(classes = MonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Monitoring", "User" }, story = "Get number of users.")
    @Test
    public void testGetNumberOfUsers() throws Exception {
        long numberOfUsers = monitoringAPI.getNumberOfUsers();
        assertEquals(0L, numberOfUsers);

        identityAPI.createUser("user", "pwsd");
        numberOfUsers = monitoringAPI.getNumberOfUsers();
        assertEquals(1L, numberOfUsers);

        identityAPI.deleteUser("user");

        numberOfUsers = monitoringAPI.getNumberOfUsers();
        assertEquals(0L, numberOfUsers);
    }

    // FIXME: add this test when APIs use multi-tenancy
    // @Test
    // public void testGetNumberOfUsersMultiTenancy() throws Exception {
    // long numberOfUsers = monitoringAPI.getNumberOfUsers();
    // long t2NumberOfUsers = t2MonitoringAPI.getNumberOfUsers();
    // assertEquals(0L, numberOfUsers);
    // assertEquals(0L, t2NumberOfUsers);
    //
    // identityAPI.createUser("user", "pwsd");
    // numberOfUsers = monitoringAPI.getNumberOfUsers();
    // t2NumberOfUsers = t2MonitoringAPI.getNumberOfUsers();
    // assertEquals(1L, numberOfUsers);
    // assertEquals(0L, t2NumberOfUsers);
    //
    // t2identityAPI.createUser("usert2", "pswd");
    // numberOfUsers = monitoringAPI.getNumberOfUsers();
    // t2NumberOfUsers = t2MonitoringAPI.getNumberOfUsers();
    // assertEquals(1L, numberOfUsers);
    // assertEquals(1L, t2NumberOfUsers);
    //
    // identityAPI.deleteUser("user");
    //
    // numberOfUsers = monitoringAPI.getNumberOfUsers();
    // t2NumberOfUsers = t2MonitoringAPI.getNumberOfUsers();
    // assertEquals(0L, numberOfUsers);
    // assertEquals(1L, t2NumberOfUsers);
    //
    // t2identityAPI.deleteUser("usert2");
    //
    // numberOfUsers = monitoringAPI.getNumberOfUsers();
    // t2NumberOfUsers = t2MonitoringAPI.getNumberOfUsers();
    // assertEquals(0L, numberOfUsers);
    // assertEquals(0L, t2NumberOfUsers);
    // }

    @Cover(classes = MonitoringAPITest.class, concept = BPMNConcept.NONE, keywords = { "Monitoring", "Executing process" }, story = "Get number of executing processes.")
    @Test
    public void getNumberOfExecutingProcesses() throws BonitaException {
        final long numberOfActiveTransactions = monitoringAPI.getNumberOfExecutingProcesses();
        assertEquals(0, numberOfActiveTransactions);
    }

    @Cover(classes = MonitoringAPITest.class, concept = BPMNConcept.NONE, keywords = { "Monitoring", "Active transaction" }, story = "Get number of active transaction.")
    @Test
    public void getNumberOfActiveTransaction() throws BonitaException {
        final long numberOfActiveTransactions = monitoringAPI.getNumberOfActiveTransactions();
        assertEquals(0L, numberOfActiveTransactions);
    }

}
