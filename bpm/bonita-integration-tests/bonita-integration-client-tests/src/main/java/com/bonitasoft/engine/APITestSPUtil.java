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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.api.ActorSorting;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.EventSorting;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.MigrationAPI;
import org.bonitasoft.engine.api.ProcessInstanceCriterion;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.model.ActivityInstance;
import org.bonitasoft.engine.bpm.model.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.model.ActorInstance;
import org.bonitasoft.engine.bpm.model.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.model.FlowNodeInstance;
import org.bonitasoft.engine.bpm.model.HumanTaskInstance;
import org.bonitasoft.engine.bpm.model.ProcessDefinition;
import org.bonitasoft.engine.bpm.model.ProcessInstance;
import org.bonitasoft.engine.bpm.model.StateCategory;
import org.bonitasoft.engine.bpm.model.archive.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.model.archive.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.model.event.EventInstance;
import org.bonitasoft.engine.core.operation.LeftOperand;
import org.bonitasoft.engine.core.operation.LeftOperandBuilder;
import org.bonitasoft.engine.core.operation.Operation;
import org.bonitasoft.engine.core.operation.OperationBuilder;
import org.bonitasoft.engine.core.operation.OperatorType;
import org.bonitasoft.engine.exception.ActivityExecutionErrorException;
import org.bonitasoft.engine.exception.ActivityExecutionFailedException;
import org.bonitasoft.engine.exception.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.exception.ActivityInterruptedException;
import org.bonitasoft.engine.exception.ActorNotFoundException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.DeletingEnabledProcessException;
import org.bonitasoft.engine.exception.GroupAlreadyExistException;
import org.bonitasoft.engine.exception.GroupCreationException;
import org.bonitasoft.engine.exception.InvalidExpressionException;
import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.exception.PageOutOfRangeException;
import org.bonitasoft.engine.exception.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.exception.ProcessDeletionException;
import org.bonitasoft.engine.exception.ProcessDisablementException;
import org.bonitasoft.engine.exception.UserAlreadyExistException;
import org.bonitasoft.engine.exception.UserCreationException;
import org.bonitasoft.engine.exception.UserDeletionException;
import org.bonitasoft.engine.exception.UserNotFoundException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.GroupBuilder;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.RoleBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserBuilder;
import org.bonitasoft.engine.identity.UserMembership;
import org.bonitasoft.engine.process.supervisor.ProcessSupervisor;
import org.bonitasoft.engine.search.FlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.search.HumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.WaitUntil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonitasoft.engine.api.LogAPI;
import com.bonitasoft.engine.api.ProcessAPI;
import com.bonitasoft.engine.api.TenantAPIAccessor;
import com.bonitasoft.engine.bpm.model.ProcessDefinitionBuilder;
import com.bonitasoft.engine.log.Log;

public class APITestSPUtil {

    protected static final Logger LOGGER = LoggerFactory.getLogger(APITestSPUtil.class);

    protected static final String SUPERVISOR_ID_KEY = "supervisorId";

    protected static final String ROLE_ID_KEY = "roleId";

    protected static final String GROUP_ID_KEY = "groupId";

    protected static final String PROCESS_DEFINITION_ID_KEY = "processDefinitionId";

    private static final String USER_ID_KEY = "userId";

    private APISession session;

    private ProcessAPI processAPI;

    private IdentityAPI identityAPI;

    private CommandAPI commandAPI;

    private LogAPI logAPI;

    private MigrationAPI migrationAPI;

    public static int DEFAULT_REPEAT = 50;

    public static int DEFAULT_TIMEOUT = 2000;

    /**
     * @author Baptiste Mesta
     */
    public final class WaitForPendingTasks extends WaitUntil {

        private final int nbPendingTasks;

        private final long userId;

        private List<HumanTaskInstance> results;

        public WaitForPendingTasks(final int repeatEach, final int timeout, final int nbPendingTasks, final long userId) {
            super(repeatEach, timeout);
            this.nbPendingTasks = nbPendingTasks;
            this.userId = userId;
        }

        @Override
        protected boolean check() throws Exception {
            results = getProcessAPI().getPendingHumanTaskInstances(userId, 0, 10, null);
            return results.size() == nbPendingTasks;
        }

        public List<HumanTaskInstance> getResults() {
            return results;
        }
    }

    public final class WaitProcessToFinishAndBeArchived extends WaitUntil {

        private final ProcessInstance processInstance;

        private final ProcessAPI processAPI;

        private final String state;

        public WaitProcessToFinishAndBeArchived(final int repeatEach, final int timeout, final boolean throwExceptions, final ProcessInstance processInstance,
                final ProcessAPI processAPI, final String state) {
            super(repeatEach, timeout, throwExceptions);
            this.processInstance = processInstance;
            this.processAPI = processAPI;
            this.state = state;
        }

