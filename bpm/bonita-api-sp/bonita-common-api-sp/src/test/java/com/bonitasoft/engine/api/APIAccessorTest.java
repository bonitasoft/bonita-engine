/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
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
    public void checkgetThemeAPI() throws SecurityException {
        final Method method = getAPIMethod("getThemeAPI");
        assertEquals("com.bonitasoft.engine.api.ThemeAPI", method.getReturnType().getName());
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
