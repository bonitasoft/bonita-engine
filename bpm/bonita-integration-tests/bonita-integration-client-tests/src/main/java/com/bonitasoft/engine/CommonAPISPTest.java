/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.api.ActorSorting;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.bpm.model.ActivityInstance;
import org.bonitasoft.engine.bpm.model.ActorInstance;
import org.bonitasoft.engine.bpm.model.Category;
import org.bonitasoft.engine.bpm.model.CategoryCriterion;
import org.bonitasoft.engine.bpm.model.ProcessDefinition;
import org.bonitasoft.engine.bpm.model.ProcessDefinitionCriterion;
import org.bonitasoft.engine.bpm.model.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.model.ProcessInstance;
import org.bonitasoft.engine.bpm.model.archive.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.model.breakpoint.Breakpoint;
import org.bonitasoft.engine.bpm.model.breakpoint.BreakpointCriterion;
import org.bonitasoft.engine.command.CommandDescriptor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.GroupCriterion;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.RoleCriterion;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserCriterion;
import org.bonitasoft.engine.search.ArchivedFlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.search.CommandSearchDescriptor;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.TestStates;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.PlatformAPIAccessor;
import com.bonitasoft.engine.api.ProcessAPI;
import com.bonitasoft.engine.api.ProcessManagementAPI;
import com.bonitasoft.engine.api.TenantAPIAccessor;
import com.bonitasoft.engine.platform.Tenant;

/**
 * @author Matthieu Chaffotte
 */
