package org.bonitasoft.engine.test.persistence.repository;

import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.SActorMember;
import org.bonitasoft.engine.actor.mapping.model.impl.SActorImpl;
import org.bonitasoft.engine.actor.mapping.model.impl.SActorMemberImpl;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SPendingActivityMapping;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.impl.SMessageInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.event.handling.impl.SWaitingMessageEventImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SConnectorInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SPendingActivityMappingImpl;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.identity.model.impl.SGroupImpl;
import org.bonitasoft.engine.identity.model.impl.SRoleImpl;
import org.bonitasoft.engine.identity.model.impl.SUserImpl;
import org.bonitasoft.engine.identity.model.impl.SUserMembershipImpl;
import org.bonitasoft.engine.persistence.PersistentObjectId;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisor;
import org.bonitasoft.engine.supervisor.mapping.model.impl.SProcessSupervisorImpl;
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
}
