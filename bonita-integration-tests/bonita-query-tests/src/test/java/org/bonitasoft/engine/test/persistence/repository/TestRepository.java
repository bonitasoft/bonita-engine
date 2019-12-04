/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import static org.bonitasoft.engine.test.persistence.builder.PersistentObjectBuilder.DEFAULT_TENANT_ID;

import java.util.Random;

import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.SActorMember;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.commons.ClassReflector;
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
import org.bonitasoft.engine.persistence.PersistentObject;
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
import org.bonitasoft.engine.theme.model.STheme;
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
        session.enableFilter("tenantFilter").setParameter("tenantId", DEFAULT_TENANT_ID);
        return session;
    }

    protected Query getNamedQuery(final String queryName) {
        return getSession().getNamedQuery(queryName);
    }

    public void flush() {
        getSession().flush();
    }

    private boolean isTenantIdSet(PersistentObject entity) {
        Long tenantId = getTenantId(entity);
        return tenantId != null && tenantId > 0;
    }

    private Long getTenantId(PersistentObject entity) {
        Long tenantId = null;
        try {
            tenantId = ClassReflector.invokeGetter(entity, "getTenantId");
        } catch (final Exception ignored) {
            //not set
        }
        return tenantId;
    }

    public <T extends PersistentObject> T add(T entity) {
        if (!isTenantIdSet(entity)) {
            entity.setTenantId(DEFAULT_TENANT_ID);
        }
        if (entity.getId()<=0) {
            entity.setId(new Random().nextLong());
        }
        getSession().save(entity);
        return (T) getSession().get(entity.getClass(), new PersistentObjectId(entity.getId(), getTenantId(entity)));
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

    public void update(final SApplication application) {
        getSession().update(application);
    }


    public STheme add(STheme theme) {
        getSession().save(theme);
        return (STheme) getSession().get(theme.getClass(), new PersistentObjectId(theme.getId(), theme.getTenantId()));
    }

}
