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
package org.bonitasoft.engine.platform.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author Celine Souchet
 */
public class STenantTest {

    @Test
    public final void isTenantActivated() {
        final STenant tenant = buildTenant("ACTIVATED");

        assertTrue(tenant.isActivated());
    }

    @Test
    public final void isNotTenantActivated() {
        final STenant tenant = buildTenant("DEACTIVATED");

        assertFalse(tenant.isActivated());
    }

    @Test
    public final void should_isTenantActivated_return_false_when_tenant_is_paused() {
        final STenant tenant = buildTenant("PAUSED");

        assertFalse(tenant.isActivated());
    }

    private STenant buildTenant(final String status) {
        return buildTenant(45, "tenant", "me", 1567l, status, false);
    }

    private STenant buildTenant(final long id, final String name, final String createdBy, final long created,
            final String status,
            final boolean defaultTenant) {
        final STenant tenant = new STenant(name, createdBy, created, status, defaultTenant);
        tenant.setId(id);
        return tenant;
    }

}
