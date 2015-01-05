/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.platform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.home.BonitaHome;
import org.bonitasoft.engine.io.PropertiesManager;
import org.bonitasoft.engine.platform.NodeNotStartedException;
import org.bonitasoft.engine.platform.Platform;
import org.bonitasoft.engine.platform.PlatformNotFoundException;
import org.bonitasoft.engine.platform.PlatformState;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.bonitasoft.engine.APITestSPUtil;
import com.bonitasoft.engine.BPMTestSPUtil;
import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.PlatformAPIAccessor;

@SuppressWarnings("javadoc")
public class SPPlatformTest {

    private static final String DEFAULT_TENANT_NAME = "default";

    private static final String ACTIVATED = "ACTIVATED";

    private static final String DEACTIVATED = "DEACTIVATED";

    private static final String tenantName1 = "test1";

    private static final String tenantName2 = "test2";

    private static final String tenantName3 = "test3";

    private static long tenantId1;

    private static long tenantId2;

    private static long tenantId3;

    private static PlatformAPI platformAPI;

    private static PlatformSession session;

    private static APITestSPUtil apiTestSpUtil = new APITestSPUtil();

    @BeforeClass
    public static void beforeClass() throws Exception {
        session = apiTestSpUtil.loginOnPlatform();
        platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        createTenants(); // create tenants in before class because this actions takes a lot of time
    }

    @AfterClass
    public static void afterClass() throws BonitaException {
        platformAPI.deleteTenant(tenantId1);
        platformAPI.deleteTenant(tenantId2);
        platformAPI.deleteTenant(tenantId3);
        apiTestSpUtil.logoutOnPlatform(session);
    }

    private static void createTenants() throws Exception {
        tenantId1 = platformAPI.createTenant(new TenantCreator(tenantName1, "Tenant", "testIconName1", "testIconPath1", "username1", "testpassword1"));
        Thread.sleep(10);// avoid conflict in creation date
        tenantId2 = platformAPI.createTenant(new TenantCreator(tenantName2, "Tenant", "testIconName2", "testIconPath2", "username2", "testpassword2"));
        tenantId3 = platformAPI.createTenant(new TenantCreator(tenantName3, "Tenant", "testIconName", "testIconPath", "testname3", "testpassword3"));
    }

    @Before
    public void setUp() throws Exception {
        if (!platformAPI.isPlatformCreated()) {
            platformAPI.createPlatform();
            BPMTestSPUtil.createEnvironmentWithDefaultTenant();
            createTenants();
        }
    }

    private long createATenant(final String tenantName) throws BonitaException {
        return platformAPI.createTenant(new TenantCreator(tenantName, "", "testIconName", "testIconPath", "default_tenant", "default_password"));
    }

