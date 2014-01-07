/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.services.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.builder.SUserBuilder;
import org.bonitasoft.engine.identity.model.builder.SUserBuilderFactory;
import org.bonitasoft.engine.test.util.TestUtil;
import org.bonitasoft.engine.transaction.STransactionCommitException;
import org.bonitasoft.engine.transaction.STransactionRollbackException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.monitoring.TenantMonitoringService;
import com.bonitasoft.engine.monitoring.mbean.MBeanStartException;
import com.bonitasoft.services.CommonServiceSPTest;

public abstract class TenantMonitoringServiceTest extends CommonServiceSPTest {

    private final TenantMonitoringService monitoringSvc;

    private static IdentityService identityService;

    private static MBeanServer mbserver = null;

    private static ObjectName entityMB;

    private static ObjectName serviceMB;

    protected abstract TenantMonitoringService getMonitoringService() throws Exception;

    public TenantMonitoringServiceTest() throws Exception {
        monitoringSvc = getMonitoringService();
    }

    static {
        identityService = getServicesBuilder().buildIdentityService();
    }

    @Before
    public void setup() throws Exception {

        final ArrayList<MBeanServer> mbservers = MBeanServerFactory.findMBeanServer(null);
        if (mbservers.size() > 0) {
            mbserver = mbservers.get(0);
        }
        if (mbserver == null) {
            mbserver = MBeanServerFactory.createMBeanServer();
        }

        final long tenantId = getSessionAccessor().getTenantId();
        // Constructs the mbean names
        entityMB = new ObjectName(TenantMonitoringService.ENTITY_MBEAN_PREFIX + tenantId);
        serviceMB = new ObjectName(TenantMonitoringService.SERVICE_MBEAN_PREFIX + tenantId);

        unregisterMBeans();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        // complete active transaction if assertion fails
        try {
            TestUtil.closeTransactionIfOpen(getTransactionService());
        } catch (final STransactionCommitException e) {
            // OK
        } catch (final STransactionRollbackException e) {
            // OK
        }
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

    @Test
    public void startMbeanAccessibility() throws NullPointerException, MBeanStartException {
        assertFalse(mbserver.isRegistered(entityMB));
        assertFalse(mbserver.isRegistered(serviceMB));

        monitoringSvc.registerMBeans();

        assertTrue(mbserver.isRegistered(entityMB));
        assertTrue(mbserver.isRegistered(serviceMB));
    }

    public SUser createNewUser(final String username, final String password) throws Exception {
        final SUserBuilder userBuilder = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName(username).setPassword(password);
        getTransactionService().begin();
        final SUser user = identityService.createUser(userBuilder.done());
        getTransactionService().complete();
        return user;
    }

    @Test
    public void getActiveTransactionTest() throws Exception {

        // assertEquals(0, svcMB.getActiveTransactionNb());
        assertEquals(0, monitoringSvc.getNumberOfActiveTransactions());

        // create a transaction
        getTransactionService().begin();
        // check the transaction has been successfully counted
        assertEquals(1, monitoringSvc.getNumberOfActiveTransactions());
        // close the transaction
        getTransactionService().complete();

        assertEquals(0, monitoringSvc.getNumberOfActiveTransactions());
    }

}
