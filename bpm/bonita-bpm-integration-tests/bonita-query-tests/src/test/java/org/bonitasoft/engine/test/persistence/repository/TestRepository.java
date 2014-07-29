package org.bonitasoft.engine.test.persistence.repository;

import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.SActorMember;
import org.bonitasoft.engine.actor.mapping.model.impl.SActorImpl;
import org.bonitasoft.engine.actor.mapping.model.impl.SActorMemberImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionDeployInfoImpl;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SPendingActivityMapping;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.impl.SMessageInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.event.handling.impl.SWaitingMessageEventImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SConnectorInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SFlowNodeInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SHiddenTaskInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SPendingActivityMappingImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SProcessInstanceImpl;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.identity.model.impl.SCustomUserInfoDefinitionImpl;
import org.bonitasoft.engine.identity.model.impl.SCustomUserInfoValueImpl;
import org.bonitasoft.engine.identity.model.impl.SGroupImpl;
import org.bonitasoft.engine.identity.model.impl.SRoleImpl;
import org.bonitasoft.engine.identity.model.impl.SUserImpl;
import org.bonitasoft.engine.identity.model.impl.SUserMembershipImpl;
import org.bonitasoft.engine.persistence.PersistentObjectId;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobLog;
import org.bonitasoft.engine.scheduler.model.impl.SJobDescriptorImpl;
import org.bonitasoft.engine.scheduler.model.impl.SJobLogImpl;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisor;
import org.bonitasoft.engine.supervisor.mapping.model.impl.SProcessSupervisorImpl;
import org.bonitasoft.engine.test.persistence.builder.PersistentObjectBuilder;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * Test Repository
 * Need to be used in a transactional context
 */
public class TestRepository {

    private final SessionFactory sessionFactory;

