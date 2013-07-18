/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnableToReadBonitaClientConfiguration;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.util.APITypeManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonitasoft.engine.api.TenantAPIAccessor;

/**
 * 
 * @author Baptiste Mesta
 * 
 */
public class ClusterTests extends CommonAPISPTest {

    static Logger LOGGER = LoggerFactory.getLogger(ClusterTests.class);

    private User user;

    // @BeforeClass
    // public static void deployCommand() throws Exception{
    // final APISession loginDefaultTenant = loginDefaultTenant();
    // ClientEventUtil.deployCommand(loginDefaultTenant);
    // logoutTenant(loginDefaultTenant);
    // }
    //
    // @AfterClass
    // public static void undeployCommand() throws Exception{
    // final APISession loginDefaultTenant = loginDefaultTenant();
    // ClientEventUtil.deployCommand(loginDefaultTenant);
    // logoutTenant(loginDefaultTenant);
    // }

    @Before
    public void beforeTest() throws Exception {
        login();
        user = createUser(USERNAME, PASSWORD);
        logout();
        // init the context here
        changeToNode2();
        loginWith(USERNAME, PASSWORD);
        changeToNode1();
    }

    @After
    public void afterTest() throws Exception {
        deleteUser(user.getId());
        changeToNode1();
        logout();
    }

    protected void changeToNode1() throws Exception {
        setConnectionPort("7180");
        changeApis();
    }

    protected void changeToNode2() throws Exception {
        setConnectionPort("7280");
        changeApis();
    }

    private void setConnectionPort(String port) throws Exception {
        Map<String, String> parameters = new HashMap<String, String>(2);
        parameters.put("server.url", "http://localhost:" + port);
        parameters.put("application.name", "bonita");
        APITypeManager.setAPITypeAndParams(ApiAccessType.HTTP, parameters);

        LOGGER.info("changed to " + port);
    }

    private void changeApis() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException, UnableToReadBonitaClientConfiguration {
        setIdentityAPI(TenantAPIAccessor.getIdentityAPI(getSession()));
        setProcessAPI(TenantAPIAccessor.getProcessAPI(getSession()));
        setProfileAPI(TenantAPIAccessor.getProfileAPI(getSession()));
        setCommandAPI(TenantAPIAccessor.getCommandAPI(getSession()));
        setReportingAPI(TenantAPIAccessor.getReportingAPI(getSession()));
        setMonitoringAPI(TenantAPIAccessor.getMonitoringAPI(getSession()));
        setPlatformMonitoringAPI(TenantAPIAccessor.getPlatformMonitoringAPI(getSession()));
    }

    @Test
    public void createUserAndGetItOnOtherNode() throws Exception {
        User createUser = getIdentityAPI().createUser("john", "bpm", "John", "Doe");
        changeToNode2();
        User userByUserName = getIdentityAPI().getUserByUserName("john");
        assertEquals(createUser, userByUserName);
        getIdentityAPI().deleteUser("john");

    }

}
