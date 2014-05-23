package org.bonitasoft.engine.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.api.ThemeAPI;
import org.bonitasoft.engine.bpm.actor.ActorCriterion;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.actor.ActorNotFoundException;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.category.Category;
import org.bonitasoft.engine.bpm.category.CategoryCriterion;
import org.bonitasoft.engine.bpm.comment.ArchivedComment;
import org.bonitasoft.engine.bpm.comment.Comment;
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
import org.bonitasoft.engine.bpm.flownode.FlowNodeExecutionException;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstanceNotFoundException;
import org.bonitasoft.engine.bpm.flownode.GatewayInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.bpm.process.ProcessActivationException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDeployException;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoCriterion;
import org.bonitasoft.engine.bpm.process.ProcessEnablementException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceCriterion;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.bpm.process.impl.ActivityDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.DocumentBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.TransitionDefinitionBuilder;
import org.bonitasoft.engine.bpm.supervisor.ProcessSupervisor;
import org.bonitasoft.engine.bpm.supervisor.ProcessSupervisorSearchDescriptor;
import org.bonitasoft.engine.command.CommandDescriptor;
import org.bonitasoft.engine.command.CommandExecutionException;
import org.bonitasoft.engine.command.CommandNotFoundException;
import org.bonitasoft.engine.command.CommandParameterizationException;
import org.bonitasoft.engine.command.CommandSearchDescriptor;
import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.Connector;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionType;
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
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.operation.LeftOperand;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.platform.PlatformState;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.check.CheckNbOfActivities;
import org.bonitasoft.engine.test.check.CheckNbOfArchivedActivities;
import org.bonitasoft.engine.test.check.CheckNbOfArchivedActivityInstances;
import org.bonitasoft.engine.test.check.CheckNbOfHumanTasks;
import org.bonitasoft.engine.test.check.CheckNbOfOpenActivities;
import org.bonitasoft.engine.test.check.CheckNbOfProcessInstances;
import org.bonitasoft.engine.test.check.CheckNbPendingTaskOf;
import org.bonitasoft.engine.test.check.CheckProcessInstanceIsArchived;
import org.bonitasoft.engine.test.wait.WaitForActivity;
import org.bonitasoft.engine.test.wait.WaitForArchivedActivity;
import org.bonitasoft.engine.test.wait.WaitForCompletedArchivedStep;
import org.bonitasoft.engine.test.wait.WaitForDataValue;
import org.bonitasoft.engine.test.wait.WaitForEvent;
import org.bonitasoft.engine.test.wait.WaitForFinalArchivedActivity;
import org.bonitasoft.engine.test.wait.WaitForFlowNode;
import org.bonitasoft.engine.test.wait.WaitForPendingTasks;
import org.bonitasoft.engine.test.wait.WaitForStep;
import org.bonitasoft.engine.test.wait.WaitProcessToFinishAndBeArchived;
import org.junit.After;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Emmanuel Duchastenier
 * @author Frederic Bouquet
 * @author Celine Souchet
 */
