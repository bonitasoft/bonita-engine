package com.bonitasoft.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserCriterion;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.PlatformSession;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.bonitasoft.engine.api.LoginAPI;
import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.PlatformAPIAccessor;
import com.bonitasoft.engine.api.TenantAPIAccessor;

/**
 * @author Yanyan Liu
 */
public class TenantTest {

    private static final String DEFAULT_TENANT = "default";

    private final String userName = "default_tenant_name";

    private final String password = "default_tenant_password";

    private static final Object LOCK = new Object();

    private APISession apiSession;

    private static long defaultTenantId;

    @BeforeClass
    public static void beforeClass() throws BonitaException, BonitaHomeNotSetException {
        final PlatformLoginAPI platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
        final PlatformSession session = platformLoginAPI.login("platformAdmin", "platform");
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        platformAPI.createAndInitializePlatform();
        platformAPI.startNode();
        defaultTenantId = platformAPI.createTenant(DEFAULT_TENANT, "default", "testIconName", "testIconPath", "default_tenant_name", "default_tenant_password");
        platformAPI.activateTenant(defaultTenantId);
        defaultTenantId = platformAPI.getTenantByName(DEFAULT_TENANT).getId();
        platformLoginAPI.logout(session);
    }

    @AfterClass
    public static void afterClass() throws BonitaException, BonitaHomeNotSetException {
        final PlatformLoginAPI platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
        final PlatformSession session = platformLoginAPI.login("platformAdmin", "platform");
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        platformAPI.deactiveTenant(defaultTenantId);
        platformAPI.deleteTenant(defaultTenantId);
        platformAPI.stopNode();
        platformAPI.deletePlaftorm();
        platformLoginAPI.logout(session);
    }

    @Test
    public void testSingleThreadTenant() throws Exception {
        final LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();
        final APISession apiSession = loginAPI.login(defaultTenantId, userName, password);
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(apiSession);

        identityAPI.createUser("auser1", "bpm");
        final List<User> users = identityAPI.getUsers(0, 5, UserCriterion.USER_NAME_ASC);
        assertEquals(1, users.size());

        identityAPI.deleteUser("auser1");
    }

    @Test
    public void testMultiThreadTenant() throws Exception {
        final LoginThread login = new LoginThread();
        final Thread loginThread = new Thread(login);
        final GetUserRequestThread getUser = new GetUserRequestThread(login);
        final Thread getUserThread = new Thread(getUser);
        loginThread.start();
        getUserThread.start();
        synchronized (LOCK) {
            while (!getUser.isDone() && !getUser.isFailed()) {
                try {
                    LOCK.wait();
                } catch (final InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (getUser.isFailed()) {
            throw new Exception("failed to retreive user");
        }
        final List<User> users = getUser.getUsers();
        assertNotNull(users);
        assertEquals(1, users.size());
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(TenantTest.this.apiSession);
        identityAPI.deleteUser("auser1");
    }

    class LoginThread implements Runnable {

        private boolean failed = false;

        @Override
        public void run() {
            LoginAPI loginAPI;
            synchronized (LOCK) {
                try {
                    loginAPI = TenantAPIAccessor.getLoginAPI();
                    apiSession = loginAPI.login(defaultTenantId, userName, password);
                } catch (final Exception e) {
                    failed = true;
                    throw new RuntimeException(e);
                } finally {
                    LOCK.notifyAll();
                }
            }
        }

        public boolean isFailed() {
            return failed;
        }

    }

    class GetUserRequestThread implements Runnable {

        private boolean done = false;

        private List<User> users;

        private final LoginThread loginThread;

        private boolean failed;

        public GetUserRequestThread(final LoginThread login) {
            loginThread = login;
        }

        @Override
        public void run() {
            IdentityAPI identityAPI;
            synchronized (LOCK) {
                try {
                    while (apiSession == null && !loginThread.isFailed()) {
                        LOCK.wait();
                    }
                    if (loginThread.isFailed()) {
                        failed = true;
                        throw new RuntimeException("login failed");
                    }
                    identityAPI = TenantAPIAccessor.getIdentityAPI(apiSession);
                    identityAPI.createUser("auser1", "bpm");
                    users = identityAPI.getUsers(0, 5, UserCriterion.USER_NAME_ASC);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    done = true;
                    LOCK.notifyAll();
                }
            }

        }

        public boolean isDone() {
            return done;
        }

        public List<User> getUsers() {
            return users;
        }

        public boolean isFailed() {
            return failed;
        }
    }

}
