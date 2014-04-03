/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.data.impl;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.dependency.SDependencyNotFoundException;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.dependency.model.builder.SDependencyBuilderFactory;
import org.bonitasoft.engine.dependency.model.builder.SDependencyMappingBuilderFactory;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.QueryOptions;

import com.bonitasoft.engine.bdm.AbstractBDMJarBuilder;
import com.bonitasoft.engine.bdm.BusinessObjectModel;
import com.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import com.bonitasoft.engine.bdm.client.ClientBDMJarBuilder;
import com.bonitasoft.engine.bdm.server.ServerBDMJarBuilder;
import com.bonitasoft.engine.business.data.BusinessDataModelRepository;
import com.bonitasoft.engine.business.data.SBusinessDataRepositoryDeploymentException;
import com.bonitasoft.engine.business.data.SBusinessDataRepositoryException;
import com.bonitasoft.engine.business.data.impl.filter.OnlyDAOFileFilter;
import com.bonitasoft.engine.business.data.impl.filter.WithoutDAOFileFilter;
import com.bonitasoft.engine.compiler.JDTCompiler;

/**
 * @author Colin PUY
 */
public class BusinessDataModelRepositoryImpl implements BusinessDataModelRepository {

    private static final String BDR_DEPENDENCY_NAME = "BDR";

    private static final String CLIENT_BDM_ZIP_NAME = "client-bdm.zip";

    private static final String MODEL_JAR_NAME = "bdm-model.jar";

    private static final String DAO_JAR_NAME = "bdm-dao.jar";

    private final DependencyService dependencyService;

    private final SchemaUpdater schemaUpdater;

    private final String compilationPath;

    private String clientStoragePath;

    public BusinessDataModelRepositoryImpl(final DependencyService dependencyService, final SchemaUpdater schemaUpdater, final String compilationPath,
            final String clientStoragePath) {
        this.dependencyService = dependencyService;
        this.schemaUpdater = schemaUpdater;
        this.compilationPath = compilationPath;
        this.clientStoragePath = clientStoragePath;
    }

    @Override
    public byte[] getClientBDMZip() throws SBusinessDataRepositoryException {
        final File clientBDMJarFile = getClientBDMZipFile();
        if (clientBDMJarFile.exists()) {
            try {
                return IOUtil.getAllContentFrom(clientBDMJarFile);
            } catch (IOException e) {
                throw new SBusinessDataRepositoryException(e);
            }
        }
        throw new SBusinessDataRepositoryException(new FileNotFoundException(clientBDMJarFile.getAbsolutePath()));
    }

    private File getClientBDMZipFile() {
        return new File(clientStoragePath, CLIENT_BDM_ZIP_NAME);
    }

    private List<SDependency> searchBDMDependencies() throws SBusinessDataRepositoryException {
        try {
            final QueryOptions queryOptions = new QueryOptions(asList(new FilterOption(SDependency.class, "name", BDR_DEPENDENCY_NAME)), null);
            return dependencyService.getDependencies(queryOptions);
        } catch (final SDependencyException e) {
            throw new SBusinessDataRepositoryException(e);
        }
    }

    @Override
    public boolean isDBMDeployed() {
        try {
            final List<SDependency> dependencies = searchBDMDependencies();
            return !dependencies.isEmpty();
        } catch (final SBusinessDataRepositoryException e) {
            return false;
        }
    }

    @Override
    public void deploy(final byte[] bdmZip, final long tenantId) throws SBusinessDataRepositoryDeploymentException {
        final BusinessObjectModel model = getBusinessObjectModel(bdmZip);

        createClientBDMZip(model);
        createAndDeployServerBDMJar(tenantId, model);
    }

    protected void createAndDeployServerBDMJar(final long tenantId, final BusinessObjectModel model) throws SBusinessDataRepositoryDeploymentException {
        final byte[] serverBdmJar = generateServerBDMJar(model);
        final SDependency sDependency = createSDependency(tenantId, serverBdmJar);
        try {
            dependencyService.createDependency(sDependency);
            final SDependencyMapping sDependencyMapping = createDependencyMapping(tenantId, sDependency);
            dependencyService.createDependencyMapping(sDependencyMapping);
            updateSchema(model.getBusinessObjectsClassNames());
        } catch (final SDependencyException e) {
            throw new SBusinessDataRepositoryDeploymentException(e);
        }
    }