    @Test
    public void searchTenants() throws Exception {
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        // according to name sort
        builder.sort(TenantSearchDescriptor.NAME, Order.ASC);
        builder.filter(TenantSearchDescriptor.DEFAULT_TENANT, false);
        SearchResult<Tenant> searchResult = platformAPI.searchTenants(builder.done());
        assertEquals(3, searchResult.getCount()); // 3 tenants created at before class
        List<Tenant> tenantList = searchResult.getResult();
        assertEquals(3, tenantList.size());
        checkOrder(tenantList, tenantName1, tenantName2, tenantName3);

        builder = new SearchOptionsBuilder(0, 10);
        builder.filter(TenantSearchDescriptor.DEFAULT_TENANT, false);
        builder.sort("name", Order.DESC);
        searchResult = platformAPI.searchTenants(builder.done());
        assertEquals(3, searchResult.getCount());
        tenantList = searchResult.getResult();
        assertNotNull(tenantList);
        assertEquals(3, tenantList.size());
        checkOrder(tenantList, tenantName3, tenantName2, tenantName1);

        builder = new SearchOptionsBuilder(0, 10);
        builder.filter(TenantSearchDescriptor.DEFAULT_TENANT, false);
        builder.sort(TenantSearchDescriptor.CREATION_DATE, Order.ASC);
        searchResult = platformAPI.searchTenants(builder.done());
        assertEquals(3, searchResult.getCount());
        tenantList = searchResult.getResult();
        assertNotNull(tenantList);
        assertEquals(3, tenantList.size());
        checkOrder(tenantList, tenantName1, tenantName2, tenantName3);

        builder = new SearchOptionsBuilder(0, 10);
        builder.filter(TenantSearchDescriptor.DEFAULT_TENANT, false);
        builder.sort(TenantSearchDescriptor.CREATION_DATE, Order.DESC);
        searchResult = platformAPI.searchTenants(builder.done());
        assertEquals(3, searchResult.getCount());
        tenantList = searchResult.getResult();
        assertNotNull(tenantList);
        assertEquals(3, tenantList.size());
        checkOrder(tenantList, tenantName3, tenantName2, tenantName1);

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

    @Cover(classes = { PlatformAPI.class }, concept = BPMNConcept.NONE, keywords = { "Search", "Tenants", "Order", "Pagination", "Column not unique" }, jira = "ENGINE-1557")
    @Test
    public void searchTenantsWithNotUniqueDescription() throws Exception {
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(TenantSearchDescriptor.DESCRIPTION, Order.ASC);
        final List<Tenant> allTenants = platformAPI.searchTenants(builder.done()).getResult();
        assertEquals(4, allTenants.size());

        builder = new SearchOptionsBuilder(0, 3);
        builder.sort(TenantSearchDescriptor.DESCRIPTION, Order.ASC);
        List<Tenant> tenants = platformAPI.searchTenants(builder.done()).getResult();
        assertEquals(3, tenants.size());
        assertEquals(allTenants.get(0).getName(), tenants.get(0).getName());
        assertEquals(allTenants.get(1).getName(), tenants.get(1).getName());
        assertEquals(allTenants.get(2).getName(), tenants.get(2).getName());

        builder = new SearchOptionsBuilder(3, 1);
        builder.sort(TenantSearchDescriptor.DESCRIPTION, Order.ASC);
        tenants = platformAPI.searchTenants(builder.done()).getResult();
        assertEquals(1, tenants.size());
        assertEquals(allTenants.get(3).getName(), tenants.get(0).getName());

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
    public void useAPIWithNullSession() throws BonitaException {
        try {
            platformAPI = PlatformAPIAccessor.getPlatformAPI(null);
            platformAPI.createTenant(new TenantCreator("test", "test create tenant", "testIconName", "testIconPath", "name", "123"));
            fail("can't get platform api with null session");
        } finally {
            session = apiTestSpUtil.loginOnPlatform();
            platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        }
    }

    @Test(expected = AlreadyExistsException.class)
    public void createAnAlreadyExistingTenantShouldThrowException() throws BonitaException {
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
            assertTrue(e.getMessage().contains("No tenant exists with name: test"));
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
            assertTrue(e.getMessage().contains("No tenant exists with id: -3"));
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
    public void activateNotExistingTenantShouldThrowException() throws BonitaException {
        platformAPI.activateTenant(9999);
    }

    @Test(expected = TenantNotFoundException.class)
    public void deactiveNotExistingTenantShouldThrowException() throws BonitaException {
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
    public void deleteNotExistingTenantShouldThrowException() throws BonitaException {
        platformAPI.deleteTenant(-3);
    }

    @Test
    public void getTenantsWithOrderByName() {
        final List<Tenant> tenants1 = platformAPI.getTenants(0, 2, TenantCriterion.NAME_ASC);
        final List<Tenant> tenants2 = platformAPI.getTenants(2, 2, TenantCriterion.NAME_ASC);
        final List<Tenant> tenants3 = platformAPI.getTenants(4, 2, TenantCriterion.NAME_ASC);
        final int count = platformAPI.getNumberOfTenants();
        assertEquals(4, count);
        assertNotNull(tenants1);
        assertNotNull(tenants2);
        assertNotNull(tenants3);
        assertEquals(2, tenants1.size());
        assertEquals(2, tenants2.size());
        assertEquals(0, tenants3.size());
        assertEquals(DEFAULT_TENANT_NAME, tenants1.get(0).getName());
        assertEquals(tenantName1, tenants1.get(1).getName());
        assertEquals(tenantName2, tenants2.get(0).getName());
    }

    @Test
    public void getTenantsWithOrderByDescription() {
        final int count = platformAPI.getNumberOfTenants();
        assertEquals(4, count);

        // Description ASC
        final List<Tenant> allTenantsAsc = platformAPI.getTenants(0, 4, TenantCriterion.DESCRIPTION_ASC);
        assertEquals(4, allTenantsAsc.size());

        List<Tenant> tenantsAsc = platformAPI.getTenants(0, 3, TenantCriterion.DESCRIPTION_ASC);
        assertEquals(3, tenantsAsc.size());
        assertEquals(DEFAULT_TENANT_NAME, tenantsAsc.get(0).getName());
        assertEquals(allTenantsAsc.get(1).getName(), tenantsAsc.get(1).getName());
        assertEquals(allTenantsAsc.get(2).getName(), tenantsAsc.get(2).getName());

        tenantsAsc = platformAPI.getTenants(3, 1, TenantCriterion.DESCRIPTION_ASC);
        assertEquals(1, tenantsAsc.size());
        assertEquals(allTenantsAsc.get(3).getName(), tenantsAsc.get(0).getName());

        // Description DESC
        final List<Tenant> allTenantsDesc = platformAPI.getTenants(0, 4, TenantCriterion.DESCRIPTION_DESC);
        assertEquals(4, allTenantsDesc.size());

        List<Tenant> tenantsDesc = platformAPI.getTenants(0, 2, TenantCriterion.DESCRIPTION_DESC);
        assertEquals(2, tenantsDesc.size());
        assertEquals(allTenantsDesc.get(0).getName(), tenantsDesc.get(0).getName());
        assertEquals(allTenantsDesc.get(1).getName(), tenantsDesc.get(1).getName());

        tenantsDesc = platformAPI.getTenants(2, 2, TenantCriterion.DESCRIPTION_DESC);
        assertEquals(2, tenantsDesc.size());
        assertEquals(allTenantsDesc.get(2).getName(), tenantsDesc.get(0).getName());
        assertEquals(DEFAULT_TENANT_NAME, tenantsDesc.get(1).getName());
    }

    @Test
    public void getTenantsWithOrderByCreationDate() {
        final List<Tenant> tenantsCreAsc = platformAPI.getTenants(0, 4, TenantCriterion.CREATION_ASC);
        assertEquals(4, tenantsCreAsc.size());
        assertEquals(DEFAULT_TENANT_NAME, tenantsCreAsc.get(0).getName());
        assertEquals(tenantName1, tenantsCreAsc.get(1).getName());
        assertEquals(tenantName2, tenantsCreAsc.get(2).getName());
        assertEquals(tenantName3, tenantsCreAsc.get(3).getName());

        final List<Tenant> tenantsCreDesc = platformAPI.getTenants(0, 4, TenantCriterion.CREATION_DESC);
        assertEquals(4, tenantsCreDesc.size());
        assertEquals(tenantName3, tenantsCreDesc.get(0).getName());
        assertEquals(tenantName2, tenantsCreDesc.get(1).getName());
        assertEquals(tenantName1, tenantsCreDesc.get(2).getName());
        assertEquals(DEFAULT_TENANT_NAME, tenantsCreDesc.get(3).getName());

        final List<Tenant> tenantsDefault = platformAPI.getTenants(0, 4, TenantCriterion.DEFAULT);
        assertEquals(4, tenantsDefault.size());
        assertEquals(tenantName3, tenantsDefault.get(0).getName());
        assertEquals(tenantName2, tenantsDefault.get(1).getName());
        assertEquals(tenantName1, tenantsDefault.get(2).getName());
        assertEquals(DEFAULT_TENANT_NAME, tenantsDefault.get(3).getName());
    }

    @Test
    public void getTenantsWithOrderByStatus() throws BonitaException {
        platformAPI.deleteTenant(tenantId3);
        platformAPI.activateTenant(tenantId1);
        try {
            final int count = platformAPI.getNumberOfTenants();
            assertEquals(3, count);

            final List<Tenant> tenantsAsc = platformAPI.getTenants(0, 10, TenantCriterion.STATE_ASC);
            assertEquals(3, tenantsAsc.size());
            assertEquals(DEFAULT_TENANT_NAME, tenantsAsc.get(0).getName());
            assertEquals(tenantName1, tenantsAsc.get(1).getName());
            assertEquals(tenantName2, tenantsAsc.get(2).getName());

            final List<Tenant> tenantsDesc = platformAPI.getTenants(0, 10, TenantCriterion.STATE_DESC);
            assertEquals(3, tenantsDesc.size());
            assertEquals(tenantName2, tenantsDesc.get(0).getName());
            assertEquals(DEFAULT_TENANT_NAME, tenantsDesc.get(1).getName());
            assertEquals(tenantName1, tenantsDesc.get(2).getName());
        } finally {
            platformAPI.deactiveTenant(tenantId1);
            tenantId3 = platformAPI.createTenant(new TenantCreator(tenantName3, "Tenant", "testIconName", "testIconPath", "testname3", "testpassword3"));
        }
    }

    @Test
    public void getTenantsWithPages() {
        final List<Tenant> testTenants = platformAPI.getTenants(0, 2, TenantCriterion.NAME_ASC);
        assertNotNull(testTenants);
        assertEquals(2, testTenants.size());
        assertEquals(DEFAULT_TENANT_NAME, testTenants.get(0).getName());
        assertEquals(tenantName1, testTenants.get(1).getName());

        final List<Tenant> tenants1 = platformAPI.getTenants(2, 10, TenantCriterion.NAME_ASC);
        assertNotNull(tenants1);
        assertEquals(2, tenants1.size());
        assertEquals(tenantName2, tenants1.get(0).getName());
        assertEquals(tenantName3, tenants1.get(1).getName());

        final List<Tenant> tenants2 = platformAPI.getTenants(0, 2, TenantCriterion.NAME_DESC);
        assertNotNull(tenants2);
        assertEquals(2, tenants2.size());
        assertEquals(tenantName3, tenants2.get(0).getName());
        assertEquals(tenantName2, tenants2.get(1).getName());
    }

    @Test
    public void getTenantsWithIndexPageOutOfRange() {
        final List<Tenant> tenants = platformAPI.getTenants(50, 100, TenantCriterion.NAME_ASC);
        assertTrue(tenants.isEmpty());
    }

    @Test(expected = PlatformNotFoundException.class)
    public void deletePlatform() throws BonitaException {
        platformAPI.stopNode();
        platformAPI.cleanPlatform();
        platformAPI.deletePlatform();
        platformAPI.getPlatform();
    }

    @Test(expected = NodeNotStartedException.class)
    public void cannotExecuteActionsWithPlatformStopped() throws BonitaException {
        platformAPI.stopNode();
        try {
            final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 1000);
            builder.filter(TenantSearchDescriptor.DEFAULT_TENANT, true);
            platformAPI.searchTenants(builder.done());
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
    public void getNumberOfTenants() {
        assertEquals(4, platformAPI.getNumberOfTenants());
    }

    @Test(expected = NodeNotStartedException.class)
    public void cannotCreateTenantWithNodeStopped() throws BonitaException {
        platformAPI.stopNode();
        try {
            createATenant("TENANT_1");
        } finally {
            platformAPI.startNode();
        }
    }

    @Test(expected = NodeNotStartedException.class)
    public void cannotGetTenantByNameWithNodeStopped() throws BonitaException {
        platformAPI.stopNode();
        try {
            platformAPI.getTenantByName(tenantName1);
        } finally {
            platformAPI.startNode();
        }
    }

    @Test(expected = NodeNotStartedException.class)
    public void canDeleteTenantWithStoppedNode() throws BonitaException {
        final long tenantId = createATenant("TENANT_1");
        try {
            platformAPI.stopNode();
            platformAPI.deleteTenant(tenantId);
        } finally {
            platformAPI.startNode();
            platformAPI.deleteTenant(tenantId);
        }
    }

    @Test(expected = NodeNotStartedException.class)
    public void cannotActivatedTenantWithNodeStopped() throws BonitaException {
        platformAPI.stopNode();
        try {
            platformAPI.activateTenant(tenantId1);
        } finally {
            platformAPI.startNode();
        }
    }

    @Test(expected = NodeNotStartedException.class)
    public void cannotDeactivateTenantWithNodeStopped() throws BonitaException {
        platformAPI.activateTenant(tenantId1);
        platformAPI.stopNode();
        try {
            platformAPI.deactiveTenant(tenantId1);
        } finally {
            platformAPI.startNode();
            platformAPI.deactiveTenant(tenantId1);
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
        platformAPI.cleanPlatform();
        platformAPI.deletePlatform();
        state = platformAPI.getPlatformState();
        assertEquals(PlatformState.STOPPED, state);
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

    @Cover(classes = { PlatformAPI.class }, concept = BPMNConcept.NONE, keywords = { "Technical user", "password", "Special characters" }, jira = "ENGINE-1224")
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

    @Test(expected = NodeNotStartedException.class)
    public void cannotUpdateTenantWithNodeNotStarted() throws Exception {
        // stop platform
        platformAPI.stopNode();
        // update tenant
        final TenantUpdater udpateDescriptor = new TenantUpdater();
        udpateDescriptor.setName("updatedTenantName");
        try {
            platformAPI.updateTenant(tenantId1, udpateDescriptor);
        } finally {
            platformAPI.startNode();
        }
    }

    @Test
    public void cleanAndDeletePlaftorm_should_deactive_tenants_before_deletion() throws Exception {
        platformAPI.activateTenant(tenantId1);
        platformAPI.stopNode();
        platformAPI.cleanAndDeletePlaftorm();

        platformAPI.createPlatform();
        apiTestSpUtil.initializeAndStartPlatformWithDefaultTenant(platformAPI, true);
        createTenants();
    }

}
