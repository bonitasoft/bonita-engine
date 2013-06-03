package org.bonitasoft.engine.monitoring;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.monitoring.mbean.SEntityMXBean;
import org.bonitasoft.engine.monitoring.mbean.impl.SEntityMXBeanImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SEntityMXBeanTest extends CommonServiceTest {

    protected static MBeanServer mbserver = null;

    protected static ObjectName entityMB;

    protected static ObjectName serviceMB;

    private final String fakeUsername = "toto";

    private SUser fakeUser;

    private static TenantMonitoringService monitoringService;

    private static IdentityService identityService;

    private static long sessionId;

    private static long tenantId;

    static {
        monitoringService = getServicesBuilder().buildTenantMonitoringService(false);
        identityService = getServicesBuilder().buildIdentityService();
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        sessionId = getSessionAccessor().getSessionId();
        tenantId = getSessionAccessor().getTenantId();
    }

    public SEntityMXBean getEntityMXBean() throws Exception {
        final SEntityMXBean entityMbean = new SEntityMXBeanImpl(getTransactionService(), monitoringService, getSessionAccessor(), getSessionService());
        return entityMbean;
    }

    @Before
    public void disableMBeans() throws Exception {
        getSessionAccessor().setSessionInfo(sessionId, tenantId);
        final ArrayList<MBeanServer> mbservers = MBeanServerFactory.findMBeanServer(null);
        if (mbservers.size() > 0) {
            mbserver = mbservers.get(0);
        }
        if (mbserver == null) {
            mbserver = MBeanServerFactory.createMBeanServer();
        }
        // Constructs the mbean names

        entityMB = new ObjectName(TenantMonitoringService.ENTITY_MBEAN_PREFIX + tenantId);
        serviceMB = new ObjectName(TenantMonitoringService.SERVICE_MBEAN_PREFIX + tenantId);

        unregisterMBeans();
    }

    /**
     * Assure that no Bonitasoft MBeans are registered in the MBServer before each test.
     * 
     * @throws MBeanRegistrationException
     * @throws InstanceNotFoundException
     */
    public void unregisterMBeans() throws MBeanRegistrationException, InstanceNotFoundException {
        if (mbserver.isRegistered(entityMB)) {
            mbserver.unregisterMBean(entityMB);
        }
        if (mbserver.isRegistered(serviceMB)) {
            mbserver.unregisterMBean(serviceMB);
        }
    }

    public SUser createNewUser(final String user) {
        return getServicesBuilder().buildIdentityModelBuilder().getUserBuilder().createNewInstance().setUserName(user).setPassword("toto").done();
    }

    @Test
    public void getNumberOfUser() throws Exception {
        final SEntityMXBean entityMB = getEntityMXBean();
        entityMB.start();

        // fetch the number of users
        final long before = entityMB.getNumberOfUsers();

        createUser(identityService);

        long after = entityMB.getNumberOfUsers();

        // check if the number of user retrieved has been increased by one
        assertEquals(1, after - before);

        deleteUser(identityService);

        after = entityMB.getNumberOfUsers();
        assertEquals(0, after - before);

        // clean up
        entityMB.stop();
        // the previously created user has already been removed
    }

    private void deleteUser(final IdentityService identSvc) throws Exception {
        // SSession session = sessionService.createSession(tenantId, username);
        getSessionAccessor().setSessionInfo(sessionId, tenantId);
        getTransactionService().begin();
        // delete the previously created user
        identSvc.deleteUser(fakeUser);
        // end the transaction
        getTransactionService().complete();
        // sessionService.deleteSession(session.getId());
    }

    private void createUser(final IdentityService identSvc) throws Exception {
        // SSession session = sessionService.createSession(tenantId, username);
        // create a transaction
        getSessionAccessor().setSessionInfo(sessionId, tenantId);
        getTransactionService().begin();
        // create a fake user
        fakeUser = identSvc.createUser(createNewUser(fakeUsername));
        // end the transaction
        getTransactionService().complete();
        // sessionService.deleteSession(session.getId());
    }

    @Test
    public void mbServerGetNbOfUserTest() throws Exception {
        final SEntityMXBean entityMXB = getEntityMXBean();
        entityMXB.start();

        final String numberOfUsers = "NumberOfUsers";
        // fetch the number of users
        final long before = (Long) mbserver.getAttribute(entityMB, numberOfUsers);

        createUser(identityService);

        long after = (Long) mbserver.getAttribute(entityMB, numberOfUsers);

        // check if the number of user retrieved has been increased by one
        assertEquals(1, after - before);

        deleteUser(identityService);

        after = (Long) mbserver.getAttribute(entityMB, numberOfUsers);
        assertEquals(0, after - before);

        // clean up
        entityMXB.stop();
        // the previously created user has already been removed
    }

}
