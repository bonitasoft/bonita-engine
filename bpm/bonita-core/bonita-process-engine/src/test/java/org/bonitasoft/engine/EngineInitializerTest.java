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
package org.bonitasoft.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.event.PlatformStartedEvent;
import org.bonitasoft.engine.platform.PlatformNotFoundException;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.platform.session.model.SPlatformSession;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

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
    private PlatformServiceAccessor platformService;
    @Mock
    private PlatformSessionService platformSessionService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private SPlatformSession sPlatformSession;

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Before
    public void before() throws Exception {
        doReturn(platformAPI).when(engineInitializer).getPlatformAPI();
        doReturn(sessionAccessor).when(engineInitializer).getSessionAccessor();
        doReturn(serviceAccessorFactory).when(engineInitializer).getServiceAccessorFactory();
        doReturn(platformService).when(engineInitializer).getPlatformService();
        doReturn(applicationEventPublisher).when(platformService).getApplicationEventPublisher();
        doReturn(platformSessionService).when(platformService).getPlatformSessionService();
        doReturn(sPlatformSession).when(platformSessionService).createSession(anyString());
    }

    @Test
    public void initializeEngine_should_start_node() throws Exception {
        // given
        doReturn(true).when(platformAPI).isPlatformCreated();
        doReturn(true).when(platformAPI).isPlatformInitialized();
        systemOutRule.clearLog();
        final PlatformStartedEvent platformStartEvent = new PlatformStartedEvent();

        // when
        engineInitializer.initializeEngine();

        //then
        verify(platformAPI).startNode();
        assertThat(systemOutRule.getLog()).contains("Initialization of Bonita Engine done!");
        verify(applicationEventPublisher).publishEvent(platformStartEvent);
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
    public void should_not_start_node_when_platform_is_not_initialized() throws Exception {
        //given
        doReturn(true).when(platformAPI).isPlatformCreated();
        doReturn(false).when(platformAPI).isPlatformInitialized();
        //when
        final Throwable throwable = catchThrowable(() -> engineInitializer.initializeEngine());
        //then
        assertThat(throwable).isInstanceOf(PlatformNotFoundException.class);
        verify(platformAPI, never()).startNode();
    }

    @Test
    public void testUnloadEngine() throws Exception {
        doReturn(true).when(platformAPI).isNodeStarted();

        engineInitializer.unloadEngine();

        verify(platformAPI).stopNode();
        verify(serviceAccessorFactory).destroyAccessors();
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

        verify(serviceAccessorFactory).destroyAccessors();
    }

}
