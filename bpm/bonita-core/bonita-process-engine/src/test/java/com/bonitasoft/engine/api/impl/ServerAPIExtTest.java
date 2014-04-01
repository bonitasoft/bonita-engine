package com.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.bonitasoft.engine.service.APIAccessResolver;
import org.bonitasoft.engine.session.Session;
import org.bonitasoft.engine.session.impl.APISessionImpl;
import org.bonitasoft.engine.session.impl.PlatformSessionImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.api.TenantIsPausedException;

@RunWith(MockitoJUnitRunner.class)
public class ServerAPIExtTest {

    @Mock
    private APIAccessResolver accessResolver;

    @Mock
    private AvailableWhenTenantIsPaused annotation;

    private ServerAPIExt serverAPIExt;

    @Before
    public void createServerAPI() {
        serverAPIExt = new ServerAPIExt(true, accessResolver);
    }

    @Test
    public void checkMethodAccessibilityOnTenantAPIShouldBePossibleOnAnnotatedMethods() throws Exception {
        // Given:
        final long tenantId = 54L;
        final APISessionImpl session = new APISessionImpl(1L, new Date(), 120L, "userName", 5487L, "mon_tenant", tenantId);
        final ServerAPIExt serverAPIExtSpy = spy(serverAPIExt);
        doReturn(false).when(serverAPIExtSpy).isTenantAvailable(tenantId, session);

        // When:
        serverAPIExtSpy.checkMethodAccessibility(new FakeTenantLevelAPI(), FakeTenantLevelAPI.class.getName(),
                FakeTenantLevelAPI.class.getMethod("canAlsoBeCalledOnMaintenanceTenant", new Class[0]), session);

        // no TenantModeException must be thrown. If so, test would fail.
    }

    @Test
    public void checkMethodAccessibilityOnTenantAPIShouldBePossibleOnAnnotatedAPI() throws Exception {
        // Given:
        final long tenantId = 54L;
        final APISessionImpl session = new APISessionImpl(1L, new Date(), 120L, "userName", 5487L, "mon_tenant", tenantId);
        final ServerAPIExt serverAPIExtSpy = spy(serverAPIExt);
        doReturn(false).when(serverAPIExtSpy).isTenantAvailable(tenantId, session);

        // When:
        serverAPIExtSpy.checkMethodAccessibility(new FakeTenantLevelFullyAccessibleAPI(), FakeTenantLevelFullyAccessibleAPI.class.getName(),
                FakeTenantLevelFullyAccessibleAPI.class.getMethod("aMethod", new Class[0]), session);

        // no TenantModeException must be thrown. If so, test would fail.
    }

    @Test
    public void checkMethodAccessibilityOnTenantAPIShouldBePossibleOnNOTAnnotatedMethodsIfNotInMaintenance() throws Exception {
        // Given:
        final long tenantId = 54L;
        final APISessionImpl session = new APISessionImpl(1L, new Date(), 120L, "userName", 5487L, "mon_tenant", tenantId);
        final ServerAPIExt serverAPIExtSpy = spy(serverAPIExt);
        doReturn(true).when(serverAPIExtSpy).isTenantAvailable(tenantId, session);

        // When:
        serverAPIExtSpy.checkMethodAccessibility(new FakeTenantLevelAPI(), FakeTenantLevelAPI.class.getName(),
                FakeTenantLevelAPI.class.getMethod("mustBeCalledOnRunningTenant", new Class[0]), session);

        // no TenantModeException must be thrown. If so, test would fail.
    }

    @Test(expected = TenantIsPausedException.class)
    public void checkMethodAccessibilityOnTenantAPIShouldNotBePossibleOnNOTAnnotatedMethodsIfTenantInMaintenance() throws Exception {
        // Given:
        final long tenantId = 54L;
        final APISessionImpl session = new APISessionImpl(1L, new Date(), 120L, "userName", 5487L, "mon_tenant", tenantId);
        final ServerAPIExt serverAPIExtSpy = spy(serverAPIExt);
        doReturn(false).when(serverAPIExtSpy).isTenantAvailable(tenantId, session);

        // When:
        serverAPIExtSpy.checkMethodAccessibility(new FakeTenantLevelAPI(), FakeTenantLevelAPI.class.getName(),
                FakeTenantLevelAPI.class.getMethod("mustBeCalledOnRunningTenant", new Class[0]), session);

    }

    @Test
    public void checkMethodAccessibilityOnPlatformAPIShouldNotCheckTenantAvailability() throws Exception {
        // Given:
        final Session session = new PlatformSessionImpl(1L, new Date(), 120L, "userName", 5487L);
        final ServerAPIExt serverAPIExtSpy = spy(serverAPIExt);

        // When:
        serverAPIExtSpy.checkMethodAccessibility(new FakeTenantLevelAPI(), FakeTenantLevelAPI.class.getName(),
                FakeTenantLevelAPI.class.getMethod("platformAPIMethod", new Class[0]), session);

        // Then:
        verify(serverAPIExtSpy, never()).isTenantAvailable(anyLong(), any(Session.class));
    }

    @Test
    public void isInAValidModeForAnActiveTenantWithAnnotationInOnlyIsInvalid() throws Exception {
        when(annotation.only()).thenReturn(true);

        final boolean valid = serverAPIExt.isInAValidModeFor(true, annotation);

        assertThat(valid).isFalse();
    }

    @Test
    public void isInAValidModeForAnActiveTenantWithAnnotationInNotOnlyIsValid() throws Exception {
        when(annotation.only()).thenReturn(false);

        final boolean valid = serverAPIExt.isInAValidModeFor(true, annotation);

        assertThat(valid).isTrue();
    }

    @Test
    public void isInAValidModeForAnActiveTenantWithoutAnnotationIsValid() throws Exception {
        final boolean valid = serverAPIExt.isInAValidModeFor(true, null);

        assertThat(valid).isTrue();
    }

    @Test
    public void isInAValidModeForAPausedTenantWithAnnotationInOnlyIsValid() throws Exception {
        when(annotation.only()).thenReturn(true);

        final boolean valid = serverAPIExt.isInAValidModeFor(false, annotation);

        assertThat(valid).isTrue();
    }

    @Test
    public void isInAValidModeForAPausedTenantWithAnnotationInNotOnlyIsValid() throws Exception {
        when(annotation.only()).thenReturn(false);

        final boolean valid = serverAPIExt.isInAValidModeFor(false, annotation);

        assertThat(valid).isTrue();
    }

    @Test
    public void isInAValidModeForAPausedTenantWithoutAnnotationIsInvalid() throws Exception {
        final boolean valid = serverAPIExt.isInAValidModeFor(false, null);

        assertThat(valid).isFalse();
    }

}
