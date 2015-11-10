/**
 * Copyright (C) 2015 Bonitasoft S.A.
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
package org.bonitasoft.engine.business.data.impl;

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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.bdm.model.field.Field;
import org.bonitasoft.engine.business.data.BusinessDataModelRepository;
import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.business.data.NonUniqueResultException;
import org.bonitasoft.engine.business.data.SBusinessDataNotFoundException;
import org.bonitasoft.engine.classloader.ClassLoaderListener;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.TransactionService;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

/**
 * @author Matthieu Chaffotte
 * @author Romain Bioteau
 */
public class JPABusinessDataRepositoryImpl implements BusinessDataRepository, ClassLoaderListener {

    private static final String BDR_PERSISTENCE_UNIT = "BDR";

    private final Map<String, Object> configuration;
    private EntityManagerFactory entityManagerFactory;
    private final ThreadLocal<EntityManager> managers = new ThreadLocal<>();
    private final BusinessDataModelRepository businessDataModelRepository;
    private final TechnicalLoggerService loggerService;
    private ClassLoaderService classLoaderService;
    private final long tenantId;

    private final TransactionService transactionService;

    public JPABusinessDataRepositoryImpl(final TransactionService transactionService, final BusinessDataModelRepository businessDataModelRepository, TechnicalLoggerService loggerService,
                                         final Map<String, Object> configuration, ClassLoaderService classLoaderService, long tenantId) {
        this.transactionService = transactionService;
        this.businessDataModelRepository = businessDataModelRepository;
        this.loggerService = loggerService;
        this.classLoaderService = classLoaderService;
        this.tenantId = tenantId;
        this.configuration = new HashMap<>(configuration);
        this.configuration.put("hibernate.ejb.resource_scanner", InactiveScanner.class.getName());
    }

    @Override
    public void start() {
        if (businessDataModelRepository.isDBMDeployed()) {
            loggerService.log(getClass(), TechnicalLogSeverity.DEBUG, "Create entity factory on tenant " + tenantId);
            entityManagerFactory = createEntityManagerFactory();
        }
        classLoaderService.addListener(ScopeType.TENANT.name(), tenantId, this);
    }

    EntityManagerFactory createEntityManagerFactory() {
        return Persistence.createEntityManagerFactory(BDR_PERSISTENCE_UNIT, configuration);
    }

    @Override
    public void stop() {
        if (getEntityManagerFactory() != null) {
            loggerService.log(getClass(), TechnicalLogSeverity.DEBUG, "Close entity factory because service is stopping on tenant " + tenantId);
            getEntityManagerFactory().close();
            entityManagerFactory = null;
        }
        classLoaderService.removeListener(ScopeType.TENANT.name(), tenantId, this);
    }

    private synchronized void recreateEntityManagerFactory(ClassLoader newClassLoader) {
        if (businessDataModelRepository.isDBMDeployed()) {
            loggerService.log(getClass(), TechnicalLogSeverity.DEBUG, "Recreate entity factory for classloader " + newClassLoader + " on tenant " + tenantId);
            ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(newClassLoader);
                entityManagerFactory.close();
                entityManagerFactory = createEntityManagerFactory();
            } finally {
                Thread.currentThread().setContextClassLoader(currentClassLoader);
            }
        }
    }

    public EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory != null && !entityManagerFactory.isOpen()) {
            /* If the entity manager is closed it means that the entity manager is currently reloading
               In this case we get it inside a method synchronized with #recreateEntityManagerFactory
             */
            return synchronizedGetEntityManagerFactory();
        }
        return entityManagerFactory;
    }

    private synchronized EntityManagerFactory synchronizedGetEntityManagerFactory() {
        return entityManagerFactory;
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
        if (getEntityManagerFactory() == null) {
            return Collections.emptySet();
        }
        final EntityManager em = getEntityManager();
        final Set<EntityType<?>> entities = em.getMetamodel().getEntities();
        final Set<String> entityClassNames = new HashSet<>();
        for (final EntityType<?> entity : entities) {
            entityClassNames.add(entity.getJavaType().getName());
        }
        return entityClassNames;
    }

    protected EntityManager getEntityManager() {
        if (getEntityManagerFactory() == null) {
            throw new IllegalStateException("The BDR is not started");
        }

        EntityManager manager = managers.get();
        if (manager == null || !manager.isOpen()) {
            manager = getEntityManagerFactory().createEntityManager();
            try {
                transactionService.registerBonitaSynchronization(new RemoveEntityManagerSynchronization(managers));
            } catch (final STransactionNotFoundException e) {
                throw new IllegalStateException(e);
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
        if (primaryKeys == null || primaryKeys.isEmpty()) {
            return new ArrayList<>();
        }
        final EntityManager em = getEntityManager();
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<T> criteriaQuery = cb.createQuery(entityClass);
        final Root<T> row = criteriaQuery.from(entityClass);
        criteriaQuery.select(row).where(row.get(Field.PERSISTENCE_ID).in(primaryKeys));
        return em.createQuery(criteriaQuery).getResultList();
    }

    @Override
    public <T extends Entity> List<T> findByIdentifiers(final Class<T> entityClass, final List<Long> primaryKeys) {
        if (primaryKeys == null || primaryKeys.isEmpty()) {
            return new ArrayList<>();
        }
        final List<T> entities = new ArrayList<>();
        for (final Long primaryKey : primaryKeys) {
            try {
                entities.add(findById(entityClass, primaryKey));
            } catch (final SBusinessDataNotFoundException e) {
                // If the business data does not exist, do not add it in the result list in order to have the same behaviour as findByIds
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

    @Override
    public void onUpdate(ClassLoader newClassLoader) {
        recreateEntityManagerFactory(newClassLoader);
    }

    @Override
    public void onDestroy(ClassLoader oldClassLoader) {

    }
}
