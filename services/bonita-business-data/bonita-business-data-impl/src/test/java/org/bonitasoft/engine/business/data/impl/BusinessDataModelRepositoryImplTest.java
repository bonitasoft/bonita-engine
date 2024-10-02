/**
 * Copyright (C) 2019 Bonitasoft S.A.
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

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.*;
import static org.bonitasoft.engine.commons.Pair.pair;
import static org.bonitasoft.engine.commons.io.IOUtil.zip;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.bonitasoft.engine.BOMBuilder;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.business.data.InvalidBusinessDataModelException;
import org.bonitasoft.engine.business.data.SBusinessDataRepositoryDeploymentException;
import org.bonitasoft.engine.business.data.SBusinessDataRepositoryException;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.SDependencyDeletionException;
import org.bonitasoft.engine.dependency.SDependencyNotFoundException;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.SPlatformProperties;
import org.bonitasoft.engine.resources.TenantResourceType;
import org.bonitasoft.engine.resources.TenantResourcesService;
import org.hibernate.tool.schema.spi.CommandAcceptanceException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(JUnitQuickcheck.class)
public class BusinessDataModelRepositoryImplTest {

    private static final long TENANT_ID = 67453L;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private DependencyService dependencyService;
    @Mock
    private ClassLoaderService classLoaderService;

    @Mock
    private TenantResourcesService tenantResourcesService;

    @Mock
    private SchemaManagerUpdate schemaManager;

    @Mock
    private PlatformService platformService;

    @Mock
    private SPlatformProperties platformProperties;

    private BusinessDataModelRepositoryImpl businessDataModelRepository;

    private ClassLoader classLoader;

    @Before
    public void setUp() {
        // store the context ClassLoader of the current thread
        classLoader = Thread.currentThread().getContextClassLoader();
        doReturn(platformProperties).when(platformService).getSPlatformProperties();
        doReturn("1.0").when(platformProperties).getPlatformVersion();
        businessDataModelRepository = spy(new BusinessDataModelRepositoryImpl(platformService, dependencyService,
                classLoaderService, schemaManager, tenantResourcesService, TENANT_ID));
    }

    @After
    public void tearDown() {
        // reset the context ClassLoader of the current thread
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    @Test
    public void should_createAndDeployServerBDMJar_add_dependency_on_bdm_server_jar() throws Exception {
        BusinessObjectModel bom = BOMBuilder.aBOM().build();
        doReturn("some bytes".getBytes()).when(businessDataModelRepository).generateServerBDMJar(bom);

        doReturn(mock(SDependency.class)).when(dependencyService).createMappedDependency("BDR", "some bytes".getBytes(),
                "BDR.jar", 1, ScopeType.TENANT);

        businessDataModelRepository.createAndDeployServerBDMJar(1, bom);

        verify(dependencyService).createMappedDependency("BDR", "some bytes".getBytes(), "BDR.jar", 1,
                ScopeType.TENANT);
    }

    @Test
    public void should_createAndDeployClientBDMZip_add_resource_on_tenantResourcesService() throws Exception {
        BusinessObjectModel bom = BOMBuilder.aBOM().build();
        doReturn("some bytes".getBytes()).when(businessDataModelRepository).generateClientBDMZip(bom);

        businessDataModelRepository.createAndDeployClientBDMZip(bom, 1154222L);

        verify(tenantResourcesService).add(eq("client-bdm.zip"), eq(TenantResourceType.BDM),
                eq("some bytes".getBytes()),
                anyLong());
    }

    @Test
    public void uninstall_should_delete_a_dependency() throws Exception {
        businessDataModelRepository.uninstall(45L);

        verify(dependencyService).deleteDependency("BDR");
    }

    @Test
    public void uninstall_should_ignore_exception_if_the_dependency_does_not_exist() throws Exception {
        doThrow(new SDependencyNotFoundException("error")).when(dependencyService).deleteDependency("BDR");

        assertThatNoException().isThrownBy(() -> businessDataModelRepository.uninstall(45L));
    }

    @Test(expected = SBusinessDataRepositoryException.class)
    public void uninstall_should_throw_an_exception_if_an_exception_occurs_during_the_dependency_deletion()
            throws Exception {
        doThrow(new SDependencyDeletionException("error")).when(dependencyService).deleteDependency("BDR");

        businessDataModelRepository.uninstall(45L);
    }

    @Test
    public void should_return_getBusinessObjectModel_return_null_when_no_bdm() throws Exception {
        //given
        doReturn(false).when(businessDataModelRepository).isBDMDeployed();

        //when then
        assertThat(businessDataModelRepository.getBusinessObjectModel()).isNull();
    }

    @Test
    public void should_return_getBusinessObjectModel_return_bdm() throws Exception {
        //given
        final InputStream resourceAsStream = this.getClass().getResourceAsStream("client-bdm.zip");
        final byte[] clientBDMZip = IOUtil.getAllContentFrom(resourceAsStream);

        doReturn(clientBDMZip).when(businessDataModelRepository).getClientBDMZip();

        //when
        final BusinessObjectModel model = businessDataModelRepository.getBusinessObjectModel();

        //then
        assertThat(model).as("should return the business model").as("should return the bom").isNotNull();

    }

    @Test(expected = SBusinessDataRepositoryDeploymentException.class)
    public void should_getBusinessObjectModel_throw_exception() throws Exception {
        //given
        final InputStream resourceAsStream = this.getClass().getResourceAsStream("client-bdm.zip");
        final byte[] clientBDMZip = IOUtil.getAllContentFrom(resourceAsStream);

        doReturn(clientBDMZip).when(businessDataModelRepository).getClientBDMZip();
        doThrow(new InvalidBusinessDataModelException(new Exception())).when(businessDataModelRepository)
                .getBusinessObjectModel(any(clientBDMZip.getClass()));

        //when then exception
        businessDataModelRepository.getBusinessObjectModel();
    }

    @Test
    public void install_should_pass_userId_to_tenantResourceService() throws Exception {
        // given:
        final BusinessObjectModel businessObjectModel = BOMBuilder.aBOM().build();
        doReturn(businessObjectModel).when(businessDataModelRepository).getBusinessObjectModel(any(byte[].class));

        final byte[] bom = "some generated bytes".getBytes();
        doReturn(bom).when(businessDataModelRepository).generateClientBDMZip(businessObjectModel);
        doReturn(9L).when(businessDataModelRepository).createAndDeployServerBDMJar(TENANT_ID, businessObjectModel);

        // when:
        final long userId = 47L;
        businessDataModelRepository.install(bom, userId);

        // then:
        verify(tenantResourcesService).add("client-bdm.zip", TenantResourceType.BDM, bom, userId);
    }

    @Test(expected = InvalidBusinessDataModelException.class)
    public void install_should_throw_an_SInvalidBusinessDataModelException_when_zip_is_invalid() throws Exception {
        // given:
        final byte[] bom = "some invalid context".getBytes();

        // when:
        businessDataModelRepository.install(bom, 47L);
    }

    @Test(expected = InvalidBusinessDataModelException.class)
    public void install_should_throw_an_SInvalidBusinessDataModelException_when_bomxml_is_invalid() throws Exception {
        // given:
        final byte[] bom = zip(pair("bom.xml", "<xml></xml>".getBytes()));

        // when:
        businessDataModelRepository.install(bom, 47L);
    }

    @Property(trials = 30)
    public void getInstalledBDMVersion_should_return_version_number(long version) throws Exception {
        // given:
        doReturn(Optional.of(version)).when(dependencyService).getIdOfDependencyOfArtifact(TENANT_ID, ScopeType.TENANT,
                BusinessDataModelRepositoryImpl.BDR_DEPENDENCY_FILENAME);

        // when:
        final String installedBDMVersion = businessDataModelRepository.getInstalledBDMVersion();

        // then:
        assertThat(installedBDMVersion).isEqualTo(String.valueOf(version));
    }

    @Test
    public void update_should_convert_exceptions_to_allow_to_see_entire_root_cause() {
        // given:
        doReturn(singletonList(new CommandAcceptanceException("Error executing DDL bla bla bla...",
                new SQLSyntaxErrorException("ORA-02275: une telle contrainte référentielle existe déjà dans la table",
                        new Exception("Root Oracle Cause")))))
                                .when(schemaManager).update(anySet());

        // when - then:
        assertThatExceptionOfType(SBusinessDataRepositoryDeploymentException.class)
                .isThrownBy(() -> businessDataModelRepository.update(new HashSet<>()))
                .withMessageContainingAll(
                        "1: org.hibernate.tool.schema.spi.CommandAcceptanceException: Error executing DDL bla bla bla...",
                        "caused by java.sql.SQLSyntaxErrorException",
                        "caused by java.lang.Exception: Root Oracle Cause");
    }

    @Test
    public void update_should_convert_all_exceptions_in_the_list() {
        // given:
        doReturn(Arrays.asList(
                new CommandAcceptanceException("Error executing DDL bla bla bla...",
                        new SQLSyntaxErrorException(
                                "ORA-02275: une telle contrainte référentielle existe déjà dans la table",
                                new Exception("Root Oracle Cause"))),
                new CommandAcceptanceException("CommandAcceptanceException bliblibli",
                        new SQLSyntaxErrorException("Hibernate error"))))
                                .when(schemaManager).update(anySet());

        // when - then:
        assertThatExceptionOfType(SBusinessDataRepositoryDeploymentException.class)
                .isThrownBy(() -> businessDataModelRepository.update(new HashSet<>()))
                .withMessageContainingAll(
                        "1: org.hibernate.tool.schema.spi.CommandAcceptanceException: Error executing DDL bla bla bla...",
                        "caused by java.lang.Exception: Root Oracle Cause",
                        "2: org.hibernate.tool.schema.spi.CommandAcceptanceException: CommandAcceptanceException bliblibli",
                        "caused by java.sql.SQLSyntaxErrorException: Hibernate error");
    }

    @Test
    public void convertExceptions_should_filter_out_empty_message_lines() {
        // given:
        final List<Exception> exceptions = singletonList(new SQLSyntaxErrorException(
                "message with trailing carriage return\n", new SQLException("syntax error")));

        // when:
        final String message = businessDataModelRepository.convertExceptions(exceptions);

        // then:
        assertThat(Arrays.asList(message.split("\n"))).doesNotContain("");
    }

    @Test
    public void isDeployedComparesBdmWithSameContent() throws Exception {
        // given:
        var bdmArchive = "fake archive content".getBytes();
        var generatedJarContent = "fake jar content".getBytes();
        when(dependencyService.getIdOfDependencyOfArtifact(TENANT_ID, ScopeType.TENANT,
                BusinessDataModelRepositoryImpl.BDR_DEPENDENCY_FILENAME))
                        .thenReturn(Optional.of(1L));
        var deployedBdm = new SDependency();
        deployedBdm.setValue_(generatedJarContent);
        when(dependencyService.getDependency(1L)).thenReturn(deployedBdm);
        doReturn(null).when(businessDataModelRepository).getBusinessObjectModel(bdmArchive);
        doReturn(generatedJarContent).when(businessDataModelRepository).generateServerBDMJar(any(), eq(false));

        // when:
        var isDeployed = businessDataModelRepository.isDeployed(bdmArchive);

        // then:
        assertThat(isDeployed).isTrue();
    }

    @Test
    public void isDeployedComparesBdmWithDifferentContent() throws Exception {
        // given:
        var bdmArchive = "fake archive content".getBytes();
        var existingJarContent = "fake existing jar content".getBytes();
        var generatedJarContent = "fake jar content".getBytes();
        when(dependencyService.getIdOfDependencyOfArtifact(TENANT_ID, ScopeType.TENANT,
                BusinessDataModelRepositoryImpl.BDR_DEPENDENCY_FILENAME))
                        .thenReturn(Optional.of(1L));
        var deployedBdm = new SDependency();
        deployedBdm.setValue_(existingJarContent);
        when(dependencyService.getDependency(1L)).thenReturn(deployedBdm);
        doReturn(null).when(businessDataModelRepository).getBusinessObjectModel(bdmArchive);
        doReturn(generatedJarContent).when(businessDataModelRepository).generateServerBDMJar(any(), eq(false));

        // when:
        var isDeployed = businessDataModelRepository.isDeployed(bdmArchive);

        // then:
        assertThat(isDeployed).isFalse();
    }

    @Test
    public void isDeployedWithoutBdmDeployed() throws Exception {
        // given:
        var bdmArchive = "fake archive content".getBytes();
        when(dependencyService.getIdOfDependencyOfArtifact(TENANT_ID, ScopeType.TENANT,
                BusinessDataModelRepositoryImpl.BDR_DEPENDENCY_FILENAME))
                        .thenReturn(Optional.empty());

        // when:
        var isDeployed = businessDataModelRepository.isDeployed(bdmArchive);

        // then:
        assertThat(isDeployed).isFalse();
    }

}
