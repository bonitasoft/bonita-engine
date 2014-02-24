/*******************************************************************************
 * Copyright (C) 2013-2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.data.impl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ClassUtils;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.bonitasoft.engine.dependency.model.builder.SDependencyBuilderFactory;
import org.bonitasoft.engine.dependency.model.builder.SDependencyMappingBuilderFactory;

import com.bonitasoft.engine.bdm.BDMCodeGenerator;
import com.bonitasoft.engine.bdm.BusinessObject;
import com.bonitasoft.engine.bdm.BusinessObjectModel;
import com.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import com.bonitasoft.engine.business.data.BusinessDataNotFoundException;
import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.business.data.NonUniqueResultException;
import com.bonitasoft.engine.business.data.SBusinessDataRepositoryDeploymentException;
import com.sun.codemodel.JClassAlreadyExistsException;

/**
 * @author Matthieu Chaffotte
 * @author Romain Bioteau
 */
public class JPABusinessDataRepositoryImpl implements BusinessDataRepository {

    private final Map<String, Object> configuration;

    private final DependencyService dependencyService;

    private EntityManagerFactory entityManagerFactory;

    public JPABusinessDataRepositoryImpl(final DependencyService dependencyService, final Map<String, Object> configuration) {
        this.dependencyService = dependencyService;
        this.configuration = configuration;
    }

    @Override
    public void deploy(final byte[] bdmArchive, final long tenantId) throws SBusinessDataRepositoryDeploymentException {
        final byte[] bdmJar = buildBDMJar(bdmArchive);
        final SDependency sDependency = createSDependency(bdmJar);
        try {
            dependencyService.createDependency(sDependency);
            final SDependencyMapping sDependencyMapping = createDependencyMapping(tenantId, sDependency);
            dependencyService.createDependencyMapping(sDependencyMapping);
        } catch (final SDependencyException e) {
            throw new SBusinessDataRepositoryDeploymentException(e);
        }
    }

    protected SDependencyMapping createDependencyMapping(final long tenantId, final SDependency sDependency) {
        return BuilderFactory.get(SDependencyMappingBuilderFactory.class).createNewInstance(sDependency.getId(), tenantId, "tenant").done();
    }

    protected SDependency createSDependency(final byte[] transformedBdrArchive) {
        return BuilderFactory.get(SDependencyBuilderFactory.class).createNewInstance("BDR", "1.0", "BDR.jar", transformedBdrArchive).done();
    }

    protected byte[] getPersistenceFileContentFor(final List<String> classNames) throws SBusinessDataRepositoryDeploymentException, IOException,
            TransformerException {
        final PersistenceUnitBuilder builder = new PersistenceUnitBuilder();
        for (final String classname : classNames) {
            builder.addClass(classname);
        }
        return IOUtil.toByteArray(builder.done());
    }

    @Override
    public void start() throws SBusinessDataRepositoryDeploymentException {
        entityManagerFactory = Persistence.createEntityManagerFactory("BDR", configuration);
        try {
            executeQueries(new SchemaGenerator(entityManagerFactory.createEntityManager()).generate());
        } catch (final SQLException e) {
            throw new SBusinessDataRepositoryDeploymentException(e);
        }
    }

