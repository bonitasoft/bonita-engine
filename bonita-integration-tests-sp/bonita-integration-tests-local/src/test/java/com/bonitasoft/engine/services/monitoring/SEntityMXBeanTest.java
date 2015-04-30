/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.services.monitoring;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import com.bonitasoft.engine.CommonBPMServicesSPTest;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.builder.SUserBuilderFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.bonitasoft.engine.monitoring.TenantMonitoringService;
import com.bonitasoft.engine.monitoring.mbean.SEntityMXBean;
import com.bonitasoft.engine.monitoring.mbean.impl.SEntityMXBeanImpl;

public class SEntityMXBeanTest extends CommonBPMServicesSPTest {

    protected static MBeanServer mbserver = null;

    protected static ObjectName entityMB;

    protected static ObjectName serviceMB;

    private final String fakeUsername = "toto";

    private SUser fakeUser;

    private TenantMonitoringService monitoringService;

    private IdentityService identityService;

    public SEntityMXBeanTest() {
        monitoringService = getTenantAccessor().getTenantMonitoringService();
        identityService = getTenantAccessor().getIdentityService();
    }

    public SEntityMXBean getEntityMXBean() {
        return new SEntityMXBeanImpl(getTransactionService(), monitoringService, getSessionAccessor(), getTenantAccessor().getSessionService());
    }

    @Before
    public void disableMBeans() throws Exception {
        final ArrayList<MBeanServer> mbservers = MBeanServerFactory.findMBeanServer(null);
        if (mbservers.size() > 0) {
            mbserver = mbservers.get(0);
        }
        if (mbserver == null) {
            mbserver = MBeanServerFactory.createMBeanServer();
        }
        // Constructs the mbean names

        entityMB = new ObjectName(TenantMonitoringService.ENTITY_MBEAN_PREFIX + getDefaultTenantId());
        serviceMB = new ObjectName(TenantMonitoringService.SERVICE_MBEAN_PREFIX + getDefaultTenantId());

        unregisterMBeans();
    }

    /**
     * Assure that no Bonitasoft MBeans are registered in the MBServer before each test.
     * 
     * @throws javax.management.MBeanRegistrationException
     * @throws javax.management.InstanceNotFoundException
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
        return BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName(user).setPassword("toto").done();
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
