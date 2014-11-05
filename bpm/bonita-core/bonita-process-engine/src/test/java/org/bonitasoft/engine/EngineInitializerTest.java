package org.bonitasoft.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.platform.PlatformNotFoundException;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.platform.session.model.SPlatformSession;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.service.impl.SpringPlatformFileSystemBeanAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ SpringPlatformFileSystemBeanAccessor.class, ServiceAccessorFactory.class })
public class EngineInitializerTest {

    private PlatformTenantManager platformManager;

    private EngineInitializerProperties platformProperties;

    private EngineInitializer engineInitializer;

    private ServiceAccessorFactory serviceAccessorFactory;

    @Before
    public void before() throws Exception {
        // static mocks
        mockStatic(ServiceAccessorFactory.class);
        mockStatic(SpringPlatformFileSystemBeanAccessor.class);

        serviceAccessorFactory = mock(ServiceAccessorFactory.class);
        when(ServiceAccessorFactory.getInstance()).thenReturn(serviceAccessorFactory);
        final PlatformServiceAccessor platformServiceAccessor = mock(PlatformServiceAccessor.class);
        when(serviceAccessorFactory.createPlatformServiceAccessor()).thenReturn(platformServiceAccessor);

        // services
        final SessionAccessor sessionAccessor = mock(SessionAccessor.class);
        when(serviceAccessorFactory.createSessionAccessor()).thenReturn(sessionAccessor);
        final PlatformSessionService platformSessionService = mock(PlatformSessionService.class);
        when(platformServiceAccessor.getPlatformSessionService()).thenReturn(platformSessionService);

        // sessions
        final SPlatformSession platformSession = mock(SPlatformSession.class);
        when(platformSessionService.createSession(anyString())).thenReturn(platformSession);

        platformManager = mock(PlatformTenantManager.class);
        platformProperties = mock(EngineInitializerProperties.class);
        engineInitializer = new EngineInitializer(platformManager, platformProperties);

    }

    @Test
    public void testInitializeEngine() throws Exception {
        when(platformProperties.shouldCreatePlatform()).thenReturn(true);
        when(platformProperties.shouldStartPlatform()).thenReturn(true);
        engineInitializer.initializeEngine();
        verify(platformManager, times(1)).createPlatform(any(PlatformAPI.class));
        verify(platformManager, times(1)).startPlatform(any(PlatformAPI.class));
    }

    @Test
    public void testDoNotCreatePlatform() throws Exception {
        when(platformProperties.shouldCreatePlatform()).thenReturn(false);
        when(platformProperties.shouldStartPlatform()).thenReturn(true);
        engineInitializer.initializeEngine();
        verify(platformManager, times(0)).createPlatform(any(PlatformAPI.class));
        verify(platformManager, times(1)).startPlatform(any(PlatformAPI.class));
    }

    @Test
    public void testDoNotCreatePlatformNorStart() throws Exception {
        when(platformProperties.shouldCreatePlatform()).thenReturn(false);
        when(platformProperties.shouldStartPlatform()).thenReturn(false);
        engineInitializer.initializeEngine();
        verify(platformManager, times(0)).createPlatform(any(PlatformAPI.class));
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