        public WaitProcessToFinishAndBeArchived(final int repeatEach, final int timeout, final boolean throwExceptions, final ProcessInstance processInstance,
                final ProcessAPI processAPI) {
            this(repeatEach, timeout, throwExceptions, processInstance, processAPI, TestStates.getNormalFinalState(null));
        }

        private WaitProcessToFinishAndBeArchived(final int repeatEach, final int timeout, final ProcessInstance processInstance, final ProcessAPI processAPI) {
            this(repeatEach, timeout, false, processInstance, processAPI);
        }

        @Override
        protected boolean check() throws Exception {
            final List<ArchivedProcessInstance> archivedProcessInstances = processAPI.getArchivedProcessInstanceList(processInstance.getId(), 0, 200);
            return containsState(archivedProcessInstances, state);
        }

    }

    /**
     * @author Emmanuel Duchastenier
     */
    public final class CheckNbOfHumanTasks extends WaitUntil {

        private final long nbOfHumanTasks;

        private final SearchOptions searchOptions;

        private SearchResult<HumanTaskInstance> humanTaskInstances;

        public CheckNbOfHumanTasks(final int repeatEach, final int timeout, final boolean throwExceptions, final long nbOfHumanTasks,
                final SearchOptions searchOptions) {
            super(repeatEach, timeout, throwExceptions);
            this.nbOfHumanTasks = nbOfHumanTasks;
            this.searchOptions = searchOptions;
        }

        @Override
        protected boolean check() throws Exception {
            humanTaskInstances = getProcessAPI().searchHumanTaskInstances(searchOptions);
            return nbOfHumanTasks == humanTaskInstances.getCount();
        }

        public SearchResult<HumanTaskInstance> getHumanTaskInstances() {
            return humanTaskInstances;
        }
    }

    protected boolean isProcessInstanceFinishedAndArchived(final int repeatEach, final int timeout, final ProcessInstance processInstance,
            final ProcessAPI processAPI) throws Exception {
        return new WaitProcessToFinishAndBeArchived(repeatEach, timeout, processInstance, processAPI).waitUntil();
    }

    protected boolean isProcessInstanceFinishedAndArchived(final ProcessInstance processInstance) throws Exception {
        return isProcessInstanceFinishedAndArchived(50, 500, processInstance, getProcessAPI());
    }

    protected void loginWith(final String userName, final String password, final long tenantId) throws BonitaException {
        session = SPBPMTestUtil.loginTenant(userName, password, tenantId);
        identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        processAPI = TenantAPIAccessor.getProcessAPI(session);
        commandAPI = TenantAPIAccessor.getCommandAPI(session);
        migrationAPI = TenantAPIAccessor.getMigrationAPI(session);
    }

    protected void loginWith(final String userName, final String password) throws BonitaException {
        session = SPBPMTestUtil.loginOnDefaultTenant(userName, password);
        identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        processAPI = TenantAPIAccessor.getProcessAPI(session);
        commandAPI = TenantAPIAccessor.getCommandAPI(session);
        logAPI = TenantAPIAccessor.getLogAPI(session);
        migrationAPI = TenantAPIAccessor.getMigrationAPI(session);
    }

    protected void login() throws BonitaException {
        session = SPBPMTestUtil.loginOnDefaultTenant();
        identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        processAPI = TenantAPIAccessor.getProcessAPI(session);
        commandAPI = TenantAPIAccessor.getCommandAPI(session);
        logAPI = TenantAPIAccessor.getLogAPI(session);
        migrationAPI = TenantAPIAccessor.getMigrationAPI(session);
    }

    protected void login(final long tenantId) throws BonitaException {
        session = SPBPMTestUtil.loginTenant(tenantId);
        identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        processAPI = TenantAPIAccessor.getProcessAPI(session);
        commandAPI = TenantAPIAccessor.getCommandAPI(session);
        migrationAPI = TenantAPIAccessor.getMigrationAPI(session);
    }

    protected void logout() throws BonitaException {
        SPBPMTestUtil.logoutTenant(session);
        session = null;
        identityAPI = null;
        processAPI = null;
        commandAPI = null;
        migrationAPI = null;
    }

    protected User addMappingAndActor(final String actorName, final String userName, final String password, final ProcessDefinition definition)
            throws BonitaException {
        final User user = createUser(userName, password);
        addMappingOfActorsForUser(actorName, user.getId(), definition);
        return user;
    }

