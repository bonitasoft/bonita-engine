package org.bonitasoft.engine.log;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.GroupBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserUpdateDescriptor;
import org.junit.Test;

public class LogTestSP extends CommonAPITest {

    @Test
    public void log() throws BonitaException {
        login();
        final Group group = getIdentityAPI().createGroup(new GroupBuilder().createNewInstance("group1").done());
        getIdentityAPI().deleteGroup(group.getId());

        assertEquals(0, getLogAPI().getNumberOfLogs());

        final User userOld = getIdentityAPI().createUser("old", "oldPassword");
        assertEquals(1, getLogAPI().getNumberOfLogs());

        final UserUpdateDescriptor updateDescriptor = new UserUpdateDescriptor();
        updateDescriptor.updateUserName("new");
        updateDescriptor.updatePassword("newPassword");
        getIdentityAPI().updateUser(userOld.getId(), updateDescriptor);
        assertEquals(2, getLogAPI().getNumberOfLogs());

        getIdentityAPI().deleteUser(userOld.getId());
        assertEquals(3, getLogAPI().getNumberOfLogs());

        final List<Log> logs = getLogAPI().getLogs(0, 3, LogCriterion.DEFAULT);
        assertEquals("IDENTITY_USER_DELETED", logs.get(0).getActionType());
        assertEquals(SeverityLevel.INTERNAL, logs.get(0).getSeverity());
        assertEquals("org.bonitasoft.engine.identity.impl.IdentityServiceImpl", logs.get(0).getCallerClassName());
        assertEquals("deleteUser", logs.get(0).getCallerMethodName());

        assertEquals("IDENTITY_USER_UPDATED", logs.get(1).getActionType());
        assertEquals(SeverityLevel.INTERNAL, logs.get(1).getSeverity());
        assertEquals("org.bonitasoft.engine.identity.impl.IdentityServiceImpl", logs.get(1).getCallerClassName());
        assertEquals("updateUser", logs.get(1).getCallerMethodName());

        assertEquals("IDENTITY_USER_CREATED", logs.get(2).getActionType());
        assertEquals(SeverityLevel.INTERNAL, logs.get(2).getSeverity());
        assertEquals("org.bonitasoft.engine.identity.impl.IdentityServiceImpl", logs.get(2).getCallerClassName());
        assertEquals("createUser", logs.get(2).getCallerMethodName());
        logout();
    }

}
