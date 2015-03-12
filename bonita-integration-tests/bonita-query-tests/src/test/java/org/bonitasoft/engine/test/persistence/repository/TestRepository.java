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
package org.bonitasoft.engine.test.persistence.repository;

import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.SActorMember;
import org.bonitasoft.engine.actor.mapping.model.impl.SActorImpl;
import org.bonitasoft.engine.actor.mapping.model.impl.SActorMemberImpl;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.business.application.model.impl.SApplicationImpl;
import org.bonitasoft.engine.business.application.model.impl.SApplicationMenuImpl;
import org.bonitasoft.engine.business.application.model.impl.SApplicationPageImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionDeployInfoImpl;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SPendingActivityMapping;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.impl.SAFlowNodeInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.archive.impl.SAProcessInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.impl.SMessageInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.event.handling.impl.SWaitingMessageEventImpl;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.impl.SEventTriggerInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SConnectorInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SFlowNodeInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SPendingActivityMappingImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SProcessInstanceImpl;
import org.bonitasoft.engine.data.instance.model.archive.SADataInstance;
import org.bonitasoft.engine.data.instance.model.archive.impl.SADataInstanceImpl;
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
import org.bonitasoft.engine.page.SPageWithContent;
import org.bonitasoft.engine.page.impl.SPageWithContentImpl;
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
        final Session session = getSession();
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

    public SAProcessInstanceImpl add(final SAProcessInstanceImpl saProcessInstance) {
        getSession().save(saProcessInstance);
        return (SAProcessInstanceImpl) getSession().get(saProcessInstance.getClass(),
                new PersistentObjectId(saProcessInstance.getId(), saProcessInstance.getTenantId()));
    }

    public SFlowNodeInstance add(final SFlowNodeInstanceImpl sFlowNode) {
        getSession().save(sFlowNode);
        return (SFlowNodeInstance) getSession().get(sFlowNode.getClass(), new PersistentObjectId(sFlowNode.getId(), sFlowNode.getTenantId()));
    }

    public SAFlowNodeInstance add(final SAFlowNodeInstanceImpl saFlowNode) {
        getSession().save(saFlowNode);
        return (SAFlowNodeInstance) getSession().get(saFlowNode.getClass(), new PersistentObjectId(saFlowNode.getId(), saFlowNode.getTenantId()));
    }

    public SEventTriggerInstanceImpl add(final SEventTriggerInstanceImpl sEventTriggerInstanceImpl) {
        getSession().save(sEventTriggerInstanceImpl);
        return (SEventTriggerInstanceImpl) getSession().get(sEventTriggerInstanceImpl.getClass(),
                new PersistentObjectId(sEventTriggerInstanceImpl.getId(), sEventTriggerInstanceImpl.getTenantId()));
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

    public SADataInstance add(final SADataInstanceImpl dataInstance) {
        getSession().save(dataInstance);
        return (SADataInstance) getSession().get(dataInstance.getClass(), new PersistentObjectId(dataInstance.getId(), dataInstance.getTenantId()));
    }

    /**
     * Need to replicate add method for each object because we don't have any superclass or interface providing getTenantId method
     */
    public SPageWithContent add(final SPageWithContentImpl sPageWithContentImpl) {
        getSession().save(sPageWithContentImpl);
        return (SPageWithContentImpl) getSession().get(sPageWithContentImpl.getClass(),
                new PersistentObjectId(sPageWithContentImpl.getId(), sPageWithContentImpl.getTenantId()));
    }

    public SApplication add(final SApplicationImpl application) {
        getSession().save(application);
        return (SApplication) getSession().get(application.getClass(),
                new PersistentObjectId(application.getId(), application.getTenantId()));
    }

    public SApplicationPage add(final SApplicationPageImpl applicationPage) {
        getSession().save(applicationPage);
        return (SApplicationPage) getSession().get(applicationPage.getClass(),
                new PersistentObjectId(applicationPage.getId(), applicationPage.getTenantId()));
    }

    public SApplicationMenu add(final SApplicationMenuImpl applicationMenu) {
        getSession().save(applicationMenu);
        return (SApplicationMenu) getSession().get(applicationMenu.getClass(),
                new PersistentObjectId(applicationMenu.getId(), applicationMenu.getTenantId()));
    }

    public void update(final SApplicationImpl application) {
        getSession().update(application);
    }

}
