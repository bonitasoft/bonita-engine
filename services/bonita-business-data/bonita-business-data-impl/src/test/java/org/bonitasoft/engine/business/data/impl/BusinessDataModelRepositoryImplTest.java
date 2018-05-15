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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.InputStream;

import org.bonitasoft.engine.BOMBuilder;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.business.data.SBusinessDataRepositoryDeploymentException;
import org.bonitasoft.engine.business.data.SBusinessDataRepositoryException;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.SDependencyDeletionException;
import org.bonitasoft.engine.dependency.SDependencyNotFoundException;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.resources.TenantResourceType;
import org.bonitasoft.engine.resources.TenantResourcesService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BusinessDataModelRepositoryImplTest {

    private static final long TENANT_ID = 67453L;

    @Mock
    private DependencyService dependencyService;

    @Mock
    private TenantResourcesService tenantResourcesService;

    private BusinessDataModelRepositoryImpl businessDataModelRepository;

    @Before
    public void setUp() {
        businessDataModelRepository = spy(new BusinessDataModelRepositoryImpl(dependencyService,
                mock(SchemaManagerUpdate.class), tenantResourcesService, TENANT_ID));
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

        verify(tenantResourcesService).add(eq("client-bdm.zip"), eq(TenantResourceType.BDM), eq("some bytes".getBytes()),
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

        businessDataModelRepository.uninstall(45L);
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
        doThrow(SBusinessDataRepositoryDeploymentException.class).when(businessDataModelRepository)
                .getBusinessObjectModel(any(clientBDMZip.getClass()));
        doReturn(true).when(businessDataModelRepository).isBDMDeployed();

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
        doReturn(9L).when(businessDataModelRepository).createAndDeployServerBDMJar(3L, businessObjectModel);

        // when:
        final long userId = 47L;
        businessDataModelRepository.install(bom, 3L, userId);

        // then:
        verify(tenantResourcesService).add("client-bdm.zip", TenantResourceType.BDM, bom, userId);
    }

}
