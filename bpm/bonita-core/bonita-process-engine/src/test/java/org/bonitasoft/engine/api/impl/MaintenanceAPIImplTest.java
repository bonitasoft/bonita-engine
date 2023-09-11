/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
import static org.mockito.Mockito.*;

import org.bonitasoft.engine.maintenance.MaintenanceInfo;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.tenant.TenantStateManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MaintenanceAPIImplTest {

    public static final long TENANT_ID = 56423L;

    @Mock
    private TenantServiceAccessor serviceAccessor;
    @Mock
    private PlatformService platformService;
    @Mock
    private TenantStateManager tenantStateManager;
    @Spy
    @InjectMocks
    private MaintenanceAPIImpl maintenanceAPI;

    @Before
    public void setup() throws Exception {
        doReturn(platformService).when(serviceAccessor).getPlatformService();
        doReturn(tenantStateManager).when(serviceAccessor).getTenantStateManager();
        doReturn(serviceAccessor).when(maintenanceAPI).getServiceAccessor();
    }

    @Test
    public void get_maintenance_info_should_retrieve_from_platform_service() throws Exception {
        //given
        STenant tenant = STenant.builder().status(STenant.PAUSED).build();
        SPlatform platform = SPlatform.builder()
                .maintenanceMessage("maintenance msg")
                .maintenanceMessageActive(true)
                .build();

        doReturn(tenant).when(platformService).getDefaultTenant();
        doReturn(platform).when(platformService).getPlatform();
        //when
        MaintenanceInfo info = maintenanceAPI.getMaintenanceInfo();
        //then
        assertThat(info.getMaintenanceState()).isEqualTo(MaintenanceInfo.State.ENABLED);
        assertThat(info.getMaintenanceMessage()).isEqualTo(platform.getMaintenanceMessage());
        assertThat(info.isMaintenanceMessageActive()).isEqualTo(platform.isMaintenanceMessageActive());
    }

    @Test
    public void enable_maintenance_mode_should_update_state_manager() throws Exception {
        //given
        doNothing().when(tenantStateManager).pause();
        //when
        maintenanceAPI.enableMaintenanceMode();
        //then
        verify(tenantStateManager).pause();
    }

    @Test
    public void disable_maintenance_mode_should_update_state_manager() throws Exception {
        //given
        doNothing().when(tenantStateManager).resume();
        //when
        maintenanceAPI.disableMaintenanceMode();
        //then
        verify(tenantStateManager).resume();
    }

    @Test
    public void update_maintenance_msg_should_pass_message_to_platform_service() throws Exception {
        //given
        doNothing().when(platformService).updatePlatform(any());
        //when
        String msg = "maintenance msg";
        maintenanceAPI.updateMaintenanceMessage(msg);
        //then
        ArgumentCaptor<EntityUpdateDescriptor> captor = ArgumentCaptor.forClass(EntityUpdateDescriptor.class);;
        verify(platformService).updatePlatform(captor.capture());
        assertThat(captor.getValue().getFields().containsValue(msg));
    }

    @Test
    public void enable_maintenance_mode_msg_should_update_platform_service() throws Exception {
        //given
        doNothing().when(platformService).updatePlatform(any());
        //when
        maintenanceAPI.enableMaintenanceMessage();
        //then
        ArgumentCaptor<EntityUpdateDescriptor> captor = ArgumentCaptor.forClass(EntityUpdateDescriptor.class);;
        verify(platformService).updatePlatform(captor.capture());
        assertThat(captor.getValue().getFields().containsValue(true));
    }

    @Test
    public void disable_maintenance_mode_msg_should_update_platform_service() throws Exception {
        //given
        doNothing().when(platformService).updatePlatform(any());
        //when
        maintenanceAPI.disableMaintenanceMessage();
        //then
        ArgumentCaptor<EntityUpdateDescriptor> captor = ArgumentCaptor.forClass(EntityUpdateDescriptor.class);;
        verify(platformService).updatePlatform(captor.capture());
        assertThat(captor.getValue().getFields().containsValue(false));
    }
}
