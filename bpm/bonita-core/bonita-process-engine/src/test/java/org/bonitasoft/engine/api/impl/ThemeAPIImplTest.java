package org.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ThemeAPIImplTest {

    @Test
    public void shouldBeAvalableWhenTenantIsPause() throws Exception {
        assertThat(ThemeAPIImpl.class.isAnnotationPresent(AvailableWhenTenantIsPaused.class)).as("should theme api be available when tenant is paused")
                .isTrue();
    }

}
