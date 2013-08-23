package org.bonitasoft.engine.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.api.ReportingAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.actor.ActorCriterion;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.actor.ActorNotFoundException;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.category.Category;
import org.bonitasoft.engine.bpm.category.CategoryCriterion;
import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ActivityStates;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.FlowNodeExecutionException;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstanceNotFoundException;
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
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoCriterion;
import org.bonitasoft.engine.bpm.process.ProcessEnablementException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceCriterion;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.bpm.process.impl.ActivityDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.TransitionDefinitionBuilder;
import org.bonitasoft.engine.bpm.supervisor.ProcessSupervisor;
import org.bonitasoft.engine.command.CommandDescriptor;
import org.bonitasoft.engine.command.CommandNotFoundException;
import org.bonitasoft.engine.command.CommandSearchDescriptor;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.GroupCreator;
import org.bonitasoft.engine.identity.GroupCriterion;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.RoleCreator;
import org.bonitasoft.engine.identity.RoleCriterion;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserCreator;
import org.bonitasoft.engine.identity.UserCriterion;
import org.bonitasoft.engine.identity.UserMembership;
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
import org.bonitasoft.engine.test.wait.WaitForEvent;
import org.bonitasoft.engine.test.wait.WaitForFinalArchivedActivity;
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

    protected static final Logger LOGGER = LoggerFactory.getLogger(APITestUtil.class);

    protected static final String SUPERVISOR_ID_KEY = "supervisorId";

    protected static final String ROLE_ID_KEY = "roleId";

    protected static final String GROUP_ID_KEY = "groupId";

    protected static final String PROCESS_DEFINITION_ID_KEY = "processDefinitionId";

    private APISession session;

    private ProcessAPI processAPI;

    private IdentityAPI identityAPI;

    private CommandAPI commandAPI;

    private ProfileAPI profileAPI;

    private ReportingAPI reportingAPI;

    public static final String DEFAULT_TENANT = "default";

    public static final String ACTOR_NAME = "Employee actor";

    public static final String PROCESS_VERSION = "1.0";

    public static final String PROCESS_NAME = "ProcessName";

    public static final String DESCRIPTION = "Coding all-night-long";

    public static final String PASSWORD = "bpm";

    public static final String USERNAME = "william.jobs";

    public static final int DEFAULT_REPEAT = 50;

    public static final int DEFAULT_TIMEOUT = 10000;

    @After
    public void clearSynchroRepository() throws Exception {
        try {
            login();
            ClientEventUtil.clearRepo(getCommandAPI());
            logout();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    protected void loginWith(final String userName, final String password) throws BonitaException {
        setSession(APITestUtil.loginDefaultTenant(userName, password));
        setIdentityAPI(TenantAPIAccessor.getIdentityAPI(getSession()));
        setProcessAPI(TenantAPIAccessor.getProcessAPI(getSession()));
        setCommandAPI(TenantAPIAccessor.getCommandAPI(getSession()));
        setProfileAPI(TenantAPIAccessor.getProfileAPI(getSession()));
        setReportingAPI(TenantAPIAccessor.getReportingAPI(getSession()));
    }

    protected void login() throws BonitaException {
        setSession(APITestUtil.loginDefaultTenant());
        setIdentityAPI(TenantAPIAccessor.getIdentityAPI(getSession()));
        setProcessAPI(TenantAPIAccessor.getProcessAPI(getSession()));
        setCommandAPI(TenantAPIAccessor.getCommandAPI(getSession()));
        setProfileAPI(TenantAPIAccessor.getProfileAPI(getSession()));
        setReportingAPI(TenantAPIAccessor.getReportingAPI(getSession()));
    }

    protected void logout() throws BonitaException {
        APITestUtil.logoutTenant(getSession());
        setSession(null);
        setIdentityAPI(null);
        setProcessAPI(null);
        setCommandAPI(null);
        setProfileAPI(null);
    }

    public void logoutThenlogin() throws BonitaException {
        logout();
        login();
    }

    public void logoutThenloginAs(final String userName, final String password) throws BonitaException {
        logout();
        loginWith(userName, password);
    }

    public User addMappingAndActor(final String actorName, final String userName, final String password, final ProcessDefinition definition)
            throws BonitaException {
        final User user = createUser(userName, password);
        addMappingOfActorsForUser(actorName, user.getId(), definition);
        return user;
    }

    public void addMappingOfActorsForUser(final String actorName, final long userId, final ProcessDefinition definition) throws BonitaException {
        getProcessAPI().addUserToActor(actorName, definition, userId);
    }

    public void addMappingOfActorsForGroup(final String actorName, final long groupId, final ProcessDefinition definition) throws BonitaException {
        getProcessAPI().addGroupToActor(actorName, groupId, definition);
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
    protected void addUserToFirstActorOfProcess(final long userId, final ProcessDefinition processDefinition) throws BonitaException {
        final List<ActorInstance> actors = getProcessAPI().getActors(processDefinition.getId(), 0, 1, ActorCriterion.NAME_ASC);
        final ActorInstance actor = actors.get(0);
        getProcessAPI().addUserToActor(actor.getId(), userId);
    }

    public ActorInstance getActor(final String actorName, final ProcessDefinition definition) throws ProcessDefinitionNotFoundException, ActorNotFoundException {
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

    protected User createUser(final String userName, final String password) throws AlreadyExistsException, CreationException {
        return getIdentityAPI().createUser(userName, password);
    }

    protected User createUser(final String userName, final String password, final String firstName, final String lastName) throws BonitaException {
        return getIdentityAPI().createUser(userName, password, firstName, lastName);
    }

    protected User createUser(final UserCreator creator) throws BonitaException {
        return getIdentityAPI().createUser(creator);
    }

    protected User createUser(final String userName, final long managerId) throws BonitaException {
        return createUser(userName, "bpm", managerId);
    }

    protected User createUser(final String userName, final String password, final long managerId) throws BonitaException {
        final UserCreator creator = new UserCreator(userName, password);
        creator.setManagerUserId(managerId);
        return getIdentityAPI().createUser(creator);
    }

    protected User createUserAndLogin(final String userName, final String password) throws BonitaException {
        final User user = getIdentityAPI().createUser(userName, password);
        logout();
        loginWith(userName, password);
        return user;
    }

    protected void deleteUsers(final User... users) throws BonitaException {
        deleteUsers(Arrays.asList(users));
    }

    public void deleteUsers(final List<User> users) throws BonitaException {
        if (users != null) {
            for (final User user : users) {
                getIdentityAPI().deleteUser(user.getId());
            }
        }
    }

    protected void deleteGroups(final Group... groups) throws BonitaException {
        deleteGroups(Arrays.asList(groups));
    }

    public void deleteGroups(final List<Group> groups) throws BonitaException {
        if (groups != null) {
            for (final Group group : groups) {
                getIdentityAPI().deleteGroup(group.getId());
            }
        }
    }

    protected void deleteRoles(final Role... roles) throws BonitaException {
        deleteRoles(Arrays.asList(roles));
    }

    public void deleteRoles(final List<Role> roles) throws BonitaException {
        if (roles != null) {
            for (final Role role : roles) {
                getIdentityAPI().deleteRole(role.getId());
            }
        }
    }

    protected void deleteUserMembership(final long id) throws BonitaException {
        getIdentityAPI().deleteUserMembership(id);
    }

    protected void deleteUserMemberships(final UserMembership... userMemberships) throws BonitaException {
        deleteUserMemberships(Arrays.asList(userMemberships));
    }

    public void deleteUserMemberships(final List<UserMembership> userMemberships) throws BonitaException {
        if (userMemberships != null) {
            for (final UserMembership userMembership : userMemberships) {
                getIdentityAPI().deleteUserMembership(userMembership.getId());
            }
        }
    }

    protected UserMembership createUserMembership(final String userName, final String roleName, final String groupName) throws BonitaException {
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

    protected ProcessSupervisor createSupervisor(final long processDefID, final long userId) throws BonitaException {
        return getProcessAPI().createProcessSupervisorForUser(processDefID, userId);
    }

    private ProcessSupervisor createSupervisorByRole(final long processDefID, final long roleId) throws BonitaException {
        return getProcessAPI().createProcessSupervisorForRole(processDefID, roleId);
    }

    private ProcessSupervisor createSupervisorByGroup(final long processDefID, final long groupId) throws BonitaException {
        return getProcessAPI().createProcessSupervisorForGroup(processDefID, groupId);
    }

    public Map<String, Object> createSupervisorByRoleAndGroup(final long processDefinitionId, final long userId) throws BonitaException {
        // add supervisor by role
        final String developer = "developer";
        final RoleCreator roleCreator = new RoleCreator(developer);
        final Role role = getIdentityAPI().createRole(roleCreator);
        final ProcessSupervisor supervisorByRole = createSupervisorByRole(processDefinitionId, role.getId());

        // add supervisor group
        final Group group = getIdentityAPI().createGroup("R&D", null);
        final ProcessSupervisor supervisorByGroup = createSupervisorByGroup(processDefinitionId, group.getId());

        // add supervisor membership
        final UserMembership membership = getIdentityAPI().addUserMembership(userId, group.getId(), role.getId());

        final HashMap<String, Object> parameters1 = new HashMap<String, Object>();
        parameters1.put(ROLE_ID_KEY, role);
        parameters1.put(GROUP_ID_KEY, group);
        parameters1.put("supervisorByRole", supervisorByRole);
        parameters1.put("supervisorByGroup", supervisorByGroup);
        parameters1.put("membership", membership);
        return parameters1;
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

    protected void deleteUserAndProcess(final String johnName, final ProcessDefinition definition) throws BonitaException {
        deleteUser(johnName);
        deleteProcess(definition);
    }

    protected void deleteProcess(final ProcessDefinition definition) throws BonitaException {
        deleteProcess(definition.getId());
    }

    protected void deleteProcess(final ProcessDefinition... processDefinitions) throws BonitaException {
        deleteProcess(Arrays.asList(processDefinitions));
    }

    protected void deleteProcess(final List<ProcessDefinition> processDefinitions) throws BonitaException {
        if (processDefinitions != null) {
            for (final ProcessDefinition processDefinition : processDefinitions) {
                deleteProcess(processDefinition);
            }
        }
    }

    protected void deleteProcess(final long processDefinitionId) throws BonitaException {
        getProcessAPI().deleteProcess(processDefinitionId);
    }

    protected void deleteUser(final String userName) throws BonitaException {
        getIdentityAPI().deleteUser(userName);
    }

    protected void deleteUser(final User user) throws BonitaException {
        deleteUser(user.getId());
    }

    protected void deleteUser(final long userId) throws BonitaException {
        getIdentityAPI().deleteUser(userId);
    }

    public void deleteCategories(final List<Category> categories) throws BonitaException {
        for (final Category category : categories) {
            getProcessAPI().deleteCategory(category.getId());
        }
    }

    protected ProcessDefinition deployAndEnableProcess(final DesignProcessDefinition designProcessDefinition) throws BonitaException {
        return getProcessAPI().deployAndEnableProcess(designProcessDefinition);
    }

    protected ProcessDefinition deployAndEnableProcess(final BusinessArchive businessArchive) throws BonitaException {
        return getProcessAPI().deployAndEnableProcess(businessArchive);
    }

    protected ProcessDefinition deployAndEnableWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final User user)
            throws BonitaException {
        return deployAndEnableWithActor(designProcessDefinition, Arrays.asList(actorName), Arrays.asList(user));
    }

    protected ProcessDefinition deployAndEnableWithActorAndParameters(final DesignProcessDefinition designProcessDefinition, final String actorName,
            final User user, final Map<String, String> parameters) throws BonitaException {
        return deployAndEnableWithActorAndParameters(designProcessDefinition, Arrays.asList(actorName), Arrays.asList(user), parameters);
    }

    protected ProcessDefinition deployAndEnableWithActor(final DesignProcessDefinition designProcessDefinition, final List<String> actorsName,
            final List<User> users) throws BonitaException {
        final ProcessDefinition processDefinition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        for (int i = 0; i < users.size(); i++) {
            addMappingOfActorsForUser(actorsName.get(i), users.get(i).getId(), processDefinition);
        }
        try {
            getProcessAPI().enableProcess(processDefinition.getId());
        } catch (final ProcessEnablementException e) {
            final List<Problem> problems = getProcessAPI().getProcessResolutionProblems(processDefinition.getId());
            throw new ProcessEnablementException("not resolved: " + problems);
        }
        return processDefinition;
    }

    protected ProcessDefinition deployAndEnableWithActor(final BusinessArchive businessArchive, final List<String> actorsName, final List<User> users)
            throws BonitaException {
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);
        for (int i = 0; i < users.size(); i++) {
            addMappingOfActorsForUser(actorsName.get(i), users.get(i).getId(), processDefinition);
        }
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    protected ProcessDefinition deployAndEnableWithActor(final BusinessArchive businessArchive, final String actorsName, final User user)
            throws BonitaException {
        return deployAndEnableWithActor(businessArchive, Collections.singletonList(actorsName), Collections.singletonList(user));
    }

    protected ProcessDefinition deployAndEnableWithActorAndParameters(final DesignProcessDefinition designProcessDefinition, final List<String> actorsName,
            final List<User> users, final Map<String, String> parameters) throws BonitaException {
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        final BusinessArchive businessArchive = businessArchiveBuilder.setParameters(parameters).setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);
        for (int i = 0; i < users.size(); i++) {
            addMappingOfActorsForUser(actorsName.get(i), users.get(i).getId(), processDefinition);
        }
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    protected ProcessDefinition deployAndEnableWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final Group group)
            throws BonitaException {
        final ProcessDefinition processDefinition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        addMappingOfActorsForGroup(actorName, group.getId(), processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    protected ProcessDefinition deployAndEnableWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final Group... groups)
            throws BonitaException {
        final ProcessDefinition processDefinition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        for (final Group group : groups) {
            addMappingOfActorsForGroup(actorName, group.getId(), processDefinition);
        }
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    protected ProcessDefinition deployAndEnableWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final Role role)
            throws BonitaException {
        final ProcessDefinition processDefinition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        addMappingOfActorsForRole(actorName, role.getId(), processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    protected ProcessDefinition deployAndEnableWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final Role... roles)
            throws BonitaException {
        final ProcessDefinition processDefinition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        for (final Role role : roles) {
            addMappingOfActorsForRole(actorName, role.getId(), processDefinition);
        }
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    protected ProcessDefinition deployAndEnableWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final Role role,
            final Group group) throws BonitaException {
        final ProcessDefinition processDefinition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        addMappingOfActorsForRoleAndGroup(actorName, role.getId(), group.getId(), processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    protected void disableAndDeleteProcess(final ProcessDefinition processDefinition) throws BonitaException {
        final ProcessDeploymentInfo deploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        if (deploymentInfo.getActivationState().equals(ActivationState.ENABLED)) {
            disableAndDeleteProcess(processDefinition.getId());
        } else {
            deleteProcess(processDefinition);
        }
    }

    protected void disableAndDeleteProcess(final long processDefinitionId) throws BonitaException {
        getProcessAPI().disableAndDelete(processDefinitionId);
    }

    protected void disableAndDeleteProcess(final ProcessDefinition... processDefinitions) throws BonitaException {
        disableAndDeleteProcess(Arrays.asList(processDefinitions));
    }

    protected void disableAndDeleteProcess(final List<ProcessDefinition> processDefinitions) throws BonitaException {
        if (processDefinitions != null) {
            for (final ProcessDefinition processDefinition : processDefinitions) {
                disableAndDeleteProcess(processDefinition);
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

    protected Group createGroup(final String groupName) throws AlreadyExistsException, CreationException {
        return createGroup(groupName, null);
    }

    protected Group createGroup(final String groupName, final String parentGroupPath) throws AlreadyExistsException, CreationException {
        return getIdentityAPI().createGroup(groupName, parentGroupPath);
    }

    protected Group createGroup(final String name, final String displayName, final String description) throws AlreadyExistsException, CreationException {
        final GroupCreator groupCreator = new GroupCreator(name);
        groupCreator.setDisplayName(displayName).setDescription(description);
        return getIdentityAPI().createGroup(groupCreator);
    }

    protected Role createRole(final String roleName) throws BonitaException {
        return getIdentityAPI().createRole(roleName);
    }

    protected Operation buildAssignOperation(final String dataInstanceName, final String expressionContent, final ExpressionType expressionType,
            final String returnType) throws InvalidExpressionException {
        final LeftOperand leftOperand = new LeftOperandBuilder().createNewInstance().setName(dataInstanceName).done();
        final Expression expression = new ExpressionBuilder().createNewInstance(dataInstanceName).setContent(expressionContent)
                .setExpressionType(expressionType.name()).setReturnType(returnType).done();
        final Operation operation;
        operation = new OperationBuilder().createNewInstance().setOperator("=").setLeftOperand(leftOperand).setType(OperatorType.ASSIGNMENT)
                .setRightOperand(expression).done();
        return operation;
    }

    protected void assignAndExecuteStep(final ActivityInstance activityInstance, final long userId) throws BonitaException {
        assignAndExecuteStep(activityInstance.getId(), userId);
    }

    public void assignAndExecuteStep(final long activityInstanceId, final long userId) throws BonitaException {
        getProcessAPI().assignUserTask(activityInstanceId, userId);
        executeFlowNodeUntilEnd(activityInstanceId);
    }

    protected void executeFlowNodeUntilEnd(final long flowNodeId) throws FlowNodeExecutionException {
        getProcessAPI().executeFlowNode(flowNodeId);
    }

    protected ActivityInstance waitForUserTask(final String taskName, final ProcessInstance processInstance) throws TimeoutException, Exception {
        return waitForUserTask(taskName, processInstance, DEFAULT_TIMEOUT);
    }

    protected ActivityInstance waitForUserTask(final String taskName, final ProcessInstance processInstance, final int timeout) throws TimeoutException,
            Exception {
        final Long waitForTask = ClientEventUtil.executeWaitServerCommand(getCommandAPI(),
                ClientEventUtil.getReadyTaskEvent(processInstance.getId(), taskName), timeout);
        return getActivityInstance(waitForTask);
    }

    protected ActivityInstance waitForUserTask(final String taskName, final long processInstanceId) throws Exception {
        return waitForUserTask(taskName, processInstanceId, DEFAULT_TIMEOUT);
    }

    protected ActivityInstance waitForUserTask(final String taskName, final long processInstance, final int timeout) throws Exception {
        final Long waitForTask = ClientEventUtil.executeWaitServerCommand(getCommandAPI(), ClientEventUtil.getReadyTaskEvent(processInstance, taskName),
                timeout);
        return getActivityInstance(waitForTask);
    }

    protected ActivityInstance waitForUserTask(final String taskName) throws Exception {
        return waitForUserTask(taskName, DEFAULT_TIMEOUT);
    }

    protected ActivityInstance waitForUserTask(final String taskName, final int timeout) throws Exception {
        final Long waitForTask = ClientEventUtil.executeWaitServerCommand(getCommandAPI(), ClientEventUtil.getReadyTaskEvent(taskName), timeout);
        return getActivityInstance(waitForTask);
    }

    private ActivityInstance getActivityInstance(final Long id) throws ActivityInstanceNotFoundException, RetrieveException {
        if (id != null) {
            return getProcessAPI().getActivityInstance(id);
        } else {
            throw new RuntimeException("no id returned for task ");
        }
    }

    private FlowNodeInstance getFlowNodeInstance(final Long id) throws ActivityInstanceNotFoundException, RuntimeException {
        try {
            return getProcessAPI().getFlowNodeInstance(id);
        } catch (final FlowNodeInstanceNotFoundException e) {
            throw new RuntimeException("no id returned for task ");
        }
    }

    protected void deleteSupervisor(final long id) throws BonitaException {
        getProcessAPI().deleteSupervisor(id);
    }

    protected ProcessDefinition createProcessDefinition() throws Exception {
        final String actorName = "Night coders";
        final String actorDescription = "Coding all-night-long";

        final ProcessDefinitionBuilder processBuilder1 = new ProcessDefinitionBuilder().createNewInstance("My_Process_with_branches", "1.0");
        processBuilder1.addActor(actorName).addDescription(actorDescription);
        final DesignProcessDefinition designProcessDefinition1 = processBuilder1.addUserTask("step2", actorName).getProcess();
        final BusinessArchive businessArchive1 = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition1).done();
        return getProcessAPI().deploy(businessArchive1);
    }

    protected List<HumanTaskInstance> waitForPendingTasks(final long userId, final int nbPendingTasks) throws Exception {
        final WaitForPendingTasks waitUntil = new WaitForPendingTasks(DEFAULT_REPEAT, DEFAULT_TIMEOUT, nbPendingTasks, userId, getProcessAPI());
        assertTrue("no pending user task instances are found", waitUntil.waitUntil());
        return waitUntil.getResults();
    }

    protected StartProcessUntilStep startProcessAndWaitForTask(final long processDefinitionId, final String taskName) throws Exception {
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinitionId);
        final ActivityInstance task = waitForUserTask(taskName, processInstance);
        return new StartProcessUntilStep(processInstance, task);
    }

    protected ArchivedActivityInstance waitForArchivedActivity(final long activityId, final String stateName) throws Exception {
        final WaitForArchivedActivity waitForArchivedActivity = new WaitForArchivedActivity(100, 10000, activityId, stateName, getProcessAPI());
        waitForArchivedActivity.waitUntil();
        final ArchivedActivityInstance archivedActivityInstance = waitForArchivedActivity.getArchivedActivityInstance();
        assertNotNull(archivedActivityInstance);
        assertEquals(stateName, archivedActivityInstance.getState());
        return archivedActivityInstance;
    }

    protected Long waitForFlowNode(final long processInstanceId, final String state, final String flowNodeName, final boolean useRootProcessInstance,
            final int timeout) throws Exception {
        Map<String, Serializable> params;
        if (useRootProcessInstance) {
            params = ClientEventUtil.getTaskInState(processInstanceId, state, flowNodeName);
        } else {
            params = ClientEventUtil.getTaskInStateWithParentId(processInstanceId, state, flowNodeName);
        }
        return ClientEventUtil.executeWaitServerCommand(getCommandAPI(), params, timeout);
    }

    protected void waitForFlowNodeInReadyState(final ProcessInstance processInstance, final String flowNodeName, final boolean useRootProcessInstance)
            throws Exception {
        final Long flowNodeInstanceId = waitForFlowNode(processInstance.getId(), TestStates.getReadyState(flowNodeName), flowNodeName, useRootProcessInstance,
                DEFAULT_TIMEOUT);
        final FlowNodeInstance flowNodeInstance = getProcessAPI().getFlowNodeInstance(flowNodeInstanceId);
        assertNotNull(flowNodeInstance);
    }

    protected void waitForProcessToFinish(final int repeatEach, final int timeout, final ProcessInstance processInstance, final String state) throws Exception {
        final WaitProcessToFinishAndBeArchived waitProcessToFinishAndBeArchived = new WaitProcessToFinishAndBeArchived(repeatEach, timeout, false,
                processInstance, getProcessAPI(), state);
        assertTrue(waitProcessToFinishAndBeArchived.waitUntil());
    }

    protected void waitForProcessToFinish(final ProcessInstance processInstance) throws Exception {
        waitForProcessToFinish(processInstance.getId());
    }

    protected void waitForProcessToFinish(final ProcessInstance processInstance, final int timeout) throws Exception {
        waitForProcessToFinish(processInstance.getId(), timeout);
    }

    protected void waitForProcessToFinish(final long processInstanceId) throws Exception {
        waitForProcessToFinish(processInstanceId, 10000);
    }

    protected void waitForProcessToFinish(final long processInstanceId, final int timeout) throws Exception {
        ClientEventUtil.executeWaitServerCommand(getCommandAPI(), ClientEventUtil.getProcessInstanceFinishedEvent(processInstanceId), timeout);
    }

    protected void waitForProcessToBeInState(final ProcessInstance processInstance, final ProcessInstanceState state) throws Exception {
        ClientEventUtil.executeWaitServerCommand(getCommandAPI(), ClientEventUtil.getProcessInstanceInState(processInstance.getId(), state.getId()), 7000);
    }

    protected void waitForProcessToFinish(final ProcessInstance processInstance, final String state) throws Exception {
        waitForProcessToFinish(50, 10000, processInstance, state);
    }

    protected boolean waitProcessToFinishAndBeArchived(final int repeatEach, final int timeout, final ProcessInstance processInstance) throws Exception {
        return new WaitProcessToFinishAndBeArchived(repeatEach, timeout, processInstance, processAPI).waitUntil();
    }

    protected boolean waitProcessToFinishAndBeArchived(final ProcessInstance processInstance) throws Exception {
        return waitProcessToFinishAndBeArchived(100, 7000, processInstance);
    }

    protected ActivityInstance waitForTaskToFail(final ProcessInstance processInstance) throws Exception {
        final Long activityId = ClientEventUtil.executeWaitServerCommand(getCommandAPI(),
                ClientEventUtil.getTaskInState(processInstance.getId(), TestStates.getFailedState()), DEFAULT_TIMEOUT);
        return getActivityInstance(activityId);
    }

    protected FlowNodeInstance waitForFlowNodeToFail(final ProcessInstance processInstance) throws Exception {
        final Long activityId = ClientEventUtil.executeWaitServerCommand(getCommandAPI(),
                ClientEventUtil.getTaskInState(processInstance.getId(), TestStates.getFailedState()), DEFAULT_TIMEOUT);
        return getFlowNodeInstance(activityId);
    }

    protected ActivityInstance waitForTaskInState(final String name, final ProcessInstance processInstance, final String state) throws Exception {
        final Long activityId = ClientEventUtil.executeWaitServerCommand(getCommandAPI(), ClientEventUtil.getTaskInState(processInstance.getId(), state, name),
                DEFAULT_TIMEOUT);
        return getActivityInstance(activityId);
    }

    protected void waitForUserTaskAndExecuteIt(final String taskName, final ProcessInstance processInstance, final long userId) throws Exception {
        final ActivityInstance waitForUserTask = waitForUserTask(taskName, processInstance);
        assignAndExecuteStep(waitForUserTask, userId);
    }

    public ActivityInstance waitForUserTaskAndAssigneIt(final String taskName, final ProcessInstance processInstance, final long userId) throws Exception,
            UpdateException {
        return waitForUserTaskAndAssigneIt(taskName, processInstance.getId(), userId);
    }

    public ActivityInstance waitForUserTaskAndAssigneIt(final String taskName, final long processInstanceId, final long userId) throws Exception,
            UpdateException {
        final ActivityInstance activityInstance = waitForUserTask(taskName, processInstanceId);
        getProcessAPI().assignUserTask(activityInstance.getId(), userId);
        return activityInstance;
    }

    public WaitForStep waitForStep(final String taskName, final ProcessInstance processInstance) throws Exception {
        return waitForStep(50, 3000, taskName, processInstance);
    }

    public WaitForStep waitForStep(final int repeatEach, final int timeout, final String taskName, final ProcessInstance processInstance) throws Exception {
        final WaitForStep waitForStep = new WaitForStep(repeatEach, timeout, taskName, processInstance.getId(), getProcessAPI());
        assertTrue("Task " + taskName + " not found", waitForStep.waitUntil());
        return waitForStep;
    }

    public WaitForStep waitForStep(final int repeatEach, final int timeout, final String taskName, final ProcessInstance processInstance, final String state)
            throws Exception {
        final WaitForStep waitForStep = new WaitForStep(repeatEach, timeout, taskName, processInstance.getId(), state, getProcessAPI());
        assertTrue("Task " + taskName + " not found", waitForStep.waitUntil());
        return waitForStep;
    }

    public WaitForActivity waitForActivity(final String activityName, final ProcessInstance processInstance) throws Exception {
        return waitForActivity(50, 1000, activityName, processInstance);
    }

    public WaitForActivity waitForActivity(final int repeatEach, final int timeout, final String activityName, final ProcessInstance processInstance)
            throws Exception {
        return waitForActivity(repeatEach, timeout, activityName, processInstance, null);
    }

    public WaitForActivity waitForActivity(final int repeatEach, final int timeout, final String activityName, final ProcessInstance processInstance,
            final String state) throws Exception {
        final WaitForActivity waitForActivity = new WaitForActivity(repeatEach, timeout, activityName, processInstance.getId(), state, getProcessAPI());
        assertTrue(waitForActivity.waitUntil());
        return waitForActivity;
    }

    public WaitForFinalArchivedActivity waitForFinalArchivedActivity(final String activityName, final ProcessInstance processInstance) throws Exception {
        final WaitForFinalArchivedActivity waitForFinalArchivedActivity = new WaitForFinalArchivedActivity(200, 1000, activityName, processInstance.getId(),
                getProcessAPI());
        assertTrue(activityName + " should be finished and archived", waitForFinalArchivedActivity.waitUntil());
        return waitForFinalArchivedActivity;
    }

    public WaitForEvent waitForEventInWaitingState(final ProcessInstance processInstance, final String eventName) throws Exception {
        return waitForEvent(processInstance, eventName, TestStates.getWaitingState());
    }

    public WaitForEvent waitForEventInWaitingState(final long processInstanceId, final String eventName) throws Exception {
        return waitForEvent(processInstanceId, eventName, TestStates.getWaitingState());
    }

    public WaitForEvent waitForEvent(final ProcessInstance processInstance, final String eventName, final String state) throws Exception {
        return waitForEvent(50, 5000, processInstance, eventName, state);
    }

    public WaitForEvent waitForEvent(final long processInstanceId, final String eventName, final String state) throws Exception {
        return waitForEvent(50, 5000, processInstanceId, eventName, state);
    }

    public WaitForEvent waitForEvent(final int repeatEach, final int timeout, final ProcessInstance processInstance, final String eventName, final String state)
            throws Exception {
        return waitForEvent(50, 5000, processInstance.getId(), eventName, state);
    }

    public WaitForEvent waitForEvent(final int repeatEach, final int timeout, final long processInstanceId, final String eventName, final String state)
            throws Exception {
        final WaitForEvent waitForEvent = new WaitForEvent(repeatEach, timeout, eventName, processInstanceId, state, getProcessAPI());
        assertTrue("Expected 1 activities in " + state + " state", waitForEvent.waitUntil());
        return waitForEvent;
    }

    public static void deletePlatformStructure() throws BonitaException {
        final PlatformLoginAPI platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
        final PlatformSession session = platformLoginAPI.login("platformAdmin", "platform");
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        platformAPI.deletePlatform();
        platformLoginAPI.logout(session);
    }

    public static void deleteStopAndCleanPlatformAndTenant(final boolean deployCommands) throws BonitaException {
        final PlatformSession session = loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        stopAndCleanPlatformAndTenant(platformAPI, deployCommands);
        platformAPI.deletePlatform();
        logoutPlatform(session);
    }

    public static void createPlatformStructure() throws BonitaException {
        final PlatformLoginAPI platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
        final PlatformSession session = platformLoginAPI.login("platformAdmin", "platform");
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        createPlatformStructure(platformAPI, false);
        platformLoginAPI.logout(session);
    }

    private static void createPlatformStructure(final PlatformAPI platformAPI, final boolean deployCommands) throws BonitaException {
        if (platformAPI.isPlatformCreated()) {
            if (PlatformState.STARTED.equals(platformAPI.getPlatformState())) {
                stopAndCleanPlatformAndTenant(deployCommands);
            }
            platformAPI.deletePlatform();
        }
        platformAPI.createPlatform();
    }

    public static void createInitializeAndStartPlatformWithDefaultTenant(final boolean deployCommands) throws BonitaException {
        final PlatformSession session = loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        createPlatformStructure(platformAPI, deployCommands);
        initializeAndStartPlatformWithDefaultTenant(platformAPI, deployCommands);
        logoutPlatform(session);
    }

    public static void initializeAndStartPlatformWithDefaultTenant(final boolean deployCommands) throws BonitaException {
        final PlatformSession session = loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        initializeAndStartPlatformWithDefaultTenant(platformAPI, deployCommands);
        logoutPlatform(session);
    }

    public static void initializeAndStartPlatformWithDefaultTenant(final PlatformAPI platformAPI, final boolean deployCommands) throws BonitaException {
        platformAPI.initializePlatform();
        platformAPI.startNode();

        if (deployCommands) {
            final APISession loginDefaultTenant = loginDefaultTenant();
            ClientEventUtil.deployCommand(loginDefaultTenant);
            logoutTenant(loginDefaultTenant);
        }
    }

    public static void stopAndCleanPlatformAndTenant(final boolean deployCommands) throws BonitaException {
        final PlatformSession session = loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        stopAndCleanPlatformAndTenant(platformAPI, deployCommands);
        logoutPlatform(session);
    }

    public static void stopAndCleanPlatformAndTenant(final PlatformAPI platformAPI, final boolean deployCommands) throws BonitaException {
        stopPlatformAndTenant(platformAPI, deployCommands);
        cleanPlatform(platformAPI);
    }

    public static void stopPlatformAndTenant(final PlatformAPI platformAPI, final boolean deployCommands) throws BonitaException {
        if (deployCommands) {
            final APISession loginDefaultTenant = loginDefaultTenant();
            ClientEventUtil.undeployCommand(loginDefaultTenant);
            logoutTenant(loginDefaultTenant);
        }

        platformAPI.stopNode();
    }

    public static void cleanPlatform(final PlatformAPI platformAPI) throws BonitaException {
        platformAPI.cleanPlatform();
    }

    public static PlatformSession loginPlatform() throws BonitaException {
        final PlatformLoginAPI platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
        return platformLoginAPI.login("platformAdmin", "platform");
    }

    public static PlatformLoginAPI getPlatformLoginAPI() throws BonitaException {
        return PlatformAPIAccessor.getPlatformLoginAPI();
    }

    public static void logoutPlatform(final PlatformSession session) throws BonitaException {
        final PlatformLoginAPI platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
        platformLoginAPI.logout(session);
    }

    public static APISession loginDefaultTenant() throws BonitaException {
        return loginDefaultTenant("install", "install");
    }

    public static APISession loginDefaultTenant(final String userName, final String password) throws BonitaException {
        final LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();
        return loginAPI.login(userName, password);
    }

    public static void logoutTenant(final APISession session) throws BonitaException {
        final LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();
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

    public static User createUserOnDefaultTenant(final String userName, final String password) throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        final User user = identityAPI.createUser(userName, password);
        assertNull(user.getLastConnection());
        APITestUtil.logoutTenant(session);
        return user;
    }

    public DesignProcessDefinition createProcessDefinitionWithActorAndThreeHumanStepsAndThreeTransition() throws InvalidProcessDefinitionException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME).addDescription(DESCRIPTION);

        // 1 instance of process def:
        return processBuilder.addAutomaticTask("step1").addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME).addUserTask("step4", ACTOR_NAME)
                .addTransition("step1", "step2").addTransition("step1", "step3").addTransition("step1", "step4").getProcess();
    }

    public void checkNbOfOpenTasks(final ProcessInstance processInstance, final String message, final int expectedNbOfOpenActivities) throws Exception {
        final CheckNbOfOpenActivities checkNbOfOpenActivities = new CheckNbOfOpenActivities(40, 5000, true, processInstance, expectedNbOfOpenActivities,
                getProcessAPI());
        checkNbOfOpenActivities.waitUntil();
        assertEquals(message, expectedNbOfOpenActivities, checkNbOfOpenActivities.getNumberOfOpenActivities());
    }

    public CheckNbPendingTaskOf checkNbPendingTaskOf(final User user) throws Exception {
        return checkNbPendingTaskOf(1, user);
    }

    public CheckNbPendingTaskOf checkNbPendingTaskOf(final int nbOfPendingTasks, final User user) throws Exception {
        return checkNbPendingTaskOf(50, 5000, false, nbOfPendingTasks, user);
    }

    public CheckNbPendingTaskOf checkNbPendingTaskOf(final int repeatEach, final int timeout, final boolean throwExceptions, final int nbOfPendingTasks,
            final User user) throws Exception {
        final CheckNbPendingTaskOf checkNbPendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), repeatEach, timeout, throwExceptions, nbOfPendingTasks,
                user);
        assertTrue("There isn't " + nbOfPendingTasks + " pending task", checkNbPendingTaskOf.waitUntil());
        return checkNbPendingTaskOf;
    }

    public void checkNbOfOpenActivities(final ProcessInstance processInstance, final int nbActivities) throws Exception {
        checkNbOfOpenActivities(40, 5000, processInstance, nbActivities);
    }

    public void checkNbOfOpenActivities(final int repeatEach, final int timeout, final ProcessInstance processInstance, final int nbActivities)
            throws Exception {
        assertTrue("Expected " + nbActivities + " OPEN activities for process instance", new CheckNbOfOpenActivities(repeatEach, timeout, true,
                processInstance, nbActivities, getProcessAPI()).waitUntil());
    }

    public CheckNbOfActivities checkNbOfActivitiesInReadyState(final ProcessInstance processInstance, final int nbActivities) throws Exception {
        return checkNbOfActivitiesInInterruptingState(processInstance, nbActivities, TestStates.getReadyState(null));
    }

    public CheckNbOfActivities checkNbOfActivitiesInInterruptingState(final ProcessInstance processInstance, final int nbActivities) throws Exception {
        return checkNbOfActivitiesInInterruptingState(processInstance, nbActivities, TestStates.getInterruptingState());
    }

    public CheckNbOfActivities checkNbOfActivitiesInInterruptingState(final ProcessInstance processInstance, final int nbActivities, final String state)
            throws Exception {
        final CheckNbOfActivities checkNbOfActivities = new CheckNbOfActivities(getProcessAPI(), 50, 5000, true, processInstance, nbActivities, state);
        assertTrue("Expected " + nbActivities + " activities in " + state + " state", checkNbOfActivities.waitUntil());
        return checkNbOfActivities;
    }

    public void checkProcessInstanceIsArchived(final ProcessInstance processInstance) throws Exception {
        checkProcessInstanceIsArchived(20, 3000, processInstance);
    }

    public void checkProcessInstanceIsArchived(final int repeatEach, final int timeout, final ProcessInstance processInstance) throws Exception {
        assertTrue(new CheckProcessInstanceIsArchived(repeatEach, timeout, processInstance.getId(), getProcessAPI()).waitUntil());
    }

    public CheckNbOfHumanTasks checkNbOfHumanTasks(final int nbHumanTaks) throws Exception {
        return checkNbOfHumanTasks(80, 3000, nbHumanTaks);
    }

    public CheckNbOfHumanTasks checkNbOfHumanTasks(final int repeatEach, final int timeout, final int nbHumanTaks) throws Exception {
        final CheckNbOfHumanTasks checkNbOfHumanTasks = new CheckNbOfHumanTasks(repeatEach, timeout, true, nbHumanTaks, new SearchOptionsBuilder(0, 15)
                .filter(HumanTaskInstanceSearchDescriptor.STATE_NAME, ActivityStates.READY_STATE).sort(HumanTaskInstanceSearchDescriptor.NAME, Order.DESC)
                .done(), getProcessAPI());
        final boolean waitUntil = checkNbOfHumanTasks.waitUntil();
        assertTrue("Expected " + nbHumanTaks + " Human tasks in ready state, but found " + checkNbOfHumanTasks.getHumanTaskInstances().getCount(), waitUntil);
        return checkNbOfHumanTasks;
    }

    public void checkNbOfArchivedActivityInstances(final ProcessInstance processInstance, final int expected) throws Exception {
        checkNbOfArchivedActivityInstances(20, 3000, processInstance, expected);
    }

    public void checkNbOfArchivedActivityInstances(final int repeatEach, final int timeout, final ProcessInstance processInstance, final int expected)
            throws Exception {
        assertTrue(new CheckNbOfArchivedActivityInstances(repeatEach, timeout, processInstance, expected, getProcessAPI()).waitUntil());
    }

    public void checkNbOfArchivedActivities(final ProcessInstance processInstance, final int nbAbortedActivities) throws Exception {
        checkNbOfArchivedActivities(100, 5000, processInstance, nbAbortedActivities);
    }

    public void checkNbOfArchivedActivities(final int repeatEach, final int timeout, final ProcessInstance processInstance, final int nbAbortedActivities)
            throws Exception {
        final CheckNbOfArchivedActivities checkNbOfActivities = new CheckNbOfArchivedActivities(getProcessAPI(), repeatEach, timeout, true, processInstance,
                nbAbortedActivities, TestStates.getAbortedState());
        final boolean waitUntil = checkNbOfActivities.waitUntil();
        assertTrue("Expected " + nbAbortedActivities + " in the aboted state. But was " + checkNbOfActivities.getResult().size(), waitUntil);
    }

    public CheckNbOfProcessInstances checkNbOfProcessInstances(final int nbOfProcInst) throws Exception {
        final CheckNbOfProcessInstances checkNbOfProcessInstances = new CheckNbOfProcessInstances(50, 5000, nbOfProcInst, getProcessAPI());
        assertTrue(checkNbOfProcessInstances.waitUntil());
        return checkNbOfProcessInstances;
    }

    public CheckNbOfProcessInstances checkNbOfProcessInstances(final int nbOfProcInst, final ProcessInstanceCriterion orderBy) throws Exception {
        return checkNbOfProcessInstances(50, 5000, nbOfProcInst, orderBy);
    }

    public CheckNbOfProcessInstances checkNbOfProcessInstances(final int repeatEach, final int timeout, final int nbOfProcInst,
            final ProcessInstanceCriterion orderBy) throws Exception {
        final CheckNbOfProcessInstances checkNbOfProcessInstances = new CheckNbOfProcessInstances(repeatEach, timeout, nbOfProcInst, orderBy, getProcessAPI());
        assertTrue(checkNbOfProcessInstances.waitUntil());
        return checkNbOfProcessInstances;
    }

    public void checkFlowNodeWasntExecuted(final long processInstancedId, final String flowNodeName) throws BonitaException {
        final List<ArchivedActivityInstance> archivedActivityInstances = getProcessAPI().getArchivedActivityInstances(processInstancedId, 0, 200,
                ActivityInstanceCriterion.DEFAULT);
        for (final ArchivedActivityInstance archivedActivityInstance : archivedActivityInstances) {
            assertFalse(flowNodeName.equals(archivedActivityInstance.getName()));
        }
    }

    protected void checkWasntExecuted(final ProcessInstance parentProcessInstance, final String flowNodeName) throws InvalidSessionException, SearchException {
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

    public Operation buildStringOperation(final String dataInstanceName, final String newConstantValue) throws InvalidExpressionException {
        final LeftOperand leftOperand = new LeftOperandBuilder().createNewInstance().setName(dataInstanceName).done();
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

    public void skipTask(final long activityId) throws Exception {
        getProcessAPI().setActivityStateByName(activityId, ActivityStates.SKIPPED_STATE);
    }

    public void updateActivityInstanceVariablesWithOperations(final String updatedValue, final long activityInstanceId, final String dataName)
            throws DataNotFoundException, InvalidExpressionException, UpdateException {
        final Operation stringOperation = buildStringOperation(dataName, updatedValue);
        final List<Operation> operations = new ArrayList<Operation>();
        operations.add(stringOperation);
        getProcessAPI().updateActivityInstanceVariables(operations, activityInstanceId, null);
    }

    public Operation buildOperation(final String dataName, final OperatorType operatorType, final String operator, final Expression rightOperand) {
        final OperationBuilder operationBuilder = new OperationBuilder().createNewInstance();
        operationBuilder.setOperator(operator);
        operationBuilder.setRightOperand(rightOperand);
        operationBuilder.setType(operatorType);
        operationBuilder.setLeftOperand(new LeftOperandBuilder().createDataLeftOperand(dataName));
        return operationBuilder.done();
    }

    public List<String> checkExistenceOfCategories() throws DeletionException {
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

    public List<String> checkExistenceOfFlowNodes() throws SearchException {
        final List<String> messages = new ArrayList<String>();
        final SearchOptionsBuilder build = new SearchOptionsBuilder(0, 1000);
        final SearchResult<FlowNodeInstance> searchEvents = getProcessAPI().searchFlowNodeInstances(build.done());
        final List<FlowNodeInstance> events = searchEvents.getResult();
        if (searchEvents.getCount() > 0) {
            final StringBuilder messageBuilder = new StringBuilder("FlowNodes are still present: ");
            for (final FlowNodeInstance event : events) {
                messageBuilder.append(event.getName()).append(", ");
            }
            messages.add(messageBuilder.toString());
        }
        return messages;
    }

    public List<String> checkExistenceOfProcessDefinitions() throws DeletionException, ProcessDefinitionNotFoundException, ProcessActivationException {
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

    public List<String> checkExistenceOfProcessIntances() throws DeletionException {
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

    public List<String> checkExistenceOfGroups() throws DeletionException {
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

    public List<String> checkExistenceOfRoles() throws DeletionException {
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

    public List<String> checkExistenceOfUsers() throws DeletionException {
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

    public List<String> checkExistenceOfCommands() throws SearchException, CommandNotFoundException, DeletionException {
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

    protected ProcessAPI getProcessAPI() {
        return processAPI;
    }

    protected void setProcessAPI(final ProcessAPI processAPI) {
        this.processAPI = processAPI;
    }

    protected IdentityAPI getIdentityAPI() {
        return identityAPI;
    }

    protected void setIdentityAPI(final IdentityAPI identityAPI) {
        this.identityAPI = identityAPI;
    }

    protected CommandAPI getCommandAPI() {
        return commandAPI;
    }

    protected void setCommandAPI(final CommandAPI commandAPI) {
        this.commandAPI = commandAPI;
    }

    protected ProfileAPI getProfileAPI() {
        return profileAPI;
    }

    protected void setProfileAPI(final ProfileAPI profileAPI) {
        this.profileAPI = profileAPI;
    }

    protected ReportingAPI getReportingAPI() {
        return reportingAPI;
    }

    protected void setReportingAPI(final ReportingAPI reportingAPI) {
        this.reportingAPI = reportingAPI;
    }

    protected void setSession(final APISession session) {
        this.session = session;
    }

}
