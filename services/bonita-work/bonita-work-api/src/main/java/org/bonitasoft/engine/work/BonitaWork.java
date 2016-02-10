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
package org.bonitasoft.engine.work;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Baptiste Mesta
 * 
 */
public abstract class BonitaWork implements Runnable, Serializable {

    private static final long serialVersionUID = 1L;

    protected long tenantId;

    private BonitaWork parentWork;

    public abstract String getDescription();

    /**
     * 
     * @return
     *         how to restart the work if it fails
     */
    public String getRecoveryProcedure() {
        return "No recovery procedure.";
    }

    /**
     * Execution code of the work
     * 
     * @param context
     *            a map of context that can be filled by a work to be given to a wrapped work
     * @throws Exception
     */
    public abstract void work(Map<String, Object> context) throws Exception;

    @Override
    public void run() {
        try {
            work(new HashMap<String, Object>());
        } catch (final Exception e) {
            throw new IllegalStateException("Exception should be handled by works.", e);
        }
    }

    public abstract void handleFailure(Exception e, Map<String, Object> context) throws Exception;

    public long getTenantId() {
        if (tenantId <= 0) {
            throw new IllegalStateException("TenantId is not set !!");
        }
        return tenantId;
    }

    public void setTenantId(final long tenantId) {
        if (tenantId <= 0) {
            throw new IllegalStateException("Invalid tenantId=" + tenantId);
        }
        this.tenantId = tenantId;
    }

    public void setParent(final BonitaWork parentWork) {
        this.parentWork = parentWork;
    }

    public BonitaWork getParent() {
        return parentWork;
    }
}
