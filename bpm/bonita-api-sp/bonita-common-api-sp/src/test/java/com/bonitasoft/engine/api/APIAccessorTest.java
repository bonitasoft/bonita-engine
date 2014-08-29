/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.junit.Test;

/**
 * @author Matthieu Chaffotte
 */
public class APIAccessorTest {

    private Method getAPIMethod(final String methodName) throws Exception {
        final Method[] declaredMethods = APIAccessor.class.getDeclaredMethods();
        for (final Method method : declaredMethods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new Exception("method " + methodName + " not found");
    }

    private void checkMethodReturnExpectedReturnType(final String methodName, final String expected) throws Exception {
        final Method method = getAPIMethod(methodName);
        assertThat(method.getReturnType().getName()).as("method " + methodName + " sould return type " + expected).isEqualTo(expected);
    }

    @Test
    public void checkIdentyAPI() throws Exception {
        checkMethodReturnExpectedReturnType("getIdentityAPI", "com.bonitasoft.engine.api.IdentityAPI");
    }

    @Test
    public void checkgetProcessAPI() throws Exception {
        checkMethodReturnExpectedReturnType("getProcessAPI", "com.bonitasoft.engine.api.ProcessAPI");
    }

    @Test
    public void checkMonitoringAPI() throws Exception {
        checkMethodReturnExpectedReturnType("getMonitoringAPI", "com.bonitasoft.engine.api.MonitoringAPI");
    }

    @Test
    public void checkPlatformMonitoringAPI() throws Exception {
        checkMethodReturnExpectedReturnType("getPlatformMonitoringAPI", "com.bonitasoft.engine.api.PlatformMonitoringAPI");
    }

    @Test
    public void checkgetLogAPI() throws Exception {
        checkMethodReturnExpectedReturnType("getLogAPI", "com.bonitasoft.engine.api.LogAPI");
    }

    @Test
    public void checkgetThemeAPI() throws Exception {
        checkMethodReturnExpectedReturnType("getThemeAPI", "com.bonitasoft.engine.api.ThemeAPI");
    }

    @Test
    public void checkNodeAPI() throws Exception {
        checkMethodReturnExpectedReturnType("getNodeAPI", "com.bonitasoft.engine.api.NodeAPI");
    }

    @Test
    public void checkCommandAPI() throws Exception {
        checkMethodReturnExpectedReturnType("getCommandAPI", "org.bonitasoft.engine.api.CommandAPI");
    }

    @Test
    public void checkReportingAPI() throws Exception {
        checkMethodReturnExpectedReturnType("getReportingAPI", "com.bonitasoft.engine.api.ReportingAPI");
    }

    @Test
    public void checkProfileAPI() throws Exception {
        checkMethodReturnExpectedReturnType("getProfileAPI", "com.bonitasoft.engine.api.ProfileAPI");
    }

    @Test
    public void checkPageAPI() throws Exception {
        checkMethodReturnExpectedReturnType("getPageAPI", "com.bonitasoft.engine.api.PageAPI");
    }

    @Test
    public void checkApplicationAPI() throws Exception {
        checkMethodReturnExpectedReturnType("getApplicationAPI", "com.bonitasoft.engine.api.ApplicationAPI");
    }

}
