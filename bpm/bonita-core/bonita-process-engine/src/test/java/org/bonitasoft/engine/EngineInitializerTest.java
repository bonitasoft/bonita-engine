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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.platform.PlatformNotFoundException;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.platform.session.model.SPlatformSession;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EngineInitializerTest {

    @Mock
    private PlatformTenantManager platformManager;
    @Mock
    private EngineInitializerProperties platformProperties;
    @Spy
    @InjectMocks
    private EngineInitializer engineInitializer;
    @Mock
    private ServiceAccessorFactory serviceAccessorFactory;

    @Before
    public void before() throws Exception {
        // static mocks

        final PlatformServiceAccessor platformServiceAccessor = mock(PlatformServiceAccessor.class);
        when(serviceAccessorFactory.createPlatformServiceAccessor()).thenReturn(platformServiceAccessor);

        // services
        when(serviceAccessorFactory.createSessionAccessor()).thenReturn(mock(SessionAccessor.class));
        final PlatformSessionService platformSessionService = mock(PlatformSessionService.class);
        when(platformServiceAccessor.getPlatformSessionService()).thenReturn(platformSessionService);

        // sessions
        when(platformSessionService.createSession(anyString())).thenReturn(mock(SPlatformSession.class));

        doReturn(serviceAccessorFactory).when(engineInitializer).getServiceAccessorFactory();
    }


    @Test
    public void testInitializeEngine() throws Exception {
        when(platformProperties.shouldCreatePlatform()).thenReturn(true);
        when(platformProperties.shouldStartPlatform()).thenReturn(true);
        engineInitializer.initializeEngine();
        verify(platformManager, times(1)).initializePlatform(any(PlatformAPI.class));
        verify(platformManager, times(1)).startPlatform(any(PlatformAPI.class));
    }

    @Test
    public void testDoNotCreatePlatform() throws Exception {
        when(platformProperties.shouldCreatePlatform()).thenReturn(false);
        when(platformProperties.shouldStartPlatform()).thenReturn(true);
        engineInitializer.initializeEngine();
        verify(platformManager, times(0)).initializePlatform(any(PlatformAPI.class));
        verify(platformManager, times(1)).startPlatform(any(PlatformAPI.class));
    }

    @Test
    public void testDoNotCreatePlatformNorStart() throws Exception {
        when(platformProperties.shouldCreatePlatform()).thenReturn(false);
        when(platformProperties.shouldStartPlatform()).thenReturn(false);
        engineInitializer.initializeEngine();
        verify(platformManager, times(0)).initializePlatform(any(PlatformAPI.class));
        verify(platformManager, times(0)).startPlatform(any(PlatformAPI.class));
    }

    @Test
    public void testUnloadEngine() throws Exception {
        when(platformProperties.shouldStopPlatform()).thenReturn(true);
        engineInitializer.unloadEngine();
        verify(platformManager, times(1)).stopPlatform(any(PlatformAPI.class));
        verify(serviceAccessorFactory, times(1)).destroyAccessors();
    }

    @Test
    public void testUnloadEnginWhenPlatformNotCreated() throws Exception {
        when(platformProperties.shouldStopPlatform()).thenReturn(true);
        doThrow(new PlatformNotFoundException("tada")).when(platformManager).stopPlatform(any(PlatformAPI.class));
        engineInitializer.unloadEngine();
        verify(serviceAccessorFactory, times(1)).destroyAccessors();
    }

    @Test
    public void testUnloadEngineDoNotStopPlatform() throws Exception {
        when(platformProperties.shouldStopPlatform()).thenReturn(false);
        engineInitializer.unloadEngine();
        verify(platformManager, times(0)).stopPlatform(any(PlatformAPI.class));
    }

    @Test
    public void getPlatformAPI_should_reuse_the_previous_instance_in_the_second_call() {
        //given
        final EngineInitializer initializer = new EngineInitializer(platformManager, platformProperties);
        final PlatformAPI firstCall = initializer.getPlatformAPI();
        assertThat(firstCall).isNotNull();

        //when
        final PlatformAPI secondCall = initializer.getPlatformAPI();

        //then
        assertThat(secondCall).isSameAs(firstCall);
    }

}
