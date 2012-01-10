package org.bonitasoft.engine.log;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.bonitasoft.engine.BPMTestUtil;
import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.LogAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserUpdateDescriptor;
import org.bonitasoft.engine.session.APISession;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class LogTestSP extends CommonAPITest {

    private static LogAPI logAPI;

    private static APISession session;

    private static IdentityAPI identityAPI;

    @BeforeClass
    public static void beforeTest() throws BonitaException {
        session = BPMTestUtil.loginTenant();
        identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        logAPI = TenantAPIAccessor.getLogAPI(session);
    }

    @AfterClass
    public static void afterTest() throws BonitaException {
        BPMTestUtil.logoutTenant(session);
    }

    @Test
    public void log() throws BonitaException {
        User userOld = identityAPI.createUser("old", "oldPassword");
        UserUpdateDescriptor updateDescriptor = new UserUpdateDescriptor();
        updateDescriptor.updateUserName("new");
        updateDescriptor.updatePassword("newPassword");

        assertEquals(0, logAPI.getNumberOfLogs());

        identityAPI.updateUser(userOld.getId(), updateDescriptor);
        assertEquals(1, logAPI.getNumberOfLogs());
        identityAPI.deleteUser(userOld.getId());
        assertEquals(2, logAPI.getNumberOfLogs());

        final List<Log> logs = logAPI.getLogs(0, 2, LogCriterion.DEFAULT);
        assertEquals("IDENTITY_USER_UPDATED", logs.get(0).getActionType());
        assertEquals(SeverityLevel.INTERNAL, logs.get(0).getSeverity());
        assertNull(logs.get(0).getCallerClassName());
        assertNull(logs.get(0).getCallerMethodName());

        assertEquals("IDENTITY_USER_DELETED", logs.get(1).getActionType());
        assertEquals(SeverityLevel.INTERNAL, logs.get(1).getSeverity());
        assertNull(logs.get(1).getCallerClassName());
        assertNull(logs.get(1).getCallerMethodName());
    }

}
