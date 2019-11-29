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
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SPendingActivityMapping;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.data.instance.model.archive.SADataInstance;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.SUserLogin;
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.page.SPageWithContent;
import org.bonitasoft.engine.persistence.PersistentObjectId;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.SProfileEntry;
import org.bonitasoft.engine.profile.model.SProfileMember;
import org.bonitasoft.engine.resources.SBARResource;
import org.bonitasoft.engine.resources.STenantResource;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobLog;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisor;
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

    public Session getSession() {
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

    public void flush() {
        getSession().flush();
    }

    /**
     * Need to replicate add method for each object because we don't have any superclass or interface providing getTenantId method
     */
    public SUser add(final SUser user) {
        getSession().save(user);
        return (SUser) getSession().get(user.getClass(), new PersistentObjectId(user.getId(), user.getTenantId()));
    }
    public SUserLogin add(final SUserLogin userLogin) {
        getSession().save(userLogin);
        return (SUserLogin) getSession().get(userLogin.getClass(), new PersistentObjectId(userLogin.getId(), userLogin.getTenantId()));
    }

    public SRole add(final SRole role) {
        getSession().save(role);
        return (SRole) getSession().get(role.getClass(), new PersistentObjectId(role.getId(), role.getTenantId()));
    }

    public SGroup add(final SGroup sGroup) {
        getSession().save(sGroup);
        return (SGroup) getSession().get(sGroup.getClass(), new PersistentObjectId(sGroup.getId(), sGroup.getTenantId()));
    }

    public SConnectorInstance add(final SConnectorInstance sConnectorInstance) {
        getSession().save(sConnectorInstance);
        return (SConnectorInstance) getSession().get(sConnectorInstance.getClass(),
                new PersistentObjectId(sConnectorInstance.getId(), sConnectorInstance.getTenantId()));
    }

    public SPendingActivityMapping add(final SPendingActivityMapping pendingActivityMapping) {
        getSession().save(pendingActivityMapping);
        return (SPendingActivityMapping) getSession().get(pendingActivityMapping.getClass(),
                new PersistentObjectId(pendingActivityMapping.getId(), pendingActivityMapping.getTenantId()));
    }

    public SActorMember add(final SActorMember actorMember) {
        getSession().save(actorMember);
        return (SActorMember) getSession().get(actorMember.getClass(), new PersistentObjectId(actorMember.getId(), actorMember.getTenantId()));
    }

    public SActor add(final SActor actor) {
        getSession().save(actor);
        return (SActor) getSession().get(actor.getClass(), new PersistentObjectId(actor.getId(), actor.getTenantId()));
    }

    public SUserMembership add(final SUserMembership membership) {
        getSession().save(membership);
        return (SUserMembership) getSession().get(membership.getClass(), new PersistentObjectId(membership.getId(), membership.getTenantId()));
    }

    public SMessageInstance add(final SMessageInstance message) {
        getSession().save(message);
        return (SMessageInstance) getSession().get(message.getClass(), new PersistentObjectId(message.getId(), message.getTenantId()));
    }

    public SWaitingMessageEvent add(final SWaitingMessageEvent waitingEvent) {
        getSession().save(waitingEvent);
        return (SWaitingMessageEvent) getSession().get(waitingEvent.getClass(), new PersistentObjectId(waitingEvent.getId(), waitingEvent.getTenantId()));
    }

    public SProcessSupervisor add(final SProcessSupervisor sProcessSupervisor) {
        getSession().save(sProcessSupervisor);
        return (SProcessSupervisor) getSession().get(sProcessSupervisor.getClass(),
                new PersistentObjectId(sProcessSupervisor.getId(), sProcessSupervisor.getTenantId()));
    }

    public SProcessDefinitionDeployInfo add(final SProcessDefinitionDeployInfo sProcessDefinitionDeployInfo) {
        getSession().save(sProcessDefinitionDeployInfo);
        return (SProcessDefinitionDeployInfo) getSession().get(sProcessDefinitionDeployInfo.getClass(),
                new PersistentObjectId(sProcessDefinitionDeployInfo.getId(), sProcessDefinitionDeployInfo.getTenantId()));
    }

    public SProcessInstance add(final SProcessInstance sProcessInstance) {
        getSession().save(sProcessInstance);
        return (SProcessInstance) getSession().get(sProcessInstance.getClass(),
                new PersistentObjectId(sProcessInstance.getId(), sProcessInstance.getTenantId()));
    }

    public SAProcessInstance add(final SAProcessInstance saProcessInstance) {
        getSession().save(saProcessInstance);
        return (SAProcessInstance) getSession().get(saProcessInstance.getClass(),
                new PersistentObjectId(saProcessInstance.getId(), saProcessInstance.getTenantId()));
    }

    public SFlowNodeInstance add(final SFlowNodeInstance sFlowNode) {
        getSession().save(sFlowNode);
        return (SFlowNodeInstance) getSession().get(sFlowNode.getClass(), new PersistentObjectId(sFlowNode.getId(), sFlowNode.getTenantId()));
    }

    public SAFlowNodeInstance add(final SAFlowNodeInstance saFlowNode) {
        getSession().save(saFlowNode);
        return (SAFlowNodeInstance) getSession().get(saFlowNode.getClass(), new PersistentObjectId(saFlowNode.getId(), saFlowNode.getTenantId()));
    }

    public STimerEventTriggerInstance add(final STimerEventTriggerInstance sTimerEventTriggerInstance) {
        getSession().save(sTimerEventTriggerInstance);
        return (STimerEventTriggerInstance) getSession().get(sTimerEventTriggerInstance.getClass(),
                new PersistentObjectId(sTimerEventTriggerInstance.getId(), sTimerEventTriggerInstance.getTenantId()));
    }

    public SCustomUserInfoDefinition add(final SCustomUserInfoDefinition infoDef) {
        getSession().save(infoDef);
        return (SCustomUserInfoDefinition) getSession().get(infoDef.getClass(), new PersistentObjectId(infoDef.getId(), infoDef.getTenantId()));
    }

    public SCustomUserInfoValue add(final SCustomUserInfoValue infoValue) {
        getSession().save(infoValue);
        return (SCustomUserInfoValue) getSession().get(infoValue.getClass(), new PersistentObjectId(infoValue.getId(), infoValue.getTenantId()));
    }

    public SJobLog addJobLog(final SJobLog jobLog) {
        getSession().save(jobLog);
        return (SJobLog) getSession().get(jobLog.getClass(), new PersistentObjectId(jobLog.getId(), jobLog.getTenantId()));
    }

    public SJobDescriptor addJobDescriptor(final SJobDescriptor jobDescriptor) {
        getSession().save(jobDescriptor);
        return (SJobDescriptor) getSession().get(jobDescriptor.getClass(), new PersistentObjectId(jobDescriptor.getId(), jobDescriptor.getTenantId()));
    }

    public SADataInstance add(final SADataInstance dataInstance) {
        getSession().save(dataInstance);
        return (SADataInstance) getSession().get(dataInstance.getClass(), new PersistentObjectId(dataInstance.getId(), dataInstance.getTenantId()));
    }

    /**
     * Need to replicate add method for each object because we don't have any superclass or interface providing getTenantId method
     */
    public SPageWithContent add(final SPageWithContent sPageWithContent) {
        getSession().save(sPageWithContent);
        return (SPageWithContent) getSession().get(sPageWithContent.getClass(),
                new PersistentObjectId(sPageWithContent.getId(), sPageWithContent.getTenantId()));
    }

    public SApplication add(final SApplication application) {
        getSession().save(application);
        return (SApplication) getSession().get(application.getClass(),
                new PersistentObjectId(application.getId(), application.getTenantId()));
    }

    public SApplicationPage add(final SApplicationPage applicationPage) {
        getSession().save(applicationPage);
        return (SApplicationPage) getSession().get(applicationPage.getClass(),
                new PersistentObjectId(applicationPage.getId(), applicationPage.getTenantId()));
    }

    public SApplicationMenu add(final SApplicationMenu applicationMenu) {
        getSession().save(applicationMenu);
        return (SApplicationMenu) getSession().get(applicationMenu.getClass(),
                new PersistentObjectId(applicationMenu.getId(), applicationMenu.getTenantId()));
    }

    public SProfile add(final SProfile profile) {
        getSession().save(profile);
        return (SProfile) getSession().get(profile.getClass(),
                new PersistentObjectId(profile.getId(), profile.getTenantId()));
    }

    public SProfileEntry add(SProfileEntry profileEntry) {
        getSession().save(profileEntry);
        return (SProfileEntry) getSession().get(profileEntry.getClass(),
                new PersistentObjectId(profileEntry.getId(), profileEntry.getTenantId()));
    }

    public SProfileMember add(SProfileMember profileMember) {
        getSession().save(profileMember);
        return (SProfileMember) getSession().get(profileMember.getClass(),
                new PersistentObjectId(profileMember.getId(), profileMember.getTenantId()));
    }

    public SBARResource add(final SBARResource sbarResource) {
        getSession().save(sbarResource);
        return (SBARResource) getSession().get(sbarResource.getClass(),
                new PersistentObjectId(sbarResource.getId(), sbarResource.getTenantId()));
    }

    public STenantResource add(final STenantResource sTenantResource) {
        getSession().save(sTenantResource);
        return (STenantResource) getSession().get(sTenantResource.getClass(),
                new PersistentObjectId(sTenantResource.getId(), sTenantResource.getTenantId()));
    }

    public void update(final SApplication application) {
        getSession().update(application);
    }

    public SDependency add(SDependency dependency){
        getSession().save(dependency);
        return (SDependency)getSession().get(dependency.getClass(),
                new PersistentObjectId(dependency.getId(), dependency.getTenantId()));
    }

    public SDependencyMapping add(SDependencyMapping dependencyMapping){
        getSession().save(dependencyMapping);
        return (SDependencyMapping) getSession().get(dependencyMapping.getClass(),
                new PersistentObjectId(dependencyMapping.getId(), dependencyMapping.getTenantId()));
    }

}