public class APITestUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger(APITestUtil.class);

    public static final String SUPERVISOR_ID_KEY = "supervisorId";

    public static final String ROLE_ID_KEY = "roleId";

    public static final String GROUP_ID_KEY = "groupId";

    public static final String PROCESS_DEFINITION_ID_KEY = "processDefinitionId";

    public static final String DEFAULT_TENANT = "default";

    public static final String ACTOR_NAME = "Employee actor";

    public static final String PROCESS_VERSION = "1.0";

    public static final String PROCESS_NAME = "ProcessName";

    public static final String DESCRIPTION = "Coding all-night-long";

    public static final String PASSWORD = "bpm";

    public static final String USERNAME = "william.jobs";

    public static final String GROUP_NAME = "R&D";

    public static final String ROLE_NAME = "Developper";

    public static final int DEFAULT_REPEAT_EACH = 500;

    public static final int DEFAULT_TIMEOUT;

    private APISession session;

    private ProcessAPI processAPI;

    private IdentityAPI identityAPI;

    private CommandAPI commandAPI;

    private ProfileAPI profileAPI;

    private ThemeAPI themeAPI;

    static {
        String strTimeout = System.getProperty("sysprop.bonita.default.test.timeout");
        if (strTimeout != null) {
            DEFAULT_TIMEOUT = Integer.valueOf(strTimeout);
        } else {
            DEFAULT_TIMEOUT = 7 * 60 * 1000;
        }
    }

    @After
    public void clearSynchroRepository() {
        try {
            login();
            ClientEventUtil.clearRepo(getCommandAPI());
            logout();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void loginWith(final String userName, final String password) throws BonitaException {
        setSession(loginTenant(userName, password));
        setIdentityAPI(TenantAPIAccessor.getIdentityAPI(getSession()));
        setProcessAPI(TenantAPIAccessor.getProcessAPI(getSession()));
        setCommandAPI(TenantAPIAccessor.getCommandAPI(getSession()));
        setProfileAPI(TenantAPIAccessor.getProfileAPI(getSession()));
        setThemeAPI(TenantAPIAccessor.getThemeAPI(getSession()));
    }

    public void login() throws BonitaException {
        setSession(loginDefaultTenant());
        setIdentityAPI(TenantAPIAccessor.getIdentityAPI(getSession()));
        setProcessAPI(TenantAPIAccessor.getProcessAPI(getSession()));
        setCommandAPI(TenantAPIAccessor.getCommandAPI(getSession()));
        setProfileAPI(TenantAPIAccessor.getProfileAPI(getSession()));
        setThemeAPI(TenantAPIAccessor.getThemeAPI(getSession()));
    }

    public void logout() throws BonitaException {
        logoutTenant(getSession());
        setSession(null);
        setIdentityAPI(null);
        setProcessAPI(null);
        setCommandAPI(null);
        setProfileAPI(null);
        setThemeAPI(null);
    }

    public void logoutThenlogin() throws BonitaException {
        logout();
        login();
    }

    public void logoutThenloginAs(final String userName, final String password) throws BonitaException {
        logout();
        loginWith(userName, password);
    }

    public User addMappingAndActor(final String actorName, final String userName, final String password, final ProcessDefinition processDefinition)
            throws BonitaException {
        final User user = getIdentityAPI().createUser(userName, password);
        getProcessAPI().addUserToActor(actorName, processDefinition, user.getId());
        return user;
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

    public ActorInstance getActor(final String actorName, final ProcessDefinition definition) throws ActorNotFoundException {
        final List<ActorInstance> actors = getProcessAPI().getActors(definition.getId(), 0, 50, ActorCriterion.NAME_ASC);
        final ActorInstance actorInstance = getActorInstance(actors, actorName);
        if (actorInstance == null) {
            throw new ActorNotFoundException(actorName + " is an unknown actor");
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
        logout();
        loginWith(userName, password);
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

    public List<String> initActorAndDescription(final ProcessDefinitionBuilder processBuilder, final int nbActor) {
        final List<String> actorList = new ArrayList<String>();
        for (int i = 1; i <= nbActor; i++) {
            final String actorName = "actor" + i;
            processBuilder.addActor(actorName).addDescription(DESCRIPTION + i);
            actorList.add(actorName);
        }
        return actorList;
    }

    /**
     * delete all the object used by method ...SupervisorBy
     * 
     * @param map
     * @param userId
     * @throws Exception
     */
    public void deleteRoleGroupSupervisor(final Map<String, Object> map, final long userId) throws BonitaException {
        deleteSupervisor(((ProcessSupervisor) map.get("supervisorByRole")).getSupervisorId());
        deleteSupervisor(((ProcessSupervisor) map.get("supervisorByGroup")).getSupervisorId());
        getIdentityAPI().deleteUserMembership(userId, ((Group) map.get(GROUP_ID_KEY)).getId(), ((Role) map.get(ROLE_ID_KEY)).getId());
        getIdentityAPI().deleteRole(((Role) map.get(ROLE_ID_KEY)).getId());
        getIdentityAPI().deleteGroup(((Group) map.get(GROUP_ID_KEY)).getId());
    }

    public void deleteUserAndProcess(final String johnName, final ProcessDefinition definition) throws BonitaException {
        deleteUser(johnName);
        deleteProcess(definition);
    }

    public void deleteProcess(final ProcessDefinition processDefinition) throws BonitaException {
        deleteProcess(processDefinition.getId());
    }

    public void deleteProcess(final ProcessDefinition... processDefinitions) throws BonitaException {
        deleteProcess(Arrays.asList(processDefinitions));
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
        deleteUser(user.getId());
    }

    public void deleteUser(final long userId) throws BonitaException {
        getIdentityAPI().deleteUser(userId);
    }

    public void deleteCategories(final List<Category> categories) throws BonitaException {
        for (final Category category : categories) {
            getProcessAPI().deleteCategory(category.getId());
        }
    }

    public ProcessDefinition deployAndEnableProcess(final DesignProcessDefinition designProcessDefinition) throws BonitaException {
        return getProcessAPI().deployAndEnableProcess(designProcessDefinition);
    }

    public ProcessDefinition deployAndEnableProcess(final BusinessArchive businessArchive) throws BonitaException {
        return getProcessAPI().deployAndEnableProcess(businessArchive);
    }

    public ProcessDefinition deployAndEnableWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final User user)
            throws BonitaException {
        return deployAndEnableWithActor(designProcessDefinition, Arrays.asList(actorName), Arrays.asList(user));
    }

    public ProcessDefinition deployAndEnableWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final List<User> users)
            throws BonitaException {
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        return deployAndEnableWithActor(businessArchive, actorName, users);
    }

    public ProcessDefinition deployAndEnableWithActor(final BusinessArchive businessArchive, final String actorName, final List<User> users)
            throws AlreadyExistsException, ProcessDeployException, ActorNotFoundException, CreationException, ProcessDefinitionNotFoundException,
            ProcessEnablementException {
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);
        for (final User user : users) {
            getProcessAPI().addUserToActor(actorName, processDefinition, user.getId());
        }
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    public ProcessDefinition deployAndEnableWithActorAndParameters(final DesignProcessDefinition designProcessDefinition, final String actorName,
            final User user, final Map<String, String> parameters) throws BonitaException {
        return deployAndEnableWithActorAndParameters(designProcessDefinition, Arrays.asList(actorName), Arrays.asList(user), parameters);
    }

    public ProcessDefinition deployAndEnableWithActor(final DesignProcessDefinition designProcessDefinition, final List<String> actorsName,
            final List<User> users) throws BonitaException {
        return deployAndEnableWithActor(new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done(),
                actorsName, users);
    }

    public ProcessDefinition deployAndEnableWithActor(final BusinessArchive businessArchive, final List<String> actorsName, final List<User> users)
            throws BonitaException {
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);
        for (int i = 0; i < users.size(); i++) {
            getProcessAPI().addUserToActor(actorsName.get(i), processDefinition, users.get(i).getId());
        }
        try {
            processAPI.enableProcess(processDefinition.getId());
        } catch (final ProcessEnablementException e) {
            final List<Problem> problems = processAPI.getProcessResolutionProblems(processDefinition.getId());
            throw new ProcessEnablementException("not resolved: " + problems);
        }
        return processDefinition;
    }

    public ProcessDefinition deployAndEnableWithActor(final BusinessArchive businessArchive, final String actorName, final User user) throws BonitaException {
        return deployAndEnableWithActor(businessArchive, Collections.singletonList(actorName), Collections.singletonList(user));
    }

    public ProcessDefinition deployAndEnableWithActorAndParameters(final DesignProcessDefinition designProcessDefinition, final List<String> actorsName,
            final List<User> users, final Map<String, String> parameters) throws BonitaException {
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        final BusinessArchive businessArchive = businessArchiveBuilder.setParameters(parameters).setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);
        for (int i = 0; i < users.size(); i++) {
            getProcessAPI().addUserToActor(actorsName.get(i), processDefinition, users.get(i).getId());
        }
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    public ProcessDefinition deployAndEnableWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final Group group)
            throws BonitaException {
        final ProcessDefinition processDefinition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        getProcessAPI().addGroupToActor(actorName, group.getId(), processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    public ProcessDefinition deployAndEnableWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final Group... groups)
            throws BonitaException {
        final ProcessDefinition processDefinition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        for (final Group group : groups) {
            getProcessAPI().addGroupToActor(actorName, group.getId(), processDefinition);
        }
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    public ProcessDefinition deployAndEnableWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final Role role)
            throws BonitaException {
        final ProcessDefinition processDefinition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        addMappingOfActorsForRole(actorName, role.getId(), processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    public ProcessDefinition deployAndEnableWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final Role... roles)
            throws BonitaException {
        final ProcessDefinition processDefinition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        for (final Role role : roles) {
            addMappingOfActorsForRole(actorName, role.getId(), processDefinition);
        }
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    public ProcessDefinition deployAndEnableWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final Role role,
            final Group group) throws BonitaException {
        final ProcessDefinition processDefinition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        addMappingOfActorsForRoleAndGroup(actorName, role.getId(), group.getId(), processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    public ProcessDefinition deployAndEnableWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final long userId)
            throws BonitaException {
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);
        getProcessAPI().addUserToActor(actorName, processDefinition, userId);
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    public ProcessDefinition deployProcessWithConnector(final ProcessDefinitionBuilder processDefinitionBuilder,
            final List<BarResource> connectorImplementations, final List<BarResource> generateConnectorDependencies) throws BonitaException {
        final BusinessArchiveBuilder businessArchiveBuilder = buildBusinessArchiveWithConnectorAndUserFilter(processDefinitionBuilder,
                connectorImplementations, generateConnectorDependencies, Collections.<BarResource> emptyList());
        return deployAndEnableProcess(businessArchiveBuilder.done());
    }

    public ProcessDefinition deployProcessWithConnector(final ProcessDefinitionBuilder processDefinitionBuilder, final String name,
            final Class<? extends AbstractConnector> clazz, final String jarName) throws BonitaException, IOException {
        return deployProcessWithConnector(processDefinitionBuilder, Arrays.asList(getContentAndBuildBarResource(name, clazz)),
                Arrays.asList(generateJarAndBuildBarResource(clazz, jarName)));
    }

    public ProcessDefinition deployProcessWithActorAndConnector(final ProcessDefinitionBuilder processDefinitionBuilder, final String actorName,
            final User user, final String name, final Class<? extends AbstractConnector> clazz, final String jarName) throws BonitaException, IOException {
        return deployProcessWithActorAndConnectorAndParameter(processDefinitionBuilder, actorName, user,
                Arrays.asList(getContentAndBuildBarResource(name, clazz)), Arrays.asList(generateJarAndBuildBarResource(clazz, jarName)), null);
    }

    public ProcessDefinition deployProcessWithActorAndConnectorAndParameter(final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actorName, final User user, final List<BarResource> connectorImplementations, final List<BarResource> generateConnectorDependencies,
            final Map<String, String> parameters) throws BonitaException {
        final BusinessArchiveBuilder businessArchiveBuilder = buildBusinessArchiveWithConnectorAndUserFilter(processDefinitionBuilder,
                connectorImplementations, generateConnectorDependencies, Collections.<BarResource> emptyList());
        if (parameters != null) {
            businessArchiveBuilder.setParameters(parameters);
        }
        return deployAndEnableWithActor(businessArchiveBuilder.done(), actorName, user);
    }

    public ProcessDefinition deployProcessWithActorAndConnectorAndUserFilter(final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actorName, final User user, final List<BarResource> connectorImplementations, final List<BarResource> generateConnectorDependencies,
            final List<BarResource> userFilters) throws BonitaException {
        final BusinessArchiveBuilder businessArchiveBuilder = buildBusinessArchiveWithConnectorAndUserFilter(processDefinitionBuilder,
                connectorImplementations, generateConnectorDependencies, userFilters);
        return deployAndEnableWithActor(businessArchiveBuilder.done(), actorName, user);
    }

    public ProcessDefinition deployProcessWithActorAndConnectorAndParameter(final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actorName, final User user, final Map<String, String> parameters, final String name, final Class<? extends AbstractConnector> clazz,
            final String jarName) throws BonitaException, IOException {
        return deployProcessWithActorAndConnectorAndParameter(processDefinitionBuilder, actorName, user,
                Arrays.asList(getContentAndBuildBarResource(name, clazz)), Arrays.asList(generateJarAndBuildBarResource(clazz, jarName)), parameters);
    }

    public ProcessDefinition buildBusinessArchiveWithActorAndUserFilter(final ProcessDefinitionBuilder processDefinitionBuilder, final String actorName,
            final User user, final List<BarResource> generateFilterDependencies, final List<BarResource> userFilters)
            throws BonitaException {
        return deployProcessWithActorAndConnectorAndUserFilter(processDefinitionBuilder, actorName, user, Collections.<BarResource> emptyList(),
                generateFilterDependencies, userFilters);
    }

    private BusinessArchiveBuilder buildBusinessArchiveWithConnectorAndUserFilter(final ProcessDefinitionBuilder processDefinitionBuilder,
            final List<BarResource> connectorImplementations, final List<BarResource> generateConnectorDependencies, final List<BarResource> userFilters)
            throws InvalidProcessDefinitionException {
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                processDefinitionBuilder.done());
        for (final BarResource barResource : connectorImplementations) {
            businessArchiveBuilder.addConnectorImplementation(barResource);
        }

        for (final BarResource barResource : generateConnectorDependencies) {
            businessArchiveBuilder.addClasspathResource(barResource);
        }

        for (final BarResource barResource : userFilters) {
            businessArchiveBuilder.addUserFilters(barResource);
        }
        return businessArchiveBuilder;
    }

    public BarResource getContentAndBuildBarResource(final String name, final Class<? extends Connector> clazz) throws IOException {
        final InputStream stream = clazz.getResourceAsStream(name);
        assertNotNull(stream);
        final byte[] byteArray = IOUtils.toByteArray(stream);
        stream.close();
        return new BarResource(name, byteArray);
    }

    public BarResource generateJarAndBuildBarResource(final Class<?> clazz, final String name) throws IOException {
        final byte[] data = IOUtil.generateJar(clazz);
        return new BarResource(name, data);
    }

    public void disableAndDeleteProcess(final ProcessDefinition processDefinition) throws BonitaException {
        if (processDefinition != null) {
            final ProcessDeploymentInfo deploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
            if (deploymentInfo.getActivationState().equals(ActivationState.ENABLED)) {
                disableAndDeleteProcess(processDefinition.getId());
            } else {
                deleteProcess(processDefinition);
            }
        }
    }

    public void disableAndDeleteProcess(final long processDefinitionId) throws BonitaException {
        // Delete all process instances
        long nbDeletedProcessInstances;
        getProcessAPI().disableProcess(processDefinitionId);
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
        while (getProcessAPI().deleteArchivedProcessInstances(processDefinitionId, 0, 10) != 0) {
        }
        while (getProcessAPI().deleteProcessInstances(processDefinitionId, 0, 10) != 0) {
        }
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
        return session;
    }

    public static boolean containsState(final List<ArchivedProcessInstance> instances, final String state) {
        for (final ArchivedProcessInstance pi : instances) {
            if (state.equals(pi.getState())) {
                return true;
            }
        }
        return false;
    }

    public Group createGroup(final String groupName) throws AlreadyExistsException, CreationException {
        return createGroup(groupName, null);
    }

    public Group createGroup(final String groupName, final String parentGroupPath) throws AlreadyExistsException, CreationException {
        return getIdentityAPI().createGroup(groupName, parentGroupPath);
    }

    public Group createGroup(final String name, final String displayName, final String description) throws AlreadyExistsException, CreationException {
        final GroupCreator groupCreator = new GroupCreator(name);
        groupCreator.setDisplayName(displayName).setDescription(description);
        return getIdentityAPI().createGroup(groupCreator);
    }

    public Role createRole(final String roleName) throws BonitaException {
        return getIdentityAPI().createRole(roleName);
    }

    public Operation buildAssignOperation(final String dataInstanceName, final String expressionContent, final ExpressionType expressionType,
            final String returnType) throws InvalidExpressionException {
        final LeftOperand leftOperand = new LeftOperandBuilder().createNewInstance().setName(dataInstanceName).done();
        final Expression expression = new ExpressionBuilder().createNewInstance(dataInstanceName).setContent(expressionContent)
                .setExpressionType(expressionType).setReturnType(returnType).done();
        return new OperationBuilder().createNewInstance().setOperator("=").setLeftOperand(leftOperand).setType(OperatorType.ASSIGNMENT)
                .setRightOperand(expression).done();
    }

    public void assignAndExecuteStep(final ActivityInstance activityInstance, final long userId) throws BonitaException {
        assignAndExecuteStep(activityInstance.getId(), userId);
    }

    public void assignAndExecuteStep(final long activityInstanceId, final long userId) throws BonitaException {
        getProcessAPI().assignUserTask(activityInstanceId, userId);
        executeFlowNodeUntilEnd(activityInstanceId);
    }

    public void executeFlowNodeUntilEnd(final long flowNodeId) throws FlowNodeExecutionException {
        getProcessAPI().executeFlowNode(flowNodeId);
    }

    public HumanTaskInstance waitForUserTask(final String taskName, final ProcessInstance processInstance) throws Exception {
        return waitForUserTask(taskName, processInstance.getId(), DEFAULT_TIMEOUT);
    }

    public HumanTaskInstance waitForUserTask(final String taskName, final long processInstanceId) throws Exception {
        return waitForUserTask(taskName, processInstanceId, DEFAULT_TIMEOUT);
    }

    private HumanTaskInstance waitForUserTask(final String taskName, final long processInstanceId, final int timeout) throws Exception {
        final Map<String, Serializable> readyTaskEvent;
        if (processInstanceId > 0) {
            readyTaskEvent = ClientEventUtil.getReadyTaskEvent(processInstanceId, taskName);
        } else {
            readyTaskEvent = ClientEventUtil.getReadyTaskEvent(taskName);
        }
        final Long activityInstanceId = ClientEventUtil.executeWaitServerCommand(getCommandAPI(), readyTaskEvent, timeout);
        final HumanTaskInstance getHumanTaskInstance = getHumanTaskInstance(activityInstanceId);
        assertNotNull(getHumanTaskInstance);
        return getHumanTaskInstance;
    }

    public HumanTaskInstance waitForUserTask(final String taskName) throws Exception {
        return waitForUserTask(taskName, -1, DEFAULT_TIMEOUT);
    }

    public HumanTaskInstance waitForUserTaskAndExecuteIt(final String taskName, final User user) throws Exception {
        final HumanTaskInstance humanTaskInstance = waitForUserTask(taskName);
        assignAndExecuteStep(humanTaskInstance, user.getId());
        return humanTaskInstance;
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

    public void deleteSupervisor(final long id) throws BonitaException {
        getProcessAPI().deleteSupervisor(id);
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
        final ActivityInstance task = waitForUserTask(taskName, processInstance.getId());
        return new StartProcessUntilStep(processInstance, task);
    }

    @Deprecated
    public ArchivedActivityInstance waitForArchivedActivity(final long activityId, final String stateName) throws Exception {
        final WaitForArchivedActivity waitForArchivedActivity = new WaitForArchivedActivity(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, activityId, stateName,
                getProcessAPI());
        waitForArchivedActivity.waitUntil();
        final ArchivedActivityInstance archivedActivityInstance = waitForArchivedActivity.getArchivedActivityInstance();
        assertNotNull(archivedActivityInstance);
        assertEquals(stateName, archivedActivityInstance.getState());
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

    @Deprecated
    private void waitForProcessToFinish(final int repeatEach, final int timeout, final ProcessInstance processInstance, final String state) throws Exception {
        final WaitProcessToFinishAndBeArchived waitProcessToFinishAndBeArchived = new WaitProcessToFinishAndBeArchived(repeatEach, timeout, false,
                processInstance, getProcessAPI(), state);
        assertTrue(waitProcessToFinishAndBeArchived.waitUntil());
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
        ClientEventUtil.executeWaitServerCommand(getCommandAPI(), ClientEventUtil.getProcessInstanceInState(processInstance.getId(), state.getId()),
                DEFAULT_TIMEOUT);
    }

    @Deprecated
    public void waitForProcessToFinish(final ProcessInstance processInstance, final String state) throws Exception {
        waitForProcessToFinish(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, processInstance, state);
    }

    @Deprecated
    private boolean waitProcessToFinishAndBeArchived(final int repeatEach, final int timeout, final ProcessInstance processInstance) throws Exception {
        final boolean waitUntil = new WaitProcessToFinishAndBeArchived(repeatEach, timeout, processInstance, processAPI).waitUntil();
        assertTrue("Process was not finished", waitUntil);
        return waitUntil;
    }

    @Deprecated
    public boolean waitForProcessToFinishAndBeArchived(final ProcessInstance processInstance) throws Exception {
        return waitProcessToFinishAndBeArchived(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, processInstance);
    }

    private Long waitForFlowNode(final long processInstanceId, final String state, final String flowNodeName, final boolean useRootProcessInstance,
            final int timeout) throws Exception {
        Map<String, Serializable> params;
        if (useRootProcessInstance) {
            params = ClientEventUtil.getTaskInState(processInstanceId, state, flowNodeName);
        } else {
            params = ClientEventUtil.getTaskInStateWithParentId(processInstanceId, state, flowNodeName);
        }
        return ClientEventUtil.executeWaitServerCommand(getCommandAPI(), params, timeout);
    }

    public FlowNodeInstance waitForFlowNodeInReadyState(final ProcessInstance processInstance, final String flowNodeName, final boolean useRootProcessInstance)
            throws Exception {
        final Long flowNodeInstanceId = waitForFlowNode(processInstance.getId(), TestStates.getReadyState(), flowNodeName, useRootProcessInstance,
                DEFAULT_TIMEOUT);
        final FlowNodeInstance flowNodeInstance = getProcessAPI().getFlowNodeInstance(flowNodeInstanceId);
        assertNotNull(flowNodeInstance);
        return flowNodeInstance;
    }

    public FlowNodeInstance waitForFlowNodeInExecutingState(final ProcessInstance processInstance, final String flowNodeName,
            final boolean useRootProcessInstance) throws Exception {
        final Long activityId = waitForFlowNode(processInstance.getId(), TestStates.getExecutingState(), flowNodeName, useRootProcessInstance, DEFAULT_TIMEOUT);
        return getFlowNodeInstance(activityId);
    }

    public void waitForFlowNodeInState(final ProcessInstance processInstance, final String flowNodeName, final String state,
            final boolean useRootProcessInstance) throws Exception {
        waitForFlowNode(processInstance.getId(), state, flowNodeName, useRootProcessInstance, DEFAULT_TIMEOUT);
    }

    public ActivityInstance waitForTaskInState(final ProcessInstance processInstance, final String flowNodeName, final String state) throws Exception {
        final Long activityId = waitForFlowNode(processInstance.getId(), state, flowNodeName, true, DEFAULT_TIMEOUT);
        return getActivityInstance(activityId);
    }

    public FlowNodeInstance waitForFlowNodeInInterruptingState(final ProcessInstance processInstance, final String flowNodeName,
            final boolean useRootProcessInstance) throws Exception {
        final Long flowNodeInstanceId = waitForFlowNode(processInstance.getId(), TestStates.getInterruptingState(), flowNodeName, useRootProcessInstance,
                DEFAULT_TIMEOUT);
        final FlowNodeInstance flowNodeInstance = getProcessAPI().getFlowNodeInstance(flowNodeInstanceId);
        assertNotNull(flowNodeInstance);
        return flowNodeInstance;
    }

    public ActivityInstance waitForTaskToFail(final ProcessInstance processInstance) throws Exception {
        final Long activityId = ClientEventUtil.executeWaitServerCommand(getCommandAPI(),
                ClientEventUtil.getTaskInState(processInstance.getId(), TestStates.getFailedState()), DEFAULT_TIMEOUT);
        return getActivityInstance(activityId);
    }

    public FlowNodeInstance waitForFlowNodeInFailedState(final ProcessInstance processInstance) throws Exception {
        final Long activityId = ClientEventUtil.executeWaitServerCommand(getCommandAPI(),
                ClientEventUtil.getTaskInState(processInstance.getId(), TestStates.getFailedState()), DEFAULT_TIMEOUT);
        return getFlowNodeInstance(activityId);
    }

    public HumanTaskInstance waitForUserTaskAndExecuteIt(final String taskName, final long processInstanceId, final long userId) throws Exception {
        final HumanTaskInstance humanTaskInstance = waitForUserTask(taskName, processInstanceId, DEFAULT_TIMEOUT);
        assignAndExecuteStep(humanTaskInstance, userId);
        return humanTaskInstance;
    }

    public HumanTaskInstance waitForUserTaskAndExecuteIt(final String taskName, final ProcessInstance processInstance, final long userId) throws Exception {
        return waitForUserTaskAndExecuteIt(taskName, processInstance.getId(), userId);
    }

    public HumanTaskInstance waitForUserTaskAndExecuteIt(final String taskName, final long processInstanceId, final User user) throws Exception {
        return waitForUserTaskAndExecuteIt(taskName, processInstanceId, user.getId());
    }

    public HumanTaskInstance waitForUserTaskAndExecuteIt(final String taskName, final ProcessInstance processInstance, final User user) throws Exception {
        return waitForUserTaskAndExecuteIt(taskName, processInstance.getId(), user.getId());
    }

    public ActivityInstance waitForUserTaskAndAssigneIt(final String taskName, final long processInstanceId, final long userId) throws Exception,
            UpdateException {
        final ActivityInstance activityInstance = waitForUserTask(taskName, processInstanceId);
        getProcessAPI().assignUserTask(activityInstance.getId(), userId);
        return activityInstance;
    }

    public ActivityInstance waitForUserTaskAndAssigneIt(final String taskName, final ProcessInstance processInstance, final long userId) throws Exception,
            UpdateException {
        return waitForUserTaskAndAssigneIt(taskName, processInstance.getId(), userId);
    }

    public ActivityInstance waitForUserTaskAndAssigneIt(final String taskName, final ProcessInstance processInstance, final User user) throws Exception,
            UpdateException {
        return waitForUserTaskAndAssigneIt(taskName, processInstance.getId(), user.getId());
    }

    @Deprecated
    public GatewayInstance waitForGateway(final String name, final ProcessInstance processInstance, final boolean useRootProcessInstance) throws Exception {
        final WaitForFlowNode waitForFlowNode = new WaitForFlowNode(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, name, processInstance.getId(),
                useRootProcessInstance, processAPI);
        waitForFlowNode.waitUntil();
        final FlowNodeInstance result = waitForFlowNode.getResult();
        assertNotNull(result);
        if (result instanceof GatewayInstance) {
            return (GatewayInstance) result;
        }
        return null;
    }

    @Deprecated
    public WaitForStep waitForStep(final String taskName, final ProcessInstance processInstance) throws Exception {
        return waitForStep(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, taskName, processInstance);
    }

    @Deprecated
    private WaitForStep waitForStep(final int repeatEach, final int timeout, final String taskName, final ProcessInstance processInstance) throws Exception {
        final WaitForStep waitForStep = new WaitForStep(repeatEach, timeout, taskName, processInstance.getId(), getProcessAPI());
        assertTrue("Task " + taskName + " not found", waitForStep.waitUntil());
        return waitForStep;
    }

    @Deprecated
    public WaitForStep waitForStep(final String taskName, final ProcessInstance processInstance, final String state) throws Exception {
        return waitForStep(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, taskName, processInstance, state);
    }

    @Deprecated
    private WaitForStep waitForStep(final int repeatEach, final int timeout, final String taskName, final ProcessInstance processInstance, final String state)
            throws Exception {
        final WaitForStep waitForStep = new WaitForStep(repeatEach, timeout, taskName, processInstance.getId(), state, getProcessAPI());
        assertTrue("Task " + taskName + " not found", waitForStep.waitUntil());
        return waitForStep;
    }

    @Deprecated
    public WaitForActivity waitForActivity(final String activityName, final ProcessInstance processInstance) throws Exception {
        return waitForActivity(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, activityName, processInstance);
    }

    @Deprecated
    private WaitForActivity waitForActivity(final int repeatEach, final int timeout, final String activityName, final ProcessInstance processInstance)
            throws Exception {
        return waitForActivity(repeatEach, timeout, activityName, processInstance, null);
    }

    @Deprecated
    public WaitForActivity waitForActivity(final String activityName, final ProcessInstance processInstance, final String state) throws Exception {
        return waitForActivity(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, activityName, processInstance, state);
    }

    @Deprecated
    private WaitForActivity waitForActivity(final int repeatEach, final int timeout, final String activityName, final ProcessInstance processInstance,
            final String state) throws Exception {
        final WaitForActivity waitForActivity = new WaitForActivity(repeatEach, timeout, activityName, processInstance.getId(), state, getProcessAPI());
        assertTrue(waitForActivity.waitUntil());
        return waitForActivity;
    }

    @Deprecated
    public WaitForFinalArchivedActivity waitForFinalArchivedActivity(final String activityName, final ProcessInstance processInstance) throws Exception {
        final WaitForFinalArchivedActivity waitForFinalArchivedActivity = new WaitForFinalArchivedActivity(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, activityName,
                processInstance.getId(), getProcessAPI());
        assertTrue(activityName + " should be finished and archived", waitForFinalArchivedActivity.waitUntil());
        return waitForFinalArchivedActivity;
    }

    @Deprecated
    public void waitForDataValue(final ProcessInstance processInstance, final String dataName, final String expectedValue) throws Exception {
        final WaitForDataValue waitForConnector = new WaitForDataValue(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, processInstance.getId(), dataName, expectedValue,
                getProcessAPI());
        assertTrue("Can't find data <" + dataName + "> with value <" + expectedValue + ">", waitForConnector.waitUntil());
    }

    @Deprecated
    public WaitForEvent waitForEventInWaitingState(final ProcessInstance processInstance, final String eventName) throws Exception {
        return waitForEvent(processInstance, eventName, TestStates.getWaitingState());
    }

    @Deprecated
    public WaitForEvent waitForEventInWaitingState(final long processInstanceId, final String eventName) throws Exception {
        return waitForEvent(processInstanceId, eventName, TestStates.getWaitingState());
    }

    @Deprecated
    public WaitForEvent waitForEvent(final long processInstanceId, final String eventName, final String state) throws Exception {
        return waitForEvent(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, processInstanceId, eventName, state);
    }

    @Deprecated
    public WaitForEvent waitForEvent(final ProcessInstance processInstance, final String eventName, final String state) throws Exception {
        return waitForEvent(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, processInstance.getId(), eventName, state);
    }

    @Deprecated
    private WaitForEvent waitForEvent(final int repeatEach, final int timeout, final long processInstanceId, final String eventName, final String state)
            throws Exception {
        final WaitForEvent waitForEvent = new WaitForEvent(repeatEach, timeout, eventName, processInstanceId, state, getProcessAPI());
        assertTrue("Expected 1 activities in " + state + " state", waitForEvent.waitUntil());
        return waitForEvent;
    }

    public void deletePlatformStructure() throws BonitaException {
        final PlatformLoginAPI platformLoginAPI = getPlatformLoginAPI();
        final PlatformSession session = platformLoginAPI.login("platformAdmin", "platform");
        final PlatformAPI platformAPI = getPlatformAPI(session);
        platformAPI.deletePlatform();
        platformLoginAPI.logout(session);
    }

    public void deleteStopAndCleanPlatformAndTenant(final boolean undeployCommands) throws BonitaException {
        final PlatformSession session = loginPlatform();
        final PlatformAPI platformAPI = getPlatformAPI(session);
        stopAndCleanPlatformAndTenant(platformAPI, undeployCommands);
        platformAPI.deletePlatform();
        logoutPlatform(session);
    }

    public void createPlatformStructure() throws BonitaException {
        final PlatformLoginAPI platformLoginAPI = getPlatformLoginAPI();
        final PlatformSession session = platformLoginAPI.login("platformAdmin", "platform");
        final PlatformAPI platformAPI = getPlatformAPI(session);
        createPlatformStructure(platformAPI, false);
        platformLoginAPI.logout(session);
    }

    public PlatformAPI getPlatformAPI(final PlatformSession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return PlatformAPIAccessor.getPlatformAPI(session);
    }

    private void createPlatformStructure(final PlatformAPI platformAPI, final boolean deployCommands) throws BonitaException {
        if (platformAPI.isPlatformCreated()) {
            if (PlatformState.STARTED.equals(platformAPI.getPlatformState())) {
                stopPlatformAndTenant(platformAPI, deployCommands);
            }
            platformAPI.cleanPlatform();
            platformAPI.deletePlatform();
        }
        platformAPI.createPlatform();
    }

    public void createInitializeAndStartPlatformWithDefaultTenant(final boolean deployCommands) throws BonitaException {
        final PlatformSession session = loginPlatform();
        final PlatformAPI platformAPI = getPlatformAPI(session);
        createPlatformStructure(platformAPI, deployCommands);
        initializeAndStartPlatformWithDefaultTenant(platformAPI, deployCommands);
        logoutPlatform(session);
    }

    public void initializeAndStartPlatformWithDefaultTenant(final boolean deployCommands) throws BonitaException {
        final PlatformSession session = loginPlatform();
        final PlatformAPI platformAPI = getPlatformAPI(session);
        initializeAndStartPlatformWithDefaultTenant(platformAPI, deployCommands);
        logoutPlatform(session);
    }

    public void initializeAndStartPlatformWithDefaultTenant(final PlatformAPI platformAPI, final boolean deployCommands) throws BonitaException {
        platformAPI.initializePlatform();
        platformAPI.startNode();

        if (deployCommands) {
            final APISession loginDefaultTenant = loginDefaultTenant();
            ClientEventUtil.deployCommand(loginDefaultTenant);
            logoutTenant(loginDefaultTenant);
        }
    }

    public void stopAndCleanPlatformAndTenant(final boolean undeployCommands) throws BonitaException {
        final PlatformSession session = loginPlatform();
        final PlatformAPI platformAPI = getPlatformAPI(session);
        stopAndCleanPlatformAndTenant(platformAPI, undeployCommands);
        logoutPlatform(session);
    }

    public void stopAndCleanPlatformAndTenant(final PlatformAPI platformAPI, final boolean undeployCommands) throws BonitaException {
        if (platformAPI.isNodeStarted()) {
            stopPlatformAndTenant(platformAPI, undeployCommands);
            cleanPlatform(platformAPI);
        }
    }

    public void stopPlatformAndTenant(final PlatformAPI platformAPI, final boolean undeployCommands) throws BonitaException {
        if (undeployCommands) {
            final APISession loginDefaultTenant = loginDefaultTenant();
            ClientEventUtil.undeployCommand(loginDefaultTenant);
            logoutTenant(loginDefaultTenant);
        }

        platformAPI.stopNode();
    }

    public static void cleanPlatform(final PlatformAPI platformAPI) throws BonitaException {
        platformAPI.cleanPlatform();
    }

    public PlatformSession loginPlatform() throws BonitaException {
        final PlatformLoginAPI platformLoginAPI = getPlatformLoginAPI();
        return platformLoginAPI.login("platformAdmin", "platform");
    }

    public PlatformLoginAPI getPlatformLoginAPI() throws BonitaException {
        return PlatformAPIAccessor.getPlatformLoginAPI();
    }

    public void logoutPlatform(final PlatformSession session) throws BonitaException {
        final PlatformLoginAPI platformLoginAPI = getPlatformLoginAPI();
        platformLoginAPI.logout(session);
    }

    public APISession loginDefaultTenant() throws BonitaException {
        return loginTenant("install", "install");
    }

    public APISession loginTenant(final String userName, final String password) throws BonitaException {
        final LoginAPI loginAPI = getLoginAPI();
        return loginAPI.login(userName, password);
    }

    public LoginAPI getLoginAPI() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return TenantAPIAccessor.getLoginAPI();
    }

    public void logoutTenant(final APISession session) throws BonitaException {
        final LoginAPI loginAPI = getLoginAPI();
        loginAPI.logout(session);
    }

    public static DesignProcessDefinition createProcessDefinitionWithHumanAndAutomaticSteps(final String processName, final String processVersion,
            final List<String> stepNames, final List<Boolean> isHuman, final String actorName, final boolean addActorInitiator, final boolean parallelActivities)
            throws InvalidProcessDefinitionException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(processName, processVersion);
        if (!isHuman.isEmpty() && isHuman.contains(true)) {
            processBuilder.addActor(actorName);
            if (addActorInitiator) {
                processBuilder.setActorInitiator(actorName);
            }
        }
        ActivityDefinitionBuilder activityDefinitionBuilder = null;
        for (int i = 0; i < stepNames.size(); i++) {
            final String stepName = stepNames.get(i);
            if (isHuman.get(i)) {
                if (activityDefinitionBuilder != null) {
                    activityDefinitionBuilder = activityDefinitionBuilder.addUserTask(stepName, actorName);
                } else {
                    activityDefinitionBuilder = processBuilder.addUserTask(stepName, actorName);
                }
            } else {
                if (activityDefinitionBuilder != null) {
                    activityDefinitionBuilder = activityDefinitionBuilder.addAutomaticTask(stepName);
                } else {
                    activityDefinitionBuilder = processBuilder.addAutomaticTask(stepName);
                }
            }
        }
        TransitionDefinitionBuilder transitionDefinitionBuilder = null;
        if (!parallelActivities) {
            for (int i = 0; i < stepNames.size() - 1; i++) {
                if (transitionDefinitionBuilder != null) {
                    transitionDefinitionBuilder = transitionDefinitionBuilder.addTransition(stepNames.get(i), stepNames.get(i + 1));
                } else {
                    assert activityDefinitionBuilder != null;
                    transitionDefinitionBuilder = activityDefinitionBuilder.addTransition(stepNames.get(i), stepNames.get(i + 1));
                }
            }
        }
        final DesignProcessDefinition processDefinition;
        if (transitionDefinitionBuilder == null) {
            processDefinition = processBuilder.done();
        } else {
            processDefinition = transitionDefinitionBuilder.getProcess();
        }
        return processDefinition;
    }

    public static DesignProcessDefinition createProcessDefinitionWithHumanAndAutomaticSteps(final String processName, final String processVersion,
            final List<String> stepNames, final List<Boolean> isHuman, final String actorName, final boolean addActorInitiator)
            throws InvalidProcessDefinitionException {
        return createProcessDefinitionWithHumanAndAutomaticSteps(processName, processVersion, stepNames, isHuman, actorName, addActorInitiator, false);
    }

    public static DesignProcessDefinition createProcessDefinitionWithHumanAndAutomaticSteps(final String processName, final String processVersion,
            final List<String> stepNames, final List<Boolean> isHuman) throws InvalidProcessDefinitionException {
        return createProcessDefinitionWithHumanAndAutomaticSteps(processName, processVersion, stepNames, isHuman, ACTOR_NAME, false);
    }

    public static DesignProcessDefinition createProcessDefinitionWithHumanAndAutomaticSteps(final List<String> stepNames, final List<Boolean> isHuman)
            throws InvalidProcessDefinitionException {
        return createProcessDefinitionWithHumanAndAutomaticSteps(PROCESS_NAME, PROCESS_VERSION, stepNames, isHuman, ACTOR_NAME, false);
    }

    public List<ProcessDefinition> createNbProcessDefinitionWithHumanAndAutomaticAndDeployWithActor(final int nbProcess, final User user,
            final List<String> stepNames, final List<Boolean> isHuman) throws InvalidProcessDefinitionException, BonitaException {
        final List<ProcessDefinition> processDefinitions = new ArrayList<ProcessDefinition>();
        for (int i = 0; i < nbProcess; i++) {
            String processName = PROCESS_NAME;
            if (i >= 0 && i < 10) {
                processName += "0";
            }
            final DesignProcessDefinition designProcessDefinition = createProcessDefinitionWithHumanAndAutomaticSteps(processName + i, PROCESS_VERSION + i,
                    stepNames, isHuman);
            processDefinitions.add(deployAndEnableWithActor(designProcessDefinition, ACTOR_NAME, user));
        }
        return processDefinitions;
    }

    public List<ProcessDefinition> createNbProcessDefinitionWithHumanAndAutomaticAndDeploy(final int nbProcess, final List<String> stepNames,
            final List<Boolean> isHuman) throws InvalidProcessDefinitionException, BonitaException {
        final List<ProcessDefinition> processDefinitions = new ArrayList<ProcessDefinition>();
        for (int i = 0; i < nbProcess; i++) {
            String processName = PROCESS_NAME;
            if (i >= 0 && i < 10) {
                processName += "0";
            }
            final DesignProcessDefinition designProcessDefinition = createProcessDefinitionWithHumanAndAutomaticSteps(processName + i, PROCESS_VERSION + i,
                    stepNames, isHuman);
            processDefinitions.add(deployAndEnableProcess(designProcessDefinition));
        }
        return processDefinitions;
    }

    public User createUserOnDefaultTenant(final String userName, final String password) throws BonitaException {
        final APISession session = loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        final User user = identityAPI.createUser(userName, password);
        assertNull(user.getLastConnection());
        logoutTenant(session);
        return user;
    }

    public DesignProcessDefinition createProcessDefinitionWithActorAndThreeHumanStepsAndThreeTransition() throws InvalidProcessDefinitionException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME).addDescription(DESCRIPTION);

        // 1 instance of process def:
        return processBuilder.addAutomaticTask("step1").addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME).addUserTask("step4", ACTOR_NAME)
                .addTransition("step1", "step2").addTransition("step1", "step3").addTransition("step1", "step4").getProcess();
    }

    @Deprecated
    public void checkNbOfOpenTasks(final ProcessInstance processInstance, final String message, final int expectedNbOfOpenActivities) throws Exception {
        final CheckNbOfOpenActivities checkNbOfOpenActivities = new CheckNbOfOpenActivities(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, false, processInstance,
                expectedNbOfOpenActivities, getProcessAPI());
        checkNbOfOpenActivities.waitUntil();
        assertEquals(message, expectedNbOfOpenActivities, checkNbOfOpenActivities.getNumberOfOpenActivities());
    }

    @Deprecated
    public CheckNbPendingTaskOf checkNbPendingTaskOf(final User user) throws Exception {
        return checkNbPendingTaskOf(1, user);
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
    public CheckNbOfActivities checkNbOfActivitiesInReadyState(final ProcessInstance processInstance, final int nbActivities) throws Exception {
        return checkNbOfActivitiesInInterruptingState(processInstance, nbActivities, TestStates.getReadyState());
    }

    @Deprecated
    public CheckNbOfActivities checkNbOfActivitiesInInterruptingState(final ProcessInstance processInstance, final int nbActivities) throws Exception {
        return checkNbOfActivitiesInInterruptingState(processInstance, nbActivities, TestStates.getInterruptingState());
    }

    @Deprecated
    public CheckNbOfActivities checkNbOfActivitiesInInterruptingState(final ProcessInstance processInstance, final int nbActivities, final String state)
            throws Exception {
        final CheckNbOfActivities checkNbOfActivities = new CheckNbOfActivities(getProcessAPI(), DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, false, processInstance,
                nbActivities, state);
        assertTrue("Expected " + nbActivities + " activities in " + state + " state", checkNbOfActivities.waitUntil());
        return checkNbOfActivities;
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
    public CheckNbOfHumanTasks checkNbOfHumanTasks(final int nbHumanTaks) throws Exception {
        return checkNbOfHumanTasks(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, nbHumanTaks);
    }

    @Deprecated
    private CheckNbOfHumanTasks checkNbOfHumanTasks(final int repeatEach, final int timeout, final int nbHumanTaks) throws Exception {
        final CheckNbOfHumanTasks checkNbOfHumanTasks = new CheckNbOfHumanTasks(repeatEach, timeout, false, nbHumanTaks, new SearchOptionsBuilder(0, 15)
                .filter(HumanTaskInstanceSearchDescriptor.STATE_NAME, ActivityStates.READY_STATE).sort(HumanTaskInstanceSearchDescriptor.NAME, Order.DESC)
                .done(), getProcessAPI());
        final boolean waitUntil = checkNbOfHumanTasks.waitUntil();
        assertTrue("Expected " + nbHumanTaks + " Human tasks in ready state, but found " + checkNbOfHumanTasks.getHumanTaskInstances().getCount(), waitUntil);
        return checkNbOfHumanTasks;
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
                nbAbortedActivities, TestStates.getAbortedState());
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

    public SearchOptionsBuilder buildSearchOptions(final long processDefId, final int pageIndex, final int numberOfResults, final String orderByField,
            final Order order) {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(pageIndex, numberOfResults);
        builder.filter(ProcessInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDefId);
        builder.sort(orderByField, order);
        return builder;
    }

    public SearchOptionsBuilder buildSearchOptions(final int pageIndex, final int numberOfResults, final String orderByField, final Order order) {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(pageIndex, numberOfResults);
        builder.sort(orderByField, order);
        return builder;
    }

    public Operation buildStringOperation(final String dataInstanceName, final String newConstantValue, final boolean isTransient)
            throws InvalidExpressionException {
        final LeftOperand leftOperand = new LeftOperandBuilder().createNewInstance().setName(dataInstanceName)
                .setType(isTransient ? LeftOperand.TYPE_TRANSIENT_DATA : LeftOperand.TYPE_DATA).done();
        final Expression expression = new ExpressionBuilder().createConstantStringExpression(newConstantValue);
        final Operation operation;
        operation = new OperationBuilder().createNewInstance().setOperator("=").setLeftOperand(leftOperand).setType(OperatorType.ASSIGNMENT)
                .setRightOperand(expression).done();
        return operation;
    }

    public Operation buildIntegerOperation(final String dataInstanceName, final int newConstantValue) throws InvalidExpressionException {
        final LeftOperand leftOperand = new LeftOperandBuilder().createNewInstance().setName(dataInstanceName).done();
        final Expression expression = new ExpressionBuilder().createConstantIntegerExpression(newConstantValue);
        final Operation operation;
        operation = new OperationBuilder().createNewInstance().setOperator("=").setLeftOperand(leftOperand).setType(OperatorType.ASSIGNMENT)
                .setRightOperand(expression).done();
        return operation;
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
        final Operation stringOperation = buildStringOperation(dataName, updatedValue, isTransient);
        final List<Operation> operations = new ArrayList<Operation>();
        operations.add(stringOperation);
        getProcessAPI().updateActivityInstanceVariables(operations, activityInstanceId, null);
    }

    public Operation buildOperation(final String dataName, final boolean isTransient, final OperatorType operatorType, final String operator,
            final Expression rightOperand) {
        final OperationBuilder operationBuilder = new OperationBuilder().createNewInstance();
        operationBuilder.setOperator(operator);
        operationBuilder.setRightOperand(rightOperand);
        operationBuilder.setType(operatorType);
        operationBuilder.setLeftOperand(new LeftOperandBuilder().createNewInstance(dataName)
                .setType(isTransient ? LeftOperand.TYPE_TRANSIENT_DATA : LeftOperand.TYPE_DATA).done());
        return operationBuilder.done();
    }

    public List<String> checkNoCategories() throws DeletionException {
        final List<String> messages = new ArrayList<String>();
        final long numberOfCategories = getProcessAPI().getNumberOfCategories();
        if (numberOfCategories > 0) {
            final List<Category> categories = getProcessAPI().getCategories(0, 5000, CategoryCriterion.NAME_ASC);
            final StringBuilder categoryBuilder = new StringBuilder("Categories are still present: ");
            for (final Category category : categories) {
                categoryBuilder.append(category.getName()).append(", ");
                getProcessAPI().deleteCategory(category.getId());
            }
            messages.add(categoryBuilder.toString());
        }
        return messages;
    }

    public List<String> checkNoFlowNodes() throws SearchException {
        final List<String> messages = new ArrayList<String>();
        final SearchOptionsBuilder build = new SearchOptionsBuilder(0, 1000);
        final SearchResult<FlowNodeInstance> searchResult = getProcessAPI().searchFlowNodeInstances(build.done());
        final List<FlowNodeInstance> flowNodeInstances = searchResult.getResult();
        if (searchResult.getCount() > 0) {
            final StringBuilder messageBuilder = new StringBuilder("FlowNodes are still present: ");
            for (final FlowNodeInstance flowNodeInstance : flowNodeInstances) {
                messageBuilder.append("{" + flowNodeInstance.getName() + " - ").append(flowNodeInstance.getType() + "}").append(", ");
            }
            messages.add(messageBuilder.toString());
        }
        return messages;
    }

    public List<String> checkNoArchivedFlowNodes() throws SearchException {
        final List<String> messages = new ArrayList<String>();
        final SearchOptionsBuilder build = new SearchOptionsBuilder(0, 1000);
        final SearchResult<ArchivedFlowNodeInstance> searchResult = getProcessAPI().searchArchivedFlowNodeInstances(build.done());
        final List<ArchivedFlowNodeInstance> archivedFlowNodeInstances = searchResult.getResult();
        if (searchResult.getCount() > 0) {
            final StringBuilder messageBuilder = new StringBuilder("Archived flowNodes are still present: ");
            for (final ArchivedFlowNodeInstance archivedFlowNodeInstance : archivedFlowNodeInstances) {
                messageBuilder.append(archivedFlowNodeInstance.getName()).append(", ");
            }
            messages.add(messageBuilder.toString());
        }
        return messages;
    }

    public List<String> checkNoComments() throws SearchException {
        final List<String> messages = new ArrayList<String>();
        final SearchOptionsBuilder build = new SearchOptionsBuilder(0, 1000);
        final SearchResult<Comment> searchResult = getProcessAPI().searchComments(build.done());
        final List<Comment> comments = searchResult.getResult();
        if (searchResult.getCount() > 0) {
            final StringBuilder messageBuilder = new StringBuilder("Comments are still present: ");
            for (final Comment comment : comments) {
                messageBuilder.append(comment.getContent()).append(", ");
            }
            messages.add(messageBuilder.toString());
        }
        return messages;
    }

    public List<String> checkNoArchivedComments() throws SearchException {
        final List<String> messages = new ArrayList<String>();
        final SearchOptionsBuilder build = new SearchOptionsBuilder(0, 1000);
        final SearchResult<ArchivedComment> searchResult = getProcessAPI().searchArchivedComments(build.done());
        final List<ArchivedComment> archivedComments = searchResult.getResult();
        if (searchResult.getCount() > 0) {
            final StringBuilder messageBuilder = new StringBuilder("Archived comments are still present: ");
            for (final ArchivedComment archivedComment : archivedComments) {
                messageBuilder.append(archivedComment.getName()).append(", ");
            }
            messages.add(messageBuilder.toString());
        }
        return messages;
    }

    public List<String> checkNoProcessDefinitions() throws DeletionException, ProcessDefinitionNotFoundException, ProcessActivationException {
        final List<String> messages = new ArrayList<String>();
        final List<ProcessDeploymentInfo> processes = getProcessAPI().getProcessDeploymentInfos(0, 200, ProcessDeploymentInfoCriterion.DEFAULT);
        if (processes.size() > 0) {
            final StringBuilder processBuilder = new StringBuilder("Process Definitions are still active: ");
            for (final ProcessDeploymentInfo processDeploymentInfo : processes) {
                processBuilder.append(processDeploymentInfo.getId()).append(", ");
                if (TestStates.getProcessDepInfoEnabledState().equals(processDeploymentInfo.getActivationState())) {
                    getProcessAPI().disableProcess(processDeploymentInfo.getProcessId());
                }
                getProcessAPI().deleteProcess(processDeploymentInfo.getProcessId());
            }
            messages.add(processBuilder.toString());
        }
        return messages;
    }

    public List<String> checkNoSupervisors() throws SearchException, DeletionException {
        final List<String> messages = new ArrayList<String>();
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 200);
        builder.sort(ProcessSupervisorSearchDescriptor.ID, Order.ASC);
        final List<ProcessSupervisor> supervisors = getProcessAPI().searchProcessSupervisors(builder.done()).getResult();

        if (supervisors.size() > 0) {
            final StringBuilder processBuilder = new StringBuilder("Process Supervisors are still active: ");
            for (final ProcessSupervisor supervisor : supervisors) {
                processBuilder.append(supervisor.getSupervisorId()).append(", ");
                getProcessAPI().deleteSupervisor(supervisor.getSupervisorId());
            }
            messages.add(processBuilder.toString());
        }
        return messages;
    }

    public List<String> checkNoProcessIntances() throws DeletionException {
        final List<String> messages = new ArrayList<String>();
        final List<ProcessInstance> processInstances = getProcessAPI().getProcessInstances(0, 1000, ProcessInstanceCriterion.DEFAULT);
        if (!processInstances.isEmpty()) {
            final StringBuilder stb = new StringBuilder("Process instances are still present: ");
            for (final ProcessInstance processInstance : processInstances) {
                stb.append(processInstance).append(", ");
                getProcessAPI().deleteProcessInstance(processInstance.getId());
            }
            messages.add(stb.toString());
        }
        return messages;
    }

    public List<String> checkNoArchivedProcessIntances() throws DeletionException {
        final List<String> messages = new ArrayList<String>();
        final List<ArchivedProcessInstance> archivedProcessInstances = getProcessAPI().getArchivedProcessInstances(0, 1000, ProcessInstanceCriterion.DEFAULT);
        if (!archivedProcessInstances.isEmpty()) {
            final StringBuilder stb = new StringBuilder("Archived process instances are still present: ");
            for (final ArchivedProcessInstance archivedProcessInstance : archivedProcessInstances) {
                stb.append(archivedProcessInstance).append(", ");
                getProcessAPI().deleteProcessInstance(archivedProcessInstance.getId());
            }
            messages.add(stb.toString());
        }
        return messages;
    }

    public List<String> checkNoGroups() throws DeletionException {
        final List<String> messages = new ArrayList<String>();
        final long numberOfGroups = getIdentityAPI().getNumberOfGroups();
        if (numberOfGroups > 0) {
            final List<Group> groups = getIdentityAPI().getGroups(0, Long.valueOf(numberOfGroups).intValue(), GroupCriterion.NAME_ASC);
            final StringBuilder groupBuilder = new StringBuilder("Groups are still present: ");
            for (final Group group : groups) {
                groupBuilder.append(group.getId()).append(", ");
                getIdentityAPI().deleteGroup(group.getId());
            }
            messages.add(groupBuilder.toString());
        }
        return messages;
    }

    public List<String> checkNoRoles() throws DeletionException {
        final List<String> messages = new ArrayList<String>();
        final long numberOfRoles = getIdentityAPI().getNumberOfRoles();
        if (numberOfRoles > 0) {
            final List<Role> roles = getIdentityAPI().getRoles(0, Long.valueOf(numberOfRoles).intValue(), RoleCriterion.NAME_ASC);
            final StringBuilder roleBuilder = new StringBuilder("Roles are still present: ");
            for (final Role role : roles) {
                roleBuilder.append(role.getId()).append(", ");
                getIdentityAPI().deleteRole(role.getId());
            }
            messages.add(roleBuilder.toString());
        }
        return messages;
    }

    public List<String> checkNoUsers() throws DeletionException {
        final List<String> messages = new ArrayList<String>();
        final long numberOfUsers = getIdentityAPI().getNumberOfUsers();
        if (numberOfUsers > 0) {
            final List<User> users = getIdentityAPI().getUsers(0, Long.valueOf(numberOfUsers).intValue(), UserCriterion.USER_NAME_ASC);
            final StringBuilder userBuilder = new StringBuilder("Users are still present: ");
            for (final User user : users) {
                userBuilder.append(user.getId()).append(", ");
                getIdentityAPI().deleteUser(user.getId());
            }
            messages.add(userBuilder.toString());
        }
        return messages;
    }

    public List<String> checkNoCommands() throws SearchException, CommandNotFoundException, DeletionException {
        final List<String> messages = new ArrayList<String>();
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 1000);
        searchOptionsBuilder.filter(CommandSearchDescriptor.SYSTEM, false);
        final SearchResult<CommandDescriptor> searchCommands = getCommandAPI().searchCommands(searchOptionsBuilder.done());
        final List<CommandDescriptor> commands = searchCommands.getResult();
        if (searchCommands.getCount() > 0) {
            final StringBuilder commandBuilder = new StringBuilder("Commands are still present: ");
            for (final CommandDescriptor command : commands) {
                commandBuilder.append(command.getName()).append(", ");
                getCommandAPI().unregister(command.getName());
            }
            messages.add(commandBuilder.toString());
        }
        return messages;
    }

    public ProcessAPI getProcessAPI() {
        return processAPI;
    }

    public void setProcessAPI(final ProcessAPI processAPI) {
        this.processAPI = processAPI;
    }

    public IdentityAPI getIdentityAPI() {
        return identityAPI;
    }

    public void setIdentityAPI(final IdentityAPI identityAPI) {
        this.identityAPI = identityAPI;
    }

    public CommandAPI getCommandAPI() {
        return commandAPI;
    }

    public void setCommandAPI(final CommandAPI commandAPI) {
        this.commandAPI = commandAPI;
    }

    public ProfileAPI getProfileAPI() {
        return profileAPI;
    }

    public void setProfileAPI(final ProfileAPI profileAPI) {
        this.profileAPI = profileAPI;
    }

    public ThemeAPI getThemeAPI() {
        return themeAPI;
    }

    public void setThemeAPI(final ThemeAPI themeAPI) {
        this.themeAPI = themeAPI;
    }

    public void setSession(final APISession session) {
        this.session = session;
    }

    public byte[] getConnectorImplementationFile(final String definitionId, final String definitionVersion, final String implementationId,
            final String implementationVersion, final String implementationClassname) {
        final StringBuilder stb = new StringBuilder();
        stb.append("<connectorImplementation>");
        stb.append("");
        stb.append("<definitionId>" + definitionId + "</definitionId>");
        stb.append("<definitionVersion>" + definitionVersion + "</definitionVersion>");
        stb.append("<implementationClassname>" + implementationClassname + "</implementationClassname>");
        stb.append("<implementationId>" + implementationId + "</implementationId>");
        stb.append("<implementationVersion>" + implementationVersion + "</implementationVersion>");
        stb.append("</connectorImplementation>");
        return stb.toString().getBytes();
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
        final Document doc = buildDocument(documentName);
        buildAndAttachDocument(processInstance, documentName, doc.getContentFileName());
    }

    public void buildAndAttachDocument(final ProcessInstance processInstance, final String documentName, final String fileName) throws BonitaException {
        final Document doc = buildDocument(documentName);
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

    public ProcessInstance deployAndEnableWithActorAndStartIt(final BusinessArchive businessArchive, final User user) throws BonitaException {
        final ProcessDefinition processDefinition = deployAndEnableWithActor(businessArchive, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        assertTrue(processInstance != null);
        return processInstance;
    }

    public Document buildDocument(final String documentName) {
        final String now = String.valueOf(System.currentTimeMillis());
        final String fileName = now + ".txt";
        final DocumentBuilder builder = new DocumentBuilder().createNewInstance(documentName, false);
        builder.setFileName(fileName);
        builder.setContentMimeType("plain/text");
        return builder.done();
    }

    public byte[] generateContent(final Document doc) {
        return doc.getName().getBytes();
    }

    /**
     * tell the engine to run BPMEventHandlingjob now
     * 
     * @throws CommandParameterizationException
     * @throws CommandExecutionException
     * @throws CommandNotFoundException
     */
    protected void forceMatchingOfEvents() throws CommandNotFoundException, CommandExecutionException, CommandParameterizationException {
        commandAPI.execute(ClientEventUtil.EXECUTE_EVENTS_COMMAND, Collections.<String, Serializable> emptyMap());
    }

}
