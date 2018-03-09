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
 **/
package org.bonitasoft.engine.bpm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.PrintTestsStatusRule;
import org.bonitasoft.engine.actor.mapping.SActorCreationException;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.SActorBuilderFactory;
import org.bonitasoft.engine.api.impl.IdentityAPIImpl;
import org.bonitasoft.engine.api.impl.LoginAPIImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.process.impl.internal.DesignProcessDefinitionImpl;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.exception.SDeletingEnabledProcessException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDeletionException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceHierarchicalDeletionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
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
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.SIdentityException;
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
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.test.junit.BonitaEngineRule;
import org.bonitasoft.engine.test.util.PlatformUtil;
import org.bonitasoft.engine.test.util.TestUtil;
import org.bonitasoft.engine.transaction.STransactionCommitException;
import org.bonitasoft.engine.transaction.STransactionCreationException;
import org.bonitasoft.engine.transaction.STransactionException;
import org.bonitasoft.engine.transaction.STransactionRollbackException;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class CommonBPMServicesTest {

    @Rule
    public BonitaEngineRule bonitaEngineRule = BonitaEngineRule.create();

    private final static Logger LOGGER = LoggerFactory.getLogger(CommonBPMServicesTest.class);
    protected static SessionAccessor sessionAccessor;
    protected static PlatformServiceAccessor platformServiceAccessor;
    protected static Map<Long, TenantServiceAccessor> tenantServiceAccessors;
    private static long tenantId;
    @Rule
    public TestRule testWatcher = new PrintTestsStatusRule(LOGGER) {

        @Override
        public void clean() throws Exception {
            CommonBPMServicesTest.this.clean();
        }

    };

    private APISession apiSession = null;

    protected ServiceAccessorFactory getServiceAccessorFactory() {
        return ServiceAccessorFactory.getInstance();
    }

    protected long getDefaultTenantId() {
        return tenantId;
    }

    APISession getAPISession() {
        return this.apiSession;
    }

    protected SessionAccessor getSessionAccessor() {
        return sessionAccessor;
    }

    protected TenantServiceAccessor getAccessor(final long tenantId) throws Exception {
        if (!tenantServiceAccessors.containsKey(tenantId)) {
            tenantServiceAccessors.put(tenantId, getServiceAccessorFactory().createTenantServiceAccessor(tenantId));
        }
        return tenantServiceAccessors.get(tenantId);
    }

    protected TenantServiceAccessor getTenantAccessor() {
        try {
            return getAccessor(getDefaultTenantId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected PlatformServiceAccessor getPlatformAccessor() {
        return platformServiceAccessor;
    }

    protected TransactionService getTransactionService() {
        return getPlatformAccessor().getTransactionService();
    }

    @Before
    public void doNotOverrideBefore() throws Exception {
        tenantServiceAccessors = new HashMap<>();
        apiSession = new LoginAPIImpl().login(TestUtil.getDefaultUserName(), TestUtil.getDefaultPassword());
        tenantId = apiSession.getTenantId();
        if (sessionAccessor == null) {
            sessionAccessor = getServiceAccessorFactory().createSessionAccessor();
        }
        if (platformServiceAccessor == null) {
            platformServiceAccessor = getServiceAccessorFactory().createPlatformServiceAccessor();
        }

        apiSession = new LoginAPIImpl().login(TestUtil.getDefaultUserName(), TestUtil.getDefaultPassword());
        tenantId = apiSession.getTenantId();
        sessionAccessor.setSessionInfo(apiSession.getId(), tenantId);
    }

    protected Group createGroup(final String groupName) throws CreationException {
        return createGroup(groupName, null);
    }

    protected Group createGroup(final String groupName, final String groupPath) throws CreationException {
        try {
            openTx();
            final Group group = new IdentityAPIImpl().createGroup(groupName, groupPath);
            closeTx();
            return group;
        } catch (final STransactionException e) {
            throw new CreationException(e);
        }
    }

    @After
    public void after() throws Exception {
        TestUtil.closeTransactionIfOpen(platformServiceAccessor.getTransactionService());
        if (apiSession != null) {
            new LoginAPIImpl().logout(apiSession);
        }
    }

    private void clean() throws Exception {
        try {
            final APISession apiSession = new LoginAPIImpl().login(TestUtil.getDefaultUserName(), TestUtil.getDefaultPassword());
            openTx();
            cleanProcessesDefinitions();
            cleanProcessInstance();
            cleanMemberships();
            cleaRoles();
            cleanUsers();
            cleanGroups();
            getTenantAccessor().getLoginService().logout(apiSession.getId());
        } finally {
            if (platformServiceAccessor.getTransactionService().isTransactionActive()) {
                closeTx();
            }
        }
    }

    private void cleanProcessesDefinitions() throws SBonitaReadException, SProcessDefinitionNotFoundException,
            SProcessDeletionException, SDeletingEnabledProcessException {
        final QueryOptions queryOptions = new QueryOptions(0, 200, SProcessDefinitionDeployInfo.class, "name", OrderByType.ASC);
        final List<SProcessDefinitionDeployInfo> processes = getTenantAccessor().getProcessDefinitionService().getProcessDeploymentInfos(queryOptions);
        if (processes.size() > 0) {
            for (final SProcessDefinitionDeployInfo process : processes) {
                try {
                    getTenantAccessor().getProcessDefinitionService().disableProcessDeploymentInfo(process.getProcessId());
                } catch (final Throwable ignored) {
                }
                getTenantAccessor().getProcessDefinitionService().delete(process.getProcessId());
            }
        }
    }

    private void cleanProcessInstance() throws SBonitaReadException, SProcessInstanceNotFoundException, SProcessInstanceReadException, SFlowNodeReadException,
            SProcessInstanceModificationException, SProcessInstanceHierarchicalDeletionException {
        // let's clean up All Process Instances:
        List<SProcessInstance> processInstances = getFirstProcessInstances(5000);
        while (processInstances.size() > 0) {
            for (final SProcessInstance sProcessInstance : processInstances) {
                getTenantAccessor().getProcessInstanceService().deleteProcessInstance(sProcessInstance.getId());
            }
            // get the next 100:
            processInstances = getFirstProcessInstances(5000);
        }
    }

    private void cleanMemberships() throws SIdentityException {
        for (final SUserMembership sMembership : getTenantAccessor().getIdentityService().getUserMemberships(0, 5000)) {
            getTenantAccessor().getIdentityService().deleteUserMembership(sMembership);
        }
    }

    private void cleaRoles() throws SIdentityException {
        for (final SRole sRole : getTenantAccessor().getIdentityService().getRoles(0, 5000)) {
            getTenantAccessor().getIdentityService().deleteRole(sRole);
        }
    }

    private void cleanUsers() throws SIdentityException {
        for (final SUser sUser : getTenantAccessor().getIdentityService().getUsers(0, 5000)) {
            getTenantAccessor().getIdentityService().deleteUser(sUser);
        }
    }

    private void cleanGroups() throws SIdentityException {
        for (final SGroup sGroup : getTenantAccessor().getIdentityService().getGroups(0, 5000)) {
            getTenantAccessor().getIdentityService().deleteGroup(sGroup);
        }
    }

    public List<SProcessInstance> getFirstProcessInstances(final int nb) throws SBonitaReadException {
        // we are already in a transaction context here:
        final OrderByOption orderByOption = new OrderByOption(SProcessInstance.class, BuilderFactory.get(SProcessInstanceBuilderFactory.class)
                .getLastUpdateKey(), OrderByType.DESC);
        final QueryOptions queryOptions = new QueryOptions(0, nb, Collections.singletonList(orderByOption), Collections.<FilterOption> emptyList(), null);
        return getTenantAccessor().getProcessInstanceService().searchProcessInstances(queryOptions);
    }

    protected SProcessInstance createSProcessInstance() throws SBonitaException {
        final SProcessInstance processInstance = BuilderFactory.get(SProcessInstanceBuilderFactory.class).createNewInstance("process", 1).done();

        openTx();
        getTenantAccessor().getProcessInstanceService().createProcessInstance(processInstance);
        closeTx();

        return processInstance;
    }

    protected void deleteSProcessInstance(final SProcessInstance processInstance) throws SBonitaException {
        openTx();
        getTenantAccessor().getProcessInstanceService().deleteProcessInstance(processInstance.getId());
        closeTx();
    }

    protected SFlowNodeInstance getFlowNodeInstance(final long flowNodeInstanceId) throws SBonitaException {
        SFlowNodeInstance flowNodeInstance = null;
        openTx();
        try {
            flowNodeInstance = getTenantAccessor().getActivityInstanceService().getFlowNodeInstance(flowNodeInstanceId);
        } finally {
            closeTx();
        }
        return flowNodeInstance;
    }

    protected SEventInstance createSStartEventInstance(final String eventName, final long flowNodeDefinitionId, final long rootProcessInstanceId,
            final long processDefinitionId, final long parentProcessInstanceId) throws STransactionCreationException, SEventInstanceCreationException,
            STransactionCommitException, STransactionRollbackException {
        final SEventInstance eventInstance = BuilderFactory
                .get(SStartEventInstanceBuilderFactory.class)
                .createNewStartEventInstance(eventName, flowNodeDefinitionId, rootProcessInstanceId, rootProcessInstanceId, processDefinitionId,
                        rootProcessInstanceId, parentProcessInstanceId)
                .done();
        createSEventInstance(eventInstance);
        return eventInstance;
    }

    protected SEventInstance createSEndEventInstance(final String eventName, final long flowNodeDefinitionId, final long rootProcessInstanceId,
            final long processDefinitionId, final long parentProcessInstanceId) throws STransactionCreationException, SEventInstanceCreationException,
            STransactionCommitException, STransactionRollbackException {
        final SEventInstance eventInstance = BuilderFactory
                .get(SEndEventInstanceBuilderFactory.class)
                .createNewEndEventInstance(eventName, flowNodeDefinitionId, rootProcessInstanceId, rootProcessInstanceId, processDefinitionId,
                        rootProcessInstanceId, parentProcessInstanceId)
                .done();
        createSEventInstance(eventInstance);
        return eventInstance;
    }

    protected SEventInstance createSIntermediateCatchEventInstance(final String eventName, final long flowNodeDefinitionId, final long rootProcessInstanceId,
            final long processDefinitionId, final long parentProcessInstanceId) throws STransactionCreationException, SEventInstanceCreationException,
            STransactionCommitException, STransactionRollbackException {
        final SEventInstance eventInstance = BuilderFactory
                .get(SIntermediateCatchEventInstanceBuilderFactory.class)
                .createNewIntermediateCatchEventInstance(eventName, flowNodeDefinitionId, rootProcessInstanceId, parentProcessInstanceId, processDefinitionId,
                        rootProcessInstanceId, parentProcessInstanceId)
                .done();
        createSEventInstance(eventInstance);
        return eventInstance;
    }

    protected SEventInstance createSIntermediateThrowEventInstance(final String eventName, final long flowNodeDefinitionId, final long processInstanceId,
            final long processDefinitionId, final long parentProcessInstanceId) throws STransactionCreationException, SEventInstanceCreationException,
            STransactionCommitException, STransactionRollbackException {
        final SEventInstance eventInstance = BuilderFactory
                .get(SIntermediateThrowEventInstanceBuilderFactory.class)
                .createNewIntermediateThrowEventInstance(eventName, flowNodeDefinitionId, processInstanceId, processInstanceId, processDefinitionId,
                        processInstanceId, parentProcessInstanceId)
                .done();
        createSEventInstance(eventInstance);
        return eventInstance;
    }

    protected void createSEventInstance(final SEventInstance eventInstance) throws STransactionCreationException, SEventInstanceCreationException,
            STransactionCommitException, STransactionRollbackException {
        openTx();
        getTenantAccessor().getEventInstanceService().createEventInstance(eventInstance);
        closeTx();
    }

    protected void insertGatewayInstance(final SGatewayInstance gatewayInstance) throws SBonitaException {
        openTx();
        getTenantAccessor().getGatewayInstanceService().createGatewayInstance(gatewayInstance);
        closeTx();
    }

    protected SUserTaskInstance createSUserTaskInstance(final String name, final long flowNodeDefinitionId, final long parentId,
            final long processDefinitionId,
            final long rootProcessInst, final long actorId) throws SBonitaException {
        final SUserTaskInstance taskInstance = BuilderFactory.get(SUserTaskInstanceBuilderFactory.class)
                .createNewUserTaskInstance(name, flowNodeDefinitionId, rootProcessInst, parentId, actorId, processDefinitionId, rootProcessInst, parentId)
                .done();
        openTx();
        getTenantAccessor().getActivityInstanceService().createActivityInstance(taskInstance);
        closeTx();
        return taskInstance;
    }

    protected SActivityInstance createSAutomaticTaskInstance(final String name, final long flowNodeDefinitionId, final long parentId,
            final long processDefinitionId, final long rootProcessInst) throws SBonitaException {
        final SActivityInstance taskInstance = BuilderFactory.get(SAutomaticTaskInstanceBuilderFactory.class)
                .createNewAutomaticTaskInstance(name, flowNodeDefinitionId, rootProcessInst, parentId, processDefinitionId, rootProcessInst, parentId).done();
        openTx();
        getTenantAccessor().getActivityInstanceService().createActivityInstance(taskInstance);
        closeTx();
        return taskInstance;
    }

    protected SProcessDefinition buildSProcessDefinition(final String name, final String version) throws SProcessDefinitionException {
        final DesignProcessDefinitionImpl designProcessDefinition = new DesignProcessDefinitionImpl(name, version);
        designProcessDefinition.setProcessContainer(new FlowElementContainerDefinitionImpl());
        return getTenantAccessor().getProcessDefinitionService().store(designProcessDefinition);
    }

    private SActor buildSActor(final String name, final long scopeId, final boolean initiator) throws SActorCreationException {
        final SActorBuilderFactory sActorBuilderFactory = BuilderFactory.get(SActorBuilderFactory.class);
        final SActor sActor = sActorBuilderFactory.create(name, scopeId, initiator).getActor();
        return getTenantAccessor().getActorMappingService().addActor(sActor);
    }

    public SProcessDefinition createSProcessDefinitionWithSActor(final String name, final String version, final String actorName,
            final boolean actorIsInitiator, final List<SUser> sUsersToAddToActor) throws SBonitaException {
        openTx();

        final SProcessDefinition sProcessDefinition = buildSProcessDefinition(name, version);
        getTenantAccessor().getProcessDefinitionService().resolveProcess(sProcessDefinition.getId());
        getTenantAccessor().getProcessDefinitionService().enableProcessDeploymentInfo(sProcessDefinition.getId());

        final SActor sActor = buildSActor(actorName, sProcessDefinition.getId(), actorIsInitiator);
        for (final SUser sUser : sUsersToAddToActor) {
            getTenantAccessor().getActorMappingService().addUserToActor(sActor.getId(), sUser.getId());
        }

        closeTx();
        return sProcessDefinition;
    }

    public SProcessDefinition createSProcessDefinition(final String name, final String version) throws SBonitaException {
        openTx();

        final SProcessDefinition sProcessDefinition = buildSProcessDefinition(name, version);
        getTenantAccessor().getProcessDefinitionService().resolveProcess(sProcessDefinition.getId());
        getTenantAccessor().getProcessDefinitionService().enableProcessDeploymentInfo(sProcessDefinition.getId());

        closeTx();
        return sProcessDefinition;
    }

    public List<SProcessDefinition> createSProcessDefinitions(final int count, final String name, final String version) throws SBonitaException {
        final List<SProcessDefinition> processDefinitons = new ArrayList<>();

        openTx();
        for (int i = 1; i <= count; i++) {
            final SProcessDefinition sProcessDefinition = buildSProcessDefinition(name + i, version + i);
            getTenantAccessor().getProcessDefinitionService().resolveProcess(sProcessDefinition.getId());
            getTenantAccessor().getProcessDefinitionService().enableProcessDeploymentInfo(sProcessDefinition.getId());
            processDefinitons.add(sProcessDefinition);
        }
        closeTx();
        return processDefinitons;
    }

    public void deleteSProcessDefinition(final SProcessDefinition sProcessDefinition) throws SBonitaException {
        deleteSProcessDefinition(sProcessDefinition.getId());
    }

    public void deleteSProcessDefinition(final long sProcessDefinitionId) throws SBonitaException {
        openTx();
        getTenantAccessor().getActorMappingService().deleteActors(sProcessDefinitionId);
        getTenantAccessor().getProcessDefinitionService().disableProcessDeploymentInfo(sProcessDefinitionId);
        getTenantAccessor().getProcessDefinitionService().delete(sProcessDefinitionId);
        closeTx();
    }

    void openTx() throws STransactionCreationException {
        platformServiceAccessor.getTransactionService().begin();
    }

    public void deleteSProcessDefinitions(final SProcessDefinition... sProcessDefinitions) throws SBonitaException {
        if (sProcessDefinitions != null) {
            deleteSProcessDefinitions(Arrays.asList(sProcessDefinitions));
        }
    }

    public void deleteSProcessDefinitions(final List<SProcessDefinition> sProcessDefinitions) throws SBonitaException {
        openTx();
        for (final SProcessDefinition sProcessDefinition : sProcessDefinitions) {
            getTenantAccessor().getActorMappingService().deleteActors(sProcessDefinition.getId());
            getTenantAccessor().getProcessDefinitionService().disableProcessDeploymentInfo(sProcessDefinition.getId());
            getTenantAccessor().getProcessDefinitionService().delete(sProcessDefinition.getId());
        }
        closeTx();
    }

    public List<SUser> createEnabledSUsers(final int count, final String firstName, final String lastName, final String password) throws SBonitaException {
        openTx();
        final List<SUser> users = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            users.add(getTenantAccessor().getIdentityService().createUser(buildEnabledSUser(firstName + i, lastName + i, password + i, 0)));
        }
        closeTx();

        return users;
    }

    public SUser createEnabledSUser(final String firstName, final String lastName, final String password) throws SBonitaException {
        return createEnabledSUser(firstName, lastName, password, 0);
    }

    public SUser createEnabledSUser(final String firstName, final String lastName, final String password, final long managerUserId) throws SBonitaException {
        openTx();
        final SUser user = getTenantAccessor().getIdentityService().createUser(buildEnabledSUser(firstName, lastName, password, managerUserId));
        closeTx();

        return user;
    }

    public SUser buildEnabledSUser(final String firstName, final String lastName, final String password, final long managerUserId) {
        final SUserBuilder userBuilder = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance();
        userBuilder.setCreatedBy(2);
        userBuilder.setCreationDate(6);
        userBuilder.setEnabled(true);
        userBuilder.setFirstName(firstName);
        userBuilder.setJobTitle("jobTitle");
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
        openTx();
        final SUserBuilder userBuilder = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName(username).setPassword(password);
        final SUser user = getTenantAccessor().getIdentityService().createUser(userBuilder.done());
        closeTx();
        return user;
    }

    public void deleteSUser(final SUser sUser) throws SBonitaException {
        openTx();
        getTenantAccessor().getIdentityService().deleteUser(sUser);
        closeTx();
    }

    public void deleteSUsers(final SUser... users) throws SBonitaException {
        if (users != null) {
            deleteSUsers(Arrays.asList(users));
        }
    }

    public void deleteSUsers(final List<SUser> users) throws SBonitaException {
        openTx();
        for (final SUser sUser : users) {
            getTenantAccessor().getIdentityService().deleteUser(sUser);
        }
        closeTx();
    }

    public void deleteSGroup(final SGroup sGroup) throws SBonitaException {
        openTx();
        getTenantAccessor().getIdentityService().deleteGroup(sGroup);
        closeTx();
    }

    public void deleteSGroups(final SGroup... groups) throws SBonitaException {
        if (groups != null) {
            openTx();
            for (final SGroup sGroup : groups) {
                getTenantAccessor().getIdentityService().deleteGroup(sGroup);
            }
            closeTx();
        }
    }

    public void deleteSRole(final SRole sRole) throws SBonitaException {
        openTx();
        getTenantAccessor().getIdentityService().deleteRole(sRole);
        closeTx();
    }

    public void deleteSRoles(final SRole... roles) throws SBonitaException {
        if (roles != null) {
            openTx();
            for (final SRole sRole : roles) {
                getTenantAccessor().getIdentityService().deleteRole(sRole);
            }
            closeTx();
        }
    }

    public SRole createSRole(final String roleName) throws SBonitaException {
        openTx();
        final SRole role = BuilderFactory.get(SRoleBuilderFactory.class).createNewInstance().setName(roleName).done();
        getTenantAccessor().getIdentityService().createRole(role, null, null);
        closeTx();
        return role;
    }

    public SUserMembership createSUserMembership(final SUser user, final SGroup group, final SRole role) throws SBonitaException {
        openTx();
        final SUserMembership userMembership = BuilderFactory.get(SUserMembershipBuilderFactory.class)
                .createNewInstance(user.getId(), group.getId(), role.getId()).done();
        getTenantAccessor().getIdentityService().createUserMembership(userMembership);
        closeTx();
        return userMembership;
    }

    protected List<SFlowNodeInstance> searchFlowNodeInstances(final QueryOptions searchOptions) throws SBonitaException {
        openTx();
        final List<SFlowNodeInstance> flowNodes = getTenantAccessor().getActivityInstanceService().searchFlowNodeInstances(SFlowNodeInstance.class,
                searchOptions);
        closeTx();

        return flowNodes;
    }

    private void closeTx() throws STransactionCommitException, STransactionRollbackException {
        platformServiceAccessor.getTransactionService().complete();
    }

    protected long createTenant(String tenantName) throws Exception {
        return PlatformUtil.createTenant(getTransactionService(), getPlatformAccessor().getPlatformService(), tenantName,
                PlatformUtil.DEFAULT_CREATED_BY, PlatformUtil.TENANT_STATUS_ACTIVATED);
    }

    protected void changeTenant(final long tenantId) throws Exception {
        getTransactionService().begin();
        TestUtil.createSessionOn(getSessionAccessor(), getTenantAccessor().getSessionService(), tenantId);
        getTransactionService().complete();
    }

}
