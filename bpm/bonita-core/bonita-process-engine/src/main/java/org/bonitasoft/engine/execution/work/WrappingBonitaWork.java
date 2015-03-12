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
package org.bonitasoft.engine.execution.work;

import java.util.Map;

import org.bonitasoft.engine.work.BonitaWork;

/**
 * @author Baptiste Mesta
 */
public abstract class WrappingBonitaWork extends TenantAwareBonitaWork {

    private static final long serialVersionUID = 1L;

    private final BonitaWork wrappedWork;

    public WrappingBonitaWork(final BonitaWork wrappedWork) {
        this.wrappedWork = wrappedWork;
        wrappedWork.setParent(this);
    }

    @Override
    public String getDescription() {
        return wrappedWork.getDescription();
    }

    @Override
    public String getRecoveryProcedure() {
        return wrappedWork.getRecoveryProcedure();
    }

    public BonitaWork getWrappedWork() {
        return wrappedWork;
    }

    @Override
    public String toString() {
        return wrappedWork.toString();
    }

    @Override
    public void handleFailure(final Exception e, final Map<String, Object> context) throws Exception {
        wrappedWork.handleFailure(e, context);
    }

    @Override
    public void setTenantId(final long tenantId) {
        wrappedWork.setTenantId(tenantId);
    }

    @Override
    public long getTenantId() {
        return wrappedWork.getTenantId();
    }

}
