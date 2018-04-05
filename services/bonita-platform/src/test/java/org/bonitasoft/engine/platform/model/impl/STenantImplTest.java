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
package org.bonitasoft.engine.platform.model.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author Celine Souchet
 *
 */
public class STenantImplTest {

    @Test
    public final void isTenantActivated() {
        final STenantImpl tenant = buildTenant("ACTIVATED");

        assertTrue(tenant.isActivated());
    }

    @Test
    public final void isNotTenantActivated() {
        final STenantImpl tenant = buildTenant("DEACTIVATED");

        assertFalse(tenant.isActivated());
    }

    @Test
    public final void should_isTenantActivated_return_true_when_tenant_is_paused() {
        final STenantImpl tenant = buildTenant("PAUSED");

        assertTrue(tenant.isActivated());
    }

    private STenantImpl buildTenant(final String status) {
        return buildTenant(45, "tenant", "me", 1567l, status, false);
    }

    private STenantImpl buildTenant(final long id, final String name, final String createdBy, final long created, final String status,
            final boolean defaultTenant) {
        final STenantImpl tenant = new STenantImpl(name, createdBy, created, status, defaultTenant);
        tenant.setId(id);
        return tenant;
    }

}
