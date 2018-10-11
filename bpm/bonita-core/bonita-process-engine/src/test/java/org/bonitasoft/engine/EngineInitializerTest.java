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
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import org.assertj.core.api.Assertions;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.platform.PlatformNotFoundException;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.platform.session.model.SPlatformSession;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EngineInitializerTest {

    @Spy
    @InjectMocks
    private EngineInitializer engineInitializer;
    @Mock
    private PlatformAPI platformAPI;
    @Mock
    private SessionAccessor sessionAccessor;
    @Mock
    private ServiceAccessorFactory serviceAccessorFactory;
    @Mock
    private PlatformSessionService platformSessionService;
    @Mock
    private SPlatformSession sPlatformSession;

    @Before
    public void before() throws Exception {
        doReturn(platformAPI).when(engineInitializer).getPlatformAPI();
        doReturn(sessionAccessor).when(engineInitializer).getSessionAccessor();
        doReturn(platformSessionService).when(engineInitializer).getPlatformSessionService();
        doReturn(serviceAccessorFactory).when(engineInitializer).getServiceAccessorFactory();
        doReturn(sPlatformSession).when(platformSessionService).createSession(anyString());
    }


    @Test
    public void testInitializeEngine() throws Exception {
        doReturn(true).when(platformAPI).isPlatformCreated();

        engineInitializer.initializeEngine();

        verify(platformAPI).initializePlatform();
        verify(platformAPI).startNode();
    }

    @Test
    public void should_not_start_node_when_platform_is_not_created() throws Exception {
        //given
        doReturn(false).when(platformAPI).isPlatformCreated();
        //when
        final Throwable throwable = catchThrowable(() -> engineInitializer.initializeEngine());
        //then
        assertThat(throwable).isInstanceOf(PlatformNotFoundException.class);
        verify(platformAPI, never()).startNode();
    }

    @Test
    public void should_not_initialize_platform_when_platform_is_not_created() throws Exception {
        //given
        doReturn(false).when(platformAPI).isPlatformCreated();
        //when
        final Throwable throwable = catchThrowable(() -> engineInitializer.initializeEngine());
        //then
        assertThat(throwable).isInstanceOf(PlatformNotFoundException.class);
        verify(platformAPI, never()).initializePlatform();
    }

    @Test
    public void should_not_initialize_platform_when_platform_is_already_initialized() throws Exception {
        //given
        doReturn(true).when(platformAPI).isPlatformCreated();
        doReturn(true).when(platformAPI).isPlatformInitialized();
        //when
        engineInitializer.initializeEngine();
        //then
        verify(platformAPI, never()).initializePlatform();
    }


    @Test
    public void testUnloadEngine() throws Exception {
        doReturn(true).when(platformAPI).isNodeStarted();

        engineInitializer.unloadEngine();

        verify(platformAPI).stopNode();
        verify(serviceAccessorFactory, times(1)).destroyAccessors();
    }

    @Test
    public void should_not_stop_node_if_node_is_not_started() throws Exception {
        //given
        doReturn(false).when(platformAPI).isNodeStarted();
        //when
        engineInitializer.unloadEngine();
        //then
        verify(platformAPI, never()).stopNode();
    }

    @Test
    public void should_destroy_accessor_if_exception() throws Exception {
        engineInitializer.unloadEngine();

        verify(serviceAccessorFactory, times(1)).destroyAccessors();
    }

}
