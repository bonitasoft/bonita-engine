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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.business.data.BusinessDataModelRepository;
import org.bonitasoft.engine.business.data.SBusinessDataRepositoryDeploymentException;
import org.bonitasoft.engine.business.data.SBusinessDataRepositoryException;
import org.bonitasoft.engine.business.data.generator.AbstractBDMJarBuilder;
import org.bonitasoft.engine.business.data.generator.BDMJarGenerationException;
import org.bonitasoft.engine.business.data.generator.client.ClientBDMJarBuilder;
import org.bonitasoft.engine.business.data.generator.client.ResourcesLoader;
import org.bonitasoft.engine.business.data.generator.compiler.JDTCompiler;
import org.bonitasoft.engine.business.data.generator.filter.OnlyDAOImplementationFileFilter;
import org.bonitasoft.engine.business.data.generator.filter.WithoutDAOImplementationFileFilter;
import org.bonitasoft.engine.business.data.generator.server.ServerBDMJarBuilder;
import org.bonitasoft.engine.business.data.SchemaManager;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.dependency.SDependencyNotFoundException;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.io.IOUtils;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.resources.STenantResource;
import org.bonitasoft.engine.resources.STenantResourceLight;
import org.bonitasoft.engine.resources.TenantResourceType;
import org.bonitasoft.engine.resources.TenantResourcesService;
import org.xml.sax.SAXException;

/**
 * @author Colin PUY
 */
public class BusinessDataModelRepositoryImpl implements BusinessDataModelRepository {

    private static final String BDR_DEPENDENCY_NAME = "BDR";
    public static final String BDR_DEPENDENCY_FILENAME = BDR_DEPENDENCY_NAME + ".jar";
    private static final String CLIENT_BDM_ZIP = "client-bdm.zip";

    private static final String MODEL_JAR_NAME = "bdm-model.jar";

    private static final String DAO_JAR_NAME = "bdm-dao.jar";

    private static final String BOM_NAME = "bom.zip";

    private final DependencyService dependencyService;

    private final SchemaManager schemaManager;
    private TenantResourcesService tenantResourcesService;
    private long tenantId;

    public BusinessDataModelRepositoryImpl(final DependencyService dependencyService, final SchemaManager schemaManager,
            TenantResourcesService tenantResourcesService, long tenantId) {
        this.dependencyService = dependencyService;
        this.schemaManager = schemaManager;
        this.tenantResourcesService = tenantResourcesService;
        this.tenantId = tenantId;
    }

    @Override
    public byte[] getClientBDMZip() throws SBusinessDataRepositoryException {
        STenantResource sTenantResource;
        try {
            sTenantResource = tenantResourcesService.get(TenantResourceType.BDM, CLIENT_BDM_ZIP);
        } catch (SBonitaReadException e) {
            throw new SBusinessDataRepositoryException(e);
        }
        if (sTenantResource == null) {
            throw new SBusinessDataRepositoryException("no client-bdm.zip found in tenant resources");
        }
        return sTenantResource.getContent();
    }

    @Override
    public String getInstalledBDMVersion() throws SBusinessDataRepositoryException {
        try {
            Optional returnedId = dependencyService.getIdOfDependencyOfArtifact(tenantId, ScopeType.TENANT,
                    BDR_DEPENDENCY_FILENAME);
            if (returnedId.isPresent()) {
                return String.valueOf(returnedId);
            }
        } catch (SBonitaReadException e) {
            throw new SBusinessDataRepositoryException(e);
        }
        return null;
    }

    @Override
    public BusinessObjectModel getBusinessObjectModel() throws SBusinessDataRepositoryException {
        try {
            byte[] clientBdmZip = getClientBDMZip();
            final Map<String, byte[]> zipContent = IOUtils.unzip(clientBdmZip);
            if (zipContent.containsKey(BOM_NAME)) {
                final byte[] bomZip = zipContent.get(BOM_NAME);
                return getBusinessObjectModel(bomZip);
            }
        } catch (SBusinessDataRepositoryException e) {
            if (isBDMDeployed()) {
                // This is not a problem of BDM not deployed, let exception go up:
                throw e;
            }
        } catch (IOException e) {
            throw new SBusinessDataRepositoryException(e);
        }
        return null;
    }

    @Override
    public boolean isBDMDeployed() {
        try {
            return dependencyService.getIdOfDependencyOfArtifact(tenantId, ScopeType.TENANT, BDR_DEPENDENCY_FILENAME)
                    .isPresent();
        } catch (SBonitaReadException e) {
            return false;
        }
    }

    @Override
    public String install(final byte[] bdmZip, final long tenantId, long userId)
            throws SBusinessDataRepositoryDeploymentException {
        final BusinessObjectModel model = getBusinessObjectModel(bdmZip);

        createAndDeployClientBDMZip(model, userId);
        final long bdmVersion = createAndDeployServerBDMJar(tenantId, model);
        return String.valueOf(bdmVersion);
    }

