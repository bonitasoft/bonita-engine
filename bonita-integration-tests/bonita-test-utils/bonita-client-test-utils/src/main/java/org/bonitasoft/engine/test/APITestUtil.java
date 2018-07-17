/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 */
package org.bonitasoft.engine.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder.aBusinessArchive;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.api.APIClient;
import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.BusinessDataAPI;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.api.PermissionAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.api.TenantAdministrationAPI;
import org.bonitasoft.engine.api.ThemeAPI;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.Query;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.RelationField;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import org.bonitasoft.engine.bpm.actor.ActorCriterion;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.actor.ActorNotFoundException;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.category.Category;
import org.bonitasoft.engine.bpm.category.CategoryCriterion;
import org.bonitasoft.engine.bpm.data.ArchivedDataInstance;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentCriterion;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ActivityStates;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstanceNotFoundException;
import org.bonitasoft.engine.bpm.flownode.GatewayInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoCriterion;
import org.bonitasoft.engine.bpm.process.ProcessEnablementException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceCriterion;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.supervisor.ProcessSupervisor;
import org.bonitasoft.engine.bpm.supervisor.ProcessSupervisorSearchDescriptor;
import org.bonitasoft.engine.command.CommandDescriptor;
import org.bonitasoft.engine.command.CommandExecutionException;
import org.bonitasoft.engine.command.CommandNotFoundException;
import org.bonitasoft.engine.command.CommandParameterizationException;
import org.bonitasoft.engine.command.CommandSearchDescriptor;
import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connectors.TestConnectorEngineExecutionContext;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.GroupCreator;
import org.bonitasoft.engine.identity.GroupCriterion;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.RoleCriterion;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserCreator;
import org.bonitasoft.engine.identity.UserCriterion;
import org.bonitasoft.engine.identity.UserMembership;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.engine.test.check.CheckNbOfArchivedActivities;
import org.bonitasoft.engine.test.check.CheckNbOfArchivedActivityInstances;
import org.bonitasoft.engine.test.check.CheckNbOfOpenActivities;
import org.bonitasoft.engine.test.check.CheckNbOfProcessInstances;
import org.bonitasoft.engine.test.check.CheckNbPendingTaskOf;
import org.bonitasoft.engine.test.check.CheckProcessInstanceIsArchived;
import org.bonitasoft.engine.test.wait.WaitForArchivedActivity;
import org.bonitasoft.engine.test.wait.WaitForCompletedArchivedStep;
import org.bonitasoft.engine.test.wait.WaitForEvent;
import org.bonitasoft.engine.test.wait.WaitForFinalArchivedActivity;
import org.bonitasoft.engine.test.wait.WaitForPendingTasks;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @author Emmanuel Duchastenier
 * @author Frederic Bouquet
 * @author Celine Souchet
 */