    protected void addMappingOfActorsForUser(final String actorName, final long userId, final ProcessDefinition definition) throws BonitaException {
        final ActorInstance actorInstance = getActor(actorName, definition);
        if (actorInstance != null) {
            processAPI.addUserToActor(actorInstance.getId(), userId);
        }
    }

    protected void addMappingOfActorsForGroup(final String actorName, final long groupId, final ProcessDefinition definition) throws BonitaException {
        final ActorInstance actorInstance = getActor(actorName, definition);
        if (actorInstance != null) {
            processAPI.addGroupToActor(actorInstance.getId(), groupId);
        }
    }

    protected void addMappingOfActorsForRole(final String actorName, final long roleId, final ProcessDefinition definition) throws BonitaException {
        final ActorInstance actorInstance = getActor(actorName, definition);
        if (actorInstance != null) {
            processAPI.addRoleToActor(actorInstance.getId(), roleId);
        }
    }

    protected void addMappingOfActorsForRoleAndGroup(final String actorName, final long roleId, final long groupId, final ProcessDefinition definition)
            throws BonitaException {
        final ActorInstance actorInstance = getActor(actorName, definition);
        if (actorInstance != null) {
            processAPI.addRoleAndGroupToActor(actorInstance.getId(), roleId, groupId);
        }
    }

    private ActorInstance getActor(final String actorName, final ProcessDefinition definition) throws InvalidSessionException,
            ProcessDefinitionNotFoundException, PageOutOfRangeException, ActorNotFoundException {
        final List<ActorInstance> actors = processAPI.getActors(definition.getId(), 0, 50, ActorSorting.NAME_ASC);
        final ActorInstance actorInstance = getActorInstance(actors, actorName);
        if (actorInstance != null) {
            return actorInstance;
        } else {
            throw new ActorNotFoundException(actorName + " is not a known actor");
        }
    }

    private ActorInstance getActorInstance(final List<ActorInstance> actors, final String actorName) {
        for (final ActorInstance actor : actors) {
            if (actor.getName().equals(actorName)) {
                return actor;
            }
        }
        return null;
    }

    protected User createUser(final String userName, final String password) throws InvalidSessionException, UserAlreadyExistException, UserCreationException {
        final UserBuilder userBuilder = new UserBuilder();
        userBuilder.createNewInstance(userName, password);
        return identityAPI.createUser(userBuilder.done(), null, null);
    }

    protected User createUserAndLogin(final String userName, final String password) throws BonitaException {
        final User user = identityAPI.createUser(userName, password);
        logout();
        loginWith(userName, password);
        return user;
    }

    protected void deleteUsers(final User... users) throws BonitaException {
        if (users != null) {
            for (final User user : users) {
                identityAPI.deleteUser(user.getId());
            }
        }
    }

    protected void deleteGroups(final Group... groups) throws BonitaException {
        if (groups != null) {
            for (final Group group : groups) {
                identityAPI.deleteGroup(group.getId());
            }
        }
    }

    protected void deleteRoles(final Role... roles) throws BonitaException {
        if (roles != null) {
            for (final Role role : roles) {
                identityAPI.deleteRole(role.getId());
            }
        }
    }

    protected void deleteUserMembership(final long id) throws BonitaException {
        identityAPI.deleteUserMembership(id);
    }

    protected UserMembership createUserMembership(final String userName, final String roleName, final String groupName) throws BonitaException {
        return identityAPI.addUserMembership(identityAPI.getUserByUserName(userName).getId(), identityAPI.getGroupByPath(groupName).getId(), identityAPI
                .getRoleByName(roleName).getId());
    }

    protected User createUser(final User user) throws BonitaException {
        return identityAPI.createUser(user, null, null);
    }

    protected User createUserWithManager(final String userName, final long managerId) throws BonitaException {
        final UserBuilder userBuilder = new UserBuilder();
        userBuilder.createNewInstance(userName, "bpm").setManagerUserId(managerId);
        return identityAPI.createUser(userBuilder.done(), null, null);
    }

    public ProcessAPI getProcessAPI() {
        return processAPI;
    }

    public IdentityAPI getIdentityAPI() {
        return identityAPI;
    }

    public CommandAPI getCommandAPI() {
        return commandAPI;
    }

    public LogAPI getLogAPI() {
        return logAPI;
    }

    public MigrationAPI getMigrationAPI() {
        return migrationAPI;
    }

    protected ProcessSupervisor createSupervisor(final long processDefID, final long userId) throws BonitaException {
        final String commandName = "createSupervisor";
        final HashMap<String, Serializable> parameters1 = new HashMap<String, Serializable>();
        parameters1.put(PROCESS_DEFINITION_ID_KEY, processDefID);
        parameters1.put(USER_ID_KEY, userId);
        final ProcessSupervisor createdSupervisor = (ProcessSupervisor) getCommandAPI().execute(commandName, parameters1);
        return createdSupervisor;
    }

