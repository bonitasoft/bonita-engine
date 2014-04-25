package org.bonitasoft.engine.test.persistence.repository;

import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.SActorMember;
import org.bonitasoft.engine.actor.mapping.model.impl.SActorImpl;
import org.bonitasoft.engine.actor.mapping.model.impl.SActorMemberImpl;
import org.bonitasoft.engine.core.process.instance.model.SPendingActivityMapping;
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

    public TestRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    protected Query getNamedQuery(String queryName) {
        return getSession().getNamedQuery(queryName);
    }

    /**
     * Need to replicate add method for each object because we don't have any superclass or interface providing getTenantId method
     */
    public SUser add(SUserImpl user) {
        getSession().save(user);
        return (SUser) getSession().get(user.getClass(), new PersistentObjectId(user.getId(), user.getTenantId()));
    }

    public SRole add(final SRoleImpl role) {
        getSession().save(role);
        return (SRole) getSession().get(role.getClass(), new PersistentObjectId(role.getId(), role.getTenantId()));
    }

    public SGroup add(SGroupImpl sGroup) {
        getSession().save(sGroup);
        return (SGroup) getSession().get(sGroup.getClass(), new PersistentObjectId(sGroup.getId(), sGroup.getTenantId()));
    }

    public SPendingActivityMapping add(SPendingActivityMappingImpl pendingActivityMapping) {
        getSession().save(pendingActivityMapping);
        return (SPendingActivityMapping) getSession().get(pendingActivityMapping.getClass(),
                new PersistentObjectId(pendingActivityMapping.getId(), pendingActivityMapping.getTenantId()));
    }

    public SActorMember add(SActorMemberImpl actorMember) {
        getSession().save(actorMember);
        return (SActorMember) getSession().get(actorMember.getClass(), new PersistentObjectId(actorMember.getId(), actorMember.getTenantId()));
    }

    public SActor add(SActorImpl actor) {
        getSession().save(actor);
        return (SActor) getSession().get(actor.getClass(), new PersistentObjectId(actor.getId(), actor.getTenantId()));
    }

    public SUserMembership add(SUserMembershipImpl membership) {
        getSession().save(membership);
        return (SUserMembership) getSession().get(membership.getClass(), new PersistentObjectId(membership.getId(), membership.getTenantId()));
    }

    public SProcessSupervisor add(final SProcessSupervisorImpl sProcessSupervisor) {
        getSession().save(sProcessSupervisor);
        return (SProcessSupervisor) getSession().get(sProcessSupervisor.getClass(),
                new PersistentObjectId(sProcessSupervisor.getId(), sProcessSupervisor.getTenantId()));
    }
}
