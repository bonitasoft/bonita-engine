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
import static org.junit.Assert.assertNotSame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.model.Comment;
import org.bonitasoft.engine.bpm.model.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.model.ProcessDefinition;
import org.bonitasoft.engine.bpm.model.ProcessDefinitionCriterion;
import org.bonitasoft.engine.bpm.model.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.model.ProcessInstance;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.InvalidBusinessArchiveFormat;
import org.bonitasoft.engine.exception.InvalidProcessDefinitionException;
import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.exception.ObjectAlreadyExistsException;
import org.bonitasoft.engine.exception.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.exception.ProcessDeployException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchCommentsDescriptor;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.APITestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.SPBPMTestUtil;
import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.PlatformAPIAccessor;

public class SPProcessManagementTest extends CommonAPISPTest {

    private static final String PROCESS_VERSION = "1.0";

    private static final String PROCESS_NAME = "ProcessManagementTest";

    private static final String PASSWORD = "secretPassword";

    private static final String USERNAME = "yanyanliu";

    private static final String ACTOR_NAME = APITestUtil.ACTOR_NAME;

    @After
    public void afterTest() throws BonitaException {
        logout();
    }

    @Before
    public void beforeTest() throws BonitaException {
        login();
    }

    private void logoutThenlogin() throws BonitaException {
        logout();
        login();
    }

    private void logoutThenloginAs(final String userName, final String password, final long tenantId) throws BonitaException {
        logout();
        loginWith(userName, password, tenantId);
    }

    @Test
    public void getProcessesListOnMultipleTenants() throws Exception {
        logout();
        final String tenantName = "myTestTenant";

        PlatformSession platformSession = APITestUtil.loginPlatform();
        PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(platformSession);
        final long tenantId = platformAPI.createTenant(tenantName, "default", "defaultIconName", "defaultIconPath", "default_tenant_name",
                "default_tenant_password");
        platformAPI.activateTenant(tenantId);
        APITestUtil.logoutPlatform(platformSession);

        login();

        assertEquals(0, getProcessAPI().getNumberOfProcesses());
        List<Long> ids = createProcessDefinitionWithTwoHumanStepsAndDeployBusinessArchive(10);

        assertEquals(10, getProcessAPI().getNumberOfProcesses());
        List<ProcessDeploymentInfo> processes = getProcessAPI().getProcesses(0, 10, ProcessDefinitionCriterion.NAME_DESC);
        assertEquals(10, processes.size());
        assertEquals("ProcessManagementTest09", processes.get(0).getName());
        assertEquals("ProcessManagementTest00", processes.get(9).getName());

        logoutThenloginAs("default_tenant_name", "default_tenant_password", tenantId);

        assertEquals(0, getProcessAPI().getNumberOfProcesses());
        ids = createProcessDefinitionWithTwoHumanStepsAndDeployBusinessArchive(10);

        assertEquals(10, getProcessAPI().getNumberOfProcesses());
        processes = getProcessAPI().getProcesses(0, 10, ProcessDefinitionCriterion.NAME_DESC);
        assertEquals(10, processes.size());
        assertEquals("ProcessManagementTest09", processes.get(0).getName());
        assertEquals("ProcessManagementTest00", processes.get(9).getName());
        getProcessAPI().deleteProcesses(ids);
        assertEquals(0, getProcessAPI().getNumberOfProcesses());

        logoutThenlogin();
        ids = new ArrayList<Long>();
        assertNotSame(0, getProcessAPI().getNumberOfProcesses());
        processes = getProcessAPI().getProcesses(0, 10, ProcessDefinitionCriterion.DEFAULT);
        for (final ProcessDeploymentInfo processDeploymentInfo : processes) {
            ids.add(processDeploymentInfo.getProcessId());
        }
        getProcessAPI().deleteProcesses(ids);
        assertEquals(0, getProcessAPI().getNumberOfProcesses());

        logout();
        platformSession = APITestUtil.loginPlatform();
        platformAPI = PlatformAPIAccessor.getPlatformAPI(platformSession);
        platformAPI.deactiveTenant(tenantId);
        platformAPI.deleteTenant(tenantId);
        APITestUtil.logoutPlatform(platformSession);

        login();
    }

    @Test
    public void testSearchCommentsInTenants() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);
        loginWith(USERNAME, PASSWORD);
        DesignProcessDefinition designProcessDefinition;
        designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("step1", "step2"), Arrays.asList(true, true));
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition, ACTOR_NAME, user);
        final String commentContent1 = "commentContent1";
        final String commentContent2 = "commentContent2";
        final ProcessInstance pi = getProcessAPI().startProcess(processDefinition.getId());
        getProcessAPI().addComment(pi.getId(), commentContent1);
        getProcessAPI().addComment(pi.getId(), commentContent2);
        logout();

        final long tenant1 = SPBPMTestUtil.constructTenant("suomenlinna", null, null, "hamme", "saari");
        loginWith("hamme", "saari", tenant1);
        final User user1 = createUser(USERNAME, PASSWORD);
        loginWith(USERNAME, PASSWORD, tenant1);
        final ProcessDefinition processDefinition1 = deployAndEnableWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance pi1 = getProcessAPI().startProcess(processDefinition1.getId());
        final String commentContent11 = "commentContent11";
        final String commentContent12 = "commentContent12";
        getProcessAPI().addComment(pi1.getId(), commentContent11);
        getProcessAPI().addComment(pi1.getId(), commentContent12);

        final SearchOptionsBuilder builder0 = new SearchOptionsBuilder(0, 5);
        builder0.filter(SearchCommentsDescriptor.PROCESS_INSTANCE_ID, pi1.getId());
        builder0.sort(SearchCommentsDescriptor.POSTDATE, Order.ASC);

        final SearchResult<Comment> searchResult0 = getProcessAPI().searchComments(builder0.done());
        final List<Comment> commentList0 = searchResult0.getResult();
        assertEquals(2, commentList0.size());
        disableAndDelete(processDefinition1);
        deleteUser(user1);
        logout();
        login();
        disableAndDelete(processDefinition);
        deleteUser(user);
        SPBPMTestUtil.destroyTenant(tenant1);
    }

    private List<Long> createProcessDefinitionWithTwoHumanStepsAndDeployBusinessArchive(final int nbProcess) throws InvalidProcessDefinitionException,
            InvalidSessionException, ProcessDeployException, ProcessDefinitionNotFoundException, InvalidBusinessArchiveFormat, ObjectAlreadyExistsException {
        final List<Long> ids = new ArrayList<Long>();
        for (int i = 0; i < nbProcess; i++) {
            String processName = PROCESS_NAME;
            if (i >= 0 && i < 10) {
                processName += "0";
            }
            final DesignProcessDefinition processDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps(processName + i, PROCESS_VERSION
                    + i, Arrays.asList("step1", "step2"), Arrays.asList(true, true));
            ids.add(getProcessAPI().deploy(new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefinition).done()).getId());
        }
        return ids;
    }

}
