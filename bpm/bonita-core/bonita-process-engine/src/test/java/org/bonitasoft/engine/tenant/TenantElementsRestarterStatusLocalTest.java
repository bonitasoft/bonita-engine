/**
 * Copyright (C) 2019 BonitaSoft S.A.
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
package org.bonitasoft.engine.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class TenantElementsRestarterStatusLocalTest {

    private TenantElementsRestarterStatusLocal tenantElementsRestarterStatusLocal = new TenantElementsRestarterStatusLocal();

    @Test
    public void should_say_to_restart_elements_initially() {

        boolean shouldRestartElements = tenantElementsRestarterStatusLocal.shouldRestartElements();

        assertThat(shouldRestartElements).isTrue();
    }

    @Test
    public void should_say_to_not_restart_elements_after_restart_was_completed() {
        tenantElementsRestarterStatusLocal.notifyElementsAreRestarted();

        boolean shouldRestartElements = tenantElementsRestarterStatusLocal.shouldRestartElements();

        assertThat(shouldRestartElements).isFalse();
    }

    @Test
    public void should_say_to_restart_elements_after_restart_was_completed_but_tenant_was_stopped() {
        tenantElementsRestarterStatusLocal.notifyElementsAreRestarted();
        tenantElementsRestarterStatusLocal.stop();

        boolean shouldRestartElements = tenantElementsRestarterStatusLocal.shouldRestartElements();

        assertThat(shouldRestartElements).isTrue();
    }

    @Test
    public void should_say_to_restart_elements_after_restart_was_completed_but_tenant_was_paused() {
        tenantElementsRestarterStatusLocal.notifyElementsAreRestarted();
        tenantElementsRestarterStatusLocal.pause();

        boolean shouldRestartElements = tenantElementsRestarterStatusLocal.shouldRestartElements();

        assertThat(shouldRestartElements).isTrue();
    }

}