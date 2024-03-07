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
package org.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.tenant.TenantResourceType.BDM;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.api.impl.resolver.BusinessArchiveArtifactsManager;
import org.bonitasoft.engine.business.data.*;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.resources.STenantResourceLight;
import org.bonitasoft.engine.resources.STenantResourceState;
import org.bonitasoft.engine.resources.TenantResourceType;
import org.bonitasoft.engine.resources.TenantResourcesService;
import org.bonitasoft.engine.service.ServiceAccessor;
import org.bonitasoft.engine.tenant.TenantResource;
import org.bonitasoft.engine.tenant.TenantStateManager;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 */
@RunWith(MockitoJUnitRunner.class)
public class TenantAdministrationAPIImplTest {

    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();
    @Mock
    private TenantResourcesService tenantResourcesService;
    @Mock
    private ServiceAccessor serviceAccessor;
    @Mock
    private BusinessArchiveArtifactsManager businessArchiveArtifactsManager;
    @Mock
    private TenantStateManager tenantStateManager;
    @Mock
    private UserTransactionService userTransactionService;

    @Spy
    @InjectMocks
    private TenantAdministrationAPIImpl tenantManagementAPI;

    @Before
    public void before() throws Exception {
        doReturn(serviceAccessor).when(tenantManagementAPI).getServiceAccessorNoException();
        doReturn(serviceAccessor).when(tenantManagementAPI).getServiceAccessor();
        doReturn(tenantResourcesService).when(serviceAccessor).getTenantResourcesService();
        doReturn(userTransactionService).when(serviceAccessor).getUserTransactionService();

        doAnswer(invocation -> ((Callable) invocation.getArgument(0)).call()).when(userTransactionService)
                .executeInTransaction(any());

        when(serviceAccessor.getBusinessArchiveArtifactsManager()).thenReturn(businessArchiveArtifactsManager);
        when(serviceAccessor.getTenantStateManager()).thenReturn(tenantStateManager);
    }

    @Test
    public void resume_should_have_annotation_available_when_tenant_is_paused() throws Exception {
        final Method method = TenantAdministrationAPIImpl.class.getMethod("resume");

        final boolean present = method.isAnnotationPresent(AvailableInMaintenanceMode.class)
                || TenantAdministrationAPIImpl.class.isAnnotationPresent(AvailableInMaintenanceMode.class);

        assertThat(present).as(
                "Annotation @AvailableWhenTenantIsPaused should be present on API method 'resume' or directly on class TenantManagementAPIExt")
                .isTrue();
    }

    @Test
    public void pause_should_have_annotation_available_when_tenant_is_paused() throws Exception {
        final Method method = TenantAdministrationAPIImpl.class.getMethod("pause");

        final boolean present = method.isAnnotationPresent(AvailableInMaintenanceMode.class)
                || TenantAdministrationAPIImpl.class.isAnnotationPresent(AvailableInMaintenanceMode.class);

        assertThat(present).as(
                "Annotation @AvailableWhenTenantIsPaused should be present on API method 'pause' or directly on class TenantManagementAPIExt")
                .isTrue();
    }

    @Test
    public void pageApi_should_be_available_in_maintenance_mode() {
        // given:
        final Class<PageAPIImpl> classPageApiExt = PageAPIImpl.class;

        // then:
        assertThat(classPageApiExt.isAnnotationPresent(AvailableInMaintenanceMode.class)).as(
                "Annotation @AvailableOnMaintenanceTenant should be present on PageAPIIml");
    }

    @Test
    public void resume_should_resolve_dependencies_for_deployed_processes() throws Exception {
        tenantManagementAPI.resume();

        verify(businessArchiveArtifactsManager).resolveDependenciesForAllProcesses(serviceAccessor);
    }

    @Test
    public void installBDR_should_be_available_when_tenant_is_paused_ONLY() throws Exception {
        final Method method = TenantAdministrationAPIImpl.class.getMethod("updateBusinessDataModel", byte[].class);
        final AvailableInMaintenanceMode annotation = method.getAnnotation(AvailableInMaintenanceMode.class);

        final boolean present = annotation != null && annotation.onlyAvailableInMaintenanceMode();
        assertThat(present).as(
                "Annotation @AvailableWhenTenantIsPaused(only=true) should be present on API method 'installBusinessDataModel(byte[])'")
                .isTrue();
    }

