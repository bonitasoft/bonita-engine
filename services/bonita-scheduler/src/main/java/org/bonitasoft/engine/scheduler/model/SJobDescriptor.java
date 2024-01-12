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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@IdClass(PersistentObjectId.class)
@Table(name = "job_desc")
public class SJobDescriptor implements PersistentObject {

    public static final String JOB_NAME = "jobName";
    public static final String JOB_CLASS_NAME = "jobClassName";
    public static final String ID = "id";
    public static final String DESCRIPTION = "description";
    @Id
    private long id;
    @Id
    private long tenantId;
    private String jobClassName;
    private String jobName;
    private String description;

    public SJobDescriptor(final String jobClassName, final String jobName, final String description) {
        super();
        this.jobClassName = jobClassName;
        this.jobName = jobName;
        this.description = description;
    }

    public SJobDescriptor(final String jobClassName, final String jobName) {
        super();
        this.jobClassName = jobClassName;
        this.jobName = jobName;
    }
}
