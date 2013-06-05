/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.platform;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.home.BonitaHome;
import org.bonitasoft.engine.io.PropertiesManager;
import org.bonitasoft.engine.platform.Platform;
import org.bonitasoft.engine.platform.PlatformLoginException;
import org.bonitasoft.engine.platform.PlatformNotFoundException;
import org.bonitasoft.engine.platform.PlatformState;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.session.SessionNotFoundException;
import org.bonitasoft.engine.session.impl.PlatformSessionImpl;
import org.junit.AfterClass;
import org.junit.Before;
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

@SuppressWarnings("javadoc")
public class SPPlatformTest {

    private static final String DEFAULT_TENANT_NAME = "default";

    private static final String ACTIVATED = "ACTIVATED";

    private static final String DEACTIVATED = "DEACTIVATED";

    private static final Logger LOGGER = LoggerFactory.getLogger(SPPlatformTest.class);

    private static PlatformAPI platformAPI;

    private static PlatformLoginAPI platformLoginAPI;

    private static PlatformSession session;

    private static final String tenantName1 = "test1";

    private static final String tenantName2 = "test2";

    private static long tenantId1;

    private static long tenantId2;

    @BeforeClass
    public static void beforeClass() throws Exception {
        platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
        logAsPlatformAdmin();
        try {
            platformAPI.initializePlatform();
            assertEquals(PlatformState.STARTED, platformAPI.getPlatformState());
            createTenants(); // create tenants in before class because this actions takes a lot of time
        } catch (final CreationException e) {
            // Platform already created
        }
    }

