/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
        this.id = jobId;
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

}
