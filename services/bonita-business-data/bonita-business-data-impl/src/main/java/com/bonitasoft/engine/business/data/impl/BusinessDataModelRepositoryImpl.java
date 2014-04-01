package com.bonitasoft.engine.business.data.impl;

import java.util.ArrayList;
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

public class BusinessDataModelRepositoryImpl implements BusinessDataModelRepository {

    private static final String BDR = "BDR";

    private DependencyService dependencyService;

    private SchemaUpdater schemaUpdater;

    public BusinessDataModelRepositoryImpl(final DependencyService dependencyService, final SchemaUpdater schemaUpdater) {
        this.dependencyService = dependencyService;
        this.schemaUpdater = schemaUpdater;
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
        if (dependencies.isEmpty()) {
            return null;
        }
        return dependencies.get(0).getValue();
    }
    
    @Override
    public boolean isDBMDeployed()  {
        byte[] dependency;
        try {
            dependency = getDeployedBDMDependency();
        } catch (SBusinessDataRepositoryException e) {
            return false;
        }
        return dependency != null && dependency.length > 0;
    }
    
    @Override
    public void deploy(final byte[] bdmZip, final long tenantId) throws SBusinessDataRepositoryDeploymentException {
        BusinessObjectModel model = getBusinessObjectModel(bdmZip);
        final byte[] bdmJar = generateBDMJar(model);
        final SDependency sDependency = createSDependency(tenantId, bdmJar);
        try {
            dependencyService.createDependency(sDependency);
            final SDependencyMapping sDependencyMapping = createDependencyMapping(tenantId, sDependency);
            dependencyService.createDependencyMapping(sDependencyMapping);
            updateSchema(model.getBusinessObjectsClassNames());
        } catch (final SDependencyException e) {
            throw new SBusinessDataRepositoryDeploymentException(e);
        }
    }

    protected void updateSchema(Set<String> annotatedClassNames) throws SBusinessDataRepositoryDeploymentException {

        schemaUpdater.execute(annotatedClassNames);

        final List<Exception> exceptions = schemaUpdater.getExceptions();
        if (!exceptions.isEmpty()) {
            throw new SBusinessDataRepositoryDeploymentException("Upating schema fails due to: " + exceptions);
        }
    }
    
    protected SDependencyMapping createDependencyMapping(final long tenantId, final SDependency sDependency) {
        return BuilderFactory.get(SDependencyMappingBuilderFactory.class).createNewInstance(sDependency.getId(), tenantId, ScopeType.TENANT).done();
    }

    protected SDependency createSDependency(final long tenantId, final byte[] transformedBdrArchive) {
        return BuilderFactory.get(SDependencyBuilderFactory.class).createNewInstance(BDR, tenantId, ScopeType.TENANT, BDR + ".jar", transformedBdrArchive)
                .done();
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
    protected byte[] generateBDMJar(BusinessObjectModel model) throws SBusinessDataRepositoryDeploymentException {
        final BDMJarBuilder builder = new BDMJarBuilder(BDMCompiler.create());
        return builder.build(model);
    }
    
    protected BusinessObjectModel getBusinessObjectModel(byte[] bdmZip) throws SBusinessDataRepositoryDeploymentException {
        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter();
        try {
            return converter.unzip(bdmZip);
        } catch (Exception e) {
            throw new SBusinessDataRepositoryDeploymentException("Unable to get business object model", e);
        }
    }
}
