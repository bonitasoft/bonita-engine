/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.platform;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.platform.Platform;
import org.bonitasoft.engine.platform.PlatformLoginException;
import org.bonitasoft.engine.platform.PlatformLogoutException;
import org.bonitasoft.engine.platform.PlatformNotFoundException;
import org.bonitasoft.engine.platform.PlatformNotStartedException;
import org.bonitasoft.engine.platform.PlatformState;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.session.impl.PlatformSessionImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.PlatformAPIAccessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SPPlatformTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SPPlatformTest.class);

    private static final String DEFAULT_TENANT_NAME = "lukai";

    private static PlatformAPI platformAPI;

    private static PlatformLoginAPI platformLoginAPI;

    private static PlatformSession session;

    @BeforeClass
    public static void beforeClass() throws BonitaException, BonitaHomeNotSetException {
        platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
        logAsPlatformAdmin();
        try {
            platformAPI.initializePlatform();
        } catch (final CreationException e) {
            // Platform already created
        }
        platformAPI.startNode();
    }

    private static void logAsPlatformAdmin() throws PlatformLoginException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        session = platformLoginAPI.login("platformAdmin", "platform");
        platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
    }

    @AfterClass
    public static void afterClass() throws BonitaException {
        if (PlatformState.STARTED.equals(platformAPI.getPlatformState())) {
            platformAPI.stopNode();
        }
        platformAPI.cleanPlatform();
        platformLoginAPI.logout(session);
    }

    @Rule
    public TestRule testWatcher = new TestWatcher() {

        @Override
        public void starting(final Description d) {
            LOGGER.info("Starting test: " + getClass().getName() + "." + d.getMethodName());
        }

        @Override
        public void failed(@SuppressWarnings("unused") final Throwable cause, final Description d) {
            LOGGER.info("Failed test: " + getClass().getName() + "." + d.getMethodName());
        }

        @Override
        public void succeeded(final Description d) {
            LOGGER.info("Succeeded test: " + getClass().getName() + "." + d.getMethodName());
        }

    };

    private void deleteATenant(final long tenantId) throws BonitaException {
        platformAPI.deleteTenant(tenantId);
    }

    private long createATenant(final String tenantName) throws BonitaException {
        return platformAPI.createTenant(new TenantCreator(tenantName, "", "testIconName", "testIconPath", "default_tenant", "default_password"));
    }

    @Test(expected = PlatformLoginException.class)
    public void testWrongLogin() throws Exception {
        try {
            platformLoginAPI.logout(session);
            platformLoginAPI.login("titi", "toto");
            fail();
        } finally {
            logAsPlatformAdmin();
        }
    }

    @Test(expected = InvalidSessionException.class)
    public void useAnAPIWithInvalidSession() throws Exception {
        try {
            platformLoginAPI.logout(session);
            platformAPI.searchTenants(new SearchOptionsBuilder(0, 1000).done());
            fail();
        } finally {
            logAsPlatformAdmin();
        }
    }

    @Test(expected = PlatformLogoutException.class)
    public void testLogoutWithWrongSession() throws Exception {
        try {
            platformLoginAPI.logout(new PlatformSessionImpl(123l, null, -1l, null, -1l));
        } finally {
            platformLoginAPI.logout(session);
            logAsPlatformAdmin();
        }
    }

    @Test
    public void testSearchTenants() throws Exception {
        // create tenant
        final List<Tenant> listTenant = new ArrayList<Tenant>();
        for (int i = 0; i < 10; i++) {
            final long tenantID = platformAPI.createTenant(new TenantCreator("tenantName" + i, "test search tenant ", "testIconName" + i, "testIconPath" + i,
                    "username" + i, "123"));
            final Tenant tenant = platformAPI.getTenantById(tenantID);
            listTenant.add(tenant);
        }

        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        // according to name sort
        builder.sort("name", Order.ASC);
        SearchResult<Tenant> searchResult = platformAPI.searchTenants(builder.done());
        assertEquals(10, searchResult.getCount());
        List<Tenant> tenantList = searchResult.getResult();
        assertNotNull(tenantList);
        assertEquals(10, tenantList.size());
        for (int j = 0; j < 10; j++) {
            assertEquals(listTenant.get(j).getId(), tenantList.get(j).getId());
        }

        builder = new SearchOptionsBuilder(1, 5);
        builder.sort("name", Order.DESC);
        searchResult = platformAPI.searchTenants(builder.done());
        assertEquals(10, searchResult.getCount());
        tenantList = searchResult.getResult();
        assertNotNull(tenantList);
        assertEquals(5, tenantList.size());
        for (int j = 0; j < 5; j++) {
            assertEquals(listTenant.get(4 - j).getId(), tenantList.get(j).getId());
        }

        // according to state sort
        builder = new SearchOptionsBuilder(0, 10);
        builder.sort("status", Order.ASC);
        searchResult = platformAPI.searchTenants(builder.done());
        assertEquals(10, searchResult.getCount());
        tenantList = searchResult.getResult();
        assertNotNull(tenantList);
        assertEquals(10, tenantList.size());
        for (int j = 0; j < 10; j++) {
            assertEquals(listTenant.get(j).getId(), tenantList.get(j).getId());
        }

        builder = new SearchOptionsBuilder(1, 5);
        builder.sort("status", Order.DESC);
        searchResult = platformAPI.searchTenants(builder.done());
        assertEquals(10, searchResult.getCount());
        tenantList = searchResult.getResult();
        assertNotNull(tenantList);
        assertEquals(5, tenantList.size());
        for (int j = 0; j < 5; j++) {
            assertEquals(listTenant.get(5 + j).getId(), tenantList.get(j).getId());
        }

        builder = new SearchOptionsBuilder(0, 10);
        builder.sort("created", Order.ASC);
        searchResult = platformAPI.searchTenants(builder.done());
        assertEquals(10, searchResult.getCount());
        tenantList = searchResult.getResult();
        assertNotNull(tenantList);
        assertEquals(10, tenantList.size());
        for (int j = 0; j < 10; j++) {
            assertEquals(listTenant.get(j).getId(), tenantList.get(j).getId());
        }

        builder = new SearchOptionsBuilder(1, 5);
        builder.sort("created", Order.DESC);
        searchResult = platformAPI.searchTenants(builder.done());
        assertEquals(10, searchResult.getCount());
        tenantList = searchResult.getResult();
        assertNotNull(tenantList);
        assertEquals(5, tenantList.size());
        for (int j = 0; j < 5; j++) {
            assertEquals(listTenant.get(4 - j).getId(), tenantList.get(j).getId());
        }

        // according to name search
        builder = new SearchOptionsBuilder(0, 5);
        builder.searchTerm("tenantName2");
        searchResult = platformAPI.searchTenants(builder.done());
        assertEquals(1, searchResult.getCount());
        tenantList = searchResult.getResult();
        assertNotNull(tenantList);
        assertEquals(1, tenantList.size());
        assertEquals(listTenant.get(2).getId(), tenantList.get(0).getId());

        // according to iconName search
        builder = new SearchOptionsBuilder(0, 5);
        builder.searchTerm("testIconName2");
        searchResult = platformAPI.searchTenants(builder.done());
        assertEquals(1, searchResult.getCount());
        tenantList = searchResult.getResult();
        assertNotNull(tenantList);
        assertEquals(1, tenantList.size());
        assertEquals(listTenant.get(2).getId(), tenantList.get(0).getId());

        // update some tenants
        builder = new SearchOptionsBuilder(0, 10);
        builder.filter("status", "DEACTIVATED");
        searchResult = platformAPI.searchTenants(builder.done());
        assertEquals(10, searchResult.getCount());
        tenantList = searchResult.getResult();
        assertNotNull(tenantList);
        assertEquals(10, tenantList.size());
        for (int j = 0; j < 10; j++) {
            assertEquals("DEACTIVATED", tenantList.get(j).getState());
        }

        for (int k = 0; k < 5; k++) {
            final TenantUpdater udpateDescriptor = new TenantUpdater();
            udpateDescriptor.setStatus("ACTIVATED");
            platformAPI.updateTenant(listTenant.get(k).getId(), udpateDescriptor);
        }

        builder = new SearchOptionsBuilder(0, 5);
        builder.filter("status", "ACTIVATED");
        searchResult = platformAPI.searchTenants(builder.done());
        assertEquals(5, searchResult.getCount());
        tenantList = searchResult.getResult();
        assertNotNull(tenantList);
        assertEquals(5, tenantList.size());
        for (int k = 0; k < 5; k++) {
            assertEquals("ACTIVATED", tenantList.get(k).getState());
        }

        builder = new SearchOptionsBuilder(0, 5);
        builder.filter("status", "DEACTIVATED");
        searchResult = platformAPI.searchTenants(builder.done());
        assertEquals(5, searchResult.getCount());
        tenantList = searchResult.getResult();
        assertNotNull(tenantList);
        assertEquals(5, tenantList.size());
        for (int k = 0; k < 5; k++) {
            assertEquals("DEACTIVATED", tenantList.get(k).getState());
        }

        for (int k = 0; k < 5; k++) {
            final TenantUpdater udpateDescriptor = new TenantUpdater();
            udpateDescriptor.setStatus("DEACTIVATED");
            platformAPI.updateTenant(listTenant.get(k).getId(), udpateDescriptor);
        }

        // delete all tenants
        for (final Tenant t : listTenant) {
            platformAPI.deleteTenant(t.getId());
        }

    }

    @Test
    public void isPlatformCreated() throws BonitaException {
        assertTrue(platformAPI.isPlatformCreated());
    }

    @Test(expected = CreationException.class)
    public void createPlatformException() throws BonitaException {
        assertTrue(platformAPI.isPlatformCreated());
        platformAPI.createAndInitializePlatform();
    }

    @Test(expected = TenantActivationException.class)
    public void activateTenantWhichIsAlreadyActivated() throws BonitaException {
        final long tenantId = createATenant("TENANT_1");
        platformAPI.activateTenant(tenantId);
        try {
            platformAPI.activateTenant(tenantId);
        } finally {
            platformAPI.deactiveTenant(tenantId);
            deleteATenant(tenantId);
        }
    }

    @Test(expected = TenantDeactivationException.class)
    public void activateTenantWhichIsAlreadyDeactivated() throws BonitaException {
        final long tenantId = createATenant("TENANT_1");
        platformAPI.activateTenant(tenantId);
        platformAPI.deactiveTenant(tenantId);
        try {
            platformAPI.deactiveTenant(tenantId);
        } finally {
            deleteATenant(tenantId);
        }
    }

    @Test
    public void getPlatform() throws BonitaException {
        final Platform platform = platformAPI.getPlatform();

        assertNotNull("can't find the platform", platform);
        assertEquals("platformAdmin", platform.getCreatedBy());
    }

    @Test
    public void createTenant() throws BonitaException {
        final long testId = platformAPI.createTenant(new TenantCreator("test", "test create tenant", "testIconName", "testIconPath", "name", "123"));
        final Tenant tenant = platformAPI.getTenantById(testId);
        assertEquals("test", tenant.getName());
        assertEquals("testIconName", tenant.getIconName());
        assertEquals("testIconPath", tenant.getIconPath());

        assertEquals(false, tenant.isDefaultTenant());
        platformAPI.deleteTenant(testId);
    }

    @Test(expected = InvalidSessionException.class)
    public void getAPIWithNullSession() throws BonitaException {
        platformLoginAPI.logout(session);
        try {
            platformAPI = PlatformAPIAccessor.getPlatformAPI(null);
            platformAPI.createTenant(new TenantCreator("test", "test create tenant", "testIconName", "testIconPath", "name", "123"));
            fail("can't get platform api with null session");
        } finally {
            logAsPlatformAdmin();
        }

    }

    @Test(expected = AlreadyExistsException.class)
    public void createExistedTenant() throws BonitaException {
        final long tenantId = platformAPI.createTenant(new TenantCreator("tenantName", "it is a tenant", "testIconName", "testIconPath", "bole", "321"));
        try {
            platformAPI.createTenant(new TenantCreator("tenantName", "", "testIconName", "testIconPath", "", ""));
        } finally {
            platformAPI.deleteTenant(tenantId);
        }
    }

    @Test
    public void getTenantByName() throws BonitaException {
        final long tenantId = createATenant("TENANT_1");

        assertEquals(2, platformAPI.searchTenants(new SearchOptionsBuilder(0, 1000).done()).getCount());
        final Tenant tenant = platformAPI.getTenantByName("TENANT_1");

        assertEquals("TENANT_1", tenant.getName());
        assertEquals("DEACTIVATED", tenant.getState());
        deleteATenant(tenantId);
    }

    @Test
    public void getDefaultTenant() throws BonitaException {
        final Tenant tenant = platformAPI.getDefaultTenant();
        assertNotNull(tenant);
        assertEquals("default", tenant.getName());
        assertEquals("default", tenant.getDescription());
    }

    @Test(expected = TenantNotFoundException.class)
    public void getTenantByNotExistName() throws BonitaException {
        try {
            platformAPI.getTenantByName("test");
        } catch (final TenantNotFoundException e) {
            assertTrue(e.getMessage().startsWith("No tenant exists with name: test"));
            throw e;
        }
    }

    @Test
    public void getTenantById() throws BonitaException {
        final long tenantId = createATenant("TENANT_1");

        assertEquals(1, platformAPI.searchTenants(new SearchOptionsBuilder(0, 1000).done()).getCount());
        final Tenant tenant = platformAPI.getTenantById(tenantId);

        assertEquals(DEFAULT_TENANT_NAME, tenant.getName());
        assertEquals(tenantId, tenant.getId());
        assertEquals("DEACTIVATED", tenant.getState());
        deleteATenant(tenantId);
    }

    @Test(expected = TenantNotFoundException.class)
    public void getTenantByNotExistId() throws BonitaException {
        try {
            platformAPI.getTenantById(-3);
        } catch (final TenantNotFoundException e) {
            assertTrue(e.getMessage().startsWith("No tenant exists with id: -3"));
            throw e;
        }
    }

    @Test
    public void activateTenant() throws BonitaException {
        final long tenantId = createATenant("TENANT_1");
        final Tenant tenantA = platformAPI.getTenantByName(DEFAULT_TENANT_NAME);
        assertEquals("DEACTIVATED", tenantA.getState());

        platformAPI.activateTenant(tenantId);
        final Tenant tenantB = platformAPI.getTenantByName(DEFAULT_TENANT_NAME);
        assertEquals("ACTIVATED", tenantB.getState());

        platformAPI.deactiveTenant(tenantId);
        deleteATenant(tenantId);
    }

    @Test(expected = TenantNotFoundException.class)
    public void activeNotExistTenant() throws BonitaException {
        platformAPI.activateTenant(9999);
    }

    @Test
    public void deactiveTenant() throws BonitaException {
        final long tenantId = createATenant("TENANT_1");
        platformAPI.activateTenant(tenantId);
        assertEquals("ACTIVATED", platformAPI.getTenantByName(DEFAULT_TENANT_NAME).getState());

        platformAPI.deactiveTenant(tenantId);
        assertEquals("DEACTIVATED", platformAPI.getTenantByName(DEFAULT_TENANT_NAME).getState());

        deleteATenant(tenantId);
    }

    @Test(expected = TenantNotFoundException.class)
    public void deactiveNotExistTenant() throws BonitaException {
        platformAPI.deactiveTenant(9999);
    }

    @Test
    public void deleteTenant() throws BonitaException {
        final String tenantNameA = "testA";
        final String tenantNameB = "testB";
        final long tenantA = platformAPI.createTenant(new TenantCreator(tenantNameA, "", "testIconName", "testIconPath", "nameA", "passwordA"));
        final long tenantB = platformAPI.createTenant(new TenantCreator(tenantNameB, "", "testIconName", "testIconPath", "nameB", "passwordB"));

        final List<Tenant> tenantsA = platformAPI.searchTenants(new SearchOptionsBuilder(0, 1000).done()).getResult();
        assertEquals(2, tenantsA.size());
        platformAPI.deleteTenant(tenantA);

        final List<Tenant> tenantsB = platformAPI.searchTenants(new SearchOptionsBuilder(0, 1000).done()).getResult();
        assertEquals(1, tenantsB.size());
        platformAPI.deleteTenant(tenantB);
    }

    @Test(expected = TenantNotFoundException.class)
    public void deleteNotExistTenant() throws BonitaException {
        platformAPI.deleteTenant(9999);
    }

    @Test
    public void getTenantsWithOrderByName() throws BonitaException {
        final String tenantName1 = "test1";
        final String tenantName2 = "test2";
        final String tenantName3 = "test3";
        final String tenantName4 = "test4";
        final String tenantName5 = "test5";
        final String tenantName6 = "test6";
        final String tenantName7 = "test7";
        final String tenantName8 = "test8";
        final String tenantName9 = "test9";
        final long tenant1 = platformAPI.createTenant(new TenantCreator(tenantName1, "", "testIconName", "testIconPath", "testname1", "testpassword1"));
        final long tenant2 = platformAPI.createTenant(new TenantCreator(tenantName2, "", "testIconName", "testIconPath", "testname2", "testpassword2"));
        final long tenant3 = platformAPI.createTenant(new TenantCreator(tenantName3, "", "testIconName", "testIconPath", "testname3", "testpassword3"));
        final long tenant4 = platformAPI.createTenant(new TenantCreator(tenantName4, "", "testIconName", "testIconPath", "testname4", "testpassword4"));
        final long tenant5 = platformAPI.createTenant(new TenantCreator(tenantName5, "", "testIconName", "testIconPath", "testname5", "testpassword5"));
        final long tenant6 = platformAPI.createTenant(new TenantCreator(tenantName6, "", "testIconName", "testIconPath", "testname6", "testpassword6"));
        final long tenant7 = platformAPI.createTenant(new TenantCreator(tenantName7, "", "testIconName", "testIconPath", "testname7", "testpassword7"));
        final long tenant8 = platformAPI.createTenant(new TenantCreator(tenantName8, "", "testIconName", "testIconPath", "testname8", "testpassword8"));
        final long tenant9 = platformAPI.createTenant(new TenantCreator(tenantName9, "", "testIconName", "testIconPath", "testname9", "testpassword9"));

        final List<Tenant> tenants1 = platformAPI.getTenants(0, 3, TenantCriterion.NAME_ASC);
        final List<Tenant> tenants2 = platformAPI.getTenants(1, 3, TenantCriterion.NAME_ASC);
        final List<Tenant> tenants3 = platformAPI.getTenants(2, 3, TenantCriterion.NAME_ASC);
        final int count = platformAPI.getNumberOfTenants();
        assertEquals(9, count);
        assertNotNull(tenants1);
        assertNotNull(tenants2);
        assertNotNull(tenants3);
        assertEquals(3, tenants1.size());
        assertEquals(3, tenants2.size());
        assertEquals(3, tenants3.size());
        assertEquals("test1", tenants1.get(0).getName());
        assertEquals("test2", tenants1.get(1).getName());
        assertEquals("test3", tenants1.get(2).getName());
        assertEquals("test4", tenants2.get(0).getName());
        assertEquals("test5", tenants2.get(1).getName());
        assertEquals("test6", tenants2.get(2).getName());
        assertEquals("test7", tenants3.get(0).getName());
        assertEquals("test8", tenants3.get(1).getName());
        assertEquals("test9", tenants3.get(2).getName());

        platformAPI.deleteTenant(tenant1);
        platformAPI.deleteTenant(tenant2);
        platformAPI.deleteTenant(tenant3);
        platformAPI.deleteTenant(tenant4);
        platformAPI.deleteTenant(tenant5);
        platformAPI.deleteTenant(tenant6);
        platformAPI.deleteTenant(tenant7);
        platformAPI.deleteTenant(tenant8);
        platformAPI.deleteTenant(tenant9);
    }

    @Test
    public void getTenantsWithOrderByDescriptionAndCreationDate() throws BonitaException {
        final String tenantName1 = "test1";
        final String tenantName2 = "test2";
        final String tenantName3 = "test3";
        final long tenant1 = platformAPI.createTenant(new TenantCreator(tenantName1, "a", "testIconName", "testIconPath", "testname1", "testpassword1"));
        final long tenant2 = platformAPI.createTenant(new TenantCreator(tenantName2, "c", "testIconName", "testIconPath", "testname2", "testpassword2"));
        final long tenant3 = platformAPI.createTenant(new TenantCreator(tenantName3, "b", "testIconName", "testIconPath", "testname3", "testpassword3"));
        final List<Tenant> tenantsDescAsc = platformAPI.getTenants(0, 3, TenantCriterion.DESC_ASC);
        final List<Tenant> tenantsDescDesc = platformAPI.getTenants(0, 3, TenantCriterion.DESC_DESC);
        final int count = platformAPI.getNumberOfTenants();
        assertEquals(3, count);
        assertEquals(3, tenantsDescAsc.size());
        assertEquals(3, tenantsDescDesc.size());
        assertEquals("test1", tenantsDescAsc.get(0).getName());
        assertEquals("test3", tenantsDescAsc.get(1).getName());
        assertEquals("test2", tenantsDescAsc.get(2).getName());
        assertEquals("test2", tenantsDescDesc.get(0).getName());
        assertEquals("test3", tenantsDescDesc.get(1).getName());
        assertEquals("test1", tenantsDescDesc.get(2).getName());
        final List<Tenant> tenantsCreAsc = platformAPI.getTenants(0, 3, TenantCriterion.CREATION_ASC);
        final List<Tenant> tenantsCreDesc = platformAPI.getTenants(0, 3, TenantCriterion.CREATION_DESC);
        final List<Tenant> tenantsDefault = platformAPI.getTenants(0, 3, TenantCriterion.DEFAULT);

        assertEquals(3, tenantsCreAsc.size());
        assertEquals(3, tenantsCreDesc.size());
        assertEquals(3, tenantsDefault.size());
        assertEquals("test1", tenantsCreAsc.get(0).getName());
        assertEquals("test2", tenantsCreAsc.get(1).getName());
        assertEquals("test3", tenantsCreAsc.get(2).getName());
        assertEquals("test3", tenantsCreDesc.get(0).getName());
        assertEquals("test2", tenantsCreDesc.get(1).getName());
        assertEquals("test1", tenantsCreDesc.get(2).getName());
        assertEquals("test3", tenantsDefault.get(0).getName());
        assertEquals("test2", tenantsDefault.get(1).getName());
        assertEquals("test1", tenantsDefault.get(2).getName());
        platformAPI.deleteTenant(tenant1);
        platformAPI.deleteTenant(tenant2);
        platformAPI.deleteTenant(tenant3);
    }

    @Test
    public void getTenantsWithOrderByStatus() throws BonitaException {
        final String tenantName1 = "test1";
        final String tenantName2 = "test2";
        final long tenant1 = platformAPI.createTenant(new TenantCreator(tenantName1, "a", "testIconName", "testIconPath", "testname1", "testpassword1"));
        final long tenant2 = platformAPI.createTenant(new TenantCreator(tenantName2, "c", "testIconName", "testIconPath", "testname2", "testpassword2"));
        platformAPI.activateTenant(tenant2);
        final List<Tenant> tenantsDescAsc = platformAPI.getTenants(0, 2, TenantCriterion.STATE_ASC);
        final List<Tenant> tenantsDescDesc = platformAPI.getTenants(0, 2, TenantCriterion.STATE_DESC);
        final int count = platformAPI.getNumberOfTenants();
        assertEquals(2, count);
        assertEquals(2, tenantsDescAsc.size());
        assertEquals(2, tenantsDescDesc.size());
        assertEquals("test2", tenantsDescAsc.get(0).getName());
        assertEquals("test1", tenantsDescAsc.get(1).getName());
        assertEquals("test1", tenantsDescDesc.get(0).getName());
        assertEquals("test2", tenantsDescDesc.get(1).getName());
        platformAPI.deleteTenant(tenant1);
        platformAPI.deactiveTenant(tenant2);
        platformAPI.deleteTenant(tenant2);
    }

    @Test
    public void getTenantsWithPages() throws BonitaException {
        final String tenantName1 = "test1";
        final String tenantName2 = "test2";
        final String tenantName3 = "test3";
        final long createTenant1 = platformAPI.createTenant(new TenantCreator(tenantName1, "", "testIconName", "testIconPath", "testname1", "testpassword1"));
        final long createTenant2 = platformAPI.createTenant(new TenantCreator(tenantName2, "", "testIconName", "testIconPath", "testname2", "testpassword2"));
        final long createTenant3 = platformAPI.createTenant(new TenantCreator(tenantName3, "", "testIconName", "testIconPath", "testname3", "testpassword3"));
        try {
            final List<Tenant> testTenants = platformAPI.getTenants(0, 3, TenantCriterion.NAME_ASC);
            assertNotNull(testTenants);
            assertEquals(3, testTenants.size());
            assertEquals(testTenants.get(0).getName(), "test1");
            assertEquals(testTenants.get(1).getName(), "test2");
            assertEquals(testTenants.get(2).getName(), "test3");

            final List<Tenant> tenants1 = platformAPI.getTenants(1, 2, TenantCriterion.NAME_ASC);
            assertNotNull(tenants1);
            assertEquals(1, tenants1.size());
            assertEquals(tenants1.get(0).getName(), "test3");

            final List<Tenant> tenants2 = platformAPI.getTenants(1, 2, TenantCriterion.NAME_DESC);
            assertNotNull(tenants2);
            assertEquals(1, tenants2.size());
            assertEquals(tenants2.get(0).getName(), "test1");
        } finally {
            platformAPI.deleteTenant(createTenant1);
            platformAPI.deleteTenant(createTenant2);
            platformAPI.deleteTenant(createTenant3);
        }
    }

    @Test
    public void getTenantsWithIndexPageOutOfRange() throws BonitaException {
        final List<Tenant> tenants = platformAPI.getTenants(50, 100, TenantCriterion.NAME_ASC);
        assertTrue(tenants.isEmpty());
    }

    @Test
    public void getTenantsWithTotalPageOutOfRange() throws BonitaException {
        final long tenant1Id = platformAPI.createTenant(new TenantCreator("test1", "test", "testIconName", "testIconPath", "tenant_test1",
                "tenant_test_password"));
        final long tenant2Id = platformAPI.createTenant(new TenantCreator("test2", "test", "testIconName", "testIconPath", "tenant_test2",
                "tenant_test_password"));
        final long tenant3Id = platformAPI.createTenant(new TenantCreator("test3", "test", "testIconName", "testIconPath", "tenant_test3",
                "tenant_test_password"));
        final long tenant4Id = platformAPI.createTenant(new TenantCreator("test4", "test", "testIconName", "testIconPath", "tenant_test4",
                "tenant_test_password"));
        final long tenant5Id = platformAPI.createTenant(new TenantCreator("test5", "test", "testIconName", "testIconPath", "tenant_test5",
                "tenant_test_password"));
        assertNotNull(platformAPI.searchTenants(new SearchOptionsBuilder(0, 1000).done()));
        assertEquals(5, platformAPI.searchTenants(new SearchOptionsBuilder(0, 1000).done()).getCount());
        try {
            final List<Tenant> tenants = platformAPI.getTenants(20, 2, TenantCriterion.NAME_ASC);
            assertTrue(tenants.isEmpty());
        } finally {
            platformAPI.deleteTenant(tenant1Id);
            platformAPI.deleteTenant(tenant2Id);
            platformAPI.deleteTenant(tenant3Id);
            platformAPI.deleteTenant(tenant4Id);
            platformAPI.deleteTenant(tenant5Id);
        }
    }

    @Test
    public void getAllTenants() throws BonitaException {
        assertEquals(0, platformAPI.searchTenants(new SearchOptionsBuilder(0, 1000).done()).getCount());
        final long tenantId = createATenant("TENANT_1");
        assertEquals(1, platformAPI.searchTenants(new SearchOptionsBuilder(0, 1000).done()).getCount());
        deleteATenant(tenantId);
    }

    @Test(expected = PlatformNotFoundException.class)
    public void deletePlatform() throws BonitaException {
        try {
            platformAPI.deletePlaftorm();
            platformAPI.getPlatform();
        } finally {
            platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
            logAsPlatformAdmin();
            platformAPI.createAndInitializePlatform();
            platformAPI.startNode();
        }
    }

    @Test
    public void methodsThatNeedPlatformToBeStarted() throws BonitaException {
        try {
            platformAPI.stopNode();
            try {
                platformAPI.searchTenants(new SearchOptionsBuilder(0, 1000).done());
                fail();
            } catch (final SearchException e) {
            }
            try {
                platformAPI.getTenantById(1l);
                fail();
            } catch (final PlatformNotStartedException e) {
            }
            try {
                platformAPI.getDefaultTenant();
                fail();
            } catch (final PlatformNotStartedException e) {
            }
        } finally {
            platformAPI.startNode();
        }
    }

    @Test(expected = DeletionException.class)
    public void deletePlatformWithTenantExist() throws BonitaException {
        final long tenantId = createATenant("TENANT_1");
        try {
            platformAPI.activateTenant(tenantId);
            platformAPI.deletePlaftorm();
        } finally {
            platformAPI.deactiveTenant(tenantId);
            deleteATenant(tenantId);
        }
    }

    @Test
    public void getNumberOfTenants() throws BonitaException {
        assertEquals(0, platformAPI.getNumberOfTenants());
        final long createTenant = platformAPI.createTenant(new TenantCreator("test", "testDescription eeeeeeeeeeeeeeeeeeeeeee", "testIconName", "testIconPath",
                "testname", "testpass"));
        final int numberOfTenants = platformAPI.getNumberOfTenants();
        platformAPI.deleteTenant(createTenant);
        assertEquals(1, numberOfTenants);
    }

    @Test(expected = PlatformNotStartedException.class)
    public void platformStoppedCreateTenant() throws BonitaException {
        try {
            platformAPI.stopNode();
            createATenant("TENANT_1");
        } finally {
            platformAPI.startNode();
        }
    }

    @Test(expected = PlatformNotStartedException.class)
    public void platformStoppedGetTenant() throws BonitaException {
        final long tenantId = createATenant("TENANT_1");
        try {
            platformAPI.stopNode();
            platformAPI.getTenantByName(DEFAULT_TENANT_NAME);
        } finally {
            platformAPI.startNode();
            platformAPI.deleteTenant(tenantId);
        }
    }

    @Test(expected = PlatformNotStartedException.class)
    public void platformStoppedDeleteTenant() throws BonitaException {
        final long tenantId = createATenant("TENANT_1");
        try {
            platformAPI.stopNode();
            platformAPI.deleteTenant(tenantId);
        } finally {
            platformAPI.startNode();
            platformAPI.deleteTenant(tenantId);
        }
    }

    @Test(expected = PlatformNotStartedException.class)
    public void platformStoppedActivatedTenant() throws BonitaException {
        final long tenantId = createATenant("TENANT_1");
        try {
            platformAPI.stopNode();
            platformAPI.activateTenant(tenantId);
        } finally {
            platformAPI.startNode();
            platformAPI.deleteTenant(tenantId);
        }
    }

    @Test(expected = PlatformNotStartedException.class)
    public void platformStoppedDeactivatedTenant() throws BonitaException {
        final long tenantId = createATenant("TENANT_1");
        try {
            platformAPI.stopNode();
            platformAPI.deactiveTenant(tenantId);
        } finally {
            platformAPI.startNode();
            platformAPI.deleteTenant(tenantId);
        }
    }

    @Test
    public void getPlatformState() throws Exception {
        // test started state
        PlatformState state = platformAPI.getPlatformState();
        assertEquals(PlatformState.STARTED, state);
        // test stopped state
        platformAPI.stopNode();
        state = platformAPI.getPlatformState();
        assertEquals(PlatformState.STOPPED, state);
        // test exception:PlatformNotFoundException
        platformAPI.deletePlaftorm();
        try {
            state = platformAPI.getPlatformState();
            fail();
        } catch (final PlatformNotFoundException e) {
            platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
            logAsPlatformAdmin();
            platformAPI.createAndInitializePlatform();
            platformAPI.startNode();
        }
    }

    @Test
    public void updateTenant() throws Exception {
        // create tenant
        final long tenantId = platformAPI
                .createTenant(new TenantCreator("tenantName", "test update tenant", "testIconName", "testIconPath", "username", "123"));
        final Tenant tenant = platformAPI.getTenantById(tenantId);
        assertEquals("tenantName", tenant.getName());
        assertEquals("testIconName", tenant.getIconName());
        assertEquals("testIconPath", tenant.getIconPath());
        // TODO try to log in on tenant using username and password as there is no way to get username/password information from API.
        // We do not want to add in API methods to read password (for security reasons)
        // session = BPMTestUtil.loginDefaultTenant("username","123");
        // update tenant
        final TenantUpdater udpateDescriptor = new TenantUpdater();
        udpateDescriptor.setName("updatedTenantName");
        udpateDescriptor.setIconName("updatedIconName");
        udpateDescriptor.setUsername("updatedUsername");
        udpateDescriptor.setPassword("updatedPassword");
        final Tenant updatedTenant = platformAPI.updateTenant(tenantId, udpateDescriptor);
        assertEquals(tenantId, updatedTenant.getId());
        assertEquals("updatedTenantName", updatedTenant.getName());
        assertEquals("updatedIconName", updatedTenant.getIconName());
        // TODO check updated username and password by calling login API
        // XXX with old password and old username ==> should not be allowed to login
        // YYY with new password and new username ==> should be allowed to login
        // clear-up
        platformAPI.deleteTenant(tenantId);
    }

    @Test(expected = UpdateException.class)
    public void updateTenantWithTenantUpdateException() throws Exception {
        // create tenant
        final long tenantId = platformAPI
                .createTenant(new TenantCreator("tenantName", "test update tenant", "testIconName", "testIconPath", "username", "123"));
        platformAPI.getTenantById(tenantId);
        // update tenant
        final TenantUpdater udpateDescriptor = new TenantUpdater();
        udpateDescriptor.setName("updatedTenantName");
        udpateDescriptor.setIconName("updatedIconName");
        try {
            platformAPI.updateTenant(tenantId + 100, udpateDescriptor);
        } finally {
            // clear-up
            platformAPI.deleteTenant(tenantId);
        }
    }

    @Test(expected = PlatformNotStartedException.class)
    public void updateTenantWithPlatformNotStartedException() throws Exception {
        // create tenant
        final long tenantId = platformAPI.createTenant(new TenantCreator("tenantName", "test update tenant with PlatformNotStartedException", "testIconName",
                "testIconPath", "username", "123"));
        platformAPI.getTenantById(tenantId);
        // stop platform
        platformAPI.stopNode();
        // update tenant
        final TenantUpdater udpateDescriptor = new TenantUpdater();
        udpateDescriptor.setName("updatedTenantName");
        udpateDescriptor.setIconName("updatedIconName");
        try {
            platformAPI.updateTenant(tenantId, udpateDescriptor);
        } finally {
            // clear-up
            platformAPI.startNode();
            platformAPI.deleteTenant(tenantId);
        }
    }

}
