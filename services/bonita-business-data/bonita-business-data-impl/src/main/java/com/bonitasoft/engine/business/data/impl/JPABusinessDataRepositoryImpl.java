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

    private static final String BDR = "BDR";

    private final Map<String, Object> configuration;

    private EntityManagerFactory entityManagerFactory;

    private EntityManager entityManager;

    private BusinessDataModelRepository businessDataModelRepository;

    public JPABusinessDataRepositoryImpl(BusinessDataModelRepository businessDataModelRepository, final Map<String, Object> configuration) {
        this.businessDataModelRepository = businessDataModelRepository;
        this.configuration = new HashMap<String, Object>(configuration);
        this.configuration.put("hibernate.ejb.resource_scanner", InactiveScanner.class.getName());
    }

    @Override
    public void start() throws SBonitaException {
        if (businessDataModelRepository.isDBMDeployed()) {
            entityManagerFactory = Persistence.createEntityManagerFactory(BDR, configuration);
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

    private EntityManager getEntityManager() {
        if (entityManagerFactory == null) {
            throw new IllegalStateException("The BDR is not started");
        }

        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = entityManagerFactory.createEntityManager();
        } else {
            entityManager.joinTransaction();
        }
        return entityManager;
    }

    @Override
    public <T extends Entity> T findById(final Class<T> entityClass, final Long primaryKey) throws SBusinessDataNotFoundException {
        if (primaryKey == null) {
            throw new SBusinessDataNotFoundException("Impossible to get data with a null identifier");
        }
        final EntityManager em = getEntityManager();
        final T entity = em.find(entityClass, primaryKey);
        if (entity == null) {
            throw new SBusinessDataNotFoundException("Impossible to get data with id: " + primaryKey);
        }
        em.detach(entity);
        return entity;
    }

    @Override
    public <T> T find(final Class<T> resultClass, final String qlString, final Map<String, Object> parameters) throws SBusinessDataNotFoundException,
            NonUniqueResultException {
        final EntityManager em = getEntityManager();
        final TypedQuery<T> query = buildQuery(em, resultClass, qlString, parameters);
        try {
            final T entity = query.getSingleResult();
            detachEntity(em, resultClass, entity);
            return entity;
        } catch (final javax.persistence.NonUniqueResultException nure) {
            throw new NonUniqueResultException(nure);
        } catch (final NoResultException nre) {
            throw new SBusinessDataNotFoundException("Impossible to get data using query: " + qlString + " and parameters: " + parameters, nre);
        }
    }

    @Override
    public <T extends Serializable> T findByNamedQuery(String queryName, Class<T> resultClass, Map<String, Serializable> parameters)
            throws NonUniqueResultException {
        final EntityManager em = getEntityManager();
        TypedQuery<T> query = em.createNamedQuery(queryName, resultClass);
        if (parameters != null) {
            for (final Entry<String, Serializable> parameter : parameters.entrySet()) {
                query.setParameter(parameter.getKey(), parameter.getValue());
            }
        }
        try {
            final T result = query.getSingleResult();
            detachEntity(em, resultClass, result);
            return result;
        } catch (final javax.persistence.NonUniqueResultException nure) {
            throw new NonUniqueResultException(nure);
        } catch (final NoResultException nre) {
            return null;
        }
    }

    @Override
    public <T extends Serializable> List<T> findListByNamedQuery(final String queryName, final Class<T> resultClass, final Map<String, Serializable> parameters) {
        final EntityManager em = getEntityManager();
        TypedQuery<T> query = em.createNamedQuery(queryName, resultClass);
        if (parameters != null) {
            for (final Entry<String, Serializable> parameter : parameters.entrySet()) {
                query.setParameter(parameter.getKey(), parameter.getValue());
            }
        }
        final List<T> resultList = query.getResultList();
        for (final T entity : resultList) {
            detachEntity(em, resultClass, entity);
        }
        return resultList;
    }

    @Override
    public <T> List<T> findList(final Class<T> resultClass, final String qlString, final Map<String, Object> parameters) {
        final EntityManager em = getEntityManager();
        final TypedQuery<T> query = buildQuery(em, resultClass, qlString, parameters);
        final List<T> entities = query.getResultList();
        for (final T entity : entities) {
            detachEntity(em, resultClass, entity);
        }
        return entities;
    }

    private <T> TypedQuery<T> buildQuery(final EntityManager em, final Class<T> resultClass, final String qlString, final Map<String, Object> parameters) {
        final TypedQuery<T> query = em.createQuery(qlString, resultClass);
        if (parameters != null) {
            for (final Entry<String, Object> parameter : parameters.entrySet()) {
                query.setParameter(parameter.getKey(), parameter.getValue());
            }
        }
        return query;
    }

    private <T> void detachEntity(final EntityManager em, final Class<T> resultClass, final T entity) {
        if (!ClassUtils.isPrimitiveOrWrapper(resultClass)) {
            em.detach(entity);
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
