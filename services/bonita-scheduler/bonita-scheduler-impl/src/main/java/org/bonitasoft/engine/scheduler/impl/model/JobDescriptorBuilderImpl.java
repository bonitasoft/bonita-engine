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
package org.bonitasoft.engine.scheduler.impl.model;

import org.bonitasoft.engine.scheduler.JobDescriptorBuilder;
import org.bonitasoft.engine.scheduler.SJobDescriptor;

public class JobDescriptorBuilderImpl implements JobDescriptorBuilder {

    private static final String JOB_NAME = "jobName";

    private static final String JOB_CLASS_NAME = "jobClassName";

    private static final String ID = "id";

    private static final String DESCRIPTION = "description";

    private SJobDescriptorImpl entity;

    @Override
    public JobDescriptorBuilder createNewInstance(final String jobClassName, final String jobName) {
        entity = new SJobDescriptorImpl(jobClassName, jobName);
        return this;
    }

    @Override
    public JobDescriptorBuilder setDescription(final String description) {
        entity.setDescription(description);
        return this;
    }

    @Override
    public SJobDescriptor done() {
        return entity;
    }

    @Override
    public String getIdKey() {
        return ID;
    }

    @Override
    public String getJobClassNameKey() {
        return JOB_CLASS_NAME;
    }

    @Override
    public String getJobNameKey() {
        return JOB_NAME;
    }

    @Override
    public String getDescriptionKey() {
        return DESCRIPTION;
    }

}