public class APITestUtil extends PlatformTestUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger(APITestUtil.class);

    public static final int DEFAULT_REPEAT_EACH = 500;

    public static final int DEFAULT_TIMEOUT;

    public static final String ACTOR_NAME = BuildTestUtil.ACTOR_NAME;

    public static final String PROCESS_VERSION = BuildTestUtil.PROCESS_VERSION;

    public static final String PROCESS_NAME = BuildTestUtil.PROCESS_NAME;

    public static final String PASSWORD = BuildTestUtil.PASSWORD;

    public static final String USERNAME = BuildTestUtil.USERNAME;

    public static final String GROUP_NAME = BuildTestUtil.GROUP_NAME;

    public static final String ROLE_NAME = BuildTestUtil.ROLE_NAME;
    public static final String FIND_BY_HIRE_DATE_RANGE = "findByHireDateRange";
    public static final String COUNT_FOR_FIND_BY_HIRE_DATE_RANGE = "countForFindByHireDateRange";
    private static final String BDM_PACKAGE_PREFIX = "com.company.model";
    protected static final String COUNTRY_QUALIFIED_NAME = BDM_PACKAGE_PREFIX + ".Country";
    protected static final String ADDRESS_QUALIFIED_NAME = BDM_PACKAGE_PREFIX + ".Address";
    protected static final String DOG_QUALIFIED_NAME = BDM_PACKAGE_PREFIX + ".Dog";
    protected static final String CAT_QUALIFIED_NAME = BDM_PACKAGE_PREFIX + ".Cat";
    protected static final String EMPLOYEE_QUALIFIED_NAME = BDM_PACKAGE_PREFIX + ".Employee";
    protected static final String PRODUCT_QUALIFIED_NAME = BDM_PACKAGE_PREFIX + ".Product";
    protected static final String PRODUCT_CATALOG_QUALIFIED_NAME = BDM_PACKAGE_PREFIX + ".ProductCatalog";
    protected static final String GET_EMPLOYEE_BY_LAST_NAME_QUERY_NAME = "findByLastName";
    protected static final String GET_EMPLOYEE_BY_PHONE_NUMBER_QUERY_NAME = "findByPhoneNumber";
    protected static final String FIND_BY_FIRST_NAME_FETCH_ADDRESSES = "findByFirstNameFetchAddresses";
    protected static final String FIND_BY_FIRST_NAME_AND_LAST_NAME_NEW_ORDER = "findByFirstNameAndLastNameNewOrder";
    protected static final String COUNT_EMPLOYEE = "countEmployee";
    protected static final String COUNT_ADDRESS = "countAddress";
    private static final String PERSON_QUALIFIED_NAME = BDM_PACKAGE_PREFIX + ".Person";
    protected static final String FIND_EMPLOYEE_WITH_FIRSTNAMES = "findEmployeeWithFirstNames";

    private final APIClient apiClient = new APIClient();

    static {
        final String strTimeout = System.getProperty("sysprop.bonita.default.test.timeout");
        if (strTimeout != null) {
            DEFAULT_TIMEOUT = Integer.valueOf(strTimeout);
        } else {
            DEFAULT_TIMEOUT = 2 * 60 * 1000;
        }
    }

    @After
    public void clearSynchroRepository() {
        try {
            loginOnDefaultTenantWithDefaultTechnicalUser();
            ClientEventUtil.clearRepo(getCommandAPI());
            logoutOnTenant();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void loginOnDefaultTenantWith(final String userName, final String password) throws BonitaException {
        getApiClient().login(userName, password);
    }

    public void loginOnDefaultTenantWithDefaultTechnicalUser() throws BonitaException {
        getApiClient().login(DEFAULT_TECHNICAL_LOGGER_USERNAME, DEFAULT_TECHNICAL_LOGGER_PASSWORD);
    }

    public BusinessDataAPI getBusinessDataAPI() {
        return getApiClient().getBusinessDataAPI();
    }

    public void logoutOnTenant() throws BonitaException {
        getApiClient().logout();
    }

    public void logoutThenlogin() throws BonitaException {
        logoutOnTenant();
        loginOnDefaultTenantWithDefaultTechnicalUser();
    }

    public void logoutThenloginAs(final String userName, final String password) throws BonitaException {
        logoutOnTenant();
        loginOnDefaultTenantWith(userName, password);
    }

    public void addMappingOfActorsForRole(final String actorName, final long roleId, final ProcessDefinition definition) throws BonitaException {
        getProcessAPI().addRoleToActor(actorName, definition, roleId);
    }

    public void addMappingOfActorsForRoleAndGroup(final String actorName, final long roleId, final long groupId, final ProcessDefinition definition)
            throws BonitaException {
        getProcessAPI().addRoleAndGroupToActor(actorName, definition, roleId, groupId);
    }

    /**
     * First actor means "first one in Alphanumerical order !"
     */
    public void addUserToFirstActorOfProcess(final long userId, final ProcessDefinition processDefinition) throws BonitaException {
        final List<ActorInstance> actors = getProcessAPI().getActors(processDefinition.getId(), 0, 1, ActorCriterion.NAME_ASC);
        final ActorInstance actor = actors.get(0);
        getProcessAPI().addUserToActor(actor.getId(), userId);
    }

    public ActorInstance getActor(final String actorName, final ProcessDefinition processDefinition) throws ActorNotFoundException {
        final List<ActorInstance> actors = getProcessAPI().getActors(processDefinition.getId(), 0, 50, ActorCriterion.NAME_ASC);
        final ActorInstance actorInstance = getActorInstance(actors, actorName);
        if (actorInstance == null) {
            throw new ActorNotFoundException(actorName, processDefinition);
        }
        return actorInstance;
    }

    private ActorInstance getActorInstance(final List<ActorInstance> actors, final String actorName) {
        for (final ActorInstance actor : actors) {
            if (actor.getName().equals(actorName)) {
                return actor;
            }
        }
        return null;
    }

    public User createUser(final String userName, final String password) throws BonitaException {
        return getIdentityAPI().createUser(userName, password);
    }

    public User createUser(final String userName, final String password, final String firstName, final String lastName) throws BonitaException {
        return getIdentityAPI().createUser(userName, password, firstName, lastName);
    }

    public User createUser(final UserCreator creator) throws BonitaException {
        return getIdentityAPI().createUser(creator);
    }

    public User createUser(final String userName, final long managerId) throws BonitaException {
        return createUser(userName, "bpm", managerId);
    }

    public User createUser(final String userName, final String password, final long managerId) throws BonitaException {
        final UserCreator creator = new UserCreator(userName, password);
        creator.setManagerUserId(managerId);
        return getIdentityAPI().createUser(creator);
    }

    public User createUserAndLogin(final String userName, final String password) throws BonitaException {
        final User user = getIdentityAPI().createUser(userName, password);
        logoutOnTenant();
        loginOnDefaultTenantWith(userName, password);
        return user;
    }

    public void deleteUsers(final User... users) throws BonitaException {
        deleteUsers(Arrays.asList(users));
    }

    public void deleteUsers(final List<User> users) throws BonitaException {
        if (users != null) {
            for (final User user : users) {
                getIdentityAPI().deleteUser(user.getId());
            }
        }
    }

    public void deleteGroups(final Group... groups) throws BonitaException {
        deleteGroups(Arrays.asList(groups));
    }

    public void deleteGroups(final List<Group> groups) throws BonitaException {
        if (groups != null) {
            for (final Group group : groups) {
                getIdentityAPI().deleteGroup(group.getId());
            }
        }
    }

    public void deleteRoles(final Role... roles) throws BonitaException {
        deleteRoles(Arrays.asList(roles));
    }

    public void deleteRoles(final List<Role> roles) throws BonitaException {
        if (roles != null) {
            for (final Role role : roles) {
                getIdentityAPI().deleteRole(role.getId());
            }
        }
    }

    public void deleteUserMembership(final long id) throws BonitaException {
        getIdentityAPI().deleteUserMembership(id);
    }

    public void deleteUserMemberships(final UserMembership... userMemberships) throws BonitaException {
        deleteUserMemberships(Arrays.asList(userMemberships));
    }

    public void deleteUserMemberships(final List<UserMembership> userMemberships) throws BonitaException {
        if (userMemberships != null) {
            for (final UserMembership userMembership : userMemberships) {
                getIdentityAPI().deleteUserMembership(userMembership.getId());
            }
        }
    }

    public UserMembership createUserMembership(final String userName, final String roleName, final String groupName) throws BonitaException {
        return getIdentityAPI().addUserMembership(getIdentityAPI().getUserByUserName(userName).getId(), getIdentityAPI().getGroupByPath(groupName).getId(),
                getIdentityAPI().getRoleByName(roleName).getId());
    }

    public void deleteProcess(final ProcessDefinition... processDefinitions) throws BonitaException {
        deleteProcess(Arrays.asList(processDefinitions));
    }

    public void deleteProcess(final ProcessDefinition processDefinition) throws BonitaException {
        deleteProcess(processDefinition.getId());
    }

    public void deleteProcess(final List<ProcessDefinition> processDefinitions) throws BonitaException {
        if (processDefinitions != null) {
            for (final ProcessDefinition processDefinition : processDefinitions) {
                deleteProcess(processDefinition);
            }
        }
    }

    public void deleteProcess(final long processDefinitionId) throws BonitaException {
        deleteProcessInstanceAndArchived(processDefinitionId);
        getProcessAPI().deleteProcessDefinition(processDefinitionId);
    }

    public void deleteUser(final String userName) throws BonitaException {
        getIdentityAPI().deleteUser(userName);
    }

    public void deleteUser(final User user) throws BonitaException {
        getIdentityAPI().deleteUser(user.getId());
    }

    public void deleteCategories(final List<Category> categories) throws BonitaException {
        for (final Category category : categories) {
            getProcessAPI().deleteCategory(category.getId());
        }
    }

    public ProcessDefinition deployAndEnableProcess(final DesignProcessDefinition designProcessDefinition) throws BonitaException {
        return deployAndEnableProcess(new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
    }

    public ProcessDefinition deployAndEnableProcess(final BusinessArchive businessArchive) throws BonitaException {
        final ProcessDefinition processDefinition = deployProcess(businessArchive);
        enableProcess(processDefinition);
        return processDefinition;

    }

    public ProcessDefinition deployAndEnableProcessWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final User user)
            throws BonitaException {
        return deployAndEnableProcessWithActor(designProcessDefinition, Collections.singletonList(actorName), Collections.singletonList(user));
    }

    public ProcessDefinition deployAndEnableProcessWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName,
            final List<User> users)
            throws BonitaException {
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        return deployAndEnableProcessWithActor(businessArchive, actorName, users);
    }

    public ProcessDefinition deployAndEnableProcessWithActor(final BusinessArchive businessArchive, final String actorName, final List<User> users)
            throws BonitaException {
        final ProcessDefinition processDefinition = deployProcess(businessArchive);
        for (final User user : users) {
            getProcessAPI().addUserToActor(actorName, processDefinition, user.getId());
        }
        enableProcess(processDefinition);
        return processDefinition;
    }

    public ProcessDefinition deployAndEnableProcessWithActorAndParameters(final DesignProcessDefinition designProcessDefinition, final String actorName,
            final User user, final Map<String, String> parameters) throws BonitaException {
        return deployAndEnableProcessWithActorAndParameters(designProcessDefinition, Collections.singletonList(actorName), Collections.singletonList(user),
                parameters);
    }

    public ProcessDefinition deployAndEnableProcessWithActor(final DesignProcessDefinition designProcessDefinition, final List<String> actorsName,
            final List<User> users) throws BonitaException {
        return deployAndEnableProcessWithActor(new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done(),
                actorsName, users);
    }

    public ProcessDefinition deployAndEnableProcessWithActor(final BusinessArchive businessArchive, final List<String> actorsName, final List<User> users)
            throws BonitaException {
        final ProcessDefinition processDefinition = deployProcess(businessArchive);
        for (int i = 0; i < users.size(); i++) {
            getProcessAPI().addUserToActor(actorsName.get(i), processDefinition, users.get(i).getId());
        }
        enableProcess(processDefinition);
        return processDefinition;
    }

    public ProcessDefinition deployAndEnableProcessWithActor(final DesignProcessDefinition designProcessDefinition, final Map<String, List<User>> actorUsers)
            throws BonitaException {
        return deployAndEnableProcessWithActor(new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done(),
                actorUsers);
    }

    public ProcessDefinition deployAndEnableProcessWithActor(final BusinessArchive businessArchive, final Map<String, List<User>> actorUsers)
            throws BonitaException {
        final ProcessDefinition processDefinition = deployProcess(businessArchive);
        for (final Entry<String, List<User>> actorUser : actorUsers.entrySet()) {
            final String actorName = actorUser.getKey();
            final List<User> users = actorUser.getValue();
            for (final User user : users) {
                getProcessAPI().addUserToActor(actorName, processDefinition, user.getId());
            }
        }

        enableProcess(processDefinition);
        return processDefinition;
    }

    public ProcessDefinition deployProcess(final BusinessArchive businessArchive) throws BonitaException {
        return getProcessAPI().deploy(businessArchive);
    }

    private void enableProcess(final ProcessDefinition processDefinition) throws ProcessDefinitionNotFoundException, ProcessEnablementException {
        try {
            getProcessAPI().enableProcess(processDefinition.getId());
        } catch (final ProcessEnablementException e) {
            final List<Problem> problems = getProcessAPI().getProcessResolutionProblems(processDefinition.getId());
            throw new ProcessEnablementException("not resolved: " + problems);
        }
    }

    public ProcessDefinition deployAndEnableProcessWithActor(final BusinessArchive businessArchive, final String actorName, final User user)
            throws BonitaException {
        return deployAndEnableProcessWithActor(businessArchive, Collections.singletonList(actorName), Collections.singletonList(user));
    }

    public ProcessDefinition deployAndEnableProcessWithActorAndParameters(final DesignProcessDefinition designProcessDefinition, final List<String> actorsName,
            final List<User> users, final Map<String, String> parameters) throws BonitaException {
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        final BusinessArchive businessArchive = businessArchiveBuilder.setParameters(parameters).setProcessDefinition(designProcessDefinition).done();
        return deployAndEnableProcessWithActor(businessArchive, actorsName, users);
    }

    public ProcessDefinition deployAndEnableProcessWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final Group group)
            throws BonitaException {
        final ProcessDefinition processDefinition = deployProcess(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        getProcessAPI().addGroupToActor(actorName, group.getId(), processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    public ProcessDefinition deployAndEnableProcessWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName,
            final Group... groups)
            throws BonitaException {
        final ProcessDefinition processDefinition = deployProcess(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        for (final Group group : groups) {
            getProcessAPI().addGroupToActor(actorName, group.getId(), processDefinition);
        }
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    public ProcessDefinition deployAndEnableProcessWithActor(final DesignProcessDefinition designProcessDefinition,
            final String actorName,
            final List<Group> groups, final List<User> users)
            throws BonitaException {
        final ProcessDefinition processDefinition = deployProcess(
                aBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        for (final Group group : groups) {
            getProcessAPI().addGroupToActor(actorName, group.getId(), processDefinition);
        }
        for (User user : users) {
            getProcessAPI().addUserToActor(actorName, processDefinition, user.getId());
        }
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    public ProcessDefinition deployAndEnableProcessWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final Role role)
            throws BonitaException {
        final ProcessDefinition processDefinition = deployProcess(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        addMappingOfActorsForRole(actorName, role.getId(), processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    public ProcessDefinition deployAndEnableProcessWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final Role... roles)
            throws BonitaException {
        final ProcessDefinition processDefinition = deployProcess(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        for (final Role role : roles) {
            addMappingOfActorsForRole(actorName, role.getId(), processDefinition);
        }
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    public ProcessDefinition deployAndEnableProcessWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final Role role,
            final Group group) throws BonitaException {
        final ProcessDefinition processDefinition = deployProcess(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        addMappingOfActorsForRoleAndGroup(actorName, role.getId(), group.getId(), processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    public ProcessDefinition deployAndEnableProcessWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final long userId)
            throws BonitaException {
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = deployProcess(businessArchive);
        getProcessAPI().addUserToActor(actorName, processDefinition, userId);
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    public ProcessDefinition deployAndEnableProcessWithConnector(final ProcessDefinitionBuilder processDefinitionBuilder,
            final List<BarResource> connectorImplementations, final List<BarResource> generateConnectorDependencies) throws BonitaException {
        final BusinessArchiveBuilder businessArchiveBuilder = BuildTestUtil.buildBusinessArchiveWithConnectorAndUserFilter(processDefinitionBuilder,
                connectorImplementations, generateConnectorDependencies, Collections.<BarResource> emptyList());
        return deployAndEnableProcess(businessArchiveBuilder.done());
    }

    public ProcessDefinition deployAndEnableProcessWithConnector(final ProcessDefinitionBuilder processDefinitionBuilder, final String connectorImplName,
            final Class<? extends AbstractConnector> clazz, final String jarName) throws BonitaException, IOException {
        return deployAndEnableProcessWithConnector(processDefinitionBuilder,
                Collections.singletonList(BuildTestUtil.getContentAndBuildBarResource(connectorImplName, clazz)),
                Collections.singletonList(BuildTestUtil.generateJarAndBuildBarResource(clazz, jarName)));
    }

    public ProcessDefinition deployAndEnableProcessWithActorAndConnector(final ProcessDefinitionBuilder processDefinitionBuilder, final String actorName,
            final User user, final String name, final Class<? extends AbstractConnector> clazz, final String jarName) throws BonitaException, IOException {
        return deployAndEnableProcessWithActorAndConnectorAndParameter(processDefinitionBuilder, actorName, user,
                Collections.singletonList(BuildTestUtil.getContentAndBuildBarResource(name, clazz)),
                Collections.singletonList(BuildTestUtil.generateJarAndBuildBarResource(clazz, jarName)), null);
    }

    public ProcessDefinition deployAndEnableProcessWithActorAndTestConnectorEngineExecutionContext(final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actorName, final User user) throws BonitaException, IOException {
        return deployAndEnableProcessWithActorAndConnector(processDefinitionBuilder, actorName, user, "TestConnectorEngineExecutionContext.impl",
                TestConnectorEngineExecutionContext.class, "TestConnectorEngineExecutionContext.jar");
    }

    public ProcessDefinition deployAndEnableProcessWithActorAndConnectorAndParameter(final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actorName, final User user, final List<BarResource> connectorImplementations, final List<BarResource> generateConnectorDependencies,
            final Map<String, String> parameters) throws BonitaException {
        final BusinessArchiveBuilder businessArchiveBuilder = BuildTestUtil.buildBusinessArchiveWithConnectorAndUserFilter(processDefinitionBuilder,
                connectorImplementations, generateConnectorDependencies, Collections.<BarResource> emptyList());
        if (parameters != null) {
            businessArchiveBuilder.setParameters(parameters);
        }
        return deployAndEnableProcessWithActor(businessArchiveBuilder.done(), actorName, user);
    }

    public ProcessDefinition deployAndEnableProcessWithActorAndConnectorAndUserFilter(final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actorName, final User user, final List<BarResource> connectorImplementations, final List<BarResource> generateConnectorDependencies,
            final List<BarResource> userFilters) throws BonitaException {
        final BusinessArchiveBuilder businessArchiveBuilder = BuildTestUtil.buildBusinessArchiveWithConnectorAndUserFilter(processDefinitionBuilder,
                connectorImplementations, generateConnectorDependencies, userFilters);
        return deployAndEnableProcessWithActor(businessArchiveBuilder.done(), actorName, user);
    }

    public ProcessDefinition deployAndEnableProcessWithActorAndConnectorAndParameter(final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actorName, final User user, final Map<String, String> parameters, final String name, final Class<? extends AbstractConnector> clazz,
            final String jarName) throws BonitaException, IOException {
        return deployAndEnableProcessWithActorAndConnectorAndParameter(processDefinitionBuilder, actorName, user,
                Collections.singletonList(BuildTestUtil.getContentAndBuildBarResource(name, clazz)),
                Collections.singletonList(BuildTestUtil.generateJarAndBuildBarResource(clazz, jarName)), parameters);
    }

    public ProcessDefinition deployAndEnableProcessWithActorAndUserFilter(final ProcessDefinitionBuilder processDefinitionBuilder, final String actorName,
            final User user, final List<BarResource> generateFilterDependencies, final List<BarResource> userFilters)
            throws BonitaException {
        return deployAndEnableProcessWithActorAndConnectorAndUserFilter(processDefinitionBuilder, actorName, user, Collections.<BarResource> emptyList(),
                generateFilterDependencies, userFilters);
    }

    public void disableAndDeleteProcess(final ProcessDefinition processDefinition) throws BonitaException {
        if (processDefinition != null) {
            disableAndDeleteProcess(processDefinition.getId());
        }
    }

    public void disableAndDeleteProcess(final long processDefinitionId) throws BonitaException {
        getProcessAPI().disableProcess(processDefinitionId);

        // Delete all process instances
        long nbDeletedProcessInstances;
        do {
            nbDeletedProcessInstances = getProcessAPI().deleteProcessInstances(processDefinitionId, 0, 100);
        } while (nbDeletedProcessInstances > 0);

        // Delete all archived process instances
        long nbDeletedArchivedProcessInstances;
        do {
            nbDeletedArchivedProcessInstances = getProcessAPI().deleteArchivedProcessInstances(processDefinitionId, 0, 100);
        } while (nbDeletedArchivedProcessInstances > 0);

        getProcessAPI().deleteProcessDefinition(processDefinitionId);
    }

    public void disableAndDeleteProcess(final ProcessDefinition... processDefinitions) throws BonitaException {
        disableAndDeleteProcess(Arrays.asList(processDefinitions));
    }

    public void disableAndDeleteProcess(final List<ProcessDefinition> processDefinitions) throws BonitaException {
        if (processDefinitions != null) {
            for (final ProcessDefinition processDefinition : processDefinitions) {
                disableAndDeleteProcess(processDefinition);
            }
        }
    }

    public void disableAndDeleteProcessById(final List<Long> processDefinitionIds) throws BonitaException {
        if (processDefinitionIds != null) {
            for (final Long id : processDefinitionIds) {
                disableAndDeleteProcess(id);
            }
        }
    }

    public void deleteProcessInstanceAndArchived(final long processDefinitionId) throws BonitaException {
        while (getProcessAPI().deleteArchivedProcessInstances(processDefinitionId, 0, 500) != 0);
        while (getProcessAPI().deleteProcessInstances(processDefinitionId, 0, 500) != 0);
    }

    public void deleteProcessInstanceAndArchived(final ProcessDefinition... processDefinitions) throws BonitaException {
        deleteProcessInstanceAndArchived(Arrays.asList(processDefinitions));
    }

    public void deleteProcessInstanceAndArchived(final List<ProcessDefinition> processDefinitions) throws BonitaException {
        if (processDefinitions != null) {
            for (final ProcessDefinition processDefinition : processDefinitions) {
                deleteProcessInstanceAndArchived(processDefinition.getId());
            }
        }
    }

    public APISession getSession() {
        return getApiClient().getSession();
    }

    protected APIClient getApiClient() {
        return apiClient;
    }

    public static boolean containsState(final List<ArchivedProcessInstance> instances, final TestStates state) {
        for (final ArchivedProcessInstance pi : instances) {
            if (state.getStateName().equals(pi.getState())) {
                return true;
            }
        }
        return false;
    }

    public Group createGroup(final String groupName) throws CreationException {
        return createGroup(groupName, null);
    }

    public Group createGroup(final String groupName, final String parentGroupPath) throws CreationException {
        return getIdentityAPI().createGroup(groupName, parentGroupPath);
    }

    public Group createGroup(final String name, final String displayName, final String description) throws CreationException {
        final GroupCreator groupCreator = new GroupCreator(name);
        groupCreator.setDisplayName(displayName).setDescription(description);
        return getIdentityAPI().createGroup(groupCreator);
    }

    public Role createRole(final String roleName) throws BonitaException {
        return getIdentityAPI().createRole(roleName);
    }

    public void assignAndExecuteStep(final ActivityInstance activityInstance, final long userId) throws BonitaException {
        assignAndExecuteStep(activityInstance.getId(), userId);
    }

    public void assignAndExecuteStep(final ActivityInstance activityInstance, final User user) throws BonitaException {
        assignAndExecuteStep(activityInstance.getId(), user.getId());
    }

    public void assignAndExecuteStep(final long activityInstanceId, final User user) throws BonitaException {
        assignAndExecuteStep(activityInstanceId, user.getId());
    }

    public void assignAndExecuteStep(final long activityInstanceId, final long userId) throws BonitaException {
        getProcessAPI().assignUserTask(activityInstanceId, userId);
        getProcessAPI().executeFlowNode(activityInstanceId);
    }

    public HumanTaskInstance waitForUserTaskAndExecuteAndGetIt(final String taskName, final User user) throws Exception {
        final HumanTaskInstance humanTaskInstance = waitForUserTaskAndGetIt(taskName);
        assignAndExecuteStep(humanTaskInstance, user);
        return humanTaskInstance;
    }

    public long waitForUserTaskAndExecuteIt(final String taskName, final User user) throws Exception {
        final long humanTaskInstanceId = waitForUserTask(taskName);
        assignAndExecuteStep(humanTaskInstanceId, user);
        return humanTaskInstanceId;
    }

    public HumanTaskInstance waitForUserTaskAndExecuteAndGetIt(final ProcessInstance processInstance, final String taskName, final User user) throws Exception {
        final HumanTaskInstance humanTaskInstance = waitForUserTaskAndGetIt(processInstance.getId(), taskName, DEFAULT_TIMEOUT);
        assignAndExecuteStep(humanTaskInstance, user);
        return humanTaskInstance;
    }

    public long waitForUserTaskAndExecuteIt(final ProcessInstance processInstance, final String taskName, final User user) throws Exception {
        final long humanTaskInstanceId = waitForUserTask(processInstance.getId(), taskName, DEFAULT_TIMEOUT);
        assignAndExecuteStep(humanTaskInstanceId, user);
        return humanTaskInstanceId;
    }

    public HumanTaskInstance waitForUserTaskAndAssignIt(final String taskName, final User user) throws Exception {
        final HumanTaskInstance humanTaskInstance = waitForUserTaskAndGetIt(taskName);
        getProcessAPI().assignUserTask(humanTaskInstance.getId(), user.getId());
        return humanTaskInstance;
    }

    public HumanTaskInstance waitForUserTaskAndAssignIt(final ProcessInstance processInstance, final String taskName, final User user) throws Exception {
        final HumanTaskInstance humanTaskInstance = waitForUserTaskAndGetIt(processInstance.getId(), taskName);
        getProcessAPI().assignUserTask(humanTaskInstance.getId(), user.getId());
        return humanTaskInstance;
    }

    public HumanTaskInstance waitForUserTaskAndGetIt(final ProcessInstance processInstance, final String taskName) throws Exception {
        return waitForUserTaskAndGetIt(processInstance.getId(), taskName, DEFAULT_TIMEOUT);
    }

    public HumanTaskInstance waitForUserTaskAndGetIt(final long processInstanceId, final String taskName) throws Exception {
        return waitForUserTaskAndGetIt(processInstanceId, taskName, DEFAULT_TIMEOUT);
    }

    public HumanTaskInstance waitForUserTaskAndGetIt(final String taskName) throws Exception {
        return waitForUserTaskAndGetIt(-1, taskName, DEFAULT_TIMEOUT);
    }

    public long waitForUserTask(final long processInstanceId, final String taskName) throws Exception {
        return waitForUserTask(processInstanceId, taskName, DEFAULT_TIMEOUT);
    }

    public long waitForUserTask(final String taskName) throws Exception {
        return waitForUserTask(-1, taskName, DEFAULT_TIMEOUT);
    }

    private HumanTaskInstance waitForUserTaskAndGetIt(final long processInstanceId, final String taskName, final int timeout) throws Exception {
        final long activityInstanceId = waitForUserTask(processInstanceId, taskName, timeout);
        final HumanTaskInstance getHumanTaskInstance = getHumanTaskInstance(activityInstanceId);
        assertNotNull(getHumanTaskInstance);
        return getHumanTaskInstance;
    }

    private long waitForUserTask(final long processInstanceId, final String taskName, final int timeout) throws CommandNotFoundException,
            CommandParameterizationException, CommandExecutionException, TimeoutException {
        final Map<String, Serializable> readyTaskEvent;
        if (processInstanceId > 0) {
            readyTaskEvent = ClientEventUtil.getReadyFlowNodeEvent(processInstanceId, taskName);
        } else {
            readyTaskEvent = ClientEventUtil.getReadyFlowNodeEvent(taskName);
        }
        return ClientEventUtil.executeWaitServerCommand(getCommandAPI(), readyTaskEvent, timeout);
    }

    private HumanTaskInstance getHumanTaskInstance(final Long id) throws ActivityInstanceNotFoundException, RetrieveException {
        if (id != null) {
            return getProcessAPI().getHumanTaskInstance(id);
        }
        throw new RuntimeException("no id returned for human task ");
    }

    private ActivityInstance getActivityInstance(final Long id) throws ActivityInstanceNotFoundException, RetrieveException {
        if (id != null) {
            return getProcessAPI().getActivityInstance(id);
        }
        throw new RuntimeException("no id returned for activity instance ");
    }

    private FlowNodeInstance getFlowNodeInstance(final Long id) throws RuntimeException {
        try {
            return getProcessAPI().getFlowNodeInstance(id);
        } catch (final FlowNodeInstanceNotFoundException e) {
            throw new RuntimeException("no id returned for flow node instance ");
        }
    }

    public long waitForUserTask(final ProcessInstance processInstance, final String taskName) throws Exception {
        return waitForUserTask(processInstance.getId(), taskName, DEFAULT_TIMEOUT);
    }

    public void deleteSupervisor(final long id) throws BonitaException {
        getProcessAPI().deleteSupervisor(id);
    }

    public void deleteSupervisor(final ProcessSupervisor supervisor) throws BonitaException {
        getProcessAPI().deleteSupervisor(supervisor.getSupervisorId());
    }

    @Deprecated
    public List<HumanTaskInstance> waitForPendingTasks(final long userId, final int nbPendingTasks) throws Exception {
        final WaitForPendingTasks waitUntil = new WaitForPendingTasks(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, nbPendingTasks, userId, getProcessAPI());
        assertTrue("no pending user task instances are found", waitUntil.waitUntil());
        return waitUntil.getResults();
    }

    @Deprecated
    public List<HumanTaskInstance> waitForPendingTasks(final User user, final int nbPendingTasks) throws Exception {
        return waitForPendingTasks(user.getId(), nbPendingTasks);
    }

    public StartProcessUntilStep startProcessAndWaitForTask(final long processDefinitionId, final String taskName) throws Exception {
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinitionId);
        final ActivityInstance task = waitForUserTaskAndGetIt(processInstance.getId(), taskName);
        return new StartProcessUntilStep(processInstance, task);
    }

    @Deprecated
    public ArchivedActivityInstance waitForArchivedActivity(final long activityId, final TestStates state) throws Exception {
        final WaitForArchivedActivity waitForArchivedActivity = new WaitForArchivedActivity(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, activityId, state,
                getProcessAPI());
        assertTrue(waitForArchivedActivity.waitUntil());
        final ArchivedActivityInstance archivedActivityInstance = waitForArchivedActivity.getArchivedActivityInstance();
        assertNotNull(archivedActivityInstance);
        return archivedActivityInstance;
    }

    @Deprecated
    public void waitForCompletedArchivedStep(final String value, final long id, final String displayName, final String displayDescription) throws Exception {
        final WaitForCompletedArchivedStep waitUntil = new WaitForCompletedArchivedStep(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, value, id, getProcessAPI());
        assertTrue(waitUntil.waitUntil());
        final ArchivedHumanTaskInstance saUserTaskInstance = waitUntil.getArchivedTask();
        assertEquals(displayName, saUserTaskInstance.getDisplayName());
        assertEquals(displayDescription, saUserTaskInstance.getDisplayDescription());
    }

    public void waitForProcessToFinish(final ProcessInstance processInstance) throws Exception {
        waitForProcessToFinish(processInstance.getId());
    }

    public void waitForProcessToFinish(final long processInstanceId) throws Exception {
        waitForProcessToFinish(processInstanceId, DEFAULT_TIMEOUT);
    }

    private void waitForProcessToFinish(final long processInstanceId, final int timeout) throws Exception {
        ClientEventUtil.executeWaitServerCommand(getCommandAPI(), ClientEventUtil.getProcessInstanceFinishedEvent(processInstanceId), timeout);
    }

    public void waitForProcessToBeInState(final ProcessInstance processInstance, final ProcessInstanceState state) throws Exception {
        waitForProcessToBeInState(processInstance.getId(), state);
    }

    public void waitForProcessToBeInState(final long processInstanceId, final ProcessInstanceState state) throws Exception {
        ClientEventUtil.executeWaitServerCommand(getCommandAPI(), ClientEventUtil.getProcessInstanceInState(processInstanceId, state.getId()),
                DEFAULT_TIMEOUT);
    }

    public void waitForInitializingProcess() throws Exception {
        ClientEventUtil.executeWaitServerCommand(getCommandAPI(), ClientEventUtil.getProcessInstanceInState(ProcessInstanceState.INITIALIZING.getId()),
                DEFAULT_TIMEOUT);
    }

    private Long waitForFlowNode(final long processInstanceId, final TestStates state, final String flowNodeName, final boolean useRootProcessInstance,
            final int timeout) throws Exception {
        Map<String, Serializable> params;
        if (useRootProcessInstance) {
            params = ClientEventUtil.getFlowNodeInState(processInstanceId, state.getStateName(), flowNodeName);
        } else {
            params = ClientEventUtil.getFlowNodeInStateWithParentId(processInstanceId, state.getStateName(), flowNodeName);
        }
        return ClientEventUtil.executeWaitServerCommand(getCommandAPI(), params, timeout);
    }

    public FlowNodeInstance waitForFlowNodeInReadyState(final ProcessInstance processInstance, final String flowNodeName, final boolean useRootProcessInstance)
            throws Exception {
        final Long flowNodeInstanceId = waitForFlowNode(processInstance.getId(), TestStates.READY, flowNodeName, useRootProcessInstance,
                DEFAULT_TIMEOUT);
        return getFlowNode(flowNodeInstanceId);
    }

    public FlowNodeInstance waitForFlowNodeInExecutingState(final ProcessInstance processInstance, final String flowNodeName,
            final boolean useRootProcessInstance) throws Exception {
        final Long activityId = waitForFlowNodeInState(processInstance, flowNodeName, TestStates.EXECUTING, useRootProcessInstance);
        return getFlowNodeInstance(activityId);
    }

    public ArchivedActivityInstance waitForActivityInCompletedState(final ProcessInstance processInstance, final String flowNodeName,
            final boolean useRootProcessInstance) throws Exception {
        final Long activityId = waitForFlowNodeInState(processInstance, flowNodeName, TestStates.NORMAL_FINAL, useRootProcessInstance);
        // we must wait for the activity to be completely archived:
        final WaitForArchivedActivity waitForArchivedActivity = new WaitForArchivedActivity(30, 8000, activityId, TestStates.NORMAL_FINAL, getProcessAPI());
        assertTrue(waitForArchivedActivity.waitUntil());
        return waitForArchivedActivity.getArchivedActivityInstance();
    }

    public FlowNodeInstance waitForFlowNodeInWaitingState(final ProcessInstance processInstance, final String flowNodeName,
            final boolean useRootProcessInstance) throws Exception {
        final Long activityId = waitForFlowNodeInState(processInstance, flowNodeName, TestStates.WAITING, useRootProcessInstance);
        return getFlowNodeInstance(activityId);
    }

    public Long waitForFlowNodeInState(final ProcessInstance processInstance, final String flowNodeName, final TestStates state,
            final boolean useRootProcessInstance) throws Exception {
        return waitForFlowNode(processInstance.getId(), state, flowNodeName, useRootProcessInstance, DEFAULT_TIMEOUT);
    }

    public ActivityInstance waitForTaskInState(final ProcessInstance processInstance, final String flowNodeName, final TestStates state) throws Exception {
        final Long activityId = waitForFlowNode(processInstance.getId(), state, flowNodeName, true, DEFAULT_TIMEOUT);
        return getActivityInstance(activityId);
    }

    private FlowNodeInstance getFlowNode(final Long flowNodeInstanceId) throws FlowNodeInstanceNotFoundException {
        final FlowNodeInstance flowNodeInstance = getProcessAPI().getFlowNodeInstance(flowNodeInstanceId);
        assertNotNull(flowNodeInstance);
        return flowNodeInstance;
    }

    public ActivityInstance waitForTaskToFail(final ProcessInstance processInstance) throws Exception {
        final Long activityId = ClientEventUtil.executeWaitServerCommand(getCommandAPI(),
                ClientEventUtil.getFlowNodeInState(processInstance.getId(), TestStates.FAILED.getStateName()), DEFAULT_TIMEOUT);
        return getActivityInstance(activityId);
    }

    public FlowNodeInstance waitForFlowNodeInFailedState(final ProcessInstance processInstance) throws Exception {
        final Long activityId = ClientEventUtil.executeWaitServerCommand(getCommandAPI(),
                ClientEventUtil.getFlowNodeInState(processInstance.getId(), TestStates.FAILED.getStateName()), DEFAULT_TIMEOUT);
        return getFlowNodeInstance(activityId);
    }

    public void waitForFlowNodeInFailedState(final String flowNodeInstanceName) throws Exception {
        final long failedTaskId = ClientEventUtil.executeWaitServerCommand(getCommandAPI(),
                ClientEventUtil.getFlowNodeInState(TestStates.FAILED.getStateName(), flowNodeInstanceName), DEFAULT_TIMEOUT);
        assertNotNull(failedTaskId);
    }

    public FlowNodeInstance waitForFlowNodeInFailedState(final ProcessInstance processInstance, final String flowNodeName) throws Exception {
        final Long flowNodeInstanceId = waitForFlowNodeInState(processInstance, flowNodeName, TestStates.FAILED, true);
        return getFlowNodeInstance(flowNodeInstanceId);
    }

    public GatewayInstance waitForGateway(final ProcessInstance processInstance, final String name) throws Exception {
        final Map<String, Serializable> readyTaskEvent = ClientEventUtil.getFlowNode(processInstance.getId(), name);
        final Long activityInstanceId = ClientEventUtil.executeWaitServerCommand(getCommandAPI(), readyTaskEvent, DEFAULT_REPEAT_EACH * DEFAULT_TIMEOUT);
        final FlowNodeInstance flowNodeInstance = getProcessAPI().getFlowNodeInstance(activityInstanceId);
        assertNotNull(flowNodeInstance);
        if (flowNodeInstance instanceof GatewayInstance) {
            return (GatewayInstance) flowNodeInstance;
        }
        return null;
    }

    @Deprecated
    public WaitForFinalArchivedActivity waitForFinalArchivedActivity(final String activityName, final ProcessInstance processInstance) throws Exception {
        final WaitForFinalArchivedActivity waitForFinalArchivedActivity = new WaitForFinalArchivedActivity(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, activityName,
                processInstance.getId(), getProcessAPI());
        assertTrue(activityName + " should be finished and archived", waitForFinalArchivedActivity.waitUntil());
        return waitForFinalArchivedActivity;
    }

    @Deprecated
    public WaitForEvent waitForEventInWaitingState(final ProcessInstance processInstance, final String eventName) throws Exception {
        return waitForEvent(processInstance, eventName, TestStates.WAITING);
    }

    @Deprecated
    public WaitForEvent waitForEvent(final ProcessInstance processInstance, final String eventName, final TestStates state) throws Exception {
        return waitForEvent(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, processInstance.getId(), eventName, state);
    }

    @Deprecated
    private WaitForEvent waitForEvent(final int repeatEach, final int timeout, final long processInstanceId, final String eventName, final TestStates state)
            throws Exception {
        final WaitForEvent waitForEvent = new WaitForEvent(repeatEach, timeout, eventName, processInstanceId, state, getProcessAPI());
        assertTrue("Expected 1 activities in " + state + " state", waitForEvent.waitUntil());
        return waitForEvent;
    }

    @Deprecated
    public void checkNbOfOpenTasks(final ProcessInstance processInstance, final String message, final int expectedNbOfOpenActivities) throws Exception {
        final CheckNbOfOpenActivities checkNbOfOpenActivities = new CheckNbOfOpenActivities(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, false, processInstance,
                expectedNbOfOpenActivities, getProcessAPI());
        checkNbOfOpenActivities.waitUntil();
        assertEquals(message, expectedNbOfOpenActivities, checkNbOfOpenActivities.getNumberOfOpenActivities());
    }

    @Deprecated
    public CheckNbPendingTaskOf checkNbPendingTaskOf(final int nbOfPendingTasks, final User user) throws Exception {
        return checkNbPendingTaskOf(false, nbOfPendingTasks, user);
    }

    @Deprecated
    public CheckNbPendingTaskOf checkNbPendingTaskOf(final boolean throwExceptions, final int nbOfPendingTasks, final User user) throws Exception {
        return checkNbPendingTaskOf(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, throwExceptions, nbOfPendingTasks, user);
    }

    @Deprecated
    private CheckNbPendingTaskOf checkNbPendingTaskOf(final int repeatEach, final int timeout, final boolean throwExceptions, final int nbOfPendingTasks,
            final User user) throws Exception {
        final CheckNbPendingTaskOf checkNbPendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), repeatEach, timeout, throwExceptions, nbOfPendingTasks,
                user);
        assertTrue("There isn't " + nbOfPendingTasks + " pending task", checkNbPendingTaskOf.waitUntil());
        return checkNbPendingTaskOf;
    }

    @Deprecated
    public void checkNbOfOpenActivities(final ProcessInstance processInstance, final int nbActivities) throws Exception {
        checkNbOfOpenActivities(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, processInstance, nbActivities);
    }

    @Deprecated
    private void checkNbOfOpenActivities(final int repeatEach, final int timeout, final ProcessInstance processInstance, final int nbActivities)
            throws Exception {
        assertTrue("Expected " + nbActivities + " OPEN activities for process instance", new CheckNbOfOpenActivities(repeatEach, timeout, true,
                processInstance, nbActivities, getProcessAPI()).waitUntil());
    }

    @Deprecated
    public void checkProcessInstanceIsArchived(final ProcessInstance processInstance) throws Exception {
        checkProcessInstanceIsArchived(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, processInstance);
    }

    @Deprecated
    private void checkProcessInstanceIsArchived(final int repeatEach, final int timeout, final ProcessInstance processInstance) throws Exception {
        assertTrue(new CheckProcessInstanceIsArchived(repeatEach, timeout, processInstance.getId(), getProcessAPI()).waitUntil());
    }

    @Deprecated
    public void checkNbOfArchivedActivityInstances(final ProcessInstance processInstance, final int expected) throws Exception {
        checkNbOfArchivedActivityInstances(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, processInstance, expected);
    }

    @Deprecated
    private void checkNbOfArchivedActivityInstances(final int repeatEach, final int timeout, final ProcessInstance processInstance, final int expected)
            throws Exception {
        assertTrue(new CheckNbOfArchivedActivityInstances(repeatEach, timeout, processInstance, expected, getProcessAPI()).waitUntil());
    }

    @Deprecated
    public void checkNbOfArchivedActivities(final ProcessInstance processInstance, final int nbAbortedActivities) throws Exception {
        checkNbOfArchivedActivities(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, processInstance, nbAbortedActivities);
    }

    @Deprecated
    private void checkNbOfArchivedActivities(final int repeatEach, final int timeout, final ProcessInstance processInstance, final int nbAbortedActivities)
            throws Exception {
        final CheckNbOfArchivedActivities checkNbOfActivities = new CheckNbOfArchivedActivities(getProcessAPI(), repeatEach, timeout, true, processInstance,
                nbAbortedActivities, TestStates.ABORTED);
        final boolean waitUntil = checkNbOfActivities.waitUntil();
        assertTrue("Expected " + nbAbortedActivities + " in the aboted state. But was " + checkNbOfActivities.getResult().size(), waitUntil);
    }

    @Deprecated
    public CheckNbOfProcessInstances checkNbOfProcessInstances(final int nbOfProcInst) throws Exception {
        final CheckNbOfProcessInstances checkNbOfProcessInstances = new CheckNbOfProcessInstances(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, nbOfProcInst,
                getProcessAPI());
        assertTrue(checkNbOfProcessInstances.waitUntil());
        return checkNbOfProcessInstances;
    }

    @Deprecated
    public CheckNbOfProcessInstances checkNbOfProcessInstances(final int nbOfProcInst, final ProcessInstanceCriterion orderBy) throws Exception {
        return checkNbOfProcessInstances(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, nbOfProcInst, orderBy);
    }

    @Deprecated
    private CheckNbOfProcessInstances checkNbOfProcessInstances(final int repeatEach, final int timeout, final int nbOfProcInst,
            final ProcessInstanceCriterion orderBy) throws Exception {
        final CheckNbOfProcessInstances checkNbOfProcessInstances = new CheckNbOfProcessInstances(repeatEach, timeout, nbOfProcInst, orderBy, getProcessAPI());
        assertTrue(checkNbOfProcessInstances.waitUntil());
        return checkNbOfProcessInstances;
    }

    public void checkFlowNodeWasntExecuted(final long processInstancedId, final String flowNodeName) {
        final List<ArchivedActivityInstance> archivedActivityInstances = getProcessAPI().getArchivedActivityInstances(processInstancedId, 0, 200,
                ActivityInstanceCriterion.DEFAULT);
        for (final ArchivedActivityInstance archivedActivityInstance : archivedActivityInstances) {
            assertFalse(flowNodeName.equals(archivedActivityInstance.getName()));
        }
    }

    public void checkWasntExecuted(final ProcessInstance parentProcessInstance, final String flowNodeName) throws InvalidSessionException, SearchException {
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 20);
        searchOptionsBuilder.filter(ArchivedFlowNodeInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID, parentProcessInstance.getId());
        searchOptionsBuilder.filter(ArchivedFlowNodeInstanceSearchDescriptor.NAME, flowNodeName);
        final SearchResult<ArchivedFlowNodeInstance> searchArchivedActivities = getProcessAPI().searchArchivedFlowNodeInstances(searchOptionsBuilder.done());
        assertTrue(searchArchivedActivities.getCount() == 0);
    }

    public void skipTask(final long activityId) throws UpdateException {
        getProcessAPI().setActivityStateByName(activityId, ActivityStates.SKIPPED_STATE);
    }

    public void skipTasks(final ProcessInstance processInstance) throws UpdateException {
        final List<ActivityInstance> activityInstances = getProcessAPI().getActivities(processInstance.getId(), 0, 10);
        for (final ActivityInstance activityInstance : activityInstances) {
            final long activityInstanceId = activityInstance.getId();
            skipTask(activityInstanceId);
        }
    }

    public void updateActivityInstanceVariablesWithOperations(final String updatedValue, final long activityInstanceId, final String dataName,
            final boolean isTransient)
            throws InvalidExpressionException, UpdateException {
        final Operation stringOperation = BuildTestUtil.buildStringOperation(dataName, updatedValue, isTransient);
        final List<Operation> operations = new ArrayList<>();
        operations.add(stringOperation);
        getProcessAPI().updateActivityInstanceVariables(operations, activityInstanceId, null);
    }

    protected void cleanCategories() throws DeletionException {
        final long numberOfCategories = getProcessAPI().getNumberOfCategories();
        if (numberOfCategories > 0) {
            final List<Category> categories = getProcessAPI().getCategories(0, 5000, CategoryCriterion.NAME_ASC);
            for (final Category category : categories) {
                getProcessAPI().deleteCategory(category.getId());
            }
        }
    }

    protected void cleanProcessDefinitions() throws BonitaException {
        final List<ProcessDeploymentInfo> processes = getProcessAPI().getProcessDeploymentInfos(0, 200, ProcessDeploymentInfoCriterion.DEFAULT);
        if (processes.size() > 0) {
            for (final ProcessDeploymentInfo processDeploymentInfo : processes) {
                if (ActivationState.ENABLED.equals(processDeploymentInfo.getActivationState())) {
                    getProcessAPI().disableProcess(processDeploymentInfo.getProcessId());
                }
                getProcessAPI().deleteProcessDefinition(processDeploymentInfo.getProcessId());
            }
        }
    }

    protected void cleanSupervisors() throws SearchException, DeletionException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 200);
        builder.sort(ProcessSupervisorSearchDescriptor.ID, Order.ASC);
        final List<ProcessSupervisor> supervisors = getProcessAPI().searchProcessSupervisors(builder.done()).getResult();
        if (supervisors.size() > 0) {
            for (final ProcessSupervisor supervisor : supervisors) {
                getProcessAPI().deleteSupervisor(supervisor.getSupervisorId());
            }
        }
    }

    protected void cleanProcessInstances() throws DeletionException {
        final List<ProcessInstance> processInstances = getProcessAPI().getProcessInstances(0, 1000, ProcessInstanceCriterion.DEFAULT);
        if (!processInstances.isEmpty()) {
            for (final ProcessInstance processInstance : processInstances) {
                getProcessAPI().deleteProcessInstance(processInstance.getId());
            }
        }
    }

    protected void cleanArchiveProcessInstances() throws DeletionException {
        final List<ArchivedProcessInstance> archivedProcessInstances = getProcessAPI().getArchivedProcessInstances(0, 1000, ProcessInstanceCriterion.DEFAULT);
        if (!archivedProcessInstances.isEmpty()) {
            for (final ArchivedProcessInstance archivedProcessInstance : archivedProcessInstances) {
                getProcessAPI().deleteArchivedProcessInstancesInAllStates(archivedProcessInstance.getSourceObjectId());
            }
        }
    }

    protected void cleanGroups() throws DeletionException {
        final long numberOfGroups = getIdentityAPI().getNumberOfGroups();
        if (numberOfGroups > 0) {
            final List<Group> groups = getIdentityAPI().getGroups(0, Long.valueOf(numberOfGroups).intValue(), GroupCriterion.NAME_ASC);
            for (final Group group : groups) {
                getIdentityAPI().deleteGroup(group.getId());
            }
        }
    }

    protected void cleanRoles() throws DeletionException {
        final long numberOfRoles = getIdentityAPI().getNumberOfRoles();
        if (numberOfRoles > 0) {
            final List<Role> roles = getIdentityAPI().getRoles(0, Long.valueOf(numberOfRoles).intValue(), RoleCriterion.NAME_ASC);
            for (final Role role : roles) {
                getIdentityAPI().deleteRole(role.getId());
            }
        }
    }

    protected void cleanUsers() throws DeletionException {
        final long numberOfUsers = getIdentityAPI().getNumberOfUsers();
        if (numberOfUsers > 0) {
            final List<User> users = getIdentityAPI().getUsers(0, Long.valueOf(numberOfUsers).intValue(), UserCriterion.USER_NAME_ASC);
            for (final User user : users) {
                getIdentityAPI().deleteUser(user.getId());
            }
        }
    }

    protected void cleanCommands() throws SearchException, CommandNotFoundException, DeletionException {
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 1000);
        searchOptionsBuilder.filter(CommandSearchDescriptor.SYSTEM, false);
        searchOptionsBuilder.differentFrom(CommandSearchDescriptor.NAME, ClientEventUtil.ADD_HANDLER_COMMAND);
        searchOptionsBuilder.differentFrom(CommandSearchDescriptor.NAME, ClientEventUtil.WAIT_SERVER_COMMAND);
        final SearchResult<CommandDescriptor> searchCommands = getCommandAPI().searchCommands(searchOptionsBuilder.done());
        final List<CommandDescriptor> commands = searchCommands.getResult();
        if (searchCommands.getCount() > 0) {
            for (final CommandDescriptor command : commands) {
                getCommandAPI().unregister(command.getName());
            }
        }
    }

    public ProcessAPI getProcessAPI() {
        return getApiClient().getProcessAPI();
    }

    public IdentityAPI getIdentityAPI() {
        return getApiClient().getIdentityAPI();
    }

    public CommandAPI getCommandAPI() {
        return getApiClient().getCommandAPI();
    }

    public ProfileAPI getProfileAPI() {
        return getApiClient().getProfileAPI();
    }

    public ThemeAPI getThemeAPI() {
        return getApiClient().getThemeAPI();
    }

    public PermissionAPI getPermissionAPI() {
        return getApiClient().getPermissionAPI();
    }

    public PageAPI getPageAPI() {
        return getApiClient().getCustomPageAPI();
    }

    public ApplicationAPI getApplicationAPI() {
        return getApiClient().getLivingApplicationAPI();
    }

    public TenantAdministrationAPI getTenantAdministrationAPI() {
        return getApiClient().getTenantAdministrationAPI();
    }

    public void deleteSupervisors(final List<ProcessSupervisor> processSupervisors) throws BonitaException {
        if (processSupervisors != null) {
            for (final ProcessSupervisor processSupervisor : processSupervisors) {
                deleteSupervisor(processSupervisor.getSupervisorId());
            }
        }
    }

    public void buildAndAttachDocument(final ProcessInstance processInstance) throws BonitaException {
        final String documentName = String.valueOf(System.currentTimeMillis());
        final Document doc = BuildTestUtil.buildDocument(documentName);
        buildAndAttachDocument(processInstance, documentName, doc.getContentFileName());
    }

    public void buildAndAttachDocument(final ProcessInstance processInstance, final String documentName, final String fileName) throws BonitaException {
        final Document doc = BuildTestUtil.buildDocument(documentName);
        getProcessAPI().attachDocument(processInstance.getId(), documentName, fileName, doc.getContentMimeType(), documentName.getBytes());
    }

    public String getAttachmentDocumentName(final ProcessInstance processInstance) throws BonitaException {
        final Document attachment = getAttachmentWithoutItsContent(processInstance);
        return attachment.getName();
    }

    public Document getAttachmentWithoutItsContent(final ProcessInstance processInstance) throws BonitaException {
        final List<Document> attachments = getProcessAPI().getLastVersionOfDocuments(processInstance.getId(), 0, 1, DocumentCriterion.DEFAULT);
        assertTrue("No attachments found!", attachments != null && attachments.size() == 1);
        return attachments.get(0);
    }

    public ProcessInstance deployAndEnableProcessWithActorAndStartIt(final BusinessArchive businessArchive, final User user) throws BonitaException {
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(businessArchive, BuildTestUtil.ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        assertTrue(processInstance != null);
        return processInstance;
    }

    public ArchivedDataInstance getArchivedDataInstance(final List<ArchivedDataInstance> archivedDataInstances, final String dataName) {
        ArchivedDataInstance archivedDataInstance = null;
        final Iterator<ArchivedDataInstance> iterator = archivedDataInstances.iterator();
        while (archivedDataInstance == null || iterator.hasNext()) {
            final ArchivedDataInstance next = iterator.next();
            if (next.getName().equals(dataName)) {
                archivedDataInstance = next;
            }
        }
        assertNotNull(archivedDataInstance);
        return archivedDataInstance;
    }

    public List<ProcessDefinition> createNbProcessDefinitionWithHumanAndAutomaticAndDeployWithActor(final int nbProcess, final User user,
            final List<String> stepNames, final List<Boolean> isHuman) throws BonitaException {
        final List<ProcessDefinition> processDefinitions = new ArrayList<>();
        final List<DesignProcessDefinition> designProcessDefinitions = BuildTestUtil.buildNbProcessDefinitionWithHumanAndAutomatic(nbProcess, stepNames,
                isHuman);

        for (final DesignProcessDefinition designProcessDefinition : designProcessDefinitions) {
            processDefinitions.add(deployAndEnableProcessWithActor(designProcessDefinition, BuildTestUtil.ACTOR_NAME, user));
        }
        return processDefinitions;
    }

    protected void assertThatXmlHaveNoDifferences(final String xmlPrettyFormatExpected, final String xmlPrettyFormatExported) throws SAXException, IOException {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        final DetailedDiff diff = new DetailedDiff(XMLUnit.compareXML(xmlPrettyFormatExported, xmlPrettyFormatExpected));
        final List<?> allDifferences = diff.getAllDifferences();
        assertThat(allDifferences).as("should have no differences between:\n%s\n and:\n%s\n", xmlPrettyFormatExpected, xmlPrettyFormatExported).isEmpty();
    }

    public BarResource getBarResource(final String path, final String name, Class<?> clazz) throws IOException {
        final InputStream stream = clazz.getResourceAsStream(path);
        assertThat(stream).isNotNull();
        try {
            final byte[] byteArray = IOUtils.toByteArray(stream);
            return new BarResource(name, byteArray);
        } finally {
            stream.close();
        }
    }

    protected byte[] createTestPageContent(final String pageName, final String displayName, final String description) throws Exception {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ZipOutputStream zos = new ZipOutputStream(baos);
            zos.putNextEntry(new ZipEntry("Index.groovy"));
            zos.write("return \"\";".getBytes());

            zos.putNextEntry(new ZipEntry("page.properties"));
            zos.write(("name=" + pageName + "\n" + "displayName=" + displayName + "\n" + "description=" + description + "\n").getBytes());

            zos.closeEntry();
            return baos.toByteArray();
        } catch (final IOException e) {
            throw new BonitaException(e);
        }
    }

    protected BusinessObjectModel buildBOM() {
        final SimpleField name = new SimpleField();
        name.setName("name");
        name.setType(FieldType.STRING);
        final SimpleField age = new SimpleField();
        age.setName("age");
        age.setType(FieldType.INTEGER);

        final BusinessObject countryBO = new BusinessObject();
        countryBO.setQualifiedName(COUNTRY_QUALIFIED_NAME);
        countryBO.addField(name);
        countryBO.addUniqueConstraint("uk_name", "name");

        final SimpleField street = new SimpleField();
        street.setName("street");
        street.setType(FieldType.STRING);

        final SimpleField city = new SimpleField();
        city.setName("city");
        city.setType(FieldType.STRING);

        final RelationField country = new RelationField();
        country.setType(RelationField.Type.AGGREGATION);
        country.setFetchType(RelationField.FetchType.LAZY);
        country.setName("country");
        country.setCollection(Boolean.FALSE);
        country.setNullable(Boolean.TRUE);
        country.setReference(countryBO);

        final BusinessObject addressBO = new BusinessObject();
        addressBO.setQualifiedName(ADDRESS_QUALIFIED_NAME);
        addressBO.addField(street);
        addressBO.addField(city);
        addressBO.addField(country);
        addressBO.addQuery(COUNT_ADDRESS, "SELECT count(a) FROM Address a", Long.class.getName());
        addressBO.addUniqueConstraint("addressUK_with_relation", "city", "country");

        final BusinessObject dogBO = new BusinessObject();
        dogBO.setQualifiedName(DOG_QUALIFIED_NAME);
        dogBO.addField(name);
        dogBO.addField(age);
        final BusinessObject catBO = new BusinessObject();
        catBO.setQualifiedName(CAT_QUALIFIED_NAME);
        catBO.addField(name);
        catBO.addField(age);

        final RelationField addresses = new RelationField();
        addresses.setType(RelationField.Type.AGGREGATION);
        addresses.setFetchType(RelationField.FetchType.EAGER);
        addresses.setName("addresses");
        addresses.setCollection(Boolean.TRUE);
        addresses.setNullable(Boolean.TRUE);
        addresses.setReference(addressBO);

        final RelationField address = new RelationField();
        address.setType(RelationField.Type.AGGREGATION);
        address.setFetchType(RelationField.FetchType.LAZY);
        address.setName("address");
        address.setCollection(Boolean.FALSE);
        address.setNullable(Boolean.TRUE);
        address.setReference(addressBO);

        final RelationField dog = new RelationField();
        dog.setType(RelationField.Type.COMPOSITION);
        dog.setFetchType(RelationField.FetchType.EAGER);
        dog.setName("dog");
        dog.setCollection(Boolean.FALSE);
        dog.setNullable(Boolean.TRUE);
        dog.setReference(dogBO);

        final RelationField cat = new RelationField();
        cat.setType(RelationField.Type.COMPOSITION);
        cat.setFetchType(RelationField.FetchType.LAZY);
        //bug on lazy attribute in composition starting with a capital letter, see:  https://bonitasoft.atlassian.net/browse/BS-16031
        cat.setName("Cat");
        cat.setCollection(Boolean.FALSE);
        cat.setNullable(Boolean.TRUE);
        cat.setReference(catBO);

        final SimpleField firstName = new SimpleField();
        firstName.setName("firstName");
        firstName.setType(FieldType.STRING);
        firstName.setLength(10);

        final SimpleField birthDate = new SimpleField();
        birthDate.setName("birthDate");
        birthDate.setType(FieldType.LOCALDATE);
        birthDate.setNullable(Boolean.TRUE);
        
        final SimpleField lastName = new SimpleField();
        lastName.setName("lastName");
        lastName.setType(FieldType.STRING);
        lastName.setNullable(Boolean.FALSE);

        final SimpleField phoneNumbers = new SimpleField();
        phoneNumbers.setName("phoneNumbers");
        phoneNumbers.setType(FieldType.STRING);
        phoneNumbers.setLength(10);
        phoneNumbers.setCollection(Boolean.TRUE);

        final SimpleField hireDate = new SimpleField();
        hireDate.setName("hireDate");
        hireDate.setType(FieldType.DATE);

        final SimpleField booleanField = new SimpleField();
        booleanField.setName("booleanField");
        booleanField.setType(FieldType.BOOLEAN);

        final BusinessObject employee = new BusinessObject();
        employee.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        employee.addField(hireDate);
        employee.addField(booleanField);
        employee.addField(firstName);
        employee.addField(lastName);
        employee.addField(phoneNumbers);
        employee.addField(addresses);
        employee.addField(address);
        employee.addField(dog);
        employee.addField(cat);
        employee.addField(birthDate);
        employee.setDescription("Describe a simple employee");
        employee.addUniqueConstraint("uk_fl", "firstName", "lastName");

        final Query getEmployeeByPersistId = employee
                .addQuery("findByPersistId", "SELECT e FROM Employee e WHERE e.persistenceId=:id", EMPLOYEE_QUALIFIED_NAME);
        getEmployeeByPersistId.addQueryParameter("id", Long.class.getName());

        final Query getEmployeeByPhoneNumber = employee.addQuery(GET_EMPLOYEE_BY_PHONE_NUMBER_QUERY_NAME,
                "SELECT e FROM Employee e WHERE :phoneNumber IN ELEMENTS(e.phoneNumbers)", List.class.getName());
        getEmployeeByPhoneNumber.addQueryParameter("phoneNumber", String.class.getName());

        final Query findByFirstNAmeAndLastNameNewOrder = employee.addQuery(FIND_BY_FIRST_NAME_AND_LAST_NAME_NEW_ORDER,
                "SELECT e FROM Employee e WHERE e.firstName =:firstName AND e.lastName = :lastName ORDER BY e.lastName", List.class.getName());
        findByFirstNAmeAndLastNameNewOrder.addQueryParameter("firstName", String.class.getName());
        findByFirstNAmeAndLastNameNewOrder.addQueryParameter("lastName", String.class.getName());

        final Query findByFirstNameFetchAddresses = employee.addQuery(FIND_BY_FIRST_NAME_FETCH_ADDRESSES,
                "SELECT e FROM Employee e INNER JOIN FETCH e.addresses WHERE e.firstName =:firstName ORDER BY e.lastName", List.class.getName());
        findByFirstNameFetchAddresses.addQueryParameter("firstName", String.class.getName());

        final Query findByHireDate = employee.addQuery(FIND_BY_HIRE_DATE_RANGE,
                "SELECT e FROM Employee e WHERE e.hireDate >=:date1 and e.hireDate <=:date2", List.class.getName());
        findByHireDate.addQueryParameter("date1", Date.class.getName());
        findByHireDate.addQueryParameter("date2", Date.class.getName());

        final Query countForFindByHireDate = employee.addQuery(COUNT_FOR_FIND_BY_HIRE_DATE_RANGE,
                "SELECT count(e) FROM Employee e WHERE e.hireDate >=:date1 and e.hireDate <=:date2", Long.class.getName());
        countForFindByHireDate.addQueryParameter("date1", Date.class.getName());
        countForFindByHireDate.addQueryParameter("date2", Date.class.getName());

        employee.addQuery(COUNT_EMPLOYEE, "SELECT COUNT(e) FROM Employee e", Long.class.getName());

        final Query findEmployeesWithFirstNames = employee.addQuery(FIND_EMPLOYEE_WITH_FIRSTNAMES,
                "SELECT e FROM Employee e WHERE e.firstName IN (:firstNames) ORDER BY e.firstName",
                List.class.getName());
        findEmployeesWithFirstNames.addQueryParameter("firstNames", String[].class.getName());

        employee.addIndex("IDX_LSTNM", "lastName");
        employee.addIndex("IDX_LSTNM", "address");

        final BusinessObject person = new BusinessObject();
        person.setQualifiedName(PERSON_QUALIFIED_NAME);
        person.addField(hireDate);
        person.addField(firstName);
        person.addField(lastName);
        person.addField(phoneNumbers);
        person.setDescription("Describe a simple person");
        person.addUniqueConstraint("uk_fl", "firstName", "lastName");

        final BusinessObject productBO = new BusinessObject();
        productBO.setQualifiedName(PRODUCT_QUALIFIED_NAME);
        productBO.addField(name);

        final RelationField products = new RelationField();
        products.setType(RelationField.Type.AGGREGATION);
        products.setFetchType(RelationField.FetchType.LAZY);
        products.setName("products");
        products.setCollection(Boolean.TRUE);
        products.setNullable(Boolean.TRUE);
        products.setReference(productBO);

        final SimpleField releaseYear = new SimpleField();
        releaseYear.setName("releaseYear");
        releaseYear.setType(FieldType.STRING);

        final BusinessObject editionBO = new BusinessObject();
        editionBO.setQualifiedName("com.company.model.Edition");
        editionBO.addField(releaseYear);

        final RelationField editionField = new RelationField();
        editionField.setType(RelationField.Type.COMPOSITION);
        editionField.setFetchType(RelationField.FetchType.EAGER);
        editionField.setName("editions");
        editionField.setCollection(Boolean.TRUE);
        editionField.setNullable(Boolean.TRUE);
        editionField.setReference(editionBO);

        final BusinessObject catalogBO = new BusinessObject();
        catalogBO.setQualifiedName(PRODUCT_CATALOG_QUALIFIED_NAME);
        catalogBO.addField(name);
        catalogBO.addField(products);
        catalogBO.addField(editionField);

        final BusinessObjectModel model = new BusinessObjectModel();
        model.addBusinessObject(employee);
        model.addBusinessObject(person);
        model.addBusinessObject(addressBO);
        model.addBusinessObject(dogBO);
        model.addBusinessObject(catBO);
        model.addBusinessObject(countryBO);
        model.addBusinessObject(productBO);
        model.addBusinessObject(editionBO);
        model.addBusinessObject(catalogBO);
        return model;
    }
}
