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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.bdm.model.QueryParameterTypes;
import org.bonitasoft.engine.bdm.model.field.Field;
import org.bonitasoft.engine.business.data.BusinessDataModelRepository;
import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.business.data.DataRetentionBdmTrackingService;
import org.bonitasoft.engine.business.data.NonUniqueResultException;
import org.bonitasoft.engine.business.data.SBusinessDataNotFoundException;
import org.bonitasoft.engine.business.data.SDataRetentionBdmTrackingException;
import org.bonitasoft.engine.classloader.ClassLoaderIdentifier;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.SingleClassLoaderListener;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.commons.exceptions.SRetryableException;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.hibernate.Hibernate;
import org.hibernate.QueryException;
import org.hibernate.boot.archive.scan.internal.DisabledScanner;
import org.hibernate.proxy.HibernateProxy;

/**
 * Some of these methods are enriched with aspects to throw events. See BusinessDataRepositoryEventAspect for details.
 *
 * @author Matthieu Chaffotte
 * @author Romain Bioteau
 */
@Slf4j
public class JPABusinessDataRepositoryImpl
        implements BusinessDataRepository, EntityManagerFactoryAware, SingleClassLoaderListener {

    private static final String BDR_PERSISTENCE_UNIT = "BDR";

    private final Map<String, Object> configuration;

    private EntityManagerFactory entityManagerFactory;

    private final ThreadLocal<EntityManager> managers = new ThreadLocal<>();

    private final BusinessDataModelRepository businessDataModelRepository;

    private final UserTransactionService transactionService;

    private final DataRetentionBdmTrackingService dataRetentionBdmTrackingService;

    public JPABusinessDataRepositoryImpl(
            final UserTransactionService transactionService,
            final BusinessDataModelRepository businessDataModelRepository,
            final Map<String, Object> configuration,
            ClassLoaderService classLoaderService,
            DataRetentionBdmTrackingService dataRetentionBdmTrackingService) {
        this.transactionService = transactionService;
        this.businessDataModelRepository = businessDataModelRepository;
        this.configuration = new HashMap<>(configuration);
        this.configuration.put("hibernate.archive.scanner", DisabledScanner.class.getName());
        classLoaderService.addListener(ClassLoaderIdentifier.TENANT, this);
        this.dataRetentionBdmTrackingService = dataRetentionBdmTrackingService;
    }

    @Override
    public void start() {
        if (entityManagerFactory == null && businessDataModelRepository.isBDMDeployed()) {
            log.debug("Creating Entity Manager Factory");
            recreateEntityManagerFactoryEvenIfExisting();
        }
    }

    EntityManagerFactory createEntityManagerFactory() {
        return Persistence.createEntityManagerFactory(BDR_PERSISTENCE_UNIT, configuration);
    }

    @Override
    public void stop() {
        if (getEntityManagerFactory() != null) {
            log.debug("Closing Entity Manager Factory because service is stopping");
            getEntityManagerFactory().close();
            entityManagerFactory = null;
            log.debug("Entity Manager Factory closed");
        }
    }

    private synchronized void recreateEntityManagerFactoryOnClassLoaderChange(ClassLoader newClassLoader) {
        if (businessDataModelRepository.isBDMDeployed()) {
            log.debug("Recreating Entity Manager Factory for classloader {}", newClassLoader);
            final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(newClassLoader);
                recreateEntityManagerFactoryEvenIfExisting();
            } finally {
                Thread.currentThread().setContextClassLoader(currentClassLoader);
            }
            log.debug("Entity Manager Factory recreated");
        } else {
            log.debug("No BDM deployed. No Entity Manager Factory to recreate.");
        }
    }

    private void recreateEntityManagerFactoryEvenIfExisting() {
        if (entityManagerFactory != null) {
            log.warn("Entity Manager Factory should be null. Closing it and recreating a new one.");
            entityManagerFactory.close();
        }
        entityManagerFactory = createEntityManagerFactory();
        log.debug("Recreated Entity Manager Factory: {}", entityManagerFactory);
    }

    public EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null) {
            /*
             * in case the entity manager factory is reloading inside #recreateEntityManagerFactory
             * we get it inside a method synchronized with #recreateEntityManagerFactory
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
        if (manager != null && !isPartOfCurrentTransaction(manager)) {
            log.warn("Stale BDM EntityManager detected: not part of the current transaction. "
                    + "It will be replaced with a fresh one. "
                    + "This typically happens after a JTA transaction timeout.");
            closeQuietly(manager);
            managers.remove();
            manager = null;
        }
        if (manager == null) {
            manager = getEntityManagerFactory().createEntityManager();
            try {
                transactionService.registerBonitaSynchronization(new RemoveEntityManagerSynchronization(managers));
            } catch (final STransactionNotFoundException stnfe) {
                closeQuietly(manager);
                throw new IllegalStateException(stnfe);
            }
            managers.set(manager);
            log.debug("Created new BDM EntityManager for current transaction");
        } else {
            log.debug("Reusing existing BDM EntityManager in current transaction.");
        }
        try {
            manager.joinTransaction();
        } catch (Exception e) {
            log.warn(
                    "BDM EntityManager failed to join current transaction. This may indicate the transaction is already marked for rollback.",
                    e);
            closeQuietly(manager);
            managers.remove();
            throw e;
        }
        return manager;
    }

    private boolean isPartOfCurrentTransaction(EntityManager manager) {
        try {
            return manager.isOpen() && manager.isJoinedToTransaction();
        } catch (Exception e) {
            log.warn("Stale BDM EntityManager detected (exception during transaction state check). "
                    + "A fresh EntityManager will be created for the current transaction.", e);
            return false;
        }
    }

    private void closeQuietly(EntityManager manager) {
        try {
            if (manager.isOpen()) {
                manager.close();
            }
        } catch (Exception e) {
            log.warn("Failed to close stale BDM EntityManager. "
                    + "This may indicate a resource leak if it happens repeatedly.", e);
        }
    }

    @Override
    public <T extends Entity> T findById(final Class<T> entityClass, final Long primaryKey)
            throws SBusinessDataNotFoundException {
        if (primaryKey == null) {
            throw new SBusinessDataNotFoundException(
                    "Impossible to get data of type " + entityClass.getName() + " with a null identifier");
        }
        final EntityManager em = getEntityManager();
        final T entity;
        try {
            entity = em.find(entityClass, primaryKey);
        } catch (final PersistenceException e) {
            log.debug("BDM findById({}, id={}) failed", entityClass.getSimpleName(), primaryKey, e);
            //wrap in retryable exception because the issue might come from BDR reloading
            throw new SRetryableException(e);
        }
        if (entity == null) {
            throw new SBusinessDataNotFoundException(
                    "Impossible to get data of type " + entityClass.getName() + " with id: " + primaryKey);
        }
        return entity;
    }

    @Override
    public <T extends Entity> List<T> findByIds(final Class<T> entityClass, final List<Long> primaryKeys) {
        if (primaryKeys == null || primaryKeys.isEmpty()) {
            return new ArrayList<>();
        }
        final EntityManager em = getEntityManager();
        try {
            final CriteriaBuilder cb = em.getCriteriaBuilder();
            final CriteriaQuery<T> criteriaQuery = cb.createQuery(entityClass);
            final Root<T> row = criteriaQuery.from(entityClass);
            criteriaQuery.select(row).where(row.get(Field.PERSISTENCE_ID).in(primaryKeys));
            return em.createQuery(criteriaQuery).getResultList();
        } catch (final PersistenceException e) {
            //wrap in retryable exception because the issue might come from BDR reloading
            throw new SRetryableException(e);
        }
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

    protected <T extends Serializable> T find(final Class<T> resultClass, final TypedQuery<T> query,
            final Map<String, Serializable> parameters)
            throws NonUniqueResultException {
        if (query == null) {
            throw new IllegalArgumentException("query is null");
        }
        if (parameters != null) {
            for (final Entry<String, Serializable> parameter : parameters.entrySet()) {
                query.setParameter(parameter.getKey(), checkParameterValue(parameter.getValue()));
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

    Object checkParameterValue(final Serializable parameterValue) {
        if (parameterValue != null && !QueryParameterTypes.contains(parameterValue.getClass())) {
            throw new IllegalArgumentException(String.format(
                    "'%s' is not a supported type for a query parameter.",
                    parameterValue.getClass().getName()));
        }
        if (parameterValue instanceof Object[]) {
            return Arrays.asList((Object[]) parameterValue);
        }
        return parameterValue;
    }

    @Override
    public <T extends Serializable> T find(final Class<T> resultClass, final String jpqlQuery,
            final Map<String, Serializable> parameters)
            throws NonUniqueResultException {
        final TypedQuery<T> typedQuery = createTypedQuery(jpqlQuery, resultClass);
        try {
            return find(resultClass, typedQuery, parameters);
        } catch (final PersistenceException e) {
            throw new SRetryableException(e);
        }
    }

    @Override
    public <T extends Serializable> List<T> findList(final Class<T> resultClass, final String jpqlQuery,
            final Map<String, Serializable> parameters,
            final int startIndex, final int maxResults) {
        final TypedQuery<T> typedQuery = createTypedQuery(jpqlQuery, resultClass);
        try {
            return findList(typedQuery, parameters, startIndex, maxResults);
        } catch (final QueryException e) {
            throw new IllegalArgumentException(e);
        } catch (final PersistenceException e) {
            throw new SRetryableException(e);
        }
    }

    @Override
    public <T extends Serializable> T findByNamedQuery(final String queryName, final Class<T> resultClass,
            final Map<String, Serializable> parameters)
            throws NonUniqueResultException {
        final EntityManager em = getEntityManager();
        try {
            final TypedQuery<T> query = em.createNamedQuery(queryName, resultClass);
            return find(resultClass, query, parameters);
        } catch (final PersistenceException e) {
            log.debug("BDM findByNamedQuery('{}', {}) failed", queryName, resultClass.getSimpleName(), e);
            //wrap in retryable exception because the issue might come from BDR reloading
            throw new SRetryableException(e);
        }
    }

    @Override
    public <T extends Serializable> List<T> findListByNamedQuery(final String queryName, final Class<T> resultClass,
            final Map<String, Serializable> parameters, final int startIndex, final int maxResults) {
        final EntityManager em = getEntityManager();
        try {
            final TypedQuery<T> query = em.createNamedQuery(queryName, resultClass);
            return findList(query, parameters, startIndex, maxResults);
        } catch (final PersistenceException e) {
            log.debug("BDM findListByNamedQuery('{}', {}) failed", queryName, resultClass.getSimpleName(), e);
            //wrap in retryable exception because the issue might come from BDR reloading
            throw new SRetryableException(e);
        }
    }

    private <T> TypedQuery<T> createTypedQuery(final String jpqlQuery, final Class<T> resultClass) {
        return getEntityManager().createQuery(jpqlQuery, resultClass);
    }

    protected <T extends Serializable> List<T> findList(final TypedQuery<T> query,
            final Map<String, Serializable> parameters, final int startIndex,
            final int maxResults) {
        if (query == null) {
            throw new IllegalArgumentException("query is null");
        }
        if (maxResults > 0) {
            if (parameters != null) {
                for (final Entry<String, Serializable> parameter : parameters.entrySet()) {
                    query.setParameter(parameter.getKey(), checkParameterValue(parameter.getValue()));
                }
            }
            query.setFirstResult(startIndex);
            query.setMaxResults(maxResults);
            return query.getResultList();
        }
        return Collections.emptyList();
    }

    /**
     * Loads the root entity with an empty {@code EntityGraph} ({@code fetchgraph} hint) to
     * override {@code FetchType.EAGER} associations, then calls {@code em.remove()}.
     * <p>
     * The effect on memory and query shape depends on the relationship type:
     * <ul>
     * <li><b>AGGREGATION</b> (no cascade REMOVE — e.g. Invoice → Customer): the referenced
     * entity is <b>never loaded</b>, since cascade delete does not need it. This is the main
     * win — without the fetchgraph, EAGER would force loading entities just to discard them.</li>
     * <li><b>COMPOSITION</b> (cascade REMOVE — e.g. Invoice → InvoiceLine): children are still
     * loaded into memory because cascade delete requires the collection contents. The benefit
     * is that they are loaded via a separate SELECT per collection instead of a single wide
     * JOIN, avoiding cartesian-product row explosion when multiple or deep compositions exist.</li>
     * </ul>
     * <p>
     * The returned entity is used by {@link BusinessDataRepositoryEventAspect} to fire the
     * {@code BUSINESS_DATA_DELETED} event.
     */
    @Override
    public Entity removeById(Class<? extends Entity> entityClass, long persistenceId)
            throws SBusinessDataNotFoundException {
        log.trace("Removing entity of type {} with id {} using EntityGraph", entityClass.getName(), persistenceId);
        final EntityManager em = getEntityManager();
        try {
            // Empty fetchgraph overrides EAGER to LAZY for all associations. Aggregations are
            // then skipped entirely; compositions are still fetched at cascade time, but via
            // per-collection SELECTs rather than a single wide JOIN.
            EntityGraph<?> minimalGraph = em.createEntityGraph(entityClass);
            Entity entity = em.find(entityClass, persistenceId,
                    Map.of("javax.persistence.fetchgraph", minimalGraph));
            if (entity == null) {
                throw new SBusinessDataNotFoundException(
                        "Impossible to get data of type " + entityClass.getName() + " with id: " + persistenceId);
            }
            em.remove(entity);
            // Delete the tracking record linked to the removed BDM entity in the Bonita DB
            deleteTrackingRecord(persistenceId, entityClass.getName());
            return entity;
        } catch (final PersistenceException e) {
            throw new SRetryableException(
                    "Failed to remove entity " + entityClass.getName() + " with id " + persistenceId, e);
        }
    }

    @Override
    public void remove(final Entity entity) {
        if (entity != null && entity.getPersistenceId() != null) {
            log.trace("Removing entity of type {} with id {}", entity.getClass().getName(), entity.getPersistenceId());
            final EntityManager em = getEntityManager();
            try {
                em.remove(entity);
                // Delete the tracking record linked to the removed BDM entity in the Bonita DB
                deleteTrackingRecord(entity.getPersistenceId(), entity.getClass().getName());
            } catch (final PersistenceException e) {
                throw new SRetryableException(e);
            }
        } else {
            log.trace("Entity is null or has null id, nothing to remove");
        }
    }

    /**
     * Deletes the data retention tracking record associated with a removed BDM entity.
     * <p>
     * Unlike {@link #trackCreation} and {@link #trackUpdate}, failures are logged
     * but do not roll back the transaction — the BDM entity removal takes priority.
     * Any orphan tracking record will be cleaned up later by the data retention job.
     */
    private void deleteTrackingRecord(long entityId, String entityClassname) {
        log.debug("Deleting data retention tracking record for removed BDM entity {}#{}", entityClassname, entityId);
        try {
            dataRetentionBdmTrackingService.delete(entityId, entityClassname);
        } catch (Exception e) {
            // Ignore exceptions because the BDM entity is already removed, and we don't want to roll back that
            // removal if tracking deletion fails. The orphan tracking record will be cleaned up later by the data
            // retention cleanup job.
            log.warn("Failed to delete data retention tracking record for {}#{}", entityClassname, entityId, e);
        }
    }

    @Override
    public void persist(final Entity entity) {
        if (entity == null) {
            log.trace("Entity is null, nothing to persist");
            return;
        }
        log.trace("Persisting entity of type {} with id {}", entity.getClass().getName(), entity.getPersistenceId());
        try {
            // Capture whether the entity is new before JPA assigns an ID
            var isNew = entity.getPersistenceId() == null;

            getEntityManager().persist(entity);

            // Insert or update a data retention tracking record in the Bonita DB
            if (isNew) {
                trackCreation(entity.getPersistenceId(), entity.getClass().getName());
            } else {
                trackUpdate(entity.getPersistenceId(), entity.getClass().getName());
            }
        } catch (final PersistenceException e) {
            throw new SRetryableException(e);
        }
    }

    @Override
    public Entity merge(final Entity entity) {
        if (entity == null) {
            log.trace("Entity is null, nothing to merge");
            return null;
        }
        log.trace("Merging entity of type {} with id {}", entity.getClass().getName(), entity.getPersistenceId());
        try {
            // Capture whether the entity is new before JPA assigns an ID
            var isNew = entity.getPersistenceId() == null;
            // Capture the real class name before merge, because merge() may return a Hibernate proxy
            // whose getClass().getName() would be e.g. "Invoice$HibernateProxyXxx" instead of "Invoice"
            var entityClassname = entity.getClass().getName();

            Entity merged = getEntityManager().merge(entity);

            // Insert or update a data retention tracking record in the Bonita DB,
            // use the merged entity which has the JPA-assigned persistenceId
            if (isNew) {
                trackCreation(merged.getPersistenceId(), entityClassname);
            } else {
                trackUpdate(merged.getPersistenceId(), entityClassname);
            }
            return merged;
        } catch (final PersistenceException e) {
            throw new SRetryableException(e);
        }
    }

    /**
     * Inserts a tracking record in the Bonita DB for a newly created BDM entity.
     * Called within the same JTA transaction as the BDM persist/merge, so both
     * are rolled back together if either fails.
     */
    private void trackCreation(long entityId, String entityClassname) {
        log.debug("Tracking creation of new BDM entity {}#{}", entityClassname, entityId);
        try {
            dataRetentionBdmTrackingService.create(entityId, entityClassname);
        } catch (SDataRetentionBdmTrackingException e) {
            // Intentionally throwing an unchecked exception to roll back the entire JTA transaction,
            // including the BDM entity creation. Tracking and BDM data must stay consistent.
            throw new SBonitaRuntimeException("Failed to insert data retention tracking record for "
                    + entityClassname + "#" + entityId, e);
        }
    }

    /**
     * Updates the {@code lastModifiedAt} timestamp of an existing tracking record
     * in the Bonita DB. If no record is found (e.g. the entity was created before
     * the tracking feature was deployed), a new one is created instead.
     */
    private void trackUpdate(long entityId, String entityClassname) {
        log.debug("Tracking update of existing BDM entity {}#{}", entityClassname, entityId);
        try {
            dataRetentionBdmTrackingService.upsert(entityId, entityClassname);
        } catch (SDataRetentionBdmTrackingException e) {
            // Intentionally throwing an unchecked exception to roll back the entire JTA transaction,
            // including the BDM entity update. Tracking and BDM data must stay consistent.
            throw new SBonitaRuntimeException("Failed to update data retention tracking record for "
                    + entityClassname + "#" + entityId, e);
        }
    }

    @Override
    public Entity unwrap(final Entity wrapped) {
        Entity entity = wrapped;
        if (entity instanceof HibernateProxy) {
            Hibernate.initialize(entity);
            entity = (Entity) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
        }
        return entity;
    }

    @Override
    public void onUpdate(ClassLoader newClassLoader) {
        clearProxyFactoryCache();
        recreateEntityManagerFactoryOnClassLoaderChange(newClassLoader);
    }

    private void clearProxyFactoryCache() {
        log.debug("Clearing BDM proxy cache");
        try {
            new ProxyCacheManager().clearCache();
            log.debug("BDM proxy cache cleared");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new SRetryableException(e);
        }
    }
}