    private ProcessSupervisor createSupervisorByRole(final long processDefID, final long roleId) throws BonitaException {
        final String commandName = "createSupervisor";
        final HashMap<String, Serializable> parameters1 = new HashMap<String, Serializable>();
        parameters1.put(PROCESS_DEFINITION_ID_KEY, processDefID);
        parameters1.put(ROLE_ID_KEY, roleId);
        final ProcessSupervisor createdSupervisor = (ProcessSupervisor) getCommandAPI().execute(commandName, parameters1);
        return createdSupervisor;
    }

    private ProcessSupervisor createSupervisorByGroup(final long processDefID, final long groupId) throws BonitaException {
        final String commandName = "createSupervisor";
        final HashMap<String, Serializable> parameters1 = new HashMap<String, Serializable>();
        parameters1.put(PROCESS_DEFINITION_ID_KEY, processDefID);
        parameters1.put(GROUP_ID_KEY, groupId);
        final ProcessSupervisor createdSupervisor = (ProcessSupervisor) getCommandAPI().execute(commandName, parameters1);
        return createdSupervisor;
    }

    public Map<String, Object> createSupervisorByRoleAndGroup(final long processDefinitionId, final long userId) throws BonitaException {
        // add supervisor by role
        final String developer = "developer";
        final RoleBuilder roleBuilder = new RoleBuilder();
        roleBuilder.createNewInstance(developer);
        final Role role = getIdentityAPI().createRole(roleBuilder.done());
        final ProcessSupervisor supervisorByRole = createSupervisorByRole(processDefinitionId, role.getId());

        // add supervisor group
        final GroupBuilder groupBuilder = new GroupBuilder();
        groupBuilder.createNewInstance("R&D");
        final Group group = getIdentityAPI().createGroup(groupBuilder.done());
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

    protected void deleteUserAndProcess(final String johnName, final ProcessDefinition definition) throws InvalidSessionException, UserNotFoundException,
            UserDeletionException, ProcessDefinitionNotFoundException, ProcessDisablementException, ProcessDeletionException, DeletingEnabledProcessException {
        deleteUser(johnName);
        deleteProcess(definition);
    }

    protected void deleteProcess(final ProcessDefinition definition) throws InvalidSessionException, ProcessDefinitionNotFoundException,
            ProcessDisablementException, ProcessDeletionException, DeletingEnabledProcessException {
        deleteProcess(definition.getId());
    }

    protected void deleteProcess(final long processDefinitionId) throws InvalidSessionException, ProcessDefinitionNotFoundException,
            ProcessDisablementException, ProcessDeletionException, DeletingEnabledProcessException {
        processAPI.deleteProcess(processDefinitionId);
    }

    protected void deleteUser(final String userName) throws InvalidSessionException, UserNotFoundException, UserDeletionException {
        identityAPI.deleteUser(userName);
    }

    protected void deleteUser(final User user) throws InvalidSessionException, UserNotFoundException, UserDeletionException {
        deleteUser(user.getId());
    }

    protected void deleteUser(final long userId) throws InvalidSessionException, UserNotFoundException, UserDeletionException {
        identityAPI.deleteUser(userId);
    }

    protected ProcessDefinition deployAndEnableProcess(final DesignProcessDefinition designProcessDefinition) throws BonitaException {
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = processAPI.deploy(businessArchive);
        processAPI.enableProcess(processDefinition.getId());
        return processDefinition;
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
        final ProcessDefinition processDefinition = processAPI.deploy(new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(designProcessDefinition).done());
        for (int i = 0; i < users.size(); i++) {
            addMappingOfActorsForUser(actorsName.get(i), users.get(i).getId(), processDefinition);
        }
        processAPI.enableProcess(processDefinition.getId());
        return processDefinition;
    }

    protected ProcessDefinition deployAndEnableWithActor(final BusinessArchive businessArchive, final List<String> actorsName, final List<User> users)
            throws BonitaException {
        final ProcessDefinition processDefinition = processAPI.deploy(businessArchive);
        for (int i = 0; i < users.size(); i++) {
            addMappingOfActorsForUser(actorsName.get(i), users.get(i).getId(), processDefinition);
        }
        processAPI.enableProcess(processDefinition.getId());
        return processDefinition;
    }

    protected ProcessDefinition deployAndEnableWithActor(final BusinessArchive businessArchive, final String actorsName, final User users)
            throws BonitaException {
        return deployAndEnableWithActor(businessArchive, Collections.singletonList(actorsName), Collections.singletonList(users));
    }

    protected ProcessDefinition deployAndEnableWithActorAndParameters(final DesignProcessDefinition designProcessDefinition, final List<String> actorsName,
            final List<User> users, final Map<String, String> parameters) throws BonitaException {
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        final BusinessArchive businessArchive = businessArchiveBuilder.setParameters(parameters).setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = processAPI.deploy(businessArchive);
        for (int i = 0; i < users.size(); i++) {
            addMappingOfActorsForUser(actorsName.get(i), users.get(i).getId(), processDefinition);
        }
        processAPI.enableProcess(processDefinition.getId());
        return processDefinition;
    }

    protected ProcessDefinition deployAndEnableWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final Group group)
            throws BonitaException {
        final ProcessDefinition processDefinition = processAPI.deploy(new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(designProcessDefinition).done());
        addMappingOfActorsForGroup(actorName, group.getId(), processDefinition);
        processAPI.enableProcess(processDefinition.getId());
        return processDefinition;
    }

