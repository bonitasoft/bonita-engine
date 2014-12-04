/*******************************************************************************
 * Copyright (C) 2013-2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.data.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.EntityType;

import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.TransactionService;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.business.data.BusinessDataModelRepository;
import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.business.data.NonUniqueResultException;
import com.bonitasoft.engine.business.data.SBusinessDataNotFoundException;

/**
 * @author Matthieu Chaffotte
 * @author Romain Bioteau
 */
public class JPABusinessDataRepositoryImpl implements BusinessDataRepository {

    private static final String BDR_PERSISTENCE_UNIT = "BDR";

    private final Map<String, Object> configuration;

    private EntityManagerFactory entityManagerFactory;

    private final ThreadLocal<EntityManager> managers = new ThreadLocal<EntityManager>();

    private final BusinessDataModelRepository businessDataModelRepository;

    private final TransactionService transactionService;

    public JPABusinessDataRepositoryImpl(final TransactionService transactionService, final BusinessDataModelRepository businessDataModelRepository,
            final Map<String, Object> configuration) {
        this.transactionService = transactionService;
        this.businessDataModelRepository = businessDataModelRepository;
        this.configuration = new HashMap<String, Object>(configuration);
        this.configuration.put("hibernate.ejb.resource_scanner", InactiveScanner.class.getName());
    }

    @Override
    public void start() {
        if (businessDataModelRepository.isDBMDeployed()) {
            entityManagerFactory = Persistence.createEntityManagerFactory(BDR_PERSISTENCE_UNIT, configuration);
        }
    }

    @Override
    public void stop() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
            entityManagerFactory = null;
        }
    }

    @Override
    public void pause() {
        stop();
    }

    @Override
    public void resume() {
        start();
    }

    @Override
    public Set<String> getEntityClassNames() {
        if (entityManagerFactory == null) {
            return Collections.emptySet();
        }
        final EntityManager em = getEntityManager();
        final Set<EntityType<?>> entities = em.getMetamodel().getEntities();
        final Set<String> entityClassNames = new HashSet<String>();
        for (final EntityType<?> entity : entities) {
            entityClassNames.add(entity.getJavaType().getName());
        }
        return entityClassNames;
    }

    protected EntityManager getEntityManager() {
        if (entityManagerFactory == null) {
            throw new IllegalStateException("The BDR is not started");
        }

        EntityManager manager = managers.get();
        if (manager == null || !manager.isOpen()) {
            manager = entityManagerFactory.createEntityManager();
            try {
                transactionService.registerBonitaSynchronization(new RemoveEntityManagerSynchronization(managers));
            } catch (final STransactionNotFoundException stnfe) {
                throw new IllegalStateException(stnfe);
            }
            managers.set(manager);
        }
        manager.joinTransaction();
        return manager;
    }

    @Override
    public <T extends Entity> T findById(final Class<T> entityClass, final Long primaryKey) throws SBusinessDataNotFoundException {
        if (primaryKey == null) {
            throw new SBusinessDataNotFoundException("Impossible to get data of type " + entityClass.getName() + " with a null identifier");
        }
        final EntityManager em = getEntityManager();
        final T entity = em.find(entityClass, primaryKey);
        if (entity == null) {
            throw new SBusinessDataNotFoundException("Impossible to get data of type " + entityClass.getName() + " with id: " + primaryKey);
        }
        return entity;
    }

    @Override
    public <T extends Entity> List<T> findByIds(final Class<T> entityClass, final List<Long> primaryKeys) {
        final List<T> entities = new ArrayList<T>();
        final EntityManager em = getEntityManager();
        for (final Long primaryKey : primaryKeys) {
            final T entity = em.find(entityClass, primaryKey);
            if (entity != null) {
                entities.add(entity);
            }
        }
        return entities;
    }

    protected <T extends Serializable> T find(final Class<T> resultClass, final TypedQuery<T> query, final Map<String, Serializable> parameters)
            throws NonUniqueResultException {
        if (query == null) {
            throw new IllegalArgumentException("query is null");
        }
        if (parameters != null) {
            for (final Entry<String, Serializable> parameter : parameters.entrySet()) {
                query.setParameter(parameter.getKey(), parameter.getValue());
            }
        }
        try {
            return query.getSingleResult();
        } catch (final javax.persistence.NonUniqueResultException nure) {
            throw new NonUniqueResultException(nure);
        } catch (final NoResultException e) {
            return null;
        }
    }

    @Override
    public <T extends Serializable> T find(final Class<T> resultClass, final String jpqlQuery, final Map<String, Serializable> parameters)
            throws NonUniqueResultException {
        final TypedQuery<T> typedQuery = createTypedQuery(jpqlQuery, resultClass);
        return find(resultClass, typedQuery, parameters);
    }

    @Override
    public <T extends Serializable> List<T> findList(final Class<T> resultClass, final String jpqlQuery, final Map<String, Serializable> parameters,
            final int startIndex, final int maxResults) {
        final TypedQuery<T> typedQuery = createTypedQuery(jpqlQuery, resultClass);
        return findList(typedQuery, parameters, startIndex, maxResults);
    }

    @Override
    public <T extends Serializable> T findByNamedQuery(final String queryName, final Class<T> resultClass, final Map<String, Serializable> parameters)
            throws NonUniqueResultException {
        final EntityManager em = getEntityManager();
        final TypedQuery<T> query = em.createNamedQuery(queryName, resultClass);
        return find(resultClass, query, parameters);
    }

    @Override
    public <T extends Serializable> List<T> findListByNamedQuery(final String queryName, final Class<T> resultClass,
            final Map<String, Serializable> parameters, final int startIndex, final int maxResults) {
        final EntityManager em = getEntityManager();
        final TypedQuery<T> query = em.createNamedQuery(queryName, resultClass);
        return findList(query, parameters, startIndex, maxResults);
    }

    private <T> TypedQuery<T> createTypedQuery(final String jpqlQuery, final Class<T> resultClass) {
        return getEntityManager().createQuery(jpqlQuery, resultClass);
    }

    protected <T extends Serializable> List<T> findList(final TypedQuery<T> query, final Map<String, Serializable> parameters, final int startIndex,
            final int maxResults) {
        if (query == null) {
            throw new IllegalArgumentException("query is null");
        }
        if (parameters != null) {
            for (final Entry<String, Serializable> parameter : parameters.entrySet()) {
                query.setParameter(parameter.getKey(), parameter.getValue());
            }
        }
        query.setFirstResult(startIndex);
        query.setMaxResults(maxResults);
        return query.getResultList();
    }

    @Override
    public void remove(final Entity entity) {
        if (entity != null && entity.getPersistenceId() != null) {
            final EntityManager em = getEntityManager();
            em.remove(entity);
        }
    }

    @Override
    public void persist(final Entity entity) {
        if (entity != null) {
            getEntityManager().persist(entity);
        }
    }

    @Override
    public Entity merge(final Entity entity) {
        if (entity != null) {
            return getEntityManager().merge(entity);
        }
        return null;
    }

    @Override
    public Entity unwrap(final Entity wrapped) {
        Entity entity = wrapped;
        if (entity != null && entity instanceof HibernateProxy) {
            Hibernate.initialize(entity);
            entity = (Entity) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
        }
        return entity;
    }

}
