/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

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

import com.bonitasoft.engine.api.TenantStatusException;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
                FakeTenantLevelAPI.class.getMethod("canAlsoBeCalledOnPausedTenant", new Class[0]), session);

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
    public void checkMethodAccessibilityOnTenantAPIShouldBePossibleOnNOTAnnotatedMethodsIfNotInPause() throws Exception {
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

    @Test
    public void tenantStatusExceptionShouldHaveGoodMessageOnPausedTenant() throws Exception {
        // Given:
        final long tenantId = 98744L;
        final APISessionImpl session = new APISessionImpl(1L, new Date(), 120L, "userName", 5487L, "aTenant", tenantId);
        final ServerAPIExt serverAPIExtSpy = spy(serverAPIExt);
        doReturn(false).when(serverAPIExtSpy).isTenantAvailable(tenantId, session);

        try {
            // when:
            serverAPIExtSpy.checkMethodAccessibility(new FakeTenantLevelAPI(), FakeTenantLevelAPI.class.getName(),
                    FakeTenantLevelAPI.class.getMethod("mustBeCalledOnRunningTenant", new Class[0]), session);
            fail("Should have thrown TenantStatusException");
        } catch (TenantStatusException e) {
            assertThat(e.getMessage()).isEqualTo("Tenant with ID " + tenantId + " is in pause, no API call on this tenant can be made for now.");
        }
    }

    @Test
    public void tenantStatusExceptionShouldHaveGoodMessageOnRunningTenant() throws Exception {
        // Given:
        final long tenantId = 98744L;
        final APISessionImpl session = new APISessionImpl(1L, new Date(), 120L, "userName", 5487L, "aTenant", tenantId);
        final ServerAPIExt serverAPIExtSpy = spy(serverAPIExt);
        doReturn(true).when(serverAPIExtSpy).isTenantAvailable(tenantId, session);
        doReturn(false).when(serverAPIExtSpy).isMethodAvailableOnRunningTenant(anyBoolean(), any(AvailableWhenTenantIsPaused.class));

        try {
            // when:
            serverAPIExtSpy.checkMethodAccessibility(new FakeTenantLevelAPI(), FakeTenantLevelAPI.class.getName(),
                    FakeTenantLevelAPI.class.getMethod("canOnlyBeCalledOnPausedTenant", new Class[0]), session);
            fail("Should have thrown TenantStatusException");
        } catch (TenantStatusException e) {
            // then:
            assertThat(e.getMessage()).isEqualTo(
                    "Tenant with ID " + tenantId
                            + " is running, method 'com.bonitasoft.engine.api.impl.FakeTenantLevelAPI.canOnlyBeCalledOnPausedTenant()' cannot be called.");
        }
    }

    @Test(expected = TenantStatusException.class)
    public void checkMethodAccessibilityOnTenantAPIShouldNotBePossibleOnNOTAnnotatedMethodsIfTenantInPause() throws Exception {
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
    public void isInAValidModeForAnActiveTenantWithAnnotationInOnlyIsInvalid() {
        when(annotation.only()).thenReturn(true);

        final boolean valid = serverAPIExt.isMethodAvailableOnRunningTenant(true, annotation);

        assertThat(valid).isFalse();
    }

    @Test
    public void isInAValidModeForAnActiveTenantWithAnnotationInNotOnlyIsValid() {
        when(annotation.only()).thenReturn(false);

        final boolean valid = serverAPIExt.isMethodAvailableOnRunningTenant(true, annotation);

        assertThat(valid).isTrue();
    }

    @Test
    public void isInAValidModeForAnActiveTenantWithoutAnnotationIsValid() {
        final boolean valid = serverAPIExt.isMethodAvailableOnRunningTenant(true, null);

        assertThat(valid).isTrue();
    }

    @Test
    public void isInAValidModeForAPausedTenantWithAnnotationInOnlyIsValid() {
        when(annotation.only()).thenReturn(true);

        final boolean valid = serverAPIExt.isMethodAvailableOnPausedTenant(false, annotation);

        assertThat(valid).isTrue();
    }

    @Test
    public void isInAValidModeForAPausedTenantWithAnnotationInNotOnlyIsValid() {
        when(annotation.only()).thenReturn(false);

        final boolean valid = serverAPIExt.isMethodAvailableOnPausedTenant(false, annotation);

        assertThat(valid).isTrue();
    }

    @Test
    public void isInAValidModeForAPausedTenantWithoutAnnotationIsInvalid() {
        final boolean valid = serverAPIExt.isMethodAvailableOnPausedTenant(false, null);

        assertThat(valid).isFalse();
    }

}