public abstract class CommonAPISPTest extends APITestSPUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonAPISPTest.class);

    @BeforeClass
    public static void beforeClass() throws BonitaException {
        SPBPMTestUtil.createEnvironmentWithDefaultTenant();
    }

    @AfterClass
    public static void afterClass() throws BonitaException {
        SPBPMTestUtil.destroyPlatformAndTenants();
    }

    @Rule
    public TestRule testWatcher = new TestWatcher() {

        @Override
        public void starting(final Description d) {
            LOGGER.info("Starting test: " + d.getClassName() + "." + d.getMethodName());
        }

        @Override
        public void failed(final Throwable cause, final Description d) {
            LOGGER.error("Failed test: " + d.getClassName() + "." + d.getMethodName());
            try {
                clean();
            } catch (final Exception be) {
                LOGGER.error("Unable to clean db", be);
            }
        }

        @Override
        public void succeeded(final Description d) {
            List<String> clean = null;
            try {
                clean = clean();
            } catch (final BonitaException e) {
                throw new BonitaRuntimeException(e);
            }
            LOGGER.info("Succeeded test: " + d.getClassName() + "." + d.getMethodName());
            if (!clean.isEmpty()) {
                throw new BonitaRuntimeException(clean.toString());
            }
        }
    };

    /**
     * FIXME: clean actors!
     * 
     * @return
     * @throws BonitaException
     */
    private List<String> clean() throws BonitaException {
        final List<String> messages = new ArrayList<String>();
        final PlatformSession platformSession = SPBPMTestUtil.loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(platformSession);
        final List<Tenant> tenants = platformAPI.getTenants(0, 1000);
        SPBPMTestUtil.logoutPlatform(platformSession);
        for (final Tenant tenant : tenants) {
            final APISession apiSession = SPBPMTestUtil.loginTenant(tenant.getId());

            final CommandAPI commandAPI = TenantAPIAccessor.getCommandAPI(apiSession);
            final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 1000);
            searchOptionsBuilder.filter(CommandSearchDescriptor.SYSTEM, false);
            final SearchResult<CommandDescriptor> searchCommands = commandAPI.searchCommands(searchOptionsBuilder.done());
            final List<CommandDescriptor> commands = searchCommands.getResult();
            if (searchCommands.getCount() > 0) {
                final StringBuilder commandBuilder = new StringBuilder("Commands are still present: ");
                for (final CommandDescriptor command : commands) {
                    commandBuilder.append(command.getName()).append(", ");
                    commandAPI.unregister(command.getName());
                }
                messages.add(commandBuilder.toString());
            }

            final ProcessManagementAPI processManagementAPI = TenantAPIAccessor.getProcessAPI(apiSession);
            final List<ProcessDeploymentInfo> processes = processManagementAPI.getProcesses(0, 200, ProcessDefinitionCriterion.DEFAULT);
            if (processes.size() > 0) {
                final StringBuilder processBuilder = new StringBuilder("Process Definitions are still active: ");
                for (final ProcessDeploymentInfo processDeploymentInfo : processes) {
                    processBuilder.append(processDeploymentInfo.getId()).append(", ");
                    if (TestStates.getProcessDepInfoEnabledState().equals(processDeploymentInfo.getActivationState())) {
                        processManagementAPI.disableProcess(processDeploymentInfo.getProcessId());
                    }
                    processManagementAPI.deleteProcess(processDeploymentInfo.getProcessId());
                }
                messages.add(processBuilder.toString());
            }

            final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(apiSession);
            final long numberOfUsers = identityAPI.getNumberOfUsers();
            if (numberOfUsers > 0) {
                final List<User> users = identityAPI.getUsers(0, Long.valueOf(numberOfUsers).intValue(), UserCriterion.USER_NAME_ASC);
                final StringBuilder userBuilder = new StringBuilder("Users are still present: ");
                for (final User user : users) {
                    userBuilder.append(user.getId()).append(", ");
                    identityAPI.deleteUser(user.getId());
                }
                messages.add(userBuilder.toString());
            }

            final long numberOfGroups = identityAPI.getNumberOfGroups();
            if (numberOfGroups > 0) {
                final List<Group> groups = identityAPI.getGroups(0, Long.valueOf(numberOfGroups).intValue(), GroupCriterion.NAME_ASC);
                final StringBuilder groupBuilder = new StringBuilder("Groups are still present: ");
                for (final Group group : groups) {
                    groupBuilder.append(group.getId()).append(", ");
                    identityAPI.deleteGroup(group.getId());
                }
                messages.add(groupBuilder.toString());
            }

            final long numberOfRoles = identityAPI.getNumberOfRoles();
            if (numberOfRoles > 0) {
                final List<Role> roles = identityAPI.getRoles(0, Long.valueOf(numberOfRoles).intValue(), RoleCriterion.NAME_ASC);
                final StringBuilder roleBuilder = new StringBuilder("Roles are still present: ");
                for (final Role role : roles) {
                    roleBuilder.append(role.getId()).append(", ");
                    identityAPI.deleteRole(role.getId());
                }
                messages.add(roleBuilder.toString());
            }

            final ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(apiSession);
            final long numberOfCategories = processAPI.getNumberOfCategories();
            if (numberOfCategories > 0) {
                final List<Category> categories = processAPI.getCategories(0, 5000, CategoryCriterion.NAME_ASC);
                final StringBuilder categoryBuilder = new StringBuilder("Categories are still present: ");
                for (final Category category : categories) {
                    categoryBuilder.append(category.getName()).append(", ");
                    processAPI.deleteCategory(category.getId());
                }
                messages.add(categoryBuilder.toString());
            }

            final List<Breakpoint> breakpoints = processAPI.getBreakpoints(0, 10000, BreakpointCriterion.DEFINITION_ID_ASC);
            if (breakpoints.size() > 0) {
                final StringBuilder bpBuilder = new StringBuilder("Breakpoints are still present: ");
                for (final Breakpoint breakpoint : breakpoints) {
                    bpBuilder.append(breakpoint.getElementName()).append(", ");
                    processAPI.removeBreakpoint(breakpoint.getId());
                }
                messages.add(bpBuilder.toString());
            }
            // final int numberOfArchivedProcessInstances = processAPI.getNumberOfArchivedProcessInstances();
            // if (numberOfArchivedProcessInstances > 0) {
            // final List<ArchivedProcessInstance> archivedProcessInstances = processAPI.getArchivedProcessInstances(0, numberOfArchivedProcessInstances,
            // ProcessInstanceCriterion.NAME_ASC);
            // final StringBuilder categoryBuilder = new StringBuilder("Archived processes are still present: ");
            // for (final ArchivedProcessInstance archivedProcessInstance : archivedProcessInstances) {
            // categoryBuilder.append(archivedProcessInstance.getName()).append(", ");
            // processAPI.
            // }
            // messages.add(categoryBuilder.toString());
            // }

            SPBPMTestUtil.logoutTenant(apiSession);
        }
        return messages;
    }

    protected ActivityInstance waitForTaskToFail(final int repeatEach, final int timeout, final ProcessInstance processInstance) throws Exception {
        final CheckNbOfActivities waitForStep1 = new CheckNbOfActivities(getProcessAPI(), repeatEach, timeout, false, processInstance, 1,
                TestStates.getFailedState());
        assertTrue(waitForStep1.waitUntil());
        final ActivityInstance next = waitForStep1.getResult().iterator().next();
        return next;
    }

    protected void checkWasntExecuted(final ProcessInstance parentProcessInstance, final String flowNodeName) throws InvalidSessionException, SearchException {
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 20);
        searchOptionsBuilder.filter(ArchivedFlowNodeInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID, parentProcessInstance.getId());
        searchOptionsBuilder.filter(ArchivedFlowNodeInstanceSearchDescriptor.NAME, flowNodeName);
        final SearchResult<ArchivedFlowNodeInstance> searchArchivedActivities = getProcessAPI().searchArchivedFlowNodeInstances(searchOptionsBuilder.done());
        assertTrue(searchArchivedActivities.getCount() == 0);
    }

    /**
     * First actor means "first one in Alphanumerical order !"
     */
    protected void addUserToFirstActorOfProcess(final long userId, final ProcessDefinition processDefinition) throws BonitaException {
        final List<ActorInstance> actors = getProcessAPI().getActors(processDefinition.getId(), 0, 1, ActorSorting.NAME_ASC);
        final ActorInstance actor = actors.get(0);
        getProcessAPI().addUserToActor(actor.getId(), userId);
    }

}
