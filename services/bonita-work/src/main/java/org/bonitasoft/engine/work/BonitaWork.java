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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * 
 * @author Baptiste Mesta
 * 
 */
public abstract class BonitaWork {

    private String uuid = UUID.randomUUID().toString();

    protected long tenantId;

    private BonitaWork parentWork;

    public String getUuid() {
        return uuid;
    }

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
    public abstract CompletableFuture<Void> work(Map<String, Object> context) throws Exception;

    public abstract void handleFailure(Throwable e, Map<String, Object> context) throws Exception;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BonitaWork work = (BonitaWork) o;
        return new EqualsBuilder()
                .append(tenantId, work.tenantId)
                .append(uuid, work.uuid)
                .append(parentWork, work.parentWork)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(uuid)
                .append(tenantId)
                .append(parentWork)
                .toHashCode();
    }
}