    protected ProcessDefinition deployAndEnableWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final Role role)
            throws BonitaException {
        final ProcessDefinition processDefinition = processAPI.deploy(new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(designProcessDefinition).done());
        addMappingOfActorsForRole(actorName, role.getId(), processDefinition);
        processAPI.enableProcess(processDefinition.getId());
        return processDefinition;
    }

    protected ProcessDefinition deployAndEnableWithActor(final DesignProcessDefinition designProcessDefinition, final String actorName, final Role role,
            final Group group) throws BonitaException {
        final ProcessDefinition processDefinition = processAPI.deploy(new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(designProcessDefinition).done());
        addMappingOfActorsForRoleAndGroup(actorName, role.getId(), group.getId(), processDefinition);
        processAPI.enableProcess(processDefinition.getId());
        return processDefinition;
    }

    protected void disableAndDelete(final ProcessDefinition processDefinition) throws InvalidSessionException, ProcessDefinitionNotFoundException,
            ProcessDisablementException, ProcessDeletionException, DeletingEnabledProcessException {
        disableAndDelete(processDefinition.getId());
    }

    protected void disableAndDelete(final long processDefinitionId) throws InvalidSessionException, ProcessDefinitionNotFoundException,
            ProcessDisablementException, ProcessDeletionException, DeletingEnabledProcessException {
        processAPI.disableProcess(processDefinitionId);
        processAPI.deleteProcess(processDefinitionId);
    }

    public APISession getSession() {
        return session;
    }

    protected class StartProcessUntilStep {

        ProcessInstance processInstance;

        ActivityInstance activityInstance;

        public StartProcessUntilStep(final ProcessInstance processInstance, final ActivityInstance activityInstance) {
            super();
            this.processInstance = processInstance;
            this.activityInstance = activityInstance;
        }

        public ProcessInstance getProcessInstance() {
            return processInstance;
        }

        public ActivityInstance getActivityInstance() {
            return activityInstance;
        }
    }

    protected class WaitForStep extends WaitUntil {

        private final String stepName;

        private final long processInstanceId;

        private long activityInstanceId;

        private ActivityInstance result;

        private String state = null;

        public WaitForStep(final int repeatEach, final int timeout, final String stepName, final long processInstanceId) {
            super(repeatEach, timeout);
            this.stepName = stepName;
            this.processInstanceId = processInstanceId;
        }

        public WaitForStep(final int repeatEach, final int timeout, final String stepName, final long processInstanceId, final String state) {
            super(repeatEach, timeout);
            this.stepName = stepName;
            this.processInstanceId = processInstanceId;
            this.state = state;
        }

        public WaitForStep(final String stepName, final long processInstanceId) {
            super(50, 3000);
            this.stepName = stepName;
            this.processInstanceId = processInstanceId;
        }

        @Override
        protected boolean check() throws Exception {
            final List<ActivityInstance> openedActivityInstances = getProcessAPI().getOpenedActivityInstances(processInstanceId, 0, 10,
                    ActivityInstanceCriterion.DEFAULT);
            final Iterator<ActivityInstance> iterator = openedActivityInstances.iterator();
            boolean found = false;
            while (iterator.hasNext() && !found) {
                final ActivityInstance activityInstance = iterator.next();
                if (activityInstance.getName().equals(stepName)) {
                    if (state == null || state.equals(activityInstance.getState())) {
                        activityInstanceId = activityInstance.getId();
                        result = activityInstance;
                        found = true;
                    }
                }
            }
            return found;
        }

        public long getStepId() {
            return activityInstanceId;
        }

        public ActivityInstance getResult() {
            return result;
        }

    }

    protected class WaitForActivity extends WaitUntil {

        private final String activityName;

        private final long processInstanceId;

        private ActivityInstance result;

        public WaitForActivity(final int repeatEach, final int timeout, final String activityName, final long processInstanceId) {
            super(repeatEach, timeout);
            this.activityName = activityName;
            this.processInstanceId = processInstanceId;
        }

        @Override
        protected boolean check() throws BonitaException {
            final Set<ActivityInstance> activityInstances = getProcessAPI().getActivities(processInstanceId, 0, 10);
            final Iterator<ActivityInstance> iterator = activityInstances.iterator();
            boolean found = false;
            while (iterator.hasNext() && !found) {
                final ActivityInstance activityInstance = iterator.next();
                if (activityInstance.getName().equals(activityName)) {
                    result = activityInstance;
                    found = true;
                }
            }
            return found;
        }

        public ActivityInstance getResult() {
            return result;
        }

    }

    protected class WaitForArchivedActivity extends WaitUntil {

        private final long activityId;

        private ArchivedActivityInstance archivedActivityInstance;

        private final String stateName;

        public WaitForArchivedActivity(final int repeatEach, final int timeout, final long activityId, final String stateName) {
            super(repeatEach, timeout);
            this.activityId = activityId;
            this.stateName = stateName;
        }

        @Override
        protected boolean check() throws BonitaException {
            archivedActivityInstance = getProcessAPI().getArchivedActivityInstance(activityId);
            return stateName.equals(archivedActivityInstance.getState());
        }

        public ArchivedActivityInstance getArchivedActivityInstance() {
            return archivedActivityInstance;
        }

    }

    protected class WaitForFinalArchivedActivity extends WaitUntil {

        private final String activityName;

        private final long processInstanceId;

        private ArchivedActivityInstance result;

        public WaitForFinalArchivedActivity(final int repeatEach, final int timeout, final String activityName, final long processInstanceId) {
            super(repeatEach, timeout);
            this.activityName = activityName;
            this.processInstanceId = processInstanceId;
        }

        @Override
        protected boolean check() throws BonitaException {
            final List<ArchivedActivityInstance> activityInstances = getProcessAPI().getArchivedActivityInstances(processInstanceId, 0, 100,
                    ActivityInstanceCriterion.NAME_ASC);
            final Iterator<ArchivedActivityInstance> iterator = activityInstances.iterator();
            boolean found = false;
            while (iterator.hasNext() && !found) {
                final ArchivedActivityInstance activityInstance = iterator.next();
                if (activityInstance.getName().equals(activityName) && activityInstance.getState().equals(TestStates.getNormalFinalState(activityInstance))) {
                    result = activityInstance;
                    found = true;
                }
            }
            return found;
        }

        public ArchivedActivityInstance getResult() {
            return result;
        }

    }

    protected class WaitForEvent extends WaitUntil {

        private final String eventName;

        private final long processInstanceId;

        private String state = null;

        private EventInstance result;

        public WaitForEvent(final int repeatEach, final int timeout, final String eventName, final long processInstanceId) {
            super(repeatEach, timeout);
            this.eventName = eventName;
            this.processInstanceId = processInstanceId;
        }

        public WaitForEvent(final int repeatEach, final int timeout, final String eventName, final long processInstanceId, final String state) {
            this(repeatEach, timeout, eventName, processInstanceId);
            this.state = state;
        }

        @Override
        protected boolean check() throws Exception {
            final List<EventInstance> eventInstances = getProcessAPI().getEventInstances(processInstanceId, 0, 10, EventSorting.NAME_ASC);
            boolean found = false;
            final Iterator<EventInstance> iterator = eventInstances.iterator();
            while (iterator.hasNext() && !found) {
                final EventInstance eventInstance = iterator.next();
                if (eventInstance.getName().equals(eventName)) {
                    if (state == null) {
                        found = true;
                        result = eventInstance;
                    } else {
                        found = state.equals(eventInstance.getState());
                        result = eventInstance;
                    }
                }
            }
            return found;
        }

        public EventInstance getResult() {
            return result;
        }

    }

    protected class WaitForFlowNode extends WaitUntil {

        private final String name;

        private final long processInstanceId;

        private String state = null;

        private StateCategory stateCategory = null;

        private FlowNodeInstance result;

        public WaitForFlowNode(final int repeatEach, final int timeout, final String name, final long parentProcessInstanceId) {
            super(repeatEach, timeout);
            this.name = name;
            processInstanceId = parentProcessInstanceId;
        }

        public WaitForFlowNode(final int repeatEach, final int timeout, final String name, final long parentProcessInstanceId, final String state) {
            this(repeatEach, timeout, name, parentProcessInstanceId);
            this.state = state;
        }

        public WaitForFlowNode(final int repeatEach, final int timeout, final String name, final long parentProcessInstanceId, final StateCategory stateCategory) {
            this(repeatEach, timeout, name, parentProcessInstanceId);
            this.stateCategory = stateCategory;
        }

        @Override
        protected boolean check() throws Exception {
            final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
            searchOptionsBuilder.filter(FlowNodeInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID, processInstanceId);
            searchOptionsBuilder.filter(FlowNodeInstanceSearchDescriptor.NAME, name);
            final SearchResult<FlowNodeInstance> searchResult = getProcessAPI().searchFlowNodeInstances(searchOptionsBuilder.done());
            final boolean found = searchResult.getCount() > 0;
            boolean check = found;
            if (found) {
                result = searchResult.getResult().get(0);
                if (state != null) {
                    check = state.equals(result.getState());
                }
                if (stateCategory != null) {
                    check = stateCategory.equals(result.getStateCategory());
                }
            }
            return check;
        }

        public FlowNodeInstance getResult() {
            return result;
        }

    }

    protected boolean containsState(final List<ArchivedProcessInstance> instances, final String state) {
        for (final ArchivedProcessInstance pi : instances) {
            if (state.equals(pi.getState())) {
                return true;
            }
        }
        return false;
    }

    protected User createUser(final String username, final String password, final String firstName, final String lastName) throws BonitaException {
        final UserBuilder userBuilder = new UserBuilder();
        userBuilder.createNewInstance(username, password);
        userBuilder.setFirstName(firstName).setLastName(lastName);
        return identityAPI.createUser(userBuilder.done(), null, null);
    }

    protected Group createGroup(final String groupName) throws InvalidSessionException, GroupAlreadyExistException, GroupCreationException {
        return createGroup(groupName, null);
    }

    protected Group createGroup(final String groupName, final String groupPath) throws InvalidSessionException, GroupAlreadyExistException,
            GroupCreationException {
        return identityAPI.createGroup(new GroupBuilder().createNewInstance(groupName).setParentPath(groupPath).done());
    }

    protected Role createRole(final String roleName) throws BonitaException {
        return identityAPI.createRole(new RoleBuilder().createNewInstance(roleName).done());
    }

    protected boolean containsLogWithActionType(final List<Log> logs, final String actionType, final int minimalFrequency) {
        int count = 0;
        final Iterator<Log> iterator = logs.iterator();
        while (iterator.hasNext() && count < minimalFrequency) {
            final Log log = iterator.next();
            if (actionType.equals(log.getActionType())) {
                count++;
            }
        }

        return count == minimalFrequency;
    }

    /**
     * @author Baptiste Mesta
     */
    protected final class CheckNbOfActivities extends WaitUntil {

        private final ProcessAPI processAPI;

        private final ProcessInstance processInstance;

        private final int nbActivities;

        private Set<ActivityInstance> result;

        private String activityState = null;

        public CheckNbOfActivities(final ProcessAPI processAPI, final int repeatEach, final int timeout, final boolean throwExceptions,
                final ProcessInstance processInstance, final int nbActivities) {
            super(repeatEach, timeout, throwExceptions);
            this.processInstance = processInstance;
            this.nbActivities = nbActivities;
            this.processAPI = processAPI;
        }

        public CheckNbOfActivities(final ProcessAPI processAPI, final int repeatEach, final int timeout, final boolean throwExceptions,
                final ProcessInstance processInstance, final int nbActivities, final String state) {
            this(processAPI, repeatEach, timeout, throwExceptions, processInstance, nbActivities);
            activityState = state;
        }

        @Override
        protected boolean check() throws Exception {
            final Set<ActivityInstance> activities = processAPI.getActivities(processInstance.getId(), 0, 200);
            result = new HashSet<ActivityInstance>(activities.size());
            // The number of activities is the one expected...

            if (activityState != null) {
                for (final ActivityInstance activityInstance : activities) {
                    // ... and all states are equal to the expected one:
                    if (activityInstance.getState().equals(activityState)) {
                        result.add(activityInstance);
                    }
                }
            } else {
                result.addAll(activities);
            }
            final boolean check = result.size() == nbActivities;

            return check;// get activities with a state
        }

        public Set<ActivityInstance> getResult() {
            return result;
        }

    }

    protected final class CheckNbOfArchivedActivities extends WaitUntil {

        private final ProcessAPI processAPI;

        private final ProcessInstance processInstance;

        private final int nbActivities;

        private Set<ArchivedActivityInstance> result;

        private String activityState = null;

        public CheckNbOfArchivedActivities(final ProcessAPI processAPI, final int repeatEach, final int timeout, final boolean throwExceptions,
                final ProcessInstance processInstance, final int nbActivities) {
            super(repeatEach, timeout, throwExceptions);
            this.processInstance = processInstance;
            this.nbActivities = nbActivities;
            this.processAPI = processAPI;
        }

        public CheckNbOfArchivedActivities(final ProcessAPI processAPI, final int repeatEach, final int timeout, final boolean throwExceptions,
                final ProcessInstance processInstance, final int nbActivities, final String state) {
            this(processAPI, repeatEach, timeout, throwExceptions, processInstance, nbActivities);
            activityState = state;
        }

        @Override
        protected boolean check() throws Exception {
            final List<ArchivedActivityInstance> activities = processAPI.getArchivedActivityInstances(processInstance.getId(), 0, 200,
                    ActivityInstanceCriterion.NAME_ASC);
            result = new HashSet<ArchivedActivityInstance>(activities.size());
            // The number of activities is the one expected...

            if (activityState != null) {
                for (final ArchivedActivityInstance activityInstance : activities) {
                    // ... and all states are equal to the expected one:
                    if (activityInstance.getState().equals(activityState)) {
                        result.add(activityInstance);
                    }
                }
            } else {
                result.addAll(activities);
            }
            final boolean check = result.size() == nbActivities;

            return check;// get activities with a state
        }

        public Set<ArchivedActivityInstance> getResult() {
            return result;
        }

    }

    protected final class CheckNbOfProcessInstances extends WaitUntil {

        private final int nbOfProcInst;

        private List<ProcessInstance> result;

        private final ProcessInstanceCriterion orderBy;

        public CheckNbOfProcessInstances(final int repeatEach, final int timeout, final int nbOfProcInst, final ProcessInstanceCriterion orderBy) {
            super(repeatEach, timeout);
            this.nbOfProcInst = nbOfProcInst;
            this.orderBy = orderBy;
        }

        public CheckNbOfProcessInstances(final int repeatEach, final int timeout, final int nbOfProcInst) {
            this(repeatEach, timeout, nbOfProcInst, ProcessInstanceCriterion.NAME_ASC);
        }

        @Override
        protected boolean check() throws Exception {
            result = getProcessAPI().getProcessInstances(0, nbOfProcInst + 1, orderBy);
            return nbOfProcInst == result.size();
        }

        public List<ProcessInstance> getResult() {
            return result;
        }

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
        getProcessAPI().assignUserTask(activityInstance.getId(), userId);
        executeAssignedTaskUntilEnd(activityInstance.getId());
    }

    protected void executeAssignedTaskUntilEnd(final long activityInstanceId) throws InvalidSessionException, ActivityInterruptedException,
            ActivityExecutionFailedException, ActivityExecutionErrorException, ActivityInstanceNotFoundException {
        getProcessAPI().executeActivity(activityInstanceId);
        getProcessAPI().executeActivity(activityInstanceId);
    }

    protected ActivityInstance waitForUserTask(final int repeatEach, final int timeout, final String taskName, final ProcessInstance processInstance)
            throws Exception {
        final WaitForStep waitForStep1 = new WaitForStep(repeatEach, timeout, taskName, processInstance.getId(), TestStates.getReadyState(null));
        if (!waitForStep1.waitUntil()) {
            throw new ActivityInstanceNotFoundException(processInstance.getId());
        }
        return waitForStep1.getResult();
    }

    protected SearchResult<HumanTaskInstance> waitForHumanTasks(final int repeatEach, final int timeout, final int nbTasks, final String taskName,
            final long processInstanceId) throws Exception {
        final CheckNbOfHumanTasks checkNbOfHumanTasks = new CheckNbOfHumanTasks(repeatEach, timeout, true, nbTasks, new SearchOptionsBuilder(0, 10000)
                .filter(HumanTaskInstanceSearchDescriptor.PROCESS_INSTANCE_ID, processInstanceId).filter(HumanTaskInstanceSearchDescriptor.NAME, taskName)
                .done());
        assertTrue(checkNbOfHumanTasks.waitUntil());
        return checkNbOfHumanTasks.getHumanTaskInstances();
    }

    protected void deleteSupervisor(final Serializable id) throws BonitaException {
        final Map<String, Serializable> deleteParameters = new HashMap<String, Serializable>();
        deleteParameters.put(SUPERVISOR_ID_KEY, id);
        getCommandAPI().execute("deleteSupervisor", deleteParameters);
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
        final WaitForPendingTasks waitUntil = new WaitForPendingTasks(DEFAULT_REPEAT, DEFAULT_TIMEOUT, nbPendingTasks, userId);
        assertTrue("no pending user task instances are found", waitUntil.waitUntil());
        return waitUntil.getResults();
    }
}
