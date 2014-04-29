/**
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.bpm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.SActorCreationException;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.SActorBuilderFactory;
import org.bonitasoft.engine.api.impl.IdentityAPIImpl;
import org.bonitasoft.engine.api.impl.LoginAPIImpl;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.login.LoginService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.core.process.definition.model.impl.SFlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.GatewayInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.SEventInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SUserTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SAutomaticTaskInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SEndEventInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SIntermediateCatchEventInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SIntermediateThrowEventInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SStartEventInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.event.SEventInstance;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SContactInfo;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.identity.model.builder.SContactInfoBuilder;
import org.bonitasoft.engine.identity.model.builder.SContactInfoBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SRoleBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SUserBuilder;
import org.bonitasoft.engine.identity.model.builder.SUserBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SUserMembershipBuilderFactory;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.test.util.TestUtil;
import org.bonitasoft.engine.transaction.STransactionCommitException;
import org.bonitasoft.engine.transaction.STransactionCreationException;
import org.bonitasoft.engine.transaction.STransactionException;
import org.bonitasoft.engine.transaction.STransactionRollbackException;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class CommonBPMServicesTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(CommonBPMServicesTest.class);

    private static BPMServicesBuilder bpmServicesBuilder;

    protected static TransactionService transactionService;

    private static SessionAccessor sessionAccessor;

    private static LoginService loginService;

    private static ProcessDefinitionService processDefinitionService;

    private static IdentityService identityService;

    private static ProcessInstanceService processInstanceService;

    private static final EventInstanceService eventInstanceService;

    private final static ActorMappingService actorMappingService;

    private static final ActivityInstanceService activityInstanceService;

    private APISession sSession;

    public static BPMServicesBuilder getServicesBuilder() {
        return bpmServicesBuilder;
    }

    @Rule
    public TestRule testWatcher = new TestWatcher() {

        @Override
        public void starting(final Description d) {
            LOGGER.info("Starting test: " + this.getClass().getName() + "." + d.getMethodName());
        }

        @Override
        public void failed(final Throwable e, final Description d) {
            LOGGER.warn("Failed test: " + this.getClass().getName() + "." + d.getMethodName(), e);
            try {
                clean();
            } catch (final Exception be) {
                LOGGER.error("unable to clean db", be);
            } finally {
                LOGGER.info("-----------------------------------------------------------------------------------------------");
            }
        }

        @Override
        public void succeeded(final Description d) {
            try {
                List<String> clean;
                try {
                    clean = clean();
                } catch (final Exception e) {
                    throw new BonitaRuntimeException(e);
                }
                LOGGER.info("Succeeded test: " + this.getClass().getName() + "." + d.getMethodName());
                if (!clean.isEmpty()) {
                    throw new BonitaRuntimeException(clean.toString());
                }
            } finally {
                LOGGER.info("-----------------------------------------------------------------------------------------------");
            }
        }
    };

    public APISession getsSession() {
        return sSession;
    }

    static {
        bpmServicesBuilder = new BPMServicesBuilder();
        transactionService = bpmServicesBuilder.getTransactionService();
        bpmServicesBuilder.getPlatformService();
        sessionAccessor = bpmServicesBuilder.getSessionAccessor();
        loginService = bpmServicesBuilder.getLoginService();
        identityService = bpmServicesBuilder.getIdentityService();
        processDefinitionService = bpmServicesBuilder.getProcessDefinitionService();
        processInstanceService = bpmServicesBuilder.getProcessInstanceService();
        eventInstanceService = bpmServicesBuilder.getEventInstanceService();
        actorMappingService = bpmServicesBuilder.getActorMappingService();
        activityInstanceService = bpmServicesBuilder.getActivityInstanceService();
    }

    protected GatewayInstanceService gatewayInstanceService() {
        return getServicesBuilder().getGatewayInstanceService();
    }

    protected static TransactionService getTransactionService() {
        return getServicesBuilder().getTransactionService();
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        // if (!platformCreated) {
        // // Call directly the API because we want the files to be copied:
        // final PlatformAPIImpl platformAPI = new PlatformAPIImpl();
        // sessionAccessor.setSessionInfo(1l, -1);
        // platformAPI.createAndInitializePlatform();
        // platformAPI.startNode();
        // }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // if (!platformCreated) {
        // TestUtil.closeTransactionIfOpen(transactionService);
        // stopScheduler();
        // TestUtil.deleteDefaultTenantAndPlatForm(transactionService, platformService, sessionAccessor, bpmServicesBuilder.getSessionService());
        // }
    }

    protected Group createGroup(final String groupName) throws AlreadyExistsException, CreationException {
        return createGroup(groupName, null);
    }

    protected Group createGroup(final String groupName, final String groupPath) throws AlreadyExistsException, CreationException {
        try {
            transactionService.begin();
            final Group group = new IdentityAPIImpl().createGroup(groupName, groupPath);
            transactionService.complete();
            return group;
        } catch (final STransactionException e) {
            throw new CreationException(e);
        }
    }

    private static void stopScheduler() throws Exception {
        final LoginAPIImpl loginAPI = new LoginAPIImpl();
        final APISession session = loginAPI.login(TestUtil.getDefaultUserName(), TestUtil.getDefaultPassword());
        sessionAccessor.setSessionInfo(session.getId(), session.getTenantId());
        TestUtil.stopScheduler(bpmServicesBuilder.getSchedulerService(), bpmServicesBuilder.getTransactionService());
        loginAPI.logout(session);
    }

    @Before
    public void before() throws Exception {
        sSession = new LoginAPIImpl().login(TestUtil.getDefaultUserName(), TestUtil.getDefaultPassword());
        sessionAccessor.setSessionInfo(sSession.getId(), sSession.getTenantId());
    }

    @After
    public void after() throws Exception {
        TestUtil.closeTransactionIfOpen(transactionService);
        new LoginAPIImpl().logout(sSession);
    }

    private List<String> clean() throws Exception {
        try {
            sSession = new LoginAPIImpl().login(TestUtil.getDefaultUserName(), TestUtil.getDefaultPassword());
            transactionService.begin();
            sessionAccessor.setSessionInfo(sSession.getId(), sSession.getTenantId());
            final List<String> messages = new ArrayList<String>();
            // final STenant tenant = platformService.getTenant(tenantId);
            final QueryOptions queryOptions = new QueryOptions(0, 200, SProcessDefinitionDeployInfo.class, "name", OrderByType.ASC);
            final List<SProcessDefinitionDeployInfo> processes = processDefinitionService.getProcessDeploymentInfos(queryOptions);
            if (processes.size() > 0) {
                final StringBuilder processBuilder = new StringBuilder("Process Definitions are still active: ");
                for (final SProcessDefinitionDeployInfo process : processes) {
                    processBuilder.append(process.getProcessId()).append(":").append(process.getName()).append(", ");
                    try {
                        processDefinitionService.disableProcessDeploymentInfo(process.getProcessId());
                    } catch (final Throwable ignored) {
                    }
                    processDefinitionService.delete(process.getProcessId());
                }
                messages.add(processBuilder.toString());
            }

            // let's clean up All Process Instances:
            List<SProcessInstance> processInstances = getFirstProcessInstances(5000);
            while (processInstances.size() > 0) {
                for (final SProcessInstance sProcessInstance : processInstances) {
                    processInstanceService.deleteProcessInstance(sProcessInstance.getId());
                }
                // get the next 100:
                processInstances = getFirstProcessInstances(5000);
            }

            final List<SUserMembership> memberships = CommonBPMServicesTest.identityService.getUserMemberships(0, 5000);
            if (memberships.size() > 0) {
                final StringBuilder stringBuilder = new StringBuilder("Membership are still present: ");
                for (final SUserMembership sMembership : memberships) {
                    stringBuilder.append(sMembership.getId()).append(", ");
                    CommonBPMServicesTest.identityService.deleteUserMembership(sMembership);
                }
            }

            final List<SRole> roles = CommonBPMServicesTest.identityService.getRoles(0, 5000);
            if (roles.size() > 0) {
                final StringBuilder stringBuilder = new StringBuilder("Roles are still present: ");
                for (final SRole sRole : roles) {
                    stringBuilder.append(sRole.getId()).append(", ");
                    CommonBPMServicesTest.identityService.deleteRole(sRole);
                }
            }

            final List<SUser> users = CommonBPMServicesTest.identityService.getUsers(0, 5000);
            if (users.size() > 0) {
                final StringBuilder stringBuilder = new StringBuilder("Users are still present: ");
                for (final SUser sUser : users) {
                    stringBuilder.append(sUser.getId()).append(", ");
                    CommonBPMServicesTest.identityService.deleteUser(sUser);
                }
            }

            final List<SGroup> groups = CommonBPMServicesTest.identityService.getGroups(0, 5000);
            if (groups.size() > 0) {
                final StringBuilder stringBuilder = new StringBuilder("groups are still present: ");
                for (final SGroup sGroup : groups) {
                    stringBuilder.append(sGroup.getId()).append(", ");
                    CommonBPMServicesTest.identityService.deleteGroup(sGroup);
                }
            }

            // Special treatment for CMIS implementation, we clear the whole repository:
            // if (CommonBPMServicesTest.documentService instanceof CMISDocumentServiceImpl) {
            // ((CMISDocumentServiceImpl) CommonBPMServicesTest.documentService).clear();
            // }

            loginService.logout(sSession.getId());
            sSession = null;
            return messages;
        } finally {
            transactionService.complete();
            sessionAccessor.deleteSessionId();
        }
    }

    public List<SProcessInstance> getFirstProcessInstances(final int nb) throws SBonitaSearchException {
        // we are already in a transaction context here:
        final OrderByOption orderByOption = new OrderByOption(SProcessInstance.class, BuilderFactory.get(SProcessInstanceBuilderFactory.class)
                .getLastUpdateKey(), OrderByType.DESC);
        final QueryOptions queryOptions = new QueryOptions(0, nb, Collections.singletonList(orderByOption), Collections.<FilterOption> emptyList(), null);
        return processInstanceService.searchProcessInstances(queryOptions);
    }

    protected SProcessInstance createSProcessInstance() throws SBonitaException {
        final SProcessInstance processInstance = BuilderFactory.get(SProcessInstanceBuilderFactory.class).createNewInstance("process", 1).done();

        transactionService.begin();
        processInstanceService.createProcessInstance(processInstance);
        transactionService.complete();

        return processInstance;
    }

    protected void deleteSProcessInstance(final SProcessInstance processInstance) throws SBonitaException {
        transactionService.begin();
        processInstanceService.deleteProcessInstance(processInstance.getId());
        transactionService.complete();
    }

    protected SFlowNodeInstance getFlowNodeInstance(final long flowNodeInstanceId) throws SBonitaException {
        SFlowNodeInstance flowNodeInstance = null;
        transactionService.begin();
        try {
            flowNodeInstance = activityInstanceService.getFlowNodeInstance(flowNodeInstanceId);
        } finally {
            transactionService.complete();
        }
        return flowNodeInstance;
    }

    protected SEventInstance createSStartEventInstance(final String eventName, final long flowNodeDefinitionId, final long rootProcessInstanceId,
            final long processDefinitionId, final long parentProcessInstanceId) throws STransactionCreationException, SEventInstanceCreationException,
            STransactionCommitException, STransactionRollbackException {
        final SEventInstance eventInstance = BuilderFactory
                .get(SStartEventInstanceBuilderFactory.class)
                .createNewStartEventInstance(eventName, flowNodeDefinitionId, rootProcessInstanceId, rootProcessInstanceId, processDefinitionId,
                        rootProcessInstanceId, parentProcessInstanceId).done();
        createSEventInstance(eventInstance);
        return eventInstance;
    }

    protected SEventInstance createSEndEventInstance(final String eventName, final long flowNodeDefinitionId, final long rootProcessInstanceId,
            final long processDefinitionId, final long parentProcessInstanceId) throws STransactionCreationException, SEventInstanceCreationException,
            STransactionCommitException, STransactionRollbackException {
        final SEventInstance eventInstance = BuilderFactory
                .get(SEndEventInstanceBuilderFactory.class)
                .createNewEndEventInstance(eventName, flowNodeDefinitionId, rootProcessInstanceId, rootProcessInstanceId, processDefinitionId,
                        rootProcessInstanceId, parentProcessInstanceId).done();
        createSEventInstance(eventInstance);
        return eventInstance;
    }

    protected SEventInstance createSIntermediateCatchEventInstance(final String eventName, final long flowNodeDefinitionId, final long rootProcessInstanceId,
            final long processDefinitionId, final long parentProcessInstanceId) throws STransactionCreationException, SEventInstanceCreationException,
            STransactionCommitException, STransactionRollbackException {
        final SEventInstance eventInstance = BuilderFactory
                .get(SIntermediateCatchEventInstanceBuilderFactory.class)
                .createNewIntermediateCatchEventInstance(eventName, flowNodeDefinitionId, rootProcessInstanceId, parentProcessInstanceId, processDefinitionId,
                        rootProcessInstanceId, parentProcessInstanceId).done();
        createSEventInstance(eventInstance);
        return eventInstance;
    }

    protected SEventInstance createSIntermediateThrowEventInstance(final String eventName, final long flowNodeDefinitionId, final long processInstanceId,
            final long processDefinitionId, final long parentProcessInstanceId) throws STransactionCreationException, SEventInstanceCreationException,
            STransactionCommitException, STransactionRollbackException {
        final SEventInstance eventInstance = BuilderFactory
                .get(SIntermediateThrowEventInstanceBuilderFactory.class)
                .createNewIntermediateThrowEventInstance(eventName, flowNodeDefinitionId, processInstanceId, processInstanceId, processDefinitionId,
                        processInstanceId, parentProcessInstanceId).done();
        createSEventInstance(eventInstance);
        return eventInstance;
    }

    protected void createSEventInstance(final SEventInstance eventInstance) throws STransactionCreationException, SEventInstanceCreationException,
            STransactionCommitException, STransactionRollbackException {
        transactionService.begin();
        eventInstanceService.createEventInstance(eventInstance);
        transactionService.complete();
    }

    protected void insertGatewayInstance(final SGatewayInstance gatewayInstance) throws SBonitaException {
        transactionService.begin();
        gatewayInstanceService().createGatewayInstance(gatewayInstance);
        transactionService.complete();
    }

    protected void createSUserTaskInstance(final String name, final long flowNodeDefinitionId, final long parentId, final long processDefinitionId,
            final long rootProcessInst, final long actorId) throws SBonitaException {
        final SUserTaskInstance taskInstance = BuilderFactory.get(SUserTaskInstanceBuilderFactory.class)
                .createNewUserTaskInstance(name, flowNodeDefinitionId, rootProcessInst, parentId, actorId, processDefinitionId, rootProcessInst, parentId)
                .done();
        transactionService.begin();
        getServicesBuilder().getActivityInstanceService().createActivityInstance(taskInstance);
        transactionService.complete();
    }

    protected SActivityInstance createSAutomaticTaskInstance(final String name, final long flowNodeDefinitionId, final long parentId,
            final long processDefinitionId, final long rootProcessInst) throws SBonitaException {
        final SActivityInstance taskInstance = BuilderFactory.get(SAutomaticTaskInstanceBuilderFactory.class)
                .createNewAutomaticTaskInstance(name, flowNodeDefinitionId, rootProcessInst, parentId, processDefinitionId, rootProcessInst, parentId).done();
        transactionService.begin();
        getServicesBuilder().getActivityInstanceService().createActivityInstance(taskInstance);
        transactionService.complete();
        return taskInstance;
    }

    private SProcessDefinition buildSProcessDefinition(final String name, final String version) throws SProcessDefinitionException {
        final SProcessDefinitionImpl sProcessDefinition = new SProcessDefinitionImpl(name, version);
        sProcessDefinition.setProcessContainer(new SFlowElementContainerDefinitionImpl());
        return processDefinitionService.store(sProcessDefinition, "", "");
    }

    private SActor buildSActor(final String name, final long scopeId, final boolean initiator) throws SActorCreationException {
        final SActorBuilderFactory sActorBuilderFactory = BuilderFactory.get(SActorBuilderFactory.class);
        final SActor sActor = sActorBuilderFactory.create(name, scopeId, initiator).getActor();
        return actorMappingService.addActor(sActor);
    }

    public SProcessDefinition createSProcessDefinitionWithSActor(final String name, final String version, final String actorName,
            final boolean actorIsInitiator, final List<SUser> sUsersToAddToActor) throws SBonitaException {
        transactionService.begin();

        final SProcessDefinition sProcessDefinition = buildSProcessDefinition(name, version);
        processDefinitionService.resolveProcess(sProcessDefinition.getId());
        processDefinitionService.enableProcessDeploymentInfo(sProcessDefinition.getId());

        final SActor sActor = buildSActor(actorName, sProcessDefinition.getId(), actorIsInitiator);
        for (final SUser sUser : sUsersToAddToActor) {
            actorMappingService.addUserToActor(sActor.getId(), sUser.getId());
        }

        transactionService.complete();
        return sProcessDefinition;
    }

    public SProcessDefinition createSProcessDefinition(final String name, final String version) throws SBonitaException {
        transactionService.begin();

        final SProcessDefinition sProcessDefinition = buildSProcessDefinition(name, version);
        processDefinitionService.resolveProcess(sProcessDefinition.getId());
        processDefinitionService.enableProcessDeploymentInfo(sProcessDefinition.getId());

        transactionService.complete();
        return sProcessDefinition;
    }

    public List<SProcessDefinition> createSProcessDefinitions(final int count, final String name, final String version) throws SBonitaException {
        final List<SProcessDefinition> processDefinitons = new ArrayList<SProcessDefinition>();

        transactionService.begin();
        for (int i = 1; i <= count; i++) {
            final SProcessDefinition sProcessDefinition = buildSProcessDefinition(name + i, version + i);
            processDefinitionService.resolveProcess(sProcessDefinition.getId());
            processDefinitionService.enableProcessDeploymentInfo(sProcessDefinition.getId());
            processDefinitons.add(sProcessDefinition);
        }
        transactionService.complete();
        return processDefinitons;
    }

    public void deleteSProcessDefinition(final SProcessDefinition sProcessDefinition) throws SBonitaException {
        deleteSProcessDefinition(sProcessDefinition.getId());
    }

    public void deleteSProcessDefinition(final long sProcessDefinitionId) throws SBonitaException {
        transactionService.begin();
        actorMappingService.deleteActors(sProcessDefinitionId);
        processDefinitionService.disableProcessDeploymentInfo(sProcessDefinitionId);
        processDefinitionService.delete(sProcessDefinitionId);
        transactionService.complete();
    }

    public void deleteSProcessDefinitions(final SProcessDefinition... sProcessDefinitions) throws SBonitaException {
        if (sProcessDefinitions != null) {
            deleteSProcessDefinitions(Arrays.asList(sProcessDefinitions));
        }
    }

    public void deleteSProcessDefinitions(final List<SProcessDefinition> sProcessDefinitions) throws SBonitaException {
        transactionService.begin();
        for (final SProcessDefinition sProcessDefinition : sProcessDefinitions) {
            actorMappingService.deleteActors(sProcessDefinition.getId());
            processDefinitionService.disableProcessDeploymentInfo(sProcessDefinition.getId());
            processDefinitionService.delete(sProcessDefinition.getId());
        }
        transactionService.complete();
    }

    public List<SUser> createEnabledSUsers(final int count, final String firstName, final String lastName, final String password) throws SBonitaException {
        transactionService.begin();
        final List<SUser> users = new ArrayList<SUser>();
        for (int i = 1; i <= count; i++) {
            users.add(identityService.createUser(buildEnabledSUser(firstName + i, lastName + i, password + i, 0)));
        }
        transactionService.complete();

        return users;
    }

    public SUser createEnabledSUser(final String firstName, final String lastName, final String password) throws SBonitaException {
        return createEnabledSUser(firstName, lastName, password, 0);
    }

    public SUser createEnabledSUser(final String firstName, final String lastName, final String password, final long managerUserId) throws SBonitaException {
        transactionService.begin();
        final SUser user = identityService.createUser(buildEnabledSUser(firstName, lastName, password, managerUserId));
        transactionService.complete();

        return user;
    }

    public SUser buildEnabledSUser(final String firstName, final String lastName, final String password, final long managerUserId) {
        final SUserBuilder userBuilder = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance();
        userBuilder.setCreatedBy(2);
        userBuilder.setCreationDate(6);
        userBuilder.setDelegeeUserName("delegeeUserName");
        userBuilder.setEnabled(true);
        userBuilder.setFirstName(firstName);
        userBuilder.setIconName("iconName");
        userBuilder.setIconPath("iconPath");
        userBuilder.setJobTitle("jobTitle");
        userBuilder.setLastConnection(3L);
        userBuilder.setLastName(lastName);
        userBuilder.setLastUpdate(4L);
        userBuilder.setManagerUserId(managerUserId);
        userBuilder.setPassword(password);
        userBuilder.setTitle("title");
        userBuilder.setUserName(firstName);
        return userBuilder.done();
    }

    public SContactInfo buildSContactInfo(final long userId, final boolean isPersonal) {
        final SContactInfoBuilder sContactInfoBuilder = BuilderFactory.get(SContactInfoBuilderFactory.class).createNewInstance(userId, isPersonal);
        sContactInfoBuilder.setAddress("address");
        sContactInfoBuilder.setBuilding("building");
        sContactInfoBuilder.setCity("city");
        sContactInfoBuilder.setCountry("country");
        sContactInfoBuilder.setEmail("email");
        sContactInfoBuilder.setFaxNumber("faxNumber");
        sContactInfoBuilder.setMobileNumber("mobileNumber");
        sContactInfoBuilder.setPhoneNumber("phoneNumber");
        sContactInfoBuilder.setRoom("room");
        sContactInfoBuilder.setState("state");
        sContactInfoBuilder.setWebsite("website");
        sContactInfoBuilder.setZipCode("zipCode");
        return sContactInfoBuilder.done();
    }

    public SUser createSUser(final String username, final String password) throws SBonitaException {
        transactionService.begin();
        final SUserBuilder userBuilder = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName(username).setPassword(password);
        final SUser user = identityService.createUser(userBuilder.done());
        transactionService.complete();
        return user;
    }

    public void deleteSUser(final SUser sUser) throws SBonitaException {
        transactionService.begin();
        identityService.deleteUser(sUser);
        transactionService.complete();
    }

    public void deleteSUsers(final SUser... users) throws SBonitaException {
        if (users != null) {
            deleteSUsers(Arrays.asList(users));
        }
    }

    public void deleteSUsers(final List<SUser> users) throws SBonitaException {
        transactionService.begin();
        for (final SUser sUser : users) {
            identityService.deleteUser(sUser);
        }
        transactionService.complete();
    }

    public void deleteSGroup(final SGroup sGroup) throws SBonitaException {
        transactionService.begin();
        identityService.deleteGroup(sGroup);
        transactionService.complete();
    }

    public void deleteSGroups(final SGroup... groups) throws SBonitaException {
        if (groups != null) {
            transactionService.begin();
            for (final SGroup sGroup : groups) {
                identityService.deleteGroup(sGroup);
            }
            transactionService.complete();
        }
    }

    public void deleteSRole(final SRole sRole) throws SBonitaException {
        transactionService.begin();
        identityService.deleteRole(sRole);
        transactionService.complete();
    }

    public void deleteSRoles(final SRole... roles) throws SBonitaException {
        if (roles != null) {
            transactionService.begin();
            for (final SRole sRole : roles) {
                identityService.deleteRole(sRole);
            }
            transactionService.complete();
        }
    }

    public SRole createSRole(final String roleName) throws SBonitaException {
        transactionService.begin();
        final SRole role = BuilderFactory.get(SRoleBuilderFactory.class).createNewInstance().setName(roleName).done();
        identityService.createRole(role);
        transactionService.complete();
        return role;
    }

    public SUserMembership createSUserMembership(final SUser user, final SGroup group, final SRole role) throws SBonitaException {
        transactionService.begin();
        final SUserMembership userMembership = BuilderFactory.get(SUserMembershipBuilderFactory.class)
                .createNewInstance(user.getId(), group.getId(), role.getId()).done();
        identityService.createUserMembership(userMembership);
        transactionService.complete();
        return userMembership;
    }

    protected List<SFlowNodeInstance> searchFlowNodeInstances(final QueryOptions searchOptions) throws SBonitaException {
        transactionService.begin();
        final List<SFlowNodeInstance> flowNodes = activityInstanceService.searchFlowNodeInstances(SFlowNodeInstance.class, searchOptions);
        transactionService.complete();

        return flowNodes;
    }
}