    private void executeQueries(final String... sqlQuerys) {
        final EntityManager entityManager = getEntityManager();
        for (final String sqlQuery : sqlQuerys) {
            final Query query = entityManager.createNativeQuery(sqlQuery);
            query.executeUpdate();
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
    public <T> T find(final Class<T> entityClass, final Serializable primaryKey) throws BusinessDataNotFoundException {
        final EntityManager em = getEntityManager();
        final T entity = em.find(entityClass, primaryKey);
        if (entity == null) {
            throw new BusinessDataNotFoundException("Impossible to get data with id: " + primaryKey);
        }
        em.detach(entity);
        return entity;
    }

    @Override
    public <T> T find(final Class<T> resultClass, final String qlString, final Map<String, Object> parameters) throws BusinessDataNotFoundException,
            NonUniqueResultException {
        final EntityManager em = getEntityManager();
        final TypedQuery<T> query = em.createQuery(qlString, resultClass);
        if (parameters != null) {
            for (final Entry<String, Object> parameter : parameters.entrySet()) {
                query.setParameter(parameter.getKey(), parameter.getValue());
            }
        }
        try {
            final T entity = query.getSingleResult();
            final Class<? extends Object> entityClass = entity.getClass();
            if (!entityClass.isPrimitive() && !ClassUtils.isPrimitiveOrWrapper(entityClass)) {
                em.detach(entity);
            }
            return entity;
        } catch (final javax.persistence.NonUniqueResultException nure) {
            throw new NonUniqueResultException(nure);
        } catch (final NoResultException nre) {
            throw new BusinessDataNotFoundException("Impossible to get data using query: " + qlString + " and parameters: " + parameters, nre);
        }
    }

    private EntityManager getEntityManager() {
        if (entityManagerFactory == null) {
            throw new IllegalStateException("The BDR is not started");
        }
        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.joinTransaction();
        return entityManager;
    }

    @Override
    public <T> T merge(final T entity) {
        if (entity != null) {
            final EntityManager em = getEntityManager();
            return em.merge(entity);
        }
        return null;
    }

    protected byte[] buildBDMJar(final byte[] zip) throws SBusinessDataRepositoryDeploymentException {
        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter();
        try {
            final BusinessObjectModel bom = converter.unzip(zip);
            final File tmpBDMDirectory = generateJavaClasses(bom);
            compileJavaClasses(tmpBDMDirectory);
            final byte[] jar = com.bonitasoft.engine.io.IOUtils.toJar(tmpBDMDirectory.getAbsolutePath());
            FileUtils.deleteDirectory(tmpBDMDirectory);
            return addPersistenceFile(jar, bom);
        } catch (final SBusinessDataRepositoryDeploymentException sbdrde) {
            throw sbdrde;
        } catch (final Exception e) {
            throw new SBusinessDataRepositoryDeploymentException(e);
        }
    }

    protected File generateJavaClasses(final BusinessObjectModel bom) throws IOException, JClassAlreadyExistsException {
        final BDMCodeGenerator codeGenerator = new BDMCodeGenerator(bom);
        final File tmpBDMDirectory = File.createTempFile("bdm", null);
        tmpBDMDirectory.delete();
        tmpBDMDirectory.mkdir();
        codeGenerator.generate(tmpBDMDirectory);
        return tmpBDMDirectory;
    }

    protected void compileJavaClasses(final File srcDirectory) throws SBusinessDataRepositoryDeploymentException {
        final Collection<File> files = FileUtils.listFiles(srcDirectory, new String[] { "java" }, true);
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        final Iterable<? extends JavaFileObject> compUnits = fileManager.getJavaFileObjectsFromFiles(files);
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        final List<String> optionList = new ArrayList<String>();
        // set compiler's classpath to be same as the runtime's
        optionList.addAll(Arrays.asList("-classpath", System.getProperty("java.class.path")));
        final Boolean compiled = compiler.getTask(null, fileManager, diagnostics, optionList, null, compUnits).call();
        if (!compiled) {
            final StringBuilder sb = new StringBuilder();
            for (final Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                sb.append(diagnostic.getMessage(Locale.getDefault()));
                sb.append("\n");
            }
            throw new SBusinessDataRepositoryDeploymentException("bdm compilation process fails:" + sb.toString());
        }
    }

    protected byte[] addPersistenceFile(final byte[] jar, final BusinessObjectModel bom) throws SBusinessDataRepositoryDeploymentException, IOException,
            TransformerException {
        final List<BusinessObject> entities = bom.getEntities();
        final List<String> classNames = new ArrayList<String>();
        for (final BusinessObject businessObject : entities) {
            classNames.add(businessObject.getQualifiedName());
        }
        final byte[] persistenceFileContent = getPersistenceFileContentFor(classNames);
        return IOUtil.addJarEntry(jar, "META-INF/persistence.xml", persistenceFileContent);
    }

}