    public TestRepository(final SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    protected Session getSessionWithTenantFilter() {
        Session session = getSession();
        session.enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        return session;
    }

    protected Query getNamedQuery(final String queryName) {
        return getSession().getNamedQuery(queryName);
    }

    /**
     * Need to replicate add method for each object because we don't have any superclass or interface providing getTenantId method
     */
    public SUser add(final SUserImpl user) {
        getSession().save(user);
        return (SUser) getSession().get(user.getClass(), new PersistentObjectId(user.getId(), user.getTenantId()));
    }

    public SRole add(final SRoleImpl role) {
        getSession().save(role);
        return (SRole) getSession().get(role.getClass(), new PersistentObjectId(role.getId(), role.getTenantId()));
    }

    public SGroup add(final SGroupImpl sGroup) {
        getSession().save(sGroup);
        return (SGroup) getSession().get(sGroup.getClass(), new PersistentObjectId(sGroup.getId(), sGroup.getTenantId()));
    }

    public SConnectorInstance add(final SConnectorInstanceImpl sConnectorInstance) {
        getSession().save(sConnectorInstance);
        return (SConnectorInstance) getSession().get(sConnectorInstance.getClass(),
                new PersistentObjectId(sConnectorInstance.getId(), sConnectorInstance.getTenantId()));
    }

    public SPendingActivityMapping add(final SPendingActivityMappingImpl pendingActivityMapping) {
        getSession().save(pendingActivityMapping);
        return (SPendingActivityMapping) getSession().get(pendingActivityMapping.getClass(),
                new PersistentObjectId(pendingActivityMapping.getId(), pendingActivityMapping.getTenantId()));
    }

    public SActorMember add(final SActorMemberImpl actorMember) {
        getSession().save(actorMember);
        return (SActorMember) getSession().get(actorMember.getClass(), new PersistentObjectId(actorMember.getId(), actorMember.getTenantId()));
    }

    public SActor add(final SActorImpl actor) {
        getSession().save(actor);
        return (SActor) getSession().get(actor.getClass(), new PersistentObjectId(actor.getId(), actor.getTenantId()));
    }

    public SUserMembership add(final SUserMembershipImpl membership) {
        getSession().save(membership);
        return (SUserMembership) getSession().get(membership.getClass(), new PersistentObjectId(membership.getId(), membership.getTenantId()));
    }

    public SMessageInstance add(final SMessageInstanceImpl message) {
        getSession().save(message);
        return (SMessageInstance) getSession().get(message.getClass(), new PersistentObjectId(message.getId(), message.getTenantId()));
    }

    public SWaitingMessageEvent add(final SWaitingMessageEventImpl waitingEvent) {
        getSession().save(waitingEvent);
        return (SWaitingMessageEvent) getSession().get(waitingEvent.getClass(), new PersistentObjectId(waitingEvent.getId(), waitingEvent.getTenantId()));
    }

    public SProcessSupervisor add(final SProcessSupervisorImpl sProcessSupervisor) {
        getSession().save(sProcessSupervisor);
        return (SProcessSupervisor) getSession().get(sProcessSupervisor.getClass(),
                new PersistentObjectId(sProcessSupervisor.getId(), sProcessSupervisor.getTenantId()));
    }

    public SProcessDefinitionDeployInfoImpl add(final SProcessDefinitionDeployInfoImpl sProcessDefinitionDeployInfoImpl) {
        getSession().save(sProcessDefinitionDeployInfoImpl);
        return (SProcessDefinitionDeployInfoImpl) getSession().get(sProcessDefinitionDeployInfoImpl.getClass(),
                new PersistentObjectId(sProcessDefinitionDeployInfoImpl.getId(), sProcessDefinitionDeployInfoImpl.getTenantId()));
    }

    public SProcessInstanceImpl add(final SProcessInstanceImpl sProcessInstance) {
        getSession().save(sProcessInstance);
        return (SProcessInstanceImpl) getSession().get(sProcessInstance.getClass(),
                new PersistentObjectId(sProcessInstance.getId(), sProcessInstance.getTenantId()));
    }

    public SFlowNodeInstance add(final SFlowNodeInstanceImpl sFlowNode) {
        getSession().save(sFlowNode);
        return (SFlowNodeInstance) getSession().get(sFlowNode.getClass(), new PersistentObjectId(sFlowNode.getId(), sFlowNode.getTenantId()));
    }

    public SHiddenTaskInstanceImpl add(final SHiddenTaskInstanceImpl sHiddenTaskInstanceImpl) {
        getSession().save(sHiddenTaskInstanceImpl);
        return (SHiddenTaskInstanceImpl) getSession().get(sHiddenTaskInstanceImpl.getClass(),
                new PersistentObjectId(sHiddenTaskInstanceImpl.getId(), sHiddenTaskInstanceImpl.getTenantId()));
    }

    public SCustomUserInfoDefinition add(final SCustomUserInfoDefinitionImpl infoDef) {
        getSession().save(infoDef);
        return (SCustomUserInfoDefinition) getSession().get(infoDef.getClass(), new PersistentObjectId(infoDef.getId(), infoDef.getTenantId()));
    }

    public SCustomUserInfoValue add(final SCustomUserInfoValueImpl infoValue) {
        getSession().save(infoValue);
        return (SCustomUserInfoValue) getSession().get(infoValue.getClass(), new PersistentObjectId(infoValue.getId(), infoValue.getTenantId()));
    }

    public SJobLog addJobLog(final SJobLogImpl jobLog) {
        getSession().save(jobLog);
        return (SJobLog) getSession().get(jobLog.getClass(), new PersistentObjectId(jobLog.getId(), jobLog.getTenantId()));
    }

    public SJobDescriptor addJobDescriptor(final SJobDescriptorImpl jobDescriptor) {
        getSession().save(jobDescriptor);
        return (SJobDescriptor) getSession().get(jobDescriptor.getClass(), new PersistentObjectId(jobDescriptor.getId(), jobDescriptor.getTenantId()));
    }

}