    private static void createTenants() throws Exception {
        tenantId1 = platformAPI.createTenant(new TenantCreator(tenantName1, "The tenant 1", "testIconName1", "testIconPath1", "username1", "testpassword1"));
        Thread.sleep(10);// avoid conflict in creation date
        tenantId2 = platformAPI.createTenant(new TenantCreator(tenantName2, "The tenant 2", "testIconName2", "testIconPath2", "username2", "testpassword2"));
        // tenantId3 = platformAPI.createTenant(new TenantCreator(tenantName3, "", "testIconName", "testIconPath", "testname3", "testpassword3"));
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

    @Before
    public void setUp() throws Exception {
        if (!PlatformState.STARTED.equals(platformAPI.getPlatformState())) {
            platformAPI.initializePlatform();
            platformAPI.startNode();
            createTenants();
        }
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
    public void wrongLogin() throws Exception {
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

    @Test(expected = SessionNotFoundException.class)
    public void logoutWithWrongSession() throws Exception {
        try {
            platformLoginAPI.logout(new PlatformSessionImpl(123l, null, -1l, null, -1l));
        } finally {
            platformLoginAPI.logout(session);
            logAsPlatformAdmin();
        }
    }

    @Test
    public void searchTenants() throws Exception {
        // create tenant
        // final List<Tenant> listTenant = new ArrayList<Tenant>();
        // for (int i = 0; i < 10; i++) {
        // final long tenantID = platformAPI.createTenant(new TenantCreator("tenantName" + i, "test search tenant ", "testIconName" + i, "testIconPath" + i,
        // "username" + i, "123"));
        // final Tenant tenant = platformAPI.getTenantById(tenantID);
        // listTenant.add(tenant);
        // }

        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        // according to name sort
        builder.sort(TenantSearchDescriptor.NAME, Order.ASC);
        builder.filter(TenantSearchDescriptor.DEFAULT_TENANT, false);
        SearchResult<Tenant> searchResult = platformAPI.searchTenants(builder.done());
        assertEquals(2, searchResult.getCount()); // two tenants created at before class
        List<Tenant> tenantList = searchResult.getResult();
        assertEquals(2, tenantList.size());
        checkOrder(tenantList, tenantName1, tenantName2);

        builder = new SearchOptionsBuilder(0, 10);
        builder.filter(TenantSearchDescriptor.DEFAULT_TENANT, false);
        builder.sort("name", Order.DESC);
        searchResult = platformAPI.searchTenants(builder.done());
        assertEquals(2, searchResult.getCount());
        tenantList = searchResult.getResult();
        assertNotNull(tenantList);
        assertEquals(2, tenantList.size());
        checkOrder(tenantList, tenantName2, tenantName1);

        builder = new SearchOptionsBuilder(0, 10);
        builder.filter(TenantSearchDescriptor.DEFAULT_TENANT, false);
        builder.sort(TenantSearchDescriptor.CREATION_DATE, Order.ASC);
        searchResult = platformAPI.searchTenants(builder.done());
        assertEquals(2, searchResult.getCount());
        tenantList = searchResult.getResult();
        assertNotNull(tenantList);
        assertEquals(2, tenantList.size());
        checkOrder(tenantList, tenantName1, tenantName2);

        builder = new SearchOptionsBuilder(0, 10);
        builder.filter(TenantSearchDescriptor.DEFAULT_TENANT, false);
        builder.sort(TenantSearchDescriptor.CREATION_DATE, Order.DESC);
        searchResult = platformAPI.searchTenants(builder.done());
        assertEquals(2, searchResult.getCount());
        tenantList = searchResult.getResult();
        assertNotNull(tenantList);
        assertEquals(2, tenantList.size());
        checkOrder(tenantList, tenantName2, tenantName1);

        // according to name search
        builder = new SearchOptionsBuilder(0, 5);
        builder.searchTerm(tenantName2);
        searchResult = platformAPI.searchTenants(builder.done());
        assertEquals(1, searchResult.getCount());
        tenantList = searchResult.getResult();
        assertNotNull(tenantList);
        assertEquals(1, tenantList.size());
        assertEquals(tenantId2, tenantList.get(0).getId());

        // according to iconName search
        builder = new SearchOptionsBuilder(0, 5);
        builder.searchTerm("testIconName1");
        searchResult = platformAPI.searchTenants(builder.done());
        assertEquals(1, searchResult.getCount());
        tenantList = searchResult.getResult();
        assertNotNull(tenantList);
        assertEquals(1, tenantList.size());
        assertEquals(tenantId1, tenantList.get(0).getId());
    }

    private void checkOrder(final List<Tenant> tenantList, final String... tenantNames) {
        for (int i = 0; i < tenantNames.length; i++) {
            assertEquals(tenantNames[i], tenantList.get(i).getName());

        }
    }

    @Test
    public void isPlatformCreated() throws BonitaException {
        assertTrue(platformAPI.isPlatformCreated());
    }

    @Test(expected = CreationException.class)
    public void createPlatformException() throws BonitaException {
        assertTrue(platformAPI.isPlatformCreated());
        platformAPI.createPlatform();
    }

    @Test(expected = TenantActivationException.class)
    public void activateTenantWhichIsAlreadyActivated() throws BonitaException {
        platformAPI.activateTenant(tenantId1);
        try {
            platformAPI.activateTenant(tenantId1);
        } finally {
            platformAPI.deactiveTenant(tenantId1);
        }
    }

    @Test(expected = TenantDeactivationException.class)
    public void deactivateTenantWhichIsAlreadyDeactivated() throws BonitaException {
        platformAPI.deactiveTenant(tenantId1);
    }

    @Test
    public void getPlatform() throws BonitaException {
        final Platform platform = platformAPI.getPlatform();

        assertNotNull("can't find the platform", platform);
        assertEquals("platformAdmin", platform.getCreatedBy());
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
        platformAPI.createTenant(new TenantCreator(tenantName1, "", "testIconName", "testIconPath", "", ""));
    }

    @Test
    public void getTenantByName() throws BonitaException {
        final Tenant tenant = platformAPI.getTenantByName(tenantName1);
        assertEquals(tenantId1, tenant.getId());
        assertEquals(DEACTIVATED, tenant.getState());
    }

    @Test
    public void getDefaultTenant() throws BonitaException {
        final Tenant tenant = platformAPI.getDefaultTenant();
        assertNotNull(tenant);
        assertEquals(DEFAULT_TENANT_NAME, tenant.getName());
        assertEquals("Default tenant", tenant.getDescription());
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
        final Tenant tenant = platformAPI.getTenantById(tenantId1);
        assertEquals(tenantName1, tenant.getName());
        assertEquals(tenantId1, tenant.getId());
        assertEquals(DEACTIVATED, tenant.getState());
    }

    private SearchOptionsBuilder getOnlyNonDefaultTenantsSearchBuilder() {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 1000);
        builder.filter(TenantSearchDescriptor.DEFAULT_TENANT, false);
        return builder;
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
    public void activateDeactivateTenant() throws BonitaException {
        Tenant tenant = platformAPI.getTenantById(tenantId1);
        assertEquals(DEACTIVATED, tenant.getState());

        platformAPI.activateTenant(tenantId1);
        try {
            tenant = platformAPI.getTenantById(tenantId1);
            assertEquals(ACTIVATED, tenant.getState());
        } finally {
            platformAPI.deactiveTenant(tenantId1);
            tenant = platformAPI.getTenantById(tenantId1);
            assertEquals(DEACTIVATED, tenant.getState());
        }
    }

    @Test(expected = TenantNotFoundException.class)
    public void activeNotExistTenant() throws BonitaException {
        platformAPI.activateTenant(9999);
    }

    @Test(expected = TenantNotFoundException.class)
    public void deactiveNotExistTenant() throws BonitaException {
        platformAPI.deactiveTenant(9999);
    }

    @Test
    public void createAndDeleteTenant() throws BonitaException {
        final String tenantNameA = "testA";
        final long tenantAId = platformAPI.createTenant(new TenantCreator(tenantNameA, "", "testIconName", "testIconPath", "nameA", "passwordA"));

        final SearchOptionsBuilder builder = getOnlyNonDefaultTenantsSearchBuilder();
        builder.filter(TenantSearchDescriptor.NAME, tenantNameA);
        List<Tenant> tenants = platformAPI.searchTenants(builder.done()).getResult();
        assertEquals(1, tenants.size());
        assertEquals(tenantNameA, tenants.get(0).getName());
        assertEquals("testIconName", tenants.get(0).getIconName());
        assertEquals("testIconPath", tenants.get(0).getIconPath());

        platformAPI.deleteTenant(tenantAId);

        tenants = platformAPI.searchTenants(builder.done()).getResult();
        assertEquals(0, tenants.size());
    }

    @Test(expected = DeletionException.class)
    public void deleteNotExistTenant() throws BonitaException {
        platformAPI.deleteTenant(-3);
    }

    @Test
    public void getTenantsWithOrderByName() throws BonitaException {
        final List<Tenant> tenants1 = platformAPI.getTenants(0, 2, TenantCriterion.NAME_ASC);
        final List<Tenant> tenants2 = platformAPI.getTenants(2, 2, TenantCriterion.NAME_ASC);
        final List<Tenant> tenants3 = platformAPI.getTenants(3, 2, TenantCriterion.NAME_ASC);
        final int count = platformAPI.getNumberOfTenants();
        assertEquals(3, count);
        assertNotNull(tenants1);
        assertNotNull(tenants2);
        assertNotNull(tenants3);
        assertEquals(2, tenants1.size());
        assertEquals(1, tenants2.size());
        assertEquals(0, tenants3.size());
        assertEquals(DEFAULT_TENANT_NAME, tenants1.get(0).getName());
        assertEquals(tenantName1, tenants1.get(1).getName());

        assertEquals(tenantName2, tenants2.get(0).getName());
    }

    @Test
    public void getTenantsWithOrderByDescriptionAndCreationDate() throws BonitaException {
        final List<Tenant> tenantsDescAsc = platformAPI.getTenants(0, 3, TenantCriterion.DESCRIPTION_ASC);
        final List<Tenant> tenantsDescDesc = platformAPI.getTenants(0, 3, TenantCriterion.DESCRIPTION_DESC);
        final int count = platformAPI.getNumberOfTenants();
        assertEquals(3, count);
        assertEquals(3, tenantsDescAsc.size());
        assertEquals(3, tenantsDescDesc.size());
        assertEquals(DEFAULT_TENANT_NAME, tenantsDescAsc.get(0).getName());
        assertEquals(tenantName1, tenantsDescAsc.get(1).getName());
        assertEquals(tenantName2, tenantsDescAsc.get(2).getName());

        assertEquals(tenantName2, tenantsDescDesc.get(0).getName());
        assertEquals(tenantName1, tenantsDescDesc.get(1).getName());
        assertEquals(DEFAULT_TENANT_NAME, tenantsDescDesc.get(2).getName());
        final List<Tenant> tenantsCreAsc = platformAPI.getTenants(0, 3, TenantCriterion.CREATION_ASC);
        final List<Tenant> tenantsCreDesc = platformAPI.getTenants(0, 3, TenantCriterion.CREATION_DESC);
        final List<Tenant> tenantsDefault = platformAPI.getTenants(0, 3, TenantCriterion.DEFAULT);

        assertEquals(3, tenantsCreAsc.size());
        assertEquals(3, tenantsCreDesc.size());
        assertEquals(3, tenantsDefault.size());
        assertEquals(DEFAULT_TENANT_NAME, tenantsCreAsc.get(0).getName());
        assertEquals(tenantName1, tenantsCreAsc.get(1).getName());
        assertEquals(tenantName2, tenantsCreAsc.get(2).getName());
        assertEquals(tenantName2, tenantsCreDesc.get(0).getName());
        assertEquals(tenantName1, tenantsCreDesc.get(1).getName());
        assertEquals(DEFAULT_TENANT_NAME, tenantsCreDesc.get(2).getName());
        assertEquals(tenantName2, tenantsDefault.get(0).getName());
        assertEquals(tenantName1, tenantsDefault.get(1).getName());
        assertEquals(DEFAULT_TENANT_NAME, tenantsDefault.get(2).getName());
    }

    @Test
    public void getTenantsWithOrderByStatus() throws BonitaException {
        platformAPI.activateTenant(tenantId1);
        try {
            final List<Tenant> tenantsAsc = platformAPI.getTenants(0, 10, TenantCriterion.STATE_ASC);
            final List<Tenant> tenantsDesc = platformAPI.getTenants(0, 10, TenantCriterion.STATE_DESC);
            final int count = platformAPI.getNumberOfTenants();
            assertEquals(3, count);
            assertEquals(3, tenantsAsc.size());
            assertEquals(3, tenantsDesc.size());
            assertEquals(DEFAULT_TENANT_NAME, tenantsAsc.get(0).getName());
            assertEquals(tenantName1, tenantsAsc.get(1).getName());
            assertEquals(tenantName2, tenantsAsc.get(2).getName());
            assertEquals(tenantName2, tenantsDesc.get(0).getName());
            assertEquals(DEFAULT_TENANT_NAME, tenantsDesc.get(1).getName());
            assertEquals(tenantName1, tenantsDesc.get(2).getName());
        } finally {
            platformAPI.deactiveTenant(tenantId1);
        }
    }

    @Test
    public void getTenantsWithPages() throws BonitaException {
        final List<Tenant> testTenants = platformAPI.getTenants(0, 2, TenantCriterion.NAME_ASC);
        assertNotNull(testTenants);
        assertEquals(2, testTenants.size());
        assertEquals(DEFAULT_TENANT_NAME, testTenants.get(0).getName());
        assertEquals(tenantName1, testTenants.get(1).getName());

        final List<Tenant> tenants1 = platformAPI.getTenants(2, 10, TenantCriterion.NAME_ASC);
        assertNotNull(tenants1);
        assertEquals(1, tenants1.size());
        assertEquals(tenantName2, tenants1.get(0).getName());

        final List<Tenant> tenants2 = platformAPI.getTenants(0, 2, TenantCriterion.NAME_DESC);
        assertNotNull(tenants2);
        assertEquals(2, tenants2.size());
        assertEquals(tenantName2, tenants2.get(0).getName());
        assertEquals(tenantName1, tenants2.get(1).getName());
    }

    @Test
    public void getTenantsWithIndexPageOutOfRange() throws BonitaException {
        final List<Tenant> tenants = platformAPI.getTenants(50, 100, TenantCriterion.NAME_ASC);
        assertTrue(tenants.isEmpty());
    }

    @Test(expected = PlatformNotFoundException.class)
    public void deletePlatform() throws BonitaException {
        try {
            platformAPI.stopNode();
            platformAPI.cleanPlatform();
            platformAPI.deletePlatform();
            platformAPI.getPlatform();
        } finally {
            platformAPI.createPlatform();
        }
    }

    @Test
    public void actionsWithPlatformStopped() throws BonitaException {
        platformAPI.stopNode();
        try {
            final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 1000);
            builder.filter(TenantSearchDescriptor.DEFAULT_TENANT, true);
            final SearchResult<Tenant> searchTenants = platformAPI.searchTenants(builder.done());
            assertEquals(1, searchTenants.getCount());
            final long defaultTenantId = searchTenants.getResult().get(0).getId();
            final Tenant tenant = platformAPI.getTenantById(defaultTenantId);
            assertEquals(defaultTenantId, tenant.getId());
            final Tenant defaultTenant = platformAPI.getDefaultTenant();
            assertEquals(defaultTenant.getId(), defaultTenantId);
        } finally {
            platformAPI.startNode();
        }
    }

    @Test(expected = DeletionException.class)
    public void deletePlatformWithExistingTenants() throws BonitaException {
        try {
            platformAPI.activateTenant(tenantId1);
            platformAPI.deletePlatform();
        } finally {
            platformAPI.deactiveTenant(tenantId1);
        }
    }

    @Test
    public void getNumberOfTenants() throws BonitaException {
        final int numberOfTenants = platformAPI.getNumberOfTenants();
        assertEquals(3, numberOfTenants);
    }

    @Test
    public void canCreateTeantWithNodeStopped() throws BonitaException {
        platformAPI.stopNode();
        long tenantId = -1;
        try {
            tenantId = createATenant("TENANT_1");
        } finally {
            if (tenantId != -1) {
                deleteATenant(tenantId);
            }
            platformAPI.startNode();
        }
    }

    @Test
    public void canGetTenantByNameWithNodeStopped() throws BonitaException {
        platformAPI.stopNode();
        try {
            final Tenant tenant = platformAPI.getTenantByName(tenantName1);
            assertEquals(tenantName1, tenant.getName());
        } finally {
            platformAPI.startNode();
        }
    }

    @Test
    public void canDeleteTenantWithStoppedNode() throws BonitaException {
        final long tenantId = createATenant("TENANT_1");
        try {
            platformAPI.stopNode();
            platformAPI.deleteTenant(tenantId);
        } finally {
            platformAPI.startNode();
        }
    }

    @Test
    public void canActivatedDeactivateTenantWithNodeStopped() throws BonitaException {
        platformAPI.stopNode();
        try {
            platformAPI.activateTenant(tenantId1);
            platformAPI.deactiveTenant(tenantId1);
        } finally {
            platformAPI.startNode();
        }
    }

    @Test
    public void getPlatformState() throws Exception {
        // test started state
        PlatformState state = platformAPI.getPlatformState();
        assertEquals(PlatformState.STARTED, state);
        // test stopped state
        try {
            platformAPI.stopNode();
            state = platformAPI.getPlatformState();
            assertEquals(PlatformState.STOPPED, state);
            // test exception:PlatformNotFoundException
            platformAPI.cleanPlatform();
            platformAPI.deletePlatform();
            state = platformAPI.getPlatformState();
            fail();
        } catch (final PlatformNotFoundException e) {
            platformAPI.createPlatform();
        }
    }

    @Test
    public void updateTenant() throws Exception {
        // TODO try to log in on tenant using username and password as there is no way to get username/password information from API.
        // We do not want to add in API methods to read password (for security reasons)
        // session = BPMTestUtil.loginDefaultTenant("username","123");
        // update tenant
        final TenantUpdater udpateDescriptor = new TenantUpdater();
        udpateDescriptor.setName("updatedTenantName");
        udpateDescriptor.setIconName("updatedIconName");
        udpateDescriptor.setUsername("updatedUsername");
        udpateDescriptor.setPassword("updatedPassword");
        try {
            final Tenant updatedTenant = platformAPI.updateTenant(tenantId1, udpateDescriptor);
            assertEquals(tenantId1, updatedTenant.getId());
            assertEquals("updatedTenantName", updatedTenant.getName());
            assertEquals("updatedIconName", updatedTenant.getIconName());
            // TODO check updated username and password by calling login API
            // XXX with old password and old username ==> should not be allowed to login
            // YYY with new password and new username ==> should be allowed to login
            // clear-up
        } finally {
            // reset default values
            udpateDescriptor.setName(tenantName1);
            udpateDescriptor.setIconName("IconName");
            udpateDescriptor.setUsername("username1");
            udpateDescriptor.setPassword("testpassword1");
            platformAPI.updateTenant(tenantId1, udpateDescriptor);
        }
    }

    @Test
    public void updatePasswordTenantWithSpecialCharacters() throws Exception {
        // update tenant
        final TenantUpdater udpateDescriptor = new TenantUpdater();
        udpateDescriptor.setPassword("@\\[||sfgf23465");
        try {
            final Tenant updatedTenant = platformAPI.updateTenant(tenantId1, udpateDescriptor);
            assertEquals(tenantId1, updatedTenant.getId());

            final StringBuilder path = new StringBuilder(new BonitaHome() {

                @Override
                protected void refresh() {
                }
            }.getBonitaHomeFolder());
            path.append(File.separatorChar);
            path.append("server");
            path.append(File.separatorChar);
            path.append("tenants");
            path.append(File.separatorChar);
            path.append(tenantId1);
            path.append(File.separatorChar);
            path.append("conf");
            final String tenantPath = path.toString() + File.separator + "bonita-server.properties";
            final File file = new File(tenantPath);
            final Properties properties = PropertiesManager.getProperties(file);
            assertEquals("@\\[||sfgf23465", properties.getProperty("userPassword"));
        } finally {
            // clear-up
            // reset default values
            udpateDescriptor.setName(tenantName1);
            udpateDescriptor.setIconName("IconName");
            udpateDescriptor.setUsername("username1");
            udpateDescriptor.setPassword("testpassword1");
            platformAPI.updateTenant(tenantId1, udpateDescriptor);
        }
    }

    @Test(expected = UpdateException.class)
    public void updateTenantWithTenantUpdateException() throws Exception {
        // update tenant
        final TenantUpdater udpateDescriptor = new TenantUpdater();
        udpateDescriptor.setName("updatedTenantName");
        udpateDescriptor.setIconName("updatedIconName");
        platformAPI.updateTenant(-10, udpateDescriptor);
    }

    @Test
    public void canUpdateTenantWithNodeNotStarted() throws Exception {
        // stop platform
        platformAPI.stopNode();
        // update tenant
        final TenantUpdater udpateDescriptor = new TenantUpdater();
        udpateDescriptor.setName("updatedTenantName");
        try {
            platformAPI.updateTenant(tenantId1, udpateDescriptor);
        } finally {
            udpateDescriptor.setName(tenantName1);
            platformAPI.updateTenant(tenantId1, udpateDescriptor);
            platformAPI.startNode();
        }
    }

}
