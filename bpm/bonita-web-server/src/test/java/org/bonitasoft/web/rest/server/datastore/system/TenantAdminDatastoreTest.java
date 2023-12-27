/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.datastore.system;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.TenantAdministrationAPI;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.system.TenantAdminItem;
import org.bonitasoft.web.rest.server.engineclient.TenantManagementEngineClient;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.junit.Before;
import org.junit.Test;

public class TenantAdminDatastoreTest {

    private TenantAdminDatastore tenantAdminDatastore = null;

    private TenantAdministrationAPI tenantAdministrationAPI;

    @Before
    public void setUp() throws Exception {
        tenantAdministrationAPI = mock(TenantAdministrationAPI.class);
        tenantAdminDatastore = spy(new TenantAdminDatastore(mock(APISession.class)));
        doReturn(new TenantManagementEngineClient(tenantAdministrationAPI)).when(tenantAdminDatastore)
                .getTenantManagementEngineClient();
    }

    @Test
    public void testUpdateAlreadyInMaintenance() throws Exception {
        when(tenantAdministrationAPI.isPaused()).thenReturn(true);
        final APIID apiid = APIID.makeAPIID(1L);

        final TenantAdminItem tenantAdminItem = tenantAdminDatastore.update(apiid,
                mapOf(TenantAdminItem.ATTRIBUTE_IS_PAUSED, Boolean.TRUE.toString()));

        verify(tenantAdministrationAPI, times(1)).isPaused();
        verify(tenantAdministrationAPI, times(0)).resume();
        verify(tenantAdministrationAPI, times(0)).pause();
        assertTrue(tenantAdminItem.isPaused());
    }

    @Test
    public void testUpdateAlreadyInAvailable() throws Exception {
        when(tenantAdministrationAPI.isPaused()).thenReturn(false);
        final APIID apiid = APIID.makeAPIID(1L);

        final TenantAdminItem tenantAdminItem = tenantAdminDatastore.update(apiid,
                mapOf(TenantAdminItem.ATTRIBUTE_IS_PAUSED, Boolean.FALSE.toString()));

        verify(tenantAdministrationAPI, times(1)).isPaused();
        verify(tenantAdministrationAPI, times(0)).resume();
        verify(tenantAdministrationAPI, times(0)).pause();
        assertFalse(tenantAdminItem.isPaused());
    }

    @Test
    public void testUpdateGoInMaintenance() throws Exception {
        when(tenantAdministrationAPI.isPaused()).thenReturn(false);
        final APIID apiid = APIID.makeAPIID(1L);

        final TenantAdminItem tenantAdminItem = tenantAdminDatastore.update(apiid,
                mapOf(TenantAdminItem.ATTRIBUTE_IS_PAUSED, Boolean.TRUE.toString()));

        verify(tenantAdministrationAPI, times(1)).isPaused();
        verify(tenantAdministrationAPI, times(1)).pause();
        assertTrue(tenantAdminItem.isPaused());
    }

    @Test
    public void testUpdateGoInAvailable() throws Exception {
        when(tenantAdministrationAPI.isPaused()).thenReturn(true);
        final APIID apiid = APIID.makeAPIID(1L);

        final TenantAdminItem tenantAdminItem = tenantAdminDatastore.update(apiid,
                mapOf(TenantAdminItem.ATTRIBUTE_IS_PAUSED, Boolean.FALSE.toString()));

        verify(tenantAdministrationAPI, times(1)).isPaused();
        verify(tenantAdministrationAPI, times(1)).resume();
        assertFalse(tenantAdminItem.isPaused());
    }

    @Test
    public void testGetTenantAvailable() throws Exception {
        when(tenantAdministrationAPI.isPaused()).thenReturn(false);
        final APIID apiid = APIID.makeAPIID(1L);

        final TenantAdminItem tenantAdminItem = tenantAdminDatastore.get(apiid);

        verify(tenantAdministrationAPI, times(1)).isPaused();
        verify(tenantAdministrationAPI, times(0)).resume();
        verify(tenantAdministrationAPI, times(0)).pause();
        assertFalse(tenantAdminItem.isPaused());
    }

    @Test
    public void testGetTenantInMaintenance() throws Exception {
        when(tenantAdministrationAPI.isPaused()).thenReturn(true);
        final APIID apiid = APIID.makeAPIID(1L);

        final TenantAdminItem tenantAdminItem = tenantAdminDatastore.get(apiid);

        verify(tenantAdministrationAPI, times(1)).isPaused();
        verify(tenantAdministrationAPI, times(0)).resume();
        verify(tenantAdministrationAPI, times(0)).pause();
        assertTrue(tenantAdminItem.isPaused());
    }

    private static Map<String, String> mapOf(String key, String value) {
        Map<String, String> result = new HashMap<>();
        result.put(key, value);
        return result;
    }
}