    @Test
    public void uninstallBDR_should_be_available_when_tenant_is_paused_ONLY() throws Exception {
        final Method method = TenantAdministrationAPIImpl.class.getMethod("uninstallBusinessDataModel");
        final AvailableInMaintenanceMode annotation = method.getAnnotation(AvailableInMaintenanceMode.class);

        final boolean present = annotation != null && annotation.onlyAvailableInMaintenanceMode();
        assertThat(present).as(
                "Annotation @AvailableWhenTenantIsPaused(only=true) should be present on API method 'uninstallBusinessDataModel()'")
                .isTrue();
    }

    @Test
    public void uninstallBusinessDataModel_should_work() throws Exception {
        // Given
        final BusinessDataModelRepository repository = mock(BusinessDataModelRepository.class);
        when(serviceAccessor.getBusinessDataModelRepository()).thenReturn(repository);
        when(tenantStateManager.executeTenantManagementOperation(anyString(), any(Callable.class)))
                .thenAnswer(invocation -> {
                    Object[] args = invocation.getArguments();
                    return ((Callable) args[1]).call();
                });

        // When
        tenantManagementAPI.uninstallBusinessDataModel();

        // Then
        verify(repository).uninstall(anyLong());
    }

    @Test(expected = BusinessDataRepositoryException.class)
    public void uninstallBusinessDataModel_should_throw_BusinessDataRepositoryException() throws Exception {
        // Given
        final BusinessDataModelRepository repository = mock(BusinessDataModelRepository.class);
        when(serviceAccessor.getBusinessDataModelRepository()).thenReturn(repository);
        when(tenantStateManager.executeTenantManagementOperation(anyString(), any(Callable.class)))
                .thenAnswer(invocation -> {
                    Object[] args = invocation.getArguments();
                    return ((Callable) args[1]).call();
                });
        doThrow(new SBusinessDataRepositoryException("error")).when(repository).uninstall(anyLong());

        // When
        tenantManagementAPI.uninstallBusinessDataModel();
    }

    @Test
    public void getTenantResource_should_retrieve_resource_from_tenantResourceService() throws Exception {
        // Given
        final STenantResourceLight toBeReturned = new STenantResourceLight("some name",
                TenantResourceType.BDM, 111L, 222L, STenantResourceState.INSTALLED);
        doReturn(toBeReturned).when(tenantResourcesService).getSingleLightResource(TenantResourceType.BDM);

        // When
        tenantManagementAPI.getTenantResource(BDM);

        // Then
        verify(tenantResourcesService).getSingleLightResource(TenantResourceType.BDM);
    }

    @Test
    public void getTenantResource_should_return_NONE_if_exception() throws Exception {
        // Given
        doThrow(SBonitaReadException.class).when(tenantResourcesService)
                .getSingleLightResource(any(TenantResourceType.class));

        // When
        final TenantResource tenantResource = tenantManagementAPI.getTenantResource(BDM);

        // Then
        assertThat(tenantResource).isEqualTo(TenantResource.NONE);
    }

    @Test
    public void getBusinessDataModelResource_should_get_resource_for_BDM_type() {
        // given:
        doReturn(mock(TenantResource.class)).when(tenantManagementAPI)
                .getTenantResource(any(org.bonitasoft.engine.tenant.TenantResourceType.class));

        // when:
        tenantManagementAPI.getBusinessDataModelResource();

        // then:
        verify(tenantManagementAPI).getTenantResource(BDM);
    }

    @Test
    public void updateBDM_should_warn_when_update_fails()
            throws BusinessDataRepositoryDeploymentException, InvalidBusinessDataModelException {
        //given
        doThrow(new BusinessDataRepositoryDeploymentException(" an exception ")).when(tenantManagementAPI)
                .uninstallBusinessDataModel();
        systemOutRule.clearLog();

        //when
        try {
            tenantManagementAPI.updateBusinessDataModel("toto".getBytes());
        } catch (Exception e) {
            // normal
        }
        //then
        assertThat(systemOutRule.getLog()).contains(
                "Caught an error when installing/updating the BDM, the transaction will be reverted and the previous BDM restored");
    }
}
