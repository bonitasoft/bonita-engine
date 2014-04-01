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

import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.builder.BuilderFactory;
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

import com.bonitasoft.engine.bdm.BDMCompiler;
import com.bonitasoft.engine.bdm.BDMJarBuilder;
import com.bonitasoft.engine.bdm.BusinessObjectModel;
import com.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import com.bonitasoft.engine.business.data.BusinessDataModelRepository;
import com.bonitasoft.engine.business.data.SBusinessDataRepositoryDeploymentException;
import com.bonitasoft.engine.business.data.SBusinessDataRepositoryException;

/**
 * @author Colin PUY
 */
public class BusinessDataModelRepositoryImpl implements BusinessDataModelRepository {

    private static final String BDR_DEPENDENCY_NAME = "BDR";

    private DependencyService dependencyService;

    private SchemaUpdater schemaUpdater;

    public BusinessDataModelRepositoryImpl(final DependencyService dependencyService, final SchemaUpdater schemaUpdater) {
        this.dependencyService = dependencyService;
        this.schemaUpdater = schemaUpdater;
    }

    @Override
    public byte[] getDeployedBDMDependency() throws SBusinessDataRepositoryException {
        List<SDependency> dependencies = searchBDMDependencies();
        if (dependencies.isEmpty()) {
            return null;
        }
        return dependencies.get(0).getValue();
    }

    private List<SDependency> searchBDMDependencies() throws SBusinessDataRepositoryException {
        try {
            QueryOptions queryOptions = new QueryOptions(asList(new FilterOption(SDependency.class, "name", BDR_DEPENDENCY_NAME)), null);
            return dependencyService.getDependencies(queryOptions);
        } catch (SDependencyException e) {
            throw new SBusinessDataRepositoryException(e);
        }
    }

    @Override
    public boolean isDBMDeployed() {
        try {
            byte[] dependency = getDeployedBDMDependency();
            return dependency != null && dependency.length > 0;
        } catch (SBusinessDataRepositoryException e) {
            return false;
        }
    }

    @Override
    public void deploy(final byte[] bdmZip, final long tenantId) throws SBusinessDataRepositoryDeploymentException {
        BusinessObjectModel model = getBusinessObjectModel(bdmZip);
        byte[] bdmJar = generateBDMJar(model);
        SDependency sDependency = createSDependency(tenantId, bdmJar);
        try {
            dependencyService.createDependency(sDependency);
            SDependencyMapping sDependencyMapping = createDependencyMapping(tenantId, sDependency);
            dependencyService.createDependencyMapping(sDependencyMapping);
            updateSchema(model.getBusinessObjectsClassNames());
        } catch (SDependencyException e) {
            throw new SBusinessDataRepositoryDeploymentException(e);
        }
    }

    protected BusinessObjectModel getBusinessObjectModel(byte[] bdmZip) throws SBusinessDataRepositoryDeploymentException {
        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter();
        try {
            return converter.unzip(bdmZip);
        } catch (Exception e) {
            throw new SBusinessDataRepositoryDeploymentException("Unable to get business object model", e);
        }
    }

    protected byte[] generateBDMJar(BusinessObjectModel model) throws SBusinessDataRepositoryDeploymentException {
        final BDMJarBuilder builder = new BDMJarBuilder(BDMCompiler.create());
        return builder.build(model);
    }

    protected SDependency createSDependency(final long tenantId, final byte[] transformedBdrArchive) {
        return BuilderFactory.get(SDependencyBuilderFactory.class)
                .createNewInstance(BDR_DEPENDENCY_NAME, tenantId, ScopeType.TENANT, BDR_DEPENDENCY_NAME + ".jar", transformedBdrArchive).done();
    }

    protected SDependencyMapping createDependencyMapping(final long tenantId, final SDependency sDependency) {
        return BuilderFactory.get(SDependencyMappingBuilderFactory.class).createNewInstance(sDependency.getId(), tenantId, ScopeType.TENANT).done();
    }

    protected void updateSchema(Set<String> annotatedClassNames) throws SBusinessDataRepositoryDeploymentException {
        schemaUpdater.execute(annotatedClassNames);
        List<Exception> updateExceptions = schemaUpdater.getExceptions();
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
    }
}