    protected long createAndDeployServerBDMJar(final long tenantId, final BusinessObjectModel model)
            throws SBusinessDataRepositoryDeploymentException {
        final byte[] serverBdmJar = generateServerBDMJar(model);
        try {
            final SDependency mappedDependency = dependencyService.createMappedDependency(BDR_DEPENDENCY_NAME, serverBdmJar,
                    BDR_DEPENDENCY_FILENAME, tenantId,
                    ScopeType.TENANT);
            dependencyService.refreshClassLoaderAfterUpdate(ScopeType.TENANT, tenantId);
            update(model.getBusinessObjectsClassNames());
            return mappedDependency.getId();
        } catch (final SDependencyException e) {
            throw new SBusinessDataRepositoryDeploymentException(e);
        }
    }

    protected void update(final Set<String> annotatedClassNames) throws SBusinessDataRepositoryDeploymentException {
        final List<Exception> exceptions = schemaManager.update(annotatedClassNames);
        if (!exceptions.isEmpty()) {
            throw new SBusinessDataRepositoryDeploymentException("Updating schema fails due to: " + exceptions);
        }
    }

    void createAndDeployClientBDMZip(final BusinessObjectModel model, long userId)
            throws SBusinessDataRepositoryDeploymentException {
        STenantResourceLight accessControl;
        try {
            accessControl = tenantResourcesService.getSingleLightResource(TenantResourceType.BDM_ACCESS_CTRL);
        } catch (SBonitaReadException e) {
            throw new SBusinessDataRepositoryDeploymentException(e);
        }
        if (accessControl != null) {
            throw new SBusinessDataRepositoryDeploymentException(
                    "A BDM Access Control file is installed. Uninstall it before deploying the BDM");
        }
        try {
            tenantResourcesService.add(CLIENT_BDM_ZIP, TenantResourceType.BDM, generateClientBDMZip(model), userId);
        } catch (IOException | SRecorderException e) {
            throw new SBusinessDataRepositoryDeploymentException(e);
        }
    }

    protected BusinessObjectModel getBusinessObjectModel(final byte[] bdmZip)
            throws SBusinessDataRepositoryDeploymentException {
        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter();
        try {
            return converter.unzip(bdmZip);
        } catch (final Exception e) {
            throw new SBusinessDataRepositoryDeploymentException("Unable to get business object model", e);
        }
    }

    protected byte[] generateServerBDMJar(final BusinessObjectModel model)
            throws SBusinessDataRepositoryDeploymentException {
        final JDTCompiler compiler = new JDTCompiler();
        final AbstractBDMJarBuilder builder = new ServerBDMJarBuilder(compiler);
        final IOFileFilter classFileAndXmlFilefilter = new SuffixFileFilter(Arrays.asList(".class", ".xml"));
        try {
            return builder.build(model, classFileAndXmlFilefilter);
        } catch (BDMJarGenerationException e) {
            throw new SBusinessDataRepositoryDeploymentException(e);
        }
    }

    protected byte[] generateClientBDMZip(final BusinessObjectModel model)
            throws SBusinessDataRepositoryDeploymentException, IOException {
        final JDTCompiler compiler = new JDTCompiler();
        AbstractBDMJarBuilder builder = new ClientBDMJarBuilder(compiler, new ResourcesLoader());

        final Map<String, byte[]> resources = new HashMap<>();

        try {
            // Build jar with Model
            byte[] modelJarContent = builder.build(model, new WithoutDAOImplementationFileFilter());
            resources.put(MODEL_JAR_NAME, modelJarContent);

            // Build jar with DAO
            builder = new ClientBDMJarBuilder(compiler, new ResourcesLoader());
            final byte[] daoJarContent = builder.build(model, new OnlyDAOImplementationFileFilter());
            resources.put(DAO_JAR_NAME, daoJarContent);
        } catch (BDMJarGenerationException e1) {
            throw new SBusinessDataRepositoryDeploymentException(e1);
        }

        //Add bom.xml
        try {
            resources.put(BOM_NAME, new BusinessObjectModelConverter().zip(model));
        } catch (final JAXBException | SAXException e) {
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

    @Override
    public void uninstall(final long tenantId) throws SBusinessDataRepositoryException {
        try {
            dependencyService.deleteDependency(BDR_DEPENDENCY_NAME);
            dependencyService.refreshClassLoaderAfterUpdate(ScopeType.TENANT, tenantId);
        } catch (final SDependencyNotFoundException sde) {
            // do nothing
        } catch (final SDependencyException sde) {
            throw new SBusinessDataRepositoryException(sde);
        }
        try {
            STenantResource clientBDMZip = tenantResourcesService.get(TenantResourceType.BDM, CLIENT_BDM_ZIP);
            if (clientBDMZip != null) {
                tenantResourcesService.remove(clientBDMZip);
            }
        } catch (SBonitaReadException | SRecorderException e) {
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
                    throw new SBusinessDataRepositoryDeploymentException("Updating schema fails due to: " + exceptions);
                }
                uninstall(tenantId);
            } catch (final IOException | JAXBException | SAXException ioe) {
                throw new SBusinessDataRepositoryException(ioe);
            }
        }
    }

}
