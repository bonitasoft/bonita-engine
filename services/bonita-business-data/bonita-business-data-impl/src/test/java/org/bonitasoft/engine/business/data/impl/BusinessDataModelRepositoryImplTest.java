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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;

import org.bonitasoft.engine.BOMBuilder;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
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
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BusinessDataModelRepositoryImplTest {

    @Mock
    private DependencyService dependencyService;

    private BusinessDataModelRepositoryImpl businessDataModelRepository;
    @Mock
    private TenantResourcesService tenantResourcesService;

    @Before
    public void setUp() {
        businessDataModelRepository = spy(new BusinessDataModelRepositoryImpl(dependencyService, mock(SchemaManager.class), tenantResourcesService));
    }

    @Test
    public void should_createAndDeployServerBDMJar_add_dependency_on_bdm_server_jar() throws Exception {
        BusinessObjectModel bom = BOMBuilder.aBOM().build();
        doReturn("some bytes".getBytes()).when(businessDataModelRepository).generateServerBDMJar(bom);

        doReturn(mock(SDependency.class)).when(dependencyService).createMappedDependency("BDR", "some bytes".getBytes(), "BDR.jar", 1, ScopeType.TENANT);

        businessDataModelRepository.createAndDeployServerBDMJar(1, bom);

        verify(dependencyService).createMappedDependency("BDR", "some bytes".getBytes(), "BDR.jar", 1, ScopeType.TENANT);
    }

    @Test
    public void should_createAndDeployClientBDMZip_add_resource_on_tenantResourcesService() throws Exception {
        BusinessObjectModel bom = BOMBuilder.aBOM().build();
        doReturn("some bytes".getBytes()).when(businessDataModelRepository).generateClientBDMZip(bom);

        businessDataModelRepository.createAndDeployClientBDMZip(bom);

        verify(tenantResourcesService).add("client-bdm.zip", TenantResourceType.BDM ,"some bytes".getBytes());
    }

    @Test
    public void uninstall_should_delete_a_dependency() throws Exception {
        businessDataModelRepository.uninstall(45l);

        verify(dependencyService).deleteDependency("BDR");
    }

    @Test
    public void uninstall_should_ignore_exception_if_the_dependency_does_not_exist() throws Exception {
        doThrow(new SDependencyNotFoundException("error")).when(dependencyService).deleteDependency("BDR");

        businessDataModelRepository.uninstall(45l);
    }

    @Test(expected = SBusinessDataRepositoryException.class)
    public void uninstall_should_throw_an_exception_if_an_exception_occurs_during_the_dependency_deletion() throws Exception {
        doThrow(new SDependencyDeletionException("error")).when(dependencyService).deleteDependency("BDR");

        businessDataModelRepository.uninstall(45l);
    }

    @Test
    public void should_return_getBusinessObjectModel_return_null_when_no_bdm() throws Exception {
        //given
        doReturn(false).when(businessDataModelRepository).isDBMDeployed();

        //when then
        assertThat(businessDataModelRepository.getBusinessObjectModel()).isNull();
    }

    @Test
    public void should_return_getBusinessObjectModel_return_bdm() throws Exception {
        //given
        final InputStream resourceAsStream = this.getClass().getResourceAsStream("client-bdm.zip");
        final byte[] clientBDMZip = IOUtil.getAllContentFrom(resourceAsStream);

        doReturn(true).when(businessDataModelRepository).isDBMDeployed();
        doReturn(clientBDMZip).when(businessDataModelRepository).getClientBDMZip();

        //when
        final BusinessObjectModel model = businessDataModelRepository.getBusinessObjectModel();

        //then
        assertThat(model).as("should return the business model").as("should return the bom").isNotNull();

    }

    @Test(expected = SBusinessDataRepositoryException.class)
    public void should_getBusinessObjectModel_throw_exception() throws Exception {
        //given
        final InputStream resourceAsStream = this.getClass().getResourceAsStream("client-bdm.zip");
        final byte[] clientBDMZip = IOUtil.getAllContentFrom(resourceAsStream);

        doReturn(true).when(businessDataModelRepository).isDBMDeployed();
        doReturn(clientBDMZip).when(businessDataModelRepository).getClientBDMZip();
        doThrow(IOException.class).when(businessDataModelRepository).getBusinessObjectModel(any(clientBDMZip.getClass()));

        //when then exception
        businessDataModelRepository.getBusinessObjectModel();
    }

}
