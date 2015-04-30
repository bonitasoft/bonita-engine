/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.bonitasoft.engine.bdm.AbstractBDMJarBuilder;
import org.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import org.bonitasoft.engine.bdm.client.ClientBDMJarBuilder;
import org.bonitasoft.engine.bdm.client.ResourcesLoader;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.server.ServerBDMJarBuilder;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.business.data.BusinessDataModelRepository;
import org.bonitasoft.engine.business.data.SBusinessDataRepositoryDeploymentException;
import org.bonitasoft.engine.business.data.SBusinessDataRepositoryException;
import org.bonitasoft.engine.business.data.impl.filter.OnlyDAOImplementationFileFilter;
import org.bonitasoft.engine.business.data.impl.filter.WithoutDAOImplementationFileFilter;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.compiler.JDTCompiler;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.dependency.SDependencyNotFoundException;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.dependency.model.builder.SDependencyBuilderFactory;
import org.bonitasoft.engine.dependency.model.builder.SDependencyMappingBuilderFactory;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.io.IOUtils;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.xml.sax.SAXException;

/**
 * @author Colin PUY
 */
public class BusinessDataModelRepositoryImpl implements BusinessDataModelRepository {

    private static final String BDR_DEPENDENCY_NAME = "BDR";

    private static final String MODEL_JAR_NAME = "bdm-model.jar";

    private static final String DAO_JAR_NAME = "bdm-dao.jar";

    private static final String BOM_NAME = "bom.zip";

    private final DependencyService dependencyService;

    private final SchemaManager schemaManager;

    private final long tenantId;

    public BusinessDataModelRepositoryImpl(final DependencyService dependencyService, final SchemaManager schemaManager, final long tenantId) {
        this.dependencyService = dependencyService;
        this.schemaManager = schemaManager;
        this.tenantId = tenantId;
    }

    @Override
    public byte[] getClientBDMZip() throws SBusinessDataRepositoryException {
        try {
            return BonitaHomeServer.getInstance().getClientBDMZip(tenantId);
        } catch (final Exception e) {
            throw new SBusinessDataRepositoryException(e);
        }
    }

    @Override
    public String getInstalledBDMVersion() throws SBusinessDataRepositoryException {
        final List<SDependency> searchBDMDependencies = searchBDMDependencies();
        if (searchBDMDependencies != null && searchBDMDependencies.size() > 0) {
            return String.valueOf(searchBDMDependencies.get(0).getId());
        }
        return null;
    }

    @Override
    public BusinessObjectModel getBusinessObjectModel() throws SBusinessDataRepositoryException {
        if (isDBMDeployed()) {
            byte[] clientBdmZip = getClientBDMZip();
            try {
                final Map<String, byte[]> zipContent = IOUtils.unzip(clientBdmZip);
                if (zipContent.containsKey(BOM_NAME)) {
                    final byte[] bomZip = zipContent.get(BOM_NAME);
                    return getBusinessObjectModel(bomZip);
                }
            } catch (IOException e) {
                throw new SBusinessDataRepositoryException(e);
            }
        }
        return null;
    }

