package com.bonitasoft.engine.api;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import org.junit.Test;

/**
 * @author Matthieu Chaffotte
 */
public class APIAccessorTest {

    private Method getAPIMethod(final String methodName) {
        final Method[] declaredMethods = APIAccessor.class.getDeclaredMethods();
        for (final Method method : declaredMethods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    @Test
    public void checkIdentyAPI() throws SecurityException {
        final Method method = getAPIMethod("getIdentityAPI");
        assertEquals("com.bonitasoft.engine.api.IdentityAPI", method.getReturnType().getName());
    }

    @Test
    public void checkgetProcessAPI() throws SecurityException {
        final Method method = getAPIMethod("getProcessAPI");
        assertEquals("com.bonitasoft.engine.api.ProcessAPI", method.getReturnType().getName());
    }

    @Test
    public void checkMonitoringAPI() throws SecurityException {
        final Method method = getAPIMethod("getMonitoringAPI");
        assertEquals("com.bonitasoft.engine.api.MonitoringAPI", method.getReturnType().getName());
    }

    @Test
    public void checkPlatformMonitoringAPI() throws SecurityException {
        final Method method = getAPIMethod("getPlatformMonitoringAPI");
        assertEquals("com.bonitasoft.engine.api.PlatformMonitoringAPI", method.getReturnType().getName());
    }

    @Test
    public void checkgetLogAPI() throws SecurityException {
        final Method method = getAPIMethod("getLogAPI");
        assertEquals("com.bonitasoft.engine.api.LogAPI", method.getReturnType().getName());
    }

    @Test
    public void checkgetLookNFeelAPI() throws SecurityException {
        final Method method = getAPIMethod("getLookNFeelAPI");
        assertEquals("com.bonitasoft.engine.api.LookNFeelAPI", method.getReturnType().getName());
    }

    @Test
    public void checkNodeAPI() throws SecurityException {
        final Method method = getAPIMethod("getNodeAPI");
        assertEquals("com.bonitasoft.engine.api.NodeAPI", method.getReturnType().getName());
    }

    @Test
    public void checkCommandAPI() throws SecurityException {
        final Method method = getAPIMethod("getCommandAPI");
        assertEquals("org.bonitasoft.engine.api.CommandAPI", method.getReturnType().getName());
    }

    @Test
    public void checkReportingAPI() throws SecurityException {
        final Method method = getAPIMethod("getReportingAPI");
        assertEquals("com.bonitasoft.engine.api.ReportingAPI", method.getReturnType().getName());
    }

    @Test
    public void checkProfileAPI() throws SecurityException {
        final Method method = getAPIMethod("getProfileAPI");
        assertEquals("com.bonitasoft.engine.api.ProfileAPI", method.getReturnType().getName());
    }

}
