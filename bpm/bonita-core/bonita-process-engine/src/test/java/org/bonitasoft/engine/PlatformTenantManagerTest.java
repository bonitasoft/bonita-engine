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
package org.bonitasoft.engine;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.platform.PlatformNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * 
 * @author Baptiste Mesta
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class PlatformTenantManagerTest {

    @Mock
    private PlatformAPI platformAPI;

    private PlatformTenantManager platformTenantManager;

    @Before
    public void before() {
        platformTenantManager = new PlatformTenantManager();
    }

    @Test
    public void testCreatePlatform() throws Exception {
        when(platformAPI.isPlatformCreated()).thenReturn(false);
        platformTenantManager.createPlatform(platformAPI);
        verify(platformAPI, times(1)).createAndInitializePlatform();
    }

    @Test
    public void testCreatePlatformIfCreated() throws Exception {
        when(platformAPI.isPlatformCreated()).thenReturn(true);
        platformTenantManager.createPlatform(platformAPI);
        verify(platformAPI, times(0)).createAndInitializePlatform();
    }

    @Test
    public void testStartPlatform() throws Exception {
        when(platformAPI.isPlatformCreated()).thenReturn(true);
        platformTenantManager.startPlatform(platformAPI);
        verify(platformAPI, times(1)).startNode();
    }

    @Test(expected = PlatformNotFoundException.class)
    public void testStartPlatformWhenNotCreated() throws Exception {
        when(platformAPI.isPlatformCreated()).thenReturn(false);
        platformTenantManager.startPlatform(platformAPI);
    }

    @Test
    public void testStopPlatform() throws Exception {
        when(platformAPI.isPlatformCreated()).thenReturn(true);
        platformTenantManager.stopPlatform(platformAPI);
        verify(platformAPI, times(1)).stopNode();
    }

}