    private List<SDependency> searchBDMDependencies() throws SBusinessDataRepositoryException {
        try {
            final QueryOptions queryOptions = new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS, asList(new OrderByOption(SDependency.class, "name",
                    OrderByType.ASC)), asList(new FilterOption(SDependency.class, "name", BDR_DEPENDENCY_NAME)), null);
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
    public String install(final byte[] bdmZip, final long tenantId) throws SBusinessDataRepositoryDeploymentException {
        final BusinessObjectModel model = getBusinessObjectModel(bdmZip);

        createClientBDMZip(tenantId, model);
        final long bdmVersion = createAndDeployServerBDMJar(tenantId, model);
        return String.valueOf(bdmVersion);
    }

    protected long createAndDeployServerBDMJar(final long tenantId, final BusinessObjectModel model) throws SBusinessDataRepositoryDeploymentException {
        final byte[] serverBdmJar = generateServerBDMJar(model);
        final SDependency sDependency = createSDependency(tenantId, serverBdmJar);
        try {
            dependencyService.createDependency(sDependency);
            final SDependencyMapping sDependencyMapping = createDependencyMapping(tenantId, sDependency);
            dependencyService.createDependencyMapping(sDependencyMapping);
            update(model.getBusinessObjectsClassNames());
            return sDependency.getId();
        } catch (final SDependencyException e) {
            throw new SBusinessDataRepositoryDeploymentException(e);
        }
    }

    protected void update(final Set<String> annotatedClassNames) throws SBusinessDataRepositoryDeploymentException {
        final List<Exception> exceptions = schemaManager.update(annotatedClassNames);
        if (!exceptions.isEmpty()) {
            throw new SBusinessDataRepositoryDeploymentException("Upating schema fails due to: " + exceptions);
        }
    }

    private void createClientBDMZip(final long tenantId, final BusinessObjectModel model) throws SBusinessDataRepositoryDeploymentException {
        try {
            final byte[] clientBdmJar = generateClientBDMZip(model);
            BonitaHomeServer.getInstance().writeClientBDMZip(tenantId, clientBdmJar);
        } catch (final Exception e) {
            throw new SBusinessDataRepositoryDeploymentException(e);
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
        final AbstractBDMJarBuilder builder = new ServerBDMJarBuilder(compiler);
        final IOFileFilter classFileAndXmlFilefilter = new SuffixFileFilter(Arrays.asList(".class", ".xml"));
        return builder.build(model, classFileAndXmlFilefilter);
    }

    protected byte[] generateClientBDMZip(final BusinessObjectModel model) throws SBusinessDataRepositoryDeploymentException, IOException {
        final JDTCompiler compiler = new JDTCompiler();
        AbstractBDMJarBuilder builder = new ClientBDMJarBuilder(compiler, new ResourcesLoader());

        final Map<String, byte[]> resources = new HashMap<String, byte[]>();
        // Build jar with Model
        final byte[] modelJarContent = builder.build(model, new WithoutDAOImplementationFileFilter());
        resources.put(MODEL_JAR_NAME, modelJarContent);

        // Build jar with DAO
        builder = new ClientBDMJarBuilder(compiler, new ResourcesLoader());
        final byte[] daoJarContent = builder.build(model, new OnlyDAOImplementationFileFilter());
        resources.put(DAO_JAR_NAME, daoJarContent);

        //Add bom.xml
        try {
            resources.put(BOM_NAME, new BusinessObjectModelConverter().zip(model));
        } catch (final JAXBException e) {
            throw new SBusinessDataRepositoryDeploymentException(e);
        } catch (final SAXException e) {
            throw new SBusinessDataRepositoryDeploymentException(e);
        }

        //Client DAO impl dependencies
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = BusinessDataModelRepositoryImpl.class.getResourceAsStream("/javassist-3.18.1-GA.jar.res");
            resources.put("javassist-3.18.1-GA.jar", IOUtil.getAllContentFrom(resourceAsStream));
        } finally {
            if (resourceAsStream != null) {
                resourceAsStream.close();
            }
        }

        return IOUtil.generateZip(resources);
    }

    protected SDependency createSDependency(final long tenantId, final byte[] transformedBdrArchive) {
        return BuilderFactory.get(SDependencyBuilderFactory.class)
                .createNewInstance(BDR_DEPENDENCY_NAME, tenantId, ScopeType.TENANT, BDR_DEPENDENCY_NAME + ".jar", transformedBdrArchive).done();
    }

    protected SDependencyMapping createDependencyMapping(final long tenantId, final SDependency sDependency) {
        return BuilderFactory.get(SDependencyMappingBuilderFactory.class).createNewInstance(sDependency.getId(), tenantId, ScopeType.TENANT).done();
    }

    @Override
    public void uninstall(final long tenantId) throws SBusinessDataRepositoryException {
        try {
            dependencyService.deleteDependency(BDR_DEPENDENCY_NAME);
        } catch (final SDependencyNotFoundException sde) {
            // do nothing
        } catch (final SDependencyException sde) {
            throw new SBusinessDataRepositoryException(sde);
        }
        try {
            BonitaHomeServer.getInstance().removeBDMZip(tenantId);
        } catch (Exception e) {
            throw new SBusinessDataRepositoryException(e);
        }
    }

    @Override
    public void dropAndUninstall(final long tenantId) throws SBusinessDataRepositoryException {
        final URL resource = Thread.currentThread().getContextClassLoader().getResource("bom.xml");
        if (resource != null) {
            try {
                final byte[] content = IOUtil.getAllContentFrom(resource);
                final URL xsd = BusinessObjectModel.class.getResource("/bom.xsd");
                final BusinessObjectModel model = IOUtils.unmarshallXMLtoObject(content, BusinessObjectModel.class, xsd);
                final List<Exception> exceptions = schemaManager.drop(model.getBusinessObjectsClassNames());
                if (!exceptions.isEmpty()) {
                    throw new SBusinessDataRepositoryDeploymentException("Upating schema fails due to: " + exceptions);
                }
                uninstall(tenantId);
            } catch (final IOException ioe) {
                throw new SBusinessDataRepositoryException(ioe);
            } catch (final JAXBException jaxbe) {
                throw new SBusinessDataRepositoryException(jaxbe);
            } catch (final SAXException saxe) {
                throw new SBusinessDataRepositoryException(saxe);
            }
        }
    }

}
