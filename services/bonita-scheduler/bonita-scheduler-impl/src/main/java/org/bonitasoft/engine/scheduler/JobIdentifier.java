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
package org.bonitasoft.engine.scheduler;

import java.io.Serializable;

/**
 * @author Baptiste Mesta
 */
public class JobIdentifier implements Serializable {

    private static final long serialVersionUID = 5950749851853932753L;

    private final long id;

    private final long tenantId;

    private final String jobName;

    public JobIdentifier(final long jobId, final long tenantId, final String jobName) {
        id = jobId;
        this.tenantId = tenantId;
        this.jobName = jobName;
    }

    public long getId() {
        return id;
    }

    public long getTenantId() {
        return tenantId;
    }

    public String getJobName() {
        return jobName;
    }

    @Override
    public String toString() {
        return "jobName=" + jobName + ";jobId=" + id + ";tenantId=" + tenantId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ id >>> 32);
        result = prime * result + (jobName == null ? 0 : jobName.hashCode());
        result = prime * result + (int) (tenantId ^ tenantId >>> 32);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JobIdentifier other = (JobIdentifier) obj;
        if (id != other.id) {
            return false;
        }
        if (jobName == null) {
            if (other.jobName != null) {
                return false;
            }
        } else if (!jobName.equals(other.jobName)) {
            return false;
        }
        if (tenantId != other.tenantId) {
            return false;
        }
        return true;
    }

}
