/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.engine.maintenance;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.maintenance.impl.MaintenanceDetailsImpl;
import org.junit.jupiter.api.Test;

/**
 * BPA-833: <code>isMaintenanceMessageActive()</code> returns the wrapper <code>Boolean</code>, which Groovy 4
 * does not accept as a property getter, so <code>details.maintenanceMessageActive</code> would fall back to
 * reading the private field and fail on any subclass receiver. No implementation is subclassed today, so the
 * alias closes a latent trap rather than a live defect.
 */
class MaintenanceDetailsTest {

    @Test
    void getMaintenanceMessageActive_should_return_the_same_value_as_isMaintenanceMessageActive() {
        //given
        final MaintenanceDetails active = MaintenanceDetailsImpl.builder().maintenanceMessageActive(true).build();
        final MaintenanceDetails inactive = MaintenanceDetailsImpl.builder().maintenanceMessageActive(false).build();

        //when-then
        assertThat(active.getMaintenanceMessageActive()).isTrue()
                .isEqualTo(active.isMaintenanceMessageActive());
        assertThat(inactive.getMaintenanceMessageActive()).isFalse()
                .isEqualTo(inactive.isMaintenanceMessageActive());
    }

}