    private void createClientBDMZip(final BusinessObjectModel model) throws SBusinessDataRepositoryDeploymentException {
        byte[] clientBdmJar = null;
        try {
            clientBdmJar = generateClientBDMZip(model);
        } catch (IOException e) {
            throw new SBusinessDataRepositoryDeploymentException(e);
        }
        File clientBDMJarFile = getClientBDMZipFile();
        if (clientBDMJarFile.exists()) {
            clientBDMJarFile.delete();
        }
        try {
            IOUtil.write(clientBDMJarFile, clientBdmJar);
        } catch (IOException e1) {
            throw new SBusinessDataRepositoryDeploymentException(e1);
        }
    }

    protected BusinessObjectModel getBusinessObjectModel(final byte[] bdmZip) throws SBusinessDataRepositoryDeploymentException {
        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter();
        try {
            return converter.unzip(bdmZip);
        } catch (final Exception e) {
            throw new SBusinessDataRepositoryDeploymentException("Unable to get business object model", e);
        }
    }

    protected byte[] generateServerBDMJar(final BusinessObjectModel model) throws SBusinessDataRepositoryDeploymentException {
        final JDTCompiler compiler = new JDTCompiler();
        final AbstractBDMJarBuilder builder = new ServerBDMJarBuilder(compiler, compilationPath);
        IOFileFilter classFileAndXmlFilefilter = new SuffixFileFilter(Arrays.asList(".class", ".xml"));
        return builder.build(model, classFileAndXmlFilefilter);
    }

    protected byte[] generateClientBDMZip(final BusinessObjectModel model) throws SBusinessDataRepositoryDeploymentException, IOException {
        final JDTCompiler compiler = new JDTCompiler();
        final AbstractBDMJarBuilder builder = new ClientBDMJarBuilder(compiler, compilationPath);

        Map<String, byte[]> resources = new HashMap<String, byte[]>();
        // Build jar with Model
        byte[] modelJarContent = builder.build(model, new WithoutDAOFileFilter());
        resources.put(MODEL_JAR_NAME, modelJarContent);

        // Build jar with DAO
        byte[] daoJarContent = builder.build(model, new OnlyDAOFileFilter());
        resources.put(DAO_JAR_NAME, daoJarContent);

        return IOUtil.generateZip(resources);
    }

    protected SDependency createSDependency(final long tenantId, final byte[] transformedBdrArchive) {
        return BuilderFactory.get(SDependencyBuilderFactory.class)
                .createNewInstance(BDR_DEPENDENCY_NAME, tenantId, ScopeType.TENANT, BDR_DEPENDENCY_NAME + ".jar", transformedBdrArchive).done();
    }

    protected SDependencyMapping createDependencyMapping(final long tenantId, final SDependency sDependency) {
        return BuilderFactory.get(SDependencyMappingBuilderFactory.class).createNewInstance(sDependency.getId(), tenantId, ScopeType.TENANT).done();
    }

    protected void updateSchema(final Set<String> annotatedClassNames) throws SBusinessDataRepositoryDeploymentException {
        schemaUpdater.execute(annotatedClassNames);
        final List<Exception> updateExceptions = schemaUpdater.getExceptions();
        if (!updateExceptions.isEmpty()) {
            throw new SBusinessDataRepositoryDeploymentException("Upating schema fails due to: " + updateExceptions);
        }
    }

    @Override
    public void undeploy(final long tenantId) throws SBusinessDataRepositoryException {
        try {
            dependencyService.deleteDependency(BDR_DEPENDENCY_NAME);
        } catch (final SDependencyNotFoundException sde) {
            // do nothing
        } catch (final SDependencyException sde) {
            throw new SBusinessDataRepositoryException(sde);
        }
        getClientBDMZipFile().delete();
    }

}
