/*******************************************************************************
 * Copyright (C) 2013-2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.data.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.EntityType;

import org.apache.commons.lang3.ClassUtils;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.dependency.SDependencyNotFoundException;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.dependency.model.builder.SDependencyBuilderFactory;
import org.bonitasoft.engine.dependency.model.builder.SDependencyMappingBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.hibernate.cfg.Configuration;

import com.bonitasoft.engine.bdm.BDMCompiler;
import com.bonitasoft.engine.bdm.BDMJarBuilder;
import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.business.data.NonUniqueResultException;
import com.bonitasoft.engine.business.data.SBusinessDataNotFoundException;
import com.bonitasoft.engine.business.data.SBusinessDataRepositoryDeploymentException;
import com.bonitasoft.engine.business.data.SBusinessDataRepositoryException;

/**
 * @author Matthieu Chaffotte
 * @author Romain Bioteau
 */
public class JPABusinessDataRepositoryImpl implements BusinessDataRepository {

    private static final String BDR = "BDR";

    private static final String SCHEMA_CREATION_BDR = "notManagedBDR";

    private final Map<String, Object> configuration;

    private EntityManagerFactory entityManagerFactory;

    private EntityManagerFactory notManagedEntityManagerFactory;

    private final TechnicalLoggerService loggerService;

    private final DependencyService dependencyService;

    private final Map<String, Object> modelConfiguration;

    public JPABusinessDataRepositoryImpl(final DependencyService dependencyService, final TechnicalLoggerService loggerService,
            final Map<String, Object> configuration, final Map<String, Object> modelConfiguration) {
        this.dependencyService = dependencyService;
        this.loggerService = loggerService;
        this.configuration = new HashMap<String, Object>(configuration);
        this.configuration.put("hibernate.ejb.resource_scanner", InactiveScanner.class.getName());

        this.modelConfiguration = modelConfiguration;
        this.modelConfiguration.remove("hibernate.hbm2ddl.auto");
        final Object remove = this.modelConfiguration.remove("hibernate.hbm2ddl.auto");
        if (remove != null && loggerService.isLoggable(JPABusinessDataRepositoryImpl.class, TechnicalLogSeverity.INFO)) {
            this.loggerService.log(JPABusinessDataRepositoryImpl.class, TechnicalLogSeverity.INFO,
                    "'hibernate.hbm2ddl.auto' is not a valid property so it has been ignored");
        }

    }

    @Override
    public void start() throws SBonitaException {
        if (isDBMDeployed()) {
            entityManagerFactory = Persistence.createEntityManagerFactory(BDR, configuration);
            notManagedEntityManagerFactory = Persistence.createEntityManagerFactory(SCHEMA_CREATION_BDR, modelConfiguration);
            updateSchema();
        }
    }

    @Override
    public void stop() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
            entityManagerFactory = null;
        }
        if (notManagedEntityManagerFactory != null) {
            notManagedEntityManagerFactory.close();
            notManagedEntityManagerFactory = null;
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

    private void updateSchema() throws SBusinessDataRepositoryDeploymentException {
        final Configuration cfg = new Configuration();
        final Set<EntityType<?>> entities = entityManagerFactory.getMetamodel().getEntities();
        for (final EntityType<?> entity : entities) {
            cfg.addAnnotatedClass(entity.getJavaType());
        }

        final Properties properties = new Properties();
        properties.putAll(notManagedEntityManagerFactory.getProperties());
        cfg.setProperties(properties);

        final SchemaUpdater updater = new SchemaUpdater(cfg, loggerService);
        updater.execute();

        final List<Exception> exceptions = updater.getExceptions();
        if (!exceptions.isEmpty()) {
            throw new SBusinessDataRepositoryDeploymentException("Upating schema fails due to: " + exceptions);
        }
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

    protected boolean isDBMDeployed() throws SBusinessDataRepositoryException {
        final byte[] dependency = getDeployedBDMDependency();
        return dependency != null && dependency.length > 0;
    }

    
    
    @Override
    public byte[] getDeployedBDMDependency() throws SBusinessDataRepositoryException {
        final FilterOption filterOption = new FilterOption(SDependency.class, "name", BDR);
        final List<FilterOption> filters = new ArrayList<FilterOption>();
        filters.add(filterOption);
        final QueryOptions queryOptions = new QueryOptions(filters, null);
        List<SDependency> dependencies;
        try {
            dependencies = dependencyService.getDependencies(queryOptions);
        } catch (SDependencyException e) {
            throw new SBusinessDataRepositoryException(e);
        }
        if(dependencies.isEmpty()){
            return null;
        }
        return dependencies.get(0).getValue();
    }

    private EntityManager getEntityManager() {
        if (entityManagerFactory == null) {
            throw new IllegalStateException("The BDR is not started");
        }
        return entityManagerFactory.createEntityManager();
    }

    protected SDependencyMapping createDependencyMapping(final long tenantId, final SDependency sDependency) {
        return BuilderFactory.get(SDependencyMappingBuilderFactory.class).createNewInstance(sDependency.getId(), tenantId, ScopeType.TENANT).done();
    }

    protected SDependency createSDependency(final long tenantId, final byte[] transformedBdrArchive) {
        return BuilderFactory.get(SDependencyBuilderFactory.class).createNewInstance(BDR, tenantId, ScopeType.TENANT, BDR + ".jar", transformedBdrArchive)
                .done();
    }

    protected byte[] generateBDMJar(final byte[] bdmZip) throws SBusinessDataRepositoryDeploymentException {
        final BDMJarBuilder builder = new BDMJarBuilder(BDMCompiler.create());
        return builder.build(bdmZip);
    }

    @Override
    public void deploy(final byte[] bdmZip, final long tenantId) throws SBusinessDataRepositoryDeploymentException {
        final byte[] bdmJar = generateBDMJar(bdmZip);
        final SDependency sDependency = createSDependency(tenantId, bdmJar);
        try {
            dependencyService.createDependency(sDependency);
            final SDependencyMapping sDependencyMapping = createDependencyMapping(tenantId, sDependency);
            dependencyService.createDependencyMapping(sDependencyMapping);
        } catch (final SDependencyException e) {
            throw new SBusinessDataRepositoryDeploymentException(e);
        }
    }

    @Override
    public void undeploy(final long tenantId) throws SBusinessDataRepositoryException {
        try {
            dependencyService.deleteDependency(BDR);
        } catch (final SDependencyNotFoundException sde) {
            // do nothing
        } catch (final SDependencyException sde) {
            throw new SBusinessDataRepositoryException(sde);
        }
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
