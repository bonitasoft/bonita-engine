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
package org.bonitasoft.engine.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class TenantElementsRestartSupervisorLocalTest {

    private TenantElementsRestartSupervisorLocal tenantElementsRestarterSupervisorLocal = new TenantElementsRestartSupervisorLocal();

    @Test
    public void should_say_to_restart_elements_initially() {

        boolean shouldRestartElements = tenantElementsRestarterSupervisorLocal.shouldRestartElements();

        assertThat(shouldRestartElements).isTrue();
    }

    @Test
    public void should_say_to_not_restart_elements_after_restart_was_completed() {
        tenantElementsRestarterSupervisorLocal.willRestartElements();

        boolean shouldRestartElements = tenantElementsRestarterSupervisorLocal.shouldRestartElements();

        assertThat(shouldRestartElements).isFalse();
    }

    @Test
    public void should_say_to_restart_elements_after_restart_was_completed_but_tenant_was_stopped() {
        tenantElementsRestarterSupervisorLocal.willRestartElements();
        tenantElementsRestarterSupervisorLocal.stop();

        boolean shouldRestartElements = tenantElementsRestarterSupervisorLocal.shouldRestartElements();

        assertThat(shouldRestartElements).isTrue();
    }

    @Test
    public void should_say_to_restart_elements_after_restart_was_completed_but_tenant_was_paused() {
        tenantElementsRestarterSupervisorLocal.willRestartElements();
        tenantElementsRestarterSupervisorLocal.pause();

        boolean shouldRestartElements = tenantElementsRestarterSupervisorLocal.shouldRestartElements();

        assertThat(shouldRestartElements).isTrue();
    }

}
