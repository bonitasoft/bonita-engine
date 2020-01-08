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
package org.bonitasoft.engine.scheduler.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SJobDescriptor implements PersistentObject {

    public static final String JOB_NAME = "jobName";
    public static final String JOB_CLASS_NAME = "jobClassName";
    public static final String ID = "id";
    public static final String DESCRIPTION = "description";
    private long id;
    private long tenantId;
    private String jobClassName;
    private String jobName;
    private String description;
    private boolean disallowConcurrentExecution;

    public SJobDescriptor(final String jobClassName, final String jobName, final String description,
            final boolean disallowConcurrentExecution) {
        super();
        this.jobClassName = jobClassName;
        this.jobName = jobName;
        this.description = description;
        this.disallowConcurrentExecution = disallowConcurrentExecution;
    }

    public SJobDescriptor(final String jobClassName, final String jobName, final boolean disallowConcurrentExecution) {
        super();
        this.jobClassName = jobClassName;
        this.jobName = jobName;
        this.disallowConcurrentExecution = disallowConcurrentExecution;
    }

    public boolean disallowConcurrentExecution() {
        return disallowConcurrentExecution;
    }

}
