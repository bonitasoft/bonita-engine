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
package org.bonitasoft.engine.execution.work;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.work.BonitaWork;

/**
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public class InSessionBonitaWork extends WrappingBonitaWork {

    public InSessionBonitaWork(final BonitaWork work) {
        super(work);
    }

    TenantServiceAccessor getTenantAccessor() {
        try {
            return TenantServiceSingleton.getInstance();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletableFuture<Void> work(final Map<String, Object> context) throws Exception {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final SessionAccessor sessionAccessor = tenantAccessor.getSessionAccessor();
        context.put(TENANT_ACCESSOR, tenantAccessor);
        try {
            sessionAccessor.setTenantId(getTenantId());
            return getWrappedWork().work(context);
        } finally {
            sessionAccessor.deleteTenantId();
        }
    }

    @Override
    public void handleFailure(final Throwable e, final Map<String, Object> context) throws Exception {
        TenantServiceAccessor tenantAccessor = getTenantAccessor();
        SessionAccessor sessionAccessor = tenantAccessor.getSessionAccessor();
        sessionAccessor.setTenantId(getTenantId());
        try {
            getWrappedWork().handleFailure(e, context);
        } finally {
            sessionAccessor.deleteTenantId();
        }

    }

}
