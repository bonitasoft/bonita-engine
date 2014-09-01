/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.monitoring;

import static org.junit.Assert.assertEquals;

import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.api.MonitoringAPI;

/**
 * @author Elias Ricken de Medeiros
 */
public class MonitoringAPITest extends CommonAPISPTest {

    @After
    public void afterTest() throws Exception {
       logoutOnTenant();
    }

    @Before
    public void beforeTest() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalLogger();
    }

    @Cover(classes = MonitoringAPI.class, concept = BPMNConcept.NONE, keywords = { "Monitoring", "User" }, story = "Get number of users.", jira = "")
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

    @Cover(classes = MonitoringAPITest.class, concept = BPMNConcept.NONE, keywords = { "Monitoring", "Executing process" }, story = "Get number of executing processes.", jira = "")
    @Test
    public void getNumberOfExecutingProcesses() throws BonitaException {
        final long numberOfActiveTransactions = getMonitoringAPI().getNumberOfExecutingProcesses();
        assertEquals(0, numberOfActiveTransactions);
    }

}
