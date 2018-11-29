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
package org.bonitasoft.engine.scheduler.model.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SJobDescriptorImpl extends SPersistentObjectImpl implements SJobDescriptor {

    private String jobClassName;
    private String jobName;
    private String description;
    private boolean disallowConcurrentExecution;

    public SJobDescriptorImpl(final String jobClassName, final String jobName, final String description, final boolean disallowConcurrentExecution) {
        super();
        this.jobClassName = jobClassName;
        this.jobName = jobName;
        this.description = description;
        this.disallowConcurrentExecution = disallowConcurrentExecution;
    }

    public SJobDescriptorImpl(final String jobClassName, final String jobName, final boolean disallowConcurrentExecution) {
        super();
        this.jobClassName = jobClassName;
        this.jobName = jobName;
        this.disallowConcurrentExecution = disallowConcurrentExecution;
    }

    @Override
    public boolean disallowConcurrentExecution() {
        return disallowConcurrentExecution;
    }

}
