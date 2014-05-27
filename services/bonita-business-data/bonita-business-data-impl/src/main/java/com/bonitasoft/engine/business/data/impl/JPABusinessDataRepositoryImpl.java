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

import org.apache.commons.lang3.ClassUtils;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.TransactionService;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.bdm.model.field.Field;
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
    public void start() throws SBonitaException {
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
    public void pause() throws SBonitaException {
        stop();
    }

    @Override
    public void resume() throws SBonitaException {
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

        final TypedQuery<T> query = em.createNamedQuery(entityClass.getSimpleName() + ".findByPersistenceId", entityClass);
        if (query.getParameters().size() != 1 && query.getParameter(Field.PERSISTENCE_ID) == null) {
            throw new IllegalArgumentException("findByPersistenceId named query should have only one parameter named " + Field.PERSISTENCE_ID);
        }
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(Field.PERSISTENCE_ID, primaryKey);
        T entity = null;
        try {
            entity = find(entityClass, query, parameters);
        } catch (NonUniqueResultException e) {
            throw new IllegalStateException(e);
        }
        if (entity == null) {
            throw new SBusinessDataNotFoundException("Impossible to get data of type " + entityClass.getName() + " with id: " + primaryKey);
        }
        em.detach(entity);
        return entity;
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
        final EntityManager em = getEntityManager();
        try {
            final T entity = query.getSingleResult();
            return detachEntity(em, resultClass, entity);
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
        return findList(resultClass, typedQuery, parameters, startIndex, maxResults);
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
        return findList(resultClass, query, parameters, startIndex, maxResults);
    }

    private <T extends Serializable> TypedQuery<T> createTypedQuery(final String jpqlQuery, final Class<T> resultClass) {
        return getEntityManager().createQuery(jpqlQuery, resultClass);
    }

    protected <T extends Serializable> List<T> findList(final Class<T> resultClass, final TypedQuery<T> query, final Map<String, Serializable> parameters,
            final int startIndex, final int maxResults) {
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
        final EntityManager em = getEntityManager();
        final List<T> resultList = query.getResultList();
        final List<T> copyList = new ArrayList<T>();
        for (final T entity : resultList) {
            copyList.add(detachEntity(em, resultClass, entity));
        }
        return copyList;
    }

    private <T> T detachEntity(final EntityManager em, final Class<T> resultClass, final T entity) {
        if (ClassUtils.isPrimitiveOrWrapper(resultClass)) {
            return entity;
        } else {
            em.detach(entity);
            return entity;
        }
    }

    @Override
    public <T extends Entity> T merge(final T entity) {
        if (entity != null) {
            final EntityManager em = getEntityManager();
            return em.merge(entity);
        }
        return null;
    }

    @Override
    public void remove(final Entity entity) {
        if (entity != null && entity.getPersistenceId() != null) {
            final EntityManager em = getEntityManager();
            final Entity attachedEntity = em.find(entity.getClass(), entity.getPersistenceId());
            if (attachedEntity != null) {
                em.remove(attachedEntity);
            }
        }
    }

}
